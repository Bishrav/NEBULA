# dblink &mdash; connect to other PostgreSQL databases

 
  dblink
 

 

  dblink is a module that supports connections to
  other PostgreSQL databases from within a database
  session.
 

 

  dblink can report the following wait events under the wait
  event type Extension.
 

 

  

   
DblinkConnect

   

    

     Waiting to establish a connection to a remote server.
    

   

  

  

   
DblinkGetConnect

   

    

     Waiting to establish a connection to a remote server when it could not
     be found in the list of already-opened connections.
    

   

  

  

   
DblinkGetResult

   

    

     Waiting to receive the results of a query from a remote server.
    

   

  

 

 

  See also , which provides roughly the same
  functionality using a more modern and standards-compliant infrastructure.
 

 
  
   dblink_connect
  

  
   dblink_connect
   3
  

  
   dblink_connect
   opens a persistent connection to a remote database
  

  

dblink_connect(text connstr) returns text
dblink_connect(text connname, text connstr) returns text

  

  
   
# Description

   

    dblink_connect() establishes a connection to a remote
    PostgreSQL database.  The server and database to
    be contacted are identified through a standard libpq
    connection string.  Optionally, a name can be assigned to the
    connection.  Multiple named connections can be open at once, but
    only one unnamed connection is permitted at a time.  The connection
    will persist until closed or until the database session is ended.
   

   

    The connection string may also be the name of an existing foreign
    server.  It is recommended to use the foreign-data wrapper
    dblink_fdw when defining the foreign
    server.  See the example below, as well as
     and
    .
   

  

  
   
# Arguments

   

    

     

connname

     

      

       The name to use for this connection; if omitted, an unnamed
       connection is opened, replacing any existing unnamed connection.
      

     

    

    

     

connstr

     

      
libpq-style connection info string, for example
       hostaddr=127.0.0.1 port=5432 dbname=mydb user=postgres
       password=mypasswd options=-csearch_path=.
       For details see .
       Alternatively, the name of a foreign server.
      

     

    

   

  

  
   
# Return Value

   

    Returns status, which is always OK (since any error
    causes the function to throw an error instead of returning).
   

  

  
   
# Notes

   

    If untrusted users have access to a database that has not adopted a
    secure schema usage pattern,
    begin each session by removing publicly-writable schemas from
    search_path.  One could, for example,
    add options=-csearch_path= to
    
connstr
.  This consideration is not specific
    to dblink; it applies to every interface for
    executing arbitrary SQL commands.
   

   

    The foreign-data wrapper dblink_fdw has an additional
    Boolean option use_scram_passthrough that controls
    whether dblink will use the SCRAM pass-through
    authentication to connect to the remote database.  It can be specified
    for a foreign server or a user mapping.  A user mapping setting overrides
    the foreign server setting.  With SCRAM pass-through authentication,
    dblink uses SCRAM-hashed secrets instead of plain-text
    user passwords to connect to the remote server. This avoids storing
    plain-text user passwords in PostgreSQL system catalogs.
    See the documentation of the equivalent use_scram_passthrough
    option of postgres_fdw for further details and restrictions.
   

   

    Only superusers may use dblink_connect to create
    connections that use neither password authentication, SCRAM pass-through,
    nor GSSAPI-authentication.
    If non-superusers need this capability, use
    dblink_connect_u instead.
   

   

    It is unwise to choose connection names that contain equal signs,
    as this opens a risk of confusion with connection info strings
    in other dblink functions.
   

  

  
   
# Examples

```
SELECT dblink_connect('dbname=postgres options=-csearch_path=');
 dblink_connect
----------------
 OK
(1 row)

SELECT dblink_connect('myconn', 'dbname=postgres options=-csearch_path=');
 dblink_connect
----------------
 OK
(1 row)

-- FOREIGN DATA WRAPPER functionality
-- Note: local connections that don't use SCRAM pass-through require password
--       authentication for this to work properly. Otherwise, you will receive
--       the following error from dblink_connect():
--       ERROR:  password is required
--       DETAIL:  Non-superuser cannot connect if the server does not request a password.
--       HINT:  Target server's authentication method must be changed.

CREATE SERVER fdtest FOREIGN DATA WRAPPER dblink_fdw OPTIONS (hostaddr '127.0.0.1', dbname 'contrib_regression');

CREATE USER regress_dblink_user WITH PASSWORD 'secret';
CREATE USER MAPPING FOR regress_dblink_user SERVER fdtest OPTIONS (user 'regress_dblink_user', password 'secret');
GRANT USAGE ON FOREIGN SERVER fdtest TO regress_dblink_user;
GRANT SELECT ON TABLE foo TO regress_dblink_user;

\set ORIGINAL_USER :USER
\c - regress_dblink_user
SELECT dblink_connect('myconn', 'fdtest');
 dblink_connect
----------------
 OK
(1 row)

SELECT * FROM dblink('myconn', 'SELECT * FROM foo') AS t(a int, b text, c text[]);
 a  | b |       c
----+---+---------------
  0 | a | {a0,b0,c0}
  1 | b | {a1,b1,c1}
  2 | c | {a2,b2,c2}
  3 | d | {a3,b3,c3}
  4 | e | {a4,b4,c4}
  5 | f | {a5,b5,c5}
  6 | g | {a6,b6,c6}
  7 | h | {a7,b7,c7}
  8 | i | {a8,b8,c8}
  9 | j | {a9,b9,c9}
 10 | k | {a10,b10,c10}
(11 rows)

\c - :ORIGINAL_USER
REVOKE USAGE ON FOREIGN SERVER fdtest FROM regress_dblink_user;
REVOKE SELECT ON TABLE foo FROM regress_dblink_user;
DROP USER MAPPING FOR regress_dblink_user SERVER fdtest;
DROP USER regress_dblink_user;
DROP SERVER fdtest;
```

  
 

 
  
   dblink_connect_u
  

  
   dblink_connect_u
   3
  

  
   dblink_connect_u
   opens a persistent connection to a remote database, insecurely
  

  

