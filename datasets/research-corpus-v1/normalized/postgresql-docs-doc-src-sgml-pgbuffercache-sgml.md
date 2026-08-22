# pg_buffercache &mdash; inspect PostgreSQL
    buffer cache state

 
  pg_buffercache
 

 

  The pg_buffercache module provides a means for
  examining what's happening in the shared buffer cache in real time.
  It also offers a low-level way to evict data from it, for testing
  purposes.
 

 
  pg_buffercache_pages
 

 
  pg_buffercache_numa
 

 
  pg_buffercache_summary
 

 
  pg_buffercache_usage_counts
 

 
  pg_buffercache_evict
 

 
  pg_buffercache_evict_relation
 

 
  pg_buffercache_evict_all
 

 
  pg_buffercache_mark_dirty
 

 
  pg_buffercache_mark_dirty_relation
 

 
  pg_buffercache_mark_dirty_all
 

 

  This module provides the pg_buffercache_pages()
  function (wrapped in the pg_buffercache view), the
  pg_buffercache_os_pages() function (wrapped in the
  pg_buffercache_os_pages and
  pg_buffercache_numa views), the
  pg_buffercache_summary() function, the
  pg_buffercache_usage_counts() function, the
  pg_buffercache_evict() function, the
  pg_buffercache_evict_relation() function, the
  pg_buffercache_evict_all() function, the
  pg_buffercache_mark_dirty() function, the
  pg_buffercache_mark_dirty_relation() function and the
  pg_buffercache_mark_dirty_all() function.
 

 

  The pg_buffercache_pages() function returns a set of
  records, each row describing the state of one shared buffer entry. The
  pg_buffercache view wraps the function for
  convenient use.
 

 

  The pg_buffercache_os_pages() function provides OS
  pages mappings for shared buffer entries. When its argument is
  true, it also provides NUMA node
  mappings for shared buffer entries (this information is not part of
  pg_buffercache_pages() itself, as it is much
  slower to retrieve).
  The pg_buffercache_os_pages and
  pg_buffercache_numa views wrap the function for
  convenient use, with its argument set to false and
  true respectively.
 

 

  The pg_buffercache_summary() function returns a single
  row summarizing the state of the shared buffer cache.
 

 

  The pg_buffercache_usage_counts() function returns a set
  of records, each row describing the number of buffers with a given usage
  count.
 

 

  By default, use of the above functions is restricted to superusers and roles
  with privileges of the pg_monitor role. Access may be
  granted to others using GRANT.
 

 

  The pg_buffercache_evict() function allows a block to
  be evicted from the buffer pool given a buffer identifier.  Use of this
  function is restricted to superusers only.
 

 

  The pg_buffercache_evict_relation() function allows all
  unpinned shared buffers in the relation to be evicted from the buffer pool
  given a relation identifier.  Use of this function is restricted to
  superusers only.
 

 

  The pg_buffercache_evict_all() function allows all
  unpinned shared buffers to be evicted in the buffer pool.  Use of this
  function is restricted to superusers only.
 

 

  The pg_buffercache_mark_dirty() function allows a block
  to be marked as dirty in the buffer pool given a buffer identifier.  Use of
  this function is restricted to superusers only.
 

  The pg_buffercache_mark_dirty_relation() function
  allows all unpinned shared buffers in the relation to be marked as dirty in
  the buffer pool given a relation identifier.  Use of this function is
  restricted to superusers only.

 

  The pg_buffercache_mark_dirty_all() function allows all
  unpinned shared buffers to be marked as dirty in the buffer pool. Use of
  this function is restricted to superusers only.
 

 

  
# The pg_buffercache View

  

   The definitions of the columns exposed by the view are shown in .
  

  
   
