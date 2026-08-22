# Performance Tips

  
   performance
  

  

   Query performance can be affected by many things. Some of these can
   be controlled by the user, while others are fundamental to the underlying
   design of the system.  This chapter provides some hints about understanding
   and tuning PostgreSQL performance.
  

 

  
# Using EXPLAIN

   
    EXPLAIN
   

   
    query plan
   

   

    PostgreSQL devises a query
    plan for each query it receives.  Choosing the right
    plan to match the query structure and the properties of the data
    is absolutely critical for good performance, so the system includes
    a complex planner that tries to choose good plans.
    You can use the EXPLAIN command
    to see what query plan the planner creates for any query.
    Plan-reading is an art that requires some experience to master,
    but this section attempts to cover the basics.
   

   

    Examples in this section are drawn from the regression test database
    after doing a VACUUM ANALYZE, using v18 development sources.
    You should be able to get similar results if you try the examples
    yourself, but your estimated costs and row counts might vary slightly
    because ANALYZE's statistics are random samples rather
    than exact, and because costs are inherently somewhat platform-dependent.
   

   

    The examples use EXPLAIN's default text output
    format, which is compact and convenient for humans to read.
    If you want to feed EXPLAIN's output to a program for further
    analysis, you should use one of its machine-readable output formats
    (XML, JSON, or YAML) instead.
   

  

   
# EXPLAIN Basics

   

    The structure of a query plan is a tree of plan nodes.
    Nodes at the bottom level of the tree are scan nodes: they return raw rows
    from a table.  There are different types of scan nodes for different
    table access methods: sequential scans, index scans, and bitmap index
    scans.  There are also non-table row sources, such as VALUES
    clauses and set-returning functions in FROM, which have their
    own scan node types.
    If the query requires joining, aggregation, sorting, or other
    operations on the raw rows, then there will be additional nodes
    above the scan nodes to perform these operations.  Again,
    there is usually more than one possible way to do these operations,
    so different node types can appear here too.  The output
    of EXPLAIN has one line for each node in the plan
    tree, showing the basic node type plus the cost estimates that the planner
    made for the execution of that plan node.  Additional lines might appear,
    indented from the node's summary line,
    to show additional properties of the node.
    The very first line (the summary line for the topmost
    node) has the estimated total execution cost for the plan; it is this
    number that the planner seeks to minimize.
   

   

    Here is a trivial example, just to show what the output looks like:

```
EXPLAIN SELECT * FROM tenk1;

                         QUERY PLAN
-------------------------------------------------------------
 Seq Scan on tenk1  (cost=0.00..445.00 rows=10000 width=244)
```

   

   

    Since this query has no WHERE clause, it must scan all the
    rows of the table, so the planner has chosen to use a simple sequential
    scan plan.  The numbers that are quoted in parentheses are (left
    to right):

    

     

      

       Estimated start-up cost.  This is the time expended before the output
       phase can begin, e.g., time to do the sorting in a sort node.
      

     

     

      

       Estimated total cost.  This is stated on the assumption that the plan
       node is run to completion, i.e., all available rows are retrieved.
       In practice a node's parent node might stop short of reading all
       available rows (see the LIMIT example below).
      

     

     

      

       Estimated number of rows output by this plan node.  Again, the node
       is assumed to be run to completion.
      

     

     

      

       Estimated average width of rows output by this plan node (in bytes).
      

     

    

   

   

    The costs are measured in arbitrary units determined by the planner's
    cost parameters (see ).
    Traditional practice is to measure the costs in units of disk page
    fetches; that is,  is conventionally
    set to 1.0 and the other cost parameters are set relative
    to that.  The examples in this section are run with the default cost
    parameters.
   

   

    It's important to understand that the cost of an upper-level node includes
    the cost of all its child nodes.  It's also important to realize that
    the cost only reflects things that the planner cares about.
    In particular, the cost does not consider the time spent to convert
    output values to text form or to transmit them to the client, which
    could be important factors in the real elapsed time; but the planner
    ignores those costs because it cannot change them by altering the
    plan.  (Every correct plan will output the same row set, we trust.)
   

   

    The rows value is a little tricky because it is
    not the number of rows processed or scanned by the
    plan node, but rather the number emitted by the node.  This is often
    less than the number scanned, as a result of filtering by any
    WHERE-clause conditions that are being applied at the node.
    Ideally the top-level rows estimate will approximate the number of rows
    actually returned, updated, or deleted by the query.
   

   

    Returning to our example:

```
EXPLAIN SELECT * FROM tenk1;

                         QUERY PLAN
-------------------------------------------------------------
 Seq Scan on tenk1  (cost=0.00..445.00 rows=10000 width=244)
```

   

   

    These numbers are derived very straightforwardly.  If you do:

```
SELECT relpages, reltuples FROM pg_class WHERE relname = 'tenk1';
```

    you will find that tenk1 has 345 disk
    pages and 10000 rows.  The estimated cost is computed as (disk pages read *
    ) + (rows scanned *
    ).  By default,
    seq_page_cost is 1.0 and cpu_tuple_cost is 0.01,
    so the estimated cost is (345 * 1.0) + (10000 * 0.01) = 445.
   

   

    Now let's modify the query to add a WHERE condition:

```
EXPLAIN SELECT * FROM tenk1 WHERE unique1 < 7000;

                         QUERY PLAN
------------------------------------------------------------
 Seq Scan on tenk1  (cost=0.00..470.00 rows=7000 width=244)
   Filter: (unique1 < 7000)
```

    Notice that the EXPLAIN output shows the WHERE
    clause being applied as a filter condition attached to the Seq
    Scan plan node.  This means that
    the plan node checks the condition for each row it scans, and outputs
    only the ones that pass the condition.
    The estimate of output rows has been reduced because of the
    WHERE clause.
    However, the scan will still have to visit all 10000 rows, so the cost
    hasn't decreased; in fact it has gone up a bit (by 10000 * , to be exact) to reflect the extra CPU
    time spent checking the WHERE condition.
   

   

    The actual number of rows this query would select is 7000, but the rows
    estimate is only approximate.  If you try to duplicate this experiment,
    you may well get a slightly different estimate; moreover, it can
    change after each ANALYZE command, because the
    statistics produced by ANALYZE are taken from a
    randomized sample of the table.
   

   

    Now, let's make the condition more restrictive:

```
EXPLAIN SELECT * FROM tenk1 WHERE unique1 < 100;

                                  QUERY PLAN
-------------------------------------------------------------------&zwsp;-----------
 Bitmap Heap Scan on tenk1  (cost=5.06..224.98 rows=100 width=244)
   Recheck Cond: (unique1 < 100)
   ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..5.04 rows=100 width=0)
         Index Cond: (unique1 < 100)
```

    Here the planner has decided to use a two-step plan: the child plan
    node visits an index to find the locations of rows matching the index
    condition, and then the upper plan node actually fetches those rows
    from the table itself.  Fetching rows separately is much more
    expensive than reading them sequentially, but because not all the pages
    of the table have to be visited, this is still cheaper than a sequential
    scan.  (The reason for using two plan levels is that the upper plan
    node sorts the row locations identified by the index into physical order
    before reading them, to minimize the cost of separate fetches.
    The bitmap mentioned in the node names is the mechanism that
    does the sorting.)
   

   

    Now let's add another condition to the WHERE clause:

```
EXPLAIN SELECT * FROM tenk1 WHERE unique1 < 100 AND stringu1 = 'xxx';

                                  QUERY PLAN
-------------------------------------------------------------------&zwsp;-----------
 Bitmap Heap Scan on tenk1  (cost=5.04..225.20 rows=1 width=244)
   Recheck Cond: (unique1 < 100)
   Filter: (stringu1 = 'xxx'::name)
   ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..5.04 rows=100 width=0)
         Index Cond: (unique1 < 100)
```

    The added condition stringu1 = 'xxx' reduces the
    output row count estimate, but not the cost because we still have to visit
    the same set of rows.  That's because the stringu1 clause
    cannot be applied as an index condition, since this index is only on
    the unique1 column.  Instead it is applied as a filter on
    the rows retrieved using the index.  Thus the cost has actually gone up
    slightly to reflect this extra checking.
   

   

    In some cases the planner will prefer a simple index scan plan:

```
EXPLAIN SELECT * FROM tenk1 WHERE unique1 = 42;

                                 QUERY PLAN
-------------------------------------------------------------------&zwsp;----------
 Index Scan using tenk1_unique1 on tenk1  (cost=0.29..8.30 rows=1 width=244)
   Index Cond: (unique1 = 42)
```

    In this type of plan the table rows are fetched in index order, which
    makes them even more expensive to read, but there are so few that the
    extra cost of sorting the row locations is not worth it.  You'll most
    often see this plan type for queries that fetch just a single row.  It's
    also often used for queries that have an ORDER BY condition
    that matches the index order, because then no extra sorting step is needed
    to satisfy the ORDER BY.  In this example, adding
    ORDER BY unique1 would use the same plan because the
    index already implicitly provides the requested ordering.
   

   

     The planner may implement an ORDER BY clause in several
     ways.  The above example shows that such an ordering clause may be
     implemented implicitly.  The planner may also add an explicit
     Sort step:

```
EXPLAIN SELECT * FROM tenk1 ORDER BY unique1;

                            QUERY PLAN
-------------------------------------------------------------------
 Sort  (cost=1109.39..1134.39 rows=10000 width=244)
   Sort Key: unique1
   ->  Seq Scan on tenk1  (cost=0.00..445.00 rows=10000 width=244)
```

    If a part of the plan guarantees an ordering on a prefix of the
    required sort keys, then the planner may instead decide to use an
    Incremental Sort step:

```
EXPLAIN SELECT * FROM tenk1 ORDER BY hundred, ten LIMIT 100;

                                              QUERY PLAN
-------------------------------------------------------------------&zwsp;-----------------------------
 Limit  (cost=19.35..39.49 rows=100 width=244)
   ->  Incremental Sort  (cost=19.35..2033.39 rows=10000 width=244)
         Sort Key: hundred, ten
         Presorted Key: hundred
         Estimated Groups: 100
         ->  Index Scan using tenk1_hundred on tenk1  (cost=0.29..1574.20 rows=10000 width=244)
```

    Compared to regular sorts, sorting incrementally allows returning tuples
    before the entire result set has been sorted, which particularly enables
    optimizations with LIMIT queries.  It may also reduce
    memory usage and the likelihood of spilling sorts to disk, but it comes at
    the cost of the increased overhead of splitting the result set into multiple
    sorting batches.
   

   

    If there are separate indexes on several of the columns referenced
    in WHERE, the planner might choose to use an AND or OR
    combination of the indexes:

```
EXPLAIN SELECT * FROM tenk1 WHERE unique1 < 100 AND unique2 > 9000;

                                     QUERY PLAN
-------------------------------------------------------------------&zwsp;------------------
 Bitmap Heap Scan on tenk1  (cost=25.07..60.11 rows=10 width=244)
   Recheck Cond: ((unique1 < 100) AND (unique2 > 9000))
   ->  BitmapAnd  (cost=25.07..25.07 rows=10 width=0)
         ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..5.04 rows=100 width=0)
               Index Cond: (unique1 < 100)
         ->  Bitmap Index Scan on tenk1_unique2  (cost=0.00..19.78 rows=999 width=0)
               Index Cond: (unique2 > 9000)
```

    But this requires visiting both indexes, so it's not necessarily a win
    compared to using just one index and treating the other condition as
    a filter.  If you vary the ranges involved you'll see the plan change
    accordingly.
   

   

    Here is an example showing the effects of LIMIT:

```
EXPLAIN SELECT * FROM tenk1 WHERE unique1 < 100 AND unique2 > 9000 LIMIT 2;

                                     QUERY PLAN
-------------------------------------------------------------------&zwsp;------------------
 Limit  (cost=0.29..14.28 rows=2 width=244)
   ->  Index Scan using tenk1_unique2 on tenk1  (cost=0.29..70.27 rows=10 width=244)
         Index Cond: (unique2 > 9000)
         Filter: (unique1 < 100)
```

   

   

    This is the same query as above, but we added a LIMIT so that
    not all the rows need be retrieved, and the planner changed its mind about
    what to do.  Notice that the total cost and row count of the Index Scan
    node are shown as if it were run to completion.  However, the Limit node
    is expected to stop after retrieving only a fifth of those rows, so its
    total cost is only a fifth as much, and that's the actual estimated cost
    of the query.  This plan is preferred over adding a Limit node to the
    previous plan because the Limit could not avoid paying the startup cost
    of the bitmap scan, so the total cost would be something over 25 units
    with that approach.
   

   

    Let's try joining two tables, using the columns we have been discussing:

```
EXPLAIN SELECT *
FROM tenk1 t1, tenk2 t2
WHERE t1.unique1 < 10 AND t1.unique2 = t2.unique2;

                                      QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------------
 Nested Loop  (cost=4.65..118.50 rows=10 width=488)
   ->  Bitmap Heap Scan on tenk1 t1  (cost=4.36..39.38 rows=10 width=244)
         Recheck Cond: (unique1 < 10)
         ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..4.36 rows=10 width=0)
               Index Cond: (unique1 < 10)
   ->  Index Scan using tenk2_unique2 on tenk2 t2  (cost=0.29..7.90 rows=1 width=244)
         Index Cond: (unique2 = t1.unique2)
```

   

   

    In this plan, we have a nested-loop join node with two table scans as
    inputs, or children.  The indentation of the node summary lines reflects
    the plan tree structure.  The join's first, or outer, child
    is a bitmap scan similar to those we saw before.  Its cost and row count
    are the same as we'd get from SELECT ... WHERE unique1 < 10
    because we are
    applying the WHERE clause unique1 < 10
    at that node.
    The t1.unique2 = t2.unique2 clause is not relevant yet,
    so it doesn't affect the row count of the outer scan.  The nested-loop
    join node will run its second,
    or inner child once for each row obtained from the outer child.
    Column values from the current outer row can be plugged into the inner
    scan; here, the t1.unique2 value from the outer row is available,
    so we get a plan and costs similar to what we saw above for a simple
    SELECT ... WHERE t2.unique2 = constant case.
    (The estimated cost is actually a bit lower than what was seen above,
    as a result of caching that's expected to occur during the repeated
    index scans on t2.)  The
    costs of the loop node are then set on the basis of the cost of the outer
    scan, plus one repetition of the inner scan for each outer row (10 * 7.90,
    here), plus a little CPU time for join processing.
   

   

    In this example the join's output row count is the same as the product
    of the two scans' row counts, but that's not true in all cases because
    there can be additional WHERE clauses that mention both tables
    and so can only be applied at the join point, not to either input scan.
    Here's an example:

```
EXPLAIN SELECT *
FROM tenk1 t1, tenk2 t2
WHERE t1.unique1 < 10 AND t2.unique2 < 10 AND t1.hundred < t2.hundred;

                                         QUERY PLAN
-------------------------------------------------------------------&zwsp;--------------------------
 Nested Loop  (cost=4.65..49.36 rows=33 width=488)
   Join Filter: (t1.hundred < t2.hundred)
   ->  Bitmap Heap Scan on tenk1 t1  (cost=4.36..39.38 rows=10 width=244)
         Recheck Cond: (unique1 < 10)
         ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..4.36 rows=10 width=0)
               Index Cond: (unique1 < 10)
   ->  Materialize  (cost=0.29..8.51 rows=10 width=244)
         ->  Index Scan using tenk2_unique2 on tenk2 t2  (cost=0.29..8.46 rows=10 width=244)
               Index Cond: (unique2 < 10)
```

    The condition t1.hundred < t2.hundred can't be
    tested in the tenk2_unique2 index, so it's applied at the
    join node.  This reduces the estimated output row count of the join node,
    but does not change either input scan.
   

   

    Notice that here the planner has chosen to materialize the inner
    relation of the join, by putting a Materialize plan node atop it.  This
    means that the t2 index scan will be done just once, even
    though the nested-loop join node needs to read that data ten times, once
    for each row from the outer relation.  The Materialize node saves the data
    in memory as it's read, and then returns the data from memory on each
    subsequent pass.
   

   

    When dealing with outer joins, you might see join plan nodes with both
    Join Filter and plain Filter conditions attached.
    Join Filter conditions come from the outer join's ON clause,
    so a row that fails the Join Filter condition could still get emitted as
    a null-extended row.  But a plain Filter condition is applied after the
    outer-join rules and so acts to remove rows unconditionally.  In an inner
    join there is no semantic difference between these types of filters.
   

   

    If we change the query's selectivity a bit, we might get a very different
    join plan:

```
EXPLAIN SELECT *
FROM tenk1 t1, tenk2 t2
WHERE t1.unique1 < 100 AND t1.unique2 = t2.unique2;

                                        QUERY PLAN
-------------------------------------------------------------------&zwsp;-----------------------
 Hash Join  (cost=226.23..709.73 rows=100 width=488)
   Hash Cond: (t2.unique2 = t1.unique2)
   ->  Seq Scan on tenk2 t2  (cost=0.00..445.00 rows=10000 width=244)
   ->  Hash  (cost=224.98..224.98 rows=100 width=244)
         ->  Bitmap Heap Scan on tenk1 t1  (cost=5.06..224.98 rows=100 width=244)
               Recheck Cond: (unique1 < 100)
               ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..5.04 rows=100 width=0)
                     Index Cond: (unique1 < 100)
```

   

   

    Here, the planner has chosen to use a hash join, in which rows of one
    table are entered into an in-memory hash table, after which the other
    table is scanned and the hash table is probed for matches to each row.
    Again note how the indentation reflects the plan structure: the bitmap
    scan on tenk1 is the input to the Hash node, which constructs
    the hash table.  That's then returned to the Hash Join node, which reads
    rows from its outer child plan and searches the hash table for each one.
   

   

    Another possible type of join is a merge join, illustrated here:

```
EXPLAIN SELECT *
FROM tenk1 t1, onek t2
WHERE t1.unique1 < 100 AND t1.unique2 = t2.unique2;

                                        QUERY PLAN
-------------------------------------------------------------------&zwsp;-----------------------
 Merge Join  (cost=0.56..233.49 rows=10 width=488)
   Merge Cond: (t1.unique2 = t2.unique2)
   ->  Index Scan using tenk1_unique2 on tenk1 t1  (cost=0.29..643.28 rows=100 width=244)
         Filter: (unique1 < 100)
   ->  Index Scan using onek_unique2 on onek t2  (cost=0.28..166.28 rows=1000 width=244)
```

   

   

    Merge join requires its input data to be sorted on the join keys.  In this
    example each input is sorted by using an index scan to visit the rows
    in the correct order; but a sequential scan and sort could also be used.
    (Sequential-scan-and-sort frequently beats an index scan for sorting many rows,
    because of the nonsequential disk access required by the index scan.)
   

   

    One way to look at variant plans is to force the planner to disregard
    whatever strategy it thought was the cheapest, using the enable/disable
    flags described in .
    (This is a crude tool, but useful.  See
    also .)
    For example, if we're unconvinced that merge join is the best join
    type for the previous example, we could try

