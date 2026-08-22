# Monitoring Database Activity

 
  monitoring
  database activity
 

 
  database activity
  monitoring
 

 

  A database administrator frequently wonders, What is the system
  doing right now?
  This chapter discusses how to find that out.
 

  

   Several tools are available for monitoring database activity and
   analyzing performance.  Most of this chapter is devoted to describing
   PostgreSQL's cumulative statistics system,
   but one should not neglect regular Unix monitoring programs such as
   ps, top, iostat, and vmstat.
   Also, once one has identified a
   poorly-performing query, further investigation might be needed using
   PostgreSQL's EXPLAIN command.
    discusses EXPLAIN
   and other methods for understanding the behavior of an individual
   query.
  

 

  
# Standard Unix Tools

  
   ps
   to monitor activity
  

  

   On most Unix platforms, PostgreSQL modifies its
   command title as reported by ps, so that individual server
   processes can readily be identified.  A sample display is

```
$ ps auxww | grep ^postgres
postgres  15551  0.0  0.1  57536  7132 pts/0    S    18:02   0:00 postgres -i
postgres  15554  0.0  0.0  57536  1184 ?        Ss   18:02   0:00 postgres: background writer
postgres  15555  0.0  0.0  57536   916 ?        Ss   18:02   0:00 postgres: checkpointer
postgres  15556  0.0  0.0  57536   916 ?        Ss   18:02   0:00 postgres: walwriter
postgres  15557  0.0  0.0  58504  2244 ?        Ss   18:02   0:00 postgres: autovacuum launcher
postgres  15582  0.0  0.0  58772  3080 ?        Ss   18:04   0:00 postgres: joe runbug 127.0.0.1 idle
postgres  15606  0.0  0.0  58772  3052 ?        Ss   18:07   0:00 postgres: tgl regression [local] SELECT waiting
postgres  15610  0.0  0.0  58772  3056 ?        Ss   18:07   0:00 postgres: tgl regression [local] idle in transaction
```

   (The appropriate invocation of ps varies across different
   platforms, as do the details of what is shown.  This example is from a
   recent Linux system.)  The first process listed here is the
   primary server process.  The command arguments
   shown for it are the same ones used when it was launched.  The next four
   processes are background worker processes automatically launched by the
   primary process.  (The autovacuum launcher process will not
   be present if you have set the system not to run autovacuum.)
   Each of the remaining
   processes is a server process handling one client connection.  Each such
   process sets its command line display in the form

```
postgres: user database host activity
```

  The user, database, and (client) host items remain the same for
  the life of the client connection, but the activity indicator changes.
  The activity can be idle (i.e., waiting for a client command),
  idle in transaction (waiting for client inside a BEGIN block),
  or a command type name such as SELECT.  Also,
  waiting is appended if the server process is presently waiting
  on a lock held by another session.  In the above example we can infer
  that process 15606 is waiting for process 15610 to complete its transaction
  and thereby release some lock.  (Process 15610 must be the blocker, because
  there is no other active session.  In more complicated cases it would be
  necessary to look into the
  pg_locks
  system view to determine who is blocking whom.)
  

  

   If  has been configured the
   cluster name will also be shown in ps output:

```
$ psql -c 'SHOW cluster_name'
 cluster_name
--------------
 server1
(1 row)

$ ps aux|grep server1
postgres   27093  0.0  0.0  30096  2752 ?        Ss   11:34   0:00 postgres: server1: background writer
...
```

  

  

   If you have turned off  then the
   activity indicator is not updated; the process title is set only once
   when a new process is launched.  On some platforms this saves a measurable
   amount of per-command overhead;  on others it's insignificant.
  

  
  

  Solaris requires special handling. You must
  use /usr/ucb/ps, rather than
  /bin/ps. You also must use two w
  flags, not just one. In addition, your original invocation of the
  postgres command must have a shorter
  ps status display than that provided by each
  server process.  If you fail to do all three things, the ps
  output for each server process will be the original postgres
  command line.
  

  
 

 

  
# The Cumulative Statistics System

  
   statistics
  

  

   PostgreSQL's cumulative statistics
   system supports collection and reporting of information about
   server activity.  Presently, accesses to tables and indexes in both
   disk-block and individual-row terms are counted.  The total number of rows
   in each table, and information about vacuum and analyze actions for each
   table are also counted.  If enabled, calls to user-defined functions and
   the total time spent in each one are counted as well.
  

  

   PostgreSQL also supports reporting dynamic
   information about exactly what is going on in the system right now, such as
   the exact command currently being executed by other server processes, and
   which other connections exist in the system.  This facility is independent
   of the cumulative statistics system.
  

 

  
# Statistics Collection Configuration

  

   Since collection of statistics adds some overhead to query execution,
   the system can be configured to collect or not collect information.
   This is controlled by configuration parameters that are normally set in
   postgresql.conf.  (See  for
   details about setting configuration parameters.)
  

  

   The parameter  enables monitoring
   of the current command being executed by any server process.
  

  

   The parameter  enables
   monitoring of cost-based vacuum delay.
  

  

   The parameter  controls whether
   cumulative statistics are collected about table and index accesses.
  

  

   The parameter  enables tracking of
   usage of user-defined functions.
  

  

   The parameter  enables monitoring
   of block read, write, extend, and fsync times.
  

  

   The parameter  enables monitoring
   of WAL read, write and fsync times.
  

  

   Normally these parameters are set in postgresql.conf so
   that they apply to all server processes, but it is possible to turn
   them on or off in individual sessions using the  command. (To prevent
   ordinary users from hiding their activity from the administrator,
   only superusers are allowed to change these parameters with
   SET.)
  

  

   Cumulative statistics are collected in shared memory. Every
   PostgreSQL process collects statistics locally,
   then updates the shared data at appropriate intervals.  When a server,
   including a physical replica, shuts down cleanly, a permanent copy of the
   statistics data is stored in the pg_stat subdirectory,
   so that statistics can be retained across server restarts.  In contrast,
   when starting from an unclean shutdown (e.g., after an immediate shutdown,
   a server crash, starting from a base backup, and point-in-time recovery),
   all statistics counters are reset.
  

 

 

  
# Viewing Statistics

  

   Several predefined views, listed in , are available to show
   the current state of the system. There are also several other
   views, listed in , available to show the accumulated
   statistics.  Alternatively, one can
   build custom views using the underlying cumulative statistics functions, as
   discussed in .
  

  

   When using the cumulative statistics views and functions to monitor
   collected data, it is important to realize that the information does not
   update instantaneously.  Each individual server process flushes out
   accumulated statistics to shared memory just before going idle, but not
   more frequently than once per PGSTAT_MIN_INTERVAL
   milliseconds (1 second unless altered while building the server); so a
   query or transaction still in progress does not affect the displayed totals
   and the displayed information lags behind actual activity.  However,
   current-query information collected by track_activities
   is always up-to-date.
  

  

   Another important point is that when a server process is asked to display
   any of the accumulated statistics, accessed values are cached until the end
   of its current transaction in the default configuration. So the statistics
   will show static information as long as you continue the current
   transaction. Similarly, information about the current queries of all
   sessions is collected when any such information is first requested within a
   transaction, and the same information will be displayed throughout the
   transaction. This is a feature, not a bug, because it allows you to perform
   several queries on the statistics and correlate the results without
   worrying that the numbers are changing underneath you.

   When analyzing statistics interactively, or with expensive queries, the
   time delta between accesses to individual statistics can lead to
   significant skew in the cached statistics. To minimize skew,
   stats_fetch_consistency can be set to
   snapshot, at the price of increased memory usage for
   caching not-needed statistics data.  Conversely, if it's known that
   statistics are only accessed once, caching accessed statistics is
   unnecessary and can be avoided by setting
   stats_fetch_consistency to none.

   You can invoke pg_stat_clear_snapshot() to discard the
   current transaction's statistics snapshot or cached values (if any).  The
   next use of statistical information will (when in snapshot mode) cause a
   new snapshot to be built or (when in cache mode) accessed statistics to be
   cached.
  

  

   A transaction can also see its own statistics (not yet flushed out to the
   shared memory statistics) in the views
   pg_stat_xact_all_tables,
   pg_stat_xact_sys_tables,
   pg_stat_xact_user_tables, and
   pg_stat_xact_user_functions.  These numbers do not act as
   stated above; instead they update continuously throughout the transaction.
  

  

   Some of the information in the dynamic statistics views shown in  is security restricted.
   Ordinary users can only see all the information about their own sessions
   (sessions belonging to a role that they are a member of).  In rows about
   other sessions, many columns will be null.  Note, however, that the
   existence of a session and its general properties such as its sessions user
   and database are visible to all users.  Superusers and roles with privileges of
   built-in role pg_read_all_stats
   can see all the information about all sessions.
  

  
   
# Dynamic Statistics Views

   
    
     
      View Name
      Description
     
    

    
     
      
       pg_stat_activity
       pg_stat_activity
      
      
       One row per server process, showing information related to
       the current activity of that process, such as state and current query.
       See 
       pg_stat_activity for details.
      
     

     
      pg_stat_replicationpg_stat_replication
      One row per WAL sender process, showing statistics about
       replication to that sender's connected standby server.
       See 
       pg_stat_replication for details.
      
     

     
      pg_stat_wal_receiverpg_stat_wal_receiver
      Only one row, showing statistics about the WAL receiver from
       that receiver's connected server.
       See 
       pg_stat_wal_receiver for details.
      
     

     
      pg_stat_recoverypg_stat_recovery
      At most one row, showing statistics about the recovery state.
       See 
       pg_stat_recovery for details.
      
     

     
      pg_stat_recovery_prefetchpg_stat_recovery_prefetch
      Only one row, showing statistics about blocks prefetched during recovery.
       See 
       pg_stat_recovery_prefetch for details.
      
     

     
      pg_stat_subscriptionpg_stat_subscription
      At least one row per subscription, showing information about
       the subscription workers.
       See 
       pg_stat_subscription for details.
      
     

     
      pg_stat_sslpg_stat_ssl
      One row per connection (regular and replication), showing information about
       SSL used on this connection.
       See 
       pg_stat_ssl for details.
      
     

     
      pg_stat_gssapipg_stat_gssapi
      One row per connection (regular and replication), showing information about
       GSSAPI authentication and encryption used on this connection.
       See 
       pg_stat_gssapi for details.
      
     

     
      pg_stat_progress_analyzepg_stat_progress_analyze
      One row for each backend (including autovacuum worker processes) running
       ANALYZE, showing current progress.
       See .
      
     

     
      pg_stat_progress_create_indexpg_stat_progress_create_index
      One row for each backend running CREATE INDEX or REINDEX, showing
      current progress.
      See .
     
     

     
      pg_stat_progress_vacuumpg_stat_progress_vacuum
      One row for each backend (including autovacuum worker processes) running
       VACUUM, showing current progress.
       See .
      
     

     
      pg_stat_progress_clusterpg_stat_progress_cluster
      One row for each backend running REPACK,
       CLUSTER or VACUUM FULL, showing current progress.
       See .
      
     

     
      pg_stat_progress_repackpg_stat_progress_repack
      One row for each backend running REPACK,
       CLUSTER or VACUUM FULL, showing current progress.
       .
      
     

     
      pg_stat_progress_basebackuppg_stat_progress_basebackup
      One row for each WAL sender process streaming a base backup,
       showing current progress.
       See .
      
     

     
      pg_stat_progress_copypg_stat_progress_copy
      One row for each backend running COPY, showing current progress.
       See .
      
     

     
      pg_stat_progress_data_checksumspg_stat_progress_data_checksums
      One row for the data checksum launcher process while data
       checksums are being enabled or disabled.  When enabling data
       checksums, the view also has one row for each worker process,
       showing current progress.
       See .
      
     
    
   
  

  
   
# Collected Statistics Views

   
    
     
      View Name
      Description
     
    

    

     

     
      pg_stat_archiverpg_stat_archiver
      One row only, showing statistics about the
       WAL archiver process's activity. See
       
       pg_stat_archiver for details.
      
     

     
      pg_stat_bgwriterpg_stat_bgwriter
      One row only, showing statistics about the
       background writer process's activity. See
       
       pg_stat_bgwriter for details.
     
     

     
      pg_stat_checkpointerpg_stat_checkpointer
      One row only, showing statistics about the
       checkpointer process's activity. See
       
       pg_stat_checkpointer for details.
     
     

     
      pg_stat_databasepg_stat_database
      One row per database, showing database-wide statistics. See
       
       pg_stat_database for details.
      
     

     
      pg_stat_database_conflictspg_stat_database_conflicts
      
       One row per database, showing database-wide statistics about
       query cancels due to conflict with recovery on standby servers.
       See 
       pg_stat_database_conflicts for details.
      
     

     
      pg_stat_iopg_stat_io
      
       One row for each combination of backend type, context, and target object
       containing cluster-wide I/O statistics.
       See 
       pg_stat_io for details.
     
     

     
      pg_stat_kind_infopg_stat_kind_info
      
       One row for each registered statistics kind, showing information
       about each kind.
       See 
       pg_stat_kind_info for details.
      
     

     
      pg_stat_lockpg_stat_lock
      
       One row for each lock type, containing cluster-wide locks statistics.
       See 
       pg_stat_lock for details.
     
     

     
      pg_stat_replication_slotspg_stat_replication_slots
      One row per replication slot, showing statistics about the
       replication slot's usage. See
       
       pg_stat_replication_slots for details.
      
     

     
      pg_stat_slrupg_stat_slru
      One row per SLRU, showing statistics of operations. See
       
       pg_stat_slru for details.
      
     

     
      pg_stat_subscription_statspg_stat_subscription_stats
      One row per subscription, showing statistics about errors and conflicts.
      See 
      pg_stat_subscription_stats for details.
      
     

     
      pg_stat_walpg_stat_wal
      One row only, showing statistics about WAL activity. See
       
       pg_stat_wal for details.
      
     

     

     
      pg_stat_all_tablespg_stat_all_tables
      
       One row for each table in the current database, showing statistics
       about accesses to that specific table.
       See 
       pg_stat_all_tables for details.
      
     

     
      pg_stat_sys_tablespg_stat_sys_tables
      Same as pg_stat_all_tables, except that only
      system tables are shown.
     

     
      pg_stat_user_tablespg_stat_user_tables
      Same as pg_stat_all_tables, except that only user
      tables are shown.
     

     
      pg_stat_xact_all_tablespg_stat_xact_all_tables
      Similar to pg_stat_all_tables, but counts actions
      taken so far within the current transaction (which are not
      yet included in pg_stat_all_tables and related views).
      The columns for numbers of live and dead rows and vacuum and
      analyze actions are not present in this view.
     

     
      pg_stat_xact_sys_tablespg_stat_xact_sys_tables
      Same as pg_stat_xact_all_tables, except that only
      system tables are shown.
     

     
      pg_stat_xact_user_tablespg_stat_xact_user_tables
      Same as pg_stat_xact_all_tables, except that only
      user tables are shown.
     

     
      pg_stat_autovacuum_scorespg_stat_autovacuum_scores
      
       One row for each table in the current database, showing the current
       autovacuum scores for that specific table.  See
       
       pg_stat_autovacuum_scores for details.
      
     

     
      pg_stat_all_indexespg_stat_all_indexes
      
       One row for each index in the current database, showing statistics
       about accesses to that specific index.
       See 
       pg_stat_all_indexes for details.
      
     

     
      pg_stat_sys_indexespg_stat_sys_indexes
      Same as pg_stat_all_indexes, except that only
      indexes on system tables are shown.
     

     
      pg_stat_user_indexespg_stat_user_indexes
      Same as pg_stat_all_indexes, except that only
      indexes on user tables are shown.
     

     
      pg_stat_user_functionspg_stat_user_functions
      
       One row for each tracked function, showing statistics
       about executions of that function. See
       
       pg_stat_user_functions for details.
      
     

     
      pg_stat_xact_user_functionspg_stat_xact_user_functions
      Similar to pg_stat_user_functions, but counts only
      calls during the current transaction (which are not
      yet included in pg_stat_user_functions).
     

     

     
      pg_statio_all_tablespg_statio_all_tables
      
       One row for each table in the current database, showing statistics
       about I/O on that specific table.
       See 
       pg_statio_all_tables for details.
      
     

     
      pg_statio_sys_tablespg_statio_sys_tables
      Same as pg_statio_all_tables, except that only
      system tables are shown.
     

     
      pg_statio_user_tablespg_statio_user_tables
      Same as pg_statio_all_tables, except that only
      user tables are shown.
     

     
      pg_statio_all_indexespg_statio_all_indexes
      
       One row for each index in the current database,
       showing statistics about I/O on that specific index.
       See 
       pg_statio_all_indexes for details.
      
     

     
      pg_statio_sys_indexespg_statio_sys_indexes
      Same as pg_statio_all_indexes, except that only
      indexes on system tables are shown.
     

     
      pg_statio_user_indexespg_statio_user_indexes
      Same as pg_statio_all_indexes, except that only
      indexes on user tables are shown.
     

     
      pg_statio_all_sequencespg_statio_all_sequences
     
       One row for each sequence in the current database,
       showing statistics about I/O on that specific sequence.
       See 
       pg_statio_all_sequences for details.
     
     

     
      pg_statio_sys_sequencespg_statio_sys_sequences
      Same as pg_statio_all_sequences, except that only
      system sequences are shown.  (Presently, no system sequences are defined,
      so this view is always empty.)
     

     
      pg_statio_user_sequencespg_statio_user_sequences
      Same as pg_statio_all_sequences, except that only
      user sequences are shown.
     

    
   
  

  

   The per-index statistics are particularly useful to determine which
   indexes are being used and how effective they are.
  

  

   The pg_stat_io and
   pg_statio_ set of views are useful for determining
   the effectiveness of the buffer cache. They can be used to calculate a cache
   hit ratio. Note that while PostgreSQL's I/O
   statistics capture most instances in which the kernel was invoked in order
   to perform I/O, they do not differentiate between data which had to be
   fetched from disk and that which already resided in the kernel page cache.
   Users are advised to use the PostgreSQL
   statistics views in combination with operating system utilities for a more
   complete picture of their database's I/O performance.
  

 

 

  
# pg_stat_activity

  
   pg_stat_activity
  

  

   The pg_stat_activity view will have one row
   per server process, showing information related to
   the current activity of that process.
  

  
   
