# Concurrency Control

  
   concurrency
  

  

   This chapter describes the behavior of the
   PostgreSQL database system when two or
   more sessions try to access the same data at the same time.  The
   goals in that situation are to allow efficient access for all
   sessions while maintaining strict data integrity.  Every developer
   of database applications should be familiar with the topics covered
   in this chapter.
  

  

   
# Introduction

   
    Multiversion Concurrency Control
   

   
    MVCC
   

   
    Serializable Snapshot Isolation
   

   
    SSI
   

   

    PostgreSQL provides a rich set of tools
    for developers to manage concurrent access to data.  Internally,
    data consistency is maintained by using a multiversion
    model (Multiversion Concurrency Control, MVCC).
    This means that each SQL statement sees
    a snapshot of data (a database version)
    as it was some
    time ago, regardless of the current state of the underlying data.
    This prevents statements from viewing inconsistent data produced
    by concurrent transactions performing updates on the same
    data rows, providing transaction isolation
    for each database session.  MVCC, by eschewing
    the locking methodologies of traditional database systems,
    minimizes lock contention in order to allow for reasonable
    performance in multiuser environments.
   

   

    The main advantage of using the MVCC model of
    concurrency control rather than locking is that in
    MVCC locks acquired for querying (reading) data
    do not conflict with locks acquired for writing data, and so
    reading never blocks writing and writing never blocks reading.
    PostgreSQL maintains this guarantee
    even when providing the strictest level of transaction
    isolation through the use of an innovative Serializable
    Snapshot Isolation (SSI) level.
   

   

    Table- and row-level locking facilities are also available in
    PostgreSQL for applications which don't
    generally need full transaction isolation and prefer to explicitly
    manage particular points of conflict.  However, proper
    use of MVCC will generally provide better
    performance than locks.  In addition, application-defined advisory
    locks provide a mechanism for acquiring locks that are not tied
    to a single transaction.
   

  

  

   
# Transaction Isolation

   
    transaction isolation
   

   

    The SQL standard defines four levels of
    transaction isolation.  The most strict is Serializable,
    which is defined by the standard in a paragraph which says that any
    concurrent execution of a set of Serializable transactions is guaranteed
    to produce the same effect as running them one at a time in some order.
    The other three levels are defined in terms of phenomena, resulting from
    interaction between concurrent transactions, which must not occur at
    each level.  The standard notes that due to the definition of
    Serializable, none of these phenomena are possible at that level.  (This
    is hardly surprising -- if the effect of the transactions must be
    consistent with having been run one at a time, how could you see any
    phenomena caused by interactions?)
   

   

    The phenomena which are prohibited at various levels are:

    

     

      

       dirty read
       dirty read
      

      

       

        A transaction reads data written by a concurrent uncommitted transaction.
       

      

     

     

      

       nonrepeatable read
       nonrepeatable read
      

      

       

        A transaction re-reads data it has previously read and finds that data
        has been modified by another transaction (that committed since the
        initial read).
       

      

     

     

      

       phantom read
       phantom read
      

      

       

        A transaction re-executes a query returning a set of rows that satisfy a
        search condition and finds that the set of rows satisfying the condition
        has changed due to another recently-committed transaction.
       

      

     

     

      

       serialization anomaly
       serialization anomaly
      

      

       

        The result of successfully committing a group of transactions
        is inconsistent with all possible orderings of running those
        transactions one at a time.
       

      

     

    

   

   

    
     transaction isolation level
    
    The SQL standard and PostgreSQL-implemented transaction isolation levels
    are described in .
   

    
     
# Transaction Isolation Levels

     
      
       
        
         Isolation Level
        
        
         Dirty Read
        
        
         Nonrepeatable Read
        
        
         Phantom Read
        
        
         Serialization Anomaly
        
       
      
      
       
        
         Read uncommitted
        
        
         Allowed, but not in PG
        
        
         Possible
        
        
         Possible
        
        
         Possible
        
       

       
        
         Read committed
        
        
         Not possible
        
        
         Possible
        
        
         Possible
        
        
         Possible
        
       

       
        
         Repeatable read
        
        
         Not possible
        
        
         Not possible
        
        
         Allowed, but not in PG
        
        
         Possible
        
       

       
        
         Serializable
        
        
         Not possible
        
        
         Not possible
        
        
         Not possible
        
        
         Not possible
        
       
      
     
    

   

    In PostgreSQL, you can request any of
    the four standard transaction isolation levels, but internally only
    three distinct isolation levels are implemented, i.e., PostgreSQL's
    Read Uncommitted mode behaves like Read Committed.  This is because
    it is the only sensible way to map the standard isolation levels to
    PostgreSQL's multiversion concurrency control architecture.
   

   

    The table also shows that PostgreSQL's Repeatable Read implementation
    does not allow phantom reads.  This is acceptable under the SQL
    standard because the standard specifies which anomalies must
    not occur at certain isolation levels;  higher
    guarantees are acceptable.
    The behavior of the available isolation levels is detailed in the
    following subsections.
   

   

    To set the transaction isolation level of a transaction, use the
    command .
   

   
     

       Some PostgreSQL data types and functions have
       special rules regarding transactional behavior.  In particular, changes
       made to a sequence (and therefore the counter of a
       column declared using serial) are immediately visible
       to all other transactions and are not rolled back if the transaction
       that made the changes aborts.  See 
       and .
     

   

  

   
