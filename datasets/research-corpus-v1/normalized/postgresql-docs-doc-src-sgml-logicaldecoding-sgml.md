# Logical Decoding

  
   Logical Decoding
  
  

   PostgreSQL provides infrastructure to stream the modifications performed
   via SQL to external consumers.  This functionality can be used for a
   variety of purposes, including replication solutions and auditing.
  

  

   Changes are sent out in streams identified by logical replication slots.
  

  

   The format in which those changes are streamed is determined by the output
   plugin used.  An example plugin is provided in the PostgreSQL distribution.
   Additional plugins can be
   written to extend the choice of available formats without modifying any
   core code.
   Every output plugin has access to each individual new row produced
   by INSERT and the new row version created
   by UPDATE.  Availability of old row versions for
   UPDATE and DELETE depends on
   the configured replica identity (see ).
  

  

   Changes can be consumed either using the streaming replication protocol
   (see  and
   ), or by calling functions
   via SQL (see ). It is also possible
   to write additional methods of consuming the output of a replication slot
   without modifying core code
   (see ).
  

  

   
# Logical Decoding Examples

   

    The following example demonstrates controlling logical decoding using the
    SQL interface.
   

   

    Before you can use logical decoding, you must set
     to replica or higher and
     to at least 1.  Then, you
    should connect to the target database (in the example
    below, postgres) as a superuser.
   

```
postgres=# -- Create a slot named 'regression_slot' using the output plugin 'test_decoding'
postgres=# SELECT * FROM pg_create_logical_replication_slot('regression_slot', 'test_decoding', false, true);
    slot_name    |    lsn
-----------------+------------
 regression_slot | 0/016B1970
(1 row)

postgres=# SELECT slot_name, plugin, slot_type, database, active, restart_lsn, confirmed_flush_lsn FROM pg_replication_slots;
    slot_name    |    plugin     | slot_type | database | active | restart_lsn | confirmed_flush_lsn
-----------------+---------------+-----------+----------+--------+-------------+---------------------
 regression_slot | test_decoding | logical   | postgres | f      | 0/016A4408  | 0/016A4440
(1 row)

postgres=# -- There are no changes to see yet
postgres=# SELECT * FROM pg_logical_slot_get_changes('regression_slot', NULL, NULL);
 lsn | xid | data
-----+-----+------
(0 rows)

postgres=# CREATE TABLE data(id serial PRIMARY KEY, data text);
CREATE TABLE

postgres=# -- DDL isn't replicated, so all you'll see is the transaction
postgres=# SELECT * FROM pg_logical_slot_get_changes('regression_slot', NULL, NULL);
    lsn     |  xid  |     data
------------+-------+--------------
 0/0BA2DA58 | 10297 | BEGIN 10297
 0/0BA5A5A0 | 10297 | COMMIT 10297
(2 rows)

postgres=# -- Once changes are read, they're consumed and not emitted
postgres=# -- in a subsequent call:
postgres=# SELECT * FROM pg_logical_slot_get_changes('regression_slot', NULL, NULL);
 lsn | xid | data
-----+-----+------
(0 rows)

postgres=# BEGIN;
postgres=*# INSERT INTO data(data) VALUES('1');
postgres=*# INSERT INTO data(data) VALUES('2');
postgres=*# COMMIT;

postgres=# SELECT * FROM pg_logical_slot_get_changes('regression_slot', NULL, NULL);
    lsn     |  xid  |                          data
------------+-------+---------------------------------------------------------
 0/0BA5A688 | 10298 | BEGIN 10298
 0/0BA5A6F0 | 10298 | table public.data: INSERT: id[integer]:1 data[text]:'1'
 0/0BA5A7F8 | 10298 | table public.data: INSERT: id[integer]:2 data[text]:'2'
 0/0BA5A8A8 | 10298 | COMMIT 10298
(4 rows)

postgres=# INSERT INTO data(data) VALUES('3');

postgres=# -- You can also peek ahead in the change stream without consuming changes
postgres=# SELECT * FROM pg_logical_slot_peek_changes('regression_slot', NULL, NULL);
    lsn     |  xid  |                          data
------------+-------+---------------------------------------------------------
 0/0BA5A8E0 | 10299 | BEGIN 10299
 0/0BA5A8E0 | 10299 | table public.data: INSERT: id[integer]:3 data[text]:'3'
 0/0BA5A990 | 10299 | COMMIT 10299
(3 rows)

postgres=# -- The next call to pg_logical_slot_peek_changes() returns the same changes again
postgres=# SELECT * FROM pg_logical_slot_peek_changes('regression_slot', NULL, NULL);
    lsn     |  xid  |                          data
------------+-------+---------------------------------------------------------
 0/0BA5A8E0 | 10299 | BEGIN 10299
 0/0BA5A8E0 | 10299 | table public.data: INSERT: id[integer]:3 data[text]:'3'
 0/0BA5A990 | 10299 | COMMIT 10299
(3 rows)

postgres=# -- options can be passed to output plugin, to influence the formatting
postgres=# SELECT * FROM pg_logical_slot_peek_changes('regression_slot', NULL, NULL, 'include-timestamp', 'on');
    lsn     |  xid  |                          data
------------+-------+---------------------------------------------------------
 0/0BA5A8E0 | 10299 | BEGIN 10299
 0/0BA5A8E0 | 10299 | table public.data: INSERT: id[integer]:3 data[text]:'3'
 0/0BA5A990 | 10299 | COMMIT 10299 (at 2017-05-10 12:07:21.272494-04)
(3 rows)

postgres=# -- Remember to destroy a slot you no longer need to stop it consuming
postgres=# -- server resources:
postgres=# SELECT pg_drop_replication_slot('regression_slot');
 pg_drop_replication_slot
-----------------------

(1 row)
```

   

    The following examples show how logical decoding is controlled over the
    streaming replication protocol, using the
    program  included in the PostgreSQL
    distribution.  This requires that client authentication is set up to allow
    replication connections
    (see ) and
    that max_wal_senders is set sufficiently high to allow
    an additional connection.  The second example shows how to stream two-phase
    transactions.  Before you use two-phase commands, you must set
     to at least 1.
   

