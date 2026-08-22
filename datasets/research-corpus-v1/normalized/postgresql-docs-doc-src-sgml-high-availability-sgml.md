# High Availability, Load Balancing, and Replication

 high availability
 failover
 replication
 load balancing
 clustering
 data partitioning

 

  Database servers can work together to allow a second server to
  take over quickly if the primary server fails (high
  availability), or to allow several computers to serve the same
  data (load balancing).  Ideally, database servers could work
  together seamlessly.  Web servers serving static web pages can
  be combined quite easily by merely load-balancing web requests
  to multiple machines.  In fact, read-only database servers can
  be combined relatively easily too.  Unfortunately, most database
  servers have a read/write mix of requests, and read/write servers
  are much harder to combine.  This is because though read-only
  data needs to be placed on each server only once, a write to any
  server has to be propagated to all servers so that future read
  requests to those servers return consistent results.
 

 

  This synchronization problem is the fundamental difficulty for
  servers working together.  Because there is no single solution
  that eliminates the impact of the sync problem for all use cases,
  there are multiple solutions.  Each solution addresses this
  problem in a different way, and minimizes its impact for a specific
  workload.
 

 

  Some solutions deal with synchronization by allowing only one
  server to modify the data.  Servers that can modify data are
  called read/write, master or primary servers.
  Servers that track changes in the primary are called standby
  or secondary servers. A standby server that cannot be connected
  to until it is promoted to a primary server is called a warm
  standby server, and one that can accept connections and serves read-only
  queries is called a hot standby server.
 

 

  Some solutions are synchronous,
  meaning that a data-modifying transaction is not considered
  committed until all servers have committed the transaction.  This
  guarantees that a failover will not lose any data and that all
  load-balanced servers will return consistent results no matter
  which server is queried. In contrast, asynchronous solutions allow some
  delay between the time of a commit and its propagation to the other servers,
  opening the possibility that some transactions might be lost in
  the switch to a backup server, and that load balanced servers
  might return slightly stale results.  Asynchronous communication
  is used when synchronous would be too slow.
 

 

  Solutions can also be categorized by their granularity.  Some solutions
  can deal only with an entire database server, while others allow control
  at the per-table or per-database level.
 

 

  Performance must be considered in any choice.  There is usually a
  trade-off between functionality and
  performance.  For example, a fully synchronous solution over a slow
  network might cut performance by more than half, while an asynchronous
  one might have a minimal performance impact.
 

 

  The remainder of this section outlines various failover, replication,
  and load balancing solutions.
 

 

 
# Comparison of Different Solutions

 

  

   
Shared Disk Failover

   

    

     Shared disk failover avoids synchronization overhead by having only one
     copy of the database.  It uses a single disk array that is shared by
     multiple servers.  If the main database server fails, the standby server
     is able to mount and start the database as though it were recovering from
     a database crash.  This allows rapid failover with no data loss.
    

    

     Shared hardware functionality is common in network storage devices.
     Using a network file system is also possible, though care must be
     taken that the file system has full POSIX behavior (see ).  One significant limitation of this
     method is that if the shared disk array fails or becomes corrupt, the
     primary and standby servers are both nonfunctional.  Another issue is
     that the standby server should never access the shared storage while
     the primary server is running.
    

   

  

  

   
File System (Block Device) Replication

   

    

     A modified version of shared hardware functionality is file system
     replication, where all changes to a file system are mirrored to a file
     system residing on another computer.  The only restriction is that
     the mirroring must be done in a way that ensures the standby server
     has a consistent copy of the file system &mdash; specifically, writes
     to the standby must be done in the same order as those on the primary.
     DRBD is a popular file system replication solution
     for Linux.
    

   

  

  

   
Write-Ahead Log Shipping

   

    

     Warm and hot standby servers can be kept current by reading a
     stream of write-ahead log (WAL)
     records.  If the main server fails, the standby contains
     almost all of the data of the main server, and can be quickly
     made the new primary database server.  This can be synchronous or
     asynchronous and can only be done for the entire database server.
    

    

     A standby server can be implemented using file-based log shipping
     () or streaming physical replication (see
     ), or a combination of both. For
     information on hot standby, see .
    

   

  

  

   
Logical Replication

   

    

     Logical replication allows a database server to send a stream of data
     modifications to another server.  PostgreSQL
     logical replication constructs a stream of logical data modifications
     from the WAL.  Logical replication allows replication of data changes on
     a per-table basis.  In addition, a server that is publishing its own
     changes can also subscribe to changes from another server, allowing data
     to flow in multiple directions.  For more information on logical
     replication, see .  Through the
     logical decoding interface (),
     third-party extensions can also provide similar functionality.
    

   

  

  

   
Trigger-Based Primary-Standby Replication

   

    

     A trigger-based replication setup typically funnels data modification
     queries to a designated primary server. Operating on a per-table basis,
     the primary server sends data changes (typically) asynchronously to the
     standby servers.  Standby servers can answer queries while the primary is
     running, and may allow some local data changes or write activity.  This
     form of replication is often used for offloading large analytical or data
     warehouse queries.
    

    

     Slony-I is an example of this type of
     replication, with per-table granularity, and support for multiple standby
     servers.  Because it updates the standby server asynchronously (in
     batches), there is possible data loss during fail over.
    

   

  

  

   
SQL-Based Replication Middleware

   

    

     With SQL-based replication middleware, a program intercepts
     every SQL query and sends it to one or all servers.  Each server
     operates independently.  Read-write queries must be sent to all servers,
     so that every server receives any changes.  But read-only queries can be
     sent to just one server, allowing the read workload to be distributed
     among them.
    

    

     If queries are simply broadcast unmodified, functions like
     random(), CURRENT_TIMESTAMP, and
     sequences can have different values on different servers.
     This is because each server operates independently, and because
     SQL queries are broadcast rather than actual data changes.  If
     this is unacceptable, either the middleware or the application
     must determine such values from a single source and then use those
     values in write queries.  Care must also be taken that all
     transactions either commit or abort on all servers, perhaps
     using two-phase commit (
     and ).
     Pgpool-II and Continuent Tungsten
     are examples of this type of replication.
    

   

  

  

   
Asynchronous Multimaster Replication

   

    

     For servers that are not regularly connected or have slow
     communication links, like laptops or
     remote servers, keeping data consistent among servers is a
     challenge.  Using asynchronous multimaster replication, each
     server works independently, and periodically communicates with
     the other servers to identify conflicting transactions.  The
     conflicts can be resolved by users or conflict resolution rules.
     Bucardo is an example of this type of replication.
    

   

  

  

   
Synchronous Multimaster Replication

   

    

     In synchronous multimaster replication, each server can accept
     write requests, and modified data is transmitted from the
     original server to every other server before each transaction
     commits.  Heavy write activity can cause excessive locking and
     commit delays, leading to poor performance.  Read requests can
     be sent to any server.  Some implementations use shared disk
     to reduce the communication overhead.  Synchronous multimaster
     replication is best for mostly read workloads, though its big
     advantage is that any server can accept write requests &mdash;
     there is no need to partition workloads between primary and
     standby servers, and because the data changes are sent from one
     server to another, there is no problem with non-deterministic
     functions like random().
    

    

     PostgreSQL does not offer this type of replication,
     though PostgreSQL two-phase commit ( and )
     can be used to implement this in application code or middleware.
    

   

  

 

 

   summarizes
  the capabilities of the various solutions listed above.
 

 
  