dblink_connect_u(text connstr) returns text
dblink_connect_u(text connname, text connstr) returns text

  

  
   
# Description

   

    dblink_connect_u() is identical to
    dblink_connect(), except that it will allow non-superusers
    to connect using any authentication method.
   

   

    If the remote server selects an authentication method that does not
    involve a password, then impersonation and subsequent escalation of
    privileges can occur, because the session will appear to have
    originated from the user as which the local PostgreSQL
    server runs.  Also, even if the remote server does demand a password,
    it is possible for the password to be supplied from the server
    environment, such as a ~/.pgpass file belonging to the
    server's user.  This opens not only a risk of impersonation, but the
    possibility of exposing a password to an untrustworthy remote server.
    Therefore, dblink_connect_u() is initially
    installed with all privileges revoked from PUBLIC,
    making it un-callable except by superusers.  In some situations
    it may be appropriate to grant EXECUTE permission for
    dblink_connect_u() to specific users who are considered
    trustworthy, but this should be done with care.  It is also recommended
    that any ~/.pgpass file belonging to the server's user
    not contain any records specifying a wildcard host name.
   

   

    For further details see dblink_connect().
   

  
 

 
  
   dblink_disconnect
  

  
   dblink_disconnect
   3
  

  
   dblink_disconnect
   closes a persistent connection to a remote database
  

  

dblink_disconnect() returns text
dblink_disconnect(text connname) returns text

  

  
   
# Description

   

    dblink_disconnect() closes a connection previously opened
    by dblink_connect().  The form with no arguments closes
    an unnamed connection.
   

  

  
   
# Arguments

   

    

     

connname

     

      

       The name of a named connection to be closed.
      

     

    

   

  

  
   
# Return Value

   

    Returns status, which is always OK (since any error
    causes the function to throw an error instead of returning).
   

  

  
   
# Examples

```
SELECT dblink_disconnect();
 dblink_disconnect
-------------------
 OK
(1 row)

SELECT dblink_disconnect('myconn');
 dblink_disconnect
-------------------
 OK
(1 row)
```

  
 

 
  
   dblink
  

  
   dblink
   3
  

  
   dblink
   executes a query in a remote database
  

  

dblink(text connname, text sql [, bool fail_on_error]) returns setof record
dblink(text connstr, text sql [, bool fail_on_error]) returns setof record
dblink(text sql [, bool fail_on_error]) returns setof record

  

  
   
# Description

   

    dblink executes a query (usually a SELECT,
    but it can be any SQL statement that returns rows) in a remote database.
   

   

    When two text arguments are given, the first one is first
    looked up as a persistent connection's name; if found, the command
    is executed on that connection.  If not found, the first argument
    is treated as a connection info string as for dblink_connect,
    and the indicated connection is made just for the duration of this command.
   

  

  
   
# Arguments

   

    

     

connname

     

      

       Name of the connection to use; omit this parameter to use the
       unnamed connection.
      

     

    

    

     

connstr

     

      

       A connection info string, as previously described for
       dblink_connect.
      

     

    

    

     

sql

     

      

       The SQL query that you wish to execute in the remote database,
       for example SELECT * FROM foo.
      

     

    

    

     

fail_on_error

     

      

       If true (the default when omitted) then an error thrown on the
       remote side of the connection causes an error to also be thrown
       locally. If false, the remote error is locally reported as a NOTICE,
       and the function returns no rows.
      

     

    

   

  

  
   
# Return Value

   

    The function returns the row(s) produced by the query.  Since
    dblink can be used with any query, it is declared
    to return record, rather than specifying any particular
    set of columns.  This means that you must specify the expected
    set of columns in the calling query &mdash; otherwise
    PostgreSQL would not know what to expect.
    Here is an example:

```
SELECT *
    FROM dblink('dbname=mydb options=-csearch_path=',
                'SELECT proname, prosrc FROM pg_proc')
      AS t1(proname name, prosrc text)
    WHERE proname LIKE 'bytea%';
```

    The alias part of the FROM clause must
    specify the column names and types that the function will return.
    (Specifying column names in an alias is actually standard SQL
    syntax, but specifying column types is a PostgreSQL
    extension.)  This allows the system to understand what
    * should expand to, and what proname
    in the WHERE clause refers to, in advance of trying
    to execute the function.  At run time, an error will be thrown
    if the actual query result from the remote database does not
    have the same number of columns shown in the FROM clause.
    The column names need not match, however, and dblink
    does not insist on exact type matches either.  It will succeed
    so long as the returned data strings are valid input for the
    column type declared in the FROM clause.
   

  

  
   
# Notes

   

    A convenient way to use dblink with predetermined
    queries is to create a view.
    This allows the column type information to be buried in the view,
    instead of having to spell it out in every query.  For example,

```
CREATE VIEW myremote_pg_proc AS
  SELECT *
    FROM dblink('dbname=postgres options=-csearch_path=',
                'SELECT proname, prosrc FROM pg_proc')
    AS t1(proname name, prosrc text);

SELECT * FROM myremote_pg_proc WHERE proname LIKE 'bytea%';
```

  

  
   
# Examples

```
SELECT * FROM dblink('dbname=postgres options=-csearch_path=',
                     'SELECT proname, prosrc FROM pg_proc')
  AS t1(proname name, prosrc text) WHERE proname LIKE 'bytea%';
  proname   |   prosrc
------------+------------
 byteacat   | byteacat
 byteaeq    | byteaeq
 bytealt    | bytealt
 byteale    | byteale
 byteagt    | byteagt
 byteage    | byteage
 byteane    | byteane
 byteacmp   | byteacmp
 bytealike  | bytealike
 byteanlike | byteanlike
 byteain    | byteain
 byteaout   | byteaout
(12 rows)

SELECT dblink_connect('dbname=postgres options=-csearch_path=');
 dblink_connect
----------------
 OK
(1 row)

SELECT * FROM dblink('SELECT proname, prosrc FROM pg_proc')
  AS t1(proname name, prosrc text) WHERE proname LIKE 'bytea%';
  proname   |   prosrc
------------+------------
 byteacat   | byteacat
 byteaeq    | byteaeq
 bytealt    | bytealt
 byteale    | byteale
 byteagt    | byteagt
 byteage    | byteage
 byteane    | byteane
 byteacmp   | byteacmp
 bytealike  | bytealike
 byteanlike | byteanlike
 byteain    | byteain
 byteaout   | byteaout
(12 rows)

SELECT dblink_connect('myconn', 'dbname=regression options=-csearch_path=');
 dblink_connect
----------------
 OK
(1 row)

SELECT * FROM dblink('myconn', 'SELECT proname, prosrc FROM pg_proc')
  AS t1(proname name, prosrc text) WHERE proname LIKE 'bytea%';
  proname   |   prosrc
------------+------------
 bytearecv  | bytearecv
 byteasend  | byteasend
 byteale    | byteale
 byteagt    | byteagt
 byteage    | byteage
 byteane    | byteane
 byteacmp   | byteacmp
 bytealike  | bytealike
 byteanlike | byteanlike
 byteacat   | byteacat
 byteaeq    | byteaeq
 bytealt    | bytealt
 byteain    | byteain
 byteaout   | byteaout
(14 rows)
```

  
 

 
  
   dblink_exec
  

  
   dblink_exec
   3
  

  
   dblink_exec
   executes a command in a remote database
  

  

dblink_exec(text connname, text sql [, bool fail_on_error]) returns text
dblink_exec(text connstr, text sql [, bool fail_on_error]) returns text
dblink_exec(text sql [, bool fail_on_error]) returns text

  

  
   