```
Example 1:
$ pg_recvlogical -d postgres --slot=test --create-slot
$ pg_recvlogical -d postgres --slot=test --start -f -
ControlZ
$ psql -d postgres -c "INSERT INTO data(data) VALUES('4');"
$ fg
BEGIN 693
table public.data: INSERT: id[integer]:4 data[text]:'4'
COMMIT 693
ControlC
$ pg_recvlogical -d postgres --slot=test --drop-slot

Example 2:
$ pg_recvlogical -d postgres --slot=test --create-slot --enable-two-phase
$ pg_recvlogical -d postgres --slot=test --start -f -
ControlZ
$ psql -d postgres -c "BEGIN;INSERT INTO data(data) VALUES('5');PREPARE TRANSACTION 'test';"
$ fg
BEGIN 694
table public.data: INSERT: id[integer]:5 data[text]:'5'
PREPARE TRANSACTION 'test', txid 694
ControlZ
$ psql -d postgres -c "COMMIT PREPARED 'test';"
$ fg
COMMIT PREPARED 'test', txid 694
ControlC
$ pg_recvlogical -d postgres --slot=test --drop-slot
```

  

  The following example shows SQL interface that can be used to decode prepared
  transactions. Before you use two-phase commit commands, you must set
  max_prepared_transactions to at least 1. You must also have
  set the two-phase parameter as 'true' while creating the slot using
  pg_create_logical_replication_slot
  Note that we will stream the entire transaction after the commit if it
  is not already decoded.
  

```
postgres=# BEGIN;
postgres=*# INSERT INTO data(data) VALUES('5');
postgres=*# PREPARE TRANSACTION 'test_prepared1';

postgres=# SELECT * FROM pg_logical_slot_get_changes('regression_slot', NULL, NULL);
    lsn     | xid |                          data
------------+-----+---------------------------------------------------------
 0/01689DC0 | 529 | BEGIN 529
 0/01689DC0 | 529 | table public.data: INSERT: id[integer]:3 data[text]:'5'
 0/01689FC0 | 529 | PREPARE TRANSACTION 'test_prepared1', txid 529
(3 rows)

postgres=# COMMIT PREPARED 'test_prepared1';
postgres=# SELECT * from pg_logical_slot_get_changes('regression_slot', NULL, NULL);
    lsn     | xid |                    data
------------+-----+--------------------------------------------
 0/0168A060 | 529 | COMMIT PREPARED 'test_prepared1', txid 529
(4 row)

postgres=#-- you can also rollback a prepared transaction
postgres=# BEGIN;
postgres=*# INSERT INTO data(data) VALUES('6');
postgres=*# PREPARE TRANSACTION 'test_prepared2';
postgres=# SELECT * from pg_logical_slot_get_changes('regression_slot', NULL, NULL);
    lsn     | xid |                          data
------------+-----+---------------------------------------------------------
 0/0168A180 | 530 | BEGIN 530
 0/0168A1E8 | 530 | table public.data: INSERT: id[integer]:4 data[text]:'6'
 0/0168A430 | 530 | PREPARE TRANSACTION 'test_prepared2', txid 530
(3 rows)

postgres=# ROLLBACK PREPARED 'test_prepared2';
postgres=# SELECT * from pg_logical_slot_get_changes('regression_slot', NULL, NULL);
    lsn     | xid |                     data
------------+-----+----------------------------------------------
 0/0168A4B8 | 530 | ROLLBACK PREPARED 'test_prepared2', txid 530
(1 row)
```

  

   
# Logical Decoding Concepts

   

    
# Logical Decoding

    
     Logical Decoding
    

    

     Logical decoding is the process of extracting all persistent changes
     to a database's tables into a coherent, easy to understand format which
     can be interpreted without detailed knowledge of the database's internal
     state.
    

    

     In PostgreSQL, logical decoding is implemented
     by decoding the contents of the write-ahead
     log, which describe changes on a storage level, into an
     application-specific form such as a stream of tuples or SQL statements.
    

    

     Logical decoding becomes available in two conditions:
    

    

     

      

       When  is set to logical.
      

     

     

      

       When  is set to replica
       and at least one valid logical replication slot exists on the system.
      

     

    

    

     If either condition is met, the operational WAL level becomes equivalent
     to logical, which can be monitored through the
      parameter.
    

    

     When wal_level is set to replica,
     logical decoding is automatically activated upon creation of the first
     logical replication slot. This activation process involves several steps
     and requires synchronization among processes, ensuring system-wide
     consistency. Conversely, if wal_level is set to
     replica and the last logical replication slot is dropped
     or invalidated, logical decoding is automatically disabled. Note that the
     deactivation of logical decoding might take some time as it is performed
     asynchronously by the checkpointer process.
    

    
     

      When wal_level is set to replica,
      dropping or invalidating the last logical slot disables logical decoding
      on the primary, resulting in slots on standbys being invalidated.
     

    
   

   

    
# Replication Slots

    
     replication slot
     logical replication
    

    

     In the context of logical replication, a slot represents a stream of
     changes that can be replayed to a client in the order they were made on
     the origin server. Each slot streams a sequence of changes from a single
     database.
    

    
     
PostgreSQL can also use streaming replication slots
     to maintain a standby server (see ), but
     typically those use physical replication, not logical.
     

    

    

     A replication slot has an identifier that is unique across all databases
     in a PostgreSQL cluster. Slots persist
     independently of the connection using them and are crash-safe.
    

    

     A logical slot will emit each change just once in normal operation.
     The current position of each slot is persisted only at checkpoint, so in
     the case of a crash the slot might return to an earlier LSN, which will
     then cause recent changes to be sent again when the server restarts.
     Logical decoding clients are responsible for avoiding ill effects from
     handling the same message more than once.  Clients may wish to record
     the last LSN they saw when decoding and skip over any repeated data or
     (when using the replication protocol) request that decoding start from
     that LSN rather than letting the server determine the start point.
     The Replication Progress Tracking feature is designed for this purpose,
     refer to replication origins.
    

    

     Multiple independent slots may exist for a single database. Each slot has
     its own state, allowing different consumers to receive changes from
     different points in the database change stream. For most applications, a
     separate slot will be required for each consumer.
    

    

     A logical replication slot knows nothing about the state of the
     receiver(s).  It's even possible to have multiple different receivers using
     the same slot at different times; they'll just get the changes following
     on from when the last receiver stopped consuming them. Only one receiver
     may consume changes from a slot at any given time.
    

    

     A logical replication slot can also be created on a hot standby. To prevent
     VACUUM from removing required rows from the system
     catalogs, hot_standby_feedback should be set on the
     standby. In spite of that, if any required rows get removed, the slot gets
     invalidated. It's highly recommended to use a physical slot between the
     primary and the standby. Otherwise, hot_standby_feedback
     will work but only while the connection is alive (for example a node
     restart would break it). Then, the primary may delete system catalog rows
     that could be needed by the logical decoding on the standby (as it does
     not know about the catalog_xmin on the standby).
     Existing logical slots on standby also get invalidated if
     effective_wal_level on the primary is reduced to less than
     logical.
     This is done as soon as the standby detects such a change in the WAL stream.
     It means that, for walsenders that are lagging (if any), some WAL records up
     to the wal_level parameter change on the primary won't be
     decoded.
    

    

     Creation of a logical slot requires information about all the currently
     running transactions. On the primary, this information is available
     directly, but on a standby, this information has to be obtained from
     primary. Thus, slot creation may need to wait for some activity to happen
     on the primary. If the primary is idle, creating a logical slot on
     standby may take noticeable time. This can be sped up by calling the
     pg_log_standby_snapshot function on the primary.
    

    
     

      Replication slots persist across crashes and know nothing about the state
      of their consumer(s). They will prevent removal of required resources
      even when there is no connection using them. This consumes storage
      because neither required WAL nor required rows from the system catalogs
      can be removed by VACUUM as long as they are required by a replication
      slot.  In extreme cases this could cause the database to shut down to prevent
      transaction ID wraparound (see ).
      So if a slot is no longer required it should be dropped.
     

    

   

   

    
