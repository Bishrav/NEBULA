# btree_gist &mdash; GiST operator classes with B-tree behavior

 
  btree_gist
 

 

  btree_gist provides GiST index operator classes that
  implement B-tree equivalent behavior for the data types
  int2, int4, int8, float4,
  float8, numeric, timestamp with time zone,
  timestamp without time zone, time with time zone,
  time without time zone, date, interval,
  oid, money, char,
  varchar, text, bytea, bit,
  varbit, macaddr, macaddr8, inet,
  cidr, uuid, bool and all enum types.
 

 

  In general, these operator classes will not outperform the equivalent
  standard B-tree index methods, and they lack one major feature of the
  standard B-tree code: the ability to enforce uniqueness.  However,
  they provide some other features that are not available with a B-tree
  index, as described below.  Also, these operator classes are useful
  when a multicolumn GiST index is needed, wherein some of the columns
  are of data types that are only indexable with GiST but other columns
  are just simple data types.  Lastly, these operator classes are useful for
  GiST testing and as a base for developing other GiST operator classes.
 

 

  In addition to the typical B-tree search operators, btree_gist
  also provides index support for <> (not
  equals). This may be useful in combination with an
  exclusion constraint,
  as described below.
 

 

  Also, for data types for which there is a natural distance metric,
  btree_gist defines a distance operator <->,
  and provides GiST index support for nearest-neighbor searches using
  this operator.  Distance operators are provided for
  int2, int4, int8, float4,
  float8, timestamp with time zone,
  timestamp without time zone,
  time without time zone, date, interval,
  oid, and money.
 

 

  By default btree_gist builds GiST index with
  sortsupport in sorted mode. This usually results in
  much faster index built speed. It is still possible to revert to buffered built strategy
  by using the buffering parameter when creating the index.
 

 

  This module is considered trusted, that is, it can be
  installed by non-superusers who have CREATE privilege
  on the current database.
 

 

  
# Example Usage

  

   Simple example using btree_gist instead of btree:
  

```
CREATE TABLE test (a int4);
-- create index
CREATE INDEX testidx ON test USING GIST (a);
-- query
SELECT * FROM test WHERE a < 10;
-- nearest-neighbor search: find the ten entries closest to "42"
SELECT *, a <-> 42 AS dist FROM test ORDER BY a <-> 42 LIMIT 10;
```

  

   Use an exclusion
   constraint to enforce the rule that a cage at a zoo
   can contain only one kind of animal:
  

```
=> CREATE TABLE zoo (
  cage   INTEGER,
  animal TEXT,
  EXCLUDE USING GIST (cage WITH =, animal WITH <>)
);

=> INSERT INTO zoo VALUES(123, 'zebra');
INSERT 0 1
=> INSERT INTO zoo VALUES(123, 'zebra');
INSERT 0 1
=> INSERT INTO zoo VALUES(123, 'lion');
ERROR:  conflicting key value violates exclusion constraint "zoo_cage_animal_excl"
DETAIL:  Key (cage, animal)=(123, lion) conflicts with existing key (cage, animal)=(123, zebra).
=> INSERT INTO zoo VALUES(124, 'lion');
INSERT 0 1
```

 

 

  
# btree_gist Indexes
   on inet/cidr Columns

  

   The gist_inet_ops and gist_cidr_ops
   operator classes provided by btree_gist have been
   shown to be unreliable: index searches may fail to find relevant rows due
   to approximations used in creating the index entries.  This is unfixable
   without redefining the contents of indexes that use these opclasses.
   Therefore, these opclasses are being deprecated in favor of the built-in
   GiST inet_ops opclass, which does not share the design
   flaw.
  

  

   As a first step, PostgreSQL version 19 removes
   the default-opclass marking from gist_inet_ops
   and gist_cidr_ops, instead
   marking inet_ops as default for inet
   and cidr columns.  This will result in transparently
   substituting inet_ops for the faulty opclasses in most
   contexts.  It is still possible to create indexes using the faulty
   opclasses, if really necessary, by explicitly specifying which opclass to
   use; for example

```
CREATE TABLE mytable (addr inet);
CREATE INDEX dubious_index ON mytable USING GIST (addr gist_inet_ops);
```

  

  

   However, pg_upgrade cannot handle this change
   due to implementation limitations.  If asked to upgrade a pre-v19
   database that contains gist_inet_ops
   or gist_cidr_ops
   indexes, pg_upgrade will fail and tell you to
   replace those indexes before upgrading.  This would look approximately
   like

```
CREATE INDEX good_index ON mytable USING GIST (addr inet_ops);
DROP INDEX bad_index;
```

  

 

 

  
# Authors

  

   Teodor Sigaev (teodor@stack.net),
   Oleg Bartunov (oleg@sai.msu.su),
   Janko Richter (jankorichter@yahoo.de), and
   Paul Jungwirth (pj@illuminatedcomputing.com).  See
   
   for additional information.
