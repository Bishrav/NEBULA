# How the Planner Uses Statistics

  

   This chapter builds on the material covered in  and  to show some
   additional details about how the planner uses the
   system statistics to estimate the number of rows each part of a query might
   return. This is a significant part of the planning process,
   providing much of the raw material for cost calculation.
  

  

   The intent of this chapter is not to document the code in detail,
   but to present an overview of how it works.
   This will perhaps ease the learning curve for someone who subsequently
   wishes to read the code.
  

 

  
# Row Estimation Examples

  
   row estimation
   planner
  

  

   The examples shown below use tables in the PostgreSQL
   regression test database.
   Note also that since ANALYZE uses random sampling
   while producing statistics, the results will change slightly after
   any new ANALYZE.
  

  

   Let's start with a very simple query:

```
EXPLAIN SELECT * FROM tenk1;

                         QUERY PLAN
-------------------------------------------------------------
 Seq Scan on tenk1  (cost=0.00..458.00 rows=10000 width=244)
```

   How the planner determines the cardinality of tenk1
   is covered in , but is repeated here for
   completeness. The number of pages and rows is looked up in
   pg_class:

```
SELECT relpages, reltuples FROM pg_class WHERE relname = 'tenk1';

 relpages | reltuples
----------+-----------
      358 |     10000
```

    These numbers are current as of the last VACUUM or
    ANALYZE on the table.  The planner then fetches the
    actual current number of pages in the table (this is a cheap operation,
    not requiring a table scan).  If that is different from
    relpages then
    reltuples is scaled accordingly to
    arrive at a current number-of-rows estimate.  In the example above, the value of
    relpages is up-to-date so the rows estimate is
    the same as reltuples.
  

  

   Let's move on to an example with a range condition in its
   WHERE clause:

```
EXPLAIN SELECT * FROM tenk1 WHERE unique1 < 1000;

                                   QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------
 Bitmap Heap Scan on tenk1  (cost=24.06..394.64 rows=1007 width=244)
   Recheck Cond: (unique1 < 1000)
   ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..23.80 rows=1007 width=0)
         Index Cond: (unique1 < 1000)
```

   The planner examines the WHERE clause condition
   and looks up the selectivity function for the operator
   < in pg_operator.
   This is held in the column oprrest,
   and the entry in this case is scalarltsel.
   The scalarltsel function retrieves the histogram for
   unique1 from
   pg_statistic.  For manual queries it is more
   convenient to look in the simpler pg_stats
   view:

```
SELECT histogram_bounds FROM pg_stats
WHERE tablename='tenk1' AND attname='unique1';

                   histogram_bounds
------------------------------------------------------
 {0,993,1997,3050,4040,5036,5957,7057,8029,9016,9995}
```

   Next the fraction of the histogram occupied by < 1000
   is worked out. This is the selectivity. The histogram divides the range
   into equal frequency buckets, so all we have to do is locate the bucket
   that our value is in and count part of it and
   all of the ones before. The value 1000 is clearly in
   the second bucket (993&ndash;1997).  Assuming a linear distribution of
   values inside each bucket, we can calculate the selectivity as:

```
selectivity = (1 + (1000 - bucket[2].min)/(bucket[2].max - bucket[2].min))/num_buckets
            = (1 + (1000 - 993)/(1997 - 993))/10
            = 0.100697
```

   that is, one whole bucket plus a linear fraction of the second, divided by
   the number of buckets. The estimated number of rows can now be calculated as
   the product of the selectivity and the cardinality of
   tenk1:

```
rows = rel_cardinality * selectivity
     = 10000 * 0.100697
     = 1007  (rounding off)
```

  

  

   Next let's consider an example with an equality condition in its
   WHERE clause:

```
EXPLAIN SELECT * FROM tenk1 WHERE stringu1 = 'CRAAAA';

                        QUERY PLAN
----------------------------------------------------------
 Seq Scan on tenk1  (cost=0.00..483.00 rows=30 width=244)
   Filter: (stringu1 = 'CRAAAA'::name)
```

   Again the planner examines the WHERE clause condition
   and looks up the selectivity function for =, which is
   eqsel.  For equality estimation the histogram is
   not useful; instead the list of most
   common values (MCVs) is used to determine the
   selectivity. Let's have a look at the MCVs, with some additional columns
   that will be useful later:

```
SELECT null_frac, n_distinct, most_common_vals, most_common_freqs FROM pg_stats
WHERE tablename='tenk1' AND attname='stringu1';

null_frac         | 0
n_distinct        | 676
most_common_vals  | {EJAAAA,BBAAAA,CRAAAA,FCAAAA,FEAAAA,GSAAAA,&zwsp;JOAAAA,MCAAAA,NAAAAA,WGAAAA}
most_common_freqs | {0.00333333,0.003,0.003,0.003,0.003,0.003,&zwsp;0.003,0.003,0.003,0.003}
```

   Since CRAAAA appears in the list of MCVs, the selectivity is
   merely the corresponding entry in the list of most common frequencies
   (MCFs):

```
selectivity = mcf[3]
            = 0.003
```

   As before, the estimated number of rows is just the product of this with the
   cardinality of tenk1:

```
rows = 10000 * 0.003
     = 30
```

  

  

   Now consider the same query, but with a constant that is not in the
   MCV list:

```
EXPLAIN SELECT * FROM tenk1 WHERE stringu1 = 'xxx';

                        QUERY PLAN
----------------------------------------------------------
 Seq Scan on tenk1  (cost=0.00..483.00 rows=15 width=244)
   Filter: (stringu1 = 'xxx'::name)
```

   This is quite a different problem: how to estimate the selectivity when the
   value is not in the MCV list.
   The approach is to use the fact that the value is not in the list,
   combined with the knowledge of the frequencies for all of the
   MCVs:

```
selectivity = (1 - sum(mcv_freqs))/(num_distinct - num_mcv)
            = (1 - (0.00333333 + 0.003 + 0.003 + 0.003 + 0.003 + 0.003 +
                    0.003 + 0.003 + 0.003 + 0.003))/(676 - 10)
            = 0.0014559
```

   That is, add up all the frequencies for the MCVs and
   subtract them from one, then
   divide by the number of other distinct values.
   This amounts to assuming that the fraction of the column that is not any
   of the MCVs is evenly distributed among all the other distinct values.
   Notice that there are no null values so we don't have to worry about those
   (otherwise we'd subtract the null fraction from the numerator as well).
   The estimated number of rows is then calculated as usual:

```
rows = 10000 * 0.0014559
     = 15  (rounding off)
```

  

  

   The previous example with unique1 < 1000 was an
   oversimplification of what scalarltsel really does;
   now that we have seen an example of the use of MCVs, we can fill in some
   more detail.  The example was correct as far as it went, because since
   unique1 is a unique column it has no MCVs (obviously, no
   value is any more common than any other value).  For a non-unique
   column, there will normally be both a histogram and an MCV list, and
   the histogram does not include the portion of the column
   population represented by the MCVs.  We do things this way because
   it allows more precise estimation.  In this situation
   scalarltsel directly applies the condition (e.g.,
   < 1000) to each value of the MCV list, and adds up the
   frequencies of the MCVs for which the condition is true.  This gives
   an exact estimate of the selectivity within the portion of the table
   that is MCVs.  The histogram is then used in the same way as above
   to estimate the selectivity in the portion of the table that is not
   MCVs, and then the two numbers are combined to estimate the overall
   selectivity.  For example, consider

```
EXPLAIN SELECT * FROM tenk1 WHERE stringu1 < 'IAAAAA';

                         QUERY PLAN
------------------------------------------------------------
 Seq Scan on tenk1  (cost=0.00..483.00 rows=3077 width=244)
   Filter: (stringu1 < 'IAAAAA'::name)
```

   We already saw the MCV information for stringu1,
   and here is its histogram:

```
SELECT histogram_bounds FROM pg_stats
WHERE tablename='tenk1' AND attname='stringu1';

                                histogram_bounds
-------------------------------------------------------------------&zwsp;-------------
 {AAAAAA,CQAAAA,FRAAAA,IBAAAA,KRAAAA,NFAAAA,PSAAAA,SGAAAA,VAAAAA,&zwsp;XLAAAA,ZZAAAA}
```

   Checking the MCV list, we find that the condition stringu1 <
   'IAAAAA' is satisfied by the first six entries and not the last four,
   so the selectivity within the MCV part of the population is

```
selectivity = sum(relevant mvfs)
            = 0.00333333 + 0.003 + 0.003 + 0.003 + 0.003 + 0.003
            = 0.01833333
```

   Summing all the MCFs also tells us that the total fraction of the
   population represented by MCVs is 0.03033333, and therefore the
   fraction represented by the histogram is 0.96966667 (again, there
   are no nulls, else we'd have to exclude them here).  We can see
   that the value IAAAAA falls nearly at the end of the
   third histogram bucket.  Using some rather cheesy assumptions
   about the frequency of different characters, the planner arrives
   at the estimate 0.298387 for the portion of the histogram population
   that is less than IAAAAA.  We then combine the estimates
   for the MCV and non-MCV populations:

```
selectivity = mcv_selectivity + histogram_selectivity * histogram_fraction
            = 0.01833333 + 0.298387 * 0.96966667
            = 0.307669

rows        = 10000 * 0.307669
            = 3077  (rounding off)
```

   In this particular example, the correction from the MCV list is fairly
   small, because the column distribution is actually quite flat (the
   statistics showing these particular values as being more common than
   others are mostly due to sampling error).  In a more typical case where
   some values are significantly more common than others, this complicated
   process gives a useful improvement in accuracy because the selectivity
   for the most common values is found exactly.
  

  

   Now let's consider a case with more than one
   condition in the WHERE clause:

```
EXPLAIN SELECT * FROM tenk1 WHERE unique1 < 1000 AND stringu1 = 'xxx';

                                   QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------
 Bitmap Heap Scan on tenk1  (cost=23.80..396.91 rows=1 width=244)
   Recheck Cond: (unique1 < 1000)
   Filter: (stringu1 = 'xxx'::name)
   ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..23.80 rows=1007 width=0)
         Index Cond: (unique1 < 1000)
```

   The planner assumes that the two conditions are independent, so that
   the individual selectivities of the clauses can be multiplied together:

```
selectivity = selectivity(unique1 < 1000) * selectivity(stringu1 = 'xxx')
            = 0.100697 * 0.0014559
            = 0.0001466

rows        = 10000 * 0.0001466
            = 1  (rounding off)
```

   Notice that the number of rows estimated to be returned from the bitmap
   index scan reflects only the condition used with the index; this is
   important since it affects the cost estimate for the subsequent heap
   fetches.
  

  

   Finally we will examine a query that involves a join:

```
EXPLAIN SELECT * FROM tenk1 t1, tenk2 t2
WHERE t1.unique1 < 50 AND t1.unique2 = t2.unique2;

                                      QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------------
 Nested Loop  (cost=4.64..456.23 rows=50 width=488)
   ->  Bitmap Heap Scan on tenk1 t1  (cost=4.64..142.17 rows=50 width=244)
         Recheck Cond: (unique1 < 50)
         ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..4.63 rows=50 width=0)
               Index Cond: (unique1 < 50)
   ->  Index Scan using tenk2_unique2 on tenk2 t2  (cost=0.00..6.27 rows=1 width=244)
         Index Cond: (unique2 = t1.unique2)
```

   The restriction on tenk1,
   unique1 < 50,
   is evaluated before the nested-loop join.
   This is handled analogously to the previous range example.  This time the
   value 50 falls into the first bucket of the
   unique1 histogram:

```
selectivity = (0 + (50 - bucket[1].min)/(bucket[1].max - bucket[1].min))/num_buckets
            = (0 + (50 - 0)/(993 - 0))/10
            = 0.005035

rows        = 10000 * 0.005035
            = 50  (rounding off)
```

   The restriction for the join is t2.unique2 = t1.unique2.
   The operator is just
   our familiar =, however the selectivity function is
   obtained from the oprjoin column of
   pg_operator, and is eqjoinsel.
   eqjoinsel looks up the statistical information for both
   tenk2 and tenk1:

```
SELECT tablename, null_frac,n_distinct, most_common_vals FROM pg_stats
WHERE tablename IN ('tenk1', 'tenk2') AND attname='unique2';

tablename  | null_frac | n_distinct | most_common_vals
-----------+-----------+------------+------------------
 tenk1     |         0 |         -1 |
 tenk2     |         0 |         -1 |
```

   In this case there is no MCV information for
   unique2 and all the values appear to be
   unique (n_distinct = -1), so we use an algorithm that relies on the row
   count estimates for both relations (num_rows, not shown, but "tenk")
   together with the column null fractions (zero for both):

```
selectivity = (1 - null_frac1) * (1 - null_frac2) / max(num_rows1, num_rows2)
            = (1 - 0) * (1 - 0) / max(10000, 10000)
            = 0.0001
```

   This is, subtract the null fraction from one for each of the relations,
   and divide by the row count of the larger relation (this value does get
   scaled in the non-unique case).
   The number of rows
   that the join is likely to emit is calculated as the cardinality of the
   Cartesian product of the two inputs, multiplied by the
   selectivity:

```
rows = (outer_cardinality * inner_cardinality) * selectivity
     = (50 * 10000) * 0.0001
     = 50
```

  

  

   Had there been MCV lists for the two columns,
   eqjoinsel would have used direct comparison of the MCV
   lists to determine the join selectivity within the part of the column
   populations represented by the MCVs.  The estimate for the remainder of the
   populations follows the same approach shown here.
  

  

   Notice that we showed inner_cardinality as 10000, that is,
   the unmodified size of tenk2.  It might appear from
   inspection of the EXPLAIN output that the estimate of
   join rows comes from 50 * 1, that is, the number of outer rows times
   the estimated number of rows obtained by each inner index scan on
   tenk2.  But this is not the case: the join relation size
   is estimated before any particular join plan has been considered.  If
   everything is working well then the two ways of estimating the join
   size will produce about the same answer, but due to round-off error and
   other factors they sometimes diverge significantly.
  

  

   For those interested in further details, estimation of the size of
   a table (before any WHERE clauses) is done in
   src/backend/optimizer/util/plancat.c. The generic
   logic for clause selectivities is in
   src/backend/optimizer/path/clausesel.c.  The
   operator-specific selectivity functions are mostly found
   in src/backend/utils/adt/selfuncs.c.
  

 

 

  
# Multivariate Statistics Examples

  
   row estimation
   multivariate
  

  

   
# Functional Dependencies

   

    Multivariate correlation can be demonstrated with a very simple data set
    &mdash; a table with two columns, both containing the same values:

```
CREATE TABLE t (a INT, b INT);
INSERT INTO t SELECT i % 100, i % 100 FROM generate_series(1, 10000) s(i);
ANALYZE t;
```

    As explained in , the planner can determine
    cardinality of t using the number of pages and
    rows obtained from pg_class:

```
SELECT relpages, reltuples FROM pg_class WHERE relname = 't';

 relpages | reltuples
----------+-----------
       45 |     10000
```

    The data distribution is very simple; there are only 100 distinct values
    in each column, uniformly distributed.
   

   

    The following example shows the result of estimating a WHERE
    condition on the a column:

```
EXPLAIN (ANALYZE, TIMING OFF, BUFFERS OFF) SELECT * FROM t WHERE a = 1;
                                 QUERY PLAN
-------------------------------------------------------------------&zwsp;------------
 Seq Scan on t  (cost=0.00..170.00 rows=100 width=8) (actual rows=100.00 loops=1)
   Filter: (a = 1)
   Rows Removed by Filter: 9900
```

    The planner examines the condition and determines the selectivity
    of this clause to be 1%.  By comparing this estimate and the actual
    number of rows, we see that the estimate is very accurate
    (in fact exact, as the table is very small).  Changing the
    WHERE condition to use the b column, an
    identical plan is generated.  But observe what happens if we apply the same
    condition on both columns, combining them with AND:

```
EXPLAIN (ANALYZE, TIMING OFF, BUFFERS OFF) SELECT * FROM t WHERE a = 1 AND b = 1;
                                 QUERY PLAN
-------------------------------------------------------------------&zwsp;----------
 Seq Scan on t  (cost=0.00..195.00 rows=1 width=8) (actual rows=100.00 loops=1)
   Filter: ((a = 1) AND (b = 1))
   Rows Removed by Filter: 9900
```

    The planner estimates the selectivity for each condition individually,
    arriving at the same 1% estimates as above.  Then it assumes that the
    conditions are independent, and so it multiplies their selectivities,
    producing a final selectivity estimate of just 0.01%.
    This is a significant underestimate, as the actual number of rows
    matching the conditions (100) is two orders of magnitude higher.
   

   

    This problem can be fixed by creating a statistics object that
    directs ANALYZE to calculate functional-dependency
    multivariate statistics on the two columns:

```
CREATE STATISTICS stts (dependencies) ON a, b FROM t;
ANALYZE t;
EXPLAIN (ANALYZE, TIMING OFF, BUFFERS OFF) SELECT * FROM t WHERE a = 1 AND b = 1;
                                  QUERY PLAN
-------------------------------------------------------------------&zwsp;------------
 Seq Scan on t  (cost=0.00..195.00 rows=100 width=8) (actual rows=100.00 loops=1)
   Filter: ((a = 1) AND (b = 1))
   Rows Removed by Filter: 9900
```

   

  

  

   
# Multivariate N-Distinct Counts

   

    A similar problem occurs with estimation of the cardinality of sets of
    multiple columns, such as the number of groups that would be generated by
    a GROUP BY clause.  When GROUP BY
    lists a single column, the n-distinct estimate (which is visible as the
    estimated number of rows returned by the HashAggregate node) is very
    accurate:

```
EXPLAIN (ANALYZE, TIMING OFF, BUFFERS OFF) SELECT COUNT(*) FROM t GROUP BY a;
                                       QUERY PLAN
-------------------------------------------------------------------&zwsp;----------------------
 HashAggregate  (cost=195.00..196.00 rows=100 width=12) (actual rows=100.00 loops=1)
   Group Key: a
   ->  Seq Scan on t  (cost=0.00..145.00 rows=10000 width=4) (actual rows=10000.00 loops=1)
```

    But without multivariate statistics, the estimate for the number of
    groups in a query with two columns in GROUP BY, as
    in the following example, is off by an order of magnitude:

```
EXPLAIN (ANALYZE, TIMING OFF, BUFFERS OFF) SELECT COUNT(*) FROM t GROUP BY a, b;
                                       QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------------------
 HashAggregate  (cost=220.00..230.00 rows=1000 width=16) (actual rows=100.00 loops=1)
   Group Key: a, b
   ->  Seq Scan on t  (cost=0.00..145.00 rows=10000 width=8) (actual rows=10000.00 loops=1)
```

    By redefining the statistics object to include n-distinct counts for the
    two columns, the estimate is much improved:

```
DROP STATISTICS stts;
CREATE STATISTICS stts (dependencies, ndistinct) ON a, b FROM t;
ANALYZE t;
EXPLAIN (ANALYZE, TIMING OFF, BUFFERS OFF) SELECT COUNT(*) FROM t GROUP BY a, b;
                                       QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------------------
 HashAggregate  (cost=220.00..221.00 rows=100 width=16) (actual rows=100.00 loops=1)
   Group Key: a, b
   ->  Seq Scan on t  (cost=0.00..145.00 rows=10000 width=8) (actual rows=10000.00 loops=1)
```

   

  

  

   
# MCV Lists

   

    As explained in , functional
    dependencies are very cheap and efficient type of statistics, but their
    main limitation is their global nature (only tracking dependencies at
    the column level, not between individual column values).
   

   

    This section introduces multivariate variant of MCV
    (most-common values) lists, a straightforward extension of the per-column
    statistics described in .  These
    statistics address the limitation by storing individual values, but it is
    naturally more expensive, both in terms of building the statistics in
    ANALYZE, storage and planning time.
   

   

    Let's look at the query from 
    again, but this time with a MCV list created on the
    same set of columns (be sure to drop the functional dependencies, to
    make sure the planner uses the newly created statistics).

```
DROP STATISTICS stts;
CREATE STATISTICS stts2 (mcv) ON a, b FROM t;
ANALYZE t;
EXPLAIN (ANALYZE, TIMING OFF, BUFFERS OFF) SELECT * FROM t WHERE a = 1 AND b = 1;
                                   QUERY PLAN
-------------------------------------------------------------------&zwsp;------------
 Seq Scan on t  (cost=0.00..195.00 rows=100 width=8) (actual rows=100.00 loops=1)
   Filter: ((a = 1) AND (b = 1))
   Rows Removed by Filter: 9900
```

    The estimate is as accurate as with the functional dependencies, mostly
    thanks to the table being fairly small and having a simple distribution
    with a low number of distinct values. Before looking at the second query,
    which was not handled by functional dependencies particularly well,
    let's inspect the MCV list a bit.
   

   

    Inspecting the MCV list is possible using
    pg_mcv_list_items set-returning function.

```
SELECT m.* FROM pg_statistic_ext JOIN pg_statistic_ext_data ON (oid = stxoid),
                pg_mcv_list_items(stxdmcv) m WHERE stxname = 'stts2';
 index |  values  | nulls | frequency | base_frequency
-------+----------+-------+-----------+----------------
     0 | {0, 0}   | {f,f} |      0.01 |         0.0001
     1 | {1, 1}   | {f,f} |      0.01 |         0.0001
   ...
    49 | {49, 49} | {f,f} |      0.01 |         0.0001
    50 | {50, 50} | {f,f} |      0.01 |         0.0001
   ...
    97 | {97, 97} | {f,f} |      0.01 |         0.0001
    98 | {98, 98} | {f,f} |      0.01 |         0.0001
    99 | {99, 99} | {f,f} |      0.01 |         0.0001
(100 rows)
```

    This confirms there are 100 distinct combinations in the two columns, and
    all of them are about equally likely (1% frequency for each one).  The
    base frequency is the frequency computed from per-column statistics, as if
    there were no multi-column statistics. Had there been any null values in
    either of the columns, this would be identified in the
    nulls column.
   

   

    When estimating the selectivity, the planner applies all the conditions
    on items in the MCV list, and then sums the frequencies
    of the matching ones. See mcv_clauselist_selectivity
    in src/backend/statistics/mcv.c for details.
   

   

    Compared to functional dependencies, MCV lists have two
    major advantages. Firstly, the list stores actual values, making it possible
    to decide which combinations are compatible.

```
EXPLAIN (ANALYZE, TIMING OFF, BUFFERS OFF) SELECT * FROM t WHERE a = 1 AND b = 10;
                                 QUERY PLAN
-------------------------------------------------------------------&zwsp;--------
 Seq Scan on t  (cost=0.00..195.00 rows=1 width=8) (actual rows=0.00 loops=1)
   Filter: ((a = 1) AND (b = 10))
   Rows Removed by Filter: 10000
```

    Secondly, MCV lists handle a wider range of clause types,
    not just equality clauses like functional dependencies. For example,
    consider the following range query for the same table:

```
EXPLAIN (ANALYZE, TIMING OFF, BUFFERS OFF) SELECT * FROM t WHERE a <= 49 AND b > 49;
                                QUERY PLAN
-------------------------------------------------------------------&zwsp;--------
 Seq Scan on t  (cost=0.00..195.00 rows=1 width=8) (actual rows=0.00 loops=1)
   Filter: ((a <= 49) AND (b > 49))
   Rows Removed by Filter: 10000
```

   

  

 

 

  
# Planner Statistics and Security

  

   Access to the table pg_statistic is restricted to
   superusers, so that ordinary users cannot learn about the contents of the
   tables of other users from it.  Some selectivity estimation functions will
   use a user-provided operator (either the operator appearing in the query or
   a related operator) to analyze the stored statistics.  For example, in order
   to determine whether a stored most common value is applicable, the
   selectivity estimator will have to run the appropriate =
   operator to compare the constant in the query to the stored value.
   Thus the data in pg_statistic is potentially
   passed to user-defined operators.  An appropriately crafted operator can
   intentionally leak the passed operands (for example, by logging them
   or writing them to a different table), or accidentally leak them by showing
   their values in error messages, in either case possibly exposing data from
   pg_statistic to a user who should not be able to
   see it.
  

  

   In order to prevent this, the following applies to all built-in selectivity
   estimation functions.  When planning a query, in order to be able to use
   stored statistics, the current user must either
   have SELECT privilege on the table or the involved
   columns, or the operator used must be LEAKPROOF (more
   accurately, the function that the operator is based on).  If not, then the
   selectivity estimator will behave as if no statistics are available, and
   the planner will proceed with default or fall-back assumptions.
   The  program's
   \do+
   meta-command is useful to determine which operators are marked as leakproof.
  

  

   If a user does not have the required privilege on the table or columns,
   then in many cases the query will ultimately receive a permission-denied
   error, in which case this mechanism is invisible in practice.  But if the
   user is reading from a security-barrier view, then the planner might wish
   to check the statistics of an underlying table that is otherwise
   inaccessible to the user.  In that case, the operator should be leakproof
   or the statistics will not be used.  There is no direct feedback about
   that, except that the plan might be suboptimal.  If one suspects that this
   is the case, one could try running the query as a more privileged user,
   to see if a different plan results.
  

  

   This restriction applies only to cases where the planner would need to
   execute a user-defined operator on one or more values
   from pg_statistic.  Thus the planner is permitted
   to use generic statistical information, such as the fraction of null values
   or the number of distinct values in a column, regardless of access
   privileges.
  

  

   Selectivity estimation functions contained in third-party extensions that
   potentially operate on statistics with user-defined operators should follow
   the same security rules.  Consult the PostgreSQL source code for guidance.