# pg_buffercache Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       bufferid integer
      

      

       ID, in the range 1..shared_buffers
      

     

     
      

       relfilenode oid
       (references pg_class.relfilenode)
      

      

       Filenode number of the relation
      

     

     
      

       reltablespace oid
       (references pg_tablespace.oid)
      

      

       Tablespace OID of the relation
      

     

     
      

       reldatabase oid
       (references pg_database.oid)
      

      

       Database OID of the relation
      

     

     
      

       relforknumber smallint
      

      

       Fork number within the relation;  see
       common/relpath.h
      

     

     
      

       relblocknumber bigint
      

      

       Page number within the relation
      

     

     
      

       isdirty boolean
      

      

       Is the page dirty?
      

     

     
      

       usagecount smallint
      

      

       Clock-sweep access count
      

     

     
      

       pinning_backends integer
      

      

       Number of backends pinning this buffer
      

     
    
   
  

  

   There is one row for each buffer in the shared cache. Unused buffers are
   shown with all fields null except bufferid.  Shared system
   catalogs are shown as belonging to database zero.
  

  

   Because the cache is shared by all the databases, there will normally be
   pages from relations not belonging to the current database.  This means
   that there may not be matching join rows in pg_class for
   some rows, or that there could even be incorrect joins.  If you are
   trying to join against pg_class, it's a good idea to
   restrict the join to rows having reldatabase equal to
   the current database's OID or zero.
  

  

   Since buffer manager locks are not taken to copy the buffer state data that
   the view will display, accessing pg_buffercache view
   has less impact on normal buffer activity but it doesn't provide a consistent
   set of results across all buffers.  However, we ensure that the information of
   each buffer is self-consistent.
  

 

 

  
# The pg_buffercache_os_pages View

  

   The definitions of the columns exposed by the view are shown in
   .
  

  
   
# pg_buffercache_os_pages Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       bufferid integer
      

      

       ID, in the range 1..shared_buffers
      

     

     
      

       os_page_num bigint
      

      

       Number of OS memory page for this buffer
      

     

    
   
  

 

 

  
# The pg_buffercache_numa View

  

   The definitions of the columns exposed by the view are shown in .
  

  
   
# pg_buffercache_numa Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       bufferid integer
      

      

       ID, in the range 1..shared_buffers
      

     

     
      

       os_page_num bigint
      

      

       Number of OS memory page for this buffer
      

     

     
      

       numa_node int
      

      

       ID of NUMA node
      

     

    
   
  

  

   As NUMA node ID inquiry for each page requires memory pages
   to be paged-in, the first execution of this function can take a noticeable
   amount of time. In all the cases (first execution or not), retrieving this
   information is costly and querying the view at a high frequency is not recommended.
  

  
    

      When determining the NUMA node, the view touches
      all memory pages for the shared memory segment. This will force
      allocation of the shared memory, if it wasn't allocated already,
      and the memory may get allocated in a single NUMA
      node (depending on system configuration).
    

  

 

 

  
# The pg_buffercache_summary() Function

  

   The definitions of the columns exposed by the function are shown in .
  

  
   
# pg_buffercache_summary() Output Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       buffers_used int4
      

      

       Number of used shared buffers
      

     

     
      

       buffers_unused int4
      

      

       Number of unused shared buffers
      

     

     
      

       buffers_dirty int4
      

      

       Number of dirty shared buffers
      

     

     
      

       buffers_pinned int4
      

      

       Number of pinned shared buffers
      

     

     
      

       usagecount_avg float8
      

      

       Average usage count of used shared buffers
      

     
    
   
  

  

   The pg_buffercache_summary() function returns a
   single row summarizing the state of all shared buffers. Similar and more
   detailed information is provided by the
   pg_buffercache view, but
   pg_buffercache_summary() is significantly cheaper.
  

  

   Like the pg_buffercache view,
   pg_buffercache_summary() does not acquire buffer
   manager locks. Therefore concurrent activity can lead to minor inaccuracies
   in the result.
  

 

 

  
# The pg_buffercache_usage_counts() Function

  

   The definitions of the columns exposed by the function are shown in
   .
  

  
   