# Replication Slot Synchronization

    

     The logical replication slots on the primary can be synchronized to
     the hot standby by using the failover parameter of
     
     pg_create_logical_replication_slot, or by
     using the 
     failover option of
     CREATE SUBSCRIPTION during slot creation.
     Additionally, enabling 
     sync_replication_slots on the standby
     is required. By enabling 
     sync_replication_slots
     on the standby, the failover slots can be synchronized periodically in
     the slotsync worker. For the synchronization to work, it is mandatory to
     have a physical replication slot between the primary and the standby (i.e.,
     primary_slot_name
     should be configured on the standby), and
     hot_standby_feedback
     must be enabled on the standby. It is also necessary to specify a valid
     dbname in the
     primary_conninfo.
     It's highly recommended that the said physical replication slot is named in
     synchronized_standby_slots
     list on the primary, to prevent the subscriber from consuming changes
     faster than the hot standby. Even when correctly configured, some latency
     is expected when sending changes to logical subscribers due to the waiting
     on slots named in
     synchronized_standby_slots.
     When synchronized_standby_slots is utilized, the
     primary server will not completely shut down until the corresponding
     standbys, associated with the physical replication slots specified
     in synchronized_standby_slots, have confirmed
     receiving the WAL up to the latest flushed position on the primary server.
    

    
     

      While enabling 
      sync_replication_slots allows for automatic
      periodic synchronization of failover slots, they can also be manually
      synchronized using the 
      pg_sync_replication_slots function on the standby.
      However, unlike automatic synchronization, it does not perform incremental
      updates. It retries cyclically until all the failover slots that existed on
      primary at the start of the function call are synchronized. Any slots created
      after the function begins will not be synchronized. In contrast, automatic
      synchronization via sync_replication_slots provides
      continuous slot updates, enabling seamless failover and supporting high
      availability. Therefore, it is the recommended method for synchronizing slots.
     

    

    

     When slot synchronization is configured as recommended,
     and the initial synchronization is performed either automatically or
     manually via pg_sync_replication_slots, the standby
     can persist the synchronized slot only if the following condition is met:
     The logical replication slot on the primary must retain WALs and system
     catalog rows that are still available on the standby. This ensures data
     integrity and allows logical replication to continue smoothly after
     promotion.
     If the required WALs or catalog rows have already been purged from the
     standby, the slot will not be persisted to avoid data loss. In such
     cases, the following log message may appear:

```
LOG:  could not synchronize replication slot "failover_slot"
DETAIL:  Synchronization could lead to data loss, because the remote slot needs WAL at LSN 0/03003F28 and catalog xmin 754, but the standby has LSN 0/03003F28 and catalog xmin 756.
```

     If the logical replication slot is actively used by a consumer, no
     manual intervention is needed; the slot will advance automatically,
     and synchronization will resume in the next cycle. However, if no
     consumer is configured, it is advisable to manually advance the slot
     on the primary using 
     pg_logical_slot_get_changes or
     
     pg_logical_slot_get_binary_changes,
     allowing synchronization to proceed.
    

    

     The ability to resume logical replication after failover depends upon the
     pg_replication_slots.synced
     value for the synchronized slots on the standby at the time of failover.
     Only persistent slots that have attained synced state as true on the standby
     before failover can be used for logical replication after failover.
     Temporary synced slots cannot be used for logical decoding, therefore
     logical replication for those slots cannot be resumed. For example, if the
     synchronized slot could not become persistent on the standby due to a
     disabled subscription, then the subscription cannot be resumed after
     failover even when it is enabled.
    

    

     To resume logical replication after failover from the synced logical
     slots, the subscription's 'conninfo' must be altered to point to the
     new primary server. This is done using
     ALTER SUBSCRIPTION ... CONNECTION.
     It is recommended that subscriptions are first disabled before promoting
     the standby and are re-enabled after altering the connection string.
    

    
     

      There is a chance that the old primary is up again during the promotion
      and if subscriptions are not disabled, the logical subscribers may
      continue to receive data from the old primary server even after promotion
      until the connection string is altered. This might result in data
      inconsistency issues, preventing the logical subscribers from being
      able to continue replication from the new primary server.
     

    
   

   

    
# Output Plugins

    

     Output plugins transform the data from the write-ahead log's internal
     representation into the format the consumer of a replication slot desires.
    

   

   

    
# Exported Snapshots

    

     When a new replication slot is created using the streaming replication
     interface (see ), a
     snapshot is exported
     (see ), which will show
     exactly the state of the database after which all changes will be
     included in the change stream. This can be used to create a new replica by
     using SET TRANSACTION
     SNAPSHOT to read the state of the database at the moment
     the slot was created. This transaction can then be used to dump the
     database's state at that point in time, which afterwards can be updated
     using the slot's contents without losing any changes.
    

    

     Applications that do not require
     snapshot export may suppress it with the SNAPSHOT 'nothing'
     option.
    

   

  

  

   