# Read Committed Isolation Level

   
    transaction isolation level
    read committed
   

   
    read committed
   

   

    Read Committed is the default isolation
    level in PostgreSQL.  When a transaction
    uses this isolation level, a SELECT query
    (without a FOR UPDATE/SHARE clause) sees only data
    committed before the query began; it never sees either uncommitted
    data or changes committed by concurrent transactions during the query's
    execution.  In effect, a SELECT query sees
    a snapshot of the database as of the instant the query begins to
    run.   However, SELECT does see the effects
    of previous updates executed within its own transaction, even
    though they are not yet committed.  Also note that two successive
    SELECT commands can see different data, even
    though they are within a single transaction, if other transactions
    commit changes after the first SELECT starts and
    before the second SELECT starts.
   

   

    UPDATE, DELETE, SELECT
    FOR UPDATE, and SELECT FOR SHARE commands
    behave the same as SELECT
    in terms of searching for target rows: they will only find target rows
    that were committed as of the command start time.  However, such a target
    row might have already been updated (or deleted or locked) by
    another concurrent transaction by the time it is found.  In this case, the
    would-be updater will wait for the first updating transaction to commit or
    roll back (if it is still in progress).  If the first updater rolls back,
    then its effects are negated and the second updater can proceed with
    updating the originally found row.  If the first updater commits, the
    second updater will ignore the row if the first updater deleted it,
    otherwise it will attempt to apply its operation to the updated version of
    the row.  The search condition of the command (the WHERE clause) is
    re-evaluated to see if the updated version of the row still matches the
    search condition.  If so, the second updater proceeds with its operation
    using the updated version of the row.  In the case of
    SELECT FOR UPDATE and SELECT FOR
    SHARE, this means it is the updated version of the row that is
    locked and returned to the client.
   

   

    INSERT with an ON CONFLICT DO UPDATE clause
    behaves similarly. In Read Committed mode, each row proposed for insertion
    will either insert or update. Unless there are unrelated errors, one of
    those two outcomes is guaranteed.  If a conflict originates in another
    transaction whose effects are not yet visible to the INSERT,
    the UPDATE clause will affect that row,
    even though possibly no version of that row is
    conventionally visible to the command.
   

   

    INSERT with an ON CONFLICT DO
    SELECT clause behaves similarly to ON CONFLICT DO
    UPDATE.  In Read Committed mode, each row proposed for insertion
    is guaranteed to either insert or return the conflicting row (unless there are
    unrelated errors).  If a conflict originates in another transaction whose
    effects are not yet visible to the INSERT, the command
    will wait for that transaction to commit or roll back, then return the
    conflicting row if it was committed (even though that row was not visible
    when the command started).
   

   

    INSERT with an ON CONFLICT DO
    NOTHING clause may have insertion not proceed for a row due to
    the outcome of another transaction whose effects are not visible
    to the INSERT snapshot.  Again, this is only
    the case in Read Committed mode.
   

   

    MERGE allows the user to specify various
    combinations of INSERT, UPDATE
    and DELETE subcommands. A MERGE
    command with both INSERT and UPDATE
    subcommands looks similar to INSERT with an
    ON CONFLICT DO UPDATE clause but does not
    guarantee that either INSERT or
    UPDATE will occur.
    If MERGE attempts an UPDATE or
    DELETE and the row is concurrently updated but
    the join condition still passes for the current target and the
    current source tuple, then MERGE will behave
    the same as the UPDATE or
    DELETE commands and perform its action on the
    updated version of the row.  However, because MERGE
    can specify several actions and they can be conditional, the
    conditions for each action are re-evaluated on the updated version of
    the row, starting from the first action, even if the action that had
    originally matched appears later in the list of actions.
    On the other hand, if the row is concurrently updated so that the join
    condition fails, then MERGE will evaluate the
    command's NOT MATCHED BY SOURCE and
    NOT MATCHED [BY TARGET] actions next, and execute
    the first one of each kind that succeeds.
    If the row is concurrently deleted, then MERGE
    will evaluate the command's NOT MATCHED [BY TARGET]
    actions, and execute the first one that succeeds.
    If MERGE attempts an INSERT
    and a unique index is present and a duplicate row is concurrently
    inserted, then a uniqueness violation error is raised;
    MERGE does not attempt to avoid such
    errors by restarting evaluation of MATCHED
    conditions.
   

   

    Because of the above rules, it is possible for an updating command to see
    an inconsistent snapshot: it can see the effects of concurrent updating
    commands on the same rows it is trying to update, but it
    does not see effects of those commands on other rows in the database.
    This behavior makes Read Committed mode unsuitable for commands that
    involve complex search conditions; however, it is just right for simpler
    cases.  For example, consider transferring $100 from one account
    to another:

```
BEGIN;
UPDATE accounts SET balance = balance + 100.00 WHERE acctnum = 12345;
UPDATE accounts SET balance = balance - 100.00 WHERE acctnum = 7534;
COMMIT;
```

    If another transaction concurrently tries to change the balance of account
    7534, we clearly want the second statement to start with the updated
    version of the account's row.  Because each command is affecting only a
    predetermined row, letting it see the updated version of the row does
    not create any troublesome inconsistency.
   

   

    More complex usage can produce undesirable results in Read Committed
    mode.  For example, consider a DELETE command
    operating on data that is being both added and removed from its
    restriction criteria by another command, e.g., assume
    website is a two-row table with
    website.hits equaling 9 and
    10:

```
BEGIN;
UPDATE website SET hits = hits + 1;
-- run from another session:  DELETE FROM website WHERE hits = 10;
COMMIT;
```

    The DELETE will have no effect even though
    there is a website.hits = 10 row before and
    after the UPDATE. This occurs because the
    pre-update row value 9 is skipped, and when the
    UPDATE completes and DELETE
    obtains a lock, the new row value is no longer 10 but
    11, which no longer matches the criteria.
   

   

    Because Read Committed mode starts each command with a new snapshot
    that includes all transactions committed up to that instant,
    subsequent commands in the same transaction will see the effects
    of the committed concurrent transaction in any case.  The point
    at issue above is whether or not a single command
    sees an absolutely consistent view of the database.
   

   

    The partial transaction isolation provided by Read Committed mode
    is adequate for many applications, and this mode is fast and simple
    to use;  however, it is not sufficient for all cases.  Applications
    that do complex queries and updates might require a more rigorously
    consistent view of the database than Read Committed mode provides.
   

  

  

   
# Repeatable Read Isolation Level

   
    transaction isolation level
    repeatable read
   

   
    repeatable read
   

   

    The Repeatable Read isolation level only sees
    data committed before the transaction began; it never sees either
    uncommitted data or changes committed by concurrent transactions during
    the transaction's execution.  (However, each query does see the
    effects of previous updates executed within its own transaction,
    even though they are not yet committed.)  This is a stronger
    guarantee than is required by the SQL standard
    for this isolation level, and prevents all of the phenomena described
    in  except for serialization
    anomalies.  As mentioned above, this is
    specifically allowed by the standard, which only describes the
    minimum protections each isolation level must
    provide.
   

   

    This level is different from Read Committed in that a query in a
    repeatable read transaction sees a snapshot as of the start of the
    first non-transaction-control statement in the
    transaction, not as of the start
    of the current statement within the transaction.  Thus, successive
    SELECT commands within a single
    transaction see the same data, i.e., they do not see changes made by
    other transactions that committed after their own transaction started.
   

   

    Applications using this level must be prepared to retry transactions
    due to serialization failures.
   

   

    UPDATE, DELETE,
    MERGE, SELECT FOR UPDATE,
    and SELECT FOR SHARE commands
    behave the same as SELECT
    in terms of searching for target rows: they will only find target rows
    that were committed as of the transaction start time.  However, such a
    target row might have already been updated (or deleted or locked) by
    another concurrent transaction by the time it is found.  In this case, the
    repeatable read transaction will wait for the first updating transaction to commit or
    roll back (if it is still in progress).  If the first updater rolls back,
    then its effects are negated and the repeatable read transaction can proceed
    with updating the originally found row.  But if the first updater commits
    (and actually updated or deleted the row, not just locked it)
    then the repeatable read transaction will be rolled back with the message