# pg_buffercache_usage_counts() Output Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       usage_count int4
      

      

       A possible buffer usage count
      

     

     
      

       buffers int4
      

      

       Number of buffers with the usage count
      

     

     
      

       dirty int4
      

      

       Number of dirty buffers with the usage count
      

     

     
      

       pinned int4
      

      

       Number of pinned buffers with the usage count
      

     
    
   
  

  

   The pg_buffercache_usage_counts() function returns a
   set of rows summarizing the states of all shared buffers, aggregated over
   the possible usage count values.  Similar and more detailed information is
   provided by the pg_buffercache view, but
   pg_buffercache_usage_counts() is significantly cheaper.
  

  

   Like the pg_buffercache view,
   pg_buffercache_usage_counts() does not acquire buffer
   manager locks. Therefore concurrent activity can lead to minor inaccuracies
   in the result.
  

 

 

  
# The pg_buffercache_evict() Function

  

   The pg_buffercache_evict() function takes a buffer
   identifier, as shown in the bufferid column of
   the pg_buffercache view.  It returns information
   about whether the buffer was evicted and flushed.
   The buffer_evicted
   column is true on success, and false if the buffer wasn't valid, if it
   couldn't be evicted because it was pinned, or if it became dirty again
   after an attempt to write it out.
   The buffer_flushed column is true if the
   buffer was flushed.  This does not necessarily mean that buffer was flushed
   by us, it might be flushed by someone else.  The result is immediately out
   of date upon return, as the buffer might become valid again at any time due
   to concurrent activity. The function is intended for developer testing
   only.
  

 

 

  
# The pg_buffercache_evict_relation() Function

  

   The pg_buffercache_evict_relation() function is very
   similar to the pg_buffercache_evict() function.  The
   difference is that the pg_buffercache_evict_relation()
   takes a relation identifier instead of buffer identifier.  It tries to
   evict all buffers for all forks in that relation.

   It returns the number of evicted buffers, flushed buffers and the number of
   buffers that could not be evicted.  Flushed buffers haven't necessarily
   been flushed by us, they might have been flushed by someone else.  The
   result is immediately out of date upon return, as buffers might immediately
   be read back in due to concurrent activity.  The function is intended for
   developer testing only.
  

 

 

  
# The pg_buffercache_evict_all() Function

  

   The pg_buffercache_evict_all() function is very
   similar to the pg_buffercache_evict() function.  The
   difference is, the pg_buffercache_evict_all() function
   does not take an argument; instead it tries to evict all buffers in the
   buffer pool.  It returns the number of evicted buffers, flushed buffers and
   the number of buffers that could not be evicted.  Flushed buffers haven't
   necessarily been flushed by us, they might have been flushed by someone
   else.  The result is immediately out of date upon return, as buffers might
   immediately be read back in due to concurrent activity.  The function is
   intended for developer testing only.
  

 

 

  
# The pg_buffercache_mark_dirty() Function

  

   The pg_buffercache_mark_dirty() function takes a
   buffer identifier, as shown in the bufferid
   column of the pg_buffercache view.  It returns
   information about whether the buffer was marked as dirty.
   The buffer_dirtied column is true on success,
   and false if the buffer was already dirty if the buffer was not valid or
   if it could not be marked as dirty because it was pinned.
   The buffer_already_dirty column is true if
   the buffer couldn't be marked as dirty because it was already dirty. The
   result is immediately out of date upon return, as the buffer might become
   valid again at any time due to concurrent activity. The function is
   intended for developer testing only.
  

 

 

  
# The pg_buffercache_mark_dirty_relation() Function

  

   The pg_buffercache_mark_dirty_relation() function is
   very similar to the
   pg_buffercache_mark_dirty() function.
   The difference is that the
   pg_buffercache_mark_dirty_relation() function takes a
   relation identifier instead of buffer identifier. It tries to mark all
   buffers dirty for all forks in that relation.
   It returns the number of buffers marked as dirty, the number of buffers
   already dirty and the number of buffers skipped because already pinned or
   invalid.
   The result is immediately out of date upon return, as the buffer might
   become valid again at any time due to concurrent activity. The function is
   intended for developer testing only.
  

 

 

  
