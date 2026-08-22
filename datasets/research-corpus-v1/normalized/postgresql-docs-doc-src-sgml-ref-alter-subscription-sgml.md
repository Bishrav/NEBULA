ALTER SUBSCRIPTION
 

 
  ALTER SUBSCRIPTION
  7
  SQL - Language Statements
 

 
  ALTER SUBSCRIPTION
  change the definition of a subscription
 

 

ALTER SUBSCRIPTION name SERVER server_name
ALTER SUBSCRIPTION name CONNECTION 'conninfo'
ALTER SUBSCRIPTION name SET PUBLICATION publication_name [, ...] [ WITH ( publication_option [= value] [, ... ] ) ]
ALTER SUBSCRIPTION name ADD PUBLICATION publication_name [, ...] [ WITH ( publication_option [= value] [, ... ] ) ]
ALTER SUBSCRIPTION name DROP PUBLICATION publication_name [, ...] [ WITH ( publication_option [= value] [, ... ] ) ]
ALTER SUBSCRIPTION name REFRESH PUBLICATION [ WITH ( refresh_option [= value] [, ... ] ) ]
ALTER SUBSCRIPTION name REFRESH SEQUENCES
ALTER SUBSCRIPTION name ENABLE
ALTER SUBSCRIPTION name DISABLE
ALTER SUBSCRIPTION name SET ( subscription_parameter [= value] [, ... ] )
ALTER SUBSCRIPTION name SKIP ( skip_option = value )
ALTER SUBSCRIPTION name OWNER TO { new_owner | CURRENT_ROLE | CURRENT_USER | SESSION_USER }
ALTER SUBSCRIPTION name RENAME TO new_name

 

 
  
# Description

  

   ALTER SUBSCRIPTION can change most of the subscription
   properties that can be specified
   in .
  

  

   You must own the subscription to use ALTER SUBSCRIPTION.
   To rename a subscription or alter the owner, you must have
   CREATE permission on the database. In addition,
   to alter the owner, you must be able to SET ROLE to the
   new owning role. If the subscription has
   password_required=false, only superusers can modify it.
  

  

   When refreshing a publication we remove the relations that are no longer
   part of the publication and we also remove the table synchronization slots
   if there are any. It is necessary to remove these slots so that the resources
   allocated for the subscription on the remote host are released. If due to
   network breakdown or some other error, PostgreSQL
   is unable to remove the slots, an error will be reported. To proceed in this
   situation, the user either needs to retry the operation or disassociate the
   slot from the subscription and drop the subscription as explained in
   .
  

  

   Commands ALTER SUBSCRIPTION ... REFRESH PUBLICATION,
   ALTER SUBSCRIPTION ... {SET|ADD|DROP} PUBLICATION ...
   with refresh option as true,
   ALTER SUBSCRIPTION ... SET (failover = true|false) and
   ALTER SUBSCRIPTION ... SET (two_phase = false)
   cannot be executed inside a transaction block.
  

  

   Commands ALTER SUBSCRIPTION ... REFRESH PUBLICATION and
   ALTER SUBSCRIPTION ... {SET|ADD|DROP} PUBLICATION ...
   with refresh option as true also cannot
   be executed when the subscription has
   two_phase
   commit enabled, unless
   copy_data
   is false. See column subtwophasestate
   of pg_subscription
   to know the actual two-phase state.
  

 

 
  
# Parameters

  

   

    
name

    

     

      The name of a subscription whose properties are to be altered.
     

    

   

   

    
SERVER server_name

    

     

      This clause replaces the foreign server or connection string originally
      set by  with the foreign server
      server_name.
     

    

   

   

    
CONNECTION 'conninfo'

    

     

      This clause replaces the foreign server or connection string originally
      set by  with the connection
      string conninfo.
     

    

   

   

    
SET PUBLICATION publication_name

    
ADD PUBLICATION publication_name

    
DROP PUBLICATION publication_name

    

     

      These forms change the list of subscribed publications.
      SET
      replaces the entire list of publications with a new list,
      ADD adds additional publications to the list of
      publications, and DROP removes the publications from
      the list of publications.  We allow non-existent publications to be
      specified in ADD and SET variants
      so that users can add those later.  See 
      for more information.  By default, this command will also act like
      REFRESH PUBLICATION.
     

     

      publication_option specifies additional
      options for this operation.  The supported options are:

      

       

        
refresh (boolean)

        

         

          When false, the command will not try to refresh
          table and sequence information. REFRESH PUBLICATION
          should then be executed separately. The default is
          true.
         

        

       

      

      Additionally, the options described under
      REFRESH PUBLICATION may be specified, to control the
      implicit refresh operation.
     

    

   

   

    
REFRESH PUBLICATION

    

     

      Fetch missing table and sequence information from the publisher.  This will start
      replication of tables that were added to the subscribed-to publications
      since 
      CREATE SUBSCRIPTION or
      the last invocation of REFRESH PUBLICATION.
     

     

      The system catalog pg_subscription_rel
      is updated to record all tables and sequences known to the subscription,
      that are still part of the publication.
     

     

      refresh_option specifies additional options for the
      refresh operation.  The supported options are:

      

       

        