# Streaming Replication Protocol Interface

   

    The commands
    

     

      
CREATE_REPLICATION_SLOT slot_name LOGICAL output_plugin

     

     

      
DROP_REPLICATION_SLOT slot_name  WAIT 

     

     

      
START_REPLICATION SLOT slot_name LOGICAL ...

     

    

    are used to create, drop, and stream changes from a replication
    slot, respectively. These commands are only available over a replication
    connection; they cannot be used via SQL.
    See  for details on these commands.
   

   

    The command  can be used to control
    logical decoding over a streaming replication connection.  (It uses
    these commands internally.)
   

  

  

   
# Logical Decoding SQL Interface

   

     See  for detailed documentation on
     the SQL-level API for interacting with logical decoding.
   

   

    Synchronous replication (see ) is
    only supported on replication slots used over the streaming replication interface. The
    function interface and additional, non-core interfaces do not support
    synchronous replication.
   

  

  

   
# System Catalogs Related to Logical Decoding

   

    The pg_replication_slots
    view and the
    
    pg_stat_replication
    view provide information about the current state of replication slots and
    streaming replication connections respectively. These views apply to both physical and
    logical replication. The
    
    pg_stat_replication_slots
    view provides statistics information about the logical replication slots.
   

  

  

   
# Logical Decoding Output Plugins

   

    PostgreSQL provides two logical decoding
    output plugins,  and
    .  You can also develop custom output plugins
    (see  for details).
   

   

    
# pgoutput &mdash; Standard Logical Decoding Output Plugin

    
     pgoutput
    

    

     pgoutput is the standard logical decoding output
     plugin provided by PostgreSQL.
     It's used for the built-in 
     logical replication.
    

    

     
# Options

     

      

       
proto_version (integer)

       

        

         Specifies the protocol version.
         Currently versions 1, 2,
         3, and 4 are supported.  A valid
         version is required.
        

        

         Version 2 is supported on server version 14
         and above, and is required when streaming
         is set to on to stream large in-progress
         transactions.
        

        

         Version 3 is supported on server version 15
         and above, and is required when two_phase
         is enabled to stream two-phase commits.
        

        

         Version 4 is supported on server version 16
         and above, and is required when streaming
         is set to parallel to stream large in-progress
         transactions to be applied in parallel.
        

       

      

      

       
publication_names (string)

       

        

         A comma-separated list of publication names to subscribe to.
         The individual publication names are treated
         as standard objects names and can be quoted the same as needed.
         At least one publication name is required.
        

       

      

      

       
binary (boolean)

       

        

         Enables binary transfer mode.  Binary mode is faster
         than the text mode but slightly less robust.
         The default is off.
        

       

      

      

       
messages (boolean)

       

        

         Enables sending the messages that are written by
         pg_logical_emit_message.
         The default is off.
        

       

      

      

       
streaming (enum)

       

        

         Enables streaming of in-progress transactions. Valid values are
         off (the default), on and
         parallel.
        

        

         When set to off, pgoutput
         fully decodes a transaction before sending it as a whole.
         This mode works with any protocol version.
        

        

         When set to on, pgoutput
         streams large in-progress transactions.
         This requires protocol version 2 or higher.
        

        

         When set to parallel, pgoutput
         streams large in-progress transactions and also sends
         extra information in some messages to support parallel processing.
         This requires protocol version 4 or higher.
        

       

      

      

       
two_phase (boolean)

       

        

         Enables sending two-phase transactions.
         Minimum protocol version 3 is required to turn it on.
         The default is off.
        

       

      

      

       
origin (enum)

       

        

         Specifies whether to send changes by their origin.  Possible values are
         none to only send the changes that have no origin
         associated, or any
         to send the changes regardless of their origin.  This can be used
         to avoid loops (infinite replication of the same data) among
         replication nodes.
         The default is any.
        

       

      

     

    

    

     
# Notes

     

      pgoutput produces binary output,
      so functions expecting textual data (
      pg_logical_slot_peek_changes and
      pg_logical_slot_get_changes)
      cannot be used with it.  Use
      pg_logical_slot_peek_binary_changes or
      pg_logical_slot_get_binary_changes
      instead.
     

    

   

  

  

   
# Writing Logical Decoding Output Plugins

   

    An example output plugin can be found in the
    
     contrib/test_decoding
    
    subdirectory of the PostgreSQL source tree.
   

   

    
# Initialization Function

    
     _PG_output_plugin_init
    
    

     An output plugin is loaded by dynamically loading a shared library with
     the output plugin's name as the library base name. The normal library
     search path is used to locate the library. To provide the required output
     plugin callbacks and to indicate that the library is actually an output
     plugin it needs to provide a function named
     _PG_output_plugin_init. This function is passed a
     struct that needs to be filled with the callback function pointers for
     individual actions.

```
typedef struct OutputPluginCallbacks
{
    LogicalDecodeStartupCB startup_cb;
    LogicalDecodeBeginCB begin_cb;
    LogicalDecodeChangeCB change_cb;
    LogicalDecodeTruncateCB truncate_cb;
    LogicalDecodeCommitCB commit_cb;
    LogicalDecodeMessageCB message_cb;
    LogicalDecodeFilterByOriginCB filter_by_origin_cb;
    LogicalDecodeShutdownCB shutdown_cb;
    LogicalDecodeFilterPrepareCB filter_prepare_cb;
    LogicalDecodeBeginPrepareCB begin_prepare_cb;
    LogicalDecodePrepareCB prepare_cb;
    LogicalDecodeCommitPreparedCB commit_prepared_cb;
    LogicalDecodeRollbackPreparedCB rollback_prepared_cb;
    LogicalDecodeStreamStartCB stream_start_cb;
    LogicalDecodeStreamStopCB stream_stop_cb;
    LogicalDecodeStreamAbortCB stream_abort_cb;
    LogicalDecodeStreamPrepareCB stream_prepare_cb;
    LogicalDecodeStreamCommitCB stream_commit_cb;
    LogicalDecodeStreamChangeCB stream_change_cb;
    LogicalDecodeStreamMessageCB stream_message_cb;
    LogicalDecodeStreamTruncateCB stream_truncate_cb;
} OutputPluginCallbacks;

typedef void (*LogicalOutputPluginInit) (struct OutputPluginCallbacks *cb);
```

     The begin_cb, change_cb
     and commit_cb callbacks are required,
     while startup_cb, truncate_cb,
     message_cb, filter_by_origin_cb,
     and shutdown_cb are optional.
     If truncate_cb is not set but a
     TRUNCATE is to be decoded, the action will be ignored.
    

    

     An output plugin may also define functions to support streaming of large,
     in-progress transactions. The stream_start_cb,
     stream_stop_cb, stream_abort_cb,
     stream_commit_cb, and stream_change_cb
     are required, while stream_message_cb and
     stream_truncate_cb are optional. The
     stream_prepare_cb is also required if the output
     plugin also support two-phase commits.
    

    

    An output plugin may also define functions to support two-phase commits,
    which allows actions to be decoded on the PREPARE TRANSACTION.
    The begin_prepare_cb, prepare_cb,
    commit_prepared_cb and rollback_prepared_cb
    callbacks are required, while filter_prepare_cb is optional.
    The stream_prepare_cb is also required if the output plugin
    also supports the streaming of large in-progress transactions.
    

   

   

    