# High Availability, Load Balancing, and Replication Feature Matrix

  
   
   
   
   
   
   
   
   
   
   
    
     Feature
     Shared Disk
     File System Repl.
     Write-Ahead Log Shipping
     Logical Repl.
     Trigger-&zwsp;Based Repl.
     SQL Repl. Middle-ware
     Async. MM Repl.
     Sync. MM Repl.
    
   

   

    
     Popular examples
     NAS
     DRBD
     built-in streaming repl.
     built-in logical repl., pglogical
     Londiste, Slony
     pgpool-II
     Bucardo
     
    

    
     Comm. method
     shared disk
     disk blocks
     WAL
     logical decoding
     table rows
     SQL
     table rows
     table rows and row locks
    

    
     No special hardware required
     
     &bull;
     &bull;
     &bull;
     &bull;
     &bull;
     &bull;
     &bull;
    

    
     Allows multiple primary servers
     
     
     
     &bull;
     
     &bull;
     &bull;
     &bull;
    

    
     No overhead on primary
     &bull;
     
     &bull;
     &bull;
     
     &bull;
     
     
    

    
     No waiting for multiple servers
     &bull;
     
     with sync off
     with sync off
     &bull;
     
     &bull;
     
    

    
     Primary failure will never lose data
     &bull;
     &bull;
     with sync on
     with sync on
     
     &bull;
     
     &bull;
    

    
     Replicas accept read-only queries
     
     
     with hot standby
     &bull;
     &bull;
     &bull;
     &bull;
     &bull;
    

    
     Per-table granularity
     
     
     
     &bull;
     &bull;
     
     &bull;
     &bull;
    

    
     No conflict resolution necessary
     &bull;
     &bull;
     &bull;
     
     &bull;
     &bull;
     
     &bull;
    

   
  
 

 

  There are a few solutions that do not fit into the above categories:
 

 

  

   
Data Partitioning

   

    

     Data partitioning splits tables into data sets.  Each set can
     be modified by only one server.  For example, data can be
     partitioned by offices, e.g., London and Paris, with a server
     in each office.  If queries combining London and Paris data
     are necessary, an application can query both servers, or
     primary/standby replication can be used to keep a read-only copy
     of the other office's data on each server.
    

   

  

  

   
Multiple-Server Parallel Query Execution

   

    

     Many of the above solutions allow multiple servers to handle multiple
     queries, but none allow a single query to use multiple servers to
     complete faster.  This solution allows multiple servers to work
     concurrently on a single query.  It is usually accomplished by
     splitting the data among servers and having each server execute its
     part of the query and return results to a central server where they
     are combined and returned to the user. This can be implemented using the
     PL/Proxy tool set.
    

   

  

 

  

   It should also be noted that because PostgreSQL
   is open source and easily extended, a number of companies have
   taken PostgreSQL and created commercial
   closed-source solutions with unique failover, replication, and load
   balancing capabilities.  These are not discussed here.
  

 

 

 
# Log-Shipping Standby Servers

  

   Continuous archiving can be used to create a high
   availability (HA) cluster configuration with one or more
   standby servers ready to take over operations if the
   primary server fails. This capability is widely referred to as
   warm standby or log shipping.
  

  

   The primary and standby server work together to provide this capability,
   though the servers are only loosely coupled. The primary server operates
   in continuous archiving mode, while each standby server operates in
   continuous recovery mode, reading the WAL files from the primary. No
   changes to the database tables are required to enable this capability,
   so it offers low administration overhead compared to some other
   replication solutions. This configuration also has relatively low
   performance impact on the primary server.
  

  

   Directly moving WAL records from one database server to another
   is typically described as log shipping. PostgreSQL
   implements file-based log shipping by transferring WAL records
   one file (WAL segment) at a time. WAL files (16MB) can be
   shipped easily and cheaply over any distance, whether it be to an
   adjacent system, another system at the same site, or another system on
   the far side of the globe. The bandwidth required for this technique
   varies according to the transaction rate of the primary server.
   Record-based log shipping is more granular and streams WAL changes
   incrementally over a network connection (see ).
  

  

   It should be noted that log shipping is asynchronous, i.e., the WAL
   records are shipped after transaction commit. As a result, there is a
   window for data loss should the primary server suffer a catastrophic
   failure; transactions not yet shipped will be lost.  The size of the
   data loss window in file-based log shipping can be limited by use of the
   archive_timeout parameter, which can be set as low
   as a few seconds.  However such a low setting will
   substantially increase the bandwidth required for file shipping.
   Streaming replication (see )
   allows a much smaller window of data loss.
  

  

   Recovery performance is sufficiently good that the standby will
   typically be only moments away from full
   availability once it has been activated. As a result, this is called
   a warm standby configuration which offers high
   availability. Restoring a server from an archived base backup and
   rollforward will take considerably longer, so that technique only
   offers a solution for disaster recovery, not high availability.
   A standby server can also be used for read-only queries, in which case
   it is called a hot standby server. See
    for more information.
  

  
   warm standby
  

  
   PITR standby
  

  
   standby server
  

  
   log shipping
  

  
   witness server
  

  
   STONITH
  

  

   
# Planning

   

    It is usually wise to create the primary and standby servers
    so that they are as similar as possible, at least from the
    perspective of the database server.  In particular, the path names
    associated with tablespaces will be passed across unmodified, so both
    primary and standby servers must have the same mount paths for
    tablespaces if that feature is used.  Keep in mind that if
    
    is executed on the primary, any new mount point needed for it must
    be created on the primary and all standby servers before the command
    is executed. Hardware need not be exactly the same, but experience shows
    that maintaining two identical systems is easier than maintaining two
    dissimilar ones over the lifetime of the application and system.
    In any case the hardware architecture must be the same &mdash; shipping
    from, say, a 32-bit to a 64-bit system will not work.
   

   

    In general, log shipping between servers running different major
    PostgreSQL release
    levels is not possible. It is the policy of the PostgreSQL Global
    Development Group not to make changes to disk formats during minor release
    upgrades, so it is likely that running different minor release levels
    on primary and standby servers will work successfully. However, no
    formal support for that is offered and you are advised to keep primary
    and standby servers at the same release level as much as possible.
    When updating to a new minor release, the safest policy is to update
    the standby servers first &mdash; a new minor release is more likely
    to be able to read WAL files from a previous minor release than vice
    versa.
   

  

  

   
# Standby Server Operation

   

    A server enters standby mode if a
    
    standby.signal
    standby.signal
    file exists in the data directory when the server is started.
   

   

    In standby mode, the server continuously applies WAL received from the
    primary server. The standby server can read WAL from a WAL archive
    (see ) or directly from the primary
    over a TCP connection (streaming physical replication). The standby server will
    also attempt to restore any WAL found in the standby cluster's
    pg_wal directory. That typically happens after a server
    restart, when the standby replays again WAL that was streamed from the
    primary before the restart, but you can also manually copy files to
    pg_wal at any time to have them replayed.
   

   

    At startup, the standby begins by restoring all WAL available in the
    archive location, calling restore_command. Once it
    reaches the end of WAL available there and restore_command
    fails, it tries to restore any WAL available in the pg_wal directory.
    If that fails, and streaming replication has been configured, the
    standby tries to connect to the primary server and start streaming WAL
    from the last valid record found in archive or pg_wal. If that fails
    or streaming replication is not configured, or if the connection is
    later disconnected, the standby goes back to step 1 and tries to
    restore the file from the archive again. This loop of retries from the
    archive, pg_wal, and via streaming replication goes on until the server
    is stopped or is promoted.
   

   

    Standby mode is exited and the server switches to normal operation
    when pg_ctl promote is run, or
    pg_promote() is called. Before failover,
    any WAL immediately available in the archive or in pg_wal
    will be restored, but no attempt is made to connect to the primary.
   

  

  

   
# Preparing the Primary for Standby Servers

   

    Set up continuous archiving on the primary to an archive directory
    accessible from the standby, as described
    in . The archive location should be
    accessible from the standby even when the primary is down, i.e., it should
    reside on the standby server itself or another trusted server, not on
    the primary server.
   

   

    If you want to use streaming replication, set up authentication on the
    primary server to allow replication connections from the standby
    server(s); that is, create a role and provide a suitable entry or
    entries in pg_hba.conf with the database field set to
    replication.  Also ensure max_wal_senders is set
    to a sufficiently large value in the configuration file of the primary
    server. If replication slots will be used,
    ensure that max_replication_slots is set sufficiently
    high as well.
   

   

    Take a base backup as described in 
    to bootstrap the standby server.
   

  

  

   