```
SET enable_mergejoin = off;

EXPLAIN SELECT *
FROM tenk1 t1, onek t2
WHERE t1.unique1 < 100 AND t1.unique2 = t2.unique2;

                                        QUERY PLAN
-------------------------------------------------------------------&zwsp;-----------------------
 Hash Join  (cost=226.23..344.08 rows=10 width=488)
   Hash Cond: (t2.unique2 = t1.unique2)
   ->  Seq Scan on onek t2  (cost=0.00..114.00 rows=1000 width=244)
   ->  Hash  (cost=224.98..224.98 rows=100 width=244)
         ->  Bitmap Heap Scan on tenk1 t1  (cost=5.06..224.98 rows=100 width=244)
               Recheck Cond: (unique1 < 100)
               ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..5.04 rows=100 width=0)
                     Index Cond: (unique1 < 100)
```

    which shows that the planner thinks that hash join would be nearly 50%
    more expensive than merge join for this case.
    Of course, the next question is whether it's right about that.
    We can investigate that using EXPLAIN ANALYZE, as
    discussed below.
   

   

    When using the enable/disable flags to disable plan node types, many of
    the flags only discourage the use of the corresponding plan node and don't
    outright disallow the planner's ability to use the plan node type.  This
    is by design so that the planner still maintains the ability to form a
    plan for a given query.  When the resulting plan contains a disabled node,
    the EXPLAIN output will indicate this fact.

```
SET enable_seqscan = off;
EXPLAIN SELECT * FROM unit;

                       QUERY PLAN
---------------------------------------------------------
 Seq Scan on unit  (cost=0.00..21.30 rows=1130 width=44)
   Disabled: true
```

   

   

    Because the unit table has no indexes, there is no
    other means to read the table data, so the sequential scan is the only
    option available to the query planner.
   

   

    
     subplan
    
    Some query plans involve subplans, which arise
    from sub-SELECTs in the original query.  Such
    queries can sometimes be transformed into ordinary join plans, but
    when they cannot be, we get plans like:

```
EXPLAIN VERBOSE SELECT unique1
FROM tenk1 t
WHERE t.ten < ALL (SELECT o.ten FROM onek o WHERE o.four = t.four);

                               QUERY PLAN
-------------------------------------------------------------------&zwsp;------
 Seq Scan on public.tenk1 t  (cost=0.00..586095.00 rows=5000 width=4)
   Output: t.unique1
   Filter: (ALL (t.ten < (SubPlan 1).col1))
   SubPlan 1
     ->  Seq Scan on public.onek o  (cost=0.00..116.50 rows=250 width=4)
           Output: o.ten
           Filter: (o.four = t.four)
```

    This rather artificial example serves to illustrate a couple of
    points: values from the outer plan level can be passed down into a
    subplan (here, t.four is passed down) and the
    results of the sub-select are available to the outer plan.  Those
    result values are shown by EXPLAIN with notations
    like
    (subplan_name).colN,
    which refers to the N'th output column of
    the sub-SELECT.
   

   

    
     subplan
     hashed
    
    In the example above, the ALL operator runs the
    subplan again for each row of the outer query (which accounts for the
    high estimated cost).  Some queries can use a hashed
    subplan to avoid that:

```
EXPLAIN SELECT *
FROM tenk1 t
WHERE t.unique1 NOT IN (SELECT o.unique1 FROM onek o);

                                         QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------------------
 Seq Scan on tenk1 t  (cost=61.77..531.77 rows=5000 width=244)
   Filter: (NOT (ANY (unique1 = (hashed SubPlan 1).col1)))
   SubPlan 1
     ->  Index Only Scan using onek_unique1 on onek o  (cost=0.28..59.27 rows=1000 width=4)
(4 rows)
```

    Here, the subplan is run a single time and its output is loaded into
    an in-memory hash table, which is then probed by the
    outer ANY operator.  This requires that the
    sub-SELECT not reference any variables of the outer
    query, and that the ANY's comparison operator be
    amenable to hashing.
   

   

    
     initplan
    
    If, in addition to not referencing any variables of the outer query,
    the sub-SELECT cannot return more than one row,
    it may instead be implemented as an initplan:

```
EXPLAIN VERBOSE SELECT unique1
FROM tenk1 t1 WHERE t1.ten = (SELECT (random() * 10)::integer);

                             QUERY PLAN
------------------------------------------------------------&zwsp;--------
 Seq Scan on public.tenk1 t1  (cost=0.02..470.02 rows=1000 width=4)
   Output: t1.unique1
   Filter: (t1.ten = (InitPlan 1).col1)
   InitPlan 1
     ->  Result  (cost=0.00..0.02 rows=1 width=4)
           Output: ((random() * '10'::double precision))::integer
```

    An initplan is run only once per execution of the outer plan, and its
    results are saved for re-use in later rows of the outer plan.  So in
    this example random() is evaluated only once and
    all the values of t1.ten are compared to the same
    randomly-chosen integer.  That's quite different from what would
    happen without the sub-SELECT construct.
   

  

  

   
# EXPLAIN ANALYZE

   

    It is possible to check the accuracy of the planner's estimates
    by using EXPLAIN's ANALYZE option.  With this
    option, EXPLAIN actually executes the query, and then displays
    the true row counts and true run time accumulated within each plan node,
    along with the same estimates that a plain EXPLAIN
    shows.  For example, we might get a result like this:

```
EXPLAIN ANALYZE SELECT *
FROM tenk1 t1, tenk2 t2
WHERE t1.unique1 < 10 AND t1.unique2 = t2.unique2;

                                                           QUERY PLAN
-------------------------------------------------------------------&zwsp;--------------------------------------------------------------
 Nested Loop  (cost=4.65..118.50 rows=10 width=488) (actual time=0.017..0.051 rows=10.00 loops=1)
   Buffers: shared hit=36 read=6
   ->  Bitmap Heap Scan on tenk1 t1  (cost=4.36..39.38 rows=10 width=244) (actual time=0.009..0.017 rows=10.00 loops=1)
         Recheck Cond: (unique1 < 10)
         Heap Blocks: exact=10
         Buffers: shared hit=3 read=5 written=4
         ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..4.36 rows=10 width=0) (actual time=0.004..0.004 rows=10.00 loops=1)
               Index Cond: (unique1 < 10)
               Index Searches: 1
               Buffers: shared hit=2
   ->  Index Scan using tenk2_unique2 on tenk2 t2  (cost=0.29..7.90 rows=1 width=244) (actual time=0.003..0.003 rows=1.00 loops=10)
         Index Cond: (unique2 = t1.unique2)
         Index Searches: 10
         Buffers: shared hit=24 read=6
 Planning:
   Buffers: shared hit=15 dirtied=9
 Planning Time: 0.485 ms
 Execution Time: 0.073 ms
```

    Note that the actual time values are in milliseconds of
    real time, whereas the cost estimates are expressed in
    arbitrary units; so they are unlikely to match up.
    The thing that's usually most important to look for is whether the
    estimated row counts are reasonably close to reality.  In this example
    the estimates were all dead-on, but that's quite unusual in practice.
   

   

    In some query plans, it is possible for a subplan node to be executed more
    than once.  For example, the inner index scan will be executed once per
    outer row in the above nested-loop plan.  In such cases, the
    loops value reports the
    total number of executions of the node, and the actual time and rows
    values shown are averages per-execution.  This is done to make the numbers
    comparable with the way that the cost estimates are shown.  Multiply by
    the loops value to get the total time actually spent in
    the node and the total number of rows processed by the node across all
    executions.  In the above example, we spent a total of 0.030 milliseconds
    executing the index scans on tenk2, and they handled a
    total of 10 rows.
   

   

    Parallel execution will also cause nodes to be executed more than once.
    This is also reported with the loops value. We can
    change some planner settings to make the planner pick a parallel plan for
    the above query:
   

```
SET min_parallel_table_scan_size = 0;
SET parallel_tuple_cost = 0;
SET parallel_setup_cost = 0;

EXPLAIN ANALYZE SELECT *
FROM tenk1 t1, tenk2 t2
WHERE t1.unique1 < 10 AND t1.unique2 = t2.unique2;
                                                                QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------------------------------------------------------------&zwsp;----
 Gather  (cost=4.65..70.96 rows=10 width=488) (actual time=1.161..11.655 rows=10.00 loops=1)
   Workers Planned: 2
   Workers Launched: 2
   Buffers: shared hit=78 read=6
   ->  Nested Loop  (cost=4.65..70.96 rows=4 width=488) (actual time=0.247..0.317 rows=3.33 loops=3)
         Buffers: shared hit=78 read=6
         ->  Parallel Bitmap Heap Scan on tenk1 t1  (cost=4.36..39.31 rows=4 width=244) (actual time=0.228..0.249 rows=3.33 loops=3)
               Recheck Cond: (unique1 < 10)
               Heap Blocks: exact=10
               Buffers: shared hit=54
               ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..4.36 rows=10 width=0) (actual time=0.438..0.439 rows=10.00 loops=1)
                     Index Cond: (unique1 < 10)
                     Index Searches: 1
                     Buffers: shared hit=2
         ->  Index Scan using tenk2_unique2 on tenk2 t2  (cost=0.29..7.90 rows=1 width=244) (actual time=0.016..0.017 rows=1.00 loops=10)
               Index Cond: (unique2 = t1.unique2)
               Index Searches: 10
               Buffers: shared hit=24 read=6
 Planning:
   Buffers: shared hit=327 read=3
 Planning Time: 4.781 ms
 Execution Time: 11.858 ms
(22 rows)
```

   

    The parallel bitmap heap scan was split into three separate
    executions: one in the leader (since
     is on by default),
    and one in each of the two launched workers.  Similarly to sequential
    repeated executions, rows and actual time are averages per-worker.
    Multiply by the loops value to get the total number
    of rows processed by the node across all workers.  The total time
    spent in all workers can be calculated similarly, but since this time
    is spent concurrently, it is not equivalent to total elapsed time.
   

   

    In some cases EXPLAIN ANALYZE shows additional execution
    statistics beyond the plan node execution times and row counts.
    For example, Sort and Hash nodes provide extra information:

```
EXPLAIN ANALYZE SELECT *
FROM tenk1 t1, tenk2 t2
WHERE t1.unique1 < 100 AND t1.unique2 = t2.unique2 ORDER BY t1.fivethous;

                                                                 QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------------------------------------------------------------&zwsp;------
 Sort  (cost=713.05..713.30 rows=100 width=488) (actual time=2.995..3.002 rows=100.00 loops=1)
   Sort Key: t1.fivethous
   Sort Method: quicksort  Memory: 74kB
   Buffers: shared hit=440
   ->  Hash Join  (cost=226.23..709.73 rows=100 width=488) (actual time=0.515..2.920 rows=100.00 loops=1)
         Hash Cond: (t2.unique2 = t1.unique2)
         Buffers: shared hit=437
         ->  Seq Scan on tenk2 t2  (cost=0.00..445.00 rows=10000 width=244) (actual time=0.026..1.790 rows=10000.00 loops=1)
               Buffers: shared hit=345
         ->  Hash  (cost=224.98..224.98 rows=100 width=244) (actual time=0.476..0.477 rows=100.00 loops=1)
               Buckets: 1024  Batches: 1  Memory Usage: 35kB
               Buffers: shared hit=92
               ->  Bitmap Heap Scan on tenk1 t1  (cost=5.06..224.98 rows=100 width=244) (actual time=0.030..0.450 rows=100.00 loops=1)
                     Recheck Cond: (unique1 < 100)
                     Heap Blocks: exact=90
                     Buffers: shared hit=92
                     ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..5.04 rows=100 width=0) (actual time=0.013..0.013 rows=100.00 loops=1)
                           Index Cond: (unique1 < 100)
                           Index Searches: 1
                           Buffers: shared hit=2
 Planning:
   Buffers: shared hit=12
 Planning Time: 0.187 ms
 Execution Time: 3.036 ms
```

    The Sort node shows the sort method used (in particular, whether the sort
    was in-memory or on-disk) and the amount of memory or disk space needed.
    The Hash node shows the number of hash buckets and batches as well as the
    peak amount of memory used for the hash table.  (If the number of batches
    exceeds one, there will also be disk space usage involved, but that is not
    shown.)
   

   

    Index Scan nodes (as well as Bitmap Index Scan and Index-Only Scan nodes)
    show an Index Searches line that reports the total number
    of searches across all node
    executions/loops:

```
EXPLAIN ANALYZE SELECT * FROM tenk1 WHERE thousand IN (1, 500, 700, 999);
                                                            QUERY PLAN
-------------------------------------------------------------------&zwsp;---------------------------------------------------------------
 Bitmap Heap Scan on tenk1  (cost=9.45..73.44 rows=40 width=244) (actual time=0.012..0.028 rows=40.00 loops=1)
   Recheck Cond: (thousand = ANY ('{1,500,700,999}'::integer[]))
   Heap Blocks: exact=39
   Buffers: shared hit=47
   ->  Bitmap Index Scan on tenk1_thous_tenthous  (cost=0.00..9.44 rows=40 width=0) (actual time=0.009..0.009 rows=40.00 loops=1)
         Index Cond: (thousand = ANY ('{1,500,700,999}'::integer[]))
         Index Searches: 4
         Buffers: shared hit=8
 Planning Time: 0.029 ms
 Execution Time: 0.034 ms
```

    Here we see a Bitmap Index Scan node that needed 4 separate index
    searches.  The scan had to search the index from the
    tenk1_thous_tenthous index root page once per
    integer value from the predicate's IN
    construct.  However, the number of index searches often won't have such a
    simple correspondence to the query predicate:

```
EXPLAIN ANALYZE SELECT * FROM tenk1 WHERE thousand IN (1, 2, 3, 4);
                                                            QUERY PLAN
-------------------------------------------------------------------&zwsp;---------------------------------------------------------------
 Bitmap Heap Scan on tenk1  (cost=9.45..73.44 rows=40 width=244) (actual time=0.009..0.019 rows=40.00 loops=1)
   Recheck Cond: (thousand = ANY ('{1,2,3,4}'::integer[]))
   Heap Blocks: exact=38
   Buffers: shared hit=40
   ->  Bitmap Index Scan on tenk1_thous_tenthous  (cost=0.00..9.44 rows=40 width=0) (actual time=0.005..0.005 rows=40.00 loops=1)
         Index Cond: (thousand = ANY ('{1,2,3,4}'::integer[]))
         Index Searches: 1
         Buffers: shared hit=2
 Planning Time: 0.029 ms
 Execution Time: 0.026 ms
```

    This variant of our IN query performed only 1 index
    search.  It spent less time traversing the index (compared to the original
    query) because its IN construct uses values matching
    index tuples stored next to each other, on the same
    tenk1_thous_tenthous index leaf page.
   

   

    The Index Searches line is also useful with B-tree index
    scans that apply the skip scan optimization to
    more efficiently traverse through an index:

```
EXPLAIN ANALYZE SELECT four, unique1 FROM tenk1 WHERE four BETWEEN 1 AND 3 AND unique1 = 42;
                                                              QUERY PLAN
-------------------------------------------------------------------&zwsp;---------------------------------------------------------------
 Index Only Scan using tenk1_four_unique1_idx on tenk1  (cost=0.29..6.90 rows=1 width=8) (actual time=0.006..0.007 rows=1.00 loops=1)
   Index Cond: ((four >= 1) AND (four <= 3) AND (unique1 = 42))
   Heap Fetches: 0
   Index Searches: 3
   Buffers: shared hit=7
 Planning Time: 0.029 ms
 Execution Time: 0.012 ms
```

    Here we see an Index-Only Scan node using
    tenk1_four_unique1_idx, a multi-column index on the
    tenk1 table's four and
    unique1 columns.  The scan performs 3 searches
    that each read a single index leaf page:
    four = 1 AND unique1 = 42,
    four = 2 AND unique1 = 42, and
    four = 3 AND unique1 = 42.  This index
    is generally a good target for skip scan, since, as discussed in
    , its leading column (the
    four column) contains only 4 distinct values,
    while its second/final column (the unique1
    column) contains many distinct values.
   

   

    Another type of extra information is the number of rows removed by a
    filter condition:

```
EXPLAIN ANALYZE SELECT * FROM tenk1 WHERE ten < 7;

                                               QUERY PLAN
-------------------------------------------------------------------&zwsp;--------------------------------------
 Seq Scan on tenk1  (cost=0.00..470.00 rows=7000 width=244) (actual time=0.030..1.995 rows=7000.00 loops=1)
   Filter: (ten < 7)
   Rows Removed by Filter: 3000
   Buffers: shared hit=345
 Planning Time: 0.102 ms
 Execution Time: 2.145 ms
```

    These counts can be particularly valuable for filter conditions applied at
    join nodes.  The Rows Removed line only appears when at least
    one scanned row, or potential join pair in the case of a join node,
    is rejected by the filter condition.
   

   

    A case similar to filter conditions occurs with lossy
    index scans.  For example, consider this search for polygons containing a
    specific point:

```
EXPLAIN ANALYZE SELECT * FROM polygon_tbl WHERE f1 @> polygon '(0.5,2.0)';

                                              QUERY PLAN
-------------------------------------------------------------------&zwsp;-----------------------------------
 Seq Scan on polygon_tbl  (cost=0.00..1.09 rows=1 width=85) (actual time=0.023..0.023 rows=0.00 loops=1)
   Filter: (f1 @> '((0.5,2))'::polygon)
   Rows Removed by Filter: 7
   Buffers: shared hit=1
 Planning Time: 0.039 ms
 Execution Time: 0.033 ms
```

    The planner thinks (quite correctly) that this sample table is too small
    to bother with an index scan, so we have a plain sequential scan in which
    all the rows got rejected by the filter condition.  But if we force an
    index scan to be used, we see:

```
SET enable_seqscan TO off;

EXPLAIN ANALYZE SELECT * FROM polygon_tbl WHERE f1 @> polygon '(0.5,2.0)';

                                                        QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------------------------------------------------
 Index Scan using gpolygonind on polygon_tbl  (cost=0.13..8.15 rows=1 width=85) (actual time=0.074..0.074 rows=0.00 loops=1)
   Index Cond: (f1 @> '((0.5,2))'::polygon)
   Rows Removed by Index Recheck: 1
   Index Searches: 1
   Buffers: shared hit=1
 Planning Time: 0.039 ms
 Execution Time: 0.098 ms
```

    Here we can see that the index returned one candidate row, which was
    then rejected by a recheck of the index condition.  This happens because a
    GiST index is lossy for polygon containment tests: it actually
    returns the rows with polygons that overlap the target, and then we have
    to do the exact containment test on those rows.
   

   

    EXPLAIN has a BUFFERS option which
    provides additional detail about I/O operations performed during the
    planning and execution of the given query.  The buffer numbers displayed
    show the count of the non-distinct buffers hit, read, dirtied, and written
    for the given node and all of its child nodes.  The
    ANALYZE option implicitly enables the
    BUFFERS option.  If this
    is undesired, BUFFERS may be explicitly disabled:

```
EXPLAIN (ANALYZE, BUFFERS OFF) SELECT * FROM tenk1 WHERE unique1 < 100 AND unique2 > 9000;

                                                           QUERY PLAN
-------------------------------------------------------------------&zwsp;--------------------------------------------------------------
 Bitmap Heap Scan on tenk1  (cost=25.07..60.11 rows=10 width=244) (actual time=0.105..0.114 rows=10.00 loops=1)
   Recheck Cond: ((unique1 < 100) AND (unique2 > 9000))
   Heap Blocks: exact=10
   ->  BitmapAnd  (cost=25.07..25.07 rows=10 width=0) (actual time=0.100..0.101 rows=0.00 loops=1)
         ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..5.04 rows=100 width=0) (actual time=0.027..0.027 rows=100.00 loops=1)
               Index Cond: (unique1 < 100)
               Index Searches: 1
         ->  Bitmap Index Scan on tenk1_unique2  (cost=0.00..19.78 rows=999 width=0) (actual time=0.070..0.070 rows=999.00 loops=1)
               Index Cond: (unique2 > 9000)
               Index Searches: 1
 Planning Time: 0.162 ms
 Execution Time: 0.143 ms
```

   

   

    Keep in mind that because EXPLAIN ANALYZE actually
    runs the query, any side-effects will happen as usual, even though
    whatever results the query might output are discarded in favor of
    printing the EXPLAIN data.  If you want to analyze a
    data-modifying query without changing your tables, you can
    roll the command back afterwards, for example:

```
BEGIN;

EXPLAIN ANALYZE UPDATE tenk1 SET hundred = hundred + 1 WHERE unique1 < 100;

                                                           QUERY PLAN
-------------------------------------------------------------------&zwsp;-------------------------------------------------------------
 Update on tenk1  (cost=5.06..225.23 rows=0 width=0) (actual time=1.634..1.635 rows=0.00 loops=1)
   ->  Bitmap Heap Scan on tenk1  (cost=5.06..225.23 rows=100 width=10) (actual time=0.065..0.141 rows=100.00 loops=1)
         Recheck Cond: (unique1 < 100)
         Heap Blocks: exact=90
         Buffers: shared hit=4 read=2
         ->  Bitmap Index Scan on tenk1_unique1  (cost=0.00..5.04 rows=100 width=0) (actual time=0.031..0.031 rows=100.00 loops=1)
               Index Cond: (unique1 < 100)
               Index Searches: 1
               Buffers: shared read=2
 Planning Time: 0.151 ms
 Execution Time: 1.856 ms

ROLLBACK;
```

   

   

    As seen in this example, when the query is an INSERT,
    UPDATE, DELETE, or
    MERGE command, the actual work of
    applying the table changes is done by a top-level Insert, Update,
    Delete, or Merge plan node.  The plan nodes underneath this node perform
    the work of locating the old rows and/or computing the new data.
    So above, we see the same sort of bitmap table scan we've seen already,
    and its output is fed to an Update node that stores the updated rows.
    It's worth noting that although the data-modifying node can take a
    considerable amount of run time (here, it's consuming the lion's share
    of the time), the planner does not currently add anything to the cost
    estimates to account for that work.  That's because the work to be done is
    the same for every correct query plan, so it doesn't affect planning
    decisions.
   

   

    When an UPDATE, DELETE, or
    MERGE command affects a partitioned table or
    inheritance hierarchy, the output might look like this:

```
EXPLAIN UPDATE gtest_parent SET f1 = CURRENT_DATE WHERE f2 = 101;

                                       QUERY PLAN
-------------------------------------------------------------------&zwsp;---------------------
 Update on gtest_parent  (cost=0.00..3.06 rows=0 width=0)
   Update on gtest_child gtest_parent_1
   Update on gtest_child2 gtest_parent_2
   Update on gtest_child3 gtest_parent_3
   ->  Append  (cost=0.00..3.06 rows=3 width=14)
         ->  Seq Scan on gtest_child gtest_parent_1  (cost=0.00..1.01 rows=1 width=14)
               Filter: (f2 = 101)
         ->  Seq Scan on gtest_child2 gtest_parent_2  (cost=0.00..1.01 rows=1 width=14)
               Filter: (f2 = 101)
         ->  Seq Scan on gtest_child3 gtest_parent_3  (cost=0.00..1.01 rows=1 width=14)
               Filter: (f2 = 101)
```

    In this example the Update node needs to consider three child tables,
    but not the originally-mentioned partitioned table (since that never
    stores any data).  So there are three input
    scanning subplans, one per table.  For clarity, the Update node is
    annotated to show the specific target tables that will be updated, in the
    same order as the corresponding subplans.
   

   

    The Planning time shown by EXPLAIN
    ANALYZE is the time it took to generate the query plan from the
    parsed query and optimize it. It does not include parsing or rewriting.
   

   

    The Execution time shown by EXPLAIN
    ANALYZE includes executor start-up and shut-down time, as well
    as the time to run any triggers that are fired, but it does not include
    parsing, rewriting, or planning time.
    Time spent executing BEFORE triggers, if any, is included in
    the time for the related Insert, Update, or Delete node; but time
    spent executing AFTER triggers is not counted there because
    AFTER triggers are fired after completion of the whole plan.
    The total time spent in each trigger
    (either BEFORE or AFTER) is also shown separately.
    Note that deferred constraint triggers will not be executed
    until end of transaction and are thus not considered at all by
    EXPLAIN ANALYZE.
   

   

    The time shown for the top-level node does not include any time needed
    to convert the query's output data into displayable form or to send it
    to the client.  While EXPLAIN ANALYZE will never
    send the data to the client, it can be told to convert the query's
    output data to displayable form and measure the time needed for that,
    by specifying the SERIALIZE option.  That time will
    be shown separately, and it's also included in the
    total Execution time.
   

  

  

   
# Caveats

   

    There are two significant ways in which run times measured by
    EXPLAIN ANALYZE can deviate from normal execution of
    the same query.  First, since no output rows are delivered to the client,
    network transmission costs are not included.  I/O conversion costs are
    not included either unless SERIALIZE is specified.
    Second, the measurement overhead added by EXPLAIN
    ANALYZE can be significant, especially on machines with slow
    gettimeofday() operating-system calls. You can use the
     tool to measure the overhead of timing
    on your system.
   

   

    EXPLAIN results should not be extrapolated to situations
    much different from the one you are actually testing; for example,
    results on a toy-sized table cannot be assumed to apply to large tables.
    The planner's cost estimates are not linear and so it might choose
    a different plan for a larger or smaller table.  An extreme example
    is that on a table that only occupies one disk page, you'll nearly
    always get a sequential scan plan whether indexes are available or not.
    The planner realizes that it's going to take one disk page read to
    process the table in any case, so there's no value in expending additional
    page reads to look at an index.  (We saw this happening in the
    polygon_tbl example above.)
   

   

    There are cases in which the actual and estimated values won't match up
    well, but nothing is really wrong.  One such case occurs when
    plan node execution is stopped short by a LIMIT or similar
    effect.  For example, in the LIMIT query we used before,

```
EXPLAIN ANALYZE SELECT * FROM tenk1 WHERE unique1 < 100 AND unique2 > 9000 LIMIT 2;

                                                          QUERY PLAN
-------------------------------------------------------------------&zwsp;------------------------------------------------------------
 Limit  (cost=0.29..14.33 rows=2 width=244) (actual time=0.051..0.071 rows=2.00 loops=1)
   Buffers: shared hit=16
   ->  Index Scan using tenk1_unique2 on tenk1  (cost=0.29..70.50 rows=10 width=244) (actual time=0.051..0.070 rows=2.00 loops=1)
         Index Cond: (unique2 > 9000)
         Filter: (unique1 < 100)
         Rows Removed by Filter: 287
         Index Searches: 1
         Buffers: shared hit=16
 Planning Time: 0.077 ms
 Execution Time: 0.086 ms
```

    the estimated cost and row count for the Index Scan node are shown as
    though it were run to completion.  But in reality the Limit node stopped
    requesting rows after it got two, so the actual row count is only 2 and
    the run time is less than the cost estimate would suggest.  This is not
    an estimation error, only a discrepancy in the way the estimates and true
    values are displayed.
   

   

    Merge joins also have measurement artifacts that can confuse the unwary.
    A merge join will stop reading one input if it's exhausted the other input
    and the next key value in the one input is greater than the last key value
    of the other input; in such a case there can be no more matches and so no
    need to scan the rest of the first input.  This results in not reading all
    of one child, with results like those mentioned for LIMIT.
    Also, if the outer (first) child contains rows with duplicate key values,
    the inner (second) child is backed up and rescanned for the portion of its
    rows matching that key value.  EXPLAIN ANALYZE counts these
    repeated emissions of the same inner rows as if they were real additional
    rows.  When there are many outer duplicates, the reported actual row count
    for the inner child plan node can be significantly larger than the number
    of rows that are actually in the inner relation.
   

   

    BitmapAnd and BitmapOr nodes always report their actual row counts as zero,
    due to implementation limitations.
   

   

    Normally, EXPLAIN will display every plan node
    created by the planner.  However, there are cases where the executor
    can determine that certain nodes need not be executed because they
    cannot produce any rows, based on parameter values that were not
    available at planning time.  (Currently this can only happen for child
    nodes of an Append or MergeAppend node that is scanning a partitioned
    table.)  When this happens, those plan nodes are omitted from
    the EXPLAIN output and a Subplans
    Removed: N annotation appears
    instead.
   

  

 

 

  
# Statistics Used by the Planner

  
   statistics
   of the planner
  

  

   
# Single-Column Statistics

  

   As we saw in the previous section, the query planner needs to estimate
   the number of rows retrieved by a query in order to make good choices
   of query plans.  This section provides a quick look at the statistics
   that the system uses for these estimates.
  

  

   One component of the statistics is the total number of entries in
   each table and index, as well as the number of disk blocks occupied
   by each table and index.  This information is kept in the table
   pg_class,
   in the columns reltuples and
   relpages.  We can look at it with
   queries similar to this one:

```
SELECT relname, relkind, reltuples, relpages
FROM pg_class
WHERE relname LIKE 'tenk1%';

       relname        | relkind | reltuples | relpages
----------------------+---------+-----------+----------
 tenk1                | r       |     10000 |      345
 tenk1_hundred        | i       |     10000 |       11
 tenk1_thous_tenthous | i       |     10000 |       30
 tenk1_unique1        | i       |     10000 |       30
 tenk1_unique2        | i       |     10000 |       30
(5 rows)
```

   Here we can see that tenk1 contains 10000
   rows, as do its indexes, but the indexes are (unsurprisingly) much
   smaller than the table.
  

  

   For efficiency reasons, reltuples
   and relpages are not updated on-the-fly,
   and so they usually contain somewhat out-of-date values.
   They are updated by VACUUM, ANALYZE, and a
   few DDL commands such as CREATE INDEX.  A VACUUM
   or ANALYZE operation that does not scan the entire table
   (which is commonly the case) will incrementally update the
   reltuples count on the basis of the part
   of the table it did scan, resulting in an approximate value.
   In any case, the planner
   will scale the values it finds in pg_class
   to match the current physical table size, thus obtaining a closer
   approximation.
  

  
   pg_statistic
  

  

   Most queries retrieve only a fraction of the rows in a table, due
   to WHERE clauses that restrict the rows to be
   examined.  The planner thus needs to make an estimate of the
   selectivity of WHERE clauses, that is,
   the fraction of rows that match each condition in the
   WHERE clause.  The information used for this task is
   stored in the
   pg_statistic
   system catalog.  Entries in pg_statistic
   are updated by the ANALYZE and VACUUM
   ANALYZE commands, and are always approximate even when freshly
   updated.
  

  
   pg_stats
  

  

   Rather than look at pg_statistic directly,
   it's better to look at its view
   pg_stats
   when examining the statistics manually.  pg_stats
   is designed to be more easily readable.  Furthermore,
   pg_stats is readable by all, whereas
   pg_statistic is only readable by a superuser.
   (This prevents unprivileged users from learning something about
   the contents of other people's tables from the statistics.  The
   pg_stats view is restricted to show only
   rows about tables that the current user can read.)
   For example, we might do:

```
SELECT attname, inherited, n_distinct,
       array_to_string(most_common_vals, E'\n') as most_common_vals
FROM pg_stats
WHERE tablename = 'road';

 attname | inherited | n_distinct |          most_common_vals
---------+-----------+------------+------------------------------------
 name    | f         | -0.5681108 | I- 580                        Ramp+
         |           |            | I- 880                        Ramp+
         |           |            | Sp Railroad                       +
         |           |            | I- 580                            +
         |           |            | I- 680                        Ramp+
         |           |            | I- 80                         Ramp+
         |           |            | 14th                          St  +
         |           |            | I- 880                            +
         |           |            | Mac Arthur                    Blvd+
         |           |            | Mission                       Blvd+
...
 name    | t         |    -0.5125 | I- 580                        Ramp+
         |           |            | I- 880                        Ramp+
         |           |            | I- 580                            +
         |           |            | I- 680                        Ramp+
         |           |            | I- 80                         Ramp+
         |           |            | Sp Railroad                       +
         |           |            | I- 880                            +
         |           |            | State Hwy 13                  Ramp+
         |           |            | I- 80                             +
         |           |            | State Hwy 24                  Ramp+
...
 thepath | f         |          0 |
 thepath | t         |          0 |
(4 rows)
```

   Note that two rows are displayed for the same column, one corresponding
   to the complete inheritance hierarchy starting at the
   road table (inherited=t),
   and another one including only the road table itself
   (inherited=f).
   (For brevity, we have only shown the first ten most-common values for
   the name column.)
  

  

   The amount of information stored in pg_statistic
   by ANALYZE, in particular the maximum number of entries in the
   most_common_vals and histogram_bounds
   arrays for each column, can be set on a
   column-by-column basis using the ALTER TABLE SET STATISTICS
   command, or globally by setting the
    configuration variable.
   The default limit is presently 100 entries.  Raising the limit
   might allow more accurate planner estimates to be made, particularly for
   columns with irregular data distributions, at the price of consuming
   more space in pg_statistic and slightly more
   time to compute the estimates.  Conversely, a lower limit might be
   sufficient for columns with simple data distributions.
  

  

   Further details about the planner's use of statistics can be found in
   .
  

  

  

   
# Extended Statistics

   
    statistics
    of the planner
   

   
    correlation
    in the query planner
   

   
    pg_statistic_ext
   

   
    pg_statistic_ext_data
   

   

    It is common to see slow queries running bad execution plans because
    multiple columns used in the query clauses are correlated.
    The planner normally assumes that multiple conditions
    are independent of each other,
    an assumption that does not hold when column values are correlated.
    Regular statistics, because of their per-individual-column nature,
    cannot capture any knowledge about cross-column correlation.
    However, PostgreSQL has the ability to compute
    multivariate statistics, which can capture
    such information.
   

   

    Because the number of possible column combinations is very large,
    it's impractical to compute multivariate statistics automatically.
    Instead, extended statistics objects, more often
    called just statistics objects, can be created to instruct
    the server to obtain statistics across interesting sets of columns.
   

   

    Statistics objects are created using the
    CREATE STATISTICS command.
    Creation of such an object merely creates a catalog entry expressing
    interest in the statistics.  Actual data collection is performed
    by ANALYZE (either a manual command, or background
    auto-analyze).  The collected values can be examined in the
    pg_statistic_ext_data
    catalog.
   

   

    ANALYZE computes extended statistics based on the same
    sample of table rows that it takes for computing regular single-column
    statistics.  Since the sample size is increased by increasing the
    statistics target for the table or any of its columns (as described in
    the previous section), a larger statistics target will normally result in
    more accurate extended statistics, as well as more time spent calculating
    them.
   

   

    The following subsections describe the kinds of extended statistics
    that are currently supported.
   

   

    
# Functional Dependencies

    

     The simplest kind of extended statistics tracks functional
     dependencies, a concept used in definitions of database normal forms.
     We say that column b is functionally dependent on
     column a if knowledge of the value of
     a is sufficient to determine the value
     of b, that is there are no two rows having the same value
     of a but different values of b.
     In a fully normalized database, functional dependencies should exist
     only on primary keys and superkeys. However, in practice many data sets
     are not fully normalized for various reasons; intentional
     denormalization for performance reasons is a common example.
     Even in a fully normalized database, there may be partial correlation
     between some columns, which can be expressed as partial functional
     dependency.
    

    

     The existence of functional dependencies directly affects the accuracy
     of estimates in certain queries.  If a query contains conditions on
     both the independent and the dependent column(s), the
     conditions on the dependent columns do not further reduce the result
     size; but without knowledge of the functional dependency, the query
     planner will assume that the conditions are independent, resulting
     in underestimating the result size.
    

    

     To inform the planner about functional dependencies, ANALYZE
     can collect measurements of cross-column dependency. Assessing the
     degree of dependency between all sets of columns would be prohibitively
     expensive, so data collection is limited to those groups of columns
     appearing together in a statistics object defined with
     the dependencies option.  It is advisable to create
     dependencies statistics only for column groups that are
     strongly correlated, to avoid unnecessary overhead in both
     ANALYZE and later query planning.
    

    

     Here is an example of collecting functional-dependency statistics:

```
CREATE STATISTICS stts (dependencies) ON city, zip FROM zipcodes;

ANALYZE zipcodes;

SELECT stxkeys AS k, jsonb_pretty(stxddependencies::text::jsonb) AS dep
  FROM pg_statistic_ext JOIN pg_statistic_ext_data ON (oid = stxoid)
  WHERE stxname = 'stts';
-[ RECORD 1 ]--------------------
k   | 1 5
dep | [                          +
    |     {                      +
    |         "degree": 1.000000,+
    |         "attributes": [    +
    |             1              +
    |         ],                 +
    |         "dependency": 5    +
    |     },                     +
    |     {                      +
    |         "degree": 0.423130,+
    |         "attributes": [    +
    |             5              +
    |         ],                 +
    |         "dependency": 1    +
    |     }                      +
    | ]
(1 row)
```

     Here it can be seen that column 1 (zip code) fully determines column
     5 (city) so the coefficient is 1.0, while city only determines zip code
     about 42% of the time, meaning that there are many cities (58%) that are
     represented by more than a single ZIP code.
    

    

     When computing the selectivity for a query involving functionally
     dependent columns, the planner adjusts the per-condition selectivity
     estimates using the dependency coefficients so as not to produce
     an underestimate.
    

    
     
# Limitations of Functional Dependencies

     

      Functional dependencies are currently only applied when considering
      simple equality conditions that compare columns to constant values,
      and IN clauses with constant values.
      They are not used to improve estimates for equality conditions
      comparing two columns or comparing a column to an expression, nor for
      range clauses, LIKE or any other type of condition.
     

     

      When estimating with functional dependencies, the planner assumes that
      conditions on the involved columns are compatible and hence redundant.
      If they are incompatible, the correct estimate would be zero rows, but
      that possibility is not considered.  For example, given a query like

```
SELECT * FROM zipcodes WHERE city = 'San Francisco' AND zip = '94105';
```

      the planner will disregard the city clause as not
      changing the selectivity, which is correct.  However, it will make
      the same assumption about

```
SELECT * FROM zipcodes WHERE city = 'San Francisco' AND zip = '90210';
```

      even though there will really be zero rows satisfying this query.
      Functional dependency statistics do not provide enough information
      to conclude that, however.
     

     

      In many practical situations, this assumption is usually satisfied;
      for example, there might be a GUI in the application that only allows
      selecting compatible city and ZIP code values to use in a query.
      But if that's not the case, functional dependencies may not be a viable
      option.
     

    
   

   

    
# Multivariate N-Distinct Counts

    

     Single-column statistics store the number of distinct values in each
     column.  Estimates of the number of distinct values when combining more
     than one column (for example, for GROUP BY a, b) are
     frequently wrong when the planner only has single-column statistical
     data, causing it to select bad plans.
    

    

     To improve such estimates, ANALYZE can collect n-distinct
     statistics for groups of columns.  As before, it's impractical to do
     this for every possible column grouping, so data is collected only for
     those groups of columns appearing together in a statistics object
     defined with the ndistinct option.  Data will be collected
     for each possible combination of two or more columns from the set of
     listed columns.
    

    

     Continuing the previous example, the n-distinct counts in a
     table of ZIP codes might look like the following:

```
CREATE STATISTICS stts2 (ndistinct) ON city, state, zip FROM zipcodes;

ANALYZE zipcodes;

SELECT stxkeys AS k, jsonb_pretty(stxdndistinct::text::jsonb) AS nd
  FROM pg_statistic_ext JOIN pg_statistic_ext_data on (oid = stxoid)
  WHERE stxname = 'stts2';
-[ RECORD 1 ]-------------------
k  | 1 2 5
nd | [                          +
   |     {                      +
   |         "ndistinct": 33178,+
   |         "attributes": [    +
   |             1,             +
   |             2              +
   |         ]                  +
   |     },                     +
   |     {                      +
   |         "ndistinct": 33178,+
   |         "attributes": [    +
   |             1,             +
   |             5              +
   |         ]                  +
   |     },                     +
   |     {                      +
   |         "ndistinct": 27435,+
   |         "attributes": [    +
   |             2,             +
   |             5              +
   |         ]                  +
   |     },                     +
   |     {                      +
   |         "ndistinct": 33178,+
   |         "attributes": [    +
   |             1,             +
   |             2,             +
   |             5              +
   |         ]                  +
   |     }                      +
   | ]
(1 row)
```

     This indicates that there are three combinations of columns that
     have 33178 distinct values: ZIP code and state; ZIP code and city;
     and ZIP code, city and state (the fact that they are all equal is
     expected given that ZIP code alone is unique in this table).  On the
     other hand, the combination of city and state has only 27435 distinct
     values.
    

    

     It's advisable to create ndistinct statistics objects only
     on combinations of columns that are actually used for grouping, and
     for which misestimation of the number of groups is resulting in bad
     plans.  Otherwise, the ANALYZE cycles are just wasted.
    

   

   

    
# Multivariate MCV Lists

    

     Another type of statistic stored for each column are most-common value
     lists.  This allows very accurate estimates for individual columns, but
     may result in significant misestimates for queries with conditions on
     multiple columns.
    

    

     To improve such estimates, ANALYZE can collect MCV
     lists on combinations of columns.  Similarly to functional dependencies
     and n-distinct coefficients, it's impractical to do this for every
     possible column grouping.  Even more so in this case, as the MCV list
     (unlike functional dependencies and n-distinct coefficients) does store
     the common column values.  So data is collected only for those groups
     of columns appearing together in a statistics object defined with the
     mcv option.
    

    

     Continuing the previous example, the MCV list for a table of ZIP codes
     might look like the following (unlike for simpler types of statistics,
     a function is required for inspection of MCV contents):

```
CREATE STATISTICS stts3 (mcv) ON city, state FROM zipcodes;

ANALYZE zipcodes;

SELECT m.* FROM pg_statistic_ext join pg_statistic_ext_data on (oid = stxoid),
                pg_mcv_list_items(stxdmcv) m WHERE stxname = 'stts3';

 index |         values         | nulls | frequency | base_frequency
-------+------------------------+-------+-----------+----------------
     0 | {Washington, DC}       | {f,f} |  0.003467 |        2.7e-05
     1 | {Apo, AE}              | {f,f} |  0.003067 |        1.9e-05
     2 | {Houston, TX}          | {f,f} |  0.002167 |       0.000133
     3 | {El Paso, TX}          | {f,f} |     0.002 |       0.000113
     4 | {New York, NY}         | {f,f} |  0.001967 |       0.000114
     5 | {Atlanta, GA}          | {f,f} |  0.001633 |        3.3e-05
     6 | {Sacramento, CA}       | {f,f} |  0.001433 |        7.8e-05
     7 | {Miami, FL}            | {f,f} |    0.0014 |          6e-05
     8 | {Dallas, TX}           | {f,f} |  0.001367 |        8.8e-05
     9 | {Chicago, IL}          | {f,f} |  0.001333 |        5.1e-05
   ...
(99 rows)
```

     This indicates that the most common combination of city and state is
     Washington in DC, with actual frequency (in the sample) about 0.35%.
     The base frequency of the combination (as computed from the simple
     per-column frequencies) is only 0.0027%, resulting in two orders of
     magnitude under-estimates.
    

    

     It's advisable to create MCV statistics objects only
     on combinations of columns that are actually used in conditions together,
     and for which misestimation of the number of groups is resulting in bad
     plans.  Otherwise, the ANALYZE and planning cycles
     are just wasted.
    

   

  

 

 

  
# Controlling the Planner with Explicit JOIN Clauses

  
   join
   controlling the order
  

  

   It is possible
   to control the query planner to some extent by using the explicit JOIN
   syntax.  To see why this matters, we first need some background.
  

  

   In a simple join query, such as:

```
SELECT * FROM a, b, c WHERE a.id = b.id AND b.ref = c.id;
```

   the planner is free to join the given tables in any order.  For
   example, it could generate a query plan that joins A to B, using
   the WHERE condition a.id = b.id, and then
   joins C to this joined table, using the other WHERE
   condition.  Or it could join B to C and then join A to that result.
   Or it could join A to C and then join them with B &mdash; but that
   would be inefficient, since the full Cartesian product of A and C
   would have to be formed, there being no applicable condition in the
   WHERE clause to allow optimization of the join.  (All
   joins in the PostgreSQL executor happen
   between two input tables, so it's necessary to build up the result
   in one or another of these fashions.)  The important point is that
   these different join possibilities give semantically equivalent
   results but might have hugely different execution costs.  Therefore,
   the planner will explore all of them to try to find the most
   efficient query plan.
  

  

   When a query only involves two or three tables, there aren't many join
   orders to worry about.  But the number of possible join orders grows
   exponentially as the number of tables expands.  Beyond ten or so input
   tables it's no longer practical to do an exhaustive search of all the
   possibilities, and even for six or seven tables planning might take an
   annoyingly long time.  When there are too many input tables, the
   PostgreSQL planner will switch from exhaustive
   search to a genetic probabilistic search
   through a limited number of possibilities.  (The switch-over threshold is
   set by the  run-time
   parameter.)
   The genetic search takes less time, but it won't
   necessarily find the best possible plan.
  

  

   When the query involves outer joins, the planner has less freedom
   than it does for plain (inner) joins. For example, consider:

```
SELECT * FROM a LEFT JOIN (b JOIN c ON (b.ref = c.id)) ON (a.id = b.id);
```

   Although this query's restrictions are superficially similar to the
   previous example, the semantics are different because a row must be
   emitted for each row of A that has no matching row in the join of B and C.
   Therefore the planner has no choice of join order here: it must join
   B to C and then join A to that result.  Accordingly, this query takes
   less time to plan than the previous query.  In other cases, the planner
   might be able to determine that more than one join order is safe.
   For example, given:

```
SELECT * FROM a LEFT JOIN b ON (a.bid = b.id) LEFT JOIN c ON (a.cid = c.id);
```

   it is valid to join A to either B or C first.  Currently, only
   FULL JOIN completely constrains the join order.  Most
   practical cases involving LEFT JOIN or RIGHT JOIN
   can be rearranged to some extent.
  

  

   Explicit inner join syntax (INNER JOIN, CROSS
   JOIN, or unadorned JOIN) is semantically the same as
   listing the input relations in FROM, so it does not
   constrain the join order.
  

  

   Even though most kinds of JOIN don't completely constrain
   the join order, it is possible to instruct the
   PostgreSQL query planner to treat all
   JOIN clauses as constraining the join order anyway.
   For example, these three queries are logically equivalent:

```
SELECT * FROM a, b, c WHERE a.id = b.id AND b.ref = c.id;
SELECT * FROM a CROSS JOIN b CROSS JOIN c WHERE a.id = b.id AND b.ref = c.id;
SELECT * FROM a JOIN (b JOIN c ON (b.ref = c.id)) ON (a.id = b.id);
```

   But if we tell the planner to honor the JOIN order,
   the second and third take less time to plan than the first.  This effect
   is not worth worrying about for only three tables, but it can be a
   lifesaver with many tables.
  

  

   To force the planner to follow the join order laid out by explicit
   JOINs,
   set the  run-time parameter to 1.
   (Other possible values are discussed below.)
  

  

   You do not need to constrain the join order completely in order to
   cut search time, because it's OK to use JOIN operators
   within items of a plain FROM list.  For example, consider:

```
SELECT * FROM a CROSS JOIN b, c, d, e WHERE ...;
```

   With join_collapse_limit = 1, this
   forces the planner to join A to B before joining them to other tables,
   but doesn't constrain its choices otherwise.  In this example, the
   number of possible join orders is reduced by a factor of 5.
  

  

   Constraining the planner's search in this way is a useful technique
   both for reducing planning time and for directing the planner to a
   good query plan.  If the planner chooses a bad join order by default,
   you can force it to choose a better order via JOIN syntax
   &mdash; assuming that you know of a better order, that is.  Experimentation
   is recommended.
  

  

   A closely related issue that affects planning time is collapsing of
   subqueries into their parent query.  For example, consider:

```
SELECT *
FROM x, y,
    (SELECT * FROM a, b, c WHERE something) AS ss
WHERE somethingelse;
```

   This situation might arise from use of a view that contains a join;
   the view's SELECT rule will be inserted in place of the view
   reference, yielding a query much like the above.  Normally, the planner
   will try to collapse the subquery into the parent, yielding:

```
SELECT * FROM x, y, a, b, c WHERE something AND somethingelse;
```

   This usually results in a better plan than planning the subquery
   separately.  (For example, the outer WHERE conditions might be such that
   joining X to A first eliminates many rows of A, thus avoiding the need to
   form the full logical output of the subquery.)  But at the same time,
   we have increased the planning time; here, we have a five-way join
   problem replacing two separate three-way join problems.  Because of the
   exponential growth of the number of possibilities, this makes a big
   difference.  The planner tries to avoid getting stuck in huge join search
   problems by not collapsing a subquery if more than from_collapse_limit
   FROM items would result in the parent
   query.  You can trade off planning time against quality of plan by
   adjusting this run-time parameter up or down.
  

  

    and 
   are similarly named because they do almost the same thing: one controls
   when the planner will flatten out subqueries, and the
   other controls when it will flatten out explicit joins.  Typically
   you would either set join_collapse_limit equal to
   from_collapse_limit (so that explicit joins and subqueries
   act similarly) or set join_collapse_limit to 1 (if you want
   to control join order with explicit joins).  But you might set them
   differently if you are trying to fine-tune the trade-off between planning
   time and run time.
  

 

 

  