# pg_stat_activity View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       datid oid
      

      

       OID of the database this backend is connected to
      

     

     
      

       datname name
      

      

       Name of the database this backend is connected to
      

     

     
      

       pid integer
      

      

       Process ID of this backend
      

     

     
      

       leader_pid integer
      

      

       Process ID of the parallel group leader if this process is a parallel
       query worker, or process ID of the leader apply worker if this process
       is a parallel apply worker.  NULL indicates that this
       process is a parallel group leader or leader apply worker, or does not
       participate in any parallel operation.
      

     

     
      

       usesysid oid
      

      

       OID of the user logged into this backend
      

     

     
      

       usename name
      

      

       Name of the user logged into this backend
      

     

     
      

       application_name text
      

      

       Name of the application that is connected
       to this backend
      

     

     
      

       client_addr inet
      

      

       IP address of the client connected to this backend.
       If this field is null, it indicates either that the client is
       connected via a Unix socket on the server machine or that this is an
       internal process such as autovacuum.
      

     

     
      

       client_hostname text
      

      

       Host name of the connected client, as reported by a
       reverse DNS lookup of client_addr. This field will
       only be non-null for IP connections, and only when  is enabled.
      

     

     
      

       client_port integer
      

      

       TCP port number that the client is using for communication
       with this backend, or -1 if a Unix socket is used.
       If this field is null, it indicates that this is an internal server process.
      

     

     
      

       backend_start timestamp with time zone
      

      

       Time when this process was started.  For client backends,
       this is the time the client connected to the server.
      

     

     
      

       xact_start timestamp with time zone
      

      

       Time when this process' current transaction was started, or null
       if no transaction is active. If the current
       query is the first of its transaction, this column is equal to the
       query_start column.
      

     

     
      

       query_start timestamp with time zone
      

      

       Time when the currently active query was started, or if
       state is not active, when the last query
       was started
      

     

     
      

       state_change timestamp with time zone
      

      

       Time when the state was last changed
      

     

     
      

       wait_event_type text
      

      

       The type of event for which the backend is waiting, if any;
       otherwise NULL.  See .
      

     

     
      

       wait_event text
      

      

       Wait event name if backend is currently waiting, otherwise NULL.
       See  through
       .
      

     

     
      

       state text
      

      

       Current overall state of this backend.
       Possible values are:
       

        

         

          starting: The backend is in initial startup. Client
          authentication is performed during this phase.
         

        

        

        

          active: The backend is executing a query.
         

        

        

         

          idle: The backend is waiting for a new client command.
         

        

        

         

          idle in transaction: The backend is in a transaction,
          but is not currently executing a query.
         

        

        

         

          idle in transaction (aborted): This state is similar to
          idle in transaction, except one of the statements in
          the transaction caused an error.
         

        

        

         

          fastpath function call: The backend is executing a
          fast-path function.
         

        

        

         

          disabled: This state is reported if  is disabled in this backend.
         

       

       

      

     

     
      

       backend_xid xid
      

      

       Top-level transaction identifier of this backend, if any;  see
       .
      

     

     
      

       backend_xmin xid
      

      

       The current backend's xmin horizon.
      

     

    
     

      query_id bigint
     

     

      Identifier of this backend's most recent query. If
      state is active this
      field shows the identifier of the currently executing query. In
      all other states, it shows the identifier of last query that was
      executed.  Query identifiers are not computed by default so this
      field will be null unless 
      parameter is enabled or a third-party module that computes query
      identifiers is configured.
     

    

     
      

       query text
      

      

       Text of this backend's most recent query. If
       state is active this field shows the
       currently executing query. In all other states, it shows the last query
       that was executed. By default the query text is truncated at 1024
       bytes; this value can be changed via the parameter
       .
      

     

     
      

       backend_type text
      

      

       Type of current backend. Possible types are:
       

        

         

          archiver: The WAL archiver, active when
           is enabled.
         

        

        

         

          autovacuum launcher: The background process that
          launches autovacuum workers, active when
           is on.
         

        

        

         

          autovacuum worker: A background process running
          VACUUM or ANALYZE on a single
          table.
         

        

        

         

          background writer: The background process that
          makes sure that there are enough clean buffers in shared buffers.
         

        

        

         

          checkpointer: The background process that
          performs checkpoints
          regularly.
         

        

        

         

          client backend: The server process performing
          work for a regular database connection.
         

        

        

         

          datachecksums launcher: The background process
          that launches data checksum workers.
         

        

        

         

          datachecksums worker: A background process that
          calculates data checksums for all pages in one database.
         

        

        

         

          io worker: A background process performing
          asynchronous I/O, active when  is set
          to worker.
         

        

        

         

          logical replication apply worker: A background
          process that applies data modifications on a logical subscriber.
         

        

        

         

          logical replication launcher: The background
          process that launches logical replication worker processes for
          subscriptions.
         

        

        

         

          logical replication parallel worker: A background
          process that applies data modifications on a logical subscriber
          for a subscription with streaming = parallel.
         

        

        

         

          logical replication sequencesync worker: A
          background process that replicates sequence data on a logical
          subscriber.
         

        

        

         

          logical replication tablesync worker: A
          background process that copies table data on a logical subscriber
          for a subscription with copy_data = true.
         

        

        

         

          parallel worker: A background process that helps
          a backend process to perform operations in parallel.
         

        

        

         

          REPACK decoding worker: A background process that
          decodes WAL for REPACK (CONCURRENTLY).
         

        

        

         

          slotsync worker: The background process that
          synchronizes logical replication slots on a streaming replication
          standby server, active when 
          is set to on.
         

        

        

         

          standalone backend: The backend process when
          PostgreSQL was started in
          .
         

        

        

         

          startup: The background process that replays WAL
          during crash recovery, archive recovery or streaming replication.
         

        

        

         

          walreceiver: The background process that receives
          WAL records from a WAL sender, active in streaming replication
          standby mode.
         

        

        

         

          walsender: A background process that sends WAL
          records to receivers (during streaming replication) or decodes WAL
          and sends the decoded information (during logical replication).
         

        

        

         

          walsummarizer: The background process that
          creates summaries from WAL for use with incremental backup, active
          when  is on.
         

        

        

         

          walwriter: The background process that persists
          WAL records from WAL buffers to disk.
         

        

       

       In addition, background workers registered by extensions may have
       additional types.
      

     
    
   
  

  
   

    The wait_event and state columns are
    independent.  If a backend is in the active state,
    it may or may not be waiting on some event.  If the state
    is active and wait_event is non-null, it
    means that a query is being executed, but is being blocked somewhere
    in the system.  To keep the reporting overhead low, the system does not
    attempt to synchronize different aspects of activity data for a backend.
    As a result, ephemeral discrepancies may exist between the view's columns.
   

  

  
   
# Wait Event Types

   
    
     
      Wait Event Type
      Description
     
    

    
     
      Activity
      The server process is idle.  This event type indicates a process
       waiting for activity in its main processing loop.
       wait_event will identify the specific wait point;
       see .
      
     
     
      Buffer
      The server process is waiting for access to a data buffer.
      See .
      
     
     
      Client
      The server process is waiting for activity on a socket
       connected to a user application.  Thus, the server expects something
       to happen that is independent of its internal processes.
       wait_event will identify the specific wait point;
       see .
      
     
     
      Extension
      The server process is waiting for some condition defined by an
       extension module.
       See .
      
     
     
      InjectionPoint
      The server process is waiting for an injection point to reach an
       outcome defined in a test.  See
        for more details.  This
       type has no predefined wait points.
      
     
     
      IO
      The server process is waiting for an I/O operation to complete.
       wait_event will identify the specific wait point;
       see .
      
     
     
      IPC
      The server process is waiting for some interaction with
       another server process.  wait_event will
       identify the specific wait point;
       see .
      
     
     
      Lock
      The server process is waiting for a heavyweight lock.
       Heavyweight locks, also known as lock manager locks or simply locks,
       primarily protect SQL-visible objects such as tables.  However,
       they are also used to ensure mutual exclusion for certain internal
       operations such as relation extension.  wait_event
       will identify the type of lock awaited;
       see .
      
     
     
      LWLock
       The server process is waiting for a lightweight lock.
       Most such locks protect a particular data structure in shared memory.
       wait_event will contain a name identifying the purpose
       of the lightweight lock.  (Some locks have specific names; others
       are part of a group of locks each with a similar purpose.)
       See .
      
     
     
      Timeout
      The server process is waiting for a timeout
       to expire.  wait_event will identify the specific wait
       point; see .
      
     
    
   
  

  &wait_event_types;

   

     Here are examples of how wait events can be viewed:

```
SELECT pid, wait_event_type, wait_event FROM pg_stat_activity WHERE wait_event IS NOT NULL;
 pid  | wait_event_type | wait_event
------+-----------------+------------
 2540 | Lock            | relation
 6644 | LWLock          | ProcArray
(2 rows)
```

```
SELECT a.pid, a.wait_event, w.description
  FROM pg_stat_activity a JOIN
       pg_wait_events w ON (a.wait_event_type = w.type AND
                            a.wait_event = w.name)
  WHERE a.wait_event IS NOT NULL AND a.state = 'active';
-[ RECORD 1 ]------------------------------------------------------&zwsp;------------
pid         | 686674
wait_event  | WALInitSync
description | Waiting for a newly initialized WAL file to reach durable storage
```

   

   
    

     Extensions can add Extension,
     InjectionPoint, and LWLock events
     to the lists shown in  and
     . In some cases, the name
     of an LWLock assigned by an extension will not be
     available in all server processes.  It might be reported as just
     extension rather than the
     extension-assigned name.
    

   
 

 

  
# pg_stat_replication

  
   pg_stat_replication
  

   

   The pg_stat_replication view will contain one row
   per WAL sender process, showing statistics about replication to that
   sender's connected standby server.  Only directly connected standbys are
   listed; no information is available about downstream standby servers.
  

  
   
# pg_stat_replication View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of a WAL sender process
      

     

     
      

       usesysid oid
      

      

       OID of the user logged into this WAL sender process
      

     

     
      

       usename name
      

      

       Name of the user logged into this WAL sender process
      

     

     
      

       application_name text
      

      

       Name of the application that is connected
       to this WAL sender
      

     

     
      

       client_addr inet
      

      

       IP address of the client connected to this WAL sender.
       If this field is null, it indicates that the client is
       connected via a Unix socket on the server machine.
      

     

     
      

       client_hostname text
      

      

       Host name of the connected client, as reported by a
       reverse DNS lookup of client_addr. This field will
       only be non-null for IP connections, and only when  is enabled.
      

     

     
      

       client_port integer
      

      

       TCP port number that the client is using for communication
       with this WAL sender, or -1 if a Unix socket is used
      

     

     
      

       backend_start timestamp with time zone
      

      

       Time when this process was started, i.e., when the
       client connected to this WAL sender
      

     

     
      

       backend_xmin xid
      

      

       This standby's xmin horizon reported
       by .
       This field will be null if a replication slot is used (in this case,
       the standby's xmin is shown in
       pg_replication_slots)
       or if hot_standby_feedback is disabled.
      

     

     
      

       state text
      

      

       Current WAL sender state.
       Possible values are:
       

        

         

          startup: This WAL sender is starting up.
         

        

        

         

          catchup: This WAL sender's connected standby is
          catching up with the primary.
         

        

        

         

          streaming: This WAL sender is streaming changes
          after its connected standby server has caught up with the primary.
         

        

        

         

          backup: This WAL sender is sending a backup.
         

        

        

         

          stopping: This WAL sender is stopping.
         

        

       

      

     

     
      

       sent_lsn pg_lsn
      

      

       Last write-ahead log location sent on this connection
      

     

     
      

       write_lsn pg_lsn
      

      

       Last write-ahead log location written to disk by this standby
       server
      

     

     
      

       flush_lsn pg_lsn
      

      

       Last write-ahead log location flushed to disk by this standby
       server
      

     

     
      

       replay_lsn pg_lsn
      

      

       Last write-ahead log location replayed into the database on this
       standby server
      

     

     
      

       write_lag interval
      

      

       Time elapsed between flushing recent WAL locally and receiving
       notification that this standby server has written it (but not yet
       flushed it or applied it).  This can be used to gauge the delay that
       synchronous_commit level
       remote_write incurred while committing if this
       server was configured as a synchronous standby.
      

     

     
      

       flush_lag interval
      

      

       Time elapsed between flushing recent WAL locally and receiving
       notification that this standby server has written and flushed it
       (but not yet applied it).  This can be used to gauge the delay that
       synchronous_commit level
       on incurred while committing if this
       server was configured as a synchronous standby.
      

     

     
      

       replay_lag interval
      

      

       Time elapsed between flushing recent WAL locally and receiving
       notification that this standby server has written, flushed and
       applied it.  This can be used to gauge the delay that
       synchronous_commit level
       remote_apply incurred while committing if this
       server was configured as a synchronous standby.
      

     

     
      

       sync_priority integer
      

      

       Priority of this standby server for being chosen as the
       synchronous standby in a priority-based synchronous replication.
       This has no effect in a quorum-based synchronous replication.
      

     

     
      

       sync_state text
      

      

       Synchronous state of this standby server.
       Possible values are:
       

        

         

          async: This standby server is asynchronous.
         

        

        

         

          potential: This standby server is now asynchronous,
          but can potentially become synchronous if one of current
          synchronous ones fails.
         

        

        

         

          sync: This standby server is synchronous.
         

        

        

         

          quorum: This standby server is considered as a candidate
          for quorum standbys.
         

        

       

      

     

     
      

       reply_time timestamp with time zone
      

      

       Send time of last reply message received from standby server
      

     
    
   
  

  

   The lag times reported in the pg_stat_replication
   view are measurements of the time taken for recent WAL to be written,
   flushed and replayed and for the sender to know about it.  These times
   represent the commit delay that was (or would have been) introduced by each
   synchronous commit level, if the remote server was configured as a
   synchronous standby.  For an asynchronous standby, the
   replay_lag column approximates the delay
   before recent transactions became visible to queries.  If the standby
   server has entirely caught up with the sending server and there is no more
   WAL activity, the most recently measured lag times will continue to be
   displayed for a short time and then show NULL.
  

  

   Lag times work automatically for physical replication. Logical decoding
   plugins may optionally emit tracking messages; if they do not, the tracking
   mechanism will simply display NULL lag.
  

  
   

    The reported lag times are not predictions of how long it will take for
    the standby to catch up with the sending server assuming the current
    rate of replay.  Such a system would show similar times while new WAL is
    being generated, but would differ when the sender becomes idle.  In
    particular, when the standby has caught up completely,
    pg_stat_replication shows the time taken to
    write, flush and replay the most recent reported WAL location rather than
    zero as some users might expect.  This is consistent with the goal of
    measuring synchronous commit and transaction visibility delays for
    recent write transactions.
    To reduce confusion for users expecting a different model of lag, the
    lag columns revert to NULL after a short time on a fully replayed idle
    system. Monitoring systems should choose whether to represent this
    as missing data, zero or continue to display the last known value.
   

  

 

 

  
# pg_stat_replication_slots

  
   pg_stat_replication_slots
  

  

   The pg_stat_replication_slots view will contain
   one row per logical replication slot, showing statistics about its usage.
  

  
   
# pg_stat_replication_slots View

   
    
     
      

        Column Type
       

       

        Description
      

     
    

    
     
      

        slot_name text
       

       

        A unique, cluster-wide identifier for the replication slot
      

     

     
      

        spill_txns bigint
       

       

        Number of transactions spilled to disk once the memory used by
        logical decoding to decode changes from WAL has exceeded
        logical_decoding_work_mem. The counter gets
        incremented for both top-level transactions and subtransactions.
      

     

     
      

        spill_count bigint
       

       

        Number of times transactions were spilled to disk while decoding
        changes from WAL for this slot. This counter is incremented each time
        a transaction is spilled, and the same transaction may be spilled
        multiple times.
      

     

     
      

        spill_bytes bigint
       

       

        Amount of decoded transaction data spilled to disk while performing
        decoding of changes from WAL for this slot. This and other spill
        counters can be used to gauge the I/O which occurred during logical
        decoding and allow tuning logical_decoding_work_mem.
      

     

     
      

        stream_txns bigint
       

       

        Number of in-progress transactions streamed to the decoding output
        plugin after the memory used by logical decoding to decode changes
        from WAL for this slot has exceeded
        logical_decoding_work_mem. Streaming only
        works with top-level transactions (subtransactions can't be streamed
        independently), so the counter is not incremented for subtransactions.
       

     

     
      

        stream_countbigint
       

       

        Number of times in-progress transactions were streamed to the decoding
        output plugin while decoding changes from WAL for this slot. This
        counter is incremented each time a transaction is streamed, and the
        same transaction may be streamed multiple times.
      

     

     
      

        stream_bytesbigint
       

       

        Amount of transaction data decoded for streaming in-progress
        transactions to the decoding output plugin while decoding changes from
        WAL for this slot. This and other streaming counters for this slot can
        be used to tune logical_decoding_work_mem.
       

      
     

     
      

        mem_exceeded_countbigint
       

       

        Number of times the memory used by logical decoding has exceeded
        logical_decoding_work_mem.
       

      
     

     
      

        total_txns bigint
       

       

        Number of decoded transactions sent to the decoding output plugin for
        this slot. This counts top-level transactions only, and is not incremented
        for subtransactions. Note that this includes the transactions that are
        streamed and/or spilled.
       

     

     
      

        total_bytesbigint
       

       

        Amount of transaction data decoded for sending transactions to the
        decoding output plugin while decoding changes from WAL for this slot.
        Note that this includes data that is streamed and/or spilled.
       

      
     

     
      

        slotsync_skip_countbigint
       

       

        Number of times the slot synchronization is skipped. Slot
        synchronization occurs only on standby servers and thus this column has
        no meaning on the primary server.
       

      
     

     
      

        slotsync_last_skiptimestamp with time zone
       

       

        Time at which last slot synchronization was skipped. Slot
        synchronization occurs only on standby servers and thus this column has
        no meaning on the primary server.
       

      
     

     
      

        stats_reset timestamp with time zone
       

       

        Time at which these statistics were last reset
       

     
    
   
  

 

 

  
# pg_stat_wal_receiver

  
   pg_stat_wal_receiver
  

  

   The pg_stat_wal_receiver view will contain only
   one row, showing statistics about the WAL receiver from that receiver's
   connected server.
  

  
   