```
ERROR:  could not serialize access due to concurrent update
```

    because a repeatable read transaction cannot modify or lock rows changed by
    other transactions after the repeatable read transaction began.
   

   

    When an application receives this error message, it should abort
    the current transaction and retry the whole transaction from
    the beginning.  The second time through, the transaction will see the
    previously-committed change as part of its initial view of the database,
    so there is no logical conflict in using the new version of the row
    as the starting point for the new transaction's update.
   

   

    Note that only updating transactions might need to be retried; read-only
    transactions will never have serialization conflicts.
   

   

    The Repeatable Read mode provides a rigorous guarantee that each
    transaction sees a completely stable view of the database.  However,
    this view will not necessarily always be consistent with some serial
    (one at a time) execution of concurrent transactions of the same level.
    For example, even a read-only transaction at this level may see a
    control record updated to show that a batch has been completed but
    not see one of the detail records which is logically
    part of the batch because it read an earlier revision of the control
    record.  Attempts to enforce business rules by transactions running at
    this isolation level are not likely to work correctly without careful use
    of explicit locks to block conflicting transactions.
   

   

    The Repeatable Read isolation level is implemented using a technique
    known in academic database literature and in some other database products
    as Snapshot Isolation.  Differences in behavior
    and performance may be observed when compared with systems that use a
    traditional locking technique that reduces concurrency.  Some other
    systems may even offer Repeatable Read and Snapshot Isolation as distinct
    isolation levels with different behavior.  The permitted phenomena that
    distinguish the two techniques were not formalized by database researchers
    until after the SQL standard was developed, and are outside the scope of
    this manual.  For a full treatment, please see
    .
   

   
    

     Prior to PostgreSQL version 9.1, a request
     for the Serializable transaction isolation level provided exactly the
     same behavior described here.  To retain the legacy Serializable
     behavior, Repeatable Read should now be requested.
    

   
  

  

   
# Serializable Isolation Level

   
    transaction isolation level
    serializable
   

   
    serializable
   

   
    predicate locking
   

   
    serialization anomaly
   

   

    The Serializable isolation level provides
    the strictest transaction isolation.  This level emulates serial
    transaction execution for all committed transactions;
    as if transactions had been executed one after another, serially,
    rather than concurrently.  However, like the Repeatable Read level,
    applications using this level must
    be prepared to retry transactions due to serialization failures.
    In fact, this isolation level works exactly the same as Repeatable
    Read except that it also monitors for conditions which could make
    execution of a concurrent set of serializable transactions behave
    in a manner inconsistent with all possible serial (one at a time)
    executions of those transactions.  This monitoring does not
    introduce any blocking beyond that present in repeatable read, but
    there is some overhead to the monitoring, and detection of the
    conditions which could cause a
    serialization anomaly will trigger a
    serialization failure.
   

   

    As an example,
    consider a table mytab, initially containing:

```
class | value
-------+-------
     1 |    10
     1 |    20
     2 |   100
     2 |   200
```

    Suppose that serializable transaction A computes:

```
SELECT SUM(value) FROM mytab WHERE class = 1;
```

    and then inserts the result (30) as the value in a
    new row with class = 2.  Concurrently, serializable
    transaction B computes:

```
SELECT SUM(value) FROM mytab WHERE class = 2;
```

    and obtains the result 300, which it inserts in a new row with
    class = 1.  Then both transactions try to commit.
    If either transaction were running at the Repeatable Read isolation level,
    both would be allowed to commit; but since there is no serial order of execution
    consistent with the result, using Serializable transactions will allow one
    transaction to commit and will roll the other back with this message:

```
ERROR:  could not serialize access due to read/write dependencies among transactions
```

    This is because if A had
    executed before B, B would have computed the sum 330, not 300, and
    similarly the other order would have resulted in a different sum
    computed by A.
   

   

    When relying on Serializable transactions to prevent anomalies, it is
    important that any data read from a permanent user table not be
    considered valid until the transaction which read it has successfully
    committed.  This is true even for read-only transactions, except that
    data read within a deferrable read-only
    transaction is known to be valid as soon as it is read, because such a
    transaction waits until it can acquire a snapshot guaranteed to be free
    from such problems before starting to read any data.  In all other cases
    applications must not depend on results read during a transaction that
    later aborted; instead, they should retry the transaction until it
    succeeds.
   

   

    To guarantee true serializability PostgreSQL
    uses predicate locking, which means that it keeps locks
    which allow it to determine when a write would have had an impact on
    the result of a previous read from a concurrent transaction, had it run
    first.  In PostgreSQL these locks do not
    cause any blocking and therefore can not play any part in
    causing a deadlock.  They are used to identify and flag dependencies
    among concurrent Serializable transactions which in certain combinations
    can lead to serialization anomalies.  In contrast, a Read Committed or
    Repeatable Read transaction which wants to ensure data consistency may
    need to take out a lock on an entire table, which could block other
    users attempting to use that table, or it may use SELECT FOR
    UPDATE or SELECT FOR SHARE which not only
    can block other transactions but cause disk access.
   

   

    Predicate locks in PostgreSQL, like in most
    other database systems, are based on data actually accessed by a
    transaction.  These will show up in the
    pg_locks
    system view with a mode of SIReadLock.  The
    particular locks
    acquired during execution of a query will depend on the plan used by
    the query, and multiple finer-grained locks (e.g., tuple locks) may be
    combined into fewer coarser-grained locks (e.g., page locks) during the
    course of the transaction to prevent exhaustion of the memory used to
    track the locks.  A READ ONLY transaction may be able to
    release its SIRead locks before completion, if it detects that no
    conflicts can still occur which could lead to a serialization anomaly.
    In fact, READ ONLY transactions will often be able to
    establish that fact at startup and avoid taking any predicate locks.
    If you explicitly request a SERIALIZABLE READ ONLY DEFERRABLE
    transaction, it will block until it can establish this fact.  (This is
    the only case where Serializable transactions block but
    Repeatable Read transactions don't.)  On the other hand, SIRead locks
    often need to be kept past transaction commit, until overlapping read
    write transactions complete.
   

   

    Consistent use of Serializable transactions can simplify development.
    The guarantee that any set of successfully committed concurrent
    Serializable transactions will have the same effect as if they were run
    one at a time means that if you can demonstrate that a single transaction,
    as written, will do the right thing when run by itself, you can have
    confidence that it will do the right thing in any mix of Serializable
    transactions, even without any information about what those other
    transactions might do, or it will not successfully commit.  It is
    important that an environment which uses this technique have a
    generalized way of handling serialization failures (which always return
    with an SQLSTATE value of '40001'), because it will be very hard to
    predict exactly which transactions might contribute to the read/write
    dependencies and need to be rolled back to prevent serialization
    anomalies.  The monitoring of read/write dependencies has a cost, as does
    the restart of transactions which are terminated with a serialization
    failure, but balanced against the cost and blocking involved in use of
    explicit locks and SELECT FOR UPDATE or SELECT FOR
    SHARE, Serializable transactions are the best performance choice
    for some environments.
   

   

    While PostgreSQL's Serializable transaction isolation
    level only allows concurrent transactions to commit if it can prove there
    is a serial order of execution that would produce the same effect, it
    doesn't always prevent errors from being raised that would not occur in
    true serial execution.  In particular, it is possible to see unique
    constraint violations caused by conflicts with overlapping Serializable
    transactions even after explicitly checking that the key isn't present
    before attempting to insert it.  This can be avoided by making sure
    that all Serializable transactions that insert potentially
    conflicting keys explicitly check if they can do so first.  For example,
    imagine an application that asks the user for a new key and then checks
    that it doesn't exist already by trying to select it first, or generates
    a new key by selecting the maximum existing key and adding one.  If some
    Serializable transactions insert new keys directly without following this
    protocol, unique constraints violations might be reported even in cases
    where they could not occur in a serial execution of the concurrent
    transactions.
   

   

    For optimal performance when relying on Serializable transactions for
    concurrency control, these issues should be considered:

    

     

      

       Declare transactions as READ ONLY when possible.
      

     

     

      

       Control the number of active connections, using a connection pool if
       needed.  This is always an important performance consideration, but
       it can be particularly important in a busy system using Serializable
       transactions.
      

     

     

      

       Don't put more into a single transaction than needed for integrity
       purposes.
      

     

     

      

       Don't leave connections dangling idle in transaction
       longer than necessary.  The configuration parameter
        may be used to
       automatically disconnect lingering sessions.
      

     

     

      

       Eliminate explicit locks, SELECT FOR UPDATE, and
       SELECT FOR SHARE where no longer needed due to the
       protections automatically provided by Serializable transactions.
      

     

     

      

       When the system is forced to combine multiple page-level predicate
       locks into a single relation-level predicate lock because the predicate
       lock table is short of memory, an increase in the rate of serialization
       failures may occur.  You can avoid this by increasing
       ,
       , and/or
       .
      

     

     

      

       A sequential scan will always necessitate a relation-level predicate
       lock.  This can result in an increased rate of serialization failures.
       It may be helpful to encourage the use of index scans by reducing
        and/or increasing
       .  Be sure to weigh any decrease
       in transaction rollbacks and restarts against any overall change in
       query execution time.
      

     

    

   

   

    The Serializable isolation level is implemented using a technique known
    in academic database literature as Serializable Snapshot Isolation, which
    builds on Snapshot Isolation by adding checks for serialization anomalies.
    Some differences in behavior and performance may be observed when compared
    with other systems that use a traditional locking technique.  Please see
     for detailed information.
   

  

 

  

   
# Explicit Locking

   
    lock
   

   

    PostgreSQL provides various lock modes
    to control concurrent access to data in tables.  These modes can
    be used for application-controlled locking in situations where
    MVCC does not give the desired behavior.  Also,
    most PostgreSQL commands automatically
    acquire locks of appropriate modes to ensure that referenced
    tables are not dropped or modified in incompatible ways while the
    command executes.  (For example, TRUNCATE cannot safely be
    executed concurrently with other operations on the same table, so it
    obtains an ACCESS EXCLUSIVE lock on the table to
    enforce that.)
   

   

    To examine a list of the currently outstanding locks in a database
    server, use the
    pg_locks
    system view. For more information on monitoring the status of the lock
    manager subsystem, refer to .
   

  

   
# Table-Level Locks

   
    LOCK
   

   

    The list below shows the available lock modes and the contexts in
    which they are used automatically by
    PostgreSQL.  You can also acquire any
    of these locks explicitly with the command .
    Remember that all of these lock modes are table-level locks,
    even if the name contains the word
    row; the names of the lock modes are historical.
    To some extent the names reflect the typical usage of each lock
    mode &mdash; but the semantics are all the same.  The only real difference
    between one lock mode and another is the set of lock modes with
    which each conflicts (see ).
    Two transactions cannot hold locks of conflicting
    modes on the same table at the same time.  (However, a transaction
    never conflicts with itself.  For example, it might acquire
    ACCESS EXCLUSIVE lock and later acquire
    ACCESS SHARE lock on the same table.)  Non-conflicting
    lock modes can be held concurrently by many transactions.  Notice in
    particular that some lock modes are self-conflicting (for example,
    an ACCESS EXCLUSIVE lock cannot be held by more than one
    transaction at a time) while others are not self-conflicting (for example,
    an ACCESS SHARE lock can be held by multiple transactions).
   

     

      
# Table-Level Lock Modes

      

       

        ACCESS SHARE (AccessShareLock)
       

       

        

         Conflicts with the ACCESS EXCLUSIVE lock
         mode only.
        

        

         The SELECT command acquires a lock of this mode on
         referenced tables.  In general, any query that only reads a table
         and does not modify it will acquire this lock mode.
        

       

      

      

       

        ROW SHARE (RowShareLock)
       

       

        

         Conflicts with the EXCLUSIVE and
         ACCESS EXCLUSIVE lock modes.
        

        

         The SELECT command acquires a lock of this mode
         on all tables on which one of the FOR UPDATE,
         FOR NO KEY UPDATE,
         FOR SHARE, or
         FOR KEY SHARE options is specified
         (in addition to ACCESS SHARE locks on any other
         tables that are referenced without any explicit
         FOR ... locking option).
        

       

      

      

       

        ROW EXCLUSIVE (RowExclusiveLock)
       

       

        

         Conflicts with the SHARE, SHARE ROW
         EXCLUSIVE, EXCLUSIVE, and
         ACCESS EXCLUSIVE lock modes.
        

        

         The commands UPDATE,
         DELETE, INSERT, and
         MERGE
         acquire this lock mode on the target table (in addition to
         ACCESS SHARE locks on any other referenced
         tables).  In general, this lock mode will be acquired by any
         command that modifies data in a table.
        

       

      

      

       

        SHARE UPDATE EXCLUSIVE (ShareUpdateExclusiveLock)
       

       

        

         Conflicts with the SHARE UPDATE EXCLUSIVE,
         SHARE, SHARE ROW
         EXCLUSIVE, EXCLUSIVE, and
         ACCESS EXCLUSIVE lock modes.
         This mode protects a table against
         concurrent schema changes and VACUUM runs.
        

        

         Acquired by VACUUM (without FULL),
         ANALYZE, CREATE INDEX CONCURRENTLY,
         CREATE STATISTICS, COMMENT ON,
         REINDEX CONCURRENTLY,
         and certain ALTER INDEX
         and ALTER TABLE variants
         (for full details see the documentation of these commands).
        

       

      

      

       

        SHARE (ShareLock)
       

       

        

         Conflicts with the ROW EXCLUSIVE,
         SHARE UPDATE EXCLUSIVE, SHARE ROW
         EXCLUSIVE, EXCLUSIVE, and
         ACCESS EXCLUSIVE lock modes.
         This mode protects a table against concurrent data changes.
        

        

         Acquired by CREATE INDEX
         (without CONCURRENTLY).
        

       

      

      

       

        SHARE ROW EXCLUSIVE (ShareRowExclusiveLock)
       

       

        

         Conflicts with the ROW EXCLUSIVE,
         SHARE UPDATE EXCLUSIVE,
         SHARE, SHARE ROW
         EXCLUSIVE, EXCLUSIVE, and
         ACCESS EXCLUSIVE lock modes.
         This mode protects a table against concurrent data changes, and
         is self-exclusive so that only one session can hold it at a time.
        

        

         Acquired by CREATE TRIGGER and some forms of
         ALTER TABLE.
        

       

      

      

       

        EXCLUSIVE (ExclusiveLock)
       

       

        

         Conflicts with the ROW SHARE, ROW
         EXCLUSIVE, SHARE UPDATE
         EXCLUSIVE, SHARE, SHARE
         ROW EXCLUSIVE, EXCLUSIVE, and
         ACCESS EXCLUSIVE lock modes.
         This mode allows only concurrent ACCESS SHARE locks,
         i.e., only reads from the table can proceed in parallel with a
         transaction holding this lock mode.
        

        

         Acquired by REFRESH MATERIALIZED VIEW CONCURRENTLY.
        

       

      

      

       

        ACCESS EXCLUSIVE (AccessExclusiveLock)
       

       

        

         Conflicts with locks of all modes (ACCESS
         SHARE, ROW SHARE, ROW
         EXCLUSIVE, SHARE UPDATE
         EXCLUSIVE, SHARE, SHARE
         ROW EXCLUSIVE, EXCLUSIVE, and
         ACCESS EXCLUSIVE).
         This mode guarantees that the
         holder is the only transaction accessing the table in any way.
        

        

         Acquired by the DROP TABLE,
         TRUNCATE, REINDEX,
         CLUSTER, VACUUM FULL,
         and REFRESH MATERIALIZED VIEW (without
         CONCURRENTLY)
         commands. Many forms of ALTER INDEX and ALTER TABLE also acquire
         a lock at this level. This is also the default lock mode for
         LOCK TABLE statements that do not specify
         a mode explicitly.
        

       

      

     

     
      

       Only an ACCESS EXCLUSIVE lock blocks a
       SELECT (without FOR UPDATE/SHARE)
       statement.
      

     

   

    Once acquired, a lock is normally held until the end of the transaction.  But if a
    lock is acquired after establishing a savepoint, the lock is released
    immediately if the savepoint is rolled back to.  This is consistent with
    the principle that ROLLBACK cancels all effects of the
    commands since the savepoint.  The same holds for locks acquired within a
    PL/pgSQL exception block: an error escape from the block
    releases locks acquired within it.
   

    
     
# Conflicting Lock Modes

     
      
      
      
      
      
      
      
      
      
      
      
       
        Requested Lock Mode
        Existing Lock Mode
       
       
        ACCESS SHARE
        ROW SHARE
        ROW EXCL.
        SHARE UPDATE EXCL.
        SHARE
        SHARE ROW EXCL.
        EXCL.
        ACCESS EXCL.
       
      
      
       
        ACCESS SHARE
        
        
        
        
        
        
        
        X
       
       
        ROW SHARE
        
        
        
        
        
        
        X
        X
       
       
        ROW EXCL.
        
        
        
        
        X
        X
        X
        X
       
       
        SHARE UPDATE EXCL.
        
        
        
        X
        X
        X
        X
        X
       
       
        SHARE
        
        
        X
        X
        
        X
        X
        X
       
       
        SHARE ROW EXCL.
        
        
        X
        X
        X
        X
        X
        X
       
       
        EXCL.
        
        X
        X
        X
        X
        X
        X
        X
       
       
        ACCESS EXCL.
        X
        X
        X
        X
        X
        X
        X
        X
       
      
     
    

    

     
# Fast-Path Locking

     
      fast-path locking
     

     

      Internally, PostgreSQL can record some
      table-level locks using a fast-path locking
      mechanism instead of the main lock table. This reduces the overhead of
      acquiring and releasing locks that rarely conflict. It is an
      implementation optimization and does not change lock semantics.
     

     

      Fast-path locking can be used only for eligible relation locks in the
      weak table-level lock modes ACCESS SHARE
      (AccessShareLock), ROW SHARE
      (RowShareLock), and ROW EXCLUSIVE
      (RowExclusiveLock). It does not apply to shared
      relations (those visible across all databases, such as
      pg_authid). Other lock types, and stronger
      lock modes on relations, always go through the main lock table.  Even
      for eligible locks, fast-path is used only when a per-backend slot is
      available; the number of slots is derived from . When no slot is available,
      the lock is acquired via the main lock table instead.
     

     

      Acquiring a lock via the main lock table is considerably more
      expensive than the fast path, and under heavy concurrent lock
      activity can become a point of contention (observable as the
      LockManager wait event).
     

    

   

   

    
# Row-Level Locks

    

     In addition to table-level locks, there are row-level locks, which
     are listed as below with the contexts in which they are used
     automatically by PostgreSQL.  See
      for a complete table of
     row-level lock conflicts.  Note that a transaction can hold
     conflicting locks on the same row, even in different subtransactions;
     but other than that, two transactions can never hold conflicting locks
     on the same row.  Row-level locks do not affect data querying; they
     block only writers and lockers to the same
     row.  Row-level locks are released at transaction end or during
     savepoint rollback, just like table-level locks.

    

     

      
# Row-Level Lock Modes

      

       

        FOR UPDATE
       

       

        

         FOR UPDATE causes the rows retrieved by the
         SELECT statement to be locked as though for
         update.  This prevents them from being locked, modified or deleted by
         other transactions until the current transaction ends.  That is,
         other transactions that attempt UPDATE,
         DELETE,
         SELECT FOR UPDATE,
         SELECT FOR NO KEY UPDATE,
         SELECT FOR SHARE or
         SELECT FOR KEY SHARE
         of these rows will be blocked until the current transaction ends;
         conversely, SELECT FOR UPDATE will wait for a
         concurrent transaction that has run any of those commands on the
         same row,
         and will then lock and return the updated row (or no row, if the
         row was deleted).  Within a REPEATABLE READ or
         SERIALIZABLE transaction,
         however, an error will be thrown if a row to be locked has changed
         since the transaction started.  For further discussion see
         .
        

        

         The FOR UPDATE lock mode
         is also acquired by any DELETE on a row, and also by an
         UPDATE that modifies the values of certain columns.  Currently,
         the set of columns considered for the UPDATE case are those that
         have a unique index on them that can be used in a foreign key (so partial
         indexes and expressional indexes are not considered), but this may change
         in the future.
        

       

      

      

       

        FOR NO KEY UPDATE
       

       

        

         Behaves similarly to FOR UPDATE, except that the lock
         acquired is weaker: this lock will not block
         SELECT FOR KEY SHARE commands that attempt to acquire
         a lock on the same rows. This lock mode is also acquired by any
         UPDATE that does not acquire a FOR UPDATE lock.
        

       

      

      

       

        FOR SHARE
       

       

        

         Behaves similarly to FOR NO KEY UPDATE, except that it
         acquires a shared lock rather than exclusive lock on each retrieved
         row.  A shared lock blocks other transactions from performing
         UPDATE, DELETE,
         SELECT FOR UPDATE or
         SELECT FOR NO KEY UPDATE on these rows, but it does not
         prevent them from performing SELECT FOR SHARE or
         SELECT FOR KEY SHARE.
        

       

      

      

       

        FOR KEY SHARE
       

       

        

         Behaves similarly to FOR SHARE, except that the
         lock is weaker: SELECT FOR UPDATE is blocked, but not
         SELECT FOR NO KEY UPDATE.  A key-shared lock blocks
         other transactions from performing DELETE or
         any UPDATE that changes the key values, but not
         other UPDATE, and neither does it prevent
         SELECT FOR NO KEY UPDATE, SELECT FOR SHARE,
         or SELECT FOR KEY SHARE.
        

       

      

     

    

     PostgreSQL doesn't remember any
     information about modified rows in memory, so there is no limit on
     the number of rows locked at one time.  However, locking a row
     might cause a disk write, e.g., SELECT FOR
     UPDATE modifies selected rows to mark them locked, and so
     will result in disk writes.
    

    
     
# Conflicting Row-Level Locks

     
      
      
      
      
      
      
      
       
        Requested Lock Mode
        Current Lock Mode
       
       
        FOR KEY SHARE
        FOR SHARE
        FOR NO KEY UPDATE
        FOR UPDATE
       
      
      
       
        FOR KEY SHARE
        
        
        
        X
       
       
        FOR SHARE
        
        
        X
        X
       
       
        FOR NO KEY UPDATE
        
        X
        X
        X
       
       
        FOR UPDATE
        X
        X
        X
        X
       
      
     
    
   

   

    
# Page-Level Locks

    

     In addition to table and row locks, page-level share/exclusive locks are
     used to control read/write access to table pages in the shared buffer
     pool.  These locks are released immediately after a row is fetched or
     updated.  Application developers normally need not be concerned with
     page-level locks, but they are mentioned here for completeness.
    

   

   

    
# Deadlocks

    
     deadlock
    

    

     The use of explicit locking can increase the likelihood of
     deadlocks, wherein two (or more) transactions each
     hold locks that the other wants.  For example, if transaction 1
     acquires an exclusive lock on table A and then tries to acquire
     an exclusive lock on table B, while transaction 2 has already
     exclusive-locked table B and now wants an exclusive lock on table
     A, then neither one can proceed.
     PostgreSQL automatically detects
     deadlock situations and resolves them by aborting one of the
     transactions involved, allowing the other(s) to complete.
     (Exactly which transaction will be aborted is difficult to
     predict and should not be relied upon.)
    

    

     Note that deadlocks can also occur as the result of row-level
     locks (and thus, they can occur even if explicit locking is not
     used). Consider the case in which two concurrent
     transactions modify a table. The first transaction executes:

```
UPDATE accounts SET balance = balance + 100.00 WHERE acctnum = 11111;
```

     This acquires a row-level lock on the row with the specified
     account number. Then, the second transaction executes:

```
UPDATE accounts SET balance = balance + 100.00 WHERE acctnum = 22222;
UPDATE accounts SET balance = balance - 100.00 WHERE acctnum = 11111;
```

     The first UPDATE statement successfully
     acquires a row-level lock on the specified row, so it succeeds in
     updating that row. However, the second UPDATE
     statement finds that the row it is attempting to update has
     already been locked, so it waits for the transaction that
     acquired the lock to complete. Transaction two is now waiting on
     transaction one to complete before it continues execution. Now,
     transaction one executes:

```
UPDATE accounts SET balance = balance - 100.00 WHERE acctnum = 22222;
```

     Transaction one attempts to acquire a row-level lock on the
     specified row, but it cannot: transaction two already holds such
     a lock. So it waits for transaction two to complete. Thus,
     transaction one is blocked on transaction two, and transaction
     two is blocked on transaction one: a deadlock
     condition. PostgreSQL will detect this
     situation and abort one of the transactions.
    

    

     The best defense against deadlocks is generally to avoid them by
     being certain that all applications using a database acquire
     locks on multiple objects in a consistent order. In the example
     above, if both transactions
     had updated the rows in the same order, no deadlock would have
     occurred. One should also ensure that the first lock acquired on
     an object in a transaction is the most restrictive mode that will be
     needed for that object.  If it is not feasible to verify this in
     advance, then deadlocks can be handled on-the-fly by retrying
     transactions that abort due to deadlocks.
    

    

     So long as no deadlock situation is detected, a transaction seeking
     either a table-level or row-level lock will wait indefinitely for
     conflicting locks to be released.  This means it is a bad idea for
     applications to hold transactions open for long periods of time
     (e.g., while waiting for user input).
    

   

   

    
# Advisory Locks

    
     advisory lock
    

    
     lock
     advisory
    

    

     PostgreSQL provides a means for
     creating locks that have application-defined meanings.  These are
     called advisory locks, because the system does not
     enforce their use &mdash; it is up to the application to use them
     correctly.  Advisory locks can be useful for locking strategies
     that are an awkward fit for the MVCC model.
     For example, a common use of advisory locks is to emulate pessimistic
     locking strategies typical of so-called flat file data
     management systems.
     While a flag stored in a table could be used for the same purpose,
     advisory locks are faster, avoid table bloat, and are automatically
     cleaned up by the server at the end of the session.
    

    

     There are two ways to acquire an advisory lock in
     PostgreSQL: at session level or at
     transaction level.
     Once acquired at session level, an advisory lock is held until
     explicitly released or the session ends.  Unlike standard lock requests,
     session-level advisory lock requests do not honor transaction semantics:
     a lock acquired during a transaction that is later rolled back will still
     be held following the rollback, and likewise an unlock is effective even
     if the calling transaction fails later.  A lock can be acquired multiple
     times by its owning process; for each completed lock request there must
     be a corresponding unlock request before the lock is actually released.
     Transaction-level lock requests, on the other hand, behave more like
     regular lock requests: they are automatically released at the end of the
     transaction, and there is no explicit unlock operation.  This behavior
     is often more convenient than the session-level behavior for short-term
     usage of an advisory lock.
     Session-level and transaction-level lock requests for the same advisory
     lock identifier will block each other in the expected way.
     If a session already holds a given advisory lock, additional requests by
     it will always succeed, even if other sessions are awaiting the lock; this
     statement is true regardless of whether the existing lock hold and new
     request are at session level or transaction level.
    

    

     Like all locks in
     PostgreSQL, a complete list of advisory locks
     currently held by any session can be found in the pg_locks system
     view.
    

    

     Both advisory locks and regular locks are stored in a shared memory
     pool whose size is defined by the configuration variables
      and
     .
     Care must be taken not to exhaust this
     memory or the server will be unable to grant any locks at all.
     This imposes an upper limit on the number of advisory locks
     grantable by the server, typically in the tens to hundreds of thousands
     depending on how the server is configured.
    

    

     In certain cases using advisory locking methods, especially in queries
     involving explicit ordering and LIMIT clauses, care must be
     taken to control the locks acquired because of the order in which SQL
     expressions are evaluated.  For example:

```
SELECT pg_advisory_lock(id) FROM foo WHERE id = 12345; -- ok
SELECT pg_advisory_lock(id) FROM foo WHERE id > 12345 LIMIT 100; -- danger!
SELECT pg_advisory_lock(q.id) FROM
(
  SELECT id FROM foo WHERE id > 12345 LIMIT 100
) q; -- ok
```

     In the above queries, the second form is dangerous because the
     LIMIT is not guaranteed to be applied before the locking
     function is executed.  This might cause some locks to be acquired
     that the application was not expecting, and hence would fail to release
     (until it ends the session).
     From the point of view of the application, such locks
     would be dangling, although still viewable in
     pg_locks.
    

    

     The functions provided to manipulate advisory locks are described in
     .
    

   

  

  

   
# Data Consistency Checks at the Application Level

   

    It is very difficult to enforce business rules regarding data integrity
    using Read Committed transactions because the view of the data is
    shifting with each statement, and even a single statement may not
    restrict itself to the statement's snapshot if a write conflict occurs.
   

   

    While a Repeatable Read transaction has a stable view of the data
    throughout its execution, there is a subtle issue with using
    MVCC snapshots for data consistency checks, involving
    something known as read/write conflicts.
    If one transaction writes data and a concurrent transaction attempts
    to read the same data (whether before or after the write), it cannot
    see the work of the other transaction.  The reader then appears to have
    executed first regardless of which started first or which committed
    first.  If that is as far as it goes, there is no problem, but
    if the reader also writes data which is read by a concurrent transaction
    there is now a transaction which appears to have run before either of
    the previously mentioned transactions.  If the transaction which appears
    to have executed last actually commits first, it is very easy for a
    cycle to appear in a graph of the order of execution of the transactions.
    When such a cycle appears, integrity checks will not work correctly
    without some help.
   

   

    As mentioned in , Serializable
    transactions are just Repeatable Read transactions which add
    nonblocking monitoring for dangerous patterns of read/write conflicts.
    When a pattern is detected which could cause a cycle in the apparent
    order of execution, one of the transactions involved is rolled back to
    break the cycle.
   

   

    
# Enforcing Consistency with Serializable Transactions

    

     If the Serializable transaction isolation level is used for all writes
     and for all reads which need a consistent view of the data, no other
     effort is required to ensure consistency.  Software from other
     environments which is written to use serializable transactions to
     ensure consistency should just work in this regard in
     PostgreSQL.
    

    

     When using this technique, it will avoid creating an unnecessary burden
     for application programmers if the application software goes through a
     framework which automatically retries transactions which are rolled
     back with a serialization failure.  It may be a good idea to set
     default_transaction_isolation to serializable.
     It would also be wise to take some action to ensure that no other
     transaction isolation level is used, either inadvertently or to
     subvert integrity checks, through checks of the transaction isolation
     level in triggers.
    

    

     See  for performance suggestions.
    

    
     
# Warning: Serializable Transactions and Data Replication

     

      This level of integrity protection using Serializable transactions
      does not yet extend to hot standby mode ()
      or logical replicas.
      Because of that, those using hot standby or logical replication
      may want to use Repeatable Read and explicit locking on the primary.
     

    
   

   

    
# Enforcing Consistency with Explicit Blocking Locks

    

     When non-serializable writes are possible,
     to ensure the current validity of a row and protect it against
     concurrent updates one must use SELECT FOR UPDATE,
     SELECT FOR SHARE, or an appropriate LOCK
     TABLE statement.  (SELECT FOR UPDATE
     and SELECT FOR SHARE lock just the
     returned rows against concurrent updates, while LOCK
     TABLE locks the whole table.)  This should be taken into
     account when porting applications to
     PostgreSQL from other environments.
    

    

     Also of note to those converting from other environments is the fact
     that SELECT FOR UPDATE does not ensure that a
     concurrent transaction will not update or delete a selected row.
     To do that in PostgreSQL you must actually
     update the row, even if no values need to be changed.
     SELECT FOR UPDATE temporarily blocks
     other transactions from acquiring the same lock or executing an
     UPDATE or DELETE which would
     affect the locked row, but once the transaction holding this lock
     commits or rolls back, a blocked transaction will proceed with the
     conflicting operation unless an actual UPDATE of
     the row was performed while the lock was held.
    

    

     Global validity checks require extra thought under
     non-serializable MVCC.
     For example, a banking application might wish to check that the sum of
     all credits in one table equals the sum of debits in another table,
     when both tables are being actively updated.  Comparing the results of two
     successive SELECT sum(...) commands will not work reliably in
     Read Committed mode, since the second query will likely include the results
     of transactions not counted by the first.  Doing the two sums in a
     single repeatable read transaction will give an accurate picture of only the
     effects of transactions that committed before the repeatable read transaction
     started &mdash; but one might legitimately wonder whether the answer is still
     relevant by the time it is delivered.  If the repeatable read transaction
     itself applied some changes before trying to make the consistency check,
     the usefulness of the check becomes even more debatable, since now it
     includes some but not all post-transaction-start changes.  In such cases
     a careful person might wish to lock all tables needed for the check,
     in order to get an indisputable picture of current reality.  A
     SHARE mode (or higher) lock guarantees that there are no
     uncommitted changes in the locked table, other than those of the current
     transaction.
    

    

     Note also that if one is relying on explicit locking to prevent concurrent
     changes, one should either use Read Committed mode, or in Repeatable Read
     mode be careful to obtain
     locks before performing queries.  A lock obtained by a
     repeatable read transaction guarantees that no other transactions modifying
     the table are still running, but if the snapshot seen by the
     transaction predates obtaining the lock, it might predate some now-committed
     changes in the table.  A repeatable read transaction's snapshot is actually
     frozen at the start of its first query or data-modification command
     (SELECT, INSERT,
     UPDATE, DELETE, or
     MERGE), so it is possible to obtain locks explicitly
     before the snapshot is frozen.
    

   

  

  

   
# Serialization Failure Handling

   
    serialization failure
   
   
    retryable error
   

   

    Both Repeatable Read and Serializable isolation levels can produce
    errors that are designed to prevent serialization anomalies.  As
    previously stated, applications using these levels must be prepared to
    retry transactions that fail due to serialization errors.  Such an
    error's message text will vary according to the precise circumstances,
    but it will always have the SQLSTATE code 40001
    (serialization_failure).
   

   

    It may also be advisable to retry deadlock failures.
    These have the SQLSTATE code 40P01
    (deadlock_detected).
   

   

    In some cases it is also appropriate to retry unique-key failures,
    which have SQLSTATE code 23505
    (unique_violation), and exclusion constraint
    failures, which have SQLSTATE code 23P01
    (exclusion_violation).  For example, if the
    application selects a new value for a primary key column after
    inspecting the currently stored keys, it could get a unique-key
    failure because another application instance selected the same new key
    concurrently.  This is effectively a serialization failure, but the
    server will not detect it as such because it cannot see
    the connection between the inserted value and the previous reads.
    There are also some corner cases in which the server will issue a
    unique-key or exclusion constraint error even though in principle it
    has enough information to determine that a serialization problem
    is the underlying cause.  While it's recommendable to just
    retry serialization_failure errors unconditionally,
    more care is needed when retrying these other error codes, since they
    might represent persistent error conditions rather than transient
    failures.
   

   

    It is important to retry the complete transaction, including all logic
    that decides which SQL to issue and/or which values to use.
    Therefore, PostgreSQL does not offer an
    automatic retry facility, since it cannot do so with any guarantee of
    correctness.
   

   

    Transaction retry does not guarantee that the retried transaction will
    complete; multiple retries may be needed.  In cases with very high
    contention, it is possible that completion of a transaction may take
    many attempts.  In cases involving a conflicting prepared transaction,
    it may not be possible to make progress until the prepared transaction
    commits or rolls back.
   

  

  

   
# Caveats

   

    Some commands, currently only TRUNCATE, the
    table-rewriting forms of ALTER
    TABLE and REPACK with
    the CONCURRENTLY option, are not
    MVCC-safe.  This means that after the truncation or rewrite commits, the
    table will appear empty to concurrent transactions, if they are using a
    snapshot taken before the command committed.  This will only be an
    issue for a transaction that did not access the table in question
    before the command started &mdash; any transaction that has done so
    would hold at least an ACCESS SHARE table lock,
    which would block the truncating or rewriting command until that transaction completes.
    So these commands will not cause any apparent inconsistency in the
    table contents for successive queries on the target table, but they
    could cause visible inconsistency between the contents of the target
    table and other tables in the database.
   

   

    Support for the Serializable transaction isolation level has not yet
    been added to hot standby replication targets (described in
    ).  The strictest isolation level currently
    supported in hot standby mode is Repeatable Read.  While performing all
    permanent database writes within Serializable transactions on the
    primary will ensure that all standbys will eventually reach a consistent
    state, a Repeatable Read transaction run on the standby can sometimes
    see a transient state that is inconsistent with any serial execution
    of the transactions on the primary.
   

   

    Internal access to the system catalogs is not done using the isolation
    level of the current transaction.  This means that newly created database
    objects such as tables are visible to concurrent Repeatable Read and
    Serializable transactions, even though the rows they contain are not.  In
    contrast, queries that explicitly examine the system catalogs don't see
    rows representing concurrently created database objects, in the higher
    isolation levels.
   

  

  

   
# Locking and Indexes

   
    index
    locks
   

   

    Though PostgreSQL
    provides nonblocking read/write access to table
    data, nonblocking read/write access is not currently offered for every
    index access method implemented
    in PostgreSQL.
    The various index types are handled as follows:

    

     

      

       B-tree, GiST and SP-GiST indexes
      

      

       

        Short-term share/exclusive page-level locks are used for
        read/write access. Locks are released immediately after each
        index row is fetched or inserted.  These index types provide
        the highest concurrency without deadlock conditions.
       

      

     

     

      

       Hash indexes
      

      

       

        Share/exclusive hash-bucket-level locks are used for read/write
        access.  Locks are released after the whole bucket is processed.
        Bucket-level locks provide better concurrency than index-level
        ones, but deadlock is possible since the locks are held longer
        than one index operation.
       

      

     

     

      

       GIN indexes
      

      

       

        Short-term share/exclusive page-level locks are used for
        read/write access. Locks are released immediately after each
        index row is fetched or inserted. But note that insertion of a
        GIN-indexed value usually produces several index key insertions
        per row, so GIN might do substantial work for a single value's
        insertion.
       

      

     

    

   

   

    Currently, B-tree indexes offer the best performance for concurrent
    applications; since they also have more features than hash
    indexes, they are the recommended index type for concurrent
    applications that need to index scalar data. When dealing with
    non-scalar data, B-trees are not useful, and GiST, SP-GiST or GIN
    indexes should be used instead.
