# pg_freespacemap &mdash; examine the free space map

 
  pg_freespacemap
 

 

  The pg_freespacemap module provides a means for examining the
  free space map (FSM).
  It provides a function called pg_freespace, or two
  overloaded functions, to be precise. The functions show the value recorded in
  the free space map for a given page, or for all pages in the relation.
 

 

  By default use is restricted to superusers and roles with privileges of the
  pg_stat_scan_tables role. Access may be granted to others
  using GRANT.
 

 

  
# Functions

  

   

    

     pg_freespace(rel regclass IN, blkno bigint IN) returns int2
     
      pg_freespace
     
    

    

     

      Returns the amount of free space on the page of the relation, specified
      by blkno, according to the FSM.
     

    

   

   

    

     pg_freespace(rel regclass IN, blkno OUT bigint, avail OUT int2)
    

    

     

      Displays the amount of free space on each page of the relation,
      according to the FSM. A set of
      (blkno bigint, avail int2)
      tuples is returned, one tuple for each page in the relation.
     

    

   

  

  

   The values stored in the free space map are not exact. They're rounded
   to precision of 1/256th of BLCKSZ (32 bytes with default BLCKSZ), and
   they're not kept fully up-to-date as tuples are inserted and updated.
  

  

   For indexes, what is tracked is entirely-unused pages, rather than free
   space within pages.  Therefore, the values are not meaningful, just
   whether a page is in-use or empty.
  

 

 

  
# Sample Output

```
postgres=# SELECT * FROM pg_freespace('foo');
 blkno | avail
-------+-------
     0 |     0
     1 |     0
     2 |     0
     3 |    32
     4 |   704
     5 |   704
     6 |   704
     7 |  1216
     8 |   704
     9 |   704
    10 |   704
    11 |   704
    12 |   704
    13 |   704
    14 |   704
    15 |   704
    16 |   704
    17 |   704
    18 |   704
    19 |  3648
(20 rows)

postgres=# SELECT * FROM pg_freespace('foo', 7);
 pg_freespace
--------------
         1216
(1 row)
```

 

 

  
# Author

  

   Original version by Mark Kirkwood markir@paradise.net.nz.
   Rewritten in version 8.4 to suit new FSM implementation
   by Heikki Linnakangas heikki@enterprisedb.com