# The pg_buffercache_mark_dirty_all() Function

  

   The pg_buffercache_mark_dirty_all() function is
   very similar to the pg_buffercache_mark_dirty()
   function.
   The difference is that the
   pg_buffercache_mark_dirty_all() tries to mark all
   buffers dirty in the buffer pool.
   It returns the number of buffers marked as dirty, the number of buffers
   already dirty and the number of buffers skipped because already pinned or
   invalid.
   The result is immediately out of date upon return, as the buffer might
   become valid again at any time due to concurrent activity. The function is
   intended for developer testing only.
  

 

  
# Sample Output

```
regression=# SELECT n.nspname, c.relname, count(*) AS buffers
             FROM pg_buffercache b JOIN pg_class c
             ON b.relfilenode = pg_relation_filenode(c.oid) AND
                b.reldatabase IN (0, (SELECT oid FROM pg_database
                                      WHERE datname = current_database()))
             JOIN pg_namespace n ON n.oid = c.relnamespace
             GROUP BY n.nspname, c.relname
             ORDER BY 3 DESC
             LIMIT 10;

  nspname   |        relname         | buffers
------------+------------------------+---------
 public     | delete_test_table      |     593
 public     | delete_test_table_pkey |     494
 pg_catalog | pg_attribute           |     472
 public     | quad_poly_tbl          |     353
 public     | tenk2                  |     349
 public     | tenk1                  |     349
 public     | gin_test_idx           |     306
 pg_catalog | pg_largeobject         |     206
 public     | gin_test_tbl           |     188
 public     | spgist_text_tbl        |     182
(10 rows)

regression=# SELECT pages_per_buffer, COUNT(*) as buffer_count
             FROM (
                   SELECT bufferid, COUNT(*) as pages_per_buffer
                   FROM pg_buffercache_os_pages
                   GROUP BY bufferid
                  )
             GROUP BY pages_per_buffer
             ORDER BY pages_per_buffer;

 pages_per_buffer | buffer_count
------------------+--------------
                1 |       261120
                2 |         1024
(2 rows)

regression=# SELECT n.nspname, c.relname, count(*) AS buffers_on_multiple_pages
             FROM pg_buffercache b JOIN pg_class c
             ON b.relfilenode = pg_relation_filenode(c.oid) AND
                b.reldatabase IN (0, (SELECT oid FROM pg_database
                                      WHERE datname = current_database()))
             JOIN pg_namespace n ON n.oid = c.relnamespace
             JOIN (SELECT bufferid FROM pg_buffercache_os_pages
                   GROUP BY bufferid HAVING count(*) > 1) m on m.bufferid = b.bufferid
             GROUP BY n.nspname, c.relname
             ORDER BY 3 DESC
             LIMIT 10;

  nspname   |           relname            | buffers_on_multiple_pages
------------+------------------------------+---------------------------
 public     | delete_test_table            |                         3
 public     | gin_test_idx                 |                         2
 pg_catalog | pg_depend                    |                         2
 public     | quad_poly_tbl                |                         2
 pg_catalog | pg_depend_reference_index    |                         1
 pg_catalog | pg_index_indexrelid_index    |                         1
 pg_catalog | pg_constraint_contypid_index |                         1
 pg_catalog | pg_statistic                 |                         1
 pg_catalog | pg_depend_depender_index     |                         1
 pg_catalog | pg_operator                  |                         1
(10 rows)

regression=# SELECT * FROM pg_buffercache_summary();
 buffers_used | buffers_unused | buffers_dirty | buffers_pinned | usagecount_avg
--------------+----------------+---------------+----------------+----------------
          248 |        2096904 |            39 |              0 |       3.141129
(1 row)

regression=# SELECT * FROM pg_buffercache_usage_counts();
 usage_count | buffers | dirty | pinned
-------------+---------+-------+--------
           0 |   14650 |     0 |      0
           1 |    1436 |   671 |      0
           2 |     102 |    88 |      0
           3 |      23 |    21 |      0
           4 |       9 |     7 |      0
           5 |     164 |   106 |      0
(6 rows)
```

 

 

  
# Authors

  

   Mark Kirkwood markir@paradise.net.nz
  

  

   Design suggestions: Neil Conway neilc@samurai.com
  

  

   Debugging advice: Tom Lane tgl@sss.pgh.pa.us