# Setting Up a Standby Server

   

    To set up the standby server, restore the base backup taken from primary
    server (see ). Create a file
    standby.signalstandby.signal
    in the standby's cluster data
    directory. Set  to a simple command to copy files from
    the WAL archive. If you plan to have multiple standby servers for high
    availability purposes, make sure that recovery_target_timeline is set to
    latest (the default), to make the standby server follow the timeline change
    that occurs at failover to another standby.
   

   
     

      should return immediately
     if the file does not exist; the server will retry the command again if
     necessary.
    

   

   

     If you want to use streaming replication, fill in
      with a libpq connection string, including
     the host name (or IP address) and any additional details needed to
     connect to the primary server. If the primary needs a password for
     authentication, the password needs to be specified in
      as well.
   

   

    If you're setting up the standby server for high availability purposes,
    set up WAL archiving, connections and authentication like the primary
    server, because the standby server will work as a primary server after
    failover.
   

   

    If you're using a WAL archive, its size can be minimized using the  parameter to remove files that are no
    longer required by the standby server.
    The pg_archivecleanup utility is designed specifically to
    be used with archive_cleanup_command in typical single-standby
    configurations, see .
    Note however, that if you're using the archive for backup purposes, you
    need to retain files needed to recover from at least the latest base
    backup, even if they're no longer needed by the standby.
   

   

    A simple example of configuration is:

```
primary_conninfo = 'host=192.168.1.50 port=5432 user=foo password=foopass options=''-c wal_sender_timeout=5000'''
restore_command = 'cp "/path/to/archive/%f" "%p"'
archive_cleanup_command = 'pg_archivecleanup /path/to/archive "%r"'
```

   

   

    You can have any number of standby servers, but if you use streaming
    replication, make sure you set max_wal_senders high enough in
    the primary to allow them to be connected simultaneously.
   

  

  

   
# Streaming Replication

   
    Streaming Replication
   

   

    Streaming replication allows a standby server to stay more up-to-date
    than is possible with file-based log shipping. The standby connects
    to the primary, which streams WAL records to the standby as they're
    generated, without waiting for the WAL file to be filled.
   

   
    

     This discussion of streaming replication assumes physical replication.
     Although you could treat a logical replication subscriber as a warm standby,
     it would require some differences to what is described here.
    

   

   

    Streaming replication is asynchronous by default
    (see ), in which case there is
    a small delay between committing a transaction in the primary and the
    changes becoming visible in the standby. This delay is however much
    smaller than with file-based log shipping, typically under one second
    assuming the standby is powerful enough to keep up with the load. With
    streaming replication, archive_timeout is not required to
    reduce the data loss window.
   

   

    If you use streaming replication without file-based continuous
    archiving, the server might recycle old WAL segments before the standby
    has received them.  If this occurs, the standby will need to be
    reinitialized from a new base backup.  You can avoid this by setting
    wal_keep_size to a value large enough to ensure that
    WAL segments are not recycled too early, or by configuring a replication
    slot for the standby.  If you set up a WAL archive that's accessible from
    the standby, these solutions are not required, since the standby can
    always use the archive to catch up provided it retains enough segments.
   

   

    To use streaming replication, set up a file-based log-shipping standby
    server as described in . The step that
    turns a file-based log-shipping standby into streaming replication
    standby is setting the primary_conninfo setting
    to point to the primary server. Set
     and authentication options
    (see pg_hba.conf) on the primary so that the standby server
    can connect to the replication pseudo-database on the primary
    server (see ).
   

   

    On systems that support the keepalive socket option, setting
    ,
     and
     helps the primary promptly
    notice a broken connection.
   

   

    Set the maximum number of concurrent connections from the standby servers
    (see  for details).
   

   

    When the standby is started and primary_conninfo is set
    correctly, the standby will connect to the primary after replaying all
    WAL files available in the archive. If the connection is established
    successfully, you will see a walreceiver in the standby, and
    a corresponding walsender process in the primary.
   

   

    
# Authentication

    

     It is very important that the access privileges for replication be set up
     so that only trusted users can read the WAL stream, because it is
     easy to extract privileged information from it.  Standby servers must
     authenticate to the primary as an account that has the
     REPLICATION privilege or a superuser. It is
     recommended to create a dedicated user account with
     REPLICATION and LOGIN
     privileges for replication. While REPLICATION
     privilege gives very high permissions, it does not allow the user to
     modify any data on the primary system, which the
     SUPERUSER privilege does.
    

    

     Client authentication for replication is controlled by a
     pg_hba.conf record specifying replication in the
     database field. For example, if the standby is running on
     host IP 192.168.1.100 and the account name for replication
     is foo, the administrator can add the following line to the
     pg_hba.conf file on the primary:

```
# Allow the user "foo" from host 192.168.1.100 to connect to the primary
# as a replication standby if the user's password is correctly supplied.
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    replication     foo             192.168.1.100/32        scram-sha-256
```

    

    

     The host name and port number of the primary, connection user name,
     and password are specified in the .
     The password can also be set in the ~/.pgpass file on the
     standby (specify replication in the database
     field).
     For example, if the primary is running on host IP 192.168.1.50,
     port 5432, the account name for replication is
     foo, and the password is foopass, the administrator
     can add the following line to the postgresql.conf file on the
     standby:

```
# The standby connects to the primary that is running on host 192.168.1.50
# and port 5432 as the user "foo" whose password is "foopass".
primary_conninfo = 'host=192.168.1.50 port=5432 user=foo password=foopass'
```

    

   

   

    
# Monitoring

    

     An important health indicator of streaming replication is the amount
     of WAL records generated in the primary, but not yet applied in the
     standby. You can calculate this lag by comparing the current WAL write
     location on the primary with the last WAL location received by the
     standby. These locations can be retrieved using
     pg_current_wal_lsn on the primary and
     pg_last_wal_receive_lsn on the standby,
     respectively (see  and
      for details).
     The last WAL receive location in the standby is also displayed in the
     process status of the WAL receiver process, displayed using the
     ps command (see  for details).
    

    

     You can retrieve a list of WAL sender processes via the
     
     pg_stat_replication view. Large differences between
     pg_current_wal_lsn and the view's sent_lsn field
     might indicate that the primary server is under heavy load, while
     differences between sent_lsn and
     pg_last_wal_receive_lsn on the standby might indicate
     network delay, or that the standby is under heavy load.
    

    

     On a hot standby, the status of the WAL receiver process can be retrieved
     via the 
     pg_stat_wal_receiver view.  A large
     difference between pg_last_wal_replay_lsn and the
     view's flushed_lsn indicates that WAL is being
     received faster than it can be replayed.  Recovery state and replay
     progress can also be monitored via the
     
     pg_stat_recovery view.
    

   

  

  

   
# Replication Slots

   
    replication slot
    streaming replication
   
   

    Replication slots provide an automated way to ensure that the
    primary server does not remove WAL segments until they have been
    received, physically or logically, by all standbys/subscribers,
    and that the primary does not remove rows which could cause a recovery conflict on physical
    replicas even when the standby is disconnected.
   

   

    In lieu of using replication slots, it is possible to prevent the removal
    of old WAL segments using , or by
    storing the segments in an archive using
     or .
    A disadvantage of these methods is that they
    often result in retaining more WAL segments than
    required, whereas replication slots retain only the number of segments
    known to be needed.
   

   

    Similarly,  on its own, without
    also using a physical replication slot, provides protection against relevant rows
    being removed by vacuum, but provides no protection during any time period
    when the physical replication standby is disconnected.
   

   
    

     Beware that replication slots can cause the server to retain so
     many WAL segments that they fill up the space allocated for
     pg_wal.
      can be used to limit the size
     of WAL files retained by replication slots.
    

   

   

    
# Querying and Manipulating Replication Slots

    

     Each replication slot has a name, which can contain lower-case letters,
     numbers, and the underscore character.
    

    

     Existing replication slots and their state can be seen in the
     pg_replication_slots
     view.
    

    

     Slots can be created and dropped either via the streaming replication
     protocol (see ) or via SQL
     functions (see ).
    

   

   

    
# Configuration Example

    

     You can create a physical replication slot on the primary like this:

```
postgres=# SELECT * FROM pg_create_physical_replication_slot('node_a_slot');
  slot_name  | lsn
-------------+-----
 node_a_slot |

postgres=# SELECT slot_name, slot_type, active FROM pg_replication_slots;
  slot_name  | slot_type | active
-------------+-----------+--------
 node_a_slot | physical  | f
(1 row)
```

     To configure the standby to use this slot, primary_slot_name
     should be configured on the standby. Here is a simple example:

```
primary_conninfo = 'host=192.168.1.50 port=5432 user=foo password=foopass'
primary_slot_name = 'node_a_slot'
```

    

   

  

  

   
# Cascading Replication

   
    Cascading Replication
   

   

    The cascading replication feature allows a standby server to accept replication
    connections and stream WAL records to other standbys, acting as a relay.
    This can be used to reduce the number of direct connections to the primary
    and also to minimize inter-site bandwidth overheads.
   

   

    A standby acting as both a receiver and a sender is known as a cascading
    standby.  Standbys that are more directly connected to the primary are known
    as upstream servers, while those standby servers further away are downstream
    servers.  Cascading replication does not place limits on the number or
    arrangement of downstream servers, though each standby connects to only
    one upstream server which eventually links to a single primary server.
   

   

    A cascading standby sends not only WAL records received from the
    primary but also those restored from the archive. So even if the replication
    connection in some upstream connection is terminated, streaming replication
    continues downstream for as long as new WAL records are available.
   

   

    Cascading replication is currently asynchronous. Synchronous replication
    (see ) settings have no effect on
    cascading replication at present.
   

   

    Hot standby feedback propagates upstream, whatever the cascaded arrangement.
   

   

    If an upstream standby server is promoted to become the new primary, downstream
    servers will continue to stream from the new primary if
    recovery_target_timeline is set to 'latest' (the default).
   

   

    To use cascading replication, set up the cascading standby so that it can
    accept replication connections (that is, set
     and ,
    and configure
    host-based authentication).
    You will also need to set primary_conninfo in the downstream
    standby to point to the cascading standby.
   

  

  

   
# Synchronous Replication

   
    Synchronous Replication
   

   

    PostgreSQL streaming replication is asynchronous by
    default. If the primary server
    crashes then some transactions that were committed may not have been
    replicated to the standby server, causing data loss. The amount
    of data loss is proportional to the replication delay at the time of
    failover.
   

   

    Synchronous replication offers the ability to confirm that all changes
    made by a transaction have been transferred to one or more synchronous
    standby servers. This extends that standard level of durability
    offered by a transaction commit. This level of protection is referred
    to as 2-safe replication in computer science theory, and group-1-safe
    (group-safe and 1-safe) when synchronous_commit is set to
    remote_write.
   

   

    When requesting synchronous replication, each commit of a
    write transaction will wait until confirmation is
    received that the commit has been written to the write-ahead log on disk
    of both the primary and standby server. The only possibility that data
    can be lost is if both the primary and the standby suffer crashes at the
    same time. This can provide a much higher level of durability, though only
    if the sysadmin is cautious about the placement and management of the two
    servers.  Waiting for confirmation increases the user's confidence that the
    changes will not be lost in the event of server crashes but it also
    necessarily increases the response time for the requesting transaction.
    The minimum wait time is the round-trip time between primary and standby.
   

   

    Read-only transactions and transaction rollbacks need not wait for
    replies from standby servers. Subtransaction commits do not wait for
    responses from standby servers, only top-level commits. Long
    running actions such as data loading or index building do not wait
    until the very final commit message. All two-phase commit actions
    require commit waits, including both prepare and commit.
   

   

    A synchronous standby can be a physical replication standby or a logical
    replication subscriber.  It can also be any other physical or logical WAL
    replication stream consumer that knows how to send the appropriate
    feedback messages.  Besides the built-in physical and logical replication
    systems, this includes special programs such
    as pg_receivewal and pg_recvlogical
    as well as some third-party replication systems and custom programs.
    Check the respective documentation for details on synchronous replication
    support.
   

   

    
# Basic Configuration

   

    Once streaming replication has been configured, configuring synchronous
    replication requires only one additional configuration step:
     must be set to
    a non-empty value.  synchronous_commit must also be set to
    on, but since this is the default value, typically no change is
    required.  (See  and
    .)
    This configuration will cause each commit to wait for
    confirmation that the standby has written the commit record to durable
    storage.
    synchronous_commit can be set by individual
    users, so it can be configured in the configuration file, for particular
    users or databases, or dynamically by applications, in order to control
    the durability guarantee on a per-transaction basis.
   

   

    After a commit record has been written to disk on the primary, the
    WAL record is then sent to the standby. The standby sends reply
    messages each time a new batch of WAL data is written to disk, unless
    wal_receiver_status_interval is set to zero on the standby.
    In the case that synchronous_commit is set to
    remote_apply, the standby sends reply messages when the commit
    record is replayed, making the transaction visible.
    If the standby is chosen as a synchronous standby, according to the setting
    of synchronous_standby_names on the primary, the reply
    messages from that standby will be considered along with those from other
    synchronous standbys to decide when to release transactions waiting for
    confirmation that the commit record has been received. These parameters
    allow the administrator to specify which standby servers should be
    synchronous standbys. Note that the configuration of synchronous
    replication is mainly on the primary. Named standbys must be directly
    connected to the primary; the primary knows nothing about downstream
    standby servers using cascaded replication.
   

   

    Setting synchronous_commit to remote_write will
    cause each commit to wait for confirmation that the standby has received
    the commit record and written it out to its own operating system, but not
    for the data to be flushed to disk on the standby.  This
    setting provides a weaker guarantee of durability than on
    does: the standby could lose the data in the event of an operating system
    crash, though not a PostgreSQL crash.
    However, it's a useful setting in practice
    because it can decrease the response time for the transaction.
    Data loss could only occur if both the primary and the standby crash and
    the database of the primary gets corrupted at the same time.
   

   

    Setting synchronous_commit to remote_apply will
    cause each commit to wait until the current synchronous standbys report
    that they have replayed the transaction, making it visible to user
    queries.  In simple cases, this allows for load balancing with causal
    consistency.
   

   

    Users will stop waiting if a fast shutdown is requested.  However, when
    using replication, the server will not fully shutdown until all
    outstanding WAL records are transferred to the currently connected
    standby servers, or 
    (if set) expires, regardless of whether replication is synchronous or
    asynchronous.
   

   

   

    
# Multiple Synchronous Standbys

   

    Synchronous replication supports one or more synchronous standby servers;
    transactions will wait until all the standby servers which are considered
    as synchronous confirm receipt of their data. The number of synchronous
    standbys that transactions must wait for replies from is specified in
    synchronous_standby_names. This parameter also specifies
    a list of standby names and the method (FIRST and
    ANY) to choose synchronous standbys from the listed ones.
   

   

    The method FIRST specifies a priority-based synchronous
    replication and makes transaction commits wait until their WAL records are
    replicated to the requested number of synchronous standbys chosen based on
    their priorities. The standbys whose names appear earlier in the list are
    given higher priority and will be considered as synchronous. Other standby
    servers appearing later in this list represent potential synchronous
    standbys. If any of the current synchronous standbys disconnects for
    whatever reason, it will be replaced immediately with the
    next-highest-priority standby.
   

   

    An example of synchronous_standby_names for
    a priority-based multiple synchronous standbys is:

```
synchronous_standby_names = 'FIRST 2 (s1, s2, s3)'
```

    In this example, if four standby servers s1, s2,
    s3 and s4 are running, the two standbys
    s1 and s2 will be chosen as synchronous standbys
    because their names appear early in the list of standby names.
    s3 is a potential synchronous standby and will take over
    the role of synchronous standby when either of s1 or
    s2 fails. s4 is an asynchronous standby since
    its name is not in the list.
   

   

    The method ANY specifies a quorum-based synchronous
    replication and makes transaction commits wait until their WAL records
    are replicated to at least the requested number of
    synchronous standbys in the list.
   

   

    An example of synchronous_standby_names for
    a quorum-based multiple synchronous standbys is:

```
synchronous_standby_names = 'ANY 2 (s1, s2, s3)'
```

    In this example, if four standby servers s1, s2,
    s3 and s4 are running, transaction commits will
    wait for replies from at least any two standbys of s1,
    s2 and s3. s4 is an asynchronous
    standby since its name is not in the list.
   

   

    The synchronous states of standby servers can be viewed using
    the pg_stat_replication view.
   

   

   

    
# Planning for Performance

   

    Synchronous replication usually requires carefully planned and placed
    standby servers to ensure applications perform acceptably. Waiting
    doesn't utilize system resources, but transaction locks continue to be
    held until the transfer is confirmed. As a result, incautious use of
    synchronous replication will reduce performance for database
    applications because of increased response times and higher contention.
   

   

    PostgreSQL allows the application developer
    to specify the durability level required via replication. This can be
    specified for the system overall, though it can also be specified for
    specific users or connections, or even individual transactions.
   

   

    For example, an application workload might consist of:
    10% of changes are important customer details, while
    90% of changes are less important data that the business can more
    easily survive if it is lost, such as chat messages between users.
   

   

    With synchronous replication options specified at the application level
    (on the primary) we can offer synchronous replication for the most
    important changes, without slowing down the bulk of the total workload.
    Application level options are an important and practical tool for allowing
    the benefits of synchronous replication for high performance applications.
   

   

    You should consider that the network bandwidth must be higher than
    the rate of generation of WAL data.
   

   

   

    
# Planning for High Availability

   

    synchronous_standby_names specifies the number and
    names of synchronous standbys that transaction commits made when
    synchronous_commit is set to on,
    remote_apply or remote_write will wait for
    responses from. Such transaction commits may never be completed
    if any one of the synchronous standbys should crash.
   

   

    The best solution for high availability is to ensure you keep as many
    synchronous standbys as requested. This can be achieved by naming multiple
    potential synchronous standbys using synchronous_standby_names.
   

   

    In a priority-based synchronous replication, the standbys whose names
    appear earlier in the list will be used as synchronous standbys.
    Standbys listed after these will take over the role of synchronous standby
    if one of current ones should fail.
   

   

    In a quorum-based synchronous replication, all the standbys appearing
    in the list will be used as candidates for synchronous standbys.
    Even if one of them should fail, the other standbys will keep performing
    the role of candidates of synchronous standby.
   

   

    When a standby first attaches to the primary, it will not yet be properly
    synchronized. This is described as catchup mode. Once
    the lag between standby and primary reaches zero for the first time
    we move to real-time streaming state.
    The catch-up duration may be long immediately after the standby has
    been created. If the standby is shut down, then the catch-up period
    will increase according to the length of time the standby has been down.
    The standby is only able to become a synchronous standby
    once it has reached streaming state.
    This state can be viewed using
    the pg_stat_replication view.
   

   

    If primary restarts while commits are waiting for acknowledgment, those
    waiting transactions will be marked fully committed once the primary
    database recovers.
    There is no way to be certain that all standbys have received all
    outstanding WAL data at time of the crash of the primary. Some
    transactions may not show as committed on the standby, even though
    they show as committed on the primary. The guarantee we offer is that
    the application will not receive explicit acknowledgment of the
    successful commit of a transaction until the WAL data is known to be
    safely received by all the synchronous standbys.
   

   

    If you really cannot keep as many synchronous standbys as requested
    then you should decrease the number of synchronous standbys that
    transaction commits must wait for responses from
    in synchronous_standby_names (or disable it) and
    reload the configuration file on the primary server.
   

   

    If the primary is isolated from remaining standby servers you should
    fail over to the best candidate of those other remaining standby servers.
   

   

    If you need to re-create a standby server while transactions are
    waiting, make sure that the functions pg_backup_start()
    and pg_backup_stop() are run in a session with
    synchronous_commit = off, otherwise those
    requests will wait forever for the standby to appear.
   

   

  

  

   
# Read-Your-Writes Consistency

   

    In asynchronous replication, there is always a short window where changes
    on the primary may not yet be visible on the standby due to replication
    lag. This can lead to inconsistencies when an application writes data on
    the primary and then immediately issues a read query on the standby.
    However, it is possible to address this without switching to synchronous
    replication.
   

   

    To address this, PostgreSQL offers a mechanism for read-your-writes
    consistency. The key idea is to ensure that a client sees its own writes
    by synchronizing the WAL replay on the standby with the known point of
    change on the primary.
   

   

    This is achieved by the following steps.  After performing write
    operations, the application retrieves the current WAL location using a
    function call like this.

```
postgres=# SELECT pg_current_wal_insert_lsn();
 pg_current_wal_insert_lsn
---------------------------
 0/306EE20
(1 row)
```

   

   

    The LSN obtained from the primary is then communicated
    to the standby server. This can be managed at the application level or
    via the connection pooler.  On the standby, the application issues the
     command to block further processing until
    the standby's WAL replay process reaches (or exceeds) the specified
    LSN.

```
postgres=# WAIT FOR LSN '0/306EE20';
 status
---------
 success
(1 row)
```

    Once the command returns a status of success, it guarantees that all
    changes up to the provided LSN have been applied,
    ensuring that subsequent read queries will reflect the latest updates.
   

  

  

   
# Continuous Archiving in Standby

   
     continuous archiving
     in standby
   

   

     When continuous WAL archiving is used in a standby, there are two
     different scenarios: the WAL archive can be shared between the primary
     and the standby, or the standby can have its own WAL archive. When
     the standby has its own WAL archive, set archive_mode
     to always, and the standby will call the archive
     command for every WAL segment it receives, whether it's by restoring
     from the archive or by streaming replication. The shared archive can
     be handled similarly, but the archive_command or archive_library must
     test if the file being archived exists already, and if the existing file
     has identical contents. This requires more care in the
     archive_command or archive_library, as it must
     be careful to not overwrite an existing file with different contents,
     but return success if the exactly same file is archived twice. And
     all that must be done free of race conditions, if two servers attempt
     to archive the same file at the same time.
   

   

     If archive_mode is set to on, the
     archiver is not enabled during recovery or standby mode. If the standby
     server is promoted, it will start archiving after the promotion, but
     will not archive any WAL or timeline history files that
     it did not generate itself. To get a complete
     series of WAL files in the archive, you must ensure that all WAL is
     archived, before it reaches the standby. This is inherently true with
     file-based log shipping, as the standby can only restore files that
     are found in the archive, but not if streaming replication is enabled.
     When a server is not in recovery mode, there is no difference between
     on and always modes.
   

  

  

  

   
# Failover

   

    If the primary server fails then the standby server should begin
    failover procedures.
   

   

    If the standby server fails then no failover need take place. If the
    standby server can be restarted, even some time later, then the recovery
    process can also be restarted immediately, taking advantage of
    restartable recovery. If the standby server cannot be restarted, then a
    full new standby server instance should be created.
   

   

    If the primary server fails and the standby server becomes the
    new primary, and then the old primary restarts, you must have
    a mechanism for informing the old primary that it is no longer the primary. This is
    sometimes known as STONITH (Shoot The Other Node In The Head), which is
    necessary to avoid situations where both systems think they are the
    primary, which will lead to confusion and ultimately data loss.
   

   

    Many failover systems use just two systems, the primary and the standby,
    connected by some kind of heartbeat mechanism to continually verify the
    connectivity between the two and the viability of the primary. It is
    also possible to use a third system (called a witness server) to prevent
    some cases of inappropriate failover, but the additional complexity
    might not be worthwhile unless it is set up with sufficient care and
    rigorous testing.
   

   

    PostgreSQL does not provide the system
    software required to identify a failure on the primary and notify
    the standby database server.  Many such tools exist and are well
    integrated with the operating system facilities required for
    successful failover, such as IP address migration.
   

   

    Once failover to the standby occurs, there is only a
    single server in operation. This is known as a degenerate state.
    The former standby is now the primary, but the former primary is down
    and might stay down.  To return to normal operation, a standby server
    must be recreated,
    either on the former primary system when it comes up, or on a third,
    possibly new, system. The  utility can be
    used to speed up this process on large clusters.
    Once complete, the primary and standby can be
    considered to have switched roles. Some people choose to use a third
    server to provide backup for the new primary until the new standby
    server is recreated,
    though clearly this complicates the system configuration and
    operational processes.
   

   

    So, switching from primary to standby server can be fast but requires
    some time to re-prepare the failover cluster. Regular switching from
    primary to standby is useful, since it allows regular downtime on
    each system for maintenance. This also serves as a test of the
    failover mechanism to ensure that it will really work when you need it.
    Written administration procedures are advised.
   

   

    If you have opted for logical replication slot synchronization (see
    ),
    then before switching to the standby server, it is recommended to check
    if the logical slots synchronized on the standby server are ready
    for failover. This can be done by following the steps described in
    .
   

   

    To trigger failover of a log-shipping standby server, run
    pg_ctl promote or call pg_promote().
    If you're setting up reporting servers that are only used to offload
    read-only queries from the primary, not for high availability purposes,
    you don't need to promote.
   

  

 

  