# Capabilities

    

     To decode, format and output changes, output plugins can use most of the
     backend's normal infrastructure, including calling output functions. Read
     only access to relations is permitted as long as only relations are
     accessed that either have been created by initdb in
     the pg_catalog schema, or have been marked as user
     provided catalog tables using

```
ALTER TABLE user_catalog_table SET (user_catalog_table = true);
CREATE TABLE another_catalog_table(data text) WITH (user_catalog_table = true);
```

     Note that access to user catalog tables or regular system catalog tables
     in the output plugins has to be done via the systable_*
     scan APIs only. Access via the heap_* scan APIs will
     error out. Additionally, any actions leading to transaction ID assignment
     are prohibited. That, among others, includes writing to tables, performing
     DDL changes, and calling pg_current_xact_id().
    

   

   

    
# Output Modes

    

     Output plugin callbacks can pass data to the consumer in nearly arbitrary
     formats. For some use cases, like viewing the changes via SQL, returning
     data in a data type that can contain arbitrary data (e.g., bytea) is
     cumbersome. If the output plugin only outputs textual data in the
     server's encoding, it can declare that by
     setting OutputPluginOptions.output_type
     to OUTPUT_PLUGIN_TEXTUAL_OUTPUT instead
     of OUTPUT_PLUGIN_BINARY_OUTPUT in
     the startup
     callback. In that case, all the data has to be in the server's encoding
     so that a text datum can contain it. This is checked in assertion-enabled
     builds.
    

   

   

    
# Output Plugin Callbacks

    

     An output plugin gets notified about changes that are happening via
     various callbacks it needs to provide.
    

    

     Concurrent transactions are decoded in commit order, and only changes
     belonging to a specific transaction are decoded between
     the begin and commit
     callbacks. Transactions that were rolled back explicitly or implicitly
     never get
     decoded. Successful savepoints are
     folded into the transaction containing them in the order they were
     executed within that transaction. A transaction that is prepared for
     a two-phase commit using PREPARE TRANSACTION will
     also be decoded if the output plugin callbacks needed for decoding
     them are provided. It is possible that the current prepared transaction
     which is being decoded is aborted concurrently via a
     ROLLBACK PREPARED command. In that case, the logical
     decoding of this transaction will be aborted too. All the changes of such
     a transaction are skipped once the abort is detected and the
     prepare_cb callback is invoked. Thus even in case of
     a concurrent abort, enough information is provided to the output plugin
     for it to properly deal with ROLLBACK PREPARED once
     that is decoded.
    

    
     

      Only transactions that have already safely been flushed to disk will be
      decoded. That can lead to a COMMIT not immediately being decoded in a
      directly following pg_logical_slot_get_changes()
      when synchronous_commit is set
      to off.
     

    

    

     
# Startup Callback

     

      The optional startup_cb callback is called whenever
      a replication slot is created or asked to stream changes, independent
      of the number of changes that are ready to be put out.

```
typedef void (*LogicalDecodeStartupCB) (struct LogicalDecodingContext *ctx,
                                        OutputPluginOptions *options,
                                        bool is_init);
```

      The is_init parameter will be true when the
      replication slot is being created and false
      otherwise. 
options
 points to a struct of options
      that output plugins can set:

```
typedef struct OutputPluginOptions
{
    OutputPluginOutputType output_type;
    bool        receive_rewrites;
} OutputPluginOptions;
```

      output_type has to either be set to
      OUTPUT_PLUGIN_TEXTUAL_OUTPUT
      or OUTPUT_PLUGIN_BINARY_OUTPUT. See also
      .
      If receive_rewrites is true, the output plugin will
      also be called for changes made by heap rewrites during certain DDL
      operations.  These are of interest to plugins that handle DDL
      replication, but they require special handling.
     

     

      The startup callback should validate the options present in
      ctx->output_plugin_options. If the output plugin
      needs to have a state, it can
      use ctx->output_plugin_private to store it.
     

    

    

     
# Shutdown Callback

     

      The optional shutdown_cb callback is called
      whenever a formerly active replication slot is not used anymore and can
      be used to deallocate resources private to the output plugin. The slot
      isn't necessarily being dropped, streaming is just being stopped.

```
typedef void (*LogicalDecodeShutdownCB) (struct LogicalDecodingContext *ctx);
```

     

    

    

     
# Transaction Begin Callback

     

      The required begin_cb callback is called whenever a
      start of a committed transaction has been decoded. Aborted transactions
      and their contents never get decoded.

```
typedef void (*LogicalDecodeBeginCB) (struct LogicalDecodingContext *ctx,
                                      ReorderBufferTXN *txn);
```

      The 
txn
 parameter contains meta information about
      the transaction, like the time stamp at which it has been committed and
      its XID.
     

    

    

     
# Transaction End Callback

     

      The required commit_cb callback is called whenever
      a transaction commit has been
      decoded. The change_cb callbacks for all modified
      rows will have been called before this, if there have been any modified
      rows.

```
typedef void (*LogicalDecodeCommitCB) (struct LogicalDecodingContext *ctx,
                                       ReorderBufferTXN *txn,
                                       XLogRecPtr commit_lsn);
```

     

    

    

     
# Change Callback

     

      The required change_cb callback is called for every
      individual row modification inside a transaction, may it be
      an INSERT, UPDATE,
      or DELETE. Even if the original command modified
      several rows at once the callback will be called individually for each
      row. The change_cb callback may access system or
      user catalog tables to aid in the process of outputting the row
      modification details. In case of decoding a prepared (but yet
      uncommitted) transaction or decoding of an uncommitted transaction, this
      change callback might also error out due to simultaneous rollback of
      this very same transaction. In that case, the logical decoding of this
      aborted transaction is stopped gracefully.