copy_data (boolean)

        

         

          Specifies whether to copy pre-existing data for tables and synchronize
          sequences in the publications that are being subscribed to when the replication
          starts. The default is true.
         

         

          Previously subscribed tables are not copied, even if a table's row
          filter WHERE clause has since been modified.
         

         

          Previously subscribed sequences are not re-synchronized. To do that,
          use 
          ALTER SUBSCRIPTION ... REFRESH SEQUENCES.
         

         

          See  for recommendations on how
          to handle any warnings about sequence definition differences between
          the publisher and the subscriber, which might occur when
          copy_data = true.
         

         

          See  for details of
          how copy_data = true can interact with the
          origin
          parameter.
         

         

          See the
          binary
          parameter of CREATE SUBSCRIPTION for details about
          copying pre-existing data in binary format.
         

        

       

      

    

   

   

    
REFRESH SEQUENCES

    

     

      Re-synchronize sequence data with the publisher. Unlike
      
      ALTER SUBSCRIPTION ... REFRESH PUBLICATION,
      which synchronizes the subscription's set of sequences with the
      publication (adding new and removing dropped sequences),
      REFRESH SEQUENCES will re-synchronize the sequence
      data for all currently subscribed sequences without changing which
      sequences are subscribed. Run REFRESH PUBLICATION
      first if the publication's set of sequences has changed.
     

     
      

       Sequence replication requires the publisher to be running
       PostgreSQL 19 or later.
      

     
     

      See  for
      recommendations on how to handle any warnings about sequence definition
      differences between the publisher and the subscriber.
     

     

      See  for recommendations on how to
      identify and handle out-of-sync sequences.
     

    

   

   

    
ENABLE

    

     

      Enables a previously disabled subscription, starting the logical
      replication worker at the end of the transaction.
     

    

   

   

    
DISABLE

    

     

      Disables a running subscription, stopping the logical replication
      worker at the end of the transaction.
     

    

   

   

    
SET ( subscription_parameter [= value] [, ... ] )

    

     

      This clause alters parameters originally set by
      .  See there for more
      information.  The parameters that can be altered are
      slot_name,
      synchronous_commit,
      binary,
      streaming,
      disable_on_error,
      password_required,
      run_as_owner,
      origin,
      failover,
      two_phase,
      retain_dead_tuples,
      max_retention_duration,
      wal_receiver_timeout, and
      conflict_log_destination.
      Only a superuser can set password_required = false.
     

     

      When altering the
      slot_name,
      the failover and two_phase property
      values of the named slot may differ from the counterpart
      failover
      and two_phase
      parameters specified in the subscription. When creating the slot, ensure
      the slot properties failover and two_phase
      match their counterpart parameters of the subscription.
      Otherwise, the slot on the publisher may behave differently from what these
      subscription options say: for example, the slot on the publisher could either be
      synced to the standbys even when the subscription's
      failover
      option is disabled or could be disabled for sync
      even when the subscription's
      failover
      option is enabled.
     

     

      The failover,
      two_phase, and
      retain_dead_tuples
      parameters can only be altered when the subscription is disabled.
     

     

      When altering two_phase
      from true to false, the backend
      process reports an error if any prepared transactions done by the
      logical replication worker (from when two_phase
      parameter was still true) are found. You can resolve
      prepared transactions on the publisher node, or manually roll them back
      on the subscriber, and then try again. The transactions prepared by
      logical replication worker corresponding to a particular subscription have
      the following pattern: pg_gid_%u_%u
      (parameters: subscription 
oid
, remote transaction id 
xid
).
      To resolve such transactions manually, you need to roll back all
      the prepared transactions with corresponding subscription IDs in their
      names. Applications can check
      pg_prepared_xacts
      to find the required prepared transactions. After the two_phase
      option is changed from true to false,
      the publisher will replicate the transactions again when they are committed.
     

     

      If the retain_dead_tuples
      option is altered to false and no other subscription
      has this option enabled, the replication slot named
      pg_conflict_detection, created to retain
      dead tuples for conflict detection, will be dropped.
     

     

      When the conflict_log_destination
      parameter is set to table or all, the system
      automatically creates the conflict log table. Conversely, if the destination is changed to
      log, the conflict log table is automatically dropped.
     

    

   

   

    
SKIP ( skip_option = value )

    

     

      Skips applying all changes of the remote transaction.  If incoming data
      violates any constraints, logical replication will stop until it is
      resolved.  By using the ALTER SUBSCRIPTION ... SKIP command,
      the logical replication worker skips all data modification changes within
      the transaction.  This option has no effect on the transactions that are
      already prepared by enabling
      two_phase
      on the subscriber.
      After the logical replication worker successfully skips the transaction or
      finishes a transaction, the LSN (stored in
      pg_subscription.subskiplsn)
      is cleared.  See  for
      the details of logical replication conflicts.
     

     

      skip_option specifies options for this operation.
      The supported option is:

      

       

        
lsn (pg_lsn)

        

         

          Specifies the finish LSN of the remote transaction whose changes
          are to be skipped by the logical replication worker.  The finish LSN
          is the LSN at which the transaction is either committed or prepared.
          Skipping individual subtransactions is not supported.  Setting
          NONE resets the LSN.
         

        

       

      

    

   

   

    
new_owner

    

     

      The user name of the new owner of the subscription.
     

    

   

   

    
new_name

    

     

      The new name for the subscription.
     

    

   

  

  

   When specifying a parameter of type boolean, the
   = value
   part can be omitted, which is equivalent to
   specifying TRUE.
  

 

 
  
# Examples

  

   Change the publication subscribed by a subscription to
   insert_only:

```
ALTER SUBSCRIPTION mysub SET PUBLICATION insert_only;
```

  

  

   Disable (stop) the subscription:

```
ALTER SUBSCRIPTION mysub DISABLE;
```

 

 
  
# Compatibility

  

   ALTER SUBSCRIPTION is a PostgreSQL
   extension.
  

 

 
  
# See Also
