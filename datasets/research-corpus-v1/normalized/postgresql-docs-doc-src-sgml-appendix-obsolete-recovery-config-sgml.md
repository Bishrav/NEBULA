# recovery.conf file merged into postgresql.conf

   
     recovery.conf
   

   

    PostgreSQL 11 and below used a configuration file named
    recovery.conf
    recovery.conf
    to manage replicas and standbys. Support for this file was removed in PostgreSQL 12. See
    the release notes for PostgreSQL 12 for details
    on this change.
   

   

    On PostgreSQL 12 and above,
    archive recovery, streaming replication, and PITR
    are configured using
    normal server configuration parameters.
    These are set in postgresql.conf or via
    ALTER SYSTEM
    like any other parameter.
   

   

    The server will not start if a recovery.conf exists.
   

   

    PostgreSQL 15 and below had a setting
    promote_trigger_file, or
    trigger_file before 12.
    Use pg_ctl promote or call
    pg_promote() to promote a standby instead.
   

   

    The
    standby_mode
    
     standby_mode
     standby.signal
    
    setting has been removed. A standby.signal file in the data directory
    is used instead. See  for details.