# pg_stat_wal_receiver View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of the WAL receiver process
      

     

     
      

       status text
      

      

       Activity status of the WAL receiver process. Possible values are:
       

        

         

          restarting: WAL receiver has been asked to
          restart streaming.
         

        

        

         

          starting: WAL receiver process has been launched
          but is not yet initialized.
         

        

        

         

          connecting: WAL receiver is connecting to the
          upstream server, replication has not yet started.
         

        

        

         

          stopping: WAL receiver has been requested to
          stop.
         

        

        

         

          streaming: WAL receiver is streaming WAL data.
         

        

        

         

          waiting: WAL receiver has stopped streaming and
          is waiting for new instructions from the startup process.
         

        

       

      

     

     
      

       receive_start_lsn pg_lsn
      

      

       First write-ahead log location used when WAL receiver is
       started
      

     

     
      

       receive_start_tli integer
      

      

       First timeline number used when WAL receiver is started
      

     

     
      

       written_lsn pg_lsn
      

      

       Last write-ahead log location already received and written to disk,
       but not flushed. This should not be used for data integrity checks.
      

     

     
      

       flushed_lsn pg_lsn
      

      

       Last write-ahead log location already received and flushed to
       disk, the initial value of this field being the first log location used
       when WAL receiver is started
      

     

     
      

       received_tli integer
      

      

       Timeline number of last write-ahead log location received and
       flushed to disk, the initial value of this field being the timeline
       number of the first log location used when WAL receiver is started
      

     

     
      

       last_msg_send_time timestamp with time zone
      

      

       Send time of last message received from origin WAL sender
      

     

     
      

       last_msg_receipt_time timestamp with time zone
      

      

       Receipt time of last message received from origin WAL sender
      

     

     
      

       latest_end_lsn pg_lsn
      

      

       Last write-ahead log location reported to origin WAL sender
      

     

     
      

       latest_end_time timestamp with time zone
      

      

       Time of last write-ahead log location reported to origin WAL sender
      

     

     
      

       slot_name text
      

      

       Replication slot name used by this WAL receiver
      

     

     
      

       sender_host text
      

      

       Host of the PostgreSQL instance
       this WAL receiver is connected to. This can be a host name,
       an IP address, or a directory path if the connection is via
       Unix socket.  (The path case can be distinguished because it
       will always be an absolute path, beginning with /.)
      

     

     
      

       sender_port integer
      

      

       Port number of the PostgreSQL instance
       this WAL receiver is connected to.
      

     

     
      

       conninfo text
      

      

       Connection string used by this WAL receiver,
       with security-sensitive fields obfuscated.
      

     
    
   
  

 

 

  
# pg_stat_recovery

  
   pg_stat_recovery
  

  

   The pg_stat_recovery view will contain at most
   one row, showing statistics about the recovery state of the startup
   process.  This view returns no rows when the server is not in recovery
   or the user does not have privileges of the
   pg_read_all_stats role.
  

  
   
# pg_stat_recovery View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       promote_triggered boolean
      

      

       True if a promotion has been triggered.
      

     

     
      

       last_replayed_read_lsn pg_lsn
      

      

       Start write-ahead log location of the last successfully replayed
       WAL record.
      

     

     
      

       last_replayed_end_lsn pg_lsn
      

      

       End write-ahead log location, plus one, of the last successfully
       replayed WAL record.
      

     

     
      

       last_replayed_tli integer
      

      

       Timeline of the last successfully replayed WAL record.
      

     

     
      

       replay_end_lsn pg_lsn
      

      

       Write-ahead log location of the record currently being replayed
       (end position plus one).  When no record is being actively replayed,
       equals last_replayed_end_lsn.
      

     

     
      

       replay_end_tli integer
      

      

       Timeline of the WAL record currently being replayed.  When no record
       is being actively replayed, equals
       last_replayed_tli.
      

     

     
      

       recovery_last_xact_time timestamp with time zone
      

      

       Timestamp of the last transaction commit or abort record replayed
       during recovery.  This is the time at which the commit or abort WAL
       record for that transaction was generated on the primary.
      

     

     
      

       current_chunk_start_time timestamp with time zone
      

      

       Time when the startup process observed that replay had caught up
       with the latest WAL chunk received from streaming replication.
       Used in recovery-conflict timing and replay/apply-lag diagnostics.
       NULL if streaming WAL has not yet been received or the time is not
       available.
      

     

     
      

       pause_state text
      

      

       Recovery pause state. Possible values are:
      

       

        

         

          not paused: Recovery is proceeding normally.
         

        

        

         

          pause requested: A pause has been requested
          but recovery has not yet paused.
         

        

        

         

          paused: Recovery is paused.
         

        

       

      
     

    
   
  

 

 

  
# pg_stat_recovery_prefetch

  
   pg_stat_recovery_prefetch
  

  

   The pg_stat_recovery_prefetch view will contain
   only one row.  The columns wal_distance,
   block_distance and
   io_depth show current values, and the
   other columns show cumulative counters that can be reset
   with the pg_stat_reset_shared function.
  

  
   
# pg_stat_recovery_prefetch View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      
       

        stats_reset timestamp with time zone
       

       

        Time at which these statistics were last reset
       

      
     

     
      
       

        prefetch bigint
       

       

        Number of blocks prefetched because they were not in the buffer pool
       

      
     

     
      
       

        hit bigint
       

       

        Number of blocks not prefetched because they were already in the buffer pool
       

      
     

     
      
       

        skip_init bigint
       

       

        Number of blocks not prefetched because they would be zero-initialized
       

      
     

     
      
       

        skip_new bigint
       

       

        Number of blocks not prefetched because they didn't exist yet
       

      
     

     
      
       

        skip_fpw bigint
       

       

        Number of blocks not prefetched because a full page image was included in the WAL
       

      
     

     
      
       

        skip_rep bigint
       

       

        Number of blocks not prefetched because they were already recently prefetched
       

      
     

     
      
       

        wal_distance int
       

       

        How many bytes ahead the prefetcher is looking
       

      
     

     
      
       

        block_distance int
       

       

        How many blocks ahead the prefetcher is looking
       

      
     

     
      
       

        io_depth int
       

       

        How many prefetches have been initiated but are not yet known to have completed
       

      
     
    
   
  

 

 

  
# pg_stat_subscription

  
   pg_stat_subscription
  

  
   
# pg_stat_subscription View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       subid oid
      

      

       OID of the subscription
      

     

     
      

       subname name
      

      

       Name of the subscription
      

     

     
      

       worker_type text
      

      

       Type of the subscription worker process.  Possible types are
       apply, parallel apply,
       table synchronization, and
       sequence synchronization.
      

     

     
      

       pid integer
      

      

       Process ID of the subscription worker process
      

     

     
      

       leader_pid integer
      

      

       Process ID of the leader apply worker if this process is a parallel
       apply worker; NULL if this process is a leader apply worker, a table
       synchronization worker or a sequence synchronization worker
      

     

     
      

       relid oid
      

      

       OID of the relation that the worker is synchronizing; NULL for the
       leader apply worker, parallel apply workers and the sequence
       synchronization worker
      

     

     
      

       received_lsn pg_lsn
      

      

       Last write-ahead log location received, the initial value of
       this field being 0; NULL for parallel apply workers and the sequence
       synchronization worker
      

     

     
      

       last_msg_send_time timestamp with time zone
      

      

       Send time of last message received from origin WAL sender; NULL for
       parallel apply workers and the sequence synchronization worker
      

     

     
      

       last_msg_receipt_time timestamp with time zone
      

      

       Receipt time of last message received from origin WAL sender; NULL for
       parallel apply workers and the sequence synchronization worker
      

     

     
      

       latest_end_lsn pg_lsn
      

      

       Last write-ahead log location reported to origin WAL sender; NULL for
       parallel apply workers and the sequence synchronization worker
      

     

     
      

       latest_end_time timestamp with time zone
      

      

       Time of last write-ahead log location reported to origin WAL
       sender; NULL for parallel apply workers and the sequence synchronization
       worker
      

     
    
   
  

 

 

  
# pg_stat_subscription_stats

  
   pg_stat_subscription_stats
  

  

   The pg_stat_subscription_stats view will contain
   one row per subscription.
  

  
   
# pg_stat_subscription_stats View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       subid oid
      

      

       OID of the subscription
      

     

     
      

       subname name
      

      

       Name of the subscription
      

     

     
      

       apply_error_count bigint
      

      

       Number of times an error occurred while applying changes. Note that any
       conflict resulting in an apply error will be counted in both
       apply_error_count and the corresponding conflict
       count (e.g., confl_*).
      

     

     
      

       sync_seq_error_count bigint
      

      

       Number of times an error occurred in the sequence synchronization
       worker. A single worker synchronizes all sequences, so one error
       increment may represent failures across multiple sequences.
      

     

     
      

       sync_table_error_count bigint
      

      

       Number of times an error occurred during the initial table
       synchronization
      

     

     
      

       confl_insert_exists bigint
      

      

       Number of times a row insertion violated a
       NOT DEFERRABLE unique constraint during the
       application of changes. See 
       for details about this conflict.
      

     

     
      

       confl_update_origin_differs bigint
      

      

       Number of times an update was applied to a row that had been previously
       modified by another source during the application of changes. See
        for details about this
       conflict.
      

     

     
      

       confl_update_exists bigint
      

      

       Number of times that an updated row value violated a
       NOT DEFERRABLE unique constraint during the
       application of changes. See 
       for details about this conflict.
      

     

     
      

       confl_update_deleted bigint
      

      

       Number of times the tuple to be updated was concurrently deleted by
       another source during the application of changes. See 
       for details about this conflict.
      

     

     
      

       confl_update_missing bigint
      

      

       Number of times the tuple to be updated was not found during the
       application of changes. See 
       for details about this conflict.
      

     

     
      

       confl_delete_origin_differs bigint
      

      

       Number of times a delete operation was applied to row that had been
       previously modified by another source during the application of changes.
       See  for details about
       this conflict.
      

     

     
      

       confl_delete_missing bigint
      

      

       Number of times the tuple to be deleted was not found during the application
       of changes. See  for details
       about this conflict.
      

     

     
      

       confl_multiple_unique_conflicts bigint
      

      

       Number of times a row insertion or an updated row values violated multiple
       NOT DEFERRABLE unique constraints during the
       application of changes. See 
       for details about this conflict.
      

     

     
      

       stats_reset timestamp with time zone
      

      

       Time at which these statistics were last reset
      

     
    
   
  

 

 

  
# pg_stat_ssl

  
   pg_stat_ssl
  

  

   The pg_stat_ssl view will contain one row per
   backend or WAL sender process, showing statistics about SSL usage on
   this connection. It can be joined to pg_stat_activity
   or pg_stat_replication on the
   pid column to get more details about the
   connection.
  

  
   
# pg_stat_ssl View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of a backend or WAL sender process
      

     

     
      

       ssl boolean
      

      

       True if SSL is used on this connection
      

     

     
      

       version text
      

      

       Version of SSL in use, or NULL if SSL is not in use
       on this connection
      

     

     
      

       cipher text
      

      

       Name of SSL cipher in use, or NULL if SSL is not in use
       on this connection
      

     

     
      

       bits integer
      

      

       Number of bits in the encryption algorithm used, or NULL
       if SSL is not used on this connection
      

     

     
      

       client_dn text
      

      

       Distinguished Name (DN) field from the client certificate
       used, or NULL if no client certificate was supplied or if SSL
       is not in use on this connection. This field is truncated if the
       DN field is longer than NAMEDATALEN (64 characters
       in a standard build).
      

     

     
      

       client_serial numeric
      

      

       Serial number of the client certificate, or NULL if no client
       certificate was supplied or if SSL is not in use on this connection.  The
       combination of certificate serial number and certificate issuer uniquely
       identifies a certificate (unless the issuer erroneously reuses serial
       numbers).
      

     

     
      

       issuer_dn text
      

      

       DN of the issuer of the client certificate, or NULL if no client
       certificate was supplied or if SSL is not in use on this connection.
       This field is truncated like client_dn.
      

     
    
   
  

 

 

  
# pg_stat_gssapi

  
   pg_stat_gssapi
  

  

   The pg_stat_gssapi view will contain one row per
   backend, showing information about GSSAPI usage on this connection. It can
   be joined to pg_stat_activity or
   pg_stat_replication on the
   pid column to get more details about the
   connection.
  

  
   
# pg_stat_gssapi View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of a backend
      

     

     
      

       gss_authenticated boolean
      

      

       True if GSSAPI authentication was used for this connection
      

     

     
      

       principal text
      

      

       Principal used to authenticate this connection, or NULL
       if GSSAPI was not used to authenticate this connection.  This
       field is truncated if the principal is longer than
       NAMEDATALEN (64 characters in a standard build).
      

     

     
      

       encrypted boolean
      

      

       True if GSSAPI encryption is in use on this connection
      

     

     
      

       credentials_delegated boolean
      

      

       True if GSSAPI credentials were delegated on this connection.
      

     
    
   
  

 

 

  
# pg_stat_archiver

  
   pg_stat_archiver
  

  

   The pg_stat_archiver view will always have a
   single row, containing data about the archiver process of the cluster.
  

  
   
# pg_stat_archiver View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       archived_count bigint
      

      

       Number of WAL files that have been successfully archived
      

     

     
      

       last_archived_wal text
      

      

       Name of the WAL file most recently successfully archived
      

     

     
      

       last_archived_time timestamp with time zone
      

      

       Time of the most recent successful archive operation
      

     

     
      

       failed_count bigint
      

      

       Number of failed attempts for archiving WAL files
      

     

     
      

       last_failed_wal text
      

      

       Name of the WAL file of the most recent failed archival operation
      

     

     
      

       last_failed_time timestamp with time zone
      

      

       Time of the most recent failed archival operation
      

     

     
      

       stats_reset timestamp with time zone
      

      

       Time at which these statistics were last reset
      

     
    
   
  

  

    Normally, WAL files are archived in order, oldest to newest, but that is
    not guaranteed, and does not hold under special circumstances like when
    promoting a standby or after crash recovery. Therefore it is not safe to
    assume that all files older than
    last_archived_wal have also been successfully
    archived.
  

 

 

  
# pg_stat_io

  
   pg_stat_io
  

  

   The pg_stat_io view will contain one row for each
   combination of backend type, target I/O object, and I/O context, showing
   cluster-wide I/O statistics. Combinations which do not make sense are
   omitted.
  

  

   Currently, I/O on relations (e.g. tables, indexes) and WAL activity are
   tracked. However, relation I/O which bypasses shared buffers
   (e.g. when moving a table from one tablespace to another) is currently
   not tracked.
  

  
   
# pg_stat_io View

   
    
     
      
       

        Column Type
       

       

        Description
       

      
     
    
    
     
      
       

        backend_type text
       

       

        Type of backend (e.g. background worker, autovacuum worker). See 
        pg_stat_activity for more information
        on backend_types. Some
        backend_types do not accumulate I/O operation
        statistics and will not be included in the view.
       

      
     

     
      
       

        object text
       

       

        Target object of an I/O operation. Possible values are:
       

        

         

          relation: Permanent relations.
         

        

        

         

          temp relation: Temporary relations.
         

        

        

         

          wal: Write Ahead Logs.
         

        

       

       

      
     

     
      
       

        context text
       

       

        The context of an I/O operation. Possible values are:
       

       

        

         

          normal: The default or standard
          context for a type of I/O operation. For
          example, by default, relation data is read into and written out from
          shared buffers. Thus, reads and writes of relation data to and from
          shared buffers are tracked in context
          normal.
         

        

        

         

          init: I/O operations performed while creating the
          WAL segments are tracked in context
          init.
         

        

        

         

          vacuum: I/O operations performed outside of shared
          buffers while vacuuming and analyzing permanent relations. Temporary
          table vacuums use the same local buffer pool as other temporary table
          I/O operations and are tracked in context
          normal.
         

        

        

         

          bulkread: Certain large read I/O operations
          done outside of shared buffers, for example, a sequential scan of a
          large table.
         

        

        

         

          bulkwrite: Certain large write I/O operations
          done outside of shared buffers, such as COPY.
         

        

       

      
     

     
      
       

        reads bigint
       

       

        Number of read operations.
       

      
     

     
      
       

        read_bytes numeric
       

       

        The total size of read operations in bytes.
       

      
     

     
      
       

        read_time double precision
       

       

        Time spent waiting for read operations in milliseconds (if
         is enabled and
        object is not wal,
        or if  is enabled
        and object is wal,
        otherwise zero). This may include time spent waiting for a
        read started by another backend. In that case
        reads may still be zero.
       

      
     

     
      
       

        writes bigint
       

       

        Number of write operations.
       

      
     

     
      
       

        write_bytes numeric
       

       

        The total size of write operations in bytes.
       

      
     

     
      
       

        write_time double precision
       

       

        Time spent waiting for write operations in milliseconds (if
         is enabled and
        object is not wal,
        or if  is enabled
        and object is wal,
        otherwise zero)
       

      
     

     
      
       

        writebacks bigint
       

       

        Number of units of size BLCKSZ (typically 8kB) which
        the process requested the kernel write out to permanent storage.
       

      
     

     
      
       

        writeback_time double precision
       

       

        Time spent waiting for writeback operations in milliseconds (if
         is enabled, otherwise zero). This
        includes the time spent queueing write-out requests and, potentially,
        the time spent to write out the dirty data.
       

      
     

     
      
       

        extends bigint
       

       

        Number of relation extend operations.
       

      
     

     
      
       

        extend_bytes numeric
       

       

        The total size of relation extend operations in bytes.
       

      
     

     
      
       

        extend_time double precision
       

       

        Time spent waiting for extend operations in milliseconds. (if
         is enabled and
        object is not wal,
        or if  is enabled
        and object is wal,
        otherwise zero)
       

      
     

     
      
       

        hits bigint
       

       

        The number of times a desired block was found in a shared buffer.
       

      
     

     
      
       

        evictions bigint
       

       

        Number of times a block has been written out from a shared or local
        buffer in order to make it available for another use.
       

       

        In context normal, this counts
        the number of times a block was evicted from a buffer and replaced with
        another block. In contexts
        bulkwrite, bulkread, and
        vacuum, this counts the number of times a block was
        evicted from shared buffers in order to add the shared buffer to a
        separate, size-limited ring buffer for use in a bulk I/O operation.
        

      
     

     
      
       

        reuses bigint
       

       

        The number of times an existing buffer in a size-limited ring buffer
        outside of shared buffers was reused as part of an I/O operation in the
        bulkread, bulkwrite, or
        vacuum contexts.
       

      
     

     
      
       

        fsyncs bigint
       

       

        Number of fsync calls. These are only tracked in
        context normal.
       

      
     

     
      
       

        fsync_time double precision
       

       

        Time spent waiting for fsync operations in milliseconds (if
         is enabled and
        object is not wal,
        or if  is enabled
        and object is wal,
        otherwise zero)
       

      
     

     
      
       

        stats_reset timestamp with time zone
       

       

        Time at which these statistics were last reset.
       

      
     
    
   
  

  

   Some backend types never perform I/O operations on some I/O objects and/or
   in some I/O contexts. These rows are omitted from the view. For example, the
   checkpointer does not checkpoint temporary tables, so there will be no rows
   for backend_type checkpointer and
   object temp relation.
  

  

   In addition, some I/O operations will never be performed either by certain
   backend types or on certain I/O objects and/or in certain I/O contexts.
   These cells will be NULL. For example, temporary tables are not
   fsynced, so fsyncs will be NULL for
   object temp relation. Also, the
   background writer does not perform reads, so reads will
   be NULL in rows for backend_type background
   writer.
  

  

   For the object wal,
   fsyncs and fsync_time track the
   fsync activity of WAL files done in issue_xlog_fsync.
   writes and write_time
   track the write activity of WAL files done in
   XLogWrite.
   See  for more information.
  

  

   pg_stat_io can be used to inform database tuning.
   For example:
   

    

     

      A high evictions count can indicate that shared
      buffers should be increased.
     

    

    

     

      Client backends rely on the checkpointer to ensure data is persisted to
      permanent storage. Large numbers of fsyncs by
      client backends could indicate a misconfiguration of
      shared buffers or of the checkpointer. More information on configuring
      the checkpointer can be found in .
     

    

    

     

      Normally, client backends should be able to rely on auxiliary processes
      like the checkpointer and the background writer to write out dirty data
      as much as possible. Large numbers of writes by client backends could
      indicate a misconfiguration of shared buffers or of the checkpointer.
      More information on configuring the checkpointer can be found in .
     

    

   

  

  
   

    Columns tracking I/O wait time will only be non-zero when
     is enabled. The user should be
    careful when referencing these columns in combination with their
    corresponding I/O operations in case track_io_timing
    was not enabled for the entire time since the last stats reset.
   

  

 

 

  