```
typedef void (*LogicalDecodeChangeCB) (struct LogicalDecodingContext *ctx,
                                       ReorderBufferTXN *txn,
                                       Relation relation,
                                       ReorderBufferChange *change);
```

      The 
ctx
 and 
txn
 parameters
      have the same contents as for the begin_cb
      and commit_cb callbacks, but additionally the
      relation descriptor 
relation
 points to the
      relation the row belongs to and a struct
      
change
 describing the row modification are passed
      in.
     

     
      

       Only changes in user defined tables that are not unlogged
       (see ) and not temporary
       (see ) can be extracted using
       logical decoding.
      

     
    

    

     
# Truncate Callback

     

      The optional truncate_cb callback is called for a
      TRUNCATE command.

```
typedef void (*LogicalDecodeTruncateCB) (struct LogicalDecodingContext *ctx,
                                         ReorderBufferTXN *txn,
                                         int nrelations,
                                         Relation relations[],
                                         ReorderBufferChange *change);
```

      The parameters are analogous to the change_cb
      callback.  However, because TRUNCATE actions on
      tables connected by foreign keys need to be executed together, this
      callback receives an array of relations instead of just a single one.
      See the description of the  statement for
      details.
     

    

     

     
# Origin Filter Callback

     

       The optional filter_by_origin_cb callback
       is called to determine whether data that has been replayed
       from 
origin_id
 is of interest to the
       output plugin.

```
typedef bool (*LogicalDecodeFilterByOriginCB) (struct LogicalDecodingContext *ctx,
                                               ReplOriginId origin_id);
```

      The 
ctx
 parameter has the same contents
      as for the other callbacks. No information but the origin is
      available. To signal that changes originating on the passed in
      node are irrelevant, return true, causing them to be filtered
      away; false otherwise. The other callbacks will not be called
      for transactions and changes that have been filtered away.
     

     

       This is useful when implementing cascading or multidirectional
       replication solutions. Filtering by the origin allows to
       prevent replicating the same changes back and forth in such
       setups.  While transactions and changes also carry information
       about the origin, filtering via this callback is noticeably
       more efficient.
     

     

    

     
# Generic Message Callback

     

      The optional message_cb callback is called whenever
      a logical decoding message has been decoded.

```
typedef void (*LogicalDecodeMessageCB) (struct LogicalDecodingContext *ctx,
                                        ReorderBufferTXN *txn,
                                        XLogRecPtr message_lsn,
                                        bool transactional,
                                        const char *prefix,
                                        Size message_size,
                                        const char *message);
```

      The 
txn
 parameter contains meta information about
      the transaction, like the time stamp at which it has been committed and
      its XID. Note however that it can be NULL when the message is
      non-transactional and the XID was not assigned yet in the transaction
      which logged the message. The 
lsn
 has WAL
      location of the message. The 
transactional
 says
      if the message was sent as transactional or not. Similar to the change
      callback, in case of decoding a prepared (but yet uncommitted)
      transaction or decoding of an uncommitted transaction, this message
      callback might also error out due to simultaneous rollback of
      this very same transaction. In that case, the logical decoding of this
      aborted transaction is stopped gracefully.
      The 
prefix
 is arbitrary null-terminated prefix
      which can be used for identifying interesting messages for the current
      plugin. And finally the 
message
 parameter holds
      the actual message of 
message_size
 size.
     

     

      Extra care should be taken to ensure that the prefix the output plugin
      considers interesting is unique. Using name of the extension or the
      output plugin itself is often a good choice.
     

    

    

     
# Prepare Filter Callback

     

       The optional filter_prepare_cb callback
       is called to determine whether data that is part of the current
       two-phase commit transaction should be considered for decoding
       at this prepare stage or later as a regular one-phase transaction at
       COMMIT PREPARED time. To signal that
       decoding should be skipped, return true;
       false otherwise. When the callback is not
       defined, false is assumed (i.e. no filtering, all
       transactions using two-phase commit are decoded in two phases as well).

```
typedef bool (*LogicalDecodeFilterPrepareCB) (struct LogicalDecodingContext *ctx,
                                              TransactionId xid,
                                              const char *gid);
```

       The 
ctx
 parameter has the same contents as for
       the other callbacks. The parameters 
xid

       and 
gid
 provide two different ways to identify
       the transaction.  The later COMMIT PREPARED or
       ROLLBACK PREPARED carries both identifiers,
       providing an output plugin the choice of what to use.
     

     

       The callback may be invoked multiple times per transaction to decode
       and must provide the same static answer for a given pair of
       
xid
 and 
gid
 every time
       it is called.
     

     

    

     
# Transaction Begin Prepare Callback

     

      The required begin_prepare_cb callback is called
      whenever the start of a prepared transaction has been decoded. The
      
gid
 field, which is part of the
      
txn
 parameter, can be used in this callback to
      check if the plugin has already received this PREPARE
      in which case it can either error out or skip the remaining changes of
      the transaction.

```
typedef void (*LogicalDecodeBeginPrepareCB) (struct LogicalDecodingContext *ctx,
                                             ReorderBufferTXN *txn);
```

     

    

    

     
# Transaction Prepare Callback

     

      The required prepare_cb callback is called whenever
      a transaction which is prepared for two-phase commit has been
      decoded. The change_cb callback for all modified
      rows will have been called before this, if there have been any modified
      rows. The 
gid
 field, which is part of the
      
txn
 parameter, can be used in this callback.

```
typedef void (*LogicalDecodePrepareCB) (struct LogicalDecodingContext *ctx,
                                        ReorderBufferTXN *txn,
                                        XLogRecPtr prepare_lsn);
```

     

    

    

     
# Transaction Commit Prepared Callback

     

      The required commit_prepared_cb callback is called
      whenever a transaction COMMIT PREPARED has been decoded.
      The 
gid
 field, which is part of the
      
txn
 parameter, can be used in this callback.

```
typedef void (*LogicalDecodeCommitPreparedCB) (struct LogicalDecodingContext *ctx,
                                               ReorderBufferTXN *txn,
                                               XLogRecPtr commit_lsn);
```

     

    

    

     
# Transaction Rollback Prepared Callback

     

      The required rollback_prepared_cb callback is called
      whenever a transaction ROLLBACK PREPARED has been
      decoded. The 