# Description

   

    dblink_exec executes a command (that is, any SQL statement
    that doesn't return rows) in a remote database.
   

   

    When two text arguments are given, the first one is first
    looked up as a persistent connection's name; if found, the command
    is executed on that connection.  If not found, the first argument
    is treated as a connection info string as for dblink_connect,
    and the indicated connection is made just for the duration of this command.
   

  

  
   
# Arguments

   

    

     

connname

     

      

       Name of the connection to use; omit this parameter to use the
       unnamed connection.
      

     

    

    

     

connstr

     

      

       A connection info string, as previously described for
       dblink_connect.
      

     

    

    

     

sql

     

      

       The SQL command that you wish to execute in the remote database,
       for example
       INSERT INTO foo VALUES (0, 'a', '{"a0","b0","c0"}').
      

     

    

    

     

fail_on_error

     

      

       If true (the default when omitted) then an error thrown on the
       remote side of the connection causes an error to also be thrown
       locally. If false, the remote error is locally reported as a NOTICE,
       and the function's return value is set to ERROR.
      

     

    

   

  

  
   
# Return Value

   

    Returns status, either the command's status string or ERROR.
   

  

  
   
# Examples

```
SELECT dblink_connect('dbname=dblink_test_standby');
 dblink_connect
----------------
 OK
(1 row)

SELECT dblink_exec('insert into foo values(21, ''z'', ''{"a0","b0","c0"}'');');
   dblink_exec
-----------------
 INSERT 943366 1
(1 row)

SELECT dblink_connect('myconn', 'dbname=regression');
 dblink_connect
----------------
 OK
(1 row)

SELECT dblink_exec('myconn', 'insert into foo values(21, ''z'', ''{"a0","b0","c0"}'');');
   dblink_exec
------------------
 INSERT 6432584 1
(1 row)

SELECT dblink_exec('myconn', 'insert into pg_class values (''foo'')',false);
NOTICE:  sql error
DETAIL:  ERROR:  null value in column "relnamespace" violates not-null constraint

 dblink_exec
-------------
 ERROR
(1 row)
```

  
 

 
  
   dblink_open
  

  
   dblink_open
   3
  

  
   dblink_open
   opens a cursor in a remote database
  

  

dblink_open(text cursorname, text sql [, bool fail_on_error]) returns text
dblink_open(text connname, text cursorname, text sql [, bool fail_on_error]) returns text

  

  
   
# Description

   

    dblink_open() opens a cursor in a remote database.
    The cursor can subsequently be manipulated with
    dblink_fetch() and dblink_close().
   

  

  
   
# Arguments

   

    

     

connname

     

      

       Name of the connection to use; omit this parameter to use the
       unnamed connection.
      

     

    

    

     

cursorname

     

      

       The name to assign to this cursor.
      

     

    

    

     

sql

     

      

       The SELECT statement that you wish to execute in the remote
       database, for example SELECT * FROM pg_class.
      

     

    

    

     

fail_on_error

     

      

       If true (the default when omitted) then an error thrown on the
       remote side of the connection causes an error to also be thrown
       locally. If false, the remote error is locally reported as a NOTICE,
       and the function's return value is set to ERROR.
      

     

    

   

  

  
   
# Return Value

   

    Returns status, either OK or ERROR.
   

  

  
   
# Notes

   

    Since a cursor can only persist within a transaction,
    dblink_open starts an explicit transaction block
    (BEGIN) on the remote side, if the remote side was
    not already within a transaction.  This transaction will be
    closed again when the matching dblink_close is
    executed.  Note that if
    you use dblink_exec to change data between
    dblink_open and dblink_close,
    and then an error occurs or you use dblink_disconnect before
    dblink_close, your change will be
    lost because the transaction will be aborted.
   

  

  
   
# Examples

```
SELECT dblink_connect('dbname=postgres options=-csearch_path=');
 dblink_connect
----------------
 OK
(1 row)

SELECT dblink_open('foo', 'SELECT proname, prosrc FROM pg_proc');
 dblink_open
-------------
 OK
(1 row)
```

  
 

 
  
   dblink_fetch
  

  
   dblink_fetch
   3
  

  
   dblink_fetch
   returns rows from an open cursor in a remote database
  

  

dblink_fetch(text cursorname, int howmany [, bool fail_on_error]) returns setof record
dblink_fetch(text connname, text cursorname, int howmany [, bool fail_on_error]) returns setof record

  

  
   
# Description

   

    dblink_fetch fetches rows from a cursor previously
    established by dblink_open.
   

  

  
   
# Arguments

   

    

     

connname

     

      

       Name of the connection to use; omit this parameter to use the
       unnamed connection.
      

     

    

    

     

cursorname

     

      

       The name of the cursor to fetch from.
      

     

    

    

     

howmany

     

      

       The maximum number of rows to retrieve. The next 
howmany

       rows are fetched, starting at the current cursor position, moving
       forward. Once the cursor has reached its end, no more rows are produced.
      

     

    

    

     

fail_on_error

     

      

       If true (the default when omitted) then an error thrown on the
       remote side of the connection causes an error to also be thrown
       locally. If false, the remote error is locally reported as a NOTICE,
       and the function returns no rows.
      

     

    

   

  

  
   
# Return Value

   

    The function returns the row(s) fetched from the cursor.  To use this
    function, you will need to specify the expected set of columns,
    as previously discussed for dblink.
   

  

  
   
# Notes

   

    On a mismatch between the number of return columns specified in the
    FROM clause, and the actual number of columns returned by the
    remote cursor, an error will be thrown. In this event, the remote cursor
    is still advanced by as many rows as it would have been if the error had
    not occurred.  The same is true for any other error occurring in the local
    query after the remote FETCH has been done.
   

  

  
   
# Examples

```
SELECT dblink_connect('dbname=postgres options=-csearch_path=');
 dblink_connect
----------------
 OK
(1 row)

SELECT dblink_open('foo', 'SELECT proname, prosrc FROM pg_proc WHERE proname LIKE ''bytea%''');
 dblink_open
-------------
 OK
(1 row)

SELECT * FROM dblink_fetch('foo', 5) AS (funcname name, source text);
 funcname |  source
----------+----------
 byteacat | byteacat
 byteacmp | byteacmp
 byteaeq  | byteaeq
 byteage  | byteage
 byteagt  | byteagt
(5 rows)

SELECT * FROM dblink_fetch('foo', 5) AS (funcname name, source text);
 funcname  |  source
-----------+-----------
 byteain   | byteain
 byteale   | byteale
 bytealike | bytealike
 bytealt   | bytealt
 byteane   | byteane
(5 rows)

SELECT * FROM dblink_fetch('foo', 5) AS (funcname name, source text);
  funcname  |   source
------------+------------
 byteanlike | byteanlike
 byteaout   | byteaout
(2 rows)

SELECT * FROM dblink_fetch('foo', 5) AS (funcname name, source text);
 funcname | source
----------+--------
(0 rows)
```

  
 

 
  
   dblink_close
  

  
   dblink_close
   3
  

  
   dblink_close
   closes a cursor in a remote database
  

  

dblink_close(text cursorname [, bool fail_on_error]) returns text
dblink_close(text connname, text cursorname [, bool fail_on_error]) returns text

  

  
   
# Description

   

    dblink_close closes a cursor previously opened with
    dblink_open.
   

  

  
   
# Arguments

   

    

     

connname

     

      

       Name of the connection to use; omit this parameter to use the
       unnamed connection.
      

     

    

    

     

cursorname

     

      

       The name of the cursor to close.
      

     

    

    

     

fail_on_error

     

      

       If true (the default when omitted) then an error thrown on the
       remote side of the connection causes an error to also be thrown
       locally. If false, the remote error is locally reported as a NOTICE,
       and the function's return value is set to ERROR.
      

     

    

   

  

  
   
# Return Value

   

    Returns status, either OK or ERROR.
   

  

  
   
# Notes

   

    If dblink_open started an explicit transaction block,
    and this is the last remaining open cursor in this connection,
    dblink_close will issue the matching COMMIT.
   

  

  
   
# Examples

```
SELECT dblink_connect('dbname=postgres options=-csearch_path=');
 dblink_connect
----------------
 OK
(1 row)

SELECT dblink_open('foo', 'SELECT proname, prosrc FROM pg_proc');
 dblink_open
-------------
 OK
(1 row)

SELECT dblink_close('foo');
 dblink_close
--------------
 OK
(1 row)
```

  
 

 
  
   dblink_get_connections
  

  
   dblink_get_connections
   3
  

  
   dblink_get_connections
   returns the names of all open named dblink connections
  

  

dblink_get_connections() returns text[]

  

  
   
# Description

   

    dblink_get_connections returns an array of the names
    of all open named dblink connections.
   

  

  
   
# Return Value

   
Returns a text array of connection names, or NULL if none.

  

  
   
# Examples

```
SELECT dblink_get_connections();
```

  
 

 
  
   dblink_error_message
  

  
   dblink_error_message
   3
  

  
   dblink_error_message
   gets last error message on the named connection
  

  

dblink_error_message(text connname) returns text

  

  
   
# Description

   

    dblink_error_message fetches the most recent remote
    error message for a given connection.
   

  

  
   
# Arguments

   

    

     

connname

     

      

       Name of the connection to use.
      

     

    

   

  

  
   
# Return Value

   

    Returns last error message, or OK if there has been
    no error in this connection.
   

  

  
   
# Notes

   

    When asynchronous queries are initiated by
    dblink_send_query, the error message associated with
    the connection might not get updated until the server's response message
    is consumed. This typically means that dblink_is_busy
    or dblink_get_result should be called prior to
    dblink_error_message, so that any error generated by
    the asynchronous query will be visible.
   

  

  
   
# Examples

```
SELECT dblink_error_message('dtest1');
```

  
 

 
  
   dblink_send_query
  

  
   dblink_send_query
   3
  

  
   dblink_send_query
   sends an async query to a remote database
  

  

dblink_send_query(text connname, text sql) returns int

  

  
   
# Description

   

    dblink_send_query sends a query to be executed
    asynchronously, that is, without immediately waiting for the result.
    There must not be an async query already in progress on the
    connection.
   

   

    After successfully dispatching an async query, completion status
    can be checked with dblink_is_busy, and the results
    are ultimately collected with dblink_get_result.
    It is also possible to attempt to cancel an active async query
    using dblink_cancel_query.
   

  

  
   
# Arguments

   

    

     

connname

     

      

       Name of the connection to use.
      

     

    

    

     

sql

     

      

       The SQL statement that you wish to execute in the remote database,
       for example SELECT * FROM pg_class.
      

     

    

   

  

  
   
# Return Value

   

    Returns 1 if the query was successfully dispatched, 0 otherwise.
   

  

  
   
# Examples

```
SELECT dblink_send_query('dtest1', 'SELECT * FROM foo WHERE f1 < 3');
```

  
 

 
  
   dblink_is_busy
  

  
   dblink_is_busy
   3
  

  
   dblink_is_busy
   checks if connection is busy with an async query
  

  

dblink_is_busy(text connname) returns int

  

  
   
# Description

   

    dblink_is_busy tests whether an async query is in progress.
   

  

  
   
# Arguments

   

    

     

connname

     

      

       Name of the connection to check.
      

     

    

   

  

  
   
# Return Value

   

    Returns 1 if connection is busy, 0 if it is not busy.
    If this function returns 0, it is guaranteed that
    dblink_get_result will not block.
   

  

  
   
# Examples

```
SELECT dblink_is_busy('dtest1');
```

  
 

 
  
   dblink_get_notify
  

  
   dblink_get_notify
   3
  

  
   dblink_get_notify
   retrieve async notifications on a connection
  

  

dblink_get_notify() returns setof (notify_name text, be_pid int, extra text)
dblink_get_notify(text connname) returns setof (notify_name text, be_pid int, extra text)

  

  
   
# Description

   

    dblink_get_notify retrieves notifications on either
    the unnamed connection, or on a named connection if specified.
    To receive notifications via dblink, LISTEN must
    first be issued, using dblink_exec.
    For details see  and .
   

  

  
   
# Arguments

   

    

     

connname

     

      

       The name of a named connection to get notifications on.
      

     

    

   

  

  
   
# Return Value

    
Returns setof (notify_name text, be_pid int, extra text), or an empty set if none.

  

  
   
# Examples

```
SELECT dblink_exec('LISTEN virtual');
 dblink_exec
-------------
 LISTEN
(1 row)

SELECT * FROM dblink_get_notify();
 notify_name | be_pid | extra
-------------+--------+-------
(0 rows)

NOTIFY virtual;
NOTIFY

SELECT * FROM dblink_get_notify();
 notify_name | be_pid | extra
-------------+--------+-------
 virtual     |   1229 |
(1 row)
```

  
 

 
  
   dblink_get_result
  

  
   dblink_get_result
   3
  

  
   dblink_get_result
   gets an async query result
  

  

dblink_get_result(text connname [, bool fail_on_error]) returns setof record

  

  
   
# Description

   

    dblink_get_result collects the results of an
    asynchronous query previously sent with dblink_send_query.
    If the query is not already completed, dblink_get_result
    will wait until it is.
   

  

  
   
# Arguments

   

    

     

connname

     

      

       Name of the connection to use.
      

     

    

    

     

fail_on_error

     

      

       If true (the default when omitted) then an error thrown on the
       remote side of the connection causes an error to also be thrown
       locally. If false, the remote error is locally reported as a NOTICE,
       and the function returns no rows.
      

     

    

   

  

  
   
# Return Value

   

    For an async query (that is, an SQL statement returning rows),
    the function returns the row(s) produced by the query.  To use this
    function, you will need to specify the expected set of columns,
    as previously discussed for dblink.
   

   

    For an async command (that is, an SQL statement not returning rows),
    the function returns a single row with a single text column containing
    the command's status string.  It is still necessary to specify that
    the result will have a single text column in the calling FROM
    clause.
   

  

  
   
# Notes

   

    This function must be called if
    dblink_send_query returned 1.
    It must be called once for each query
    sent, and one additional time to obtain an empty set result,
    before the connection can be used again.
   

   

    When using dblink_send_query and
    dblink_get_result, dblink fetches the entire
    remote query result before returning any of it to the local query
    processor.  If the query returns a large number of rows, this can result
    in transient memory bloat in the local session.  It may be better to open
    such a query as a cursor with dblink_open and then fetch a
    manageable number of rows at a time.  Alternatively, use plain
    dblink(), which avoids memory bloat by spooling large result
    sets to disk.
   

  

  
   
# Examples

```
contrib_regression=# SELECT dblink_connect('dtest1', 'dbname=contrib_regression');
 dblink_connect
----------------
 OK
(1 row)

contrib_regression=# SELECT * FROM
contrib_regression-# dblink_send_query('dtest1', 'SELECT * FROM foo WHERE f1 < 3') AS t1;
 t1
----
  1
(1 row)

contrib_regression=# SELECT * FROM dblink_get_result('dtest1') AS t1(f1 int, f2 text, f3 text[]);
 f1 | f2 |     f3
----+----+------------
  0 | a  | {a0,b0,c0}
  1 | b  | {a1,b1,c1}
  2 | c  | {a2,b2,c2}
(3 rows)

contrib_regression=# SELECT * FROM dblink_get_result('dtest1') AS t1(f1 int, f2 text, f3 text[]);
 f1 | f2 | f3
----+----+----
(0 rows)

contrib_regression=# SELECT * FROM
contrib_regression-# dblink_send_query('dtest1', 'SELECT * FROM foo WHERE f1 < 3; SELECT * FROM foo WHERE f1 > 6') AS t1;
 t1
----
  1
(1 row)

contrib_regression=# SELECT * FROM dblink_get_result('dtest1') AS t1(f1 int, f2 text, f3 text[]);
 f1 | f2 |     f3
----+----+------------
  0 | a  | {a0,b0,c0}
  1 | b  | {a1,b1,c1}
  2 | c  | {a2,b2,c2}
(3 rows)

contrib_regression=# SELECT * FROM dblink_get_result('dtest1') AS t1(f1 int, f2 text, f3 text[]);
 f1 | f2 |      f3
----+----+---------------
  7 | h  | {a7,b7,c7}
  8 | i  | {a8,b8,c8}
  9 | j  | {a9,b9,c9}
 10 | k  | {a10,b10,c10}
(4 rows)

contrib_regression=# SELECT * FROM dblink_get_result('dtest1') AS t1(f1 int, f2 text, f3 text[]);
 f1 | f2 | f3
----+----+----
(0 rows)
```

  
 

 
  
   dblink_cancel_query
  

  
   dblink_cancel_query
   3
  

  
   dblink_cancel_query
   cancels any active query on the named connection
  

  

dblink_cancel_query(text connname) returns text

  

  
   
# Description

   

    dblink_cancel_query attempts to cancel any query that
    is in progress on the named connection.  Note that this is not
    certain to succeed (since, for example, the remote query might
    already have finished).  A cancel request simply improves the
    odds that the query will fail soon.  You must still complete the
    normal query protocol, for example by calling
    dblink_get_result.
   

  

  
   
# Arguments

   

    

     

connname

     

      

       Name of the connection to use.
      

     

    

   

  

  
   
# Return Value

   

    Returns OK if the cancel request has been sent, or
    the text of an error message on failure.
   

  

  
   
# Examples

```
SELECT dblink_cancel_query('dtest1');
```

  
 

 
  
   dblink_get_pkey
  

  
   dblink_get_pkey
   3
  

  
   dblink_get_pkey
   returns the positions and field names of a relation's
    primary key fields
   
  

  

dblink_get_pkey(text relname) returns setof dblink_pkey_results

  

  
   
# Description

   

    dblink_get_pkey provides information about the primary
    key of a relation in the local database.  This is sometimes useful
    in generating queries to be sent to remote databases.
   

  

  
   
# Arguments

   

    

     

relname

     

      

       Name of a local relation, for example foo or
       myschema.mytab.  Include double quotes if the
       name is mixed-case or contains special characters, for
       example "FooBar"; without quotes, the string
       will be folded to lower case.
      

     

    

   

  

  
   
# Return Value

   

    Returns one row for each primary key field, or no rows if the relation
    has no primary key.  The result row type is defined as

```
CREATE TYPE dblink_pkey_results AS (position int, colname text);
```

    The position column simply runs from 1 to N;
    it is the number of the field within the primary key, not the number
    within the table's columns.
   

  

  
   
# Examples

```
CREATE TABLE foobar (
    f1 int,
    f2 int,
    f3 int,
    PRIMARY KEY (f1, f2, f3)
);
CREATE TABLE

SELECT * FROM dblink_get_pkey('foobar');
 position | colname
----------+---------
        1 | f1
        2 | f2
        3 | f3
(3 rows)
```

  
 

 
  
   dblink_build_sql_insert
  

  
   dblink_build_sql_insert
   3
  

  
   dblink_build_sql_insert
   
    builds an INSERT statement using a local tuple, replacing the
    primary key field values with alternative supplied values
   
  

  

dblink_build_sql_insert(text relname,
                        int2vector primary_key_attnums,
                        integer num_primary_key_atts,
                        text[] src_pk_att_vals_array,
                        text[] tgt_pk_att_vals_array) returns text

  

  
   
# Description

   

    dblink_build_sql_insert can be useful in doing selective
    replication of a local table to a remote database.  It selects a row
    from the local table based on primary key, and then builds an SQL
    INSERT command that will duplicate that row, but with
    the primary key values replaced by the values in the last argument.
    (To make an exact copy of the row, just specify the same values for
    the last two arguments.)
   

  

  
   
# Arguments

   

    

     

relname

     

      

       Name of a local relation, for example foo or
       myschema.mytab.  Include double quotes if the
       name is mixed-case or contains special characters, for
       example "FooBar"; without quotes, the string
       will be folded to lower case.
      

     

    

    

     

primary_key_attnums

     

      

       Attribute numbers (1-based) of the primary key fields,
       for example 1 2.
      

     

    

    

     

num_primary_key_atts

     

      

       The number of primary key fields.
      

     

    

    

     

src_pk_att_vals_array

     

      

       Values of the primary key fields to be used to look up the
       local tuple.  Each field is represented in text form.
       An error is thrown if there is no local row with these
       primary key values.
      

     

    

    

     

tgt_pk_att_vals_array

     

      

       Values of the primary key fields to be placed in the resulting
       INSERT command.  Each field is represented in text form.
      

     

    

   

  

  
   
# Return Value

   
Returns the requested SQL statement as text.

  

  
   
# Notes

   

    As of PostgreSQL 9.0, the attribute numbers in
    
primary_key_attnums
 are interpreted as logical
    column numbers, corresponding to the column's position in
    SELECT * FROM relname.  Previous versions interpreted the
    numbers as physical column positions.  There is a difference if any
    column(s) to the left of the indicated column have been dropped during
    the lifetime of the table.
   

  

  
   
# Examples

```
SELECT dblink_build_sql_insert('foo', '1 2', 2, '{"1", "a"}', '{"1", "b''a"}');
             dblink_build_sql_insert
--------------------------------------------------
 INSERT INTO foo(f1,f2,f3) VALUES('1','b''a','1')
(1 row)
```

  
 

 
  
   dblink_build_sql_delete
  

  
   dblink_build_sql_delete
   3
  

  
   dblink_build_sql_delete
   builds a DELETE statement using supplied values for primary
    key field values
   
  

  

dblink_build_sql_delete(text relname,
                        int2vector primary_key_attnums,
                        integer num_primary_key_atts,
                        text[] tgt_pk_att_vals_array) returns text

  

  
   
# Description

   

    dblink_build_sql_delete can be useful in doing selective
    replication of a local table to a remote database.  It builds an SQL
    DELETE command that will delete the row with the given
    primary key values.
   

  

  
   
# Arguments

   

    

     

relname

     

      

       Name of a local relation, for example foo or
       myschema.mytab.  Include double quotes if the
       name is mixed-case or contains special characters, for
       example "FooBar"; without quotes, the string
       will be folded to lower case.
      

     

    

    

     

primary_key_attnums

     

      

       Attribute numbers (1-based) of the primary key fields,
       for example 1 2.
      

     

    

    

     

num_primary_key_atts

     

      

       The number of primary key fields.
      

     

    

    

     

tgt_pk_att_vals_array

     

      

       Values of the primary key fields to be used in the resulting
       DELETE command.  Each field is represented in text form.
      

     

    

   

  

  
   
# Return Value

   
Returns the requested SQL statement as text.

  

  
   
# Notes

   

    As of PostgreSQL 9.0, the attribute numbers in
    
primary_key_attnums
 are interpreted as logical
    column numbers, corresponding to the column's position in
    SELECT * FROM relname.  Previous versions interpreted the
    numbers as physical column positions.  There is a difference if any
    column(s) to the left of the indicated column have been dropped during
    the lifetime of the table.
   

  

  
   
# Examples

```
SELECT dblink_build_sql_delete('"MyFoo"', '1 2', 2, '{"1", "b"}');
           dblink_build_sql_delete
---------------------------------------------
 DELETE FROM "MyFoo" WHERE f1='1' AND f2='b'
(1 row)
```

  
 

 
  
   dblink_build_sql_update
  

  
   dblink_build_sql_update
   3
  

  
   dblink_build_sql_update
   builds an UPDATE statement using a local tuple, replacing
    the primary key field values with alternative supplied values
   
  

  

dblink_build_sql_update(text relname,
                        int2vector primary_key_attnums,
                        integer num_primary_key_atts,
                        text[] src_pk_att_vals_array,
                        text[] tgt_pk_att_vals_array) returns text

  

  
   
# Description

   

    dblink_build_sql_update can be useful in doing selective
    replication of a local table to a remote database.  It selects a row
    from the local table based on primary key, and then builds an SQL
    UPDATE command that will duplicate that row, but with
    the primary key values replaced by the values in the last argument.
    (To make an exact copy of the row, just specify the same values for
    the last two arguments.)  The UPDATE command always assigns
    all fields of the row &mdash; the main difference between this and
    dblink_build_sql_insert is that it's assumed that
    the target row already exists in the remote table.
   

  

  
   
# Arguments

   

    

     

relname

     

      

       Name of a local relation, for example foo or
       myschema.mytab.  Include double quotes if the
       name is mixed-case or contains special characters, for
       example "FooBar"; without quotes, the string
       will be folded to lower case.
      

     

    

    

     

primary_key_attnums

     

      

       Attribute numbers (1-based) of the primary key fields,
       for example 1 2.
      

     

    

    

     

num_primary_key_atts

     

      

       The number of primary key fields.
      

     

    

    

     

src_pk_att_vals_array

     

      

       Values of the primary key fields to be used to look up the
       local tuple.  Each field is represented in text form.
       An error is thrown if there is no local row with these
       primary key values.
      

     

    

    

     

tgt_pk_att_vals_array

     

      

       Values of the primary key fields to be placed in the resulting
       UPDATE command.  Each field is represented in text form.
      

     

    

   

  

  
   
# Return Value

   
Returns the requested SQL statement as text.

  

  
   
# Notes

   

    As of PostgreSQL 9.0, the attribute numbers in
    
primary_key_attnums
 are interpreted as logical
    column numbers, corresponding to the column's position in
    SELECT * FROM relname.  Previous versions interpreted the
    numbers as physical column positions.  There is a difference if any
    column(s) to the left of the indicated column have been dropped during
    the lifetime of the table.
   

  

  
   
# Examples

```
SELECT dblink_build_sql_update('foo', '1 2', 2, '{"1", "a"}', '{"1", "b"}');
                   dblink_build_sql_update
-------------------------------------------------------------
 UPDATE foo SET f1='1',f2='b',f3='1' WHERE f1='1' AND f2='b'
(1 row)
```