# Hot Standby

  
   hot standby
  

   

    Hot standby is the term used to describe the ability to connect to
    the server and run read-only queries while the server is in archive
    recovery or standby mode. This
    is useful both for replication purposes and for restoring a backup
    to a desired state with great precision.
    The term hot standby also refers to the ability of the server to move
    from recovery through to normal operation while users continue running
    queries and/or keep their connections open.
   

   

    Running queries in hot standby mode is similar to normal query operation,
    though there are several usage and administrative differences
    explained below.
   

  

   
# User's Overview

   

    When the  parameter is set to true on a
    standby server, it will begin accepting connections once the recovery has
    brought the system to a consistent state and be ready for hot standby.
    All such connections are
    strictly read-only; not even temporary tables may be written.
   

   

    The data on the standby takes some time to arrive from the primary server
    so there will be a measurable delay between primary and standby. Running the
    same query nearly simultaneously on both primary and standby might therefore
    return differing results. We say that data on the standby is
    eventually consistent with the primary.  Once the
    commit record for a transaction is replayed on the standby, the changes
    made by that transaction will be visible to any new snapshots taken on
    the standby.  Snapshots may be taken at the start of each query or at the
    start of each transaction, depending on the current transaction isolation
    level.  For more details, see .
   

   

    Transactions started during hot standby may issue the following commands:

    

     

      

       Query access: SELECT, COPY TO
      

     

     

      

       Cursor commands: DECLARE, FETCH, CLOSE
      

     

     

      

       Settings: SHOW, SET, RESET
      

     

     

      

       Transaction management commands:
        

         

          

           BEGIN, END, ABORT, START TRANSACTION
          

         

         

          

           SAVEPOINT, RELEASE, ROLLBACK TO SAVEPOINT
          

         

         

          

           EXCEPTION blocks and other internal subtransactions
          

         

        

      

     

     

      

       LOCK TABLE, though only when explicitly in one of these modes:
       ACCESS SHARE, ROW SHARE or ROW EXCLUSIVE.
      

     

     

      

       Plans and resources: PREPARE, EXECUTE,
       DEALLOCATE, DISCARD
      

     

     

      

       Plugins and extensions: LOAD
      

     

     

      

       UNLISTEN
      

     

    

   

   

    Transactions started during hot standby will never be assigned a
    transaction ID and cannot write to the system write-ahead log.
    Therefore, the following actions will produce error messages:

    

     

      

       Data Manipulation Language (DML): INSERT,
       UPDATE, DELETE,
       MERGE, COPY FROM,
       TRUNCATE.
       Note that there are no allowed actions that result in a trigger
       being executed during recovery.  This restriction applies even to
       temporary tables, because table rows cannot be read or written without
       assigning a transaction ID, which is currently not possible in a
       hot standby environment.
      

     

     

      

       Data Definition Language (DDL): CREATE,
       DROP, ALTER, COMMENT.
       This restriction applies even to temporary tables, because carrying
       out these operations would require updating the system catalog tables.
      

     

     

      

       SELECT ... FOR SHARE | UPDATE, because row locks cannot be
       taken without updating the underlying data files.
      

     

     

      

       Rules on SELECT statements that generate DML commands.
      

     

     

      

       LOCK that explicitly requests a mode higher than ROW EXCLUSIVE MODE.
      

     

     

      

       LOCK in short default form, since it requests ACCESS EXCLUSIVE MODE.
      

     

     

      

       Transaction management commands that explicitly set non-read-only state:
        

         

          

            BEGIN READ WRITE,
            START TRANSACTION READ WRITE
          

         

         

          

            SET TRANSACTION READ WRITE,
            SET SESSION CHARACTERISTICS AS TRANSACTION READ WRITE
          

         

         

          

           SET transaction_read_only = off
          

         

        

      

     

     

      

       Two-phase commit commands: PREPARE TRANSACTION,
       COMMIT PREPARED, ROLLBACK PREPARED
       because even read-only transactions need to write WAL in the
       prepare phase (the first phase of two phase commit).
      

     

     

      

       Sequence updates: nextval(), setval()
      

     

     

      

       LISTEN, NOTIFY
      

     

    

   

   

    In normal operation, read-only transactions are allowed to
    use LISTEN and NOTIFY,
    so hot standby sessions operate under slightly tighter
    restrictions than ordinary read-only sessions.  It is possible that some
    of these restrictions might be loosened in a future release.
   

   

    During hot standby, the parameter transaction_read_only is always
    true and may not be changed.  But as long as no attempt is made to modify
    the database, connections during hot standby will act much like any other
    database connection.  If failover or switchover occurs, the database will
    switch to normal processing mode.  Sessions will remain connected while the
    server changes mode.  Once hot standby finishes, it will be possible to
    initiate read-write transactions (even from a session begun during
    hot standby).
   

   

    Users can determine whether hot standby is currently active for their
    session by issuing SHOW in_hot_standby.
    (In server versions before 14, the in_hot_standby
    parameter did not exist; a workable substitute method for older servers
    is SHOW transaction_read_only.)  In addition, a set of
    functions () and the
    
    pg_stat_recovery view allow users to
    access information about the standby server. These facilities allow you to
    write programs that are aware of the current state of the database. They
    can be used to monitor the progress of recovery, or to allow you to
    write complex programs that restore the database to particular states.
   

  

  

   