gid
 field, which is part of the
      
txn
 parameter, can be used in this callback. The
      parameters 
prepare_end_lsn
 and
      
prepare_time
 can be used to check if the plugin
      has received this PREPARE TRANSACTION in which case
      it can apply the rollback, otherwise, it can skip the rollback operation. The
      
gid
 alone is not sufficient because the downstream
      node can have a prepared transaction with same identifier.

```
typedef void (*LogicalDecodeRollbackPreparedCB) (struct LogicalDecodingContext *ctx,
                                                 ReorderBufferTXN *txn,
                                                 XLogRecPtr prepare_end_lsn,
                                                 TimestampTz prepare_time);
```

     

    

    

     
# Stream Start Callback

     

      The required stream_start_cb callback is called when
      opening a block of streamed changes from an in-progress transaction.

```
typedef void (*LogicalDecodeStreamStartCB) (struct LogicalDecodingContext *ctx,
                                            ReorderBufferTXN *txn);
```

     

    

    

     
# Stream Stop Callback

     

      The required stream_stop_cb callback is called when
      closing a block of streamed changes from an in-progress transaction.

```
typedef void (*LogicalDecodeStreamStopCB) (struct LogicalDecodingContext *ctx,
                                           ReorderBufferTXN *txn);
```

     

    

    

     
# Stream Abort Callback

     

      The required stream_abort_cb callback is called to
      abort a previously streamed transaction.

```
typedef void (*LogicalDecodeStreamAbortCB) (struct LogicalDecodingContext *ctx,
                                            ReorderBufferTXN *txn,
                                            XLogRecPtr abort_lsn);
```

     

    

    

     
# Stream Prepare Callback

     

      The stream_prepare_cb callback is called to prepare
      a previously streamed transaction as part of a two-phase commit. This
      callback is required when the output plugin supports both the streaming
      of large in-progress transactions and two-phase commits.
      
```
typedef void (*LogicalDecodeStreamPrepareCB) (struct LogicalDecodingContext *ctx,
                                              ReorderBufferTXN *txn,
                                              XLogRecPtr prepare_lsn);
```

     

    

    

     
# Stream Commit Callback

     

      The required stream_commit_cb callback is called to
      commit a previously streamed transaction.

```
typedef void (*LogicalDecodeStreamCommitCB) (struct LogicalDecodingContext *ctx,
                                             ReorderBufferTXN *txn,
                                             XLogRecPtr commit_lsn);
```

     

    

    

     
# Stream Change Callback

     

      The required stream_change_cb callback is called
      when sending a change in a block of streamed changes (demarcated by
      stream_start_cb and stream_stop_cb calls).
      The actual changes are not displayed as the transaction can abort at a later
      point in time and we don't decode changes for aborted transactions.

```
typedef void (*LogicalDecodeStreamChangeCB) (struct LogicalDecodingContext *ctx,
                                             ReorderBufferTXN *txn,
                                             Relation relation,
                                             ReorderBufferChange *change);
```

     

    

    

     
# Stream Message Callback

     

      The optional stream_message_cb callback is called when
      sending a generic message in a block of streamed changes (demarcated by
      stream_start_cb and stream_stop_cb calls).
      The message contents for transactional messages are not displayed as the transaction
      can abort at a later point in time and we don't decode changes for aborted
      transactions.

```
typedef void (*LogicalDecodeStreamMessageCB) (struct LogicalDecodingContext *ctx,
                                              ReorderBufferTXN *txn,
                                              XLogRecPtr message_lsn,
                                              bool transactional,
                                              const char *prefix,
                                              Size message_size,
                                              const char *message);
```

     

    

    

     
# Stream Truncate Callback

     

      The optional stream_truncate_cb callback is called
      for a TRUNCATE command in a block of streamed changes
      (demarcated by stream_start_cb and
      stream_stop_cb calls).

```
typedef void (*LogicalDecodeStreamTruncateCB) (struct LogicalDecodingContext *ctx,
                                               ReorderBufferTXN *txn,
                                               int nrelations,
                                               Relation relations[],
                                               ReorderBufferChange *change);
```

      The parameters are analogous to the stream_change_cb
      callback.  However, because TRUNCATE actions on
      tables connected by foreign keys need to be executed together, this
      callback receives an array of relations instead of just a single one.
      See the description of the  statement for
      details.
     

    

   

   

    
# Functions for Producing Output

    

     To actually produce output, output plugins can write data to
     the StringInfo output buffer
     in ctx->out when inside
     the begin_cb, commit_cb,
     or change_cb callbacks. Before writing to the output
     buffer, OutputPluginPrepareWrite(ctx, last_write) has
     to be called, and after finishing writing to the
     buffer, OutputPluginWrite(ctx, last_write) has to be
     called to perform the write. The 
last_write

     indicates whether a particular write was the callback's last write.
    

    

     The following example shows how to output data to the consumer of an
     output plugin:

```
OutputPluginPrepareWrite(ctx, true);
appendStringInfo(ctx->out, "BEGIN %u", txn->xid);
OutputPluginWrite(ctx, true);
```

    

   

  

  

   
# Logical Decoding Output Writers

   

    It is possible to add more output methods for logical decoding.
    For details, see
    src/backend/replication/logical/logicalfuncs.c.
    Essentially, three functions need to be provided: one to read WAL, one to
    prepare writing output, and one to write the output
    (see ).
   

  

  

   
# Synchronous Replication Support for Logical Decoding

   

    
# Overview

    

     Logical decoding can be used to build
     synchronous
     replication solutions with the same user interface as synchronous
     replication for streaming
     replication.  To do this, the streaming replication interface
     (see ) must be used to stream out
     data. Clients have to send Standby status update (F)
     (see ) messages, just like streaming
     replication clients do.
    

    
     

      A synchronous replica receiving changes via logical decoding will work in
      the scope of a single database. Since, in contrast to
      that, 
synchronous_standby_names
 currently is
      server wide, this means this technique will not work properly if more
      than one database is actively used.
     

    
   

   

    