# pg_stat_kind_info

  
   pg_stat_kind_info
  

  

   The pg_stat_kind_info view contains one row for
   each registered statistics kind, including both built-in and custom kinds.
  

  
   
# pg_stat_kind_info View

   
    
     
      
       

        Column Type
       

       

        Description
       

      
     
    
    
     
      
       

        id integer
       

       

        Numeric identifier of the statistics kind.
       

      
     

     
      
       

        name text
       

       

        Name of the statistics kind.
       

      
     

     
      
       

        builtin boolean
       

       

        True if this is a built-in statistics kind, false if it was registered
        by an extension.
       

      
     

     
      
       

        fixed_amount boolean
       

       

        True if this kind tracks a fixed amount of data (a single, statically
        allocated entry), false if it tracks a variable number of entries
        keyed by object identifier.
       

      
     

     
      
       

        accessed_across_databases boolean
       

       

        True if entries of this kind are accessed across databases (cluster-wide
        statistics), false if they are scoped to a single database.
       

      
     

     
      
       

        write_to_file boolean
       

       

        True if entries of this kind are persisted to the statistics file at
        shutdown and reloaded on startup, false if they are kept only in
        shared memory.
       

      
     

     
      
       

        entry_count bigint
       

       

        Number of tracked entries for this kind. For variable-numbered kinds,
        this is the number of objects currently tracked.
        NULL for fixed-sized statistics kinds, or if the
        kind does not track entry counts.
       

      
     
    
   
  
 

 

  
# pg_stat_lock

  
   pg_stat_lock
  

  

   The pg_stat_lock view will contain one row for each
   lock type, showing cluster-wide locks statistics.
  

  
   
# pg_stat_lock View

   
    
     
      
       

        Column Type
       

       

        Description
       

      
     
    
    
     
      
       

        locktype text
       

       

        Type of the lockable object. See 
        pg_locks for details.
       

      
     

     
      
       

        waits bigint
       

       

        Number of times a lock of this type had to wait because of a
        conflicting lock. Only incremented when the lock was successfully
        acquired after waiting longer than .
       

      
     

     
      
       

        wait_time double precision
       

       

        Total time spent waiting for locks of this type, in milliseconds.
        Only incremented when the lock was successfully acquired after waiting
        longer than .
       

      
     

     
      
       

        fastpath_exceeded bigint
       

       

        Number of times a lock of this type could not be acquired via fast path
        because the fast path slot limit was exceeded. Increasing
         can reduce this number.
        See  for which locks are
        eligible for fast-path locking; for ineligible lock types this
        counter is always zero.
       

      
     

     
      
       

        stats_reset timestamp with time zone
       

       

        Time at which these statistics were last reset.
       

      
     
    
   
  
 

 

  
# pg_stat_bgwriter

  
   pg_stat_bgwriter
  

  

   The pg_stat_bgwriter view will always have a
   single row, containing data about the background writer of the cluster.
  

  
   
# pg_stat_bgwriter View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       buffers_clean bigint
      

      

       Number of buffers written by the background writer
      

     

     
      

       maxwritten_clean bigint
      

      

       Number of times the background writer stopped a cleaning
       scan because it had written too many buffers
      

     

     
      

       buffers_alloc bigint
      

      

       Number of buffers allocated
      

     

     
      

       stats_reset timestamp with time zone
      

      

       Time at which these statistics were last reset
      

     
    
   
  

 

 

  
# pg_stat_checkpointer

  
   pg_stat_checkpointer
  

  

   The pg_stat_checkpointer view will always have a
   single row, containing data about the checkpointer process of the cluster.
  

  
   
# pg_stat_checkpointer View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       num_timed bigint
      

      

       Number of scheduled checkpoints due to timeout
      

     

     
      

       num_requested bigint
      

      

       Number of requested checkpoints
      

     

      
      

       num_done bigint
      

      

       Number of checkpoints that have been performed
      

     

     
      

       restartpoints_timed bigint
      

      

       Number of scheduled restartpoints due to timeout or after a failed attempt to perform it
      

     

     
      

       restartpoints_req bigint
      

      

       Number of requested restartpoints
      

     

     
      

       restartpoints_done bigint
      

      

       Number of restartpoints that have been performed
      

     

     
      

       write_time double precision
      

      

       Total amount of time that has been spent in the portion of
       processing checkpoints and restartpoints where files are written to disk,
       in milliseconds
      

     

     
      

       sync_time double precision
      

      

       Total amount of time that has been spent in the portion of
       processing checkpoints and restartpoints where files are synchronized to
       disk, in milliseconds
      

     

     
      

       buffers_written bigint
      

      

       Number of shared buffers written during checkpoints and restartpoints
      

     

     
      

        slru_written bigint
      

      

        Number of SLRU buffers written during checkpoints and restartpoints
      

     

     
      

       stats_reset timestamp with time zone
      

      

       Time at which these statistics were last reset
      

     
    
   
  

  

   Checkpoints may be skipped if the server has been idle since the last one.
   num_timed and
   num_requested count both completed and skipped
   checkpoints, while num_done tracks only
   the completed ones.  Similarly, restartpoints may be skipped
   if the last replayed checkpoint record is already the last restartpoint.
   restartpoints_timed and
   restartpoints_req count both completed and
   skipped restartpoints, while restartpoints_done
   tracks only the completed ones.
  

 

 

   
# pg_stat_wal

  
   pg_stat_wal
  

  

   The pg_stat_wal view will always have a
   single row, containing data about WAL activity of the cluster.
  

  
   
# pg_stat_wal View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       wal_records bigint
      

      

       Total number of WAL records generated
      

     

     
      

       wal_fpi bigint
      

      

       Total number of WAL full page images generated
      

     

     
      

       wal_bytes numeric
      

      

       Total amount of WAL generated in bytes
      

     

     
      

       wal_fpi_bytes numeric
      

      

       Total amount of WAL full page images in bytes
      

     

     
      

       wal_buffers_full bigint
      

      

       Number of times WAL data was written to disk because WAL buffers became full
      

     

     
      

       stats_reset timestamp with time zone
      

      

       Time at which these statistics were last reset
      

     
     
   
  

 

  
# pg_stat_database

  
   pg_stat_database
  

  

   The pg_stat_database view will contain one row
   for each database in the cluster, plus one for shared objects, showing
   database-wide statistics.
  

  
   