# Handling Query Conflicts

   

    The primary and standby servers are in many ways loosely connected. Actions
    on the primary will have an effect on the standby. As a result, there is
    potential for negative interactions or conflicts between them. The easiest
    conflict to understand is performance: if a huge data load is taking place
    on the primary then this will generate a similar stream of WAL records on the
    standby, so standby queries may contend for system resources, such as I/O.
   

   

    There are also additional types of conflict that can occur with hot standby.
    These conflicts are hard conflicts in the sense that queries
    might need to be canceled and, in some cases, sessions disconnected to resolve them.
    The user is provided with several ways to handle these
    conflicts. Conflict cases include:

      

       

        

         Access Exclusive locks taken on the primary server, including both
         explicit LOCK commands and various DDL
         actions, conflict with table accesses in standby queries.
        

       

       

        

         Dropping a tablespace on the primary conflicts with standby queries
         using that tablespace for temporary work files.
        

       

       

        

         Dropping a database on the primary conflicts with sessions connected
         to that database on the standby.
        

       

       

        

         Application of a vacuum cleanup record from WAL conflicts with
         standby transactions whose snapshots can still see any of
         the rows to be removed.
        

       

       

        

         Application of a vacuum cleanup record from WAL conflicts with
         queries accessing the target page on the standby, whether or not
         the data to be removed is visible.
        

       

      

   

   

    On the primary server, these cases simply result in waiting; and the
    user might choose to cancel either of the conflicting actions.  However,
    on the standby there is no choice: the WAL-logged action already occurred
    on the primary so the standby must not fail to apply it.  Furthermore,
    allowing WAL application to wait indefinitely may be very undesirable,
    because the standby's state will become increasingly far behind the
    primary's.  Therefore, a mechanism is provided to forcibly cancel standby
    queries that conflict with to-be-applied WAL records.
   

   

    An example of the problem situation is an administrator on the primary
    server running DROP TABLE on a table that is currently being
    queried on the standby server.  Clearly the standby query cannot continue
    if the DROP TABLE is applied on the standby. If this situation
    occurred on the primary, the DROP TABLE would wait until the
    other query had finished. But when DROP TABLE is run on the
    primary, the primary doesn't have information about what queries are
    running on the standby, so it will not wait for any such standby
    queries. The WAL change records come through to the standby while the
    standby query is still running, causing a conflict.  The standby server
    must either delay application of the WAL records (and everything after
    them, too) or else cancel the conflicting query so that the DROP
    TABLE can be applied.
   

   

    When a conflicting query is short, it's typically desirable to allow it to
    complete by delaying WAL application for a little bit; but a long delay in
    WAL application is usually not desirable.  So the cancel mechanism has
    parameters,  and , that define the maximum
    allowed delay in WAL application.  Conflicting queries will be canceled
    once it has taken longer than the relevant delay setting to apply any
    newly-received WAL data.  There are two parameters so that different delay
    values can be specified for the case of reading WAL data from an archive
    (i.e., initial recovery from a base backup or catching up a
    standby server that has fallen far behind) versus reading WAL data via
    streaming replication.
   

   

    In a standby server that exists primarily for high availability, it's
    best to set the delay parameters relatively short, so that the server
    cannot fall far behind the primary due to delays caused by standby
    queries.  However, if the standby server is meant for executing
    long-running queries, then a high or even infinite delay value may be
    preferable.  Keep in mind however that a long-running query could
    cause other sessions on the standby server to not see recent changes
    on the primary, if it delays application of WAL records.
   

   

    Once the delay specified by max_standby_archive_delay or
    max_standby_streaming_delay has been exceeded, conflicting
    queries will be canceled.  This usually results just in a cancellation
    error, although in the case of replaying a DROP DATABASE
    the entire conflicting session will be terminated.  Also, if the conflict
    is over a lock held by an idle transaction, the conflicting session is
    terminated (this behavior might change in the future).
   

   

    Canceled queries may be retried immediately (after beginning a new
    transaction, of course).  Since query cancellation depends on
    the nature of the WAL records being replayed, a query that was
    canceled may well succeed if it is executed again.
   

   

    Keep in mind that the delay parameters are compared to the elapsed time
    since the WAL data was received by the standby server.  Thus, the grace
    period allowed to any one query on the standby is never more than the
    delay parameter, and could be considerably less if the standby has already
    fallen behind as a result of waiting for previous queries to complete, or
    as a result of being unable to keep up with a heavy update load.
   

   

    The most common reason for conflict between standby queries and WAL replay
    is early cleanup.  Normally, PostgreSQL allows
    cleanup of old row versions when there are no transactions that need to
    see them to ensure correct visibility of data according to MVCC rules.
    However, this rule can only be applied for transactions executing on the
    primary.  So it is possible that cleanup on the primary will remove row
    versions that are still visible to a transaction on the standby.
   

   

    Row version cleanup isn't the only potential cause of conflicts with
    standby queries.  All index-only scans (including those that run on
    standbys) must use an MVCC snapshot that
    agrees with the visibility map.  Conflicts are therefore
    required whenever VACUUM sets a page as all-visible in the
     visibility map containing one or more rows
    not visible to all standby queries.  So even running
    VACUUM against a table with no updated or deleted rows
    requiring cleanup might lead to conflicts.
   

   

    Users should be clear that tables that are regularly and heavily updated
    on the primary server will quickly cause cancellation of longer running
    queries on the standby. In such cases the setting of a finite value for
    max_standby_archive_delay or
    max_standby_streaming_delay can be considered similar to
    setting statement_timeout.
   

   

    Remedial possibilities exist if the number of standby-query cancellations
    is found to be unacceptable.  The first option is to set the parameter
    hot_standby_feedback, which prevents VACUUM from
    removing recently-dead rows and so cleanup conflicts do not occur.
    If you do this, you
    should note that this will delay cleanup of dead rows on the primary,
    which may result in undesirable table bloat. However, the cleanup
    situation will be no worse than if the standby queries were running
    directly on the primary server, and you are still getting the benefit of
    off-loading execution onto the standby.
    If standby servers connect and disconnect frequently, you
    might want to make adjustments to handle the period when
    hot_standby_feedback feedback is not being provided.
    For example, consider increasing max_standby_archive_delay
    so that queries are not rapidly canceled by conflicts in WAL archive
    files during disconnected periods.  You should also consider increasing
    max_standby_streaming_delay to avoid rapid cancellations
    by newly-arrived streaming WAL entries after reconnection.
   

   

    The number of query cancels and the reason for them can be viewed using
    the pg_stat_database_conflicts system view on the standby
    server. The pg_stat_database system view also contains
    summary information.
   

   

    Users can control whether a log message is produced when WAL replay is waiting
    longer than deadlock_timeout for conflicts. This
    is controlled by the  parameter.
   

  

  

   
# Administrator's Overview

   

    If hot_standby is on in postgresql.conf
    (the default value) and there is a
    standby.signalstandby.signalfor hot standby
    file present, the server will run in hot standby mode.
    However, it may take some time for hot standby connections to be allowed,
    because the server will not accept connections until it has completed
    sufficient recovery to provide a consistent state against which queries
    can run.  During this period,
    clients that attempt to connect will be refused with an error message.
    To confirm the server has come up, either loop trying to connect from
    the application, or look for these messages in the server logs:

```
LOG:  entering standby mode

... then some time later ...

LOG:  consistent recovery state reached
LOG:  database system is ready to accept read-only connections
```

    Consistency information is recorded once per checkpoint on the primary.
    It is not possible to enable hot standby when reading WAL
    written during a period when wal_level was not set to
    replica or logical on the primary.
    Even after reaching a consistent state, the recovery snapshot may not
    be ready for hot standby if both of the following conditions are met,
    delaying accepting read-only connections.  To enable hot standby,
    long-lived write transactions with more than 64 subtransactions
    need to be closed on the primary.

      

       

        

         A write transaction has more than 64 subtransactions
        

       

       

        

         Very long-lived write transactions
        

       

      

    If you are running file-based log shipping ("warm standby"), you might need
    to wait until the next WAL file arrives, which could be as long as the
    archive_timeout setting on the primary.
   

   

    The settings of some parameters determine the size of shared memory for
    tracking transaction IDs, locks, and prepared transactions.  These shared
    memory structures must be no smaller on a standby than on the primary in
    order to ensure that the standby does not run out of shared memory during
    recovery.  For example, if the primary had used a prepared transaction but
    the standby had not allocated any shared memory for tracking prepared
    transactions, then recovery could not continue until the standby's
    configuration is changed.  The parameters affected are:

      

       

        

         max_connections
        

       

       

        

         max_prepared_transactions
        

       

       

        

         max_locks_per_transaction
        

       

       

        

         max_wal_senders
        

       

       

        

         max_worker_processes
        

       

      

    The easiest way to ensure this does not become a problem is to have these
    parameters set on the standbys to values equal to or greater than on the
    primary.  Therefore, if you want to increase these values, you should do
    so on all standby servers first, before applying the changes to the
    primary server.  Conversely, if you want to decrease these values, you
    should do so on the primary server first, before applying the changes to
    all standby servers.  Keep in mind that when a standby is promoted, it
    becomes the new reference for the required parameter settings for the
    standbys that follow it.  Therefore, to avoid this becoming a problem
    during a switchover or failover, it is recommended to keep these settings
    the same on all standby servers.
   

   

    The WAL tracks changes to these parameters on the
    primary.  If a hot standby processes WAL that indicates that the current
    value on the primary is higher than its own value, it will log a warning
    and pause recovery, for example:

```
WARNING:  hot standby is not possible because of insufficient parameter settings
DETAIL:  max_connections = 80 is a lower setting than on the primary server, where its value was 100.
LOG:  recovery has paused
DETAIL:  If recovery is unpaused, the server will shut down.
HINT:  You can then restart the server after making the necessary configuration changes.
```

    At that point, the settings on the standby need to be updated and the
    instance restarted before recovery can continue.  If the standby is not a
    hot standby, then when it encounters the incompatible parameter change, it
    will shut down immediately without pausing, since there is then no value
    in keeping it up.
   

   

    It is important that the administrator select appropriate settings for
     and .  The best choices vary
    depending on business priorities.  For example if the server is primarily
    tasked as a High Availability server, then you will want low delay
    settings, perhaps even zero, though that is a very aggressive setting. If
    the standby server is tasked as an additional server for decision support
    queries then it might be acceptable to set the maximum delay values to
    many hours, or even -1 which means wait forever for queries to complete.
   

   

    Transaction status "hint bits" written on the primary are not WAL-logged,
    so data on the standby will likely re-write the hints again on the standby.
    Thus, the standby server will still perform disk writes even though
    all users are read-only; no changes occur to the data values
    themselves.  Users will still write large sort temporary files and
    re-generate relcache info files, so no part of the database
    is truly read-only during hot standby mode.
    Note also that writes to remote databases using
    dblink module, and other operations outside the
    database using PL functions will still be possible, even though the
    transaction is read-only locally.
   

   

    The following types of administration commands are not accepted
    during recovery mode:

      

       

        

         Data Definition Language (DDL): e.g., CREATE INDEX
        

       

       

        

         Privilege and Ownership: GRANT, REVOKE,
         REASSIGN
        

       

       

        

         Maintenance commands: ANALYZE, VACUUM,
         CLUSTER, REINDEX
        

       

      

   

   

    Again, note that some of these commands are actually allowed during
    "read only" mode transactions on the primary.
   

   

    As a result, you cannot create additional indexes that exist solely
    on the standby, nor statistics that exist solely on the standby.
    If these administration commands are needed, they should be executed
    on the primary, and eventually those changes will propagate to the
    standby.
   

   

    pg_cancel_backend()
    and pg_terminate_backend() will work on user backends,
    but not the startup process, which performs
    recovery. pg_stat_activity does not show
    recovering transactions as active. As a result,
    pg_prepared_xacts is always empty during
    recovery. If you wish to resolve in-doubt prepared transactions, view
    pg_prepared_xacts on the primary and issue commands to
    resolve transactions there or resolve them after the end of recovery.
   

   

    pg_locks will show locks held by backends,
    as normal. pg_locks also shows
    a virtual transaction managed by the startup process that owns all
    AccessExclusiveLocks held by transactions being replayed by recovery.
    Note that the startup process does not acquire locks to
    make database changes, and thus locks other than AccessExclusiveLocks
    do not show in pg_locks for the Startup
    process; they are just presumed to exist.
   

   

    The Nagios plugin check_pgsql will
    work, because the simple information it checks for exists.
    The check_postgres monitoring script will also work,
    though some reported values could give different or confusing results.
    For example, last vacuum time will not be maintained, since no
    vacuum occurs on the standby.  Vacuums running on the primary
    do still send their changes to the standby.
   

   

    WAL file control commands will not work during recovery,
    e.g., pg_backup_start, pg_switch_wal etc.
   

   

    Dynamically loadable modules work, including pg_stat_statements.
   

   

    Advisory locks work normally in recovery, including deadlock detection.
    Note that advisory locks are never WAL logged, so it is impossible for
    an advisory lock on either the primary or the standby to conflict with WAL
    replay. Nor is it possible to acquire an advisory lock on the primary
    and have it initiate a similar advisory lock on the standby. Advisory
    locks relate only to the server on which they are acquired.
   

   

    Trigger-based replication systems such as Slony,
    Londiste and Bucardo won't run on the
    standby at all, though they will run happily on the primary server as
    long as the changes are not sent to standby servers to be applied.
    WAL replay is not trigger-based so you cannot relay from the
    standby to any system that requires additional database writes or
    relies on the use of triggers.
   

   

    New OIDs cannot be assigned, though some UUID generators may still
    work as long as they do not rely on writing new status to the database.
   

   

    Currently, temporary table creation is not allowed during read-only
    transactions, so in some cases existing scripts will not run correctly.
    This restriction might be relaxed in a later release. This is
    both an SQL standard compliance issue and a technical issue.
   

   

    DROP TABLESPACE can only succeed if the tablespace is empty.
    Some standby users may be actively using the tablespace via their
    temp_tablespaces parameter. If there are temporary files in the
    tablespace, all active queries are canceled to ensure that temporary
    files are removed, so the tablespace can be removed and WAL replay
    can continue.
   

   

    Running DROP DATABASE or ALTER DATABASE ... SET
    TABLESPACE on the primary
    will generate a WAL entry that will cause all users connected to that
    database on the standby to be forcibly disconnected. This action occurs
    immediately, whatever the setting of
    max_standby_streaming_delay. Note that
    ALTER DATABASE ... RENAME does not disconnect users, which
    in most cases will go unnoticed, though might in some cases cause a
    program confusion if it depends in some way upon database name.
   

   

    In normal (non-recovery) mode, if you issue DROP USER or DROP ROLE
    for a role with login capability while that user is still connected then
    nothing happens to the connected user &mdash; they remain connected. The user cannot
    reconnect however. This behavior applies in recovery also, so a
    DROP USER on the primary does not disconnect that user on the standby.
   

   

    The cumulative statistics system is active during recovery. All scans,
    reads, blocks, index usage, etc., will be recorded normally on the
    standby. However, WAL replay will not increment relation and database
    specific counters. I.e. replay will not increment
    pg_stat_all_tables columns (like n_tup_ins),
    nor will reads or writes performed by the startup process be tracked in the
    pg_statio_ views, nor will associated
    pg_stat_database columns be incremented.
   

   

    Autovacuum is not active during recovery.  It will start normally at the
    end of recovery.
   

   

    The checkpointer process and the background writer process are active during
    recovery. The checkpointer process will perform restartpoints (similar to
    checkpoints on the primary) and the background writer process will perform
    normal block cleaning activities. This can include updates of the hint bit
    information stored on the standby server.
    The CHECKPOINT command is accepted during recovery,
    though it performs a restartpoint rather than a new checkpoint.
   

  

  

   
# Hot Standby Parameter Reference

   

    Various parameters have been mentioned above in
     and
    .
   

   

    On the primary, the  parameter can be used.
     and
     have no effect if set on
    the primary.
   

   

    On the standby, parameters ,
     and
     can be used.
   

  

  

   
# Caveats

   

    There are several limitations of hot standby.
    These can and probably will be fixed in future releases:

  

   

    

     Full knowledge of running transactions is required before snapshots
     can be taken. Transactions that use large numbers of subtransactions
     (currently greater than 64) will delay the start of read-only
     connections until the completion of the longest running write transaction.
     If this situation occurs, explanatory messages will be sent to the server log.
    

   

   

    

     Valid starting points for standby queries are generated at each
     checkpoint on the primary. If the standby is shut down while the primary
     is in a shutdown state, it might not be possible to re-enter hot standby
     until the primary is started up, so that it generates further starting
     points in the WAL logs.  This situation isn't a problem in the most
     common situations where it might happen. Generally, if the primary is
     shut down and not available anymore, that's likely due to a serious
     failure that requires the standby being converted to operate as
     the new primary anyway.  And in situations where the primary is
     being intentionally taken down, coordinating to make sure the standby
     becomes the new primary smoothly is also standard procedure.
    

   

   

    

     At the end of recovery, AccessExclusiveLocks held by prepared transactions
     will require twice the normal number of lock table entries. If you plan
     on running either a large number of concurrent prepared transactions
     that normally take AccessExclusiveLocks, or you plan on having one
     large transaction that takes many AccessExclusiveLocks, you are
     advised to select a larger value of max_locks_per_transaction,
     perhaps as much as twice the value of the parameter on
     the primary server. You need not consider this at all if
     your setting of max_prepared_transactions is 0.
    

   

   

    

     The Serializable transaction isolation level is not yet available in hot
     standby.  (See  and
      for details.)
     An attempt to set a transaction to the serializable isolation level in
     hot standby mode will generate an error.