# Populating a Database

  

   One might need to insert a large amount of data when first populating
   a database. This section contains some suggestions on how to make
   this process as efficient as possible.
  

  

   
# Disable Autocommit

   
    autocommit
    bulk-loading data
   

   

    When using multiple INSERTs, turn off autocommit and just do
    one commit at the end.  (In plain
    SQL, this means issuing BEGIN at the start and
    COMMIT at the end.  Some client libraries might
    do this behind your back, in which case you need to make sure the
    library does it when you want it done.)  If you allow each
    insertion to be committed separately,
    PostgreSQL is doing a lot of work for
    each row that is added.  An additional benefit of doing all
    insertions in one transaction is that if the insertion of one row
    were to fail then the insertion of all rows inserted up to that
    point would be rolled back, so you won't be stuck with partially
    loaded data.
   

  

  

   
# Use COPY

   

    Use COPY to load
    all the rows in one command, instead of using a series of
    INSERT commands.  The COPY
    command is optimized for loading large numbers of rows; it is less
    flexible than INSERT, but incurs significantly
    less overhead for large data loads. Since COPY
    is a single command, there is no need to disable autocommit if you
    use this method to populate a table.
   

   

    If you cannot use COPY, it might help to use PREPARE to create a
    prepared INSERT statement, and then use
    EXECUTE as many times as required.  This avoids
    some of the overhead of repeatedly parsing and planning
    INSERT. Different interfaces provide this facility
    in different ways; look for prepared statements in the interface
    documentation.
   

   

    Note that loading a large number of rows using
    COPY is almost always faster than using
    INSERT, even if PREPARE is used and
    multiple insertions are batched into a single transaction.
   

   

    COPY is fastest when used within the same
    transaction as an earlier CREATE TABLE or
    TRUNCATE command. In such cases no WAL
    needs to be written, because in case of an error, the files
    containing the newly loaded data will be removed anyway.
    However, this consideration only applies when
     is minimal
    as all commands must write WAL otherwise.
   

  

  

   
# Remove Indexes

   

    If you are loading a freshly created table, the fastest method is to
    create the table, bulk load the table's data using
    COPY, then create any indexes needed for the
    table.  Creating an index on pre-existing data is quicker than
    updating it incrementally as each row is loaded.
   

   

    If you are adding large amounts of data to an existing table,
    it might be a win to drop the indexes,
    load the table, and then recreate the indexes.  Of course, the
    database performance for other users might suffer
    during the time the indexes are missing.  One should also think
    twice before dropping a unique index, since the error checking
    afforded by the unique constraint will be lost while the index is
    missing.
   

  

  

   
# Remove Foreign Key Constraints

   

    Just as with indexes, a foreign key constraint can be checked
    in bulk more efficiently than row-by-row.  So it might be
    useful to drop foreign key constraints, load data, and re-create
    the constraints.  Again, there is a trade-off between data load
    speed and loss of error checking while the constraint is missing.
   

   

    What's more, when you load data into a table with existing foreign key
    constraints, each new row requires an entry in the server's list of
    pending trigger events (since it is the firing of a trigger that checks
    the row's foreign key constraint).  Loading many millions of rows can
    cause the trigger event queue to overflow available memory, leading to
    intolerable swapping or even outright failure of the command.  Therefore
    it may be necessary, not just desirable, to drop and re-apply
    foreign keys when loading large amounts of data.  If temporarily removing
    the constraint isn't acceptable, the only other recourse may be to split
    up the load operation into smaller transactions.
   

  

  

   
# Increase maintenance_work_mem

   

    Temporarily increasing the 
    configuration variable when loading large amounts of data can
    lead to improved performance.  This will help to speed up CREATE
    INDEX commands and ALTER TABLE ADD FOREIGN KEY commands.
    It won't do much for COPY itself, so this advice is
    only useful when you are using one or both of the above techniques.
   

  

  

   
# Increase max_wal_size

   

    Temporarily increasing the 
    configuration variable can also
    make large data loads faster.  This is because loading a large
    amount of data into PostgreSQL will
    cause checkpoints to occur more often than the normal checkpoint
    frequency (specified by the checkpoint_timeout
    configuration variable). Whenever a checkpoint occurs, all dirty
    pages must be flushed to disk. By increasing
    max_wal_size temporarily during bulk
    data loads, the number of checkpoints that are required can be
    reduced.
   

  

  

   
# Disable WAL Archival and Streaming Replication

   

    When loading large amounts of data into an installation that uses
    WAL archiving or streaming replication, it might be faster to take a
    new base backup after the load has completed than to process a large
    amount of incremental WAL data.  To prevent incremental WAL logging
    while loading, disable archiving and streaming replication, by setting
     to minimal,
     to off, and
     to zero.
    But note that changing these settings requires a server restart,
    and makes any base backups taken before unavailable for archive
    recovery and standby server, which may lead to data loss.
   

   

    Aside from avoiding the time for the archiver or WAL sender to process the
    WAL data, doing this will actually make certain commands faster, because
    they do not to write WAL at all if wal_level
    is minimal and the current subtransaction (or top-level
    transaction) created or truncated the table or index they change.  (They
    can guarantee crash safety more cheaply by doing
    an fsync at the end than by writing WAL.)
   

  

  

   
# Run ANALYZE Afterwards

   

    Whenever you have significantly altered the distribution of data
    within a table, running ANALYZE is strongly recommended. This
    includes bulk loading large amounts of data into the table.  Running
    ANALYZE (or VACUUM ANALYZE)
    ensures that the planner has up-to-date statistics about the
    table.  With no statistics or obsolete statistics, the planner might
    make poor decisions during query planning, leading to poor
    performance on any tables with inaccurate or nonexistent
    statistics.  Note that if the autovacuum daemon is enabled, it might
    run ANALYZE automatically; see
    
    and  for more information.
   

  

  

   
# Some Notes about pg_dump

   

    Dump scripts generated by pg_dump automatically apply
    several, but not all, of the above guidelines.  To restore a
    pg_dump dump as quickly as possible, you need to
    do a few extra things manually.  (Note that these points apply while
    restoring a dump, not while creating it.
    The same points apply whether loading a text dump with
    psql or using pg_restore to load
    from a pg_dump archive file.)
   

   

    By default, pg_dump uses COPY, and when
    it is generating a complete schema-and-data dump, it is careful to
    load data before creating indexes and foreign keys.  So in this case
    several guidelines are handled automatically.  What is left
    for you to do is to:
    

     

      

       Set appropriate (i.e., larger than normal) values for
       maintenance_work_mem and
       max_wal_size.
      

     

     

      

       If using WAL archiving or streaming replication, consider disabling
       them during the restore. To do that, set archive_mode
       to off,
       wal_level to minimal, and
       max_wal_senders to zero before loading the dump.
       Afterwards, set them back to the right values and take a fresh
       base backup.
      

     

     

      

       Experiment with the parallel dump and restore modes of both
       pg_dump and pg_restore and find the
       optimal number of concurrent jobs to use. Dumping and restoring in
       parallel by means of the -j option should give you a
       significantly higher performance over the serial mode.
      

     

     

      

       Consider whether the whole dump should be restored as a single
       transaction.  To do that, pass the -1 or
       --single-transaction command-line option to
       psql or pg_restore. When using this
       mode, even the smallest of errors will rollback the entire restore,
       possibly discarding many hours of processing.  Depending on how
       interrelated the data is, that might seem preferable to manual cleanup,
       or not.  COPY commands will run fastest if you use a single
       transaction and have WAL archiving turned off.
      

     

     

      

       If multiple CPUs are available in the database server, consider using
       pg_restore's --jobs option.  This
       allows concurrent data loading and index creation.
      

     

     

      

       Run ANALYZE afterwards.
      

     

    

   

   

    A data-only dump will still use COPY, but it does not
    drop or recreate indexes, and it does not normally touch foreign
    keys.

     
      

       You can get the effect of disabling foreign keys by using
       the --disable-triggers option &mdash; but realize that
       that eliminates, rather than just postpones, foreign key
       validation, and so it is possible to insert bad data if you use it.
      

     

    So when loading a data-only dump, it is up to you to drop and recreate
    indexes and foreign keys if you wish to use those techniques.
    It's still useful to increase max_wal_size
    while loading the data, but don't bother increasing
    maintenance_work_mem; rather, you'd do that while
    manually recreating indexes and foreign keys afterwards.
    And don't forget to ANALYZE when you're done; see
    
    and  for more information.
   

  

  

  

   
# Non-Durable Settings

   
    non-durable
   

   

    Durability is a database feature that guarantees the recording of
    committed transactions even if the server crashes or loses
    power.  However, durability adds significant database overhead,
    so if your site does not require such a guarantee,
    PostgreSQL can be configured to run
    much faster.  The following are configuration changes you can make
    to improve performance in such cases.  Except as noted below, durability
    is still guaranteed in case of a crash of the database software;
    only an abrupt operating system crash creates a risk of data loss
    or corruption when these settings are used.

    

     

      

       Place the database cluster's data directory in a memory-backed
       file system (i.e., RAM disk).  This eliminates all
       database disk I/O, but limits data storage to the amount of
       available memory (and perhaps swap).
      

     

     

      

       Turn off ;  there is no need to flush
       data to disk.
      

     

     

      

       Turn off ;  there might be no
       need to force WAL writes to disk on every
       commit.  This setting does risk transaction loss (though not data
       corruption) in case of a crash of the database.
      

     

     

      

       Turn off ;  there is no need
       to guard against partial page writes.
      

     

     

      

       Increase  and ; this reduces the frequency
       of checkpoints, but increases the storage requirements of
       /pg_wal.
      

     

     

      

       Create unlogged
       tables to avoid WAL writes, though it
       makes the tables non-crash-safe.