# pg_stat_database View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       datid oid
      

      

       OID of this database, or 0 for objects belonging to a shared
       relation
      

     

     
      

       datname name
      

      

       Name of this database, or NULL for shared
       objects.
      

     

     
      

       numbackends integer
      

      

       Number of backends currently connected to this database, or
       NULL for shared objects.  This is the only column
       in this view that returns a value reflecting current state; all other
       columns return the accumulated values since the last reset.
      

     

     
      

       xact_commit bigint
      

      

       Number of transactions in this database that have been
       committed
      

     

     
      

       xact_rollback bigint
      

      

       Number of transactions in this database that have been
       rolled back
      

     

     
      

       blks_read bigint
      

      

       Number of disk blocks read in this database
      

     

     
      

       blks_hit bigint
      

      

       Number of times disk blocks were found already in the buffer
       cache, so that a read was not necessary (this only includes hits in the
       PostgreSQL buffer cache, not the operating system's file system cache)
      

     

     
      

       tup_returned bigint
      

      

       Number of live rows fetched by sequential scans and index entries returned by index scans in this database
      

     

     
      

       tup_fetched bigint
      

      

       Number of live rows fetched by index scans in this database
      

     

     
      

       tup_inserted bigint
      

      

       Number of rows inserted by queries in this database
      

     

     
      

       tup_updated bigint
      

      

       Number of rows updated by queries in this database
      

     

     
      

       tup_deleted bigint
      

      

       Number of rows deleted by queries in this database
      

     

     
      

       conflicts bigint
      

      

       Number of queries canceled due to conflicts with recovery
       in this database. (Conflicts occur only on standby servers; see
       
       pg_stat_database_conflicts for details.)
      

     

     
      

       temp_files bigint
      

      

       Number of temporary files created by queries in this database.
       All temporary files are counted, regardless of why the temporary file
       was created (e.g., sorting or hashing), and regardless of the
        setting.
      

     

     
      

       temp_bytes bigint
      

      

       Total amount of data written to temporary files by queries in
       this database. All temporary files are counted, regardless of why
       the temporary file was created, and
       regardless of the  setting.
      

     

     
      

       deadlocks bigint
      

      

       Number of deadlocks detected in this database
      

     

     
      

       checksum_failures bigint
      

      

       Number of data page checksum failures detected in this
       database (or on a shared object).  Detected failures are not reset if
       the  setting changes.  Clusters
       which are initialized without data checksums will show this as
       0. In PostgreSQL version
       18 and earlier, this was set to NULL for clusters
       with data checksums disabled.
      

     
     

     
      

       checksum_last_failure timestamp with time zone
      

      

       Time at which the last data page checksum failure was detected in
       this database (or on a shared object). Last failure is reported
       regardless of the  setting.
      

     

     
      

       blk_read_time double precision
      

      

       Time spent reading data file blocks by backends in this database,
       in milliseconds (if  is enabled,
       otherwise zero)
      

     

     
      

       blk_write_time double precision
      

      

       Time spent writing data file blocks by backends in this database,
       in milliseconds (if  is enabled,
       otherwise zero)
      

     

     
      

       session_time double precision
      

      

       Time spent by database sessions in this database, in milliseconds
       (note that statistics are only updated when the state of a session
       changes, so if sessions have been idle for a long time, this idle time
       won't be included)
      

     

     
      

       active_time double precision
      

      

       Time spent executing SQL statements in this database, in milliseconds
       (this corresponds to the states active and
       fastpath function call in
       
       pg_stat_activity)
      

     

     
      

       idle_in_transaction_time double precision
      

      

       Time spent idling while in a transaction in this database, in milliseconds
       (this corresponds to the states idle in transaction and
       idle in transaction (aborted) in
       
       pg_stat_activity)
      

     

     
      

       sessions bigint
      

      

       Total number of sessions established to this database
      

     

     
      

       sessions_abandoned bigint
      

      

       Number of database sessions to this database that were terminated
       because connection to the client was lost
      

     

     
      

       sessions_fatal bigint
      

      

       Number of database sessions to this database that were terminated
       by fatal errors
      

     

     
      

       sessions_killed bigint
      

      

       Number of database sessions to this database that were terminated
       by operator intervention
      

     

     
      

       parallel_workers_to_launch bigint
      

      

       Number of parallel workers planned to be launched by queries on this database
      

     

     
      

       parallel_workers_launched bigint
      

      

       Number of parallel workers launched by queries on this database
      

     

     
      

       stats_reset timestamp with time zone
      

      

       Time at which these statistics were last reset
      

     
    
   
  

 

 

  
# pg_stat_database_conflicts

  
   pg_stat_database_conflicts
  

  

   The pg_stat_database_conflicts view will contain
   one row per database, showing database-wide statistics about
   query cancels occurring due to conflicts with recovery on standby servers.
   This view will only contain information on standby servers, since
   conflicts do not occur on primary servers.
  

  
   
# pg_stat_database_conflicts View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       datid oid
      

      

       OID of a database
      

     

     
      

       datname name
      

      

       Name of this database
      

     

     
      

       confl_tablespace bigint
      

      

       Number of queries in this database that have been canceled due to
       dropped tablespaces
      

     

     
      

       confl_lock bigint
      

      

       Number of queries in this database that have been canceled due to
       lock timeouts
      

     

     
      

       confl_snapshot bigint
      

      

       Number of queries in this database that have been canceled due to
       old snapshots
      

     

     
      

       confl_bufferpin bigint
      

      

       Number of queries in this database that have been canceled due to
       pinned buffers
      

     

     
      

       confl_deadlock bigint
      

      

       Number of queries in this database that have been canceled due to
       deadlocks
      

     

     
      

       confl_active_logicalslot bigint
      

      

       Number of uses of logical slots in this database that have been
       canceled due to old snapshots or too low a 
       on the primary
      

     

     
      

       stats_reset timestamp with time zone
      

      

       Time at which these statistics were last reset
      

     
    
   
  

 

 

  
# pg_stat_all_tables

  
   pg_stat_all_tables
  

  

   The pg_stat_all_tables view will contain
   one row for each table in the current database (including TOAST
   tables), showing statistics about accesses to that specific table. The
   pg_stat_user_tables and
   pg_stat_sys_tables views
   contain the same information,
   but filtered to only show user and system tables respectively.
  

  
   
# pg_stat_all_tables View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       relid oid
      

      

       OID of a table
      

     

     
      

       schemaname name
      

      

       Name of the schema that this table is in
      

     

     
      

       relname name
      

      

       Name of this table
      

     

     
      

       seq_scan bigint
      

      

       Number of sequential scans initiated on this table
      

     

     
      

       last_seq_scan timestamp with time zone
      

      

       The time of the last sequential scan on this table, based on the
       most recent transaction stop time
      

     

     
      

       seq_tup_read bigint
      

      

       Number of live rows fetched by sequential scans
      

     

     
      

       idx_scan bigint
      

      

       Number of index scans initiated on this table
      

     

     
      

       last_idx_scan timestamp with time zone
      

      

       The time of the last index scan on this table, based on the
       most recent transaction stop time
      

     

     
      

       idx_tup_fetch bigint
      

      

       Number of live rows fetched by index scans
      

     

     
      

       n_tup_ins bigint
      

      

       Total number of rows inserted
      

     

     
      

       n_tup_upd bigint
      

      

       Total number of rows updated.  (This includes row updates
       counted in n_tup_hot_upd and
       n_tup_newpage_upd, and remaining
       non-HOT updates.)
      

     

     
      

       n_tup_del bigint
      

      

       Total number of rows deleted
      

     

     
      

       n_tup_hot_upd bigint
      

      

       Number of rows HOT updated.
       These are updates where no successor versions are required in
       indexes.
      

     

     
      

       n_tup_newpage_upd bigint
      

      

       Number of rows updated where the successor version goes onto a
       new heap page, leaving behind an original
       version with a
       t_ctid
        field that points to a different heap page.  These are
       always non-HOT updates.
      

     

     
      

       n_live_tup bigint
      

      

       Estimated number of live rows
      

     

     
      

       n_dead_tup bigint
      

      

       Estimated number of dead rows
      

     

     
      

       n_mod_since_analyze bigint
      

      

       Estimated number of rows modified since this table was last analyzed
      

     

     
      

       n_ins_since_vacuum bigint
      

      

       Estimated number of rows inserted since this table was last vacuumed
       (not counting VACUUM FULL)
      

     

     
      

       last_vacuum timestamp with time zone
      

      

       Last time at which this table was manually vacuumed
       (not counting VACUUM FULL)
      

     

     
      

       last_autovacuum timestamp with time zone
      

      

       Last time at which this table was vacuumed by the autovacuum
       daemon
      

     

     
      

       last_analyze timestamp with time zone
      

      

       Last time at which this table was manually analyzed
      

     

     
      

       last_autoanalyze timestamp with time zone
      

      

       Last time at which this table was analyzed by the autovacuum
       daemon
      

     

     
      

       vacuum_count bigint
      

      

       Number of times this table has been manually vacuumed
       (not counting VACUUM FULL)
      

     

     
      

       autovacuum_count bigint
      

      

       Number of times this table has been vacuumed by the autovacuum
       daemon
      

     

     
      

       analyze_count bigint
      

      

       Number of times this table has been manually analyzed
      

     

     
      

       autoanalyze_count bigint
      

      

       Number of times this table has been analyzed by the autovacuum
       daemon
      

     

     
      

       total_vacuum_time double precision
      

      

       Total time this table has been manually vacuumed, in milliseconds
       (not counting VACUUM FULL).
       (This includes the time spent sleeping due to cost-based delays.)
      

     

     
      

       total_autovacuum_time double precision
      

      

       Total time this table has been vacuumed by the autovacuum daemon,
       in milliseconds. (This includes the time spent sleeping due to
       cost-based delays.)
      

     

     
      

       total_analyze_time double precision
      

      

       Total time this table has been manually analyzed, in milliseconds.
       (This includes the time spent sleeping due to cost-based delays.)
      

     

     
      

       total_autoanalyze_time double precision
      

      

       Total time this table has been analyzed by the autovacuum daemon,
       in milliseconds. (This includes the time spent sleeping due to
       cost-based delays.)
      

     

     
      

        stats_reset timestamp with time zone
       

       

        Time at which these statistics were last reset
       

     
    
   
  

 

 

  
# pg_stat_autovacuum_scores

  
   pg_stat_autovacuum_scores
  

  

   The pg_stat_autovacuum_scores view will contain one
   row for each table in the current database (including TOAST tables), showing
   the current autovacuum scores for that specific table.  Autovacuum
   prioritizes tables deemed eligible for processing based on their
   score, with higher scores indicating higher
   priority.  See  for more information.
  

  

   While this view generates its results the same way that autovacuum workers
   do, it does so using the current source information, which might differ from
   the source information that an autovacuum worker sees when it gathers its
   list of tables to process.  Therefore, this view is not a completely
   reliable indicator of which tables autovacuum will process and what order it
   will process them.
  

  
   
# pg_stat_autovacuum_scores View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       relid oid
      

      

       Oid of the table.
      

     

     
      

       schemaname name
      

      

       Name of the schema that the table is in.
      

     

     
      

       relname name
      

      

       Name of the table.
      

     

     
      

       score double precision
      

      

       Maximum value of all component scores.  This is the value that
       autovacuum would use to sort the list of tables to process.
      

     

     
      

       xid_score double precision
      

      

       Transaction ID age component score.  Scores greater than
        indicate that
       autovacuum would vacuum the table for transaction ID wraparound
       prevention.
      

     

     
      

       mxid_score double precision
      

      

       Multixact ID age component score.  Scores greater than
        indicate
       that autovacuum would vacuum the table for multixact ID wraparound
       prevention.
      

     

     
      

       vacuum_score double precision
      

      

       Vacuum component score.  Scores greater than
        indicate that
       autovacuum would vacuum the table (unless autovacuum is disabled).
      

     

     
      

       vacuum_insert_score double precision
      

      

       Vacuum insert component score.  Scores greater than
        indicate
       that autovacuum would vacuum the table (unless autovacuum is disabled).
      

     

     
      

       analyze_score double precision
      

      

       Analyze component score.  Scores greater than
        indicate that
       autovacuum would analyze the table (unless autovacuum is disabled).
      

     

     
      

       do_vacuum bool
      

      

       Whether autovacuum would vacuum the table.  Note that even if the
       component scores indicate that autovacuum would vacuum the table, this
       may be false if autovacuum is disabled.
      

     

     
      

       do_analyze bool
      

      

       Whether autovacuum would analyze the table.  Note that even if the
       component scores indicate that autovacuum would analyze the table, this
       may be false if autovacuum is disabled.
      

     

     
      

       for_wraparound bool
      

      

       Whether autovacuum would vacuum the table for wraparound prevention.
      

     
    
   
  
 

 

  
# pg_stat_all_indexes

  
   pg_stat_all_indexes
  

  

   The pg_stat_all_indexes view will contain
   one row for each index in the current database,
   showing statistics about accesses to that specific index. The
   pg_stat_user_indexes and
   pg_stat_sys_indexes views
   contain the same information,
   but filtered to only show user and system indexes respectively.
  

  
   
# pg_stat_all_indexes View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       relid oid
      

      

       OID of the table for this index
      

     

     
      

       indexrelid oid
      

      

       OID of this index
      

     

     
      

       schemaname name
      

      

       Name of the schema this index is in
      

     

     
      

       relname name
      

      

       Name of the table for this index
      

     

     
      

       indexrelname name
      

      

       Name of this index
      

     

     
      

       idx_scan bigint
      

      

       Number of index scans initiated on this index
      

     

     
      

       last_idx_scan timestamp with time zone
      

      

       The time of the last scan on this index, based on the
       most recent transaction stop time
      

     

     
      

       idx_tup_read bigint
      

      

       Number of index entries returned by scans on this index
      

     

     
      

       idx_tup_fetch bigint
      

      

       Number of live table rows fetched by simple index scans using this
       index
      

     

     
      

        stats_reset timestamp with time zone
       

       

        Time at which these statistics were last reset
       

     
    
   
  

  

   Indexes can be used by simple index scans, bitmap index scans,
   and the optimizer.  In a bitmap scan
   the output of several indexes can be combined via AND or OR rules,
   so it is difficult to associate individual heap row fetches
   with specific indexes when a bitmap scan is used.  Therefore, a bitmap
   scan increments the
   pg_stat_all_indexes.idx_tup_read
   count(s) for the index(es) it uses, and it increments the
   pg_stat_all_tables.idx_tup_fetch
   count for the table, but it does not affect
   pg_stat_all_indexes.idx_tup_fetch.
   The optimizer also accesses indexes to check for supplied constants
   whose values are outside the recorded range of the optimizer statistics
   because the optimizer statistics might be stale.
  

  
   

    The idx_tup_read and idx_tup_fetch counts
    can be different even without any use of bitmap scans,
    because idx_tup_read counts
    index entries retrieved from the index while idx_tup_fetch
    counts live rows fetched from the table.  The latter will be less if any
    dead or not-yet-committed rows are fetched using the index, or if any
    heap fetches are avoided by means of an index-only scan.
   

  

  
   

    Index scans may sometimes perform multiple index searches per execution.
    Each index search increments pg_stat_all_indexes.idx_scan,
    so it's possible for the count of index scans to significantly exceed the
    total number of index scan executor node executions.
   

   

    This can happen with queries that use certain SQL
    constructs to search for rows matching any value out of a list or array of
    multiple scalar values (see ).  It
    can also happen to queries with a
    column_name =
     value1 OR
     column_name =
     value2 ... construct, though only
    when the optimizer transforms the construct into an equivalent
    multi-valued array representation.  Similarly, when B-tree index scans use
    the skip scan optimization, an index search is performed each time the
    scan is repositioned to the next index leaf page that might have matching
    tuples (see ).
   

  
  
   

    EXPLAIN ANALYZE outputs the total number of index
    searches performed by each index scan node.  See
     for an example demonstrating how
    this works.
   

  

 

 

  
# pg_statio_all_tables

  
   pg_statio_all_tables
  

  

   The pg_statio_all_tables view will contain
   one row for each table in the current database (including TOAST
   tables), showing statistics about I/O on that specific table. The
   pg_statio_user_tables and
   pg_statio_sys_tables views
   contain the same information,
   but filtered to only show user and system tables respectively.
  

  
   
# pg_statio_all_tables View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       relid oid
      

      

       OID of a table
      

     

     
      

       schemaname name
      

      

       Name of the schema that this table is in
      

     

     
      

       relname name
      

      

       Name of this table
      

     

     
      

       heap_blks_read bigint
      

      

       Number of disk blocks read from this table
      

     

     
      

       heap_blks_hit bigint
      

      

       Number of buffer hits in this table
      

     

     
      

       idx_blks_read bigint
      

      

       Number of disk blocks read from all indexes on this table
      

     

     
      

       idx_blks_hit bigint
      

      

       Number of buffer hits in all indexes on this table
      

     

     
      

       toast_blks_read bigint
      

      

       Number of disk blocks read from this table's TOAST table (if any)
      

     

     
      

       toast_blks_hit bigint
      

      

       Number of buffer hits in this table's TOAST table (if any)
      

     

     
      

       tidx_blks_read bigint
      

      

       Number of disk blocks read from this table's TOAST table indexes (if any)
      

     

     
      

       tidx_blks_hit bigint
      

      

       Number of buffer hits in this table's TOAST table indexes (if any)
      

     

     
      

        stats_reset timestamp with time zone
       

       

        Time at which these statistics were last reset
       

     
    
   
  

 

 

  
# pg_statio_all_indexes

  
   pg_statio_all_indexes
  

  

   The pg_statio_all_indexes view will contain
   one row for each index in the current database,
   showing statistics about I/O on that specific index. The
   pg_statio_user_indexes and
   pg_statio_sys_indexes views
   contain the same information,
   but filtered to only show user and system indexes respectively.
  

  
   
# pg_statio_all_indexes View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       relid oid
      

      

       OID of the table for this index
      

     

     
      

       indexrelid oid
      

      

       OID of this index
      

     

     
      

       schemaname name
      

      

       Name of the schema this index is in
      

     

     
      

       relname name
      

      

       Name of the table for this index
      

     

     
      

       indexrelname name
      

      

       Name of this index
      

     

     
      

       idx_blks_read bigint
      

      

       Number of disk blocks read from this index
      

     

     
      

       idx_blks_hit bigint
      

      

       Number of buffer hits in this index
      

     

     
      

        stats_reset timestamp with time zone
       

       

        Time at which these statistics were last reset
       

     
    
   
  

 

 

  
# pg_statio_all_sequences

  
   pg_statio_all_sequences
  

  

   The pg_statio_all_sequences view will contain
   one row for each sequence in the current database,
   showing statistics about I/O on that specific sequence.
  

  
   
# pg_statio_all_sequences View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       relid oid
      

      

       OID of a sequence
      

     

     
      

       schemaname name
      

      

       Name of the schema this sequence is in
      

     

     
      

       relname name
      

      

       Name of this sequence
      

     

     
      

       blks_read bigint
      

      

       Number of disk blocks read from this sequence
      

     

     
      

       blks_hit bigint
      

      

       Number of buffer hits in this sequence
      

     

     
      

       stats_reset timestamp with time zone
      

      

       Time at which these statistics were last reset
      

     
    
   
  

 

 

  
# pg_stat_user_functions

  
   pg_stat_user_functions
  

  

   The pg_stat_user_functions view will contain
   one row for each tracked function, showing statistics about executions of
   that function.  The  parameter
   controls exactly which functions are tracked.
  

  
   
# pg_stat_user_functions View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       funcid oid
      

      

       OID of a function
      

     

     
      

       schemaname name
      

      

       Name of the schema this function is in
      

     

     
      

       funcname name
      

      

       Name of this function
      

     

     
      

       calls bigint
      

      

       Number of times this function has been called
      

     

     
      

       total_time double precision
      

      

       Total time spent in this function and all other functions
       called by it, in milliseconds
      

     

     
      

       self_time double precision
      

      

       Total time spent in this function itself, not including
       other functions called by it, in milliseconds
      

     

     
      

        stats_reset timestamp with time zone
       

       

        Time at which these statistics were last reset
       

     
    
   
  

 

 

  
# pg_stat_slru

  
   SLRU
  

  
   pg_stat_slru
  

  

   PostgreSQL accesses certain on-disk information
   via SLRU (simple least-recently-used)
   caches.
   The pg_stat_slru view will contain
   one row for each tracked SLRU cache, showing statistics about access
   to cached pages.
  

  

   For each SLRU cache that's part of the core server,
   there is a configuration parameter that controls its size, with the suffix
   _buffers appended.
  

  
   
# pg_stat_slru View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       name text
      

      

       Name of the SLRU
      

     

     
      

       blks_zeroed bigint
      

      

       Number of blocks zeroed during initializations
      

     

     
      

       blks_hit bigint
      

      

       Number of times disk blocks were found already in the SLRU,
       so that a read was not necessary (this only includes hits in the
       SLRU, not the operating system's file system cache)
      

     

     
      

       blks_read bigint
      

      

       Number of disk blocks read for this SLRU
      

     

     
      

       blks_written bigint
      

      

       Number of disk blocks written for this SLRU
      

     

     
      

       blks_exists bigint
      

      

       Number of blocks checked for existence for this SLRU
      

     

     
      

       flushes bigint
      

      

       Number of flushes of dirty data for this SLRU
      

     

     
      

       truncates bigint
      

      

       Number of truncates for this SLRU
      

     

     
      

       stats_reset timestamp with time zone
      

      

       Time at which these statistics were last reset
      

     
    
   
  

 

 

  
# Statistics Functions

  

   Other ways of looking at the statistics can be set up by writing
   queries that use the same underlying statistics access functions used by
   the standard views shown above.  For details such as the functions' names,
   consult the definitions of the standard views.  (For example, in
   psql you could issue \d+ pg_stat_activity.)
   The access functions for per-database statistics take a database OID as an
   argument to identify which database to report on.
   The per-table and per-index functions take a table or index OID.
   The functions for per-function statistics take a function OID.
   Note that only tables, indexes, and functions in the current database
   can be seen with these functions.
  

  

   Additional functions related to the cumulative statistics system are listed
   in .
  

   
    
# Additional Statistics Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       
       

        pg_backend_pid ()
        integer
       

       

        Returns the process ID of the server process attached to the current
        session.
       

      

      
       

        
         pg_stat_get_backend_io
        
        pg_stat_get_backend_io ( integer )
        setof record
       

       

        Returns I/O statistics about the backend with the specified
        process ID. The output fields are exactly the same as the ones in the
        pg_stat_io view.
       

       

        The function does not return I/O statistics for the checkpointer,
        the background writer, the startup process and the autovacuum launcher
        as they are already visible in the pg_stat_io
        view and there is only one of each.
       

      

      
       

        
         pg_stat_get_activity
        
        pg_stat_get_activity ( integer )
        setof record
       

       

        Returns a record of information about the backend with the specified
        process ID, or one record for each active backend in the system
        if NULL is specified.  The fields returned are a
        subset of those in the pg_stat_activity view.
       

      

      
       

        
         pg_stat_get_backend_lock
        
        pg_stat_get_backend_lock ( integer )
        setof record
       

       

        Returns lock statistics about the backend with the specified
        process ID. The output fields are exactly the same as the ones in the
        pg_stat_lock view.
       

       

        The function does not return lock statistics for the checkpointer,
        the background writer, the startup process and the autovacuum launcher.
       

      

      
       

        
         pg_stat_get_backend_wal
        
        pg_stat_get_backend_wal ( integer )
        record
       

       

        Returns WAL statistics about the backend with the specified
        process ID. The output fields are exactly the same as the ones in the
        pg_stat_wal view.
       

       

        The function does not return WAL statistics for the checkpointer,
        the background writer, the startup process and the autovacuum launcher.
       

      

      
       

        
         pg_stat_get_snapshot_timestamp
        
        pg_stat_get_snapshot_timestamp ()
        timestamp with time zone
       

       

        Returns the timestamp of the current statistics snapshot, or NULL if
        no statistics snapshot has been taken. A snapshot is taken the first
        time cumulative statistics are accessed in a transaction if
        stats_fetch_consistency is set to
        snapshot
       

      

      
       

        
         pg_stat_reset_single_index_counters
        
        pg_stat_reset_single_index_counters ( oid )
        void
       

       

        Resets statistics for a single index in the current database
        or shared across all databases in the cluster to zero.
       

       

        This function is restricted to superusers by default, but other users
        can be granted EXECUTE to run the function.
       

      

      
       

        
         pg_stat_get_xact_blocks_fetched
        
        pg_stat_get_xact_blocks_fetched ( oid )
        bigint
       

       

        Returns the number of block read requests for table or index, in the
        current transaction. This number minus
        pg_stat_get_xact_blocks_hit gives the number of
        kernel read() calls; the number of actual
        physical reads is usually lower due to kernel-level buffering.
       

      

      
       

        
         pg_stat_get_xact_blocks_hit
        
        pg_stat_get_xact_blocks_hit ( oid )
        bigint
       

       

        Returns the number of block read requests for table or index, in the
        current transaction, found in cache (not triggering kernel
        read() calls).
       

      

      
       

        
         pg_stat_clear_snapshot
        
        pg_stat_clear_snapshot ()
        void
       

       

        Discards the current statistics snapshot or cached information.
       

      

      
       

        
         pg_stat_reset
        
        pg_stat_reset ()
        void
       

       

        Resets all statistics counters for the current database to zero.
       

       

        This function is restricted to superusers by default, but other users
        can be granted EXECUTE to run the function.
       

      

      
       

        
         pg_stat_reset_shared
        
        pg_stat_reset_shared ( [ 
target
 text DEFAULT NULL ] )
        void
       

       

        Resets some cluster-wide statistics counters to zero, depending on the
        argument. 
target
 can be:
       

        

         

          archiver: Reset all the counters shown in the
          pg_stat_archiver view.
         

        

        

         

           bgwriter: Reset all the counters shown in the
           pg_stat_bgwriter view.
         

        

        

         

          checkpointer: Reset all the counters shown in the
          pg_stat_checkpointer view.
         

        

        

         

          io: Reset all the counters shown in the
          pg_stat_io view.
         

        

        

         

          lock: Reset all the counters shown in the
          pg_stat_lock view.
         

        

        

         

          recovery_prefetch: Reset all the counters shown in
          the pg_stat_recovery_prefetch view.
         

        

        

         

          slru: Reset all the counters shown in the
          pg_stat_slru view.
         

        

        

         

          wal: Reset all the counters shown in the
          pg_stat_wal view.
         

        

        

         

          NULL or not specified: All the counters from the
          views listed above are reset.
         

        

       

       

       

        This function is restricted to superusers by default, but other users
        can be granted EXECUTE to run the function.
       

      

      
       

        
         pg_stat_reset_single_table_counters
        
        pg_stat_reset_single_table_counters ( oid )
        void
       

       

        Resets statistics for a single table in the current database
        or shared across all databases in the cluster to zero.
        It also resets statistics for a single sequence or materialized view
        in the current database.
       

       

        This function is restricted to superusers by default, but other users
        can be granted EXECUTE to run the function.
       

      

      
       

        
         pg_stat_reset_backend_stats
        
        pg_stat_reset_backend_stats ( integer )
        void
       

       

        Resets statistics for a single backend with the specified process ID
        to zero.
       

       

        This function is restricted to superusers by default, but other users
        can be granted EXECUTE to run the function.
       

      

      
       

        
         pg_stat_reset_single_function_counters
        
        pg_stat_reset_single_function_counters ( oid )
        void
       

       

        Resets statistics for a single function in the current database to
        zero.
       

       

        This function is restricted to superusers by default, but other users
        can be granted EXECUTE to run the function.
       

      

      
       

        
         pg_stat_reset_slru
        
        pg_stat_reset_slru ( [ 
target
 text DEFAULT NULL ] )
        void
       

       

        Resets statistics to zero for a single SLRU cache, or for all SLRUs in
        the cluster. If 
target
 is
        NULL or is not specified, all the counters shown in
        the pg_stat_slru view for all SLRU caches are
        reset. The argument can be one of
        commit_timestamp,
        multixact_member,
        multixact_offset,
        notify,
        serializable,
        subtransaction, or
        transaction
        to reset the counters for only that entry.
        If the argument is other (or indeed, any
        unrecognized name), then the counters for all other SLRU caches, such
        as extension-defined caches, are reset.
       

       

        This function is restricted to superusers by default, but other users
        can be granted EXECUTE to run the function.
       

      

      
       

        
          pg_stat_reset_replication_slot
        
        pg_stat_reset_replication_slot ( text )
        void
       

       

        Resets statistics of the replication slot defined by the argument. If
        the argument is NULL, resets statistics for all
        the replication slots.
       

       

         This function is restricted to superusers by default, but other users
         can be granted EXECUTE to run the function.
       

      

      
       

        
          pg_stat_reset_subscription_stats
        
        pg_stat_reset_subscription_stats ( oid )
        void
       

       

        Resets statistics for a single subscription shown in the
        pg_stat_subscription_stats view to zero. If
        the argument is NULL, reset statistics for all
        subscriptions.
       

       

        This function is restricted to superusers by default, but other users
        can be granted EXECUTE to run the function.
       

      
     
    
   

  
   

    Using pg_stat_reset() also resets counters that
    autovacuum uses to determine when to trigger a vacuum or an analyze.
    Resetting these counters can cause autovacuum to not perform necessary
    work, which can cause problems such as table bloat or out-dated
    table statistics.  A database-wide ANALYZE is
    recommended after the statistics have been reset.
   

  

  

   pg_stat_get_activity, the underlying function of
   the pg_stat_activity view, returns a set of records
   containing all the available information about each backend process.
   Sometimes it may be more convenient to obtain just a subset of this
   information.  In such cases, another set of per-backend statistics
   access functions can be used; these are shown in .
   These access functions use the session's backend ID number, which is a
   small integer (>= 0) that is distinct from the backend ID of any
   concurrent session, although a session's ID can be recycled as soon as
   it exits.  The backend ID is used, among other things, to identify the
   session's temporary schema if it has one.
   The function pg_stat_get_backend_idset provides a
   convenient way to list all the active backends' ID numbers for
   invoking these functions.  For example, to show the PIDs and
   current queries of all backends:

```
SELECT pg_stat_get_backend_pid(backendid) AS pid,
       pg_stat_get_backend_activity(backendid) AS query
FROM pg_stat_get_backend_idset() AS backendid;
```

  

   
    
# Per-Backend Statistics Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         pg_stat_get_backend_activity
        
        pg_stat_get_backend_activity ( integer )
        text
       

       

        Returns the text of this backend's most recent query.
       

      

      
       

        
         pg_stat_get_backend_activity_start
        
        pg_stat_get_backend_activity_start ( integer )
        timestamp with time zone
       

       

        Returns the time when the backend's most recent query was started.
       

      

      
       

        
         pg_stat_get_backend_client_addr
        
        pg_stat_get_backend_client_addr ( integer )
        inet
       

       

        Returns the IP address of the client connected to this backend.
       

      

      
       

        
         pg_stat_get_backend_client_port
        
        pg_stat_get_backend_client_port ( integer )
        integer
       

       

        Returns the TCP port number that the client is using for communication.
       

      

      
       

        
         pg_stat_get_backend_dbid
        
        pg_stat_get_backend_dbid ( integer )
        oid
       

       

        Returns the OID of the database this backend is connected to.
       

      

      
       

        
         pg_stat_get_backend_idset
        
        pg_stat_get_backend_idset ()
        setof integer
       

       

        Returns the set of currently active backend ID numbers.
       

      

      
       

        
         pg_stat_get_backend_pid
        
        pg_stat_get_backend_pid ( integer )
        integer
       

       

        Returns the process ID of this backend.
       

      

      
       

        
         pg_stat_get_backend_start
        
        pg_stat_get_backend_start ( integer )
        timestamp with time zone
       

       

        Returns the time when this process was started.
       

      

      
       

        
         pg_stat_get_backend_subxact
        
        pg_stat_get_backend_subxact ( integer )
        record
       

       

        Returns a record of information about the subtransactions of the
        backend with the specified ID.
        The fields returned are 
subxact_count
, which
        is the number of subtransactions in the backend's subtransaction cache,
        and 
subxact_overflow
, which indicates whether
        the backend's subtransaction cache is overflowed or not.
       

      

      
       

        
         pg_stat_get_backend_userid
        
        pg_stat_get_backend_userid ( integer )
        oid
       

       

        Returns the OID of the user logged into this backend.
       

      

      
       

        
         pg_stat_get_backend_wait_event
        
        pg_stat_get_backend_wait_event ( integer )
        text
       

       

        Returns the wait event name if this backend is currently waiting,
        otherwise NULL. See  through
        .
       

      

      
       

        
         pg_stat_get_backend_wait_event_type
        
        pg_stat_get_backend_wait_event_type ( integer )
        text
       

       

        Returns the wait event type name if this backend is currently waiting,
        otherwise NULL.  See  for details.
       

      

      
       

        
         pg_stat_get_backend_xact_start
        
        pg_stat_get_backend_xact_start ( integer )
        timestamp with time zone
       

       

        Returns the time when the backend's current transaction was started.
       

      
     
    
   

 

 

 

  
# Viewing Locks

  
   lock
   monitoring
  

  

   Another useful tool for monitoring database activity is the
   pg_locks system table.  It allows the
   database administrator to view information about the outstanding
   locks in the lock manager. For example, this capability can be used
   to:

   

    

     

      View all the locks currently outstanding, all the locks on
      relations in a particular database, all the locks on a
      particular relation, or all the locks held by a particular
      PostgreSQL session.
     

    

    

     

      Determine the relation in the current database with the most
      ungranted locks (which might be a source of contention among
      database clients).
     

    

    

     

      Determine the effect of lock contention on overall database
      performance, as well as the extent to which contention varies
      with overall database traffic.
     

    

   

   Details of the pg_locks view appear in
   .
   For more information on locking and managing concurrency with
   PostgreSQL, refer to .
  

 

 

  
# Progress Reporting

  

   PostgreSQL has the ability to report the progress of
   certain commands during command execution.  Currently, the only commands
   which support progress reporting are ANALYZE,
   COPY, CREATE INDEX,
   REPACK (and its obsolete spelling CLUSTER),
   VACUUM,
    (i.e., replication
   command that  issues to take
   a base backup), and online data checksum operations.
   This may be expanded in the future.
  

 

  
# ANALYZE Progress Reporting

  
   pg_stat_progress_analyze
  

  

   Whenever ANALYZE is running, the
   pg_stat_progress_analyze view will contain a
   row for each backend that is currently running that command.  The tables
   below describe the information that will be reported and provide
   information about how to interpret it.
  

  
   
# pg_stat_progress_analyze View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of backend.
      

     

     
      

       datid oid
      

      

       OID of the database to which this backend is connected.
      

     

     
      

       datname name
      

      

       Name of the database to which this backend is connected.
      

     

     
      

       relid oid
      

      

       OID of the table being analyzed.
      

     

     
      

       phase text
      

      

       Current processing phase. See .
      

     

     
      

       sample_blks_total bigint
      

      

       Total number of heap blocks that will be sampled.
      

     

     
      

       sample_blks_scanned bigint
      

      

       Number of heap blocks scanned.
      

     

     
      

       ext_stats_total bigint
      

      

       Number of extended statistics.
      

     

     
      

       ext_stats_computed bigint
      

      

       Number of extended statistics computed. This counter only advances
       when the phase is computing extended statistics.
      

     

     
      

       child_tables_total bigint
      

      

       Number of child tables.
      

     

     
      

       child_tables_done bigint
      

      

       Number of child tables scanned. This counter only advances when the
       phase is acquiring inherited sample rows.
      

     

     
      

       current_child_table_relid oid
      

      

       OID of the child table currently being scanned. This field is
       only valid when the phase is
       acquiring inherited sample rows.
      

     

     
      

       delay_time double precision
      

      

       Total time spent sleeping due to cost-based delay (see
       ), in milliseconds
       (if  is enabled, otherwise
       zero).
      

     

     
      

       started_by text
      

      

       Shows what caused the current ANALYZE operation to be
       started. Possible values are:
       

        

         

          manual: The analyze was started by an explicit
          ANALYZE, or by VACUUM with
          the ANALYZE option.
         

        

        

         

          autovacuum: The analyze was started by an
          autovacuum worker.
         

        

       

      

     
    
   
  

  
   
# ANALYZE Phases

   
    
    
    
     
      Phase
      Description
     
    
    
     
      initializing
      
       The command is preparing to begin scanning the heap.  This phase is
       expected to be very brief.
      
     
     
      acquiring sample rows
      
       The command is currently scanning the table given by
       relid to obtain sample rows.
      
     
     
      acquiring inherited sample rows
      
       The command is currently scanning child tables to obtain sample rows.
       Columns child_tables_total,
       child_tables_done, and
       current_child_table_relid contain the
       progress information for this phase.
      
     
     
      computing statistics
      
       The command is computing statistics from the sample rows obtained
       during the table scan.
      
     
     
      computing extended statistics
      
       The command is computing extended statistics from the sample rows
       obtained during the table scan.
      
     
     
      finalizing analyze
      
       The command is updating pg_class. When this
       phase is completed, ANALYZE will end.
      
     
    
   
  

  
   

    Note that when ANALYZE is run on a partitioned table
    without the ONLY keyword, all of its partitions are
    also recursively analyzed.  In that case, ANALYZE
    progress is reported first for the parent table, whereby its inheritance
    statistics are collected, followed by that for each partition.
   

  
 

 

  
# CLUSTER Progress Reporting

  
   pg_stat_progress_cluster
  

  

   Whenever REPACK, CLUSTER or
   VACUUM FULL is running,
   the backwards-compatibility pg_stat_progress_cluster
   view will
   contain a row for each backend that is currently running either command.
   The tables below describe the information that will be reported and
   provide information about how to interpret it.
  

  
   
# pg_stat_progress_cluster View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of backend.
      

     

     
      

       datid oid
      

      

       OID of the database to which this backend is connected.
      

     

     
      

       datname name
      

      

       Name of the database to which this backend is connected.
      

     

     
      

       relid oid
      

      

       OID of the table being clustered.
      

     

     
      

       command text
      

      

       The command that is running. Either CLUSTER or
       VACUUM FULL.
       Because this view exists for backwards-compatibility purposes only,
       it will translate any REPACK command into one of
       these other two.
      

     

     
      

       phase text
      

      

       Current processing phase. See .
      

     

     
      

       cluster_index_relid oid
      

      

       If the table is being scanned using an index, this is the OID of the
       index being used; otherwise, it is zero.
      

     

     
      

       heap_tuples_scanned bigint
      

      

       Number of heap tuples scanned.
       This counter only advances when the phase is
       seq scanning heap,
       index scanning heap
       or writing new heap.
      

     

     
      

       heap_tuples_written bigint
      

      

       Number of heap tuples written.
       This counter only advances when the phase is
       seq scanning heap,
       index scanning heap
       or writing new heap.
      

     

     
      

       heap_blks_total bigint
      

      

       Total number of heap blocks in the table.  This number is reported
       as of the beginning of seq scanning heap.
      

     

     
      

       heap_blks_scanned bigint
      

      

       Number of heap blocks scanned.  This counter only advances when the
       phase is seq scanning heap.
      

     

     
      

       index_rebuild_count bigint
      

      

       Number of indexes rebuilt.  This counter only advances when the phase
       is rebuilding index.
      

     
    
   
  

  
   
# CLUSTER and VACUUM FULL Phases

   
    
    
    
    
      Phase
      Description
     
    

   
    
     initializing
     
       The command is preparing to begin scanning the heap.  This phase is
       expected to be very brief.
     
    
    
     seq scanning heap
     
       The command is currently scanning the table using a sequential scan.
     
    
    
     index scanning heap
     
       CLUSTER is currently scanning the table using an index scan.
     
    
    
     sorting tuples
     
       CLUSTER is currently sorting tuples.
     
    
    
     writing new heap
     
       CLUSTER is currently writing the new heap.
     
    
    
     swapping relation files
     
       The command is currently swapping newly-built files into place.
     
    
    
     rebuilding index
     
       The command is currently rebuilding an index.
     
    
    
     performing final cleanup
     
       The command is performing final cleanup.  When this phase is
       completed, CLUSTER
       or VACUUM FULL will end.
     
    
   
   
  
 

 

  
# COPY Progress Reporting

  
   pg_stat_progress_copy
  

  

   Whenever COPY is running, the
   pg_stat_progress_copy view will contain one row
   for each backend that is currently running a COPY command.
   The table below describes the information that will be reported and provides
   information about how to interpret it.
  

  
   
# pg_stat_progress_copy View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of backend.
      

     

     
      

       datid oid
      

      

       OID of the database to which this backend is connected.
      

     

     
      

       datname name
      

      

       Name of the database to which this backend is connected.
      

     

     
      

       relid oid
      

      

       OID of the table on which the COPY command is
       executed. It is set to 0 if copying from a
       SELECT query.
      

     

     
      

       command text
      

      

       The command that is running: COPY FROM, or
       COPY TO.
      

     

     
      

       type text
      

      

       The I/O type that the data is read from or written to:
       FILE, PROGRAM,
       PIPE (for COPY FROM STDIN and
       COPY TO STDOUT), or CALLBACK
       (used for example during the initial table synchronization in
       logical replication).
      

     

     
      

       bytes_processed bigint
      

      

       Number of bytes already processed by COPY command.
      

     

     
      

       bytes_total bigint
      

      

       Size of source file for COPY FROM command in bytes.
       It is set to 0 if not available.
      

     

     
      

       tuples_processed bigint
      

      

       Number of tuples already processed by COPY command.
      

     

     
      

       tuples_excluded bigint
      

      

       Number of tuples not processed because they were excluded by the
       WHERE clause of the COPY command.
      

     

     
      

       tuples_skipped bigint
      

      

       Number of tuples skipped because they contain malformed data.
       This counter only advances when
       ignore is specified to the ON_ERROR
       option.
      

     
    
   
  
 

 

  
# CREATE INDEX Progress Reporting

  
   pg_stat_progress_create_index
  

  

   Whenever CREATE INDEX or REINDEX is running, the
   pg_stat_progress_create_index view will contain
   one row for each backend that is currently creating indexes.  The tables
   below describe the information that will be reported and provide information
   about how to interpret it.
  

  
   
# pg_stat_progress_create_index View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of the backend creating indexes.
      

     

     
      

       datid oid
      

      

       OID of the database to which this backend is connected.
      

     

     
      

       datname name
      

      

       Name of the database to which this backend is connected.
      

     

     
      

       relid oid
      

      

       OID of the table on which the index is being created.
      

     

     
      

       index_relid oid
      

      

       OID of the index being created or reindexed.  During a
       non-concurrent CREATE INDEX, this is 0.
      

     

     
      

       command text
      

      

       Specific command type: CREATE INDEX,
       CREATE INDEX CONCURRENTLY,
       REINDEX, or REINDEX CONCURRENTLY.
      

     

     
      

       phase text
      

      

       Current processing phase of index creation.  See .
      

     

     
      

       lockers_total bigint
      

      

       Total number of lockers to wait for, when applicable.
      

     

     
      

       lockers_done bigint
      

      

       Number of lockers already waited for.
      

     

     
      

       current_locker_pid bigint
      

      

       Process ID of the locker currently being waited for.
      

     

     
      

       blocks_total bigint
      

      

       Total number of blocks to be processed in the current phase.
      

     

     
      

       blocks_done bigint
      

      

       Number of blocks already processed in the current phase.
      

     

     
      

       tuples_total bigint
      

      

       Total number of tuples to be processed in the current phase.
      

     

     
      

       tuples_done bigint
      

      

       Number of tuples already processed in the current phase.
      

     

     
      

       partitions_total bigint
      

      

       Total number of partitions on which the index is to be created
       or attached, including both direct and indirect partitions.
       0 during a REINDEX, or when
       the index is not partitioned.
      

     

     
      

       partitions_done bigint
      

      

       Number of partitions on which the index has already been created
       or attached, including both direct and indirect partitions.
       0 during a REINDEX, or when
       the index is not partitioned.
      

     
    
   
  

  
   
# CREATE INDEX Phases

   
    
    
    
     
      Phase
      Description
     
    
    
     
      initializing
      
       CREATE INDEX or REINDEX is preparing to create the index.  This
       phase is expected to be very brief.
      
     
     
      waiting for writers before build
      
       CREATE INDEX CONCURRENTLY or REINDEX CONCURRENTLY is waiting for transactions
       with write locks that can potentially see the table to finish.
       This phase is skipped when not in concurrent mode.
       Columns lockers_total, lockers_done
       and current_locker_pid contain the progress
       information for this phase.
      
     
     
      building index
      
       The index is being built by the access method-specific code.  In this phase,
       access methods that support progress reporting fill in their own progress data,
       and the subphase is indicated in this column.  Typically,
       blocks_total and blocks_done
       will contain progress data, as well as potentially
       tuples_total and tuples_done.
      
     
     
      waiting for writers before validation
      
       CREATE INDEX CONCURRENTLY or REINDEX CONCURRENTLY is waiting for transactions
       with write locks that can potentially write into the table to finish.
       This phase is skipped when not in concurrent mode.
       Columns lockers_total, lockers_done
       and current_locker_pid contain the progress
       information for this phase.
      
     
     
      index validation: scanning index
      
       CREATE INDEX CONCURRENTLY is scanning the index searching
       for tuples that need to be validated.
       This phase is skipped when not in concurrent mode.
       Columns blocks_total (set to the total size of the index)
       and blocks_done contain the progress information for this phase.
      
     
     
      index validation: sorting tuples
      
       CREATE INDEX CONCURRENTLY is sorting the output of the
       index scanning phase.
      
     
     
      index validation: scanning table
      
       CREATE INDEX CONCURRENTLY is scanning the table
       to validate the index tuples collected in the previous two phases.
       This phase is skipped when not in concurrent mode.
       Columns blocks_total (set to the total size of the table)
       and blocks_done contain the progress information for this phase.
      
     
     
      waiting for old snapshots
      
       CREATE INDEX CONCURRENTLY or REINDEX CONCURRENTLY is waiting for transactions
       that can potentially see the table to release their snapshots.  This
       phase is skipped when not in concurrent mode.
       Columns lockers_total, lockers_done
       and current_locker_pid contain the progress
       information for this phase.
      
     
     
      waiting for readers before marking dead
      
       REINDEX CONCURRENTLY is waiting for transactions
       with read locks on the table to finish, before marking the old index dead.
       This phase is skipped when not in concurrent mode.
       Columns lockers_total, lockers_done
       and current_locker_pid contain the progress
       information for this phase.
      
     
     
      waiting for readers before dropping
      
       REINDEX CONCURRENTLY is waiting for transactions
       with read locks on the table to finish, before dropping the old index.
       This phase is skipped when not in concurrent mode.
       Columns lockers_total, lockers_done
       and current_locker_pid contain the progress
       information for this phase.
      
     
    
   
  

 

 

  
# REPACK Progress Reporting

  
   pg_stat_progress_repack
  

  

   Whenever REPACK is running,
   the pg_stat_progress_repack view will contain a
   row for each backend that is currently running the command.  The tables
   below describe the information that will be reported and provide
   information about how to interpret it.
  

  
   
# pg_stat_progress_repack View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of backend.
      

     

     
      

       datid oid
      

      

       OID of the database to which this backend is connected.
      

     

     
      

       datname name
      

      

       Name of the database to which this backend is connected.
      

     

     
      

       relid oid
      

      

       OID of the table being repacked.
      

     

     
      

       command text
      

      

       The command that is running. Either REPACK or
       VACUUM FULL, or CLUSTER.
      

     

     
      

       phase text
      

      

       Current processing phase. See .
      

     

     
      

       repack_index_relid oid
      

      

       If the table is being scanned using an index, this is the OID of the
       index being used; otherwise, it is zero.
      

     

     
      

       heap_tuples_scanned bigint
      

      

       Number of heap tuples scanned.
       This counter only advances when the phase is
       seq scanning heap,
       index scanning heap
       or writing new heap.
      

     

     
      

       heap_tuples_inserted bigint
      

      

       Number of heap tuples inserted.
       This counter only advances when the phase is
       seq scanning heap,
       index scanning heap,
       writing new heap
       or catch-up.
      

     

     
      

       heap_tuples_updated bigint
      

      

       Number of heap tuples updated.
       This counter only advances when the phase is catch-up.
      

     

     
      

       heap_tuples_deleted bigint
      

      

       Number of heap tuples deleted.
       This counter only advances when the phase is catch-up.
      

     

     
      

       heap_blks_total bigint
      

      

       Total number of heap blocks in the table.  This number is reported
       as of the beginning of seq scanning heap.
      

     

     
      

       heap_blks_scanned bigint
      

      

       Number of heap blocks scanned.  This counter only advances when the
       phase is seq scanning heap.
      

     

     
      

       index_rebuild_count bigint
      

      

       Number of indexes rebuilt.  This counter only advances when the phase
       is rebuilding index.
      

     
    
   
  

  
   
# REPACK Phases

   
    
    
    
    
      Phase
      Description
     
    

   
    
     initializing
     
       The command is preparing to begin scanning the heap.  This phase is
       expected to be very brief.
     
    
    
     seq scanning heap
     
       The command is currently scanning the table using a sequential scan.
     
    
    
     index scanning heap
     
       REPACK is currently scanning the table using an index scan.
     
    
    
     sorting tuples
     
       REPACK is currently sorting tuples.
     
    
    
     writing new heap
     
       REPACK is currently writing the new heap.
     
    
    
     catch-up
     
       REPACK CONCURRENTLY is currently processing the DML
       commands that other transactions executed during any of the preceding
       phases.
     
    
    
     swapping relation files
     
       The command is currently swapping newly-built files into place.
     
    
    
     rebuilding index
     
       The command is currently rebuilding an index.
     
    
    
     performing final cleanup
     
       The command is performing final cleanup.  When this phase is
       completed, REPACK will end.
     
    
   
   
  
 

 

  
# VACUUM Progress Reporting

  
   pg_stat_progress_vacuum
  

  

   Whenever VACUUM is running, the
   pg_stat_progress_vacuum view will contain
   one row for each backend (including autovacuum worker processes) that is
   currently vacuuming.  The tables below describe the information
   that will be reported and provide information about how to interpret it.
   Progress for VACUUM FULL commands is reported via
   pg_stat_progress_cluster
   because both VACUUM FULL and CLUSTER
   rewrite the table, while regular VACUUM only modifies it
   in place. See .
  

  
   
# pg_stat_progress_vacuum View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of backend.
      

     

     
      

       datid oid
      

      

       OID of the database to which this backend is connected.
      

     

     
      

       datname name
      

      

       Name of the database to which this backend is connected.
      

     

     
      

       relid oid
      

      

       OID of the table being vacuumed.
      

     

     
      

       phase text
      

      

       Current processing phase of vacuum.  See .
      

     

     
      

       heap_blks_total bigint
      

      

       Total number of heap blocks in the table.  This number is reported
       as of the beginning of the scan; blocks added later will not be (and
       need not be) visited by this VACUUM.
      

     

     
      

       heap_blks_scanned bigint
      

      

       Number of heap blocks scanned.  Because the
       visibility map is used to optimize scans,
       some blocks will be skipped without inspection; skipped blocks are
       included in this total, so that this number will eventually become
       equal to heap_blks_total when the vacuum is complete.
       This counter only advances when the phase is scanning heap.
      

     

     
      

       heap_blks_vacuumed bigint
      

      

       Number of heap blocks vacuumed.  Unless the table has no indexes, this
       counter only advances when the phase is vacuuming heap.
       Blocks that contain no dead tuples are skipped, so the counter may
       sometimes skip forward in large increments.
      

     

     
      

       index_vacuum_count bigint
      

      

       Number of completed index vacuum cycles.
      

     

     
      

       max_dead_tuple_bytes bigint
      

      

       Amount of dead tuple data that we can store before needing to perform
       an index vacuum cycle, based on
       .
      

     

     
      

       dead_tuple_bytes bigint
      

      

       Amount of dead tuple data collected since the last index vacuum cycle.
      

     

     
      

      num_dead_item_ids bigint
      

      

       Number of dead item identifiers collected since the last index vacuum cycle.
      

     

     
      

       indexes_total bigint
      

      

       Total number of indexes that will be vacuumed or cleaned up. This
       number is reported at the beginning of the
       vacuuming indexes phase or the
       cleaning up indexes phase.
      

     

     
      

       indexes_processed bigint
      

      

       Number of indexes processed. This counter only advances when the
       phase is vacuuming indexes or
       cleaning up indexes.
      

     

     
      

       delay_time double precision
      

      

       Total time spent sleeping due to cost-based delay (see
       ), in milliseconds
       (if  is enabled, otherwise
       zero).  This includes the time that any associated parallel workers have
       slept.  However, parallel workers report their sleep time no more
       frequently than once per second, so the reported value may be slightly
       stale.
      

     

     
      

       mode text
      

      

       The mode in which the current VACUUM operation is
       running. See  for details of each
       mode. Possible values are:
       

        

         

          normal: The operation is performing a standard
          vacuum. It is neither required to run in aggressive mode nor operating
          in failsafe mode.
         

        

        

         

          aggressive: The operation is running an aggressive
          vacuum, which must scan every page that is not marked all-frozen.
          The parameters  and
           determine when a
          table requires aggressive vacuuming.
         

        

        

         

          failsafe: The vacuum has entered failsafe mode,
          in which it performs only the minimum work necessary to avoid
          transaction ID or multixact ID wraparound failure.
          The parameters  and
           determine when the
          vacuum enters failsafe mode. The vacuum may start in this mode or
          switch to it while running; the value of the
          mode column may transition from another
          mode to failsafe during the operation.
         

        

       

      

     

     
      

       started_by text
      

      

       Shows what caused the current VACUUM operation to be
       started. Possible values are:
       

        

         

          manual: The vacuum was started by an explicit
          VACUUM command.
         

        

        

         

          autovacuum: The vacuum was started by an autovacuum
          worker. Vacuums run by autovacuum workers may be interrupted due to
          lock conflicts.
         

        

        

         

          autovacuum_wraparound: The vacuum was started by an
          autovacuum worker to prevent transaction ID or multixact ID
          wraparound. Vacuums run for wraparound protection are not interrupted
          due to lock conflicts.
         

        

       

      

     
    
   
  

  
   
# VACUUM Phases

   
    
    
    
    
      Phase
      Description
     
    

   
    
     initializing
     
       VACUUM is preparing to begin scanning the heap.  This
       phase is expected to be very brief.
     
    
    
     scanning heap
     
       VACUUM is currently scanning the heap.  It will prune and
       defragment each page if required, and possibly perform freezing
       activity.  The heap_blks_scanned column can be used
       to monitor the progress of the scan.
     
    
    
     vacuuming indexes
     
       VACUUM is currently vacuuming the indexes.  If a table has
       any indexes, this will happen at least once per vacuum, after the heap
       has been completely scanned.  It may happen multiple times per vacuum
       if  (or, in the case of autovacuum,
        if set) is insufficient to store
       the number of dead tuples found.
     
    
    
     vacuuming heap
     
       VACUUM is currently vacuuming the heap.  Vacuuming the heap
       is distinct from scanning the heap, and occurs after each instance of
       vacuuming indexes.  If heap_blks_scanned is less than
       heap_blks_total, the system will return to scanning
       the heap after this phase is completed; otherwise, it will begin
       cleaning up indexes after this phase is completed.
     
    
    
     cleaning up indexes
     
       VACUUM is currently cleaning up indexes.  This occurs after
       the heap has been completely scanned and all vacuuming of the indexes
       and the heap has been completed.
     
    
    
     truncating heap
     
       VACUUM is currently truncating the heap so as to return
       empty pages at the end of the relation to the operating system.  This
       occurs after cleaning up indexes.
     
    
    
     performing final cleanup
     
       VACUUM is performing final cleanup.  During this phase,
       VACUUM will vacuum the free space map, update statistics
       in pg_class, and report statistics to the cumulative
       statistics system. When this phase is completed, VACUUM will end.
     
    
   
   
  
 

 

  
# Base Backup Progress Reporting

  
   pg_stat_progress_basebackup
  

  

   Whenever an application like pg_basebackup
   is taking a base backup, the
   pg_stat_progress_basebackup
   view will contain a row for each WAL sender process that is currently
   running the BASE_BACKUP replication command
   and streaming the backup. The tables below describe the information
   that will be reported and provide information about how to interpret it.
  

  
   
# pg_stat_progress_basebackup View

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       pid integer
      

      

       Process ID of a WAL sender process.
      

     

     
      

       phase text
      

      

       Current processing phase. See .
      

     

     
      

       backup_total bigint
      

      

       Total amount of data that will be streamed. This is estimated and
       reported as of the beginning of
       streaming database files phase. Note that
       this is only an approximation since the database
       may change during streaming database files phase
       and WAL log may be included in the backup later. This is always
       the same value as backup_streamed
       once the amount of data streamed exceeds the estimated
       total size. If the estimation is disabled in
       pg_basebackup
       (i.e., --no-estimate-size option is specified),
       this is NULL.
      

     

     
      

       backup_streamed bigint
      

      

       Amount of data streamed. This counter only advances
       when the phase is streaming database files or
       transferring wal files.
      

     

     
      

       tablespaces_total bigint
      

      

       Total number of tablespaces that will be streamed.
      

     

     
      

       tablespaces_streamed bigint
      

      

       Number of tablespaces streamed. This counter only
       advances when the phase is streaming database files.
      

     

     
      

       backup_type text
      

      

        Backup type. Either full or
        incremental.
      

     
    
   
  

  
   
# Base Backup Phases

   
    
    
    
     
      Phase
      Description
     
    
    
     
      initializing
      
       The WAL sender process is preparing to begin the backup.
       This phase is expected to be very brief.
      
     
     
      waiting for checkpoint to finish
      
       The WAL sender process is currently performing
       pg_backup_start to prepare to
       take a base backup, and waiting for the start-of-backup
       checkpoint to finish.
      
     
     
      estimating backup size
      
       The WAL sender process is currently estimating the total amount
       of database files that will be streamed as a base backup.
      
     
     
      streaming database files
      
       The WAL sender process is currently streaming database files
       as a base backup.
      
     
     
      waiting for wal archiving to finish
      
       The WAL sender process is currently performing
       pg_backup_stop to finish the backup,
       and waiting for all the WAL files required for the base backup
       to be successfully archived.
       If either --wal-method=none or
       --wal-method=stream is specified in
       pg_basebackup, the backup will end
       when this phase is completed.
      
     
     
      transferring wal files
      
       The WAL sender process is currently transferring all WAL logs
       generated during the backup. This phase occurs after
       waiting for wal archiving to finish phase if
       --wal-method=fetch is specified in
       pg_basebackup. The backup will end
       when this phase is completed.
      
     
    
   
  

 

 

  
# Data Checksum Progress Reporting

  
   pg_stat_progress_data_checksums
  

  

   When data checksums are being enabled or disabled on a running cluster, the
   pg_stat_progress_data_checksums view will contain
   a row for the launcher process.  When enabling data checksums, the view
   will also contain one row for each worker process which is currently
   calculating and writing checksums for the data pages in a database.  The
   launcher provides an overview of the overall progress, such as how many
   databases have been processed and how many remain, while the workers track
   progress for currently processed databases.
  

  
   
# pg_stat_progress_data_checksums View

   
    
     
      
       

        Column Type
       

       

        Description
       

      
     
    

    
     
      
       

        pid integer
       

       

        Process ID of the data checksum process, launcher or worker.
       

      
     

     
      
       

        datid oid
       

       

        OID of this database, or 0 for the launcher process.
       

      
     

     
      
       

        datname name
       

       

        Name of this database, or NULL for the
        launcher process.
       

      
     

     
      
       

        phase text
       

       

        Current processing phase, see 
        for description of the phases.
       

      
     

     
      
       

        databases_total bigint
       

       

        The total number of databases which will be processed. Only the
        launcher process has this value set when enabling data checksums;
        otherwise this is set to NULL.
       

      
     

     
      
       

        databases_done bigint
       

       

        The number of databases which have been processed. Only the launcher
        process has this value set when enabling data checksums; otherwise
        this is set to NULL.
       

      
     

     
      
       

        relations_total bigint
       

       

        The total number of relations which will be processed, or
        NULL if the worker process hasn't
        calculated the number of relations yet. The launcher process has
        this set to NULL since it isn't responsible for
        processing relations, only launching worker processes.
       

      
     

     
      
       

        relations_done bigint
       

       

        The number of relations which have been processed. The launcher
        process has this set to NULL.
       

      
     

     
      
       

        blocks_total bigint
       

       

        The number of blocks in the current relation which will be processed,
        or NULL if the worker process hasn't
        calculated the number of blocks yet. The launcher process has
        this set to NULL.
       

      
     

     
      
       

        blocks_done bigint
       

       

        The number of blocks in the current relation which have been processed.
        The launcher process has this set to NULL.
       

      
     

    
   
  

  
   
# Data Checksum Phases

   
    
    
    
     
      Phase
      Description
     
    
    
     
      enabling
      
       The command is currently enabling data checksums on the cluster.
      
     
     
      disabling
      
       The command is currently disabling data checksums on the cluster.
      
     
     
      done
      
       The command is done and the data checksum state in the cluster has
       changed.
      
     
     
      waiting on barrier
      
       The command is currently waiting for the current active backends to
       acknowledge the change in data checksum state.
      
     
     
      waiting on temporary tables
      
       The command is currently waiting for all temporary tables which existed
       at the time the command was started to be removed.
      
     
    
   
  
 

 

 

  
# Dynamic Tracing

 
  DTrace
 

  

   PostgreSQL provides facilities to support
   dynamic tracing of the database server. This allows an external
   utility to be called at specific points in the code and thereby trace
   execution.
  

  

   A number of probes or trace points are already inserted into the source
   code. These probes are intended to be used by database developers and
   administrators. By default the probes are not compiled into
   PostgreSQL; the user needs to explicitly tell
   the configure script to make the probes available.
  

  

   Currently, the
   DTrace
   utility is supported, which, at the time of this writing, is available
   on Solaris, macOS, FreeBSD, NetBSD, and Oracle Linux.  The
   SystemTap project
   for Linux provides a DTrace equivalent and can also be used.  Supporting other dynamic
   tracing utilities is theoretically possible by changing the definitions for
   the macros in src/include/utils/probes.h.
  

  

   
# Compiling for Dynamic Tracing

  

   By default, probes are not available, so you will need to
   explicitly tell the configure script to make the probes available
   in PostgreSQL. To include DTrace support
   specify --enable-dtrace to configure.  See  for further information.
  

  

  

   
# Built-in Probes

  

   A number of standard probes are provided in the source code,
   as shown in ;
   
   shows the types used in the probes.  More probes can certainly be
   added to enhance PostgreSQL's observability.
  

 
  
# Built-in DTrace Probes

  
   
   
   
   
    
     Name
     Parameters
     Description
    
   

   

    
     transaction-start
     (LocalTransactionId)
     Probe that fires at the start of a new transaction.
      arg0 is the transaction ID.
    
    
     transaction-commit
     (LocalTransactionId)
     Probe that fires when a transaction completes successfully.
      arg0 is the transaction ID.
    
    
     transaction-abort
     (LocalTransactionId)
     Probe that fires when a transaction completes unsuccessfully.
      arg0 is the transaction ID.
    
    
     query-start
     (const char *)
     Probe that fires when the processing of a query is started.
      arg0 is the query string.
    
    
     query-done
     (const char *)
     Probe that fires when the processing of a query is complete.
      arg0 is the query string.
    
    
     query-parse-start
     (const char *)
     Probe that fires when the parsing of a query is started.
      arg0 is the query string.
    
    
     query-parse-done
     (const char *)
     Probe that fires when the parsing of a query is complete.
      arg0 is the query string.
    
    
     query-rewrite-start
     (const char *)
     Probe that fires when the rewriting of a query is started.
      arg0 is the query string.
    
    
     query-rewrite-done
     (const char *)
     Probe that fires when the rewriting of a query is complete.
      arg0 is the query string.
    
    
     query-plan-start
     ()
     Probe that fires when the planning of a query is started.
    
    
     query-plan-done
     ()
     Probe that fires when the planning of a query is complete.
    
    
     query-execute-start
     ()
     Probe that fires when the execution of a query is started.
    
    
     query-execute-done
     ()
     Probe that fires when the execution of a query is complete.
    
    
     statement-status
     (const char *)
     Probe that fires anytime the server process updates its
      pg_stat_activity.status.
      arg0 is the new status string.
    
    
     checkpoint-start
     (int)
     Probe that fires when a checkpoint is started.
      arg0 holds the bitwise flags used to distinguish different checkpoint
      types, such as shutdown, immediate or force.
    
    
     checkpoint-done
     (int, int, int, int, int)
     Probe that fires when a checkpoint is complete.
      (The probes listed next fire in sequence during checkpoint processing.)
      arg0 is the number of buffers written. arg1 is the total number of
      buffers. arg2, arg3 and arg4 contain the number of WAL files added,
      removed and recycled respectively.
    
    
     clog-checkpoint-start
     (bool)
     Probe that fires when the CLOG portion of a checkpoint is started.
      arg0 is true for normal checkpoint, false for shutdown
      checkpoint.
    
    
     clog-checkpoint-done
     (bool)
     Probe that fires when the CLOG portion of a checkpoint is
      complete. arg0 has the same meaning as for clog-checkpoint-start.
    
    
     subtrans-checkpoint-start
     (bool)
     Probe that fires when the SUBTRANS portion of a checkpoint is
      started.
      arg0 is true for normal checkpoint, false for shutdown
      checkpoint.
    
    
     subtrans-checkpoint-done
     (bool)
     Probe that fires when the SUBTRANS portion of a checkpoint is
      complete. arg0 has the same meaning as for
      subtrans-checkpoint-start.
    
    
     multixact-checkpoint-start
     (bool)
     Probe that fires when the MultiXact portion of a checkpoint is
      started.
      arg0 is true for normal checkpoint, false for shutdown
      checkpoint.
    
    
     multixact-checkpoint-done
     (bool)
     Probe that fires when the MultiXact portion of a checkpoint is
      complete. arg0 has the same meaning as for
      multixact-checkpoint-start.
    
    
     buffer-checkpoint-start
     (int)
     Probe that fires when the buffer-writing portion of a checkpoint
      is started.
      arg0 holds the bitwise flags used to distinguish different checkpoint
      types, such as shutdown, immediate or force.
    
    
     buffer-sync-start
     (int, int)
     Probe that fires when we begin to write dirty buffers during
      checkpoint (after identifying which buffers must be written).
      arg0 is the total number of buffers.
      arg1 is the number that are currently dirty and need to be written.
    
    
     buffer-sync-written
     (int)
     Probe that fires after each buffer is written during checkpoint.
      arg0 is the ID number of the buffer.
    
    
     buffer-sync-done
     (int, int, int)
     Probe that fires when all dirty buffers have been written.
      arg0 is the total number of buffers.
      arg1 is the number of buffers actually written by the checkpoint process.
      arg2 is the number that were expected to be written (arg1 of
      buffer-sync-start); any difference reflects other processes flushing
      buffers during the checkpoint.
    
    
     buffer-checkpoint-sync-start
     ()
     Probe that fires after dirty buffers have been written to the
      kernel, and before starting to issue fsync requests.
    
    
     buffer-checkpoint-done
     ()
     Probe that fires when syncing of buffers to disk is
      complete.
    
    
     twophase-checkpoint-start
     ()
     Probe that fires when the two-phase portion of a checkpoint is
      started.
    
    
     twophase-checkpoint-done
     ()
     Probe that fires when the two-phase portion of a checkpoint is
      complete.
    
    
     buffer-extend-start
     (ForkNumber, BlockNumber, Oid, Oid, Oid, int, unsigned int)
     Probe that fires when a relation extension starts.
       arg0 contains the fork to be extended. arg1, arg2, and arg3 contain the
       tablespace, database, and relation OIDs identifying the relation.  arg4
       is the ID of the backend which created the temporary relation for a
       local buffer, or INVALID_PROC_NUMBER (-1) for a shared
       buffer. arg5 is the number of blocks the caller would like to extend
       by.
    
    
     buffer-extend-done
     (ForkNumber, BlockNumber, Oid, Oid, Oid, int, unsigned int, BlockNumber)
     Probe that fires when a relation extension is complete.
       arg0 contains the fork to be extended. arg1, arg2, and arg3 contain the
       tablespace, database, and relation OIDs identifying the relation.  arg4
       is the ID of the backend which created the temporary relation for a
       local buffer, or INVALID_PROC_NUMBER (-1) for a shared
       buffer. arg5 is the number of blocks the relation was extended by, this
       can be less than the number in the
       buffer-extend-start due to resource
       constraints. arg6 contains the BlockNumber of the first new
       block.
    
    
     buffer-read-start
     (ForkNumber, BlockNumber, Oid, Oid, Oid, int)
     Probe that fires when a buffer read is started.
      arg0 and arg1 contain the fork and block numbers of the page.
      arg2, arg3, and arg4 contain the tablespace, database, and relation OIDs
      identifying the relation.
      arg5 is the ID of the backend which created the temporary relation for a
      local buffer, or INVALID_PROC_NUMBER (-1) for a shared buffer.
      
    
    
     buffer-read-done
     (ForkNumber, BlockNumber, Oid, Oid, Oid, int, bool)
     Probe that fires when a buffer read is complete.
      arg0 and arg1 contain the fork and block numbers of the page.
      arg2, arg3, and arg4 contain the tablespace, database, and relation OIDs
      identifying the relation.
      arg5 is the ID of the backend which created the temporary relation for a
      local buffer, or INVALID_PROC_NUMBER (-1) for a shared buffer.
      arg6 is true if the buffer was found in the pool, false if not.
    
    
     buffer-flush-start
     (ForkNumber, BlockNumber, Oid, Oid, Oid)
     Probe that fires before issuing any write request for a shared
      buffer.
      arg0 and arg1 contain the fork and block numbers of the page.
      arg2, arg3, and arg4 contain the tablespace, database, and relation OIDs
      identifying the relation.
    
    
     buffer-flush-done
     (ForkNumber, BlockNumber, Oid, Oid, Oid)
     Probe that fires when a write request is complete.  (Note
      that this just reflects the time to pass the data to the kernel;
      it's typically not actually been written to disk yet.)
      The arguments are the same as for buffer-flush-start.
    
    
     wal-buffer-write-dirty-start
     ()
     Probe that fires when a server process begins to write a
      dirty WAL buffer because no more WAL buffer space is available.
      (If this happens often, it implies that
       is too small.)
    
    
     wal-buffer-write-dirty-done
     ()
     Probe that fires when a dirty WAL buffer write is complete.
    
    
     wal-insert
     (unsigned char, unsigned char)
     Probe that fires when a WAL record is inserted.
      arg0 is the resource manager (rmid) for the record.
      arg1 contains the info flags.
    
    
     wal-switch
     ()
     Probe that fires when a WAL segment switch is requested.
    
    
     smgr-md-read-start
     (ForkNumber, BlockNumber, Oid, Oid, Oid, int)
     Probe that fires when beginning to read a block from a relation.
      arg0 and arg1 contain the fork and block numbers of the page.
      arg2, arg3, and arg4 contain the tablespace, database, and relation OIDs
      identifying the relation.
      arg5 is the ID of the backend which created the temporary relation for a
      local buffer, or INVALID_PROC_NUMBER (-1) for a shared buffer.
    
    
     smgr-md-read-done
     (ForkNumber, BlockNumber, Oid, Oid, Oid, int, ssize_t, size_t)
     Probe that fires when a block read is complete.
      arg0 and arg1 contain the fork and block numbers of the page.
      arg2, arg3, and arg4 contain the tablespace, database, and relation OIDs
      identifying the relation.
      arg5 is the ID of the backend which created the temporary relation for a
      local buffer, or INVALID_PROC_NUMBER (-1) for a shared buffer.
      arg6 is the number of bytes actually read, while arg7 is the number
      requested (if these are different it indicates a short read).
    
    
     smgr-md-write-start
     (ForkNumber, BlockNumber, Oid, Oid, Oid, int)
     Probe that fires when beginning to write a block to a relation.
      arg0 and arg1 contain the fork and block numbers of the page.
      arg2, arg3, and arg4 contain the tablespace, database, and relation OIDs
      identifying the relation.
      arg5 is the ID of the backend which created the temporary relation for a
      local buffer, or INVALID_PROC_NUMBER (-1) for a shared buffer.
    
    
     smgr-md-write-done
     (ForkNumber, BlockNumber, Oid, Oid, Oid, int, ssize_t, size_t)
     Probe that fires when a block write is complete.
      arg0 and arg1 contain the fork and block numbers of the page.
      arg2, arg3, and arg4 contain the tablespace, database, and relation OIDs
      identifying the relation.
      arg5 is the ID of the backend which created the temporary relation for a
      local buffer, or INVALID_PROC_NUMBER (-1) for a shared buffer.
      arg6 is the number of bytes actually written, while arg7 is the number
      requested (if these are different it indicates a short write).
    
    
     sort-start
     (int, bool, int, int, bool, int)
     Probe that fires when a sort operation is started.
      arg0 indicates heap, index or datum sort.
      arg1 is true for unique-value enforcement.
      arg2 is the number of key columns.
      arg3 is the number of kilobytes of work memory allowed.
      arg4 is true if random access to the sort result is required.
      arg5 indicates serial when 0, parallel worker when
      1, or parallel leader when 2.
    
    
     sort-done
     (bool, long)
     Probe that fires when a sort is complete.
      arg0 is true for external sort, false for internal sort.
      arg1 is the number of disk blocks used for an external sort,
      or kilobytes of memory used for an internal sort.
    
    
     lwlock-acquire
     (char *, LWLockMode)
     Probe that fires when an LWLock has been acquired.
      arg0 is the LWLock's tranche.
      arg1 is the requested lock mode, either exclusive or shared.
    
    
     lwlock-release
     (char *)
     Probe that fires when an LWLock has been released (but note
      that any released waiters have not yet been awakened).
      arg0 is the LWLock's tranche.
    
    
     lwlock-wait-start
     (char *, LWLockMode)
     Probe that fires when an LWLock was not immediately available and
      a server process has begun to wait for the lock to become available.
      arg0 is the LWLock's tranche.
      arg1 is the requested lock mode, either exclusive or shared.
    
    
     lwlock-wait-done
     (char *, LWLockMode)
     Probe that fires when a server process has been released from its
      wait for an LWLock (it does not actually have the lock yet).
      arg0 is the LWLock's tranche.
      arg1 is the requested lock mode, either exclusive or shared.
    
    
     lwlock-condacquire
     (char *, LWLockMode)
     Probe that fires when an LWLock was successfully acquired when the
      caller specified no waiting.
      arg0 is the LWLock's tranche.
      arg1 is the requested lock mode, either exclusive or shared.
    
    
     lwlock-condacquire-fail
     (char *, LWLockMode)
     Probe that fires when an LWLock was not successfully acquired when
      the caller specified no waiting.
      arg0 is the LWLock's tranche.
      arg1 is the requested lock mode, either exclusive or shared.
    
    
     lock-wait-start
     (unsigned int, unsigned int, unsigned int, unsigned int, unsigned int, LOCKMODE)
     Probe that fires when a request for a heavyweight lock (lmgr lock)
      has begun to wait because the lock is not available.
      arg0 through arg3 are the tag fields identifying the object being
      locked.  arg4 indicates the type of object being locked.
      arg5 indicates the lock type being requested.
    
    
     lock-wait-done
     (unsigned int, unsigned int, unsigned int, unsigned int, unsigned int, LOCKMODE)
     Probe that fires when a request for a heavyweight lock (lmgr lock)
      has finished waiting (i.e., has acquired the lock).
      The arguments are the same as for lock-wait-start.
    
    
     deadlock-found
     ()
     Probe that fires when a deadlock is found by the deadlock
      detector.
    

   
   
  

 
  
# Defined Types Used in Probe Parameters

  
   
    
     Type
     Definition
    
   

   

    
     LocalTransactionId
     unsigned int
    
    
     LWLockMode
     int
    
    
     LOCKMODE
     int
    
    
     BlockNumber
     unsigned int
    
    
     Oid
     unsigned int
    
    
     ForkNumber
     int
    
    
     bool
     unsigned char
    

   
   
  

  

  

   
# Using Probes

  

   The example below shows a DTrace script for analyzing transaction
   counts in the system, as an alternative to snapshotting
   pg_stat_database before and after a performance test:

```
#!/usr/sbin/dtrace -qs

postgresql$1:::transaction-start
{
      @start["Start"] = count();
      self->ts  = timestamp;
}

postgresql$1:::transaction-abort
{
      @abort["Abort"] = count();
}

postgresql$1:::transaction-commit
/self->ts/
{
      @commit["Commit"] = count();
      @time["Total time (ns)"] = sum(timestamp - self->ts);
      self->ts=0;
}
```

   When executed, the example D script gives output such as:

```
# ./txn_count.d `pgrep -n postgres` or ./txn_count.d <PID>
^C

Start                                          71
Commit                                         70
Total time (ns)                        2312105013
```

  

  
   

    SystemTap uses a different notation for trace scripts than DTrace does,
    even though the underlying trace points are compatible.  One point worth
    noting is that at this writing, SystemTap scripts must reference probe
    names using double underscores in place of hyphens.  This is expected to
    be fixed in future SystemTap releases.
   

  

  

   You should remember that DTrace scripts need to be carefully written and
   debugged, otherwise the trace information collected might
   be meaningless. In most cases where problems are found it is the
   instrumentation that is at fault, not the underlying system. When
   discussing information found using dynamic tracing, be sure to enclose
   the script used to allow that too to be checked and discussed.
  

  

  

   
# Defining New Probes

  

   New probes can be defined within the code wherever the developer
   desires, though this will require a recompilation. Below are the steps
   for inserting new probes:
  

  
   
    

     Decide on probe names and data to be made available through the probes
    

   

   
    

     Add the probe definitions to src/backend/utils/probes.d
    

   

   
    

     Include pg_trace.h if it is not already present in the
     module(s) containing the probe points, and insert
     TRACE_POSTGRESQL probe macros at the desired locations
     in the source code
    

   

   
    

     Recompile and verify that the new probes are available
    

   
  

  
   
# Example:

   

    Here is an example of how you would add a probe to trace all new
    transactions by transaction ID.
   

  

  
   
    

     Decide that the probe will be named transaction-start and
     requires a parameter of type LocalTransactionId
    

   

   
    

     Add the probe definition to src/backend/utils/probes.d:

```
probe transaction__start(LocalTransactionId);
```

     Note the use of the double underline in the probe name. In a DTrace
     script using the probe, the double underline needs to be replaced with a
     hyphen, so transaction-start is the name to document for
     users.
    

   

   
    

     At compile time, transaction__start is converted to a macro
     called TRACE_POSTGRESQL_TRANSACTION_START (notice the
     underscores are single here), which is available by including
     pg_trace.h.  Add the macro call to the appropriate location
     in the source code.  In this case, it looks like the following:

```
TRACE_POSTGRESQL_TRANSACTION_START(vxid.localTransactionId);
```

    

   

   
    

     After recompiling and running the new binary, check that your newly added
     probe is available by executing the following DTrace command.  You
     should see similar output:

```
# dtrace -ln transaction-start
   ID    PROVIDER          MODULE           FUNCTION NAME
18705 postgresql49878     postgres     StartTransactionCommand transaction-start
18755 postgresql49877     postgres     StartTransactionCommand transaction-start
18805 postgresql49876     postgres     StartTransactionCommand transaction-start
18855 postgresql49875     postgres     StartTransactionCommand transaction-start
18986 postgresql49873     postgres     StartTransactionCommand transaction-start
```

    

   
  

  

   There are a few things to be careful about when adding trace macros
   to the C code:

   

    

     

      You should take care that the data types specified for a probe's
      parameters match the data types of the variables used in the macro.
      Otherwise, you will get compilation errors.
     

    

    

     

      On most platforms, if PostgreSQL is
      built with --enable-dtrace, the arguments to a trace
      macro will be evaluated whenever control passes through the
      macro, even if no tracing is being done.  This is
      usually not worth worrying about if you are just reporting the
      values of a few local variables.  But beware of putting expensive
      function calls into the arguments.  If you need to do that,
      consider protecting the macro with a check to see if the trace
      is actually enabled:

```
if (TRACE_POSTGRESQL_TRANSACTION_START_ENABLED())
    TRACE_POSTGRESQL_TRANSACTION_START(some_function(...));
```

      Each trace macro has a corresponding ENABLED macro.
     

    

   

  

  

 

 

  
# Monitoring Disk Usage

  

   This section discusses how to monitor the disk usage of a
   PostgreSQL database system.
  

  

   
# Determining Disk Usage

   
    disk usage
   

   

    Each table has a primary heap disk file where most of the data is
    stored. If the table has any columns with potentially-wide values,
    there also might be a TOAST file associated with the table,
    which is used to store values too wide to fit comfortably in the main
    table (see ).  There will be one valid index
    on the TOAST table, if present. There also might be indexes
    associated with the base table.  Each table and index is stored in a
    separate disk file &mdash; possibly more than one file, if the file would
    exceed one gigabyte.  Naming conventions for these files are described
    in .
   

   

    You can monitor disk space in three ways:
    using the SQL functions listed in ,
    using the  module, or
    using manual inspection of the system catalogs.
    The SQL functions are the easiest to use and are generally recommended.
    The remainder of this section shows how to do it by inspection of the
    system catalogs.
   

   

    Using psql on a recently vacuumed or analyzed
    database, you can issue queries to see the disk usage of any table:

```
SELECT pg_relation_filepath(oid), relpages FROM pg_class WHERE relname = 'customer';

 pg_relation_filepath | relpages
----------------------+----------
 base/16384/16806     |       60
(1 row)
```

    Each page is typically 8 kilobytes. (Remember, relpages
    is only updated by VACUUM, ANALYZE, and
    a few DDL commands such as CREATE INDEX.)  The file path name
    is of interest if you want to examine the table's disk file directly.
   

   

    To show the space used by TOAST tables, use a query
    like the following:

```
SELECT relname, relpages
FROM pg_class,
     (SELECT reltoastrelid
      FROM pg_class
      WHERE relname = 'customer') AS ss
WHERE oid = ss.reltoastrelid OR
      oid = (SELECT indexrelid
             FROM pg_index
             WHERE indrelid = ss.reltoastrelid)
ORDER BY relname;

       relname        | relpages
----------------------+----------
 pg_toast_16806       |        0
 pg_toast_16806_index |        1
```

   

   

    You can easily display index sizes, too:

```
SELECT c2.relname, c2.relpages
FROM pg_class c, pg_class c2, pg_index i
WHERE c.relname = 'customer' AND
      c.oid = i.indrelid AND
      c2.oid = i.indexrelid
ORDER BY c2.relname;

      relname      | relpages
-------------------+----------
 customer_id_index |       26
```

   

   

    It is easy to find your largest tables and indexes using this
    information:

```
SELECT relname, relpages
FROM pg_class
ORDER BY relpages DESC;

       relname        | relpages
----------------------+----------
 bigtable             |     3290
 customer             |     3144
```

   

  

  

   
# Disk Full Failure

   

    The most important disk monitoring task of a database administrator
    is to make sure the disk doesn't become full.  A filled data disk will
    not result in data corruption, but it might prevent useful activity
    from occurring. If the disk holding the WAL files grows full, database
    server panic and consequent shutdown might occur.
   

   

    If you cannot free up additional space on the disk by deleting
    other things, you can move some of the database files to other file
    systems by making use of tablespaces. See  for more information about that.
   

   
    

     Some file systems perform badly when they are almost full, so do
     not wait until the disk is completely full to take action.
    

   

   

    If your system supports per-user disk quotas, then the database
    will naturally be subject to whatever quota is placed on the user
    the server runs as.  Exceeding the quota will have the same bad
    effects as running out of disk space entirely.