# Caveats

    

     In synchronous replication setup, a deadlock can happen, if the transaction
     has locked [user] catalog tables exclusively. See
      for information on user
     catalog tables. This is because logical decoding of transactions can lock
     catalog tables to access them. To avoid this users must refrain from taking
     an exclusive lock on [user] catalog tables. This can happen in the following
     ways:

     

      

       

        Issuing an explicit LOCK on pg_class
        in a transaction.
       

      

      

       

        Perform CLUSTER on pg_class in
        a transaction.
       

      

      

       

        PREPARE TRANSACTION after LOCK command
        on pg_class and allow logical decoding of two-phase
        transactions.
       

      

      

       

        PREPARE TRANSACTION after CLUSTER
        command on pg_trigger and allow logical decoding of
        two-phase transactions. This will lead to deadlock only when published table
        have a trigger.
       

      

      

       

        Executing TRUNCATE on [user] catalog table in a
        transaction.
       

      

     

     Note that these commands can cause deadlocks not only for the system
     catalog tables listed above but for other catalog tables.
    

   

  

  

   
# Streaming of Large Transactions for Logical Decoding

   

    The basic output plugin callbacks (e.g., begin_cb,
    change_cb, commit_cb and
    message_cb) are only invoked when the transaction
    actually commits. The changes are still decoded from the transaction
    log, but are only passed to the output plugin at commit (and discarded
    if the transaction aborts).
   

   

    This means that while the decoding happens incrementally, and may spill
    to disk to keep memory usage under control, all the decoded changes have
    to be transmitted when the transaction finally commits (or more precisely,
    when the commit is decoded from the transaction log). Depending on the
    size of the transaction and network bandwidth, the transfer time may
    significantly increase the apply lag.
   

   

    To reduce the apply lag caused by large transactions, an output plugin
    may provide additional callback to support incremental streaming of
    in-progress transactions. There are multiple required streaming callbacks
    (stream_start_cb, stream_stop_cb,
    stream_abort_cb, stream_commit_cb
    and stream_change_cb) and two optional callbacks
    (stream_message_cb and stream_truncate_cb).
    Also, if streaming of two-phase commands is to be supported, then additional
    callbacks must be provided. (See 
    for details).
   

   

    When streaming an in-progress transaction, the changes (and messages) are
    streamed in blocks demarcated by stream_start_cb
    and stream_stop_cb callbacks. Once all the decoded
    changes are transmitted, the transaction can be committed using the
    stream_commit_cb callback
    (or possibly aborted using the stream_abort_cb callback).
    If two-phase commits are supported, the transaction can be prepared using the
    stream_prepare_cb callback,
    COMMIT PREPARED using the
    commit_prepared_cb callback or aborted using the
    rollback_prepared_cb.
   

   

    One example sequence of streaming callback calls for one transaction may
    look like this:

```
stream_start_cb(...);   <-- start of first block of changes
  stream_change_cb(...);
  stream_change_cb(...);
  stream_message_cb(...);
  stream_change_cb(...);
  ...
  stream_change_cb(...);
stream_stop_cb(...);    <-- end of first block of changes

stream_start_cb(...);   <-- start of second block of changes
  stream_change_cb(...);
  stream_change_cb(...);
  stream_change_cb(...);
  ...
  stream_message_cb(...);
  stream_change_cb(...);
stream_stop_cb(...);    <-- end of second block of changes

[a. when using normal commit]
stream_commit_cb(...);    <-- commit of the streamed transaction

[b. when using two-phase commit]
stream_prepare_cb(...);   <-- prepare the streamed transaction
commit_prepared_cb(...);  <-- commit of the prepared transaction
```

   

   

    The actual sequence of callback calls may be more complicated, of course.
    There may be blocks for multiple streamed transactions, some of the
    transactions may get aborted, etc.
   

   

    Similar to spill-to-disk behavior, streaming is triggered when the total
    amount of changes decoded from the WAL (for all in-progress transactions)
    exceeds the limit defined by logical_decoding_work_mem setting.
    At that point, the largest top-level transaction (measured by the amount of memory
    currently used for decoded changes) is selected and streamed.  However, in
    some cases we still have to spill to disk even if streaming is enabled
    because we exceed the memory threshold but still have not decoded the
    complete tuple e.g., only decoded TOAST table insert but not the main table
    insert.
   

   

    Even when streaming large transactions, the changes are still applied in
    commit order, preserving the same guarantees as the non-streaming mode.
   

  

  

   
# Two-phase Commit Support for Logical Decoding

   

    With the basic output plugin callbacks (eg., begin_cb,
    change_cb, commit_cb and
    message_cb) two-phase commit commands like
    PREPARE TRANSACTION, COMMIT PREPARED
    and ROLLBACK PREPARED are not decoded. While the
    PREPARE TRANSACTION is ignored,
    COMMIT PREPARED is decoded as a COMMIT
    and ROLLBACK PREPARED is decoded as a
    ROLLBACK.
   

   

    To support the streaming of two-phase commands, an output plugin needs to
    provide additional callbacks. There are multiple two-phase commit callbacks
    that are required, (begin_prepare_cb,
    prepare_cb, commit_prepared_cb,
    rollback_prepared_cb and
    stream_prepare_cb) and an optional callback
    (filter_prepare_cb).
   

   

    If the output plugin callbacks for decoding two-phase commit commands are
    provided, then on PREPARE TRANSACTION, the changes of
    that transaction are decoded, passed to the output plugin, and the
    prepare_cb callback is invoked. This differs from the
    basic decoding setup where changes are only passed to the output plugin
    when a transaction is committed. The start of a prepared transaction is
    indicated by the begin_prepare_cb callback.
   

   

    When a prepared transaction is rolled back using the
    ROLLBACK PREPARED, then the
    rollback_prepared_cb callback is invoked and when the
    prepared transaction is committed using COMMIT PREPARED,
    then the commit_prepared_cb callback is invoked.
   

   

    Optionally the output plugin can define filtering rules via
    filter_prepare_cb to decode only specific transaction
    in two phases.  This can be achieved by pattern matching on the
    
gid
 or via lookups using the
    
xid
.
   

   

    The users that want to decode prepared transactions need to be careful about
    below mentioned points:

    

     

      

       If the prepared transaction has locked [user] catalog tables exclusively
       then decoding prepare can block till the main transaction is committed.
      

     

     

      

       The logical replication solution that builds distributed two phase commit
       using this feature can deadlock if the prepared transaction has locked
       [user] catalog tables exclusively. To avoid this users must refrain from
       having locks on catalog tables (e.g. explicit LOCK command)
       in such transactions.
       See  for the details.
