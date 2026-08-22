# libpq &mdash; C Library

 
  libpq
 

 
  C
 

 

  libpq is the C
  application programmer's interface to PostgreSQL.
  libpq is a set of library functions that allow
  client programs to pass queries to the PostgreSQL
  backend server and to receive the results of these queries.
 

 

  libpq is also the underlying engine for several
  other PostgreSQL application interfaces, including
  those written for C++, Perl, Python, Tcl and ECPG.
  So some aspects of libpq's behavior will be
  important to you if you use one of those packages.  In particular,
  ,
   and
  
  describe behavior that is visible to the user of any application
  that uses libpq.
 

 

  Some short programs are included at the end of this chapter () to show how
  to write programs that use libpq.  There are also several
  complete examples of libpq applications in the
  directory src/test/examples in the source code distribution.
 

 

  Client programs that use libpq must
  include the header file
  libpq-fe.hlibpq-fe.h
  and must link with the libpq library.
 

 

  
# Database Connection Control Functions

  

   The following functions deal with making a connection to a
   PostgreSQL backend server.  An
   application program can have several backend connections open at
   one time.  (One reason to do that is to access more than one
   database.)  Each connection is represented by a
   PGconnPGconn object, which
   is obtained from the function ,
   , or
   .  Note that these functions will always
   return a non-null object pointer, unless perhaps there is too
   little memory even to allocate the PGconn object.
   The  function should be called to check
   the return value for a successful connection before queries are sent
   via the connection object.

   
    

     If untrusted users have access to a database that has not adopted a
     secure schema usage pattern,
     begin each session by removing publicly-writable schemas from
     search_path.  One can set parameter key
     word options to
     value -csearch_path=.  Alternately, one can
     issue PQexec(conn, "SELECT
     pg_catalog.set_config('search_path', '', false)") after
     connecting.  This consideration is not specific
     to libpq; it applies to every interface for
     executing arbitrary SQL commands.
    

   

   
    

     On Unix, forking a process with open libpq connections can lead to
     unpredictable results because the parent and child processes share
     the same sockets and operating system resources.  For this reason,
     such usage is not recommended, though doing an exec from
     the child process to load a new executable is safe.
    

   

   

    

     
PQconnectdbParamsPQconnectdbParams

     

      

       Makes a new connection to the database server.

PGconn *PQconnectdbParams(const char * const *keywords,
                          const char * const *values,
                          int expand_dbname);

      

      

       This function opens a new database connection using the parameters taken
       from two NULL-terminated arrays. The first,
       keywords, is defined as an array of strings, each one
       being a key word. The second, values, gives the value
       for each key word. Unlike  below, the parameter
       set can be extended without changing the function signature, so use of
       this function (or its nonblocking analogs 
       and PQconnectPoll) is preferred for new application
       programming.
      

      

       The currently recognized parameter key words are listed in
       .
      

      

       The passed arrays can be empty to use all default parameters, or can
       contain one or more parameter settings. They must be matched in length.
       Processing will stop at the first NULL entry
       in the keywords array.
       Also, if the values entry associated with a
       non-NULL keywords entry is
       NULL or an empty string, that entry is ignored and
       processing continues with the next pair of array entries.
      

      

       When expand_dbname is non-zero, the value for
       the first 
dbname
 key word is checked to see
       if it is a connection string.  If so, it
       is expanded into the individual connection
       parameters extracted from the string.  The value is considered to
       be a connection string, rather than just a database name, if it
       contains an equal sign (=) or it begins with a
       URI scheme designator.  (More details on connection string formats
       appear in .)  Only the first
       occurrence of 
dbname
 is treated in this way;
       any subsequent 
dbname
 parameter is processed
       as a plain database name.
      

      

       In general the parameter arrays are processed from start to end.
       If any key word is repeated, the last value (that is
       not NULL or empty) is used.  This rule applies in
       particular when a key word found in a connection string conflicts
       with one appearing in the keywords array.  Thus,
       the programmer may determine whether array entries can override or
       be overridden by values taken from a connection string.  Array
       entries appearing before an expanded 
dbname

       entry can be overridden by fields of the connection string, and in
       turn those fields are overridden by array entries appearing
       after 
dbname
 (but, again, only if those
       entries supply non-empty values).
      

      

       After processing all the array entries and any expanded connection
       string, any connection parameters that remain unset are filled with
       default values.  If an unset parameter's corresponding environment
       variable (see ) is set, its value is
       used.  If the environment variable is not set either, then the
       parameter's built-in default value is used.
      

     

    

    

     
PQconnectdbPQconnectdb

     

      

       Makes a new connection to the database server.

PGconn *PQconnectdb(const char *conninfo);

      

      

       This function opens a new database connection using the parameters taken
       from the string conninfo.
      

      

       The passed string can be empty to use all default parameters, or it can
       contain one or more parameter settings separated by whitespace,
       or it can contain a URI.
       See  for details.
     

    

    

    

     
PQsetdbLoginPQsetdbLogin

     

      

       Makes a new connection to the database server.

PGconn *PQsetdbLogin(const char *pghost,
                     const char *pgport,
                     const char *pgoptions,
                     const char *pgtty,
                     const char *dbName,
                     const char *login,
                     const char *pwd);

       

       

        This is the predecessor of  with a fixed
        set of parameters.  It has the same functionality except that the
        missing parameters will always take on default values.  Write NULL or an
        empty string for any one of the fixed parameters that is to be defaulted.
      

      

        If the 
dbName
 contains
        an = sign or has a valid connection URI prefix, it
        is taken as a 
conninfo
 string in exactly the same way as
        if it had been passed to , and the remaining
        parameters are then applied as specified for .
      

      

        pgtty is no longer used and any value passed will
        be ignored.
      

     

    

    

     
PQsetdbPQsetdb

     

      

   Makes a new connection to the database server.

PGconn *PQsetdb(char *pghost,
                char *pgport,
                char *pgoptions,
                char *pgtty,
                char *dbName);

     

     

      This is a macro that calls  with null pointers
      for the 
login
 and 
pwd
 parameters.  It is provided
      for backward compatibility with very old programs.
     

     

    

    

     
PQconnectStartParamsPQconnectStartParams

     
PQconnectStartPQconnectStart

     
PQconnectPollPQconnectPoll

     

      

       nonblocking connection
       Make a connection to the database server in a nonblocking manner.

PGconn *PQconnectStartParams(const char * const *keywords,
                             const char * const *values,
                             int expand_dbname);

PGconn *PQconnectStart(const char *conninfo);

PostgresPollingStatusType PQconnectPoll(PGconn *conn);

      

      

       These three functions are used to open a connection to a database server such
       that your application's thread of execution is not blocked on remote I/O
       whilst doing so. The point of this approach is that the waits for I/O to
       complete can occur in the application's main loop, rather than down inside
        or , and so the
       application can manage this operation in parallel with other activities.
      

      

       With , the database connection is made
       using the parameters taken from the keywords and
       values arrays, and controlled by expand_dbname,
       as described above for .
      

      

       With PQconnectStart, the database connection is made
       using the parameters taken from the string conninfo as
       described above for .
      

      

       Neither  nor PQconnectStart
       nor PQconnectPoll will block, so long as a number of
       restrictions are met:
       

        

         

          The hostaddr parameter must be used appropriately
          to prevent DNS queries from being made.  See the documentation of
          this parameter in  for details.
         

        

        

         

          If you call , ensure that the stream object
          into which you trace will not block.
         

        

        

         

          You must ensure that the socket is in the appropriate state
          before calling PQconnectPoll, as described below.
         

        

       

      

      

       To begin a nonblocking connection request,
       call PQconnectStart
       or .  If the result is null,
       then libpq has been unable to allocate a
       new PGconn structure.  Otherwise, a
       valid PGconn pointer is returned (though not
       yet representing a valid connection to the database).  Next
       call PQstatus(conn).  If the result
       is CONNECTION_BAD, the connection attempt has already
       failed, typically because of invalid connection parameters.
      

      

       If PQconnectStart
       or  succeeds, the next stage
       is to poll libpq so that it can proceed with
       the connection sequence.
       Use PQsocket(conn) to obtain the descriptor of the
       socket underlying the database connection.
       (Caution: do not assume that the socket remains the same
       across PQconnectPoll calls.)
       Loop thus: If PQconnectPoll(conn) last returned
       PGRES_POLLING_READING, wait until the socket is ready to
       read (as indicated by select(), poll(), or
       similar system function).  Note that PQsocketPoll
       can help reduce boilerplate by abstracting the setup of
       select(2) or poll(2) if it is
       available on your system.
       Then call PQconnectPoll(conn) again.
       Conversely, if PQconnectPoll(conn) last returned
       PGRES_POLLING_WRITING, wait until the socket is ready
       to write, then call PQconnectPoll(conn) again.
       On the first iteration, i.e., if you have yet to call
       PQconnectPoll, behave as if it last returned
       PGRES_POLLING_WRITING.  Continue this loop until
       PQconnectPoll(conn) returns
       PGRES_POLLING_FAILED, indicating the connection procedure
       has failed, or PGRES_POLLING_OK, indicating the connection
       has been successfully made.
      

      

       At any time during connection, the status of the connection can be
       checked by calling . If this call returns CONNECTION_BAD, then the
       connection procedure has failed; if the call returns CONNECTION_OK, then the
       connection is ready.  Both of these states are equally detectable
       from the return value of PQconnectPoll, described above. Other states might also occur
       during (and only during) an asynchronous connection procedure. These
       indicate the current stage of the connection procedure and might be useful
       to provide feedback to the user for example. These statuses are:

       

        

         
CONNECTION_STARTED

         

          

           Waiting for connection to be made.
          

         

        

        

         
CONNECTION_MADE

         

          

           Connection OK; waiting to send.
          

         

        

        

         
CONNECTION_AWAITING_RESPONSE

         

          

           Waiting for a response from the server.
          

         

        

        

         
CONNECTION_AUTH_OK

         

          

           Received authentication; waiting for backend start-up to finish.
          

         

        

        

         
CONNECTION_SSL_STARTUP

         

          

           Negotiating SSL encryption.
          

         

        

        

         
CONNECTION_GSS_STARTUP

         

          

           Negotiating GSS encryption.
          

         

        

        

         
CONNECTION_CHECK_WRITABLE

         

          

           Checking if connection is able to handle write transactions.
          

         

        

        

         
CONNECTION_CHECK_STANDBY

         

          

           Checking if connection is to a server in standby mode.
          

         

        

        

         
CONNECTION_CONSUME

         

          

           Consuming any remaining response messages on connection.
          

         

        

       

       Note that, although these constants will remain (in order to maintain
       compatibility), an application should never rely upon these occurring in a
       particular order, or at all, or on the status always being one of these
       documented values. An application might do something like this:

```
switch(PQstatus(conn))
{
        case CONNECTION_STARTED:
            feedback = "Connecting...";
            break;

        case CONNECTION_MADE:
            feedback = "Connected to server...";
            break;
.
.
.
        default:
            feedback = "Connecting...";
}
```

      

      

       The connect_timeout connection parameter is ignored
       when using PQconnectPoll; it is the application's
       responsibility to decide whether an excessive amount of time has elapsed.
       Otherwise, PQconnectStart followed by a
       PQconnectPoll loop is equivalent to
       .
      

      

       Note that when PQconnectStart
       or  returns a non-null
       pointer, you must call  when you are
       finished with it, in order to dispose of the structure and any
       associated memory blocks.  This must be done even if the connection
       attempt fails or is abandoned.
      

     

    

    

     
PQsocketPollPQsocketPoll

     

      

       nonblocking connection
       Poll a connection's underlying socket descriptor retrieved with
       .
       The primary use of this function is iterating through the connection
       sequence described in the documentation of
       .

typedef int64_t pg_usec_time_t;

int PQsocketPoll(int sock, int forRead, int forWrite,
                 pg_usec_time_t end_time);

      

      

       This function performs polling of a file descriptor, optionally with
       a timeout.
       If 
forRead
 is nonzero, the
       function will terminate when the socket is ready for
       reading. If 
forWrite
 is nonzero,
       the function will terminate when the
       socket is ready for writing.
      

      

       The timeout is specified by 
end_time
, which
       is the time to stop waiting expressed as a number of microseconds since
       the Unix epoch (that is, time_t times 1 million).
       Timeout is infinite if 
end_time

       is -1.  Timeout is immediate (no blocking) if
       
end_time
 is 0 (or indeed, any
       time before now).  Timeout values can be calculated conveniently by
       adding the desired number of microseconds to the result of
       .
       Note that the underlying system calls may have less than microsecond
       precision, so that the actual delay may be imprecise.
      

      

       The function returns a value greater than 0 if the
       specified condition is met, 0 if a timeout occurred,
       or -1 if an error occurred. The error can be
       retrieved by checking the errno(3) value. In the
       event both 
forRead

       and 
forWrite
 are zero, the function immediately
       returns a timeout indication.
      

      

       PQsocketPoll is implemented using either
       poll(2) or select(2),
       depending on platform.  See POLLIN
       and POLLOUT from poll(2),
       or 
readfds
 and
       
writefds
 from select(2),
       for more information.
      

     

    

    

     
PQconndefaultsPQconndefaults

     

      

       Returns the default connection options.

PQconninfoOption *PQconndefaults(void);

typedef struct
{
    char   *keyword;   /* The keyword of the option */
    char   *envvar;    /* Fallback environment variable name */
    char   *compiled;  /* Fallback compiled in default value */
    char   *val;       /* Option's current value, or NULL */
    char   *label;     /* Label for field in connect dialog */
    char   *dispchar;  /* Indicates how to display this field
                          in a connect dialog. Values are:
                          ""        Display entered value as is
                          "*"       Password field - hide value
                          "D"       Debug option - don't show by default */
    int     dispsize;  /* Field size in characters for dialog */
} PQconninfoOption;

      

      

       Returns a connection options array.  This can be used to determine
       all possible  options and their
       current default values.  The return value points to an array of
       PQconninfoOption structures, which ends
       with an entry having a null keyword pointer.  The
       null pointer is returned if memory could not be allocated. Note that
       the current default values (val fields)
       will depend on environment variables and other context.  A
       missing or invalid service file will be silently ignored.  Callers
       must treat the connection options data as read-only.
      

      

       After processing the options array, free it by passing it to
       .  If this is not done, a small amount of memory
       is leaked for each call to .
      

     

    

    

     
PQconninfoPQconninfo

     

      

       Returns the connection options used by a live connection.

PQconninfoOption *PQconninfo(PGconn *conn);

      

      

       Returns a connection options array.  This can be used to determine
       all possible  options and the
       values that were used to connect to the server. The return
       value points to an array of PQconninfoOption
       structures, which ends with an entry having a null keyword
       pointer. All notes above for  also
       apply to the result of .
      

     

    

    

     
PQconninfoParsePQconninfoParse

     

      

       Returns parsed connection options from the provided connection string.

PQconninfoOption *PQconninfoParse(const char *conninfo, char **errmsg);

      

      

       Parses a connection string and returns the resulting options as an
       array; or returns NULL if there is a problem with the connection
       string.  This function can be used to extract
       the  options in the provided
       connection string.  The return value points to an array of
       PQconninfoOption structures, which ends
       with an entry having a null keyword pointer.
      

      

       All legal options will be present in the result array, but the
       PQconninfoOption for any option not present
       in the connection string will have val set to
       NULL; default values are not inserted.
      

      

       If errmsg is not NULL, then *errmsg is set
       to NULL on success, else to a malloc'd error string explaining
       the problem.  (It is also possible for *errmsg to be
       set to NULL and the function to return NULL;
       this indicates an out-of-memory condition.)
      

      

       After processing the options array, free it by passing it to
       .  If this is not done, some memory
       is leaked for each call to .
       Conversely, if an error occurs and errmsg is not NULL,
       be sure to free the error string using .
      

   

    

    

     
PQfinishPQfinish

     

      

       Closes  the  connection to the server.  Also frees
       memory used by the PGconn object.

void PQfinish(PGconn *conn);

      

      

       Note that even if the server connection attempt fails (as
       indicated by ), the application should call 
       to free the memory used by the PGconn object.
       The PGconn pointer must not be used again after
        has been called.
      

     

    

    

     
PQresetPQreset

     

      

       Resets the communication channel to the server.

void PQreset(PGconn *conn);

      

      

       This function will close the connection
       to the server and attempt to establish a new
       connection, using all the same
       parameters previously used.  This might be useful for
       error recovery if a working connection is lost.
      

     

    

    

     
PQresetStartPQresetStart

     
PQresetPollPQresetPoll

     

      

       Reset the communication channel to the server, in a nonblocking manner.

int PQresetStart(PGconn *conn);

PostgresPollingStatusType PQresetPoll(PGconn *conn);

      

      

       These functions will close the connection to the server and attempt to
       establish a new connection, using all the same
       parameters previously used. This can be useful for error recovery if a
       working connection is lost. They differ from  (above) in that they
       act in a nonblocking manner. These functions suffer from the same
       restrictions as , PQconnectStart
       and PQconnectPoll.
      

      

       To initiate a connection reset, call
       . If it returns 0, the reset has
       failed. If it returns 1, poll the reset using
       PQresetPoll in exactly the same way as you
       would create the connection using PQconnectPoll.
      

     

    

    

     
PQpingParamsPQpingParams

     

      

        reports the status of the
       server.  It accepts connection parameters identical to those of
       , described above.  It is not
       necessary to supply correct user name, password, or database name
       values to obtain the server status; however, if incorrect values
       are provided, the server will log a failed connection attempt.

PGPing PQpingParams(const char * const *keywords,
                    const char * const *values,
                    int expand_dbname);

       The function returns one of the following values:

       

        

         
PQPING_OK

         

          

           The server is running and appears to be accepting connections.
          

         

        

        

         
PQPING_REJECT

         

          

           The server is running but is in a state that disallows connections
           (startup, shutdown, or crash recovery).
          

         

        

        

         
PQPING_NO_RESPONSE

         

          

           The server could not be contacted.  This might indicate that the
           server is not running, or that there is something wrong with the
           given connection parameters (for example, wrong port number), or
           that there is a network connectivity problem (for example, a
           firewall blocking the connection request).
          

         

        

        

         
PQPING_NO_ATTEMPT

         

          

           No attempt was made to contact the server, because the supplied
           parameters were obviously incorrect or there was some client-side
           problem (for example, out of memory).
          

         

        

       

      

     

    

    

     
PQpingPQping

     

      

        reports the status of the
       server.  It accepts connection parameters identical to those of
       , described above.  It is not
       necessary to supply correct user name, password, or database name
       values to obtain the server status; however, if incorrect values
       are provided, the server will log a failed connection attempt.

PGPing PQping(const char *conninfo);

      

      

       The return values are the same as for .
      

     

    

    

     
PQsetSSLKeyPassHook_OpenSSLPQsetSSLKeyPassHook_OpenSSL

     

      

       PQsetSSLKeyPassHook_OpenSSL lets an application override
       libpq's default
       handling of encrypted client certificate key files using
        or interactive prompting.

void PQsetSSLKeyPassHook_OpenSSL(PQsslKeyPassHook_OpenSSL_type hook);

       The application passes a pointer to a callback function with signature:

```
int callback_fn(char *buf, int size, PGconn *conn);
```

       which libpq will then call
       instead of its default
       PQdefaultSSLKeyPassHook_OpenSSL handler. The
       callback should determine the password for the key and copy it to
       result-buffer 
buf
 of size
       
size
. The string in 
buf

       must be null-terminated. The callback must return the length of the
       password stored in 
buf
 excluding the null
       terminator. On failure, the callback should set
       buf[0] = '\0' and return 0. See
       PQdefaultSSLKeyPassHook_OpenSSL in
       libpq's source code for an example.
      

      

       If the user specified an explicit key location,
       its path will be in conn->sslkey when the callback
       is invoked. This will be empty if the default key path is being used.
       For keys that are engine specifiers, it is up to engine implementations
       whether they use the OpenSSL password
       callback or define their own handling.
      

      

       The app callback may choose to delegate unhandled cases to
       PQdefaultSSLKeyPassHook_OpenSSL,
       or call it first and try something else if it returns 0, or completely override it.
      

      

       The callback must not escape normal flow control with exceptions,
       longjmp(...), etc. It must return normally.
      

     

    

    

     
PQgetSSLKeyPassHook_OpenSSLPQgetSSLKeyPassHook_OpenSSL

     

      

       PQgetSSLKeyPassHook_OpenSSL returns the current
       client certificate key password hook, or NULL
       if none has been set.

PQsslKeyPassHook_OpenSSL_type PQgetSSLKeyPassHook_OpenSSL(void);

      

     

    

   

  

  

   
# Connection Strings

   
    conninfo
   

   
    URI
   

   

    Several libpq functions parse a user-specified string to obtain
    connection parameters.  There are two accepted formats for these strings:
    plain keyword/value strings
    and URIs.  URIs generally follow
    RFC
    3986, except that multi-host connection strings are allowed
    as further described below.
   

   

    
# Keyword/Value Connection Strings

   

    In the keyword/value format, each parameter setting is in the form
    keyword =
    value, with space(s) between settings.
    Spaces around a setting's equal sign are
    optional. To write an empty value, or a value containing spaces, surround it
    with single quotes, for example keyword = 'a value'.
    Single quotes and backslashes within
    a value must be escaped with a backslash, i.e., \' and
    \\.
   

   

    Example:

```
host=localhost port=5432 dbname=mydb connect_timeout=10
```

   

   

    The recognized parameter key words are listed in .
   

   

   

    
# Connection URIs

   

   The general form for a connection URI is:

postgresql://userspec@hostspec/dbname?paramspec

where userspec is:

user:password

and hostspec is:

host:port,...

and paramspec is:

name=value&...

   

   

    The URI scheme designator can be either
    postgresql:// or postgres://.  Each
    of the remaining URI parts is optional.  The
    following examples illustrate valid URI syntax:

```
postgresql://
postgresql://localhost
postgresql://localhost:5433
postgresql://localhost/mydb
postgresql://user@localhost
postgresql://user:secret@localhost
postgresql://other@localhost/otherdb?connect_timeout=10&application_name=myapp
postgresql://host1:123,host2:456/somedb?target_session_attrs=any&application_name=myapp
```

    Values that would normally appear in the hierarchical part of
    the URI can alternatively be given as named
    parameters.  For example:

```
postgresql:///mydb?host=localhost&port=5433
```

    All named parameters must match key words listed in
    , except that for compatibility
    with JDBC connection URIs, instances
    of ssl=true are translated into
    sslmode=require.
   

   

    The connection URI needs to be encoded with percent-encoding
    if it includes symbols with special meaning in any of its parts.  Here is
    an example where the equal sign (=) is replaced with
    %3D and the space character with
    %20:

```
postgresql://user@localhost:5433/mydb?options=-c%20synchronous_commit%3Doff
```

   

   

    The host part may be either a host name or an IP address.  To specify an
    IPv6 address, enclose it in square brackets:

postgresql://[2001:db8::1234]/database

   

   

    The host part is interpreted as described for the parameter .  In particular, a Unix-domain socket
    connection is chosen if the host part is either empty or looks like an
    absolute path name,
    otherwise a TCP/IP connection is initiated.  Note, however, that the
    slash is a reserved character in the hierarchical part of the URI.  So, to
    specify a non-standard Unix-domain socket directory, either omit the host
    part of the URI and specify the host as a named parameter, or
    percent-encode the path in the host part of the URI:

```
postgresql:///dbname?host=/var/lib/postgresql
postgresql://%2Fvar%2Flib%2Fpostgresql/dbname
```

   

   

    It is possible to specify multiple host components, each with an optional
    port component, in a single URI.  A URI of the form
    postgresql://host1:port1,host2:port2,host3:port3/
    is equivalent to a connection string of the form
    host=host1,host2,host3 port=port1,port2,port3.
    As further described below, each
    host will be tried in turn until a connection is successfully established.
   

   

   

     
# Specifying Multiple Hosts

     

       It is possible to specify multiple hosts to connect to, so that they are
       tried in the given order. In the Keyword/Value format, the host,
       hostaddr, and port options accept comma-separated
       lists of values. The same number of elements must be given in each
       option that is specified, such
       that e.g., the first hostaddr corresponds to the first host name,
       the second hostaddr corresponds to the second host name, and so
       forth. As an exception, if only one port is specified, it
       applies to all the hosts.
     

     

       In the connection URI format, you can list multiple host:port pairs
       separated by commas in the host component of the URI.
     

     

       In either format, a single host name can translate to multiple network
       addresses. A common example of this is a host that has both an IPv4 and
       an IPv6 address.
     

     

       When multiple hosts are specified, or when a single host name is
       translated to multiple addresses,  all the hosts and addresses will be
       tried in order, until one succeeds. If none of the hosts can be reached,
       the connection fails. If a connection is established successfully, but
       authentication fails, the remaining hosts in the list are not tried.
     

     

       If a password file is used, you can have different passwords for
       different hosts. All the other connection options are the same for every
       host in the list; it is not possible to e.g., specify different
       usernames for different hosts.
     

   

  

  

   
# Parameter Key Words

   

    The currently recognized parameter key words are:

    

     

      
host

      

       

        Name of host to connect to.host
        name If a host name looks like an absolute path
        name, it specifies Unix-domain communication rather than TCP/IP
        communication; the value is the name of the directory in which the
        socket file is stored.  (On Unix, an absolute path name begins with a
        slash.  On Windows, paths starting with drive letters are also
        recognized.)  If the host name starts with @, it is
        taken as a Unix-domain socket in the abstract namespace (currently
        supported on Linux and Windows).
        The default behavior when host is not
        specified, or is empty, is to connect to a Unix-domain
        socketUnix domain socket in
        /tmp (or whatever socket directory was specified
        when PostgreSQL was built).  On Windows,
        the default is to connect to localhost.
       

       

        A comma-separated list of host names is also accepted, in which case
        each host name in the list is tried in order; an empty item in the
        list selects the default behavior as explained above. See
         for details.
       

      

     

     

      
hostaddr

      

       

        Numeric IP address of host to connect to.  This should be in the
        standard IPv4 address format, e.g., 172.28.40.9.  If
        your machine supports IPv6, you can also use those addresses.
        TCP/IP communication is
        always used when a nonempty string is specified for this parameter.
        If this parameter is not specified, the value of host
        will be looked up to find the corresponding IP address &mdash; or, if
        host specifies an IP address, that value will be
        used directly.
       

       

        Using hostaddr allows the
        application to avoid a host name look-up, which might be important
        in applications with time constraints. However, a host name is
        required for GSSAPI or SSPI authentication
        methods, as well as for verify-full SSL
        certificate verification.  The following rules are used:
        

         

          

           If host is specified
           without hostaddr, a host name lookup occurs.
           (When using PQconnectPoll, the lookup occurs
           when PQconnectPoll first considers this host
           name, and it may cause PQconnectPoll to block
           for a significant amount of time.)
          

         

         

          

           If hostaddr is specified without host,
           the value for hostaddr gives the server network address.
           The connection attempt will fail if the authentication
           method requires a host name.
          

         

         

          

           If both host and hostaddr are specified,
           the value for hostaddr gives the server network address.
           The value for host is ignored unless the
           authentication method requires it, in which case it will be
           used as the host name.
          

         

        

        Note that authentication is likely to fail if host
        is not the name of the server at network address hostaddr.
        Also, when both host and hostaddr
        are specified, host
        is used to identify the connection in a password file (see
        ).
       

       

        A comma-separated list of hostaddr values is also
        accepted, in which case each host in the list is tried in order.
        An empty item in the list causes the corresponding host name to be
        used, or the default host name if that is empty as well. See
         for details.
       

       

        Without either a host name or host address,
        libpq will connect using a local
        Unix-domain socket; or on Windows, it will attempt to connect to
        localhost.
       

       

      

      

       
port

       

       

        Port number to connect to at the server host, or socket file
        name extension for Unix-domain
        connections.port
        If multiple hosts were given in the host or
        hostaddr parameters, this parameter may specify a
        comma-separated list of ports of the same length as the host list, or
        it may specify a single port number to be used for all hosts.
        An empty string, or an empty item in a comma-separated list,
        specifies the default port number established
        when PostgreSQL was built.
       

      

     

     

      
dbname

      

      

       The database name.  Defaults to be the same as the user name.
       In certain contexts, the value is checked for extended
       formats; see  for more details on
       those.
      

      

     

     

      
user

      

      

       PostgreSQL user name to connect as.
       Defaults to be the same as the operating system name of the user
       running the application.
      

      

     

     

      
password

      

      

       Password to be used if the server demands password authentication.
      

      

     

     

      
passfile

      

      

       Specifies the name of the file used to store passwords
       (see ).
       Defaults to ~/.pgpass, or
       %APPDATA%\postgresql\pgpass.conf on Microsoft Windows.
       (No error is reported if this file does not exist.)
      

      

     

     

      
require_auth

      

      

        Specifies the authentication method that the client requires from the
        server. If the server does not use the required method to authenticate
        the client, or if the authentication handshake is not fully completed by
        the server, the connection will fail. A comma-separated list of methods
        may also be provided, of which the server must use exactly one in order
        for the connection to succeed. By default, any authentication method is
        accepted, and the server is free to skip authentication altogether.
      

      

        Methods may be negated with the addition of a !
        prefix, in which case the server must not attempt
        the listed method; any other method is accepted, and the server is free
        not to authenticate the client at all. If a comma-separated list is
        provided, the server may not attempt any of the
        listed negated methods. Negated and non-negated forms may not be
        combined in the same setting.
      

      

        As a final special case, the none method requires the
        server not to use an authentication challenge. (It may also be negated,
        to require some form of authentication.)
      

      

        The following methods may be specified:

        

         

          
password

          

           

            The server must request plaintext password authentication.
           

          

         

         

          
md5

          

           

            The server must request MD5 hashed password authentication.
           

           
            

             Support for MD5-encrypted passwords is deprecated and will be
             removed in a future release of
             PostgreSQL.  Refer to
              for details about migrating to
             another password type.
            

           
          

         

         

          
gss

          

           

            The server must either request a Kerberos handshake via
            GSSAPI or establish a
            GSS-encrypted channel (see also
            ).
           

          

         

         

          
sspi

          

           

            The server must request Windows SSPI
            authentication.
           

          

         

         

          
scram-sha-256

          

           

            The server must successfully complete a SCRAM-SHA-256 authentication
            exchange with the client.
           

          

         

         

          
oauth

          

           

            The server must request an OAuth bearer token from the client.
           

          

         

         

          
none

          

           

            The server must not prompt the client for an authentication
            exchange. (This does not prohibit client certificate authentication
            via TLS, nor GSS authentication via its encrypted transport.)
           

          

         

        

      

      

     

     

      
channel_binding

      

      

        This option controls the client's use of channel binding. A setting
        of require means that the connection must employ
        channel binding, prefer means that the client will
        choose channel binding if available, and disable
        prevents the use of channel binding. The default
        is prefer if
        PostgreSQL is compiled with SSL support;
        otherwise the default is disable.
      

      

        Channel binding is a method for the server to authenticate itself to
        the client. It is only supported over SSL connections
        with PostgreSQL 11 or later servers using
        the SCRAM authentication method.
      

      

     

     

      
connect_timeout

      

      

       Maximum time to wait while connecting, in seconds (write as a decimal integer,
       e.g., 10).  Zero, negative, or not specified means
       wait indefinitely.
       This timeout applies separately to each host name or IP address.
       For example, if you specify two hosts and connect_timeout
       is 5, each host will time out if no connection is made within 5
       seconds, so the total time spent waiting for a connection might be
       up to 10 seconds.
      

      

     

     

      
client_encoding

      

      

       This sets the client_encoding
       configuration parameter for this connection.  In addition to
       the values accepted by the corresponding server option, you
       can use auto to determine the right
       encoding from the current locale in the client
       (LC_CTYPE environment variable on Unix
       systems).
      

      

     

     

      
options

      

       

        Specifies command-line options to send to the server at connection
        start.  For example, setting this to -c geqo=off
        or --geqo=off sets the session's value of the
        geqo parameter to off.
        Spaces within this string are considered to
        separate command-line arguments, unless escaped with a backslash
        (\); write \\ to represent a literal
        backslash.  For a detailed discussion of the available
        options, consult .
       

      

     

     

      
application_name

      

       

        Specifies a value for the 
        configuration parameter.
       

      

     

     

      
fallback_application_name

      

       

        Specifies a fallback value for the  configuration parameter.
        This value will be used if no value has been given for
        application_name via a connection parameter or the
        PGAPPNAME environment variable.  Specifying
        a fallback name is useful in generic utility programs that
        wish to set a default application name but allow it to be
        overridden by the user.
       

      

     

     

      
keepalives

      

       

        Controls whether client-side TCP keepalives are used. The default
        value is 1, meaning on, but you can change this to 0, meaning off,
        if keepalives are not wanted.  This parameter is ignored for
        connections made via a Unix-domain socket.
       

      

     

     

      
keepalives_idle

      

       

        Controls the number of seconds of inactivity after which TCP should
        send a keepalive message to the server.  A value of zero uses the
        system default. This parameter is ignored for connections made via a
        Unix-domain socket, or if keepalives are disabled.
        It is only supported on systems where TCP_KEEPIDLE or
        an equivalent socket option is available, and on Windows; on other
        systems, it has no effect.
       

      

     

     

      
keepalives_interval

      

       

        Controls the number of seconds after which a TCP keepalive message
        that is not acknowledged by the server should be retransmitted.  A
        value of zero uses the system default. This parameter is ignored for
        connections made via a Unix-domain socket, or if keepalives are disabled.
        It is only supported on systems where TCP_KEEPINTVL or
        an equivalent socket option is available, and on Windows; on other
        systems, it has no effect.
       

      

     

     

      
keepalives_count

      

       

        Controls the number of TCP keepalives that can be lost before the
        client's connection to the server is considered dead.  A value of
        zero uses the system default. This parameter is ignored for
        connections made via a Unix-domain socket, or if keepalives are disabled.
        It is only supported on systems where TCP_KEEPCNT or
        an equivalent socket option is available; on other systems, it has no
        effect.
       

      

     

     

      
tcp_user_timeout

      

       

        Controls the number of milliseconds that transmitted data may
        remain unacknowledged before a connection is forcibly closed.
        A value of zero uses the system default. This parameter is
        ignored for connections made via a Unix-domain socket.
        It is only supported on systems where TCP_USER_TIMEOUT
        is available; on other systems, it has no effect.
       

      

     

     

      
replication

      

      

       This option determines whether the connection should use the
       replication protocol instead of the normal protocol.  This is what
       PostgreSQL replication connections as well as tools such as
       pg_basebackup use internally, but it can
       also be used by third-party applications.  For a description of the
       replication protocol, consult .
      

      

       The following values, which are case-insensitive, are supported:
       

        

         

          true, on,
          yes, 1
         

         

          

           The connection goes into physical replication mode.
          

         

        

        

         
database

         

          

           The connection goes into logical replication mode, connecting to
           the database specified in the dbname parameter.
          

         

        

        

         

          false, off,
          no, 0
         

         

          

           The connection is a regular one, which is the default behavior.
          

         

        

       

      

      

       In physical or logical replication mode, only the simple query protocol
       can be used.
      

      

     

     

      
gssencmode

      

       

        This option determines whether or with what priority a secure
        GSS TCP/IP connection will be negotiated with the
        server. There are three modes:

        

         

          
disable

          

           

            only try a non-GSSAPI-encrypted connection
           

          

         

         

          
prefer (default)

          

           

            if there are GSSAPI credentials present (i.e.,
            in a credentials cache), first try
            a GSSAPI-encrypted connection; if that fails or
            there are no credentials, try a
            non-GSSAPI-encrypted connection.  This is the
            default when PostgreSQL has been
            compiled with GSSAPI support.
           

          

         

         

          
require

          

           

            only try a GSSAPI-encrypted connection
           

          

         

        

       

       

        gssencmode is ignored for Unix domain socket
        communication.  If PostgreSQL is compiled
        without GSSAPI support, using the require option
        will cause an error, while prefer will be accepted
        but libpq will not actually attempt
        a GSSAPI-encrypted
        connection.GSSAPIwith
        libpq
       

      

     

     

      
sslmode

      

       

        This option determines whether or with what priority a secure
        SSL TCP/IP connection will be negotiated with the
        server. There are six modes:

        

         

          
disable

          

           

            only try a non-SSL connection
           

          

         

         

          
allow

          

           

            first try a non-SSL connection; if that
            fails, try an SSL connection
           

          

         

         

          
prefer (default)

          

           

            first try an SSL connection; if that fails,
            try a non-SSL connection
           

          

         

         

          
require

          

           

            only try an SSL connection. If a root CA
            file is present, verify the certificate in the same way as
            if verify-ca was specified
           

          

         

         

          
verify-ca

          

           

            only try an SSL connection, and verify that
            the server certificate is issued by a trusted
            certificate authority (CA)
           

          

         

         

          
verify-full

          

           

            only try an SSL connection, verify that the
            server certificate is issued by a
            trusted CA and that the requested server host name
            matches that in the certificate
           

          

         

        

        See  for a detailed description of how
        these options work.
       

       

        sslmode is ignored for Unix domain socket
        communication.
        If PostgreSQL is compiled without SSL support,
        using options require, verify-ca, or
        verify-full will cause an error, while
        options allow and prefer will be
        accepted but libpq will not actually attempt
        an SSL
        connection.SSLwith libpq
       

       

        Note that if GSSAPI encryption is possible,
        that will be used in preference to SSL
        encryption, regardless of the value of sslmode.
        To force use of SSL encryption in an
        environment that has working GSSAPI
        infrastructure (such as a Kerberos server), also set
        gssencmode to disable.
       

      

     

     

      
requiressl

      

       

        This option is deprecated in favor of the sslmode
        setting.
       

       

        If set to 1, an SSL connection to the server
        is required (this is equivalent to sslmode
        require).  libpq will then refuse
        to connect if the server does not accept an
        SSL connection.  If set to 0 (default),
        libpq will negotiate the connection type with
        the server (equivalent to sslmode
        prefer).  This option is only available if
        PostgreSQL is compiled with SSL support.
       

      

     

     

      
sslnegotiation

      

       

        This option controls how SSL encryption is negotiated with the server,
        if SSL is used. In the default postgres mode, the
        client first asks the server if SSL is supported. In
        direct mode, the client starts the standard SSL
        handshake directly after establishing the TCP/IP connection. Traditional
        PostgreSQL protocol negotiation is the most
        flexible with different server configurations. If the server is known
        to support direct SSL connections then the latter
        requires one fewer round trip reducing connection latency and also
        allows the use of protocol agnostic SSL network tools. The direct SSL
        option was introduced in PostgreSQL version
        17.
       

        

         

          
postgres

          

           

             perform PostgreSQL protocol
             negotiation. This is the default if the option is not provided.
           

          

         

         

          
direct

          

           

            start SSL handshake directly after establishing the TCP/IP
            connection.  This is only allowed with
            sslmode=require or higher, because the weaker
            settings could lead to unintended fallback to plaintext
            authentication when the server does not support direct SSL
            handshake.
           

          

         

        

      

     

     

      
sslcompression

      

       

        If set to 1, data sent over SSL connections will be compressed.  If
        set to 0, compression will be disabled.  The default is 0.  This
        parameter is ignored if a connection without SSL is made.
       

       

        SSL compression is nowadays considered insecure and its use is no
        longer recommended.  OpenSSL 1.1.0 disabled
        compression by default, and many operating system distributions
        disabled it in prior versions as well, so setting this parameter to on
        will not have any effect if the server does not accept compression.
        PostgreSQL 14 disabled compression
        completely in the backend.
       

       

        If security is not a primary concern, compression can improve
        throughput if the network is the bottleneck.  Disabling compression
        can improve response time and throughput if CPU performance is the
        limiting factor.
       

      

     

     

      
sslcert

      

       

        This parameter specifies the file name of the client SSL
        certificate, replacing the default
        ~/.postgresql/postgresql.crt.
        This parameter is ignored if an SSL connection is not made.
       

      

     

     

      
sslkey

      

       

        This parameter specifies the location for the secret key used for
        the client certificate. It can either specify a file name that will
        be used instead of the default
        ~/.postgresql/postgresql.key, or it can specify a key
        obtained from an external engine (engines are
        OpenSSL loadable modules).  An external engine
        specification should consist of a colon-separated engine name and
        an engine-specific key identifier.  This parameter is ignored if an
        SSL connection is not made.
       

      

     

     

      
sslkeylogfile

      

       

        This parameter specifies the location where libpq
        will log keys used in this SSL context.  This is useful for debugging
        PostgreSQL protocol interactions or client
        connections using network inspection tools like
        Wireshark.  This parameter is ignored if an
        SSL connection is not made, or if LibreSSL
        is used (LibreSSL does not support key
        logging).  Keys are logged using the NSS
        format.
        
         

          Key logging will expose potentially sensitive information in the
          keylog file.  Keylog files should be handled with the same care as
           files.
         

        
       

      

     

     

      
sslpassword

      

       

        This parameter specifies the password for the secret key specified in
        sslkey, allowing client certificate private keys
        to be stored in encrypted form on disk even when interactive passphrase
        input is not practical.
       

       

        Specifying this parameter with any non-empty value suppresses the
        Enter PEM pass phrase:
        prompt that OpenSSL will emit by default
        when an encrypted client certificate key is provided to
        libpq.
       

       

        If the key is not encrypted this parameter is ignored. The parameter
        has no effect on keys specified by OpenSSL
        engines unless the engine uses the OpenSSL
        password callback mechanism for prompts.
       

       

        There is no environment variable equivalent to this option, and no
        facility for looking it up in .pgpass. It can be
        used in a service file connection definition. Users with
        more sophisticated uses should consider using OpenSSL engines and
        tools like PKCS#11 or USB crypto offload devices.
       

      

     

     

      
sslcertmode

      

       

        This option determines whether a client certificate may be sent to the
        server, and whether the server is required to request one. There are
        three modes:

        

         

          
disable

          

           

            A client certificate is never sent, even if one is available
            (default location or provided via
            ).
           

          

         

         

          
allow (default)

          

           

            A certificate may be sent, if the server requests one and the
            client has one to send.
           

          

         

         

          
require

          

           

            The server must request a certificate. The
            connection will fail if the client does not send a certificate and
            the server successfully authenticates the client anyway.
           

          

         

        

       

       
        

         sslcertmode=require doesn't add any additional
         security, since there is no guarantee that the server is validating
         the certificate correctly; PostgreSQL servers generally request TLS
         certificates from clients whether they validate them or not. The
         option may be useful when troubleshooting more complicated TLS
         setups.
        

       
      

     

     

      
sslrootcert

      

       

        This parameter specifies the name of a file containing SSL
        certificate authority (CA) certificate(s).
        If the file exists, the server's certificate will be verified
        to be signed by one of these authorities.  The default is
        ~/.postgresql/root.crt.
       

       

        The special value system may be specified instead, in
        which case the trusted CA roots from the SSL implementation will be loaded. The exact
        locations of these root certificates differ by SSL implementation and
        platform. For OpenSSL in particular, the
        locations may be further modified by the SSL_CERT_DIR
        and SSL_CERT_FILE environment variables.
       

       
        

         When using sslrootcert=system, the default
         sslmode is changed to verify-full,
         and any weaker setting will result in an error. In most cases it is
         trivial for anyone to obtain a certificate trusted by the system for a
         hostname they control, rendering verify-ca and all
         weaker modes useless.
        

        

         The magic system value will take precedence over a
         local certificate file with the same name. If for some reason you find
         yourself in this situation, use an alternative path like
         sslrootcert=./system instead.
        

       
      

     

     

      
sslcrl

      

       

        This parameter specifies the file name of the SSL server certificate
        revocation list (CRL).  Certificates listed in this file, if it
        exists, will be rejected while attempting to authenticate the
        server's certificate.  If neither
         nor
         is set, this setting is
        taken as
        ~/.postgresql/root.crl.
       

      

     

     

      
sslcrldir

      

       

        This parameter specifies the directory name of the SSL server certificate
        revocation list (CRL).  Certificates listed in the files in this
        directory, if it exists, will be rejected while attempting to
        authenticate the server's certificate.
       

       

        The directory needs to be prepared with the
        OpenSSL command
        openssl rehash or c_rehash.  See
        its documentation for details.
       

       

        Both sslcrl and sslcrldir can be
        specified together.
       

      

     

     

      
sslsniServer Name Indication

      

       

        If set to 1 (default), libpq sets the TLS extension Server Name
        Indication (SNI) on SSL-enabled connections.
        By setting this parameter to 0, this is turned off.
       

       

        The Server Name Indication can be used by SSL-aware proxies to route
        connections without having to decrypt the SSL stream.  (Note that
        unless the proxy is aware of the PostgreSQL protocol handshake this
        would require setting sslnegotiation
        to direct.)
        However, SNI makes the destination host name appear
        in cleartext in the network traffic, so it might be undesirable in
        some cases.
       

      

     

     

      
requirepeer

      

       

        This parameter specifies the operating-system user name of the
        server, for example requirepeer=postgres.
        When making a Unix-domain socket connection, if this
        parameter is set, the client checks at the beginning of the
        connection that the server process is running under the specified
        user name; if it is not, the connection is aborted with an error.
        This parameter can be used to provide server authentication similar
        to that available with SSL certificates on TCP/IP connections.
        (Note that if the Unix-domain socket is in
        /tmp or another publicly writable location,
        any user could start a server listening there.  Use this parameter
        to ensure that you are connected to a server run by a trusted user.)
        This option is only supported on platforms for which the
        peer authentication method is implemented; see
        .
       

      

     

     

      
ssl_min_protocol_version

      

       

        This parameter specifies the minimum SSL/TLS protocol version to allow
        for the connection. Valid values are TLSv1,
        TLSv1.1, TLSv1.2 and
        TLSv1.3. The supported protocols depend on the
        version of OpenSSL used, older versions
        not supporting the most modern protocol versions. If not specified,
        the default is TLSv1.2, which satisfies industry
        best practices as of this writing.
       

      

     

     

      
ssl_max_protocol_version

      

       

        This parameter specifies the maximum SSL/TLS protocol version to allow
        for the connection. Valid values are TLSv1,
        TLSv1.1, TLSv1.2 and
        TLSv1.3. The supported protocols depend on the
        version of OpenSSL used, older versions
        not supporting the most modern protocol versions. If not set, this
        parameter is ignored and the connection will use the maximum bound
        defined by the backend, if set. Setting the maximum protocol version
        is mainly useful for testing or if some component has issues working
        with a newer protocol.
       

      

     

     

      
min_protocol_version

      

       

        Specifies the minimum protocol version to allow for the connection.
        The default is to allow any version of the
        PostgreSQL protocol supported by libpq,
        which currently means 3.0. If the server
        does not support at least this protocol version the connection will be
        closed.
       

       

        The current supported values are
        3.0, 3.2,
        and latest. The latest value is
        equivalent to the latest protocol version supported by the libpq
        version being used, which is currently 3.2.
       

      

     

     

      
max_protocol_version

      

       
        

        During the PostgreSQL 19 beta period, libpq connections that do not
        specify a max_protocol_version will "grease" the
        handshake by sending unsupported startup parameters, including version
        3.9999, in order to identify software that does not
        correctly negotiate the connection. This replaces the default behavior
        described below.
        

        

        If you know that a server doesn't properly implement protocol version
        negotiation, you can set max_protocol_version=3.0 to
        revert to the standard behavior (preferably after notifying the server's
        maintainers that their software needs to be fixed).
        

       

       

        Specifies the protocol version to request from the server.
        The default is to use version 3.0 of the
        PostgreSQL protocol, unless the connection
        string specifies a feature that relies on a higher protocol version,
        in which case the latest version supported by libpq is used. If the
        server does not support the protocol version requested by the client,
        the connection is automatically downgraded to a lower minor protocol
        version that the server supports. After the connection attempt has
        completed you can use  to
        find out which exact protocol version was negotiated.
       

       

        The current supported values are
        3.0, 3.2,
        and latest. The latest value is
        equivalent to the latest protocol version supported by the libpq
        version being used, which is currently 3.2.
       

      

     

     

      
krbsrvname

      

       

        Kerberos service name to use when authenticating with GSSAPI.
        This must match the service name specified in the server
        configuration for Kerberos authentication to succeed. (See also
        .)
        The default value is normally postgres,
        but that can be changed when
        building PostgreSQL via
        the --with-krb-srvnam option
        of configure.
        In most environments, this parameter never needs to be changed.
        Some Kerberos implementations might require a different service name,
        such as Microsoft Active Directory which requires the service name
        to be in upper case (POSTGRES).
       

      

     

     

      
gsslib

      

       

        GSS library to use for GSSAPI authentication.
        Currently this is disregarded except on Windows builds that include
        both GSSAPI and SSPI support.  In that case, set
        this to gssapi to cause libpq to use the GSSAPI
        library for authentication instead of the default SSPI.
       

      

     

     

      
gssdelegation

      

       

        Forward (delegate) GSS credentials to the server.  The default is
        0 which means credentials will not be forwarded
        to the server.  Set this to 1 to have credentials
        forwarded when possible.
       

      

     

     

      
scram_client_key

      

       

        The base64-encoded SCRAM client key.  This can be used by foreign-data
        wrappers or similar middleware to enable pass-through SCRAM
        authentication. See  for one such
        implementation.  It is not meant to be specified directly by users or
        client applications.
       

      

     

     

      
scram_server_key

      

       

        The base64-encoded SCRAM server key.  This can be used by foreign-data
        wrappers or similar middleware to enable pass-through SCRAM
        authentication. See  for one such
        implementation.  It is not meant to be specified directly by users or
        client applications.
       

      

     

     

      
service

      

       

        Service name to use for additional parameters.  It specifies a service
        name in pg_service.conf that holds additional connection parameters.
        This allows applications to specify only a service name so connection parameters
        can be centrally maintained. See .
       

      

     

     

      
servicefile

      

       

        This option specifies the name of the per-user connection service file
        (see ).
        Defaults to ~/.pg_service.conf, or
        %APPDATA%\postgresql\.pg_service.conf on
        Microsoft Windows.
       

      

     

     

      
target_session_attrs

      

       

        This option determines whether the session must have certain
        properties to be acceptable.  It's typically used in combination
        with multiple host names to select the first acceptable alternative
        among several hosts.  There are six modes:

        

         

          
any (default)

          

           

            any successful connection is acceptable
           

          

         

         

          
read-write

          

           

            session must accept read-write transactions by default (that
            is, the server must not be in hot standby mode and
            the default_transaction_read_only parameter
            must be off)
           

          

         

         

          
read-only

          

           

            session must not accept read-write transactions by default (the
            converse)
           

          

         

         

          
primary

          

           

            server must not be in hot standby mode
           

          

         

         

          
standby

          

           

            server must be in hot standby mode
           

          

         

         

          
prefer-standby

          

           

            first try to find a standby server, but if none of the listed
            hosts is a standby server, try again in any
            mode
           

          

         

        

       

      

     

     

      
load_balance_hosts

      

       

        Controls the order in which the client tries to connect to the available
        hosts and addresses. Once a connection attempt is successful no other
        hosts and addresses will be tried. This parameter is typically used in
        combination with multiple host names or a DNS record that returns
        multiple IPs. This parameter can be used in combination with
        
        to, for example, load balance over standby servers only. Once successfully
        connected, subsequent queries on the returned connection will all be
        sent to the same server. There are currently two modes:
        

         

          
disable (default)

          

           

            No load balancing across hosts is performed.  Hosts are tried in
            the order in which they are provided and addresses are tried in
            the order they are received from DNS or a hosts file.
           

          

         

         

          
random

          

           

            Hosts and addresses are tried in random order. This value is mostly
            useful when opening multiple connections at the same time, possibly
            from different machines. This way connections can be load balanced
            across multiple PostgreSQL servers.
           

           

            While random load balancing, due to its random nature, will almost
            never result in a completely uniform distribution, it statistically
            gets quite close. One important aspect here is that this algorithm
            uses two levels of random choices: First the hosts
            will be resolved in random order. Then secondly, before resolving
            the next host, all resolved addresses for the current host will be
            tried in random order. This behaviour can skew the amount of
            connections each node gets greatly in certain cases, for instance
            when some hosts resolve to more addresses than others. But such a
            skew can also be used on purpose, e.g. to increase the number of
            connections a larger server gets by providing its hostname multiple
            times in the host string.
           

           

            When using this value it's recommended to also configure a reasonable
            value for . Because then,
            if one of the nodes that are used for load balancing is not responding,
            a new node will be tried.
           

          

         

        

       

      

     

     

      
oauth_issuer

      

       

        The HTTPS URL of a trusted issuer to contact if the server requests an
        OAuth token for the connection. This parameter is required for all OAuth
        connections; it should exactly match the issuer
        setting in the server's HBA configuration.
       

       

        As part of the standard authentication handshake, libpq
        will ask the server for a discovery document: a URL
        providing a set of OAuth configuration parameters. The server must
        provide a URL that is directly constructed from the components of the
        oauth_issuer, and this value must exactly match the
        issuer identifier that is declared in the discovery document itself, or
        the connection will fail. This is required to prevent a class of
        
        "mix-up attacks" on OAuth clients.
       

       

        You may also explicitly set oauth_issuer to the
        /.well-known/ URI used for OAuth discovery. In this
        case, if the server asks for a different URL, the connection will fail,
        but a custom OAuth flow
        may be able to speed up the standard handshake by using previously
        cached tokens. (In this case, it is recommended that
         be set as well, since the
        client will not have a chance to ask the server for a correct scope
        setting, and the default scopes for a token may not be sufficient to
        connect.) libpq currently supports the
        following well-known endpoints:
        

         

/.well-known/openid-configuration

         

/.well-known/oauth-authorization-server

        

       

       
        

         Issuers are highly privileged during the OAuth connection handshake. As
         a rule of thumb, if you would not trust the operator of a URL to handle
         access to your servers, or to impersonate you directly, that URL should
         not be trusted as an oauth_issuer.
        

       
      

     

     

      
oauth_client_id

      

       

        An OAuth 2.0 client identifier, as issued by the authorization server.
        If the PostgreSQL server
        requests an OAuth token for the
        connection (and if no custom
        OAuth hook is installed to provide one), then this parameter must
        be set; otherwise, the connection will fail.
       

      

     

     

      
oauth_client_secret

      

       

        The client password, if any, to use when contacting the OAuth
        authorization server. Whether this parameter is required or not is
        determined by the OAuth provider; "public" clients generally do not use
        a secret, whereas "confidential" clients generally do.
       

      

     

     

      
oauth_scope

      

       

        The scope of the access request sent to the authorization server,
        specified as a (possibly empty) space-separated list of OAuth scope
        identifiers. This parameter is optional and intended for advanced usage.
       

       

        Usually the client will obtain appropriate scope settings from the
        PostgreSQL server. If this parameter is used,
        the server's requested scope list will be ignored. This can prevent a
        less-trusted server from requesting inappropriate access scopes from the
        end user. However, if the client's scope setting does not contain the
        server's required scopes, the server is likely to reject the issued
        token, and the connection will fail.
       

       

        The meaning of an empty scope list is provider-dependent. An OAuth
        authorization server may choose to issue a token with "default scope",
        whatever that happens to be, or it may reject the token request
        entirely.
       

      

     

     

      
oauth_ca_file

      

       

        The name of a file containing one or more SSL certificate authority
        (CA) certificates, which will be used to verify the
        identity of the authorization server and its endpoints. By default, the
        Curl system certificate bundle is used.
       

       

        This parameter does not affect verification of the
        PostgreSQL server certificate; see
         instead.
       

      

     

    

   

  

 

 

  
# Connection Status Functions

  

   These functions can be used to interrogate the status
   of an existing database connection object.
  

  
   

    libpq-fe.h
    libpq-int.h
    libpq application programmers should be careful to
    maintain the PGconn abstraction.  Use the accessor
    functions described below to get at the contents of PGconn.
    Reference to internal PGconn fields using
    libpq-int.h is not recommended because they are subject to change
    in the future.
   

  

  

   The following functions return parameter values established at connection.
   These values are fixed for the life of the connection.  If a multi-host
   connection string is used, the values of ,
   , and  can change if a new connection
   is established using the same PGconn object.  Other values
   are fixed for the lifetime of the PGconn object.

   

    

     
PQdbPQdb

     

      

       Returns the database name of the connection.

char *PQdb(const PGconn *conn);

      

     

    

    

     
PQuserPQuser

     

      

       Returns the user name of the connection.

char *PQuser(const PGconn *conn);

      

     

    

    

     
PQpassPQpass

     

      

       Returns the password of the connection.

char *PQpass(const PGconn *conn);

      

      

        will return either the password specified
       in the connection parameters, or if there was none and the password
       was obtained from the password
       file, it will return that.  In the latter case,
       if multiple hosts were specified in the connection parameters, it is
       not possible to rely on the result of  until
       the connection is established.  The status of the connection can be
       checked using the function .
      

     

    

    

     
PQhostPQhost

     

      

       Returns the server host name of the active connection.
       This can be a host name, an IP address, or a directory path if the
       connection is via Unix socket.  (The path case can be distinguished
       because it will always be an absolute path, beginning
       with /.)

char *PQhost(const PGconn *conn);

      

      

       If the connection parameters specified both host and
       hostaddr, then  will
       return the host information.  If only
       hostaddr was specified, then that is returned.
       If multiple hosts were specified in the connection parameters,
        returns the host actually connected to.
      

      

        returns NULL if the
       
conn
 argument is NULL.
       Otherwise, if there is an error producing the host information (perhaps
       if the connection has not been fully established or there was an
       error), it returns an empty string.
      

      

       If multiple hosts were specified in the connection parameters, it is
       not possible to rely on the result of  until
       the connection is established.  The status of the connection can be
       checked using the function .
      

     

    

    

     
PQhostaddrPQhostaddr

     

      

       Returns the server IP address of the active connection.
       This can be the address that a host name resolved to,
       or an IP address provided through the hostaddr
       parameter.

char *PQhostaddr(const PGconn *conn);

      

      

        returns NULL if the
       
conn
 argument is NULL.
       Otherwise, if there is an error producing the host information
       (perhaps if the connection has not been fully established or
       there was an error), it returns an empty string.
      

     

    

    

     
PQportPQport

     

      

       Returns the port of the active connection.

char *PQport(const PGconn *conn);

      

      

       If multiple ports were specified in the connection parameters,
        returns the port actually connected to.
      

      

        returns NULL if the
       
conn
 argument is NULL.
       Otherwise, if there is an error producing the port information (perhaps
       if the connection has not been fully established or there was an
       error), it returns an empty string.
      

      

       If multiple ports were specified in the connection parameters, it is
       not possible to rely on the result of  until
       the connection is established.  The status of the connection can be
       checked using the function .
      

     

    

    

     
PQttyPQtty

     

      

       This function no longer does anything, but it remains for backwards
       compatibility.  The function always return an empty string, or
       NULL if the 
conn
 argument is
       NULL.

char *PQtty(const PGconn *conn);

      

     

    

    

     
PQoptionsPQoptions

     

      

       Returns the command-line options passed in the connection request.

char *PQoptions(const PGconn *conn);

      

     

    

   

  

  

   The following functions return status data that can change as operations
   are executed on the PGconn object.

   

    

     
PQstatusPQstatus

     

      

       Returns the status of the connection.

ConnStatusType PQstatus(const PGconn *conn);

      

      

       The status can be one of a number of values.  However, only two of
       these are seen outside of an asynchronous connection procedure:
       CONNECTION_OK and
       CONNECTION_BAD. A good connection to the database
       has the status CONNECTION_OK.  A failed
       connection attempt is signaled by status
       CONNECTION_BAD.  Ordinarily, an OK status will
       remain so until , but a communications
       failure might result in the status changing to
       CONNECTION_BAD prematurely.  In that case the
       application could try to recover by calling
       .
      

      

       See the entry for , PQconnectStart
       and PQconnectPoll with regards to other status codes that
       might be returned.
      

     

    

    

     
PQtransactionStatusPQtransactionStatus

     

      

       Returns the current in-transaction status of the server.

PGTransactionStatusType PQtransactionStatus(const PGconn *conn);

       The status can be PQTRANS_IDLE (currently idle),
       PQTRANS_ACTIVE (a command is in progress),
       PQTRANS_INTRANS (idle, in a valid transaction block),
       or PQTRANS_INERROR (idle, in a failed transaction block).
       PQTRANS_UNKNOWN is reported if the connection is bad.
       PQTRANS_ACTIVE is reported only when a query
       has been sent to the server and not yet completed.
      

     

    

    

     
PQparameterStatusPQparameterStatus

     

      

       Looks up a current parameter setting of the server.

const char *PQparameterStatus(const PGconn *conn, const char *paramName);

       Certain parameter values are reported by the server automatically at
       connection startup or whenever their values change.
        can be used to interrogate these settings.
       It returns the current value of a parameter if known, or NULL
       if the parameter is not known.
      

      

       Parameters reported as of the current release include:
       
        application_name
        client_encoding
        DateStyle
        default_transaction_read_only
        in_hot_standby
        integer_datetimes
        IntervalStyle
        is_superuser
        scram_iterations
        search_path
        server_encoding
        server_version
        session_authorization
        standard_conforming_strings
        TimeZone
       
       (default_transaction_read_only and
       in_hot_standby were not reported by releases before
       14; scram_iterations was not reported by releases
       before 16; search_path was not reported by releases
       before 18.)
       Note that
       server_version and
       server_encoding
       cannot change after startup.
       Also, integer_datetimes is
       always on in releases 9.5 and later,
       and standard_conforming_strings is
       always on in releases 19 and later.
      

      

       Although the returned pointer is declared const, it in fact
       points to mutable storage associated with the PGconn structure.
       It is unwise to assume the pointer will remain valid across queries.
      

     

    

    

     
PQfullProtocolVersionPQfullProtocolVersion

     

      

       Interrogates the frontend/backend protocol being used.

int PQfullProtocolVersion(const PGconn *conn);

       Applications might wish to use this function to determine whether certain
       features are supported. The result is formed by multiplying the server's
       major version number by 10000 and adding the minor version number. For
       example, version 3.2 would be returned as 30002, and version 4.0 would
       be returned as 40000. Zero is returned if the connection is bad. The 3.0
       protocol is supported by PostgreSQL server
       versions 7.4 and above.
      

      

       The protocol version will not change after connection startup is
       complete, but it could theoretically change during a connection reset.
      

     

    

    

     
PQprotocolVersionPQprotocolVersion

     

      

       Interrogates the frontend/backend protocol major version.

int PQprotocolVersion(const PGconn *conn);

       Unlike , this returns only
       the major protocol version in use, but it is supported by a wider range
       of libpq releases back to version 7.4. Currently, the possible values are
       3 (3.0 protocol), or zero (connection bad). Prior to release version
       14.0, libpq could additionally return 2 (2.0 protocol).
      

     

    

    

     
PQserverVersionPQserverVersion

     

      

       Returns an integer representing the server version.

int PQserverVersion(const PGconn *conn);

      

      

       Applications might use this function to determine the version of the
       database server they are connected to.  The result is formed by
       multiplying the server's major version number by 10000 and adding
       the minor version number.  For example, version 10.1 will be
       returned as 100001, and version 11.0 will be returned as 110000.
       Zero is returned if the connection is bad.
      

      

       Prior to major version 10, PostgreSQL used
       three-part version numbers in which the first two parts together
       represented the major version.  For those
       versions,  uses two digits for each
       part; for example version 9.1.5 will be returned as 90105, and
       version 9.2.0 will be returned as 90200.
      

      

       Therefore, for purposes of determining feature compatibility,
       applications should divide the result of 
       by 100 not 10000 to determine a logical major version number.
       In all release series, only the last two digits differ between
       minor releases (bug-fix releases).
      

     

    

    

     

      PQerrorMessagePQerrorMessage
      error messagein PGconn
     

     

      

       Returns the error message most recently generated by
       an operation on the connection.

char *PQerrorMessage(const PGconn *conn);

      

      

       Nearly all libpq functions will set a message for
        if they fail.  Note that by
       libpq convention, a nonempty
        result can consist of multiple lines,
       and will include a trailing newline. The caller should not free
       the result directly. It will be freed when the associated
       PGconn handle is passed to
       .  The result string should not be
       expected to remain the same across operations on the
       PGconn structure.
      

     

    

    

     
PQsocketPQsocket

     

      

       Obtains the file descriptor number of the connection socket to
       the server.  A valid descriptor will be greater than or equal
       to 0; a result of -1 indicates that no server connection is
       currently open.  (This will not change during normal operation,
       but could change during connection setup or reset.)

int PQsocket(const PGconn *conn);

      

     

    

    

     
PQbackendPIDPQbackendPID

     

      

       Returns the process ID (PID)
        PID
        determining PID of server process
        in libpq
       
       of the backend process handling this connection.

int PQbackendPID(const PGconn *conn);

      

      

       The backend PID is useful for debugging
       purposes and for comparison to NOTIFY
       messages (which include the PID of the
       notifying backend process).  Note that the
       PID belongs to a process executing on the
       database server host, not the local host!
      

     

    

    

     
PQconnectionNeedsPasswordPQconnectionNeedsPassword

     

      

       Returns true (1) if the connection authentication method
       required a password, but none was available.
       Returns false (0) if not.

int PQconnectionNeedsPassword(const PGconn *conn);

      

      

       This function can be applied after a failed connection attempt
       to decide whether to prompt the user for a password.
      

     

    

    

     
PQconnectionUsedPasswordPQconnectionUsedPassword

     

      

       Returns true (1) if the connection authentication method
       used a password. Returns false (0) if not.

int PQconnectionUsedPassword(const PGconn *conn);

      

      

       This function can be applied after either a failed or successful
       connection attempt to detect whether the server demanded a password.
      

     

    

    

     
PQconnectionUsedGSSAPIPQconnectionUsedGSSAPI

     

      

       Returns true (1) if the connection authentication method
       used GSSAPI. Returns false (0) if not.

int PQconnectionUsedGSSAPI(const PGconn *conn);

      

      

       This function can be applied to detect whether the connection was
       authenticated with GSSAPI.
      

     

    

   

  

  

    The following functions return information related to SSL. This information
    usually doesn't change after a connection is established.

    

    

     
PQsslInUsePQsslInUse

     

      

        Returns true (1) if the connection uses SSL, false (0) if not.

int PQsslInUse(const PGconn *conn);

      

     

    

    

     
PQsslAttributePQsslAttribute

     

      

        Returns SSL-related information about the connection.

const char *PQsslAttribute(const PGconn *conn, const char *attribute_name);

      

      

       The list of available attributes varies depending on the SSL library
       being used and the type of connection.  Returns NULL if the connection
       does not use SSL or the specified attribute name is not defined for the
       library in use.
      

      

       The following attributes are commonly available:
       

        

         
library

          

           

            Name of the SSL implementation in use. (Currently, only
            "OpenSSL" is implemented)
           

          

         

        

         
protocol

          

           

             SSL/TLS version in use. Common values
             are "TLSv1", "TLSv1.1"
             and "TLSv1.2", but an implementation may
             return other strings if some other protocol is used.
           

          

         

        

         
key_bits

          

           

            Number of key bits used by the encryption algorithm.
           

          

         

        

         
cipher

          

           

            A short name of the ciphersuite used, e.g.,
            "DHE-RSA-DES-CBC3-SHA". The names are specific
            to each SSL implementation.
           

          

         

        

         
compression

          

           

            Returns "on" if SSL compression is in use, else it returns "off".
           

          

         

        

         
alpn

          

           

            Application protocol selected by the TLS Application-Layer
            Protocol Negotiation (ALPN) extension.  The only protocol
            supported by libpq is postgresql, so this is
            mainly useful for checking whether the server supported ALPN or
            not. Empty string if ALPN was not used.
           

          

         

       

      

      

       As a special case, the library attribute may be
       queried without a connection by passing NULL as
       the conn argument.  The result will be the default
       SSL library name, or NULL if libpq was
       compiled without any SSL support.  (Prior
       to PostgreSQL version 15, passing NULL as
       the conn argument always resulted in NULL.
       Client programs needing to differentiate between the newer and older
       implementations of this case may check the
       LIBPQ_HAS_SSL_LIBRARY_DETECTION feature macro.)
      

     

    

    

     
PQsslAttributeNamesPQsslAttributeNames

     

      

       Returns an array of SSL attribute names that can be used
       in PQsslAttribute().
       The array is terminated by a NULL pointer.

const char * const * PQsslAttributeNames(const PGconn *conn);

      

      

       If conn is NULL, the attributes available for the
       default SSL library are returned, or an empty list
       if libpq was compiled without any SSL
       support.  If conn is not NULL, the attributes
       available for the SSL library in use for the connection are returned,
       or an empty list if the connection is not encrypted.
      

     

    

    

     
PQsslStructPQsslStruct

     

      

       Returns a pointer to an SSL-implementation-specific object describing
       the connection.  Returns NULL if the connection is not encrypted
       or the requested type of object is not available from the connection's
       SSL implementation.

void *PQsslStruct(const PGconn *conn, const char *struct_name);

      

      

       The struct(s) available depend on the SSL implementation in use.
       For OpenSSL, there is one struct,
       available under the name OpenSSL,
       and it returns a pointer to
       OpenSSL's SSL struct.
       To use this function, code along the following lines could be used:

```

#include 

...

    SSL *ssl;

    dbconn = PQconnectdb(...);
    ...

    ssl = PQsslStruct(dbconn, "OpenSSL");
    if (ssl)
    {
        /* use OpenSSL functions to access ssl */
    }
]]>
```

      

      

       This structure can be used to verify encryption levels, check server
       certificates, and more. Refer to the OpenSSL
       documentation for information about this structure.
      

     

    

    

     
PQgetsslPQgetssl

     

      

       SSLin libpq
       Returns the SSL structure used in the connection, or NULL
       if SSL is not in use.

void *PQgetssl(const PGconn *conn);

      

      

       This function is equivalent to PQsslStruct(conn, "OpenSSL"). It should
       not be used in new applications, because the returned struct is
       specific to OpenSSL and will not be
       available if another SSL implementation is used.
       To check if a connection uses SSL, call
        instead, and for more details about the
       connection, use .
      

     

    

   

  

 

 

  
# Command Execution Functions

  

   Once a connection to a database server has been successfully
   established, the functions described here are used to perform
   SQL queries and commands.
  

  

   
# Main Functions

   

    

     

      
PQexecPQexec

      

       

        Submits a command to the server and waits for the result.

PGresult *PQexec(PGconn *conn, const char *command);

       

       

        Returns a PGresult pointer or possibly a null
        pointer.  A non-null pointer will generally be returned except in
        out-of-memory conditions or serious errors such as inability to send
        the command to the server.  The  function
        should be called to check the return value for any errors (including
        the value of a null pointer, in which case it will return
        PGRES_FATAL_ERROR).  Use
         to get more information about such
        errors.
       

      

     

    

    The command string can include multiple SQL commands
    (separated by semicolons).  Multiple queries sent in a single
     call are processed in a single transaction, unless
    there are explicit BEGIN/COMMIT
    commands included in the query string to divide it into multiple
    transactions.  (See 
    for more details about how the server handles multi-query strings.)
    Note however that the returned
    PGresult structure describes only the result
    of the last command executed from the string.  Should one of the
    commands fail, processing of the string stops with it and the returned
    PGresult describes the error condition.
   

   

    

     

      
PQexecParamsPQexecParams

      

       

        Submits a command to the server and waits for the result,
        with the ability to pass parameters separately from the SQL
        command text.

PGresult *PQexecParams(PGconn *conn,
                       const char *command,
                       int nParams,
                       const Oid *paramTypes,
                       const char * const *paramValues,
                       const int *paramLengths,
                       const int *paramFormats,
                       int resultFormat);

       

       

         is like , but offers additional
        functionality: parameter values can be specified separately from the command
        string proper, and query results can be requested in either text or binary
        format.
       

       

        The function arguments are:

        

         

          

conn

          

           

            The connection object to send the command through.
           

          

         

         

          

command

          

           

            The SQL command string to be executed. If parameters are used,
            they are referred to in the command string as $1,
            $2, etc.
           

          

         

         

          

nParams

          

           

            The number of parameters supplied; it is the length of the arrays
            
paramTypes[]
, 
paramValues[]
,
            
paramLengths[]
, and 
paramFormats[]
. (The
            array pointers can be NULL when 
nParams

            is zero.)
           

          

         

         

          

paramTypes[]

          

           

            Specifies, by OID, the data types to be assigned to the
            parameter symbols.  If 
paramTypes
 is
            NULL, or any particular element in the array
            is zero, the server infers a data type for the parameter symbol
            in the same way it would do for an untyped literal string.
           

          

         

         

          

paramValues[]

          

           

            Specifies the actual values of the parameters.  A null pointer
            in this array means the corresponding parameter is null;
            otherwise the pointer points to a zero-terminated text string
            (for text format) or binary data in the format expected by the
            server (for binary format).
           

          

         

         

          

paramLengths[]

          

           

            Specifies the actual data lengths of binary-format parameters.
            It is ignored for null parameters and text-format parameters.
            The array pointer can be null when there are no binary parameters.
           

          

         

         

          

paramFormats[]

          

           

            Specifies whether parameters are text (put a zero in the
            array entry for the corresponding parameter) or binary (put
            a one in the array entry for the corresponding parameter).
            If the array pointer is null then all parameters are presumed
            to be text strings.
           

           

            Values passed in binary format require knowledge of
            the internal representation expected by the backend.
            For example, integers must be passed in network byte
            order.  Passing numeric values requires
            knowledge of the server storage format, as implemented
            in
            src/backend/utils/adt/numeric.c::numeric_send() and
            src/backend/utils/adt/numeric.c::numeric_recv().
           

          

         

         

          

resultFormat

          

           

            Specify zero to obtain results in text format, or one to obtain
            results in binary format.  (There is not currently a provision
            to obtain different result columns in different formats,
            although that is possible in the underlying protocol.)
           

          

         

        

       

      

     

    

   

   

    The primary advantage of  over
     is that parameter values can be separated from the
    command string, thus avoiding the need for tedious and error-prone
    quoting and escaping.
   

   

    Unlike ,  allows at most
    one SQL command in the given string.  (There can be semicolons in it,
    but not more than one nonempty command.)  This is a limitation of the
    underlying protocol, but has some usefulness as an extra defense against
    SQL-injection attacks.
   

   
    

     Specifying parameter types via OIDs is tedious, particularly if you prefer
     not to hard-wire particular OID values into your program.  However, you can
     avoid doing so even in cases where the server by itself cannot determine the
     type of the parameter, or chooses a different type than you want.  In the
     SQL command text, attach an explicit cast to the parameter symbol to show what
     data type you will send.  For example:

```
SELECT * FROM mytable WHERE x = $1::bigint;
```

     This forces parameter $1 to be treated as bigint, whereas
     by default it would be assigned the same type as x.  Forcing the
     parameter type decision, either this way or by specifying a numeric type OID,
     is strongly recommended when sending parameter values in binary format, because
     binary format has less redundancy than text format and so there is less chance
     that the server will detect a type mismatch mistake for you.
    

   

   

    

     

      
PQpreparePQprepare

      

       

        Submits a request to create a prepared statement with the
        given parameters, and waits for completion.

PGresult *PQprepare(PGconn *conn,
                    const char *stmtName,
                    const char *query,
                    int nParams,
                    const Oid *paramTypes);

       

       

         creates a prepared statement for later
        execution with .  This feature allows
        commands to be executed repeatedly without being parsed and
        planned each time;  see  for details.
       

       

        The function creates a prepared statement named
        
stmtName
 from the 
query
 string, which
        must contain a single SQL command.  
stmtName
 can be
        "" to create an unnamed statement, in which case any
        pre-existing unnamed statement is automatically replaced; otherwise
        it is an error if the statement name is already defined in the
        current session.  If any parameters are used, they are referred
        to in the query as $1, $2, etc.
        
nParams
 is the number of parameters for which types
        are pre-specified in the array 
paramTypes[]
.  (The
        array pointer can be NULL when
        
nParams
 is zero.) 
paramTypes[]

        specifies, by OID, the data types to be assigned to the parameter
        symbols.  If 
paramTypes
 is NULL,
        or any particular element in the array is zero, the server assigns
        a data type to the parameter symbol in the same way it would do
        for an untyped literal string.  Also, the query can use parameter
        symbols with numbers higher than 
nParams
; data types
        will be inferred for these symbols as well.  (See
         for a means to find out
        what data types were inferred.)
       

       

        As with , the result is normally a
        PGresult object whose contents indicate
        server-side success or failure.  A null result indicates
        out-of-memory or inability to send the command at all.  Use
         to get more information about
        such errors.
       

      

     

    

    Prepared statements for use with  can also
    be created by executing SQL 
    statements.
   

   

    

     

      
PQexecPreparedPQexecPrepared

      

       

        Sends a request to execute a prepared statement with given
        parameters, and waits for the result.

PGresult *PQexecPrepared(PGconn *conn,
                         const char *stmtName,
                         int nParams,
                         const char * const *paramValues,
                         const int *paramLengths,
                         const int *paramFormats,
                         int resultFormat);

       

       

         is like ,
        but the command to be executed is specified by naming a
        previously-prepared statement, instead of giving a query string.
        This feature allows commands that will be used repeatedly to be
        parsed and planned just once, rather than each time they are
        executed.  The statement must have been prepared previously in
        the current session.
       

       

        The parameters are identical to , except that the
        name of a prepared statement is given instead of a query string, and the
        
paramTypes[]
 parameter is not present (it is not needed since
        the prepared statement's parameter types were determined when it was created).
       

      

     

     

      
PQdescribePreparedPQdescribePrepared

      

       

        Submits a request to obtain information about the specified
        prepared statement, and waits for completion.

PGresult *PQdescribePrepared(PGconn *conn, const char *stmtName);

       

       

         allows an application to obtain
        information about a previously prepared statement.
       

       

        
stmtName
 can be "" or NULL to reference
        the unnamed statement, otherwise it must be the name of an existing
        prepared statement.  On success, a PGresult with
        status PGRES_COMMAND_OK is returned.  The
        functions  and
         can be applied to this
        PGresult to obtain information about the parameters
        of the prepared statement, and the functions
        , ,
        , etc. provide information about the
        result columns (if any) of the statement.
       

      

     

     

      
PQdescribePortalPQdescribePortal

      

       

        Submits a request to obtain information about the specified
        portal, and waits for completion.

PGresult *PQdescribePortal(PGconn *conn, const char *portalName);

       

       

         allows an application to obtain
        information about a previously created portal.
        (libpq does not provide any direct access to
        portals, but you can use this function to inspect the properties
        of a cursor created with a DECLARE CURSOR SQL command.)
       

       

        
portalName
 can be "" or NULL to reference
        the unnamed portal, otherwise it must be the name of an existing
        portal.  On success, a PGresult with status
        PGRES_COMMAND_OK is returned.  The functions
        , ,
        , etc. can be applied to the
        PGresult to obtain information about the result
        columns (if any) of the portal.
       

      

     

     

      
PQclosePreparedPQclosePrepared

      

       

        Submits a request to close the specified prepared statement, and waits
        for completion.

PGresult *PQclosePrepared(PGconn *conn, const char *stmtName);

       

       

         allows an application to close
        a previously prepared statement. Closing a statement releases all
        of its associated resources on the server and allows its name to be
        reused.
       

       

        
stmtName
 can be "" or
        NULL to reference the unnamed statement. It is fine
        if no statement exists with this name, in that case the operation is a
        no-op. On success, a PGresult with
        status PGRES_COMMAND_OK is returned.
       

      

     

     

      
PQclosePortalPQclosePortal

      

       

        Submits a request to close the specified portal, and waits for
        completion.

PGresult *PQclosePortal(PGconn *conn, const char *portalName);

       

       

         allows an application to trigger
        a close of a previously created portal. Closing a portal releases all
        of its associated resources on the server and allows its name to be
        reused. (libpq does not provide any
        direct access to portals, but you can use this function to close a
        cursor created with a DECLARE CURSOR SQL command.)
       

       

        
portalName
 can be "" or
        NULL to reference the unnamed portal. It is fine
        if no portal exists with this name, in that case the operation is a
        no-op. On success, a PGresult with status
        PGRES_COMMAND_OK is returned.
       

      

     

    

   

   

    The PGresultPGresult
    structure encapsulates the result returned by the server.
    libpq application programmers should be
    careful to maintain the PGresult abstraction.
    Use the accessor functions below to get at the contents of
    PGresult.  Avoid directly referencing the
    fields of the PGresult structure because they
    are subject to change in the future.

    

     

      
PQresultStatusPQresultStatus

      

       

        Returns the result status of the command.

ExecStatusType PQresultStatus(const PGresult *res);

       

       

         can return one of the following values:

        

         

          
PGRES_EMPTY_QUERY

          

           

            The string sent to the server was empty.
           

          

         

         

          
PGRES_COMMAND_OK

          

           

            Successful completion of a command returning no data.
           

          

         

         

          
PGRES_TUPLES_OK

          

           

            Successful completion of a command returning data (such as
            a SELECT or SHOW).
           

          

         

         

          
PGRES_COPY_OUT

          

           

            Copy Out (from server) data transfer started.
           

          

         

         

          
PGRES_COPY_IN

          

           

            Copy In (to server) data transfer started.
           

          

         

         

          
PGRES_BAD_RESPONSE

          

           

            The server's response was not understood.
           

          

         

         

          
PGRES_NONFATAL_ERROR

          

           

            A nonfatal error (a notice or warning) occurred.
           

          

         

         

          
PGRES_FATAL_ERROR

          

           

            A fatal error occurred.
           

          

         

         

          
PGRES_COPY_BOTH

          

           

            Copy In/Out (to and from server) data transfer started.  This
            feature is currently used only for streaming replication,
            so this status should not occur in ordinary applications.
           

          

         

         

          
PGRES_SINGLE_TUPLE

          

           

            The PGresult contains a single result tuple
            from the current command.  This status occurs only when
            single-row mode has been selected for the query
            (see ).
           

          

         

         

          
PGRES_TUPLES_CHUNK

          

           

            The PGresult contains several result tuples
            from the current command.  This status occurs only when
            chunked mode has been selected for the query
            (see ).
            The number of tuples will not exceed the limit passed to
            .
           

          

         

         

          
PGRES_PIPELINE_SYNC

          

           

            The PGresult represents a
            synchronization point in pipeline mode, requested by either
             or
            .
            This status occurs only when pipeline mode has been selected.
           

          

         

         

          
PGRES_PIPELINE_ABORTED

          

           

            The PGresult represents a pipeline that has
            received an error from the server.  PQgetResult
            must be called repeatedly, and each time it will return this status code
            until the end of the current pipeline, at which point it will return
            PGRES_PIPELINE_SYNC and normal processing can
            resume.
           

          

         

        

        If the result status is PGRES_TUPLES_OK,
        PGRES_SINGLE_TUPLE, or
        PGRES_TUPLES_CHUNK, then
        the functions described below can be used to retrieve the rows
        returned by the query.  Note that a SELECT
        command that happens to retrieve zero rows still shows
        PGRES_TUPLES_OK.
        PGRES_COMMAND_OK is for commands that can never
        return rows (INSERT or UPDATE
        without a RETURNING clause,
        etc.). A response of PGRES_EMPTY_QUERY might
        indicate a bug in the client software.
       

       

        A result of status PGRES_NONFATAL_ERROR will
        never be returned directly by  or other
        query execution functions; results of this kind are instead passed
        to the notice processor (see ).
       

      

     

     

      
PQresStatusPQresStatus

      

       

        Converts the enumerated type returned by
         into a string constant describing the
        status code. The caller should not free the result.

char *PQresStatus(ExecStatusType status);

       

      

     

     

      
PQresultErrorMessagePQresultErrorMessage

      

       

        Returns the error message associated with the command, or an empty string
        if there was no error.

char *PQresultErrorMessage(const PGresult *res);

        If there was an error, the returned string will include a trailing
        newline.  The caller should not free the result directly. It will
        be freed when the associated PGresult handle is
        passed to .
       

       

        Immediately following a  or
         call,
         (on the connection) will return
        the same string as  (on
        the result).  However, a PGresult will
        retain its error message until destroyed, whereas the connection's
        error message will change when subsequent operations are done.
        Use  when you want to
        know the status associated with a particular
        PGresult; use
         when you want to know the
        status from the latest operation on the connection.
       

      

     

     

      
PQresultVerboseErrorMessagePQresultVerboseErrorMessage

      

       

        Returns a reformatted version of the error message associated with
        a PGresult object.

char *PQresultVerboseErrorMessage(const PGresult *res,
                                  PGVerbosity verbosity,
                                  PGContextVisibility show_context);

        In some situations a client might wish to obtain a more detailed
        version of a previously-reported error.
         addresses this need
        by computing the message that would have been produced
        by  if the specified
        verbosity settings had been in effect for the connection when the
        given PGresult was generated.  If
        the PGresult is not an error result,
        PGresult is not an error result is reported instead.
        The returned string includes a trailing newline.
       

       

        Unlike most other functions for extracting data from
        a PGresult, the result of this function is a freshly
        allocated string.  The caller must free it
        using PQfreemem() when the string is no longer needed.
       

       

        A NULL return is possible if there is insufficient memory.
       

      

     

     

      
PQresultErrorFieldPQresultErrorField

      

       

        Returns an individual field of an error report.

char *PQresultErrorField(const PGresult *res, int fieldcode);

        
fieldcode
 is an error field identifier; see the symbols
        listed below.  NULL is returned if the
        PGresult is not an error or warning result,
        or does not include the specified field.  Field values will normally
        not include a trailing newline. The caller should not free the
        result directly. It will be freed when the
        associated PGresult handle is passed to
        .
       

       

        The following field codes are available:
        

         

          
PG_DIAG_SEVERITY

          

           

            The severity; the field contents are ERROR,
            FATAL, or PANIC (in an error message),
            or WARNING, NOTICE, DEBUG,
            INFO, or LOG (in a notice message), or
            a localized translation of one of these.  Always present.
           

          

         

         

          
PG_DIAG_SEVERITY_NONLOCALIZED

          

           

            The severity; the field contents are ERROR,
            FATAL, or PANIC (in an error message),
            or WARNING, NOTICE, DEBUG,
            INFO, or LOG (in a notice message).
            This is identical to the PG_DIAG_SEVERITY field except
            that the contents are never localized.  This is present only in
            reports generated by PostgreSQL versions 9.6
            and later.
           

          

         

         

          
PG_DIAG_SQLSTATEerror codeslibpq

          

           

            The SQLSTATE code for the error. The SQLSTATE code identifies
            the type of error that has occurred; it can be used by
            front-end applications to perform specific operations (such
            as error handling) in response to a particular database error.
            For a list of the possible SQLSTATE codes, see . This field is not localizable,
            and is always present.
           

          

         

         

          
PG_DIAG_MESSAGE_PRIMARY

          

           

            The primary human-readable error message (typically one line).
            Always present.
           

          

         

         

          
PG_DIAG_MESSAGE_DETAIL

          

           

            Detail: an optional secondary error message carrying more
            detail about the problem.  Might run to multiple lines.
           

          

         

         

          
PG_DIAG_MESSAGE_HINT

          

           

            Hint: an optional suggestion what to do about the problem.
            This is intended to differ from detail in that it offers advice
            (potentially inappropriate) rather than hard facts.  Might
            run to multiple lines.
           

          

         

         

          
PG_DIAG_STATEMENT_POSITION

          

           

            A string containing a decimal integer indicating an error cursor
            position as an index into the original statement string.  The
            first character has index 1, and positions are measured in
            characters not bytes.
           

          

         

         

          
PG_DIAG_INTERNAL_POSITION

          

           

            This is defined the same as the
            PG_DIAG_STATEMENT_POSITION field, but it is used
            when the cursor position refers to an internally generated
            command rather than the one submitted by the client.  The
            PG_DIAG_INTERNAL_QUERY field will always appear when
            this field appears.
           

          

         

         

          
PG_DIAG_INTERNAL_QUERY

          

           

            The text of a failed internally-generated command.  This could
            be, for example, an SQL query issued by a PL/pgSQL function.
           

          

         

         

          
PG_DIAG_CONTEXT

          

           

            An indication of the context in which the error occurred.
            Presently this includes a call stack traceback of active
            procedural language functions and internally-generated queries.
            The trace is one entry per line, most recent first.
           

          

         

         

          
PG_DIAG_SCHEMA_NAME

          

           

            If the error was associated with a specific database object,
            the name of the schema containing that object, if any.
           

          

         

         

          
PG_DIAG_TABLE_NAME

          

           

            If the error was associated with a specific table, the name of the
            table.  (Refer to the schema name field for the name of the
            table's schema.)
           

          

         

         

          
PG_DIAG_COLUMN_NAME

          

           

            If the error was associated with a specific table column, the name
            of the column.  (Refer to the schema and table name fields to
            identify the table.)
           

          

         

         

          
PG_DIAG_DATATYPE_NAME

          

           

            If the error was associated with a specific data type, the name of
            the data type.  (Refer to the schema name field for the name of
            the data type's schema.)
           

          

         

         

          
PG_DIAG_CONSTRAINT_NAME

          

           

            If the error was associated with a specific constraint, the name
            of the constraint.  Refer to fields listed above for the
            associated table or domain.  (For this purpose, indexes are
            treated as constraints, even if they weren't created with
            constraint syntax.)
           

          

         

         

          
PG_DIAG_SOURCE_FILE

          

           

            The file name of the source-code location where the error was
            reported.
           

          

         

         

          
PG_DIAG_SOURCE_LINE

          

           

            The line number of the source-code location where the error
            was reported.
           

          

         

         

          
PG_DIAG_SOURCE_FUNCTION

          

           

            The name of the source-code function reporting the error.
           

          

         

        

       

       
        

         The fields for schema name, table name, column name, data type name,
         and constraint name are supplied only for a limited number of error
         types; see .  Do not assume that
         the presence of any of these fields guarantees the presence of
         another field.  Core error sources observe the interrelationships
         noted above, but user-defined functions may use these fields in other
         ways.  In the same vein, do not assume that these fields denote
         contemporary objects in the current database.
        

       

       

        The client is responsible for formatting displayed information to meet
        its needs; in particular it should break long lines as needed.
        Newline characters appearing in the error message fields should be
        treated as paragraph breaks, not line breaks.
       

       

        Errors generated internally by libpq will
        have severity and primary message, but typically no other fields.
       

       

        Note that error fields are only available from
        PGresult objects, not
        PGconn objects; there is no
        PQerrorField function.
       

      

     

     

      
PQclearPQclear

      

       

        Frees  the  storage  associated with a
        PGresult.  Every command result should be
        freed via  when it  is  no  longer
        needed.

void PQclear(PGresult *res);

        If the argument is a NULL pointer, no operation is
        performed.
       

       

        You can keep a PGresult object around for
        as long as you need it; it does not go away when you issue a new
        command, nor even if you close the connection.  To get rid of it,
        you must call .  Failure to do this
        will result in memory leaks in your application.
       

      

     

    

   

  

  

   
# Retrieving Query Result Information

   

    These functions are used to extract information from a
    PGresult object that represents a successful
    query result (that is, one that has status
    PGRES_TUPLES_OK,
    PGRES_SINGLE_TUPLE, or
    PGRES_TUPLES_CHUNK).
    They can also be used to extract
    information from a successful Describe operation: a Describe's result
    has all the same column information that actual execution of the query
    would provide, but it has zero rows.  For objects with other status values,
    these functions will act as though the result has zero rows and zero columns.
   

   

    

     
PQntuplesPQntuples

     

      

       Returns the number of rows (tuples) in the query result.
       (Note that PGresult objects are limited to no more
       than INT_MAX rows, so an int result is
       sufficient.)

int PQntuples(const PGresult *res);

      

     

    

    

     
PQnfieldsPQnfields

     

      

       Returns the number of columns (fields) in each row of the query
       result.

int PQnfields(const PGresult *res);

      

     

    

    

     
PQfnamePQfname

     

      

       Returns the column name associated with the given column number.
       Column numbers start at 0. The caller should not free the result
       directly. It will be freed when the associated
       PGresult handle is passed to
       .

char *PQfname(const PGresult *res,
              int column_number);

      

      

       NULL is returned if the column number is out of range.
      

     

    

    

     
PQfnumberPQfnumber

     

      

       Returns the column number associated with the given column name.

int PQfnumber(const PGresult *res,
              const char *column_name);

      

      

       -1 is returned if the given name does not match any column.
      

      

       The given name is treated like an identifier in an SQL command,
       that is, it is downcased unless double-quoted.  For example, given
       a query result generated from the SQL command:

```
SELECT 1 AS FOO, 2 AS "BAR";
```

       we would have the results:

```
PQfname(res, 0)              foo
PQfname(res, 1)              BAR
PQfnumber(res, "FOO")        0
PQfnumber(res, "foo")        0
PQfnumber(res, "BAR")        -1
PQfnumber(res, "\"BAR\"")    1
```

      

     

    

    

     
PQftablePQftable

     

      

       Returns the OID of the table from which the given column was
       fetched.  Column numbers start at 0.

Oid PQftable(const PGresult *res,
             int column_number);

      

      

       InvalidOid is returned if the column number is out of range,
       or if the specified column is not a simple reference to a table column.
       You can query the system table pg_class to determine
       exactly which table is referenced.
      

      

       The type Oid and the constant
       InvalidOid will be defined when you include
       the libpq header file. They will both
       be some integer type.
      

     

    

    

     
PQftablecolPQftablecol

     

      

       Returns the column number (within its table) of the column making
       up the specified query result column.  Query-result column numbers
       start at 0, but table columns have nonzero numbers.

int PQftablecol(const PGresult *res,
                int column_number);

      

      

       Zero is returned if the column number is out of range, or if the
       specified column is not a simple reference to a table column.
      

     

    

    

     
PQfformatPQfformat

     

      

       Returns the format code indicating the format of the given
       column.  Column numbers start at 0.

int PQfformat(const PGresult *res,
              int column_number);

      

      

       Format code zero indicates textual data representation, while format
       code one indicates binary representation.  (Other codes are reserved
       for future definition.)
      

     

    

    

     
PQftypePQftype

     

      

       Returns the data type associated with the given  column number.
       The  integer  returned is the internal OID number of the type.
       Column numbers start at 0.

Oid PQftype(const PGresult *res,
            int column_number);

      

      

       You can query the system table pg_type to
       obtain the names and properties of the various data types. The
       OIDs of the built-in data types are defined
       in the file catalog/pg_type_d.h
       in the PostgreSQL
       installation's include directory.
      

     

    

    

     
PQfmodPQfmod

     

      

       Returns  the type modifier of the column associated with the
       given column number.  Column numbers start at 0.

int PQfmod(const PGresult *res,
           int column_number);

      

      

       The interpretation of modifier values is type-specific; they
       typically indicate precision or size limits.  The value -1 is
       used to indicate no information available.  Most data
       types do not use modifiers, in which case the value is always
       -1.
      

     

    

    

     
PQfsizePQfsize

     

      

       Returns  the  size  in bytes of the column associated with the
       given column number.  Column numbers start at 0.

int PQfsize(const PGresult *res,
            int column_number);

      

      

        returns the space allocated for this column
       in a database row, in other words the size of the server's
       internal representation of the data type.  (Accordingly, it is
       not really very useful to clients.) A negative value indicates
       the data type is variable-length.
      

     

    

    

     
PQbinaryTuplesPQbinaryTuples

     

      

       Returns 1 if the PGresult contains binary data
       and 0 if it contains text data.

int PQbinaryTuples(const PGresult *res);

      

      

       This function is deprecated (except for its use in connection with
       COPY), because it is possible for a single
       PGresult to contain text data in some columns and
       binary data in others.   is preferred.
        returns 1 only if all columns of the
       result are binary (format 1).
      

     

    

    

     
PQgetvaluePQgetvalue

     

      

       Returns a single field value of one row of a
       PGresult.  Row and column numbers start
       at 0.  The caller should not free the result directly.  It will
       be freed when the associated PGresult handle is
       passed to .

char *PQgetvalue(const PGresult *res,
                 int row_number,
                 int column_number);

      

      

       For data in text format, the value returned by
        is a null-terminated character
       string  representation of the field value.  For data in binary
       format, the value is in the binary representation determined by
       the data type's typsend and typreceive
       functions.  (The value is actually followed by a zero byte in
       this case too, but that is not ordinarily useful, since the
       value is likely to contain embedded nulls.)
      

      

       An empty string is returned if the field value is null.  See
        to distinguish null values from
       empty-string values.
      

      

       The pointer returned  by   points
       to storage that is part of the PGresult
       structure.  One should not modify the data it points to, and one
       must explicitly copy the data into other storage if it is to be
       used past the lifetime of the  PGresult
       structure itself.
      

     

    

    

     
PQgetisnullPQgetisnullnull valuein libpq

     

      

       Tests a field for a null value.  Row and column numbers start
       at 0.

int PQgetisnull(const PGresult *res,
                int row_number,
                int column_number);

      

      

       This function returns  1 if the field is null and 0 if it
       contains a non-null value.  (Note that
        will return an empty string,
       not a null pointer, for a null field.)
      

     

    

    

     
PQgetlengthPQgetlength

     

      

       Returns the actual length of a field value in bytes.  Row and
       column numbers start at 0.

int PQgetlength(const PGresult *res,
                int row_number,
                int column_number);

      

      

       This is the actual data length for the particular data value,
       that is, the size of the object pointed to by
       .  For text data format this is
       the same as strlen().  For binary format this is
       essential information.  Note that one should not
       rely on  to obtain the actual data
       length.
      

     

    

    

     
PQnparamsPQnparams

     

      

       Returns the number of parameters of a prepared statement.

int PQnparams(const PGresult *res);

      

      

       This function is only useful when inspecting the result of
       .  For other types of results it
       will return zero.
      

     

    

    

     
PQparamtypePQparamtype

     

      

       Returns the data type of the indicated statement parameter.
       Parameter numbers start at 0.

Oid PQparamtype(const PGresult *res, int param_number);

      

      

       This function is only useful when inspecting the result of
       .  For other types of results it
       will return zero.
      

     

    

    

     
PQprintPQprint

     

      

       Prints out all the rows and,  optionally,  the column names  to
       the specified output stream.

void PQprint(FILE *fout,      /* output stream */
             const PGresult *res,
             const PQprintOpt *po);
typedef struct
{
    pqbool  header;      /* print output field headings and row count */
    pqbool  align;       /* fill align the fields */
    pqbool  standard;    /* old brain dead format */
    pqbool  html3;       /* output HTML tables */
    pqbool  expanded;    /* expand tables */
    pqbool  pager;       /* use pager for output if needed */
    char    *fieldSep;   /* field separator */
    char    *tableOpt;   /* attributes for HTML table element */
    char    *caption;    /* HTML table caption */
    char    **fieldName; /* null-terminated array of replacement field names */
} PQprintOpt;

      

      

       This function was formerly used by psql
       to print query results, but this is no longer the case.  Note
       that it assumes all the data is in text format.
      

     

    

   

  

  

   
# Retrieving Other Result Information

   

    These functions are used to extract other information from
    PGresult objects.
   

   

    

     
PQcmdStatusPQcmdStatus

     

      

       Returns the command status tag from the SQL command that generated
       the PGresult.

char *PQcmdStatus(PGresult *res);

      

      

       Commonly this is just the name of the command, but it might include
       additional data such as the number of rows processed. The caller
       should not free the result directly. It will be freed when the
       associated PGresult handle is passed to
       .
      

     

    

    

     
PQcmdTuplesPQcmdTuples

     

      

       Returns the number of rows affected by the SQL command.

char *PQcmdTuples(PGresult *res);

      

      

       This function returns a string containing the number of rows
       affected by the SQL statement that generated the
       PGresult. This function can only be used following
       the execution of a SELECT, CREATE TABLE AS,
       INSERT, UPDATE, DELETE,
       MERGE, MOVE, FETCH,
       or COPY statement, or an EXECUTE of a
       prepared query that contains an INSERT,
       UPDATE, DELETE,
       or MERGE statement.
       If the command that generated the PGresult was anything
       else,  returns an empty string. The caller
       should not free the return value directly. It will be freed when
       the associated PGresult handle is passed to
       .
      

     

    

    

     
PQoidValuePQoidValue

     

      

       Returns the OIDOIDin libpq
       of the inserted row, if the SQL command was an
       INSERT that inserted exactly one row into a table that
       has OIDs, or a EXECUTE of a prepared query containing
       a suitable INSERT statement.  Otherwise, this function
       returns InvalidOid. This function will also
       return InvalidOid if the table affected by the
       INSERT statement does not contain OIDs.

Oid PQoidValue(const PGresult *res);

      

     

    

    

     
PQoidStatusPQoidStatus

     

      

       This function is deprecated in favor of
        and is not thread-safe.
       It returns a string with the OID of the inserted row, while
        returns the OID value.

char *PQoidStatus(const PGresult *res);

      

     

    

   

  

  

   
# Escaping Strings for Inclusion in SQL Commands

   
    escaping strings
    in libpq
   

   

    

     
PQescapeLiteralPQescapeLiteral

     

     

char *PQescapeLiteral(PGconn *conn, const char *str, size_t length);

     

     

       escapes a string for
      use within an SQL command.  This is useful when inserting data
      values as literal constants in SQL commands.  Certain characters
      (such as quotes and backslashes) must be escaped to prevent them
      from being interpreted specially by the SQL parser.
       performs this operation.
     

     

       returns an escaped version of the
      
str
 parameter in memory allocated with
      malloc().  This memory should be freed using
      PQfreemem() when the result is no longer needed.
      A terminating zero byte is not required, and should not be
      counted in 
length
.  (If a terminating zero byte is found
      before 
length
 bytes are processed,
       stops at the zero; the behavior is
      thus rather like strncpy.) The
      return string has all special characters replaced so that they can
      be properly processed by the PostgreSQL
      string literal parser.  A terminating zero byte is also added.  The
      single quotes that must surround PostgreSQL
      string literals are included in the result string.
     

     

      On error,  returns NULL and a suitable
      message is stored in the 
conn
 object.
     

     
      

       It is especially important to do proper escaping when handling
       strings that were received from an untrustworthy source.
       Otherwise there is a security risk: you are vulnerable to
       SQL injection attacks wherein unwanted SQL commands are
       fed to your database.
      

     

     

      Note that it is neither necessary nor correct to do escaping when a data
      value is passed as a separate parameter in  or
      its sibling routines.
     

     

    

    

     
PQescapeIdentifierPQescapeIdentifier

     

     

char *PQescapeIdentifier(PGconn *conn, const char *str, size_t length);

     

     

       escapes a string for
      use as an SQL identifier, such as a table, column, or function name.
      This is useful when a user-supplied identifier might contain
      special characters that would otherwise not be interpreted as part
      of the identifier by the SQL parser, or when the identifier might
      contain upper case characters whose case should be preserved.
     

     

       returns a version of the
      
str
 parameter escaped as an SQL identifier
      in memory allocated with malloc().  This memory must be
      freed using PQfreemem() when the result is no longer
      needed.  A terminating zero byte is not required, and should not be
      counted in 
length
.  (If a terminating zero byte is found
      before 
length
 bytes are processed,
       stops at the zero; the behavior is
      thus rather like strncpy.) The
      return string has all special characters replaced so that it
      will be properly processed as an SQL identifier.  A terminating zero byte
      is also added.  The return string will also be surrounded by double
      quotes.
     

     

      On error,  returns NULL and a suitable
      message is stored in the 
conn
 object.
     

     
      

       As with string literals, to prevent SQL injection attacks,
       SQL identifiers must be escaped when they are received from an
       untrustworthy source.
      

     
     

    

    

     
PQescapeStringConnPQescapeStringConn

     

     

size_t PQescapeStringConn(PGconn *conn,
                          char *to, const char *from, size_t length,
                          int *error);

     

     

       escapes string literals, much like
      .  Unlike ,
      the caller is responsible for providing an appropriately sized buffer.
      Furthermore,  does not generate the
      single quotes that must surround PostgreSQL string
      literals; they should be provided in the SQL command that the
      result is inserted into.  The parameter 
from
 points to
      the first character of the string that is to be escaped, and the
      
length
 parameter gives the number of bytes in this
      string.  A terminating zero byte is not required, and should not be
      counted in 
length
.  (If a terminating zero byte is found
      before 
length
 bytes are processed,
       stops at the zero; the behavior is
      thus rather like strncpy.) 
to
 shall point
      to a buffer that is able to hold at least one more byte than twice
      the value of 
length
, otherwise the behavior is undefined.
      Behavior is likewise undefined if the 
to
 and
      
from
 strings overlap.
     

     

      If the 
error
 parameter is not NULL, then
      *error is set to zero on success, nonzero on error.
      Presently the only possible error conditions involve invalid multibyte
      encoding in the source string.  The output string is still generated
      on error, but it can be expected that the server will reject it as
      malformed.  On error, a suitable message is stored in the
      
conn
 object, whether or not 
error
 is NULL.
     

     

       returns the number of bytes written
      to 
to
, not including the terminating zero byte.
     

     

    

    

     
PQescapeStringPQescapeString

     

     

        is an older, deprecated version of
       .

size_t PQescapeString (char *to, const char *from, size_t length);

     

     

      The only difference from  is that
       does not take PGconn
      or 
error
 parameters.
      Because of this, it cannot adjust its behavior depending on the
      connection properties (such as character encoding) and therefore
      it might give the wrong results.  Also, it has no way
      to report error conditions.
     

     

       can be used safely in
      client programs that work with only one PostgreSQL
      connection at a time (in this case it can find out what it needs to
      know behind the scenes).  In other contexts it is a security
      hazard and should be avoided in favor of
      .
     

     

    

    

     
PQescapeByteaConnPQescapeByteaConn

     

     

       Escapes binary data for use within an SQL command with the type
       bytea.  As with ,
       this is only used when inserting data directly into an SQL command string.

unsigned char *PQescapeByteaConn(PGconn *conn,
                                 const unsigned char *from,
                                 size_t from_length,
                                 size_t *to_length);

      

      

       Certain byte values must be escaped when used as part of a
       bytea literal in an SQL statement.
        escapes bytes using
       either hex encoding or backslash escaping.  See  for more information.
      

      

       The 
from
 parameter points to the first
       byte of the string that is to be escaped, and the
       
from_length
 parameter gives the number of
       bytes in this binary string.  (A terminating zero byte is
       neither necessary nor counted.)  The 
to_length

       parameter points to a variable that will hold the resultant
       escaped string length. This result string length includes the terminating
       zero byte of the result.
      

      

        returns an escaped version of the
       
from
 parameter binary string in memory
       allocated with malloc().  This memory should be freed using
       PQfreemem() when the result is no longer needed.  The
       return string has all special characters replaced so that they can
       be properly processed by the PostgreSQL
       string literal parser, and the bytea input function. A
       terminating zero byte is also added.  The single quotes that must
       surround PostgreSQL string literals are
       not part of the result string.
      

      

       On error, a null pointer is returned, and a suitable error message
       is stored in the 
conn
 object.  Currently, the only
       possible error is insufficient memory for the result string.
      

     

    

    

     
PQescapeByteaPQescapeBytea

     

      

        is an older, deprecated version of
       .

unsigned char *PQescapeBytea(const unsigned char *from,
                             size_t from_length,
                             size_t *to_length);

      

      

       The only difference from  is that
        does not take a PGconn
       parameter.  Because of this,  can
       only be used safely in client programs that use a single
       PostgreSQL connection at a time (in this case
       it can find out what it needs to know behind the
       scenes).  It might give the wrong results if
       used in programs that use multiple database connections (use
        in such cases).
      

     

    

    

     
PQunescapeByteaPQunescapeBytea

     

      

       Converts a string representation of binary data into binary data
       &mdash; the reverse of .  This
       is needed when retrieving bytea data in text format,
       but not when retrieving it in binary format.

unsigned char *PQunescapeBytea(const unsigned char *from, size_t *to_length);

      

      

       The 
from
 parameter points to a string
       such as might be returned by  when applied
       to a bytea column. 
       converts this string representation into its binary representation.
       It returns a pointer to a buffer allocated with
       malloc(), or NULL on error, and puts the size of
       the buffer in 
to_length
. The result must be
       freed using  when it is no longer needed.
      

      

       This conversion is not exactly the inverse of
       , because the string is not expected
       to be escaped when received from .
       In particular this means there is no need for string quoting considerations,
       and so no need for a PGconn parameter.
      

     

    

   

  

 

 

  
# Asynchronous Command Processing

  
   nonblocking connection
  

  

   The  function is adequate for submitting
   commands in normal, synchronous applications.  It has a few
   deficiencies, however, that can be of importance to some users:

   

    

     

       waits for the command to be completed.
      The application might have other work to do (such as maintaining a
      user interface), in which case it won't want to block waiting for
      the response.
     

    

    

     

      Since the execution of the client application is suspended while it
      waits for the result, it is hard for the application to decide that
      it would like to try to cancel the ongoing command.  (It can be done
      from a signal handler, but not otherwise.)
     

    

    

     

       can return only one
      PGresult structure.  If the submitted command
      string contains multiple SQL commands, all but
      the last PGresult are discarded by
      .
     

    

    

     

       always collects the command's entire result,
      buffering it in a single PGresult.  While
      this simplifies error-handling logic for the application, it can be
      impractical for results containing many rows.
     

    

   

  

  

   Applications that do not like these limitations can instead use the
   underlying functions that  is built from:
    and .
   There are also
   ,
   ,
   ,
   ,
   ,
   , and
   ,
   which can be used with  to duplicate
   the functionality of
   ,
   ,
   ,
   ,
   ,
   , and
   
   respectively.

   

    

     
PQsendQueryPQsendQuery

     

      

       Submits a command to the server without waiting for the result(s).
       1 is returned if the command was successfully dispatched and 0 if
       not (in which case, use  to get more
       information about the failure).

int PQsendQuery(PGconn *conn, const char *command);

       After successfully calling , call
        one or more times to obtain the
       results.   cannot be called again
       (on the same connection) until 
       has returned a null pointer, indicating that the command is done.
      

      

       In pipeline mode, this function is disallowed.
      

     

    

    

     
PQsendQueryParamsPQsendQueryParams

     

      

       Submits a command and separate parameters to the server without
       waiting for the result(s).

int PQsendQueryParams(PGconn *conn,
                      const char *command,
                      int nParams,
                      const Oid *paramTypes,
                      const char * const *paramValues,
                      const int *paramLengths,
                      const int *paramFormats,
                      int resultFormat);

       This is equivalent to  except that
       query parameters can be specified separately from the query string.
       The function's parameters are handled identically to
       .  Like
       , it allows only one command in the
       query string.
      

     

    

    

     
PQsendPreparePQsendPrepare

     

      

       Sends a request to create a prepared statement with the given
       parameters, without waiting for completion.

int PQsendPrepare(PGconn *conn,
                  const char *stmtName,
                  const char *query,
                  int nParams,
                  const Oid *paramTypes);

       This is an asynchronous version of : it
       returns 1 if it was able to dispatch the request, and 0 if not.
       After a successful call, call  to
       determine whether the server successfully created the prepared
       statement.  The function's parameters are handled identically to
       .
      

     

    

    

     
PQsendQueryPreparedPQsendQueryPrepared

     

      

       Sends a request to execute a prepared statement with given
       parameters, without waiting for the result(s).

int PQsendQueryPrepared(PGconn *conn,
                        const char *stmtName,
                        int nParams,
                        const char * const *paramValues,
                        const int *paramLengths,
                        const int *paramFormats,
                        int resultFormat);

       This is similar to , but
       the command to be executed is specified by naming a
       previously-prepared statement, instead of giving a query string.
       The function's parameters are handled identically to
       .
      

     

    

    

     
PQsendDescribePreparedPQsendDescribePrepared

     

      

       Submits a request to obtain information about the specified
       prepared statement, without waiting for completion.

int PQsendDescribePrepared(PGconn *conn, const char *stmtName);

       This is an asynchronous version of :
       it returns 1 if it was able to dispatch the request, and 0 if not.
       After a successful call, call  to
       obtain the results.  The function's parameters are handled
       identically to .
      

     

    

    

     
PQsendDescribePortalPQsendDescribePortal

     

      

       Submits a request to obtain information about the specified
       portal, without waiting for completion.

int PQsendDescribePortal(PGconn *conn, const char *portalName);

       This is an asynchronous version of :
       it returns 1 if it was able to dispatch the request, and 0 if not.
       After a successful call, call  to
       obtain the results.  The function's parameters are handled
       identically to .
      

     

    

    

     
PQsendClosePreparedPQsendClosePrepared

     

      

       Submits a request to close the specified prepared statement, without
       waiting for completion.

int PQsendClosePrepared(PGconn *conn, const char *stmtName);

       This is an asynchronous version of :
       it returns 1 if it was able to dispatch the request, and 0 if not.
       After a successful call, call  to
       obtain the results.  The function's parameters are handled
       identically to .
      

     

    

    

     
PQsendClosePortalPQsendClosePortal

     

      

       Submits a request to close specified portal, without waiting for
       completion.

int PQsendClosePortal(PGconn *conn, const char *portalName);

       This is an asynchronous version of :
       it returns 1 if it was able to dispatch the request, and 0 if not.
       After a successful call, call  to
       obtain the results.  The function's parameters are handled
       identically to .
      

     

    

    

     
PQgetResultPQgetResult

     

      

       Waits for the next result from a prior
       ,
       ,
       ,
       ,
       ,
       ,
       ,
       ,
       , or
       
       call, and returns it.
       A null pointer is returned when the command is complete and there
       will be no more results.

PGresult *PQgetResult(PGconn *conn);

      

      

        must be called repeatedly until
       it returns a null pointer, indicating that the command is done.
       (If called when no command is active,
        will just return a null pointer
       at once.) Each non-null result from
        should be processed using the
       same PGresult accessor functions previously
       described.  Don't forget to free each result object with
        when done with it.  Note that
        will block only if a command is
       active and the necessary response data has not yet been read by
       .
      

      

       In pipeline mode, PQgetResult will return normally
       unless an error occurs; for any subsequent query sent after the one
       that caused the error until (and excluding) the next synchronization point,
       a special result of type PGRES_PIPELINE_ABORTED will
       be returned, and a null pointer will be returned after it.
       When the pipeline synchronization point is reached, a result of type
       PGRES_PIPELINE_SYNC will be returned.
       The result of the next query after the synchronization point follows
       immediately (that is, no null pointer is returned after
       the synchronization point).
      

      
       

        Even when  indicates a fatal
        error,  should be called until it
        returns a null pointer, to allow libpq to
        process the error information completely.
       

      
     

    

   

  

  

   Using  and
    solves one of
   's problems:  If a command string contains
   multiple SQL commands, the results of those commands
   can be obtained individually.  (This allows a simple form of overlapped
   processing, by the way: the client can be handling the results of one
   command while the server is still working on later queries in the same
   command string.)
  

  

   Another frequently-desired feature that can be obtained with
    and 
   is retrieving large query results a limited number of rows at a time.
   This is discussed
   in .
  

  

   By itself, calling 
   will still cause the client to block until the server completes the
   next SQL command.  This can be avoided by proper
   use of two more functions:

   

    

     
PQconsumeInputPQconsumeInput
     

     

      

       If input is available from the server, consume it.

int PQconsumeInput(PGconn *conn);

      

      

        normally returns 1 indicating
       no error, but returns 0 if there was some kind of
       trouble (in which case  can be
       consulted).  Note that the result does not say whether any input
       data was actually collected. After calling
       , the application can check
        and/or
       PQnotifies to see if their state has changed.
      

      

        can be called even if the
       application is not prepared to deal with a result or notification
       just yet.  The function will read available data and save it in
       a buffer, thereby causing a select()
       read-ready indication to go away.  The application can thus use
        to clear the
       select() condition immediately, and then
       examine the results at leisure.
      

     

    

    

     
PQisBusyPQisBusy

     

      

       Returns 1 if a command is busy, that is,
        would block waiting for input.
       A 0 return indicates that  can be
       called with assurance of not blocking.

int PQisBusy(PGconn *conn);

      

      

        will not itself attempt to read data
       from the server; therefore 
       must be invoked first, or the busy state will never end.
      

     

    

   

  

  

   A typical application using these functions will have a main loop that
   uses select() or poll() to wait for
   all the conditions that it must respond to.  One of the conditions
   will be input available from the server, which in terms of
   select() means readable data on the file
   descriptor identified by .  When the main
   loop detects input ready, it should call
    to read the input.  It can then
   call , followed by
    if 
   returns false (0).  It can also call PQnotifies
   to detect NOTIFY messages (see ).
  

  

   A client that uses
   /
   can also attempt to cancel a command that is still being processed
   by the server; see .  But regardless of
   the return value of , the application
   must continue with the normal result-reading sequence using
   .  A successful cancellation will
   simply cause the command to terminate sooner than it would have
   otherwise.
  

  

   By using the functions described above, it is possible to avoid
   blocking while waiting for input from the database server.  However,
   it is still possible that the application will block waiting to send
   output to the server.  This is relatively uncommon but can happen if
   very long SQL commands or data values are sent.  (It is much more
   probable if the application sends data via COPY IN,
   however.)  To prevent this possibility and achieve completely
   nonblocking database operation, the following additional functions
   can be used.

   

    

     
PQsetnonblockingPQsetnonblocking

     

      

       Sets the nonblocking status of the connection.

int PQsetnonblocking(PGconn *conn, int arg);

      

      

       Sets the state of the connection to nonblocking if
       
arg
 is 1, or blocking if
       
arg
 is 0.  Returns 0 if OK, -1 if error.
      

      

       In the nonblocking state, successful calls to
       , ,
       , ,
       and  will not block;  their changes
       are stored in the local output buffer until they are flushed.
       Unsuccessful calls will return an error and must be retried.
      

      

       Note that  does not honor nonblocking
       mode; if it is called, it will act in blocking fashion anyway.
      

     

    

    

     
PQisnonblockingPQisnonblocking

     

      

       Returns the blocking status of the database connection.

int PQisnonblocking(const PGconn *conn);

      

      

       Returns 1 if the connection is set to nonblocking mode and 0 if
       blocking.
      

     

    

    

     
PQflushPQflush

      

       

       Attempts to flush any queued output data to the server.  Returns
       0 if successful (or if the send queue is empty), -1 if it failed
       for some reason, or 1 if it was unable to send all the data in
       the send queue yet (this case can only occur if the connection
       is nonblocking).

int PQflush(PGconn *conn);

      

     

    

   

  

  

   After sending any command or data on a nonblocking connection, call
   .  If it returns 1, wait for the socket
   to become read- or write-ready.  If it becomes write-ready, call
    again.  If it becomes read-ready, call
   , then call
    again.  Repeat until
    returns 0.  (It is necessary to check for
   read-ready and drain the input with ,
   because the server can block trying to send us data, e.g., NOTICE
   messages, and won't read our data until we read its.)  Once
    returns 0, wait for the socket to be
   read-ready and then read the response as described above.
  

 

 

  
# Pipeline Mode

  
   libpq
   pipeline mode
  

  
   pipelining
   in libpq
  

  
   batch mode
   in libpq
  

  

   libpq pipeline mode allows applications to
   send a query without having to read the result of the previously
   sent query.  Taking advantage of the pipeline mode, a client will wait
   less for the server, since multiple queries/results can be
   sent/received in a single network transaction.
  

  

   While pipeline mode provides a significant performance boost, writing
   clients using the pipeline mode is more complex because it involves
   managing a queue of pending queries and finding which result
   corresponds to which query in the queue.
  

  

   Pipeline mode also generally consumes more memory on both the client and server,
   though careful and aggressive management of the send/receive queue can mitigate
   this.  This applies whether or not the connection is in blocking or non-blocking
   mode.
  

  

   While libpq's pipeline API was introduced in
   PostgreSQL 14, it is a client-side feature
   which doesn't require special server support and works on any server
   that supports the v3 extended query protocol.  For more information see
   .
  

  

   
# Using Pipeline Mode

   

    To issue pipelines, the application must switch the connection
    into pipeline mode,
    which is done with .
     can be used
    to test whether pipeline mode is active.
    In pipeline mode, only asynchronous operations
    that utilize the extended query protocol
    are permitted, command strings containing multiple SQL commands are
    disallowed, and so is COPY.
    Using synchronous command execution functions
    such as
    PQexec,
    PQexecParams,
    PQprepare,
    PQexecPrepared,
    PQdescribePrepared,
    PQdescribePortal,
    PQclosePrepared,
    PQclosePortal,
    is an error condition.
    PQsendQuery is
    also disallowed, because it uses the simple query protocol.
    Once all dispatched commands have had their results processed, and
    the end pipeline result has been consumed, the application may return
    to non-pipelined mode with .
   

   
    

     It is best to use pipeline mode with libpq in
     non-blocking mode. If used
     in blocking mode it is possible for a client/server deadlock to occur.
      
       

        The client will block trying to send queries to the server, but the
        server will block trying to send results to the client from queries
        it has already processed. This only occurs when the client sends
        enough queries to fill both its output buffer and the server's receive
        buffer before it switches to processing input from the server,
        but it's hard to predict exactly when that will happen.
       

      
    

   

   

    
# Issuing Queries

    

     After entering pipeline mode, the application dispatches requests using
     
     or its prepared-query sibling
     .
     These requests are queued on the client-side until flushed to the server;
     this occurs when  is used to
     establish a synchronization point in the pipeline,
     or when  is called.
     The functions ,
     ,
     ,
     , and
      also work in pipeline mode.
     Result processing is described below.
    

    

     The server executes statements, and returns results, in the order the
     client sends them.  The server will begin executing the commands in the
     pipeline immediately, not waiting for the end of the pipeline.
     Note that results are buffered on the server side; the server flushes
     that buffer when a synchronization point is established with either
     PQpipelineSync or
     PQsendPipelineSync, or when
     PQsendFlushRequest is called.
     If any statement encounters an error, the server aborts the current
     transaction and does not execute any subsequent command in the queue
     until the next synchronization point;
     a PGRES_PIPELINE_ABORTED result is produced for
     each such command.
     (This remains true even if the commands in the pipeline would rollback
     the transaction.)
     Query processing resumes after the synchronization point.
    

    

     It's fine for one operation to depend on the results of a
     prior one; for example, one query may define a table that the next
     query in the same pipeline uses. Similarly, an application may
     create a named prepared statement and execute it with later
     statements in the same pipeline.
    

   

   

    
# Processing Results

    

     To process the result of one query in a pipeline, the application calls
     PQgetResult repeatedly and handles each result
     until PQgetResult returns null.
     The result from the next query in the pipeline may then be retrieved using
     PQgetResult again and the cycle repeated.
     The application handles individual statement results as normal.
     When the results of all the queries in the pipeline have been
     returned, PQgetResult returns a result
     containing the status value PGRES_PIPELINE_SYNC
    

    

     The client may choose to defer result processing until the complete
     pipeline has been sent, or interleave that with sending further
     queries in the pipeline; see .
    

    

     PQgetResult behaves the same as for normal
     asynchronous processing except that it may contain the new
     PGresult types PGRES_PIPELINE_SYNC
     and PGRES_PIPELINE_ABORTED.
     PGRES_PIPELINE_SYNC is reported exactly once for each
     PQpipelineSync or
     PQsendPipelineSync at the corresponding point
     in the pipeline.
     PGRES_PIPELINE_ABORTED is emitted in place of a normal
     query result for the first error and all subsequent results
     until the next PGRES_PIPELINE_SYNC;
     see .
    

    

     PQisBusy, PQconsumeInput, etc
     operate as normal when processing pipeline results.  In particular,
     a call to PQisBusy in the middle of a pipeline
     returns 0 if the results for all the queries issued so far have been
     consumed.
    

    

     libpq does not provide any information to the
     application about the query currently being processed (except that
     PQgetResult returns null to indicate that we start
     returning the results of next query). The application must keep track
     of the order in which it sent queries, to associate them with their
     corresponding results.
     Applications will typically use a state machine or a FIFO queue for this.
    

   

   

    
# Error Handling

    

     From the client's perspective, after PQresultStatus
     returns PGRES_FATAL_ERROR,
     the pipeline is flagged as aborted.
     PQresultStatus will report a
     PGRES_PIPELINE_ABORTED result for each remaining queued
     operation in an aborted pipeline. The result for
     PQpipelineSync or
     PQsendPipelineSync is reported as
     PGRES_PIPELINE_SYNC to signal the end of the aborted pipeline
     and resumption of normal result processing.
    

    

     The client must process results with
     PQgetResult during error recovery.
    

    

     If the pipeline used an implicit transaction, then operations that have
     already executed are rolled back and operations that were queued to follow
     the failed operation are skipped entirely. The same behavior holds if the
     pipeline starts and commits a single explicit transaction (i.e. the first
     statement is BEGIN and the last is
     COMMIT) except that the session remains in an aborted
     transaction state at the end of the pipeline. If a pipeline contains
     multiple explicit transactions, all transactions that
     committed prior to the error remain committed, the currently in-progress
     transaction is aborted, and all subsequent operations are skipped completely,
     including subsequent transactions.  If a pipeline synchronization point
     occurs with an explicit transaction block in aborted state, the next pipeline
     will become aborted immediately unless the next command puts the transaction
     in normal mode with ROLLBACK.
    

    
     

      The client must not assume that work is committed when it
      sends a COMMIT &mdash; only when the
      corresponding result is received to confirm the commit is complete.
      Because errors arrive asynchronously, the application needs to be able to
      restart from the last received committed change and
      resend work done after that point if something goes wrong.
     

    
   

   

    
# Interleaving Result Processing and Query Dispatch

    

     To avoid deadlocks on large pipelines the client should be structured
     around a non-blocking event loop using operating system facilities
     such as select, poll,
     WaitForMultipleObjectEx, etc.
    

    

     The client application should generally maintain a queue of work
     remaining to be dispatched and a queue of work that has been dispatched
     but not yet had its results processed. When the socket is writable
     it should dispatch more work. When the socket is readable it should
     read results and process them, matching them up to the next entry in
     its corresponding results queue.  Based on available memory, results from the
     socket should be read frequently: there's no need to wait until the
     pipeline end to read the results.  Pipelines should be scoped to logical
     units of work, usually (but not necessarily) one transaction per pipeline.
     There's no need to exit pipeline mode and re-enter it between pipelines,
     or to wait for one pipeline to finish before sending the next.
    

    

     An example using select() and a simple state
     machine to track sent and received work is in
     src/test/modules/libpq_pipeline/libpq_pipeline.c
     in the PostgreSQL source distribution.
    

   

  

  

   
# Functions Associated with Pipeline Mode

   

    

     
PQpipelineStatusPQpipelineStatus

     

      

      Returns the current pipeline mode status of the
      libpq connection.

PGpipelineStatus PQpipelineStatus(const PGconn *conn);

      

      

       PQpipelineStatus can return one of the following values:

       

        

         

          PQ_PIPELINE_ON
         

         

          

           The libpq connection is in
           pipeline mode.
          

         

        

        

         

          PQ_PIPELINE_OFF
         

         

          

           The libpq connection is
           not in pipeline mode.
          

         

        

        

         

          PQ_PIPELINE_ABORTED
         

         

          

           The libpq connection is in pipeline
           mode and an error occurred while processing the current pipeline.
           The aborted flag is cleared when PQgetResult
           returns a result of type PGRES_PIPELINE_SYNC.
          

         

        

       

      

     

    

    

     
PQenterPipelineModePQenterPipelineMode

     

      

      Causes a connection to enter pipeline mode if it is currently idle or
      already in pipeline mode.

int PQenterPipelineMode(PGconn *conn);

      

      

       Returns 1 for success.
       Returns 0 and has no effect if the connection is not currently
       idle, i.e., it has a result ready, or it is waiting for more
       input from the server, etc.
       This function does not actually send anything to the server,
       it just changes the libpq connection
       state.
      

     

    

    

     
PQexitPipelineModePQexitPipelineMode

     

      

       Causes a connection to exit pipeline mode if it is currently in pipeline mode
       with an empty queue and no pending results.

int PQexitPipelineMode(PGconn *conn);

      

      

       Returns 1 for success.  Returns 1 and takes no action if not in
       pipeline mode. If the current statement isn't finished processing,
       or PQgetResult has not been called to collect
       results from all previously sent query, returns 0 (in which case,
       use  to get more information
       about the failure).
      

     

    

    

     
PQpipelineSyncPQpipelineSync

     

      

       Marks a synchronization point in a pipeline by sending a
       sync message
       and flushing the send buffer. This serves as
       the delimiter of an implicit transaction and an error recovery
       point; see .

int PQpipelineSync(PGconn *conn);

      

      

       Returns 1 for success. Returns 0 if the connection is not in
       pipeline mode or sending a
       sync message
       failed.
      

     

    

    

     
PQsendPipelineSyncPQsendPipelineSync

     

      

       Marks a synchronization point in a pipeline by sending a
       sync message
       without flushing the send buffer. This serves as
       the delimiter of an implicit transaction and an error recovery
       point; see .

int PQsendPipelineSync(PGconn *conn);

      

      

       Returns 1 for success. Returns 0 if the connection is not in
       pipeline mode or sending a
       sync message
       failed.
       Note that the message is not itself flushed to the server automatically;
       use PQflush if necessary.
      

     

    

    

     
PQsendFlushRequestPQsendFlushRequest

     

      

       Sends a request for the server to flush its output buffer.

int PQsendFlushRequest(PGconn *conn);

      

      

       Returns 1 for success.  Returns 0 on any failure.
      

      

       The server flushes its output buffer automatically as a result of
       PQpipelineSync being called, or
       on any request when not in pipeline mode; this function is useful
       to cause the server to flush its output buffer in pipeline mode
       without establishing a synchronization point.
       Note that the request is not itself flushed to the server automatically;
       use PQflush if necessary.
      

     

    

   

  

  

   
# When to Use Pipeline Mode

   

    Much like asynchronous query mode, there is no meaningful performance
    overhead when using pipeline mode. It increases client application complexity,
    and extra caution is required to prevent client/server deadlocks, but
    pipeline mode can offer considerable performance improvements, in exchange for
    increased memory usage from leaving state around longer.
   

   

    Pipeline mode is most useful when the server is distant, i.e., network latency
    (ping time) is high, and also when many small operations
    are being performed in rapid succession.  There is usually less benefit
    in using pipelined commands when each query takes many multiples of the client/server
    round-trip time to execute.  A 100-statement operation run on a server
    300 ms round-trip-time away would take 30 seconds in network latency alone
    without pipelining; with pipelining it may spend as little as 0.3 s waiting for
    results from the server.
   

   

    Use pipelined commands when your application does lots of small
    INSERT, UPDATE and
    DELETE operations that can't easily be transformed
    into operations on sets, or into a COPY operation.
   

   

    Pipeline mode is not useful when information from one operation is required by
    the client to produce the next operation. In such cases, the client
    would have to introduce a synchronization point and wait for a full client/server
    round-trip to get the results it needs. However, it's often possible to
    adjust the client design to exchange the required information server-side.
    Read-modify-write cycles are especially good candidates; for example:

```
BEGIN;
SELECT x FROM mytable WHERE id = 42 FOR UPDATE;
-- result: x=2
-- client adds 1 to x:
UPDATE mytable SET x = 3 WHERE id = 42;
COMMIT;
```

    could be much more efficiently done with:

```
UPDATE mytable SET x = x + 1 WHERE id = 42;
```

   

   

    Pipelining is less useful, and more complex, when a single pipeline contains
    multiple transactions (see ).
   

  

 

 
 

  
# Retrieving Query Results in Chunks

  
   libpq
   single-row mode
  

  
   libpq
   chunked mode
  

  

   Ordinarily, libpq collects an SQL command's
   entire result and returns it to the application as a single
   PGresult.  This can be unworkable for commands
   that return a large number of rows.  For such cases, applications can use
    and  in
   single-row mode or chunked
   mode.  In these modes, result row(s) are returned to the
   application as they are received from the server, one at a time for
   single-row mode or in groups for chunked mode.
  

  

   To enter one of these modes, call 
    or 
   immediately after a successful call of 
   (or a sibling function).  This mode selection is effective only for the
   currently executing query.  Then call 
   repeatedly, until it returns null, as documented in .  If the query returns any rows, they are returned
   as one or more PGresult objects, which look like
   normal query results except for having status code
   PGRES_SINGLE_TUPLE for single-row mode or
   PGRES_TUPLES_CHUNK for chunked mode, instead of
   PGRES_TUPLES_OK.  There is exactly one result row in
   each PGRES_SINGLE_TUPLE object, while
   a PGRES_TUPLES_CHUNK object contains at least one
   row but not more than the specified number of rows per chunk.
   After the last row, or immediately if
   the query returns zero rows, a zero-row object with status
   PGRES_TUPLES_OK is returned; this is the signal that no
   more rows will arrive.  (But note that it is still necessary to continue
   calling  until it returns null.)  All of
   these PGresult objects will contain the same row
   description data (column names, types, etc.) that an ordinary
   PGresult object for the query would have.
   Each object should be freed with  as usual.
  

  

   When using pipeline mode, single-row or chunked mode needs to be
   activated for each query in the pipeline before retrieving results for
   that query with PQgetResult.
   See  for more information.
  

  

   

    

     
PQsetSingleRowModePQsetSingleRowMode

     

      

       Select single-row mode for the currently-executing query.

int PQsetSingleRowMode(PGconn *conn);

      

      

       This function can only be called immediately after
        or one of its sibling functions,
       before any other operation on the connection such as
        or
       .  If called at the correct time,
       the function activates single-row mode for the current query and
       returns 1.  Otherwise the mode stays unchanged and the function
       returns 0.  In any case, the mode reverts to normal after
       completion of the current query.
      

     

    

    

     
PQsetChunkedRowsModePQsetChunkedRowsMode

     

      

       Select chunked mode for the currently-executing query.

int PQsetChunkedRowsMode(PGconn *conn, int chunkSize);

      

      

       This function is similar to
       , except that it
       specifies retrieval of up to chunkSize rows
       per PGresult, not necessarily just one row.
       This function can only be called immediately after
        or one of its sibling functions,
       before any other operation on the connection such as
        or
       .  If called at the correct time,
       the function activates chunked mode for the current query and
       returns 1.  Otherwise the mode stays unchanged and the function
       returns 0.  In any case, the mode reverts to normal after
       completion of the current query.
      

     

    

   

  

  
   

    While processing a query, the server may return some rows and then
    encounter an error, causing the query to be aborted.  Ordinarily,
    libpq discards any such rows and reports only the
    error.  But in single-row or chunked mode, some rows may have already
    been returned to the application. Hence, the application will see some
    PGRES_SINGLE_TUPLE or PGRES_TUPLES_CHUNK
    PGresult
    objects followed by a PGRES_FATAL_ERROR object.  For
    proper transactional behavior, the application must be designed to
    discard or undo whatever has been done with the previously-processed
    rows, if the query ultimately fails.
   

  

 

 

  
# Canceling Queries in Progress

  
   canceling SQL queries
  
  
   query cancellation
  

  

   
# Functions for Sending Cancel Requests

   

    

     
PQcancelCreatePQcancelCreate

     

      

       Prepares a connection over which a cancel request can be sent.

PGcancelConn *PQcancelCreate(PGconn *conn);

      

      

        creates a
       PGcancelConnPGcancelConn
       object, but it won't instantly start sending a cancel request over this
       connection. A cancel request can be sent over this connection in a
       blocking manner using  and in a
       non-blocking manner using .
       The return value can be passed to 
       to check if the PGcancelConn object was
       created successfully. The PGcancelConn object
       is an opaque structure that is not meant to be accessed directly by the
       application. This PGcancelConn object can be
       used to cancel the query that's running on the original connection in a
       thread-safe way.
      

      

       Many connection parameters of the original client will be reused when
       setting up the connection for the cancel request. Importantly, if the
       original connection requires encryption of the connection and/or
       verification of the target host (using sslmode or
       gssencmode), then the connection for the cancel
       request is made with these same requirements. Any connection options
       that are only used during authentication or after authentication of the
       client are ignored though, because cancellation requests do not require
       authentication and the connection is closed right after the cancellation
       request is submitted.
      

      

       Note that when PQcancelCreate returns a non-null
       pointer, you must call  when you
       are finished with it, in order to dispose of the structure and any
       associated memory blocks. This must be done even if the cancel request
       failed or was abandoned.
      

     

    

    

     
PQcancelBlockingPQcancelBlocking

     

      

       Requests that the server abandons processing of the current command
       in a blocking manner.

int PQcancelBlocking(PGcancelConn *cancelConn);

      

      

       The request is made over the given PGcancelConn,
       which needs to be created with .
       The return value of 
       is 1 if the cancel request was successfully
       dispatched and 0 if not. If it was unsuccessful, the error message can be
       retrieved using .
      

      

       Successful dispatch of the cancellation is no guarantee that the request
       will have any effect, however. If the cancellation is effective, the
       command being canceled will terminate early and return an error result.
       If the cancellation fails (say, because the server was already done
       processing the command), then there will be no visible result at all.
      

     

    

    

     
PQcancelStartPQcancelStart

     
PQcancelPollPQcancelPoll

     

      

       Requests that the server abandons processing of the current command
       in a non-blocking manner.

int PQcancelStart(PGcancelConn *cancelConn);

PostgresPollingStatusType PQcancelPoll(PGcancelConn *cancelConn);

      

      

       The request is made over the given PGcancelConn,
       which needs to be created with .
       The return value of 
       is 1 if the cancellation request could be started and 0 if not.
       If it was unsuccessful, the error message can be
       retrieved using .
      

      

       If PQcancelStart succeeds, the next stage
       is to poll libpq so that it can proceed with
       the cancel connection sequence.
       Use  to obtain the descriptor of the
       socket underlying the database connection.
       (Caution: do not assume that the socket remains the same
       across PQcancelPoll calls.)
       Loop thus: If PQcancelPoll(cancelConn) last returned
       PGRES_POLLING_READING, wait until the socket is ready to
       read (as indicated by select(),
       poll(), or similar system function).
       Then call PQcancelPoll(cancelConn) again.
       Conversely, if PQcancelPoll(cancelConn) last returned
       PGRES_POLLING_WRITING, wait until the socket is ready
       to write, then call PQcancelPoll(cancelConn) again.
       On the first iteration, i.e., if you have yet to call
       PQcancelPoll(cancelConn), behave as if it last returned
       PGRES_POLLING_WRITING.  Continue this loop until
       PQcancelPoll(cancelConn) returns
       PGRES_POLLING_FAILED, indicating the connection procedure
       has failed, or PGRES_POLLING_OK, indicating cancel
       request was successfully dispatched.
      

      

       Successful dispatch of the cancellation is no guarantee that the request
       will have any effect, however. If the cancellation is effective, the
       command being canceled will terminate early and return an error result.
       If the cancellation fails (say, because the server was already done
       processing the command), then there will be no visible result at all.
      

      

       At any time during connection, the status of the connection can be
       checked by calling .
       If this call returns CONNECTION_BAD, then
       the cancel procedure has failed; if the call returns
       CONNECTION_OK, then cancel request was
       successfully dispatched.
       Both of these states are equally detectable from the return value of
       PQcancelPoll, described above.
       Other states might also occur during (and only during) an asynchronous
       connection procedure.
       These indicate the current stage of the connection procedure and might
       be useful to provide feedback to the user for example.
       These statuses are:

       

        

         
CONNECTION_ALLOCATED

         

          

           Waiting for a call to  or
           , to actually open the
           socket. This is the connection state right after
           calling 
           or . No connection to the
           server has been initiated yet at this point. To actually start
           sending the cancel request use  or
           .
          

         

        

        

         
CONNECTION_STARTED

         

          

           Waiting for connection to be made.
          

         

        

        

         
CONNECTION_MADE

         

          

           Connection OK; waiting to send.
          

         

        

        

         
CONNECTION_AWAITING_RESPONSE

         

          

           Waiting for a response from the server.
          

         

        

        

         
CONNECTION_SSL_STARTUP

         

          

           Negotiating SSL encryption.
          

         

        

        

         
CONNECTION_GSS_STARTUP

         

          

           Negotiating GSS encryption.
          

         

        

       

       Note that, although these constants will remain (in order to maintain
       compatibility), an application should never rely upon these occurring in a
       particular order, or at all, or on the status always being one of these
       documented values. An application might do something like this:

```
switch(PQcancelStatus(conn))
{
        case CONNECTION_STARTED:
            feedback = "Connecting...";
            break;

        case CONNECTION_MADE:
            feedback = "Connected to server...";
            break;
.
.
.
        default:
            feedback = "Connecting...";
}
```

      

      

       The connect_timeout connection parameter is ignored
       when using PQcancelPoll; it is the application's
       responsibility to decide whether an excessive amount of time has elapsed.
       Otherwise, PQcancelStart followed by a
       PQcancelPoll loop is equivalent to
       .
      

     

    

    

     
PQcancelStatusPQcancelStatus

     

      

       Returns the status of the cancel connection.

ConnStatusType PQcancelStatus(const PGcancelConn *cancelConn);

      

      

       The status can be one of a number of values.  However, only three of
       these are seen outside of an asynchronous cancel procedure:
       CONNECTION_ALLOCATED,
       CONNECTION_OK and
       CONNECTION_BAD. The initial state of a
       PGcancelConn that's successfully created using
        is CONNECTION_ALLOCATED.
       A cancel request that was successfully dispatched
       has the status CONNECTION_OK.  A failed
       cancel attempt is signaled by status
       CONNECTION_BAD.  An OK status will
       remain so until  or
        is called.
      

      

       See the entry for  with regards
       to other status codes that might be returned.
      

      

       Successful dispatch of the cancellation is no guarantee that the request
       will have any effect, however. If the cancellation is effective, the
       command being canceled will terminate early and return an error result.
       If the cancellation fails (say, because the server was already done
       processing the command), then there will be no visible result at all.
      

     

    

    

     
PQcancelSocketPQcancelSocket

     

      

       Obtains the file descriptor number of the cancel connection socket to
       the server.

int PQcancelSocket(const PGcancelConn *cancelConn);

      

      

       A valid descriptor will be greater than or equal to 0;
       a result of -1 indicates that no server connection is currently open.
       This might change as a result of calling any of the functions
       in this section on the PGcancelConn
       (except for  and
       PQcancelSocket itself).
      

     

    

    

     

      PQcancelErrorMessagePQcancelErrorMessage
      error messagein PGcancelConn
     

     

      

       Returns the error message most recently generated by an
       operation on the cancel connection.

char *PQcancelErrorMessage(const PGcancelConn *cancelconn);

      

      

       Nearly all libpq functions that take a
       PGcancelConn will set a message for
        if they fail.
       Note that by libpq convention,
       a nonempty  result
       can consist of multiple lines, and will include a trailing newline.
       The caller should not free the result directly.
       It will be freed when the associated
       PGcancelConn handle is passed to
       .  The result string should not be
       expected to remain the same across operations on the
       PGcancelConn structure.
      

     

    

    

     
PQcancelFinishPQcancelFinish

     

      

       Closes the cancel connection (if it did not finish sending the
       cancel request yet). Also frees memory used by the
       PGcancelConn object.

void PQcancelFinish(PGcancelConn *cancelConn);

      

      

       Note that even if the cancel attempt fails (as
       indicated by ), the
       application should call 
       to free the memory used by the PGcancelConn
       object.
       The PGcancelConn pointer must not be used
       again after  has been called.
      

     

    

    

     
PQcancelResetPQcancelReset

     

      

       Resets the PGcancelConn so it can be reused for a new
       cancel connection.

void PQcancelReset(PGcancelConn *cancelConn);

      

      

       If the PGcancelConn is currently used to send a cancel
       request, then this connection is closed. It will then prepare the
       PGcancelConn object such that it can be used to send a
       new cancel request.
      

      

       This can be used to create one PGcancelConn
       for a PGconn and reuse it multiple times
       throughout the lifetime of the original PGconn.
      

     

    

   

  

  

   
# Obsolete Functions for Sending Cancel Requests

   

    These functions represent older methods of sending cancel requests.
    Although they still work, they are deprecated due to not sending the cancel
    requests in an encrypted manner, even when the original connection
    specified sslmode or gssencmode to
    require encryption. Thus these older methods are heavily discouraged from
    being used in new code, and it is recommended to change existing code to
    use the new functions instead.
   

   

    

     
PQgetCancelPQgetCancel

     

      

       Creates a data structure containing the information needed to cancel
       a command using .

PGcancel *PQgetCancel(PGconn *conn);

      

      

        creates a
       PGcancelPGcancel
       object given a PGconn connection object.
       It will return NULL if the given 
conn

       is NULL or an invalid connection.
       The PGcancel object is an opaque
       structure that is not meant to be accessed directly by the
       application; it can only be passed to 
       or .
      

     

    

    

     
PQfreeCancelPQfreeCancel

     

      

       Frees a data structure created by .

void PQfreeCancel(PGcancel *cancel);

      

      

        frees a data object previously created
       by .
      

     

    

    

     
PQcancelPQcancel

     

      

        is a deprecated and insecure
       variant of , but one that can be
       used safely from within a signal handler.

int PQcancel(PGcancel *cancel, char *errbuf, int errbufsize);

      

      

        only exists because of backwards
       compatibility reasons.  should be
       used instead. The only benefit that  has
       is that it can be safely invoked from a signal handler, if the
       
errbuf
 is a local variable in the signal handler.
       However, this is generally not considered a big enough benefit to be
       worth the security issues that this function has.
      

      

       The PGcancel object is read-only as far as
        is concerned, so it can also be invoked
       from a thread that is separate from the one manipulating the
       PGconn object.
      

      

       The return value of  is 1 if the
       cancel request was successfully dispatched and 0 if not.
       If not, 
errbuf
 is filled with an explanatory
       error message.
       
errbuf
 must be a char array of size
       
errbufsize
 (the recommended size is 256 bytes).
      

     

    

   

   

    

     
PQrequestCancelPQrequestCancel

     

      

        is a deprecated and insecure
       variant of .

int PQrequestCancel(PGconn *conn);

      

      

        only exists because of backwards
       compatibility reasons.  should be
       used instead. There is no benefit to using
        over
       .
      

      

       Requests that the server abandon processing of the current
       command.  It operates directly on the
       PGconn object, and in case of failure stores the
       error message in the PGconn object (whence it can
       be retrieved by ).  Although
       the functionality is the same, this approach is not safe within
       multiple-thread programs or signal handlers, since it is possible
       that overwriting the PGconn's error message will
       mess up the operation currently in progress on the connection.
      

     

    

   

  

 

 

  
# Asynchronous Notification

  
   NOTIFY
   in libpq
  

  

   PostgreSQL offers asynchronous notification
   via the LISTEN and NOTIFY
   commands.  A client session registers its interest in a particular
   notification channel with the LISTEN command (and
   can stop listening with the UNLISTEN command).  All
   sessions listening on a particular channel will be notified
   asynchronously when a NOTIFY command with that
   channel name is executed by any session. A payload string can
   be passed to communicate additional data to the listeners.
  

  

   libpq applications submit
   LISTEN, UNLISTEN,
   and NOTIFY commands as
   ordinary SQL commands.  The arrival of NOTIFY
   messages can subsequently be detected by calling
   PQnotifies.PQnotifies
  

  

   The function PQnotifies returns the next notification
   from a list of unhandled notification messages received from the server.
   It returns a null pointer if there are no pending notifications.  Once a
   notification is returned from PQnotifies, it is considered
   handled and will be removed from the list of notifications.

PGnotify *PQnotifies(PGconn *conn);

typedef struct pgNotify
{
    char *relname;              /* notification channel name */
    int  be_pid;                /* process ID of notifying server process */
    char *extra;                /* notification payload string */
} PGnotify;

   After processing a PGnotify object returned
   by PQnotifies, be sure to free it with
   .  It is sufficient to free the
   PGnotify pointer; the
   relname and extra
   fields do not represent separate allocations.  (The names of these fields
   are historical; in particular, channel names need not have anything to
   do with relation names.)
  

  

    gives a sample program that illustrates
   the use of asynchronous notification.
  

  

   PQnotifies does not actually read data from the
   server; it just returns messages previously absorbed by another
   libpq function.  In ancient releases of
   libpq, the only way to ensure timely receipt
   of NOTIFY messages was to constantly submit commands, even
   empty ones, and then check PQnotifies after each
   .  While this still works, it is deprecated
   as a waste of processing power.
  

  

   A better way to check for NOTIFY messages when you have no
   useful commands to execute is to call
   , then check
   PQnotifies.  You can use
   select() to wait for data to arrive from the
   server, thereby using no CPU power unless there is
   something to do.  (See  to obtain the file
   descriptor number to use with select().) Note that
   this will work OK whether you submit commands with
   / or
   simply use .  You should, however, remember
   to check PQnotifies after each
    or , to
   see if any notifications came in during the processing of the command.
  

 

 

  
# Functions Associated with the COPY Command

  
   COPY
   with libpq
  

  

   The COPY command in
   PostgreSQL has options to read from or write
   to the network connection used by libpq.
   The functions described in this section allow applications to take
   advantage of this capability by supplying or consuming copied data.
  

  

   The overall process is that the application first issues the SQL
   COPY command via  or one
   of the equivalent functions.  The response to this (if there is no
   error in the command) will be a PGresult object bearing
   a status code of PGRES_COPY_OUT or
   PGRES_COPY_IN (depending on the specified copy
   direction).  The application should then use the functions of this
   section to receive or transmit data rows.  When the data transfer is
   complete, another PGresult object is returned to indicate
   success or failure of the transfer.  Its status will be
   PGRES_COMMAND_OK for success or
   PGRES_FATAL_ERROR if some problem was encountered.
   At this point further SQL commands can be issued via
   .  (It is not possible to execute other SQL
   commands using the same connection while the COPY
   operation is in progress.)
  

  

   If a COPY command is issued via
    in a string that could contain additional
   commands, the application must continue fetching results via
    after completing the COPY
   sequence.  Only when  returns
   NULL is it certain that the 
   command string is done and it is safe to issue more commands.
  

  

   The functions of this section should be executed only after obtaining
   a result status of PGRES_COPY_OUT or
   PGRES_COPY_IN from  or
   .
  

  

   A PGresult object bearing one of these status values
   carries some additional data about the COPY operation
   that is starting.  This additional data is available using functions
   that are also used in connection with query results:

   

    

     
PQnfieldsPQnfieldswith COPY

     

      

       Returns the number of columns (fields) to be copied.
      

     

    

    

     
PQbinaryTuplesPQbinaryTupleswith COPY

     

      

       0 indicates the overall copy format is textual (rows separated by
       newlines, columns separated by separator characters, etc.).  1
       indicates the overall copy format is binary.  See  for more information.
      

     

    

    

     
PQfformatPQfformatwith COPY

     

      

       Returns the format code (0 for text, 1 for binary) associated with
       each column of the copy operation.  The per-column format codes
       will always be zero when the overall copy format is textual, but
       the binary format can support both text and binary columns.
       (However, as of the current implementation of COPY,
       only binary columns appear in a binary copy; so the per-column
       formats always match the overall format at present.)
      

     

    

   

  

  

   
# Functions for Sending COPY Data

   

    These functions are used to send data during COPY FROM
    STDIN.  They will fail if called when the connection is not in
    COPY_IN state.
   

   

    

     
PQputCopyDataPQputCopyData

     

      

       Sends data to the server during COPY_IN state.

int PQputCopyData(PGconn *conn,
                  const char *buffer,
                  int nbytes);

      

      

       Transmits the COPY data in the specified
       
buffer
, of length 
nbytes
, to the server.
       The result is 1 if the data was queued, zero if it was not queued
       because of full buffers (this will only happen in nonblocking mode),
       or -1 if an error occurred.
       (Use  to retrieve details if
       the return value is -1.  If the value is zero, wait for write-ready
       and try again.)
      

      

       The application can divide the COPY data stream
       into buffer loads of any convenient size.  Buffer-load boundaries
       have no semantic significance when sending.  The contents of the
       data stream must match the data format expected by the
       COPY command; see  for details.
      

     

    

    

     
PQputCopyEndPQputCopyEnd

     

      

       Sends end-of-data indication to the server during COPY_IN state.

int PQputCopyEnd(PGconn *conn,
                 const char *errormsg);

      

      

       Ends the COPY_IN operation successfully if
       
errormsg
 is NULL.  If
       
errormsg
 is not NULL then the
       COPY is forced to fail, with the string pointed to by
       
errormsg
 used as the error message.  (One should not
       assume that this exact error message will come back from the server,
       however, as the server might have already failed the
       COPY for its own reasons.)
      

      

       The result is 1 if the termination message was sent; or in
       nonblocking mode, this may only indicate that the termination
       message was successfully queued.  (In nonblocking mode, to be
       certain that the data has been sent, you should next wait for
       write-ready and call , repeating until it
       returns zero.)  Zero indicates that the function could not queue
       the termination message because of full buffers; this will only
       happen in nonblocking mode.  (In this case, wait for
       write-ready and try the  call
       again.)  If a hard error occurs, -1 is returned; you can use
        to retrieve details.
      

      

       After successfully calling , call
        to obtain the final result status of the
       COPY command.  One can wait for this result to be
       available in the usual way.  Then return to normal operation.
      

     

    

   

  

  

   
# Functions for Receiving COPY Data

   

    These functions are used to receive data during COPY TO
    STDOUT.  They will fail if called when the connection is not in
    COPY_OUT state.
   

   

    

     
PQgetCopyDataPQgetCopyData

     

      

       Receives data from the server during COPY_OUT state.

int PQgetCopyData(PGconn *conn,
                  char **buffer,
                  int async);

      

      

       Attempts to obtain another row of data from the server during a
       COPY.  Data is always returned one data row at
       a time; if only a partial row is available, it is not returned.
       Successful return of a data row involves allocating a chunk of
       memory to hold the data.  The 
buffer
 parameter must
       be non-NULL.  
*buffer
 is set to
       point to the allocated memory, or to NULL in cases
       where no buffer is returned.  A non-NULL result
       buffer should be freed using  when no longer
       needed.
      

      

       When a row is successfully returned, the return value is the number
       of data bytes in the row (this will always be greater than zero).
       The returned string is always null-terminated, though this is
       probably only useful for textual COPY.  A result
       of zero indicates that the COPY is still in
       progress, but no row is yet available (this is only possible when
       
async
 is true).  A result of -1 indicates that the
       COPY is done.  A result of -2 indicates that an
       error occurred (consult  for the reason).
      

      

       When 
async
 is true (not zero),
        will not block waiting for input; it
       will return zero if the COPY is still in progress
       but no complete row is available.  (In this case wait for read-ready
       and then call  before calling
        again.)  When 
async
 is
       false (zero),  will block until data is
       available or the operation completes.
      

      

       After  returns -1, call
        to obtain the final result status of the
       COPY command.  One can wait for this result to be
       available in the usual way.  Then return to normal operation.
      

     

    

   

  

  

   
# Obsolete Functions for COPY

   

    These functions represent older methods of handling COPY.
    Although they still work, they are deprecated due to poor error handling,
    inconvenient methods of detecting end-of-data, and lack of support for binary
    or nonblocking transfers.
   

   

    

     
PQgetlinePQgetline

     

      

       Reads  a  newline-terminated  line  of  characters (transmitted
       by the server) into a buffer string of size 
length
.

int PQgetline(PGconn *conn,
              char *buffer,
              int length);

      

      

       This function copies up to 
length
-1 characters into
       the buffer and converts the terminating newline into a zero byte.
        returns EOF at the
       end of input, 0 if the entire line has been read, and 1 if the
       buffer is full but the terminating newline has not yet been read.
       

       

       Note that the application must check to see if a new line consists
       of  the  two characters  \., which  indicates
       that the server has finished sending the results  of  the
       COPY command.  If  the  application might receive
       lines that are more than 
length
-1  characters  long,
       care is needed to be sure it recognizes the \.
       line correctly (and does not, for example, mistake the end of a
       long data line for a terminator line).
      

     

    

    

     
PQgetlineAsyncPQgetlineAsync

     

      

       Reads a row of COPY data (transmitted  by the
       server) into a buffer without blocking.

int PQgetlineAsync(PGconn *conn,
                   char *buffer,
                   int bufsize);

      

      

       This function is similar to , but it can be used
       by applications
       that must read COPY data asynchronously, that is, without blocking.
       Having issued the COPY command and gotten a PGRES_COPY_OUT
       response, the
       application should call  and
        until the
       end-of-data signal is detected.
       

       

       Unlike , this function takes
       responsibility for detecting end-of-data.
      

      

       On each call,  will return data if a
       complete data row is available in libpq's input buffer.
       Otherwise, no data is returned until the rest of the row arrives.
       The function returns -1 if the end-of-copy-data marker has been recognized,
       or 0 if no data is available, or a positive number giving the number of
       bytes of data returned.  If -1 is returned, the caller must next call
       , and then return to normal processing.
      

      

       The data returned will not extend beyond a data-row boundary.  If possible
       a whole row will be returned at one time.  But if the buffer offered by
       the caller is too small to hold a row sent by the server, then a partial
       data row will be returned.  With textual data this can be detected by testing
       whether the last returned byte is \n or not.  (In a binary
       COPY, actual parsing of the COPY data format will be needed to make the
       equivalent determination.)
       The returned string is not null-terminated.  (If you want to add a
       terminating null, be sure to pass a 
bufsize
 one smaller
       than the room actually available.)
      

     

    

    

     
PQputlinePQputline

     

      

       Sends  a  null-terminated  string  to  the server.  Returns 0 if
       OK and EOF if unable to send the string.

int PQputline(PGconn *conn,
              const char *string);

      

      

       The COPY data stream sent by a series of calls
       to  has the same format as that
       returned by , except that
       applications are not obliged to send exactly one data row per
        call; it is okay to send a partial
       line or multiple lines per call.
      

      
       

        Before PostgreSQL protocol 3.0, it was necessary
        for the application to explicitly send the two characters
        \. as a final line to indicate to the server that it had
        finished sending COPY data.  While this still works, it is deprecated and the
        special meaning of \. can be expected to be removed in a
        future release.  (It already will misbehave in CSV
        mode.)  It is sufficient to call 
        after having sent the actual data.
       

      
     

    

    

     
PQputnbytesPQputnbytes

     

      

       Sends  a  non-null-terminated  string  to  the server.  Returns
       0 if OK and EOF if unable to send the string.

int PQputnbytes(PGconn *conn,
                const char *buffer,
                int nbytes);

      

      

       This is exactly like , except that the data
       buffer need not be null-terminated since the number of bytes to send is
       specified directly.  Use this procedure when sending binary data.
      

     

    

    

     
PQendcopyPQendcopy

     

      

       Synchronizes with the server.

int PQendcopy(PGconn *conn);

       This function waits until the  server  has  finished  the copying.
       It should either be issued when the  last  string  has  been sent
       to  the  server using  or when the
       last string has been  received  from  the  server using
       PQgetline.  It must be issued or the server
       will get out of sync with  the client.   Upon return
       from this function, the server is ready to receive the next SQL
       command.  The return value is 0  on  successful  completion,
       nonzero otherwise.  (Use  to
       retrieve details if the return value is nonzero.)
      

      

       When using , the application should
       respond to a PGRES_COPY_OUT result by executing
        repeatedly, followed by
        after the terminator line is seen.
       It should then return to the  loop
       until  returns a null pointer.
       Similarly a PGRES_COPY_IN result is processed
       by a series of  calls followed by
       , then return to the
        loop.  This arrangement will
       ensure that a COPY command embedded in a series
       of SQL commands will be executed correctly.
      

      

       Older applications are likely to submit a COPY
       via  and assume that the transaction
       is done after .  This will work
       correctly only if the COPY is the only
       SQL command in the command string.
      

     

    

   

  

 

 

  
# Control Functions

  

   These functions control miscellaneous details of libpq's
   behavior.
  

  

   

    
PQclientEncodingPQclientEncoding

    

     

      Returns the client encoding.

int PQclientEncoding(const PGconn *conn);

      Note that it returns the encoding ID, not a symbolic string
      such as EUC_JP. If unsuccessful, it returns -1.
      To convert an encoding ID to an encoding name, you
      can use:

char *pg_encoding_to_char(int encoding_id);

     

    

   

   

    
PQsetClientEncodingPQsetClientEncoding

    

     

      Sets the client encoding.

int PQsetClientEncoding(PGconn *conn, const char *encoding);

      conn is a connection to the server,
      and encoding is the encoding you want to
      use. If the function successfully sets the encoding, it returns 0,
      otherwise -1. The current encoding for this connection can be
      determined by using .
     

    

   

   

    
PQsetErrorVerbosityPQsetErrorVerbosity

    

     

      Determines the verbosity of messages returned by
       and .

typedef enum
{
    PQERRORS_TERSE,
    PQERRORS_DEFAULT,
    PQERRORS_VERBOSE,
    PQERRORS_SQLSTATE
} PGVerbosity;

PGVerbosity PQsetErrorVerbosity(PGconn *conn, PGVerbosity verbosity);

       sets the verbosity mode,
      returning the connection's previous setting.
      In TERSE mode, returned messages include
      severity, primary text, and position only; this will normally fit on a
      single line.  The DEFAULT mode produces messages
      that include the above plus any detail, hint, or context fields (these
      might span multiple lines).  The VERBOSE mode
      includes all available fields.  The SQLSTATE
      mode includes only the error severity and the SQLSTATE
      error code, if one is available (if not, the output is like
      TERSE mode).
     

     

      Changing the verbosity setting does not affect the messages available
      from already-existing PGresult objects, only
      subsequently-created ones.
      (But see  if you
      want to print a previous error with a different verbosity.)
     

    

   

   

    
PQsetErrorContextVisibilityPQsetErrorContextVisibility

    

     

      Determines the handling of CONTEXT fields in messages
      returned by 
      and .

typedef enum
{
    PQSHOW_CONTEXT_NEVER,
    PQSHOW_CONTEXT_ERRORS,
    PQSHOW_CONTEXT_ALWAYS
} PGContextVisibility;

PGContextVisibility PQsetErrorContextVisibility(PGconn *conn, PGContextVisibility show_context);

       sets the context display mode,
      returning the connection's previous setting.  This mode controls
      whether the CONTEXT field is included in messages.
      The NEVER mode
      never includes CONTEXT, while ALWAYS always
      includes it if available.  In ERRORS mode (the
      default), CONTEXT fields are included only in error
      messages, not in notices and warnings.
      (However, if the verbosity setting is TERSE
      or SQLSTATE, CONTEXT fields
      are omitted regardless of the context display mode.)
     

     

      Changing this mode does not
      affect the messages available from
      already-existing PGresult objects, only
      subsequently-created ones.
      (But see  if you
      want to print a previous error with a different display mode.)
     

    

   

   

    
PQtracePQtrace

    

     

      Enables tracing of the client/server communication to a debugging file
      stream.

void PQtrace(PGconn *conn, FILE *stream);

     

     

      Each line consists of: an optional timestamp, a direction indicator
      (F for messages from client to server
      or B for messages from server to client),
      message length, message type, and message contents.
      Non-message contents fields (timestamp, direction, length and message type)
      are separated by a tab. Message contents are separated by a space.
      Protocol strings are enclosed in double quotes, while strings used as data
      values are enclosed in single quotes.  Non-printable chars are printed as
      hexadecimal escapes.
      Further message-type-specific detail can be found in
      .
     

     
      

       On Windows, if the libpq library and an application are
       compiled with different flags, this function call will crash the
       application because the internal representation of the FILE
       pointers differ.  Specifically, multithreaded/single-threaded,
       release/debug, and static/dynamic flags should be the same for the
       library and all applications using that library.
      

     

    

   

   

    
PQsetTraceFlagsPQsetTraceFlags

    

     

      Controls the tracing behavior of client/server communication.

void PQsetTraceFlags(PGconn *conn, int flags);

     

     

      flags contains flag bits describing the operating mode
      of tracing.
      If flags contains PQTRACE_SUPPRESS_TIMESTAMPS,
      then the timestamp is not included when printing each message.
      If flags contains PQTRACE_REGRESS_MODE,
      then some fields are redacted when printing each message, such as object
      OIDs, to make the output more convenient to use in testing frameworks.
      This function must be called after calling PQtrace.
     

    

   

   

    
PQuntracePQuntrace

    

     

      Disables tracing started by .

void PQuntrace(PGconn *conn);

     

    

   

  

 

 

  
# Miscellaneous Functions

  

   As always, there are some functions that just don't fit anywhere.
  

  

   

    
PQfreememPQfreemem

    

     

      Frees memory allocated by libpq.

void PQfreemem(void *ptr);

     

     

      Frees memory allocated by libpq, particularly
      ,
      ,
      ,
      and PQnotifies.
      It is particularly important that this function, rather than
      free(), be used on Microsoft Windows.  This is because
      allocating memory in a DLL and releasing it in the application works
      only if multithreaded/single-threaded, release/debug, and static/dynamic
      flags are the same for the DLL and the application.  On non-Microsoft
      Windows platforms, this function is the same as the standard library
      function free().
     

    

   

   

    
PQconninfoFreePQconninfoFree

    

     

      Frees the data structures allocated by
       or .

void PQconninfoFree(PQconninfoOption *connOptions);

      If the argument is a NULL pointer, no operation is
      performed.
     

     

      A simple  will not do for this, since
      the array contains references to subsidiary strings.
     

    

   

   

    
PQencryptPasswordConnPQencryptPasswordConn

    

     

      Prepares the encrypted form of a PostgreSQL password.

char *PQencryptPasswordConn(PGconn *conn, const char *passwd, const char *user, const char *algorithm);

      This function is intended to be used by client applications that
      wish to send commands like ALTER USER joe PASSWORD
      'pwd'.  It is good practice not to send the original cleartext
      password in such a command, because it might be exposed in command
      logs, activity displays, and so on.  Instead, use this function to
      convert the password to encrypted form before it is sent.
     

     

      The 
passwd
 and 
user
 arguments
      are the cleartext password, and the SQL name of the user it is for.
      
algorithm
 specifies the encryption algorithm
      to use to encrypt the password. Currently supported algorithms are
      md5 and scram-sha-256 (on and
      off are also accepted as aliases for md5, for
      compatibility with older server versions). Note that support for
      scram-sha-256 was introduced in PostgreSQL
      version 10, and will not work correctly with older server versions. If
      
algorithm
 is NULL, this function will query
      the server for the current value of the
       setting. That can block, and
      will fail if the current transaction is aborted, or if the connection
      is busy executing another query. If you wish to use the default
      algorithm for the server but want to avoid blocking, query
      password_encryption yourself before calling
      , and pass that value as the
      
algorithm
.
     

     

      The return value is a string allocated by malloc.
      The caller can assume the string doesn't contain any special characters
      that would require escaping.  Use  to free the
      result when done with it. On error, returns NULL, and
      a suitable message is stored in the connection object.
     

    

   

   

    
PQchangePasswordPQchangePassword

    

     

      Changes a PostgreSQL password.

PGresult *PQchangePassword(PGconn *conn, const char *user, const char *passwd);

      This function uses PQencryptPasswordConn
      to build and execute the command ALTER USER ... PASSWORD
      '...', thereby changing the user's password. It exists for
      the same reason as PQencryptPasswordConn, but
      is more convenient as it both builds and runs the command for you.
       is passed a
      NULL for the algorithm argument, hence encryption is
      done according to the server's 
      setting.
     

     

      The 
user
 and 
passwd
 arguments
      are the SQL name of the target user, and the new cleartext password.
     

     

      Returns a PGresult pointer representing
      the result of the ALTER USER command, or
      a null pointer if the routine failed before issuing any command.
      The  function should be called
      to check the return value for any errors (including the value of a null
      pointer, in which case it will return
      PGRES_FATAL_ERROR). Use
       to get more information about
      such errors.
     

    

   

   

    
PQencryptPasswordPQencryptPassword

    

     

      Prepares the md5-encrypted form of a PostgreSQL password.

char *PQencryptPassword(const char *passwd, const char *user);

       is an older, deprecated version of
      . The difference is that
       does not
      require a connection object, and md5 is always used as the
      encryption algorithm.
     

    

   

   

    
PQmakeEmptyPGresultPQmakeEmptyPGresult

    

     

      Constructs an empty PGresult object with the given status.

PGresult *PQmakeEmptyPGresult(PGconn *conn, ExecStatusType status);

     

     

      This is libpq's internal function to allocate and
      initialize an empty PGresult object.  This
      function returns NULL if memory could not be allocated. It is
      exported because some applications find it useful to generate result
      objects (particularly objects with error status) themselves.  If
      
conn
 is not null and 
status

      indicates an error, the current error message of the specified
      connection is copied into the PGresult.
      Also, if 
conn
 is not null, any event procedures
      registered in the connection are copied into the
      PGresult.  (They do not get
      PGEVT_RESULTCREATE calls, but see
      .)
      Note that  should eventually be called
      on the object, just as with a PGresult
      returned by libpq itself.
     

    

   

   

    
PQfireResultCreateEventsPQfireResultCreateEvents

    

     

      Fires a PGEVT_RESULTCREATE event (see ) for each event procedure registered in the
      PGresult object.  Returns non-zero for success,
      zero if any event procedure fails.

int PQfireResultCreateEvents(PGconn *conn, PGresult *res);

     

     

      The conn argument is passed through to event procedures
      but not used directly.  It can be NULL if the event
      procedures won't use it.
     

     

      Event procedures that have already received a
      PGEVT_RESULTCREATE or PGEVT_RESULTCOPY event
      for this object are not fired again.
     

     

      The main reason that this function is separate from
       is that it is often appropriate
      to create a PGresult and fill it with data
      before invoking the event procedures.
     

    

   

   

    
PQcopyResultPQcopyResult

    

     

      Makes a copy of a PGresult object.  The copy is
      not linked to the source result in any way and
       must be called when the copy is no longer
      needed.  If the function fails, NULL is returned.

PGresult *PQcopyResult(const PGresult *src, int flags);

     

     

      This is not intended to make an exact copy.  The returned result is
      always put into PGRES_TUPLES_OK status, and does not
      copy any error message in the source.  (It does copy the command status
      string, however.)  The 
flags
 argument determines
      what else is copied.  It is a bitwise OR of several flags.
      PG_COPYRES_ATTRS specifies copying the source
      result's attributes (column definitions).
      PG_COPYRES_TUPLES specifies copying the source
      result's tuples.  (This implies copying the attributes, too.)
      PG_COPYRES_NOTICEHOOKS specifies
      copying the source result's notify hooks.
      PG_COPYRES_EVENTS specifies copying the source
      result's events.  (But any instance data associated with the source
      is not copied.)
      The event procedures receive PGEVT_RESULTCOPY events.
     

    

   

   

    
PQsetResultAttrsPQsetResultAttrs

    

     

      Sets the attributes of a PGresult object.

int PQsetResultAttrs(PGresult *res, int numAttributes, PGresAttDesc *attDescs);

     

     

      The provided 
attDescs
 are copied into the result.
      If the 
attDescs
 pointer is NULL or
      
numAttributes
 is less than one, the request is
      ignored and the function succeeds.  If 
res

      already contains attributes, the function will fail.  If the function
      fails, the return value is zero.  If the function succeeds, the return
      value is non-zero.
     

    

   

   

    
PQsetvaluePQsetvalue

    

     

      Sets a tuple field value of a PGresult object.

int PQsetvalue(PGresult *res, int tup_num, int field_num, char *value, int len);

     

     

      The function will automatically grow the result's internal tuples array
      as needed.  However, the 
tup_num
 argument must be
      less than or equal to , meaning this
      function can only grow the tuples array one tuple at a time.  But any
      field of any existing tuple can be modified in any order.  If a value at
      
field_num
 already exists, it will be overwritten.
      If 
len
 is -1 or
      
value
 is NULL, the field value
      will be set to an SQL null value.  The
      
value
 is copied into the result's private storage,
      thus is no longer needed after the function
      returns.  If the function fails, the return value is zero.  If the
      function succeeds, the return value is non-zero.
     

    

   

   

    
PQresultAllocPQresultAlloc

    

     

      Allocate subsidiary storage for a PGresult object.

void *PQresultAlloc(PGresult *res, size_t nBytes);

     

     

      Any memory allocated with this function will be freed when
      
res
 is cleared.  If the function fails,
      the return value is NULL.  The result is
      guaranteed to be adequately aligned for any type of data,
      just as for malloc.
     

    

   

   

    
PQresultMemorySizePQresultMemorySize

    

     

      Retrieves the number of bytes allocated for
      a PGresult object.

size_t PQresultMemorySize(const PGresult *res);

     

     

      This value is the sum of all malloc requests
      associated with the PGresult object, that is,
      all the memory that will be freed by .
      This information can be useful for managing memory consumption.
     

    

   

   

    
PQlibVersionPQlibVersionPQserverVersion

    

     

      Return the version of libpq that is being used.

int PQlibVersion(void);

     

     

      The result of this function can be used to determine, at
      run time, whether specific functionality is available in the currently
      loaded version of libpq. The function can be used, for example,
      to determine which connection options are available in
      .
     

     

      The result is formed by multiplying the library's major version
      number by 10000 and adding the minor version number.  For example,
      version 10.1 will be returned as 100001, and version 11.0 will be
      returned as 110000.
     

     

      Prior to major version 10, PostgreSQL used
      three-part version numbers in which the first two parts together
      represented the major version.  For those
      versions,  uses two digits for each
      part; for example version 9.1.5 will be returned as 90105, and
      version 9.2.0 will be returned as 90200.
     

     

      Therefore, for purposes of determining feature compatibility,
      applications should divide the result of 
      by 100 not 10000 to determine a logical major version number.
      In all release series, only the last two digits differ between
      minor releases (bug-fix releases).
     

     
      

       This function appeared in PostgreSQL version 9.1, so
       it cannot be used to detect required functionality in earlier
       versions, since calling it will create a link dependency
       on version 9.1 or later.
      

     
    

   

   

    
PQgetCurrentTimeUSecPQgetCurrentTimeUSec

    

     

      Retrieves the current time, expressed as the number of microseconds
      since the Unix epoch (that is, time_t times 1 million).

pg_usec_time_t PQgetCurrentTimeUSec(void);

     

     

      This is primarily useful for calculating timeout values to use with
      .
     

    

   

  

 

 

  
# Notice Processing

  
   notice processing
   in libpq
  

  

   Notice and warning messages generated by the server are not returned
   by the query execution functions, since they do not imply failure of
   the query.  Instead they are passed to a notice handling function, and
   execution continues normally after the handler returns.  The default
   notice handling function prints the message on
   stderr, but the application can override this
   behavior by supplying its own handling function.
  

  

   For historical reasons, there are two levels of notice handling, called
   the notice receiver and notice processor.  The default behavior is for
   the notice receiver to format the notice and pass a string to the notice
   processor for printing.  However, an application that chooses to provide
   its own notice receiver will typically ignore the notice processor
   layer and just do all the work in the notice receiver.
  

  

   The function PQsetNoticeReceiver
   notice receiver
   PQsetNoticeReceiver sets or
   examines the current notice receiver for a connection object.
   Similarly, PQsetNoticeProcessor
   notice processor
   PQsetNoticeProcessor sets or
   examines the current notice processor.

typedef void (*PQnoticeReceiver) (void *arg, const PGresult *res);

PQnoticeReceiver
PQsetNoticeReceiver(PGconn *conn,
                    PQnoticeReceiver proc,
                    void *arg);

typedef void (*PQnoticeProcessor) (void *arg, const char *message);

PQnoticeProcessor
PQsetNoticeProcessor(PGconn *conn,
                     PQnoticeProcessor proc,
                     void *arg);

   Each of these functions returns the previous notice receiver or
   processor function pointer, and sets the new value.  If you supply a
   null function pointer, no action is taken, but the current pointer is
   returned.
  

  

   When a notice or warning message is received from the server, or
   generated internally by libpq, the notice
   receiver function is called.  It is passed the message in the form of
   a PGRES_NONFATAL_ERROR
   PGresult.  (This allows the receiver to extract
   individual fields using , or obtain a
   complete preformatted message using 
   or .)  The same
   void pointer passed to PQsetNoticeReceiver is also
   passed.  (This pointer can be used to access application-specific state
   if needed.)
  

  

   The default notice receiver simply extracts the message (using
   ) and passes it to the notice
   processor.
  

  

   The notice processor is responsible for handling a notice or warning
   message given in text form.  It is passed the string text of the message
   (including a trailing newline), plus a void pointer that is the same
   one passed to PQsetNoticeProcessor.  (This pointer
   can be used to access application-specific state if needed.)
  

  

   The default notice processor is simply:

```
static void
defaultNoticeProcessor(void *arg, const char *message)
{
    fprintf(stderr, "%s", message);
}
```

  

  

   Once you have set a notice receiver or processor, you should expect
   that that function could be called as long as either the
   PGconn object or PGresult objects made
   from it exist.  At creation of a PGresult, the
   PGconn's current notice handling pointers are copied
   into the PGresult for possible use by functions like
   .
  

 

 

  
# Event System

  

   libpq's event system is designed to notify
   registered event handlers about interesting
   libpq events, such as the creation or
   destruction of PGconn and
   PGresult objects.  A principal use case is that
   this allows applications to associate their own data with a
   PGconn or PGresult
   and ensure that that data is freed at an appropriate time.
  

  

   Each registered event handler is associated with two pieces of data,
   known to libpq only as opaque void *
   pointers.  There is a pass-through pointer that is provided
   by the application when the event handler is registered with a
   PGconn.  The pass-through pointer never changes for the
   life of the PGconn and all PGresults
   generated from it; so if used, it must point to long-lived data.
   In addition there is an instance data pointer, which starts
   out NULL in every PGconn and PGresult.
   This pointer can be manipulated using the
   ,
   ,
    and
    functions.  Note that
   unlike the pass-through pointer, instance data of a PGconn
   is not automatically inherited by PGresults created from
   it.  libpq does not know what pass-through
   and instance data pointers point to (if anything) and will never attempt
   to free them &mdash; that is the responsibility of the event handler.
  

  

   
# Event Types

   

    The enum PGEventId names the types of events handled by
    the event system.  All its values have names beginning with
    PGEVT.  For each event type, there is a corresponding
    event info structure that carries the parameters passed to the event
    handlers.  The event types are:
   

   

    

     
PGEVT_REGISTER

     

      

       The register event occurs when 
       is called.  It is the ideal time to initialize any
       instanceData an event procedure may need.  Only one
       register event will be fired per event handler per connection.  If the
       event procedure fails (returns zero), the registration is canceled.

typedef struct
{
    PGconn *conn;
} PGEventRegister;

       When a PGEVT_REGISTER event is received, the
       
evtInfo
 pointer should be cast to a
       PGEventRegister *.  This structure contains a
       PGconn that should be in the
       CONNECTION_OK status; guaranteed if one calls
        right after obtaining a good
       PGconn.  When returning a failure code, all
       cleanup must be performed as no PGEVT_CONNDESTROY
       event will be sent.
      

     

    

    

     
PGEVT_CONNRESET

     

      

       The connection reset event is fired on completion of
        or PQresetPoll.  In
       both cases, the event is only fired if the reset was successful.
       The return value of the event procedure is ignored
       in PostgreSQL v15 and later.
       With earlier versions, however, it's important to return success
       (nonzero) or the connection will be aborted.

typedef struct
{
    PGconn *conn;
} PGEventConnReset;

       When a PGEVT_CONNRESET event is received, the
       
evtInfo
 pointer should be cast to a
       PGEventConnReset *.  Although the contained
       PGconn was just reset, all event data remains
       unchanged.  This event should be used to reset/reload/requery any
       associated instanceData.  Note that even if the
       event procedure fails to process PGEVT_CONNRESET, it will
       still receive a PGEVT_CONNDESTROY event when the connection
       is closed.
      

     

    

    

     
PGEVT_CONNDESTROY

     

      

       The connection destroy event is fired in response to
       .  It is the event procedure's
       responsibility to properly clean up its event data as libpq has no
       ability to manage this memory.  Failure to clean up will lead
       to memory leaks.

typedef struct
{
    PGconn *conn;
} PGEventConnDestroy;

       When a PGEVT_CONNDESTROY event is received, the
       
evtInfo
 pointer should be cast to a
       PGEventConnDestroy *.  This event is fired
       prior to  performing any other cleanup.
       The return value of the event procedure is ignored since there is no
       way of indicating a failure from .  Also,
       an event procedure failure should not abort the process of cleaning up
       unwanted memory.
      

     

    

    

     
PGEVT_RESULTCREATE

     

      

       The result creation event is fired in response to any query execution
       function that generates a result, including
       .  This event will only be fired after
       the result has been created successfully.

typedef struct
{
    PGconn *conn;
    PGresult *result;
} PGEventResultCreate;

       When a PGEVT_RESULTCREATE event is received, the
       
evtInfo
 pointer should be cast to a
       PGEventResultCreate *.  The
       
conn
 is the connection used to generate the
       result.  This is the ideal place to initialize any
       instanceData that needs to be associated with the
       result.  If an event procedure fails (returns zero), that event
       procedure will be ignored for the remaining lifetime of the result;
       that is, it will not receive PGEVT_RESULTCOPY
       or PGEVT_RESULTDESTROY events for this result or
       results copied from it.
      

     

    

    

     
PGEVT_RESULTCOPY

     

      

       The result copy event is fired in response to
       .  This event will only be fired after
       the copy is complete.  Only event procedures that have
       successfully handled the PGEVT_RESULTCREATE
       or PGEVT_RESULTCOPY event for the source result
       will receive PGEVT_RESULTCOPY events.

typedef struct
{
    const PGresult *src;
    PGresult *dest;
} PGEventResultCopy;

       When a PGEVT_RESULTCOPY event is received, the
       
evtInfo
 pointer should be cast to a
       PGEventResultCopy *.  The
       
src
 result is what was copied while the
       
dest
 result is the copy destination.  This event
       can be used to provide a deep copy of instanceData,
       since PQcopyResult cannot do that.  If an event
       procedure fails (returns zero), that event procedure will be
       ignored for the remaining lifetime of the new result; that is, it
       will not receive PGEVT_RESULTCOPY
       or PGEVT_RESULTDESTROY events for that result or
       results copied from it.
      

     

    

    

     
PGEVT_RESULTDESTROY

     

      

       The result destroy event is fired in response to a
       .  It is the event procedure's
       responsibility to properly clean up its event data as libpq has no
       ability to manage this memory.  Failure to clean up will lead
       to memory leaks.

typedef struct
{
    PGresult *result;
} PGEventResultDestroy;

       When a PGEVT_RESULTDESTROY event is received, the
       
evtInfo
 pointer should be cast to a
       PGEventResultDestroy *.  This event is fired
       prior to  performing any other cleanup.
       The return value of the event procedure is ignored since there is no
       way of indicating a failure from .  Also,
       an event procedure failure should not abort the process of cleaning up
       unwanted memory.
      

     

    

   

  

  

   
# Event Callback Procedure

   

    

     
PGEventProcPGEventProc

     

      

       PGEventProc is a typedef for a pointer to an
       event procedure, that is, the user callback function that receives
       events from libpq.  The signature of an event procedure must be

int eventproc(PGEventId evtId, void *evtInfo, void *passThrough)

       The 
evtId
 parameter indicates which
       PGEVT event occurred.  The
       
evtInfo
 pointer must be cast to the appropriate
       structure type to obtain further information about the event.
       The 
passThrough
 parameter is the pointer
       provided to  when the event
       procedure was registered.  The function should return a non-zero value
       if it succeeds and zero if it fails.
      

      

       A particular event procedure can be registered only once in any
       PGconn.  This is because the address of the procedure
       is used as a lookup key to identify the associated instance data.
      

      
       

        On Windows, functions can have two different addresses: one visible
        from outside a DLL and another visible from inside the DLL.  One
        should be careful that only one of these addresses is used with
        libpq's event-procedure functions, else confusion will
        result.  The simplest rule for writing code that will work is to
        ensure that event procedures are declared static.  If the
        procedure's address must be available outside its own source file,
        expose a separate function to return the address.
       

      
     

    

   

  

  

   
# Event Support Functions

    

    

     
PQregisterEventProcPQregisterEventProc

     

      

       Registers an event callback procedure with libpq.

int PQregisterEventProc(PGconn *conn, PGEventProc proc,
                        const char *name, void *passThrough);

      

      

       An event procedure must be registered once on each
       PGconn you want to receive events about.  There is no
       limit, other than memory, on the number of event procedures that
       can be registered with a connection.  The function returns a non-zero
       value if it succeeds and zero if it fails.
      

      

       The 
proc
 argument will be called when a libpq
       event is fired.  Its memory address is also used to lookup
       instanceData.  The 
name

       argument is used to refer to the event procedure in error messages.
       This value cannot be NULL or a zero-length string.  The name string is
       copied into the PGconn, so what is passed need not be
       long-lived.  The 
passThrough
 pointer is passed
       to the 
proc
 whenever an event occurs. This
       argument can be NULL.
      

     

    

    

     
PQsetInstanceDataPQsetInstanceData

     

      

       Sets the connection 
conn
's instanceData
       for procedure 
proc
 to 
data
.  This
       returns non-zero for success and zero for failure.  (Failure is
       only possible if 
proc
 has not been properly
       registered in 
conn
.)

int PQsetInstanceData(PGconn *conn, PGEventProc proc, void *data);

      

     

    

    

     
PQinstanceDataPQinstanceData

     

      

       Returns the
       connection 
conn
's instanceData
       associated with procedure 
proc
,
       or NULL if there is none.

void *PQinstanceData(const PGconn *conn, PGEventProc proc);

      

     

    

    

     
PQresultSetInstanceDataPQresultSetInstanceData

     

      

       Sets the result's instanceData
       for 
proc
 to 
data
.  This returns
       non-zero for success and zero for failure.  (Failure is only
       possible if 
proc
 has not been properly registered
       in the result.)

int PQresultSetInstanceData(PGresult *res, PGEventProc proc, void *data);

      

      

       Beware that any storage represented by 
data

       will not be accounted for by ,
       unless it is allocated using .
       (Doing so is recommendable because it eliminates the need to free
       such storage explicitly when the result is destroyed.)
      

     

    

    

     
PQresultInstanceDataPQresultInstanceData

     

      

       Returns the result's instanceData associated with 
proc
, or NULL
       if there is none.

void *PQresultInstanceData(const PGresult *res, PGEventProc proc);

      

     

    

   

  

  

   
# Event Example

   

    Here is a skeleton example of managing private data associated with
    libpq connections and results.
   

```

/* The instanceData */
typedef struct
{
    int n;
    char *str;
} mydata;

/* PGEventProc */
static int myEventProc(PGEventId evtId, void *evtInfo, void *passThrough);

int
main(void)
{
    mydata *data;
    PGresult *res;
    PGconn *conn =
        PQconnectdb("dbname=postgres options=-csearch_path=");

    if (PQstatus(conn) != CONNECTION_OK)
    {
        /* PQerrorMessage's result includes a trailing newline */
        fprintf(stderr, "%s", PQerrorMessage(conn));
        PQfinish(conn);
        return 1;
    }

    /* called once on any connection that should receive events.
     * Sends a PGEVT_REGISTER to myEventProc.
     */
    if (!PQregisterEventProc(conn, myEventProc, "mydata_proc", NULL))
    {
        fprintf(stderr, "Cannot register PGEventProc\n");
        PQfinish(conn);
        return 1;
    }

    /* conn instanceData is available */
    data = PQinstanceData(conn, myEventProc);

    /* Sends a PGEVT_RESULTCREATE to myEventProc */
    res = PQexec(conn, "SELECT 1 + 1");

    /* result instanceData is available */
    data = PQresultInstanceData(res, myEventProc);

    /* If PG_COPYRES_EVENTS is used, sends a PGEVT_RESULTCOPY to myEventProc */
    res_copy = PQcopyResult(res, PG_COPYRES_TUPLES | PG_COPYRES_EVENTS);

    /* result instanceData is available if PG_COPYRES_EVENTS was
     * used during the PQcopyResult call.
     */
    data = PQresultInstanceData(res_copy, myEventProc);

    /* Both clears send a PGEVT_RESULTDESTROY to myEventProc */
    PQclear(res);
    PQclear(res_copy);

    /* Sends a PGEVT_CONNDESTROY to myEventProc */
    PQfinish(conn);

    return 0;
}

static int
myEventProc(PGEventId evtId, void *evtInfo, void *passThrough)
{
    switch (evtId)
    {
        case PGEVT_REGISTER:
        {
            PGEventRegister *e = (PGEventRegister *)evtInfo;
            mydata *data = get_mydata(e->conn);

            /* associate app specific data with connection */
            PQsetInstanceData(e->conn, myEventProc, data);
            break;
        }

        case PGEVT_CONNRESET:
        {
            PGEventConnReset *e = (PGEventConnReset *)evtInfo;
            mydata *data = PQinstanceData(e->conn, myEventProc);

            if (data)
              memset(data, 0, sizeof(mydata));
            break;
        }

        case PGEVT_CONNDESTROY:
        {
            PGEventConnDestroy *e = (PGEventConnDestroy *)evtInfo;
            mydata *data = PQinstanceData(e->conn, myEventProc);

            /* free instance data because the conn is being destroyed */
            if (data)
              free_mydata(data);
            break;
        }

        case PGEVT_RESULTCREATE:
        {
            PGEventResultCreate *e = (PGEventResultCreate *)evtInfo;
            mydata *conn_data = PQinstanceData(e->conn, myEventProc);
            mydata *res_data = dup_mydata(conn_data);

            /* associate app specific data with result (copy it from conn) */
            PQresultSetInstanceData(e->result, myEventProc, res_data);
            break;
        }

        case PGEVT_RESULTCOPY:
        {
            PGEventResultCopy *e = (PGEventResultCopy *)evtInfo;
            mydata *src_data = PQresultInstanceData(e->src, myEventProc);
            mydata *dest_data = dup_mydata(src_data);

            /* associate app specific data with result (copy it from a result) */
            PQresultSetInstanceData(e->dest, myEventProc, dest_data);
            break;
        }

        case PGEVT_RESULTDESTROY:
        {
            PGEventResultDestroy *e = (PGEventResultDestroy *)evtInfo;
            mydata *data = PQresultInstanceData(e->result, myEventProc);

            /* free instance data because the result is being destroyed */
            if (data)
              free_mydata(data);
            break;
        }

        /* unknown event ID, just return true. */
        default:
            break;
    }

    return true; /* event processing succeeded */
}
]]>
```

  

 

 

  
# Environment Variables

  
   environment variable
  

  

   The following environment variables can be used to select default
   connection parameter values, which will be used by
   ,  and
    if no value is directly specified by the calling
   code.  These are useful to avoid hard-coding database connection
   information into simple client applications, for example.

   

    

     

      
       PGHOST
      
      PGHOST behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLNEGOTIATION
      
      PGSSLNEGOTIATION behaves the same as the  connection parameter.
     

    

    

     

      
       PGHOSTADDR
      
      PGHOSTADDR behaves the same as the  connection parameter.
      This can be set instead of or in addition to PGHOST
      to avoid DNS lookup overhead.
     

    

    

     

      
       PGPORT
      
      PGPORT behaves the same as the  connection parameter.
     

    

    

     

      
       PGDATABASE
      
      PGDATABASE behaves the same as the  connection parameter.
      

    

    

     

      
       PGUSER
      
      PGUSER behaves the same as the  connection parameter.
     

    

    

     

      
       PGPASSWORD
      
      PGPASSWORD behaves the same as the  connection parameter.
      Use of this environment variable
      is not recommended for security reasons, as some operating systems
      allow non-root users to see process environment variables via
      ps; instead consider using a password file
      (see ).
     

    

    

     

      
       PGPASSFILE
      
      PGPASSFILE behaves the same as the  connection parameter.
     

    

    

     

      
       PGREQUIREAUTH
      
      PGREQUIREAUTH behaves the same as the  connection parameter.
     

    

    

     

      
       PGCHANNELBINDING
      
      PGCHANNELBINDING behaves the same as the  connection parameter.
     

    

    

     

      
       PGSERVICE
      
      PGSERVICE behaves the same as the  connection parameter.
     

    

    

     

      
       PGSERVICEFILE
      
      PGSERVICEFILE behaves the same as the
       connection parameter.
     

    

    

     

      
       PGOPTIONS
      
      PGOPTIONS behaves the same as the  connection parameter.
     

    

    

     

      
       PGAPPNAME
      
      PGAPPNAME behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLMODE
      
      PGSSLMODE behaves the same as the  connection parameter.
     

    

    

     

      
       PGREQUIRESSL
      
      PGREQUIRESSL behaves the same as the  connection parameter.
      This environment variable is deprecated in favor of the
      PGSSLMODE variable; setting both variables suppresses the
      effect of this one.
     

    

    

     

      
       PGSSLCOMPRESSION
      
      PGSSLCOMPRESSION behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLCERT
      
      PGSSLCERT behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLKEY
      
      PGSSLKEY behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLCERTMODE
      
      PGSSLCERTMODE behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLROOTCERT
      
      PGSSLROOTCERT  behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLCRL
      
      PGSSLCRL  behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLCRLDIR
      
      PGSSLCRLDIR behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLSNI
      
      PGSSLSNI  behaves the same as the  connection parameter.
     

    

    

     

      
       PGREQUIREPEER
      
      PGREQUIREPEER behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLMINPROTOCOLVERSION
      
      PGSSLMINPROTOCOLVERSION behaves the same as the  connection parameter.
     

    

    

     

      
       PGSSLMAXPROTOCOLVERSION
      
      PGSSLMAXPROTOCOLVERSION behaves the same as the  connection parameter.
     

    

    

     

      
       PGGSSENCMODE
      
      PGGSSENCMODE behaves the same as the  connection parameter.
     

    

    

     

      
       PGKRBSRVNAME
      
      PGKRBSRVNAME  behaves the same as the  connection parameter.
     

    

    

     

      
       PGGSSLIB
      
      PGGSSLIB behaves the same as the  connection parameter.
     

    

    

     

      
       PGGSSDELEGATION
      
      PGGSSDELEGATION behaves the same as the  connection parameter.
     

    

    

     

      
       PGCONNECT_TIMEOUT
      
      PGCONNECT_TIMEOUT  behaves the same as the  connection parameter.
     

    

    

     

      
       PGCLIENTENCODING
      
      PGCLIENTENCODING behaves the same as the  connection parameter.
     

    

    

     

      
       PGTARGETSESSIONATTRS
      
      PGTARGETSESSIONATTRS behaves the same as the  connection parameter.
     

    

    

     

      
       PGLOADBALANCEHOSTS
      
      PGLOADBALANCEHOSTS behaves the same as the  connection parameter.
     

    

    

     

      
       PGMINPROTOCOLVERSION
      
      PGMINPROTOCOLVERSION behaves the same as the  connection parameter.
     

    

    

     

      
       PGMAXPROTOCOLVERSION
      
      PGMAXPROTOCOLVERSION behaves the same as the  connection parameter.
     

    

    

     

      
       PGOAUTHCAFILE
      
      PGOAUTHCAFILE behaves the same as the  connection parameter.
     

    

   

  

  

   The following environment variables can be used to specify default
   behavior for each PostgreSQL session.  (See
   also the 
   and 
   commands for ways to set default behavior on a per-user or per-database
   basis.)

   

    

     

      
       PGDATESTYLE
      
      PGDATESTYLE sets the default style of date/time
      representation.  (Equivalent to SET datestyle TO
      ....)
     

    

    

     

      
       PGTZ
      
      PGTZ sets the default time zone.  (Equivalent to
      SET timezone TO ....)
     

    

    

     

      
       PGGEQO
      
      PGGEQO sets the default mode for the genetic query
      optimizer.  (Equivalent to SET geqo TO ....)
     

    

   

   Refer to the SQL command 
   for information on correct values for these
   environment variables.
  

  

   The following environment variables determine internal behavior of
   libpq; they override compiled-in defaults.

   

    

     

      
       PGSYSCONFDIR
      
      PGSYSCONFDIR sets the directory containing the
      pg_service.conf file and in a future version
      possibly other system-wide configuration files.
     

    

    

     

      
       PGLOCALEDIR
      
      PGLOCALEDIR sets the directory containing the
      locale files for message localization.
     

    

   

  

 

 

  
# The Password File

  
   password file
  
  
   .pgpass
  

  

   The file .pgpass in a user's home directory can
   contain passwords to
   be used if the connection requires a password (and no password has been
   specified otherwise). On Unix systems, the directory can be specified by
   the HOME environment variable, or if undefined, the home
   directory of the effective user.  On Microsoft Windows the file is named
   %APPDATA%\postgresql\pgpass.conf (where
   %APPDATA% refers to the Application Data subdirectory in
   the user's profile).
   Alternatively, the password file to use can be specified
   using the connection parameter 
   or the environment variable PGPASSFILE.
  

  

   This file should contain lines of the following format:

hostname:port:database:username:password

   (You can add a reminder comment to the file by copying the line above and
   preceding it with #.)
   Each of the first four fields can be a literal value, or
   *, which matches anything.  The password field from
   the first line that matches the current connection parameters will be
   used.  (Therefore, put more-specific entries first when you are using
   wildcards.) If an entry needs to contain : or
   \, escape this character with \.
   The host name field is matched to the host connection
   parameter if that is specified, otherwise to
   the hostaddr parameter if that is specified; if neither
   are given then the host name localhost is searched for.
   The host name localhost is also searched for when
   the connection is a Unix-domain socket connection and
   the host parameter
   matches libpq's default socket directory path.
   In a standby server, a database field of replication
   matches streaming replication connections made to the primary server.
   The database field is of limited usefulness otherwise, because users have
   the same password for all databases in the same cluster.
  

  

   On Unix systems, the permissions on a password file must
   disallow any access to world or group; achieve this by a command such as
   chmod 0600 ~/.pgpass.  If the permissions are less
   strict than this, the file will be ignored.  On Microsoft Windows, it
   is assumed that the file is stored in a directory that is secure, so
   no special permissions check is made.
  

 

 

  
# The Connection Service File

  
   connection service file
  
  
   pg_service.conf
  
  
   .pg_service.conf
  

  

   The connection service file allows libpq connection parameters to be
   associated with a single service name. That service name can then be
   specified using the service key word
   in a libpq connection string, and the associated settings will be
   used. This allows connection parameters to be modified without requiring
   a recompile of the libpq-using application. The service name can also be
   specified using the PGSERVICE environment variable.
  

  

   Service names can be defined in either a per-user service file or a
   system-wide file.  If the same service name exists in both the user
   and the system file, the user file takes precedence.
   By default, the per-user service file is named
   ~/.pg_service.conf.
   On Microsoft Windows, it is named
   %APPDATA%\postgresql\.pg_service.conf (where
   %APPDATA% refers to the Application Data subdirectory
   in the user's profile).  A different file name can be specified using the
   servicefile key word in a libpq connection string or by
   setting the environment variable PGSERVICEFILE.
   The system-wide file is named pg_service.conf.
   By default it is sought in the etc directory
   of the PostgreSQL installation
   (use pg_config --sysconfdir to identify this
   directory precisely).  Another directory, but not a different file
   name, can be specified by setting the environment variable
   PGSYSCONFDIR.
  

  

   Either service file uses an INI file format where the section
   name is the service name and the parameters are connection
   parameters; see  for a list.  For
   example:

```
# comment
[mydb]
host=somehost
port=5433
user=admin
```

   An example file is provided in
   the PostgreSQL installation at
   share/pg_service.conf.sample.
  

  

   Connection parameters obtained from a service file are combined with
   parameters obtained from other sources.  A service file setting
   overrides the corresponding environment variable, and in turn can be
   overridden by a value given directly in the connection string.
   For example, using the above service file, a connection string
   service=mydb port=5434 will use
   host somehost, port 5434,
   user admin, and other parameters as set by
   environment variables or built-in defaults.
  

 

 

  
# LDAP Lookup of Connection Parameters

  
   LDAP connection parameter lookup
  

  

   If libpq has been compiled with LDAP support (option
   --with-ldap for configure)
   it is possible to retrieve connection options like host
   or dbname via LDAP from a central server.
   The advantage is that if the connection parameters for a database change,
   the connection information doesn't have to be updated on all client machines.
  

  

   LDAP connection parameter lookup uses the connection service file
   pg_service.conf (see ).  A line in a
   pg_service.conf stanza that starts with
   ldap:// will be recognized as an LDAP URL and an
   LDAP query will be performed. The result must be a list of
   keyword = value pairs which will be used to set
   connection options.  The URL must conform to
   RFC 1959
   and be of the form

ldap://[hostname[:port]]/search_base?attribute?search_scope?filter

   where hostname defaults to
   localhost and port
   defaults to 389.
  

  

   Processing of pg_service.conf is terminated after
   a successful LDAP lookup, but is continued if the LDAP server cannot
   be contacted.  This is to provide a fallback with further LDAP URL
   lines that point to different LDAP servers, classical keyword
   = value pairs, or default connection options.  If you would
   rather get an error message in this case, add a syntactically incorrect
   line after the LDAP URL.
  

  

   A sample LDAP entry that has been created with the LDIF file

```
version:1
dn:cn=mydatabase,dc=mycompany,dc=com
changetype:add
objectclass:top
objectclass:device
cn:mydatabase
description:host=dbserver.mycompany.com
description:port=5439
description:dbname=mydb
description:user=mydb_user
description:sslmode=require
```

   might be queried with the following LDAP URL:

```
ldap://ldap.mycompany.com/dc=mycompany,dc=com?description?one?(cn=mydatabase)
```

  

  

   You can also mix regular service file entries with LDAP lookups.
   A complete example for a stanza in pg_service.conf
   would be:

```
# only host and port are stored in LDAP, specify dbname and user explicitly
[customerdb]
dbname=customer
user=appuser
ldap://ldap.acme.com/cn=dbserver,cn=hosts?pgconnectinfo?base?(objectclass=*)
```

  

 

 

  
# SSL Support

  
   SSL
   TLS
  

  

   PostgreSQL has native support for using SSL
   connections to encrypt client/server communications using
   TLS protocols for increased security.
   See  for details about the server-side
   SSL functionality.
  

  

   libpq reads the system-wide
   OpenSSL configuration file. By default, this
   file is named openssl.cnf and is located in the
   directory reported by openssl version -d.  This default
   can be overridden by setting environment variable
   OPENSSL_CONF to the name of the desired configuration
   file.
  

 

  
# Client Verification of Server Certificates

  

   By default, PostgreSQL will not perform any verification of
   the server certificate. This means that it is possible to spoof the server
   identity (for example by modifying a DNS record or by taking over the server
   IP address) without the client knowing. In order to prevent spoofing,
   the client must be able to verify the server's identity via a chain of
   trust.  A chain of trust is established by placing a root (self-signed)
   certificate authority (CA) certificate on one
   computer and a leaf certificate signed by the
   root certificate on another computer.  It is also possible to use an
   intermediate certificate which is signed by the root
   certificate and signs leaf certificates.
  

  

   To allow the client to verify the identity of the server, place a root
   certificate on the client and a leaf certificate signed by the root
   certificate on the server.  To allow the server to verify the identity
   of the client, place a root certificate on the server and a leaf
   certificate signed by the root certificate on the client.  One or more
   intermediate certificates (usually stored with the leaf certificate)
   can also be used to link the leaf certificate to the root certificate.
  

  

   Once a chain of trust has been established, there are two ways for
   the client to validate the leaf certificate sent by the server.
   If the parameter sslmode is set to verify-ca,
   libpq will verify that the server is trustworthy by checking the
   certificate chain up to the root certificate stored on the client.
   If sslmode is set to verify-full,
   libpq will also verify that the server host
   name matches the name stored in the server certificate. The
   SSL connection will fail if the server certificate cannot be
   verified. verify-full is recommended in most
   security-sensitive environments.
  

  

   In verify-full mode, the host name is matched against the
   certificate's Subject Alternative Name attribute(s) (SAN), or against the
   Common Name attribute if no SAN of type dNSName is
   present.  If the certificate's name attribute starts with an asterisk
   (*), the asterisk will be treated as
   a wildcard, which will match all characters except a dot
   (.). This means the certificate will not match subdomains.
   If the connection is made using an IP address instead of a host name, the
   IP address will be matched (without doing any DNS lookups) against SANs of
   type iPAddress or dNSName.  If no
   iPAddress SAN is present and no
   matching dNSName SAN is present, the host IP address is
   matched against the Common Name attribute.
  

  
   

    For backward compatibility with earlier versions of PostgreSQL, the host
    IP address is verified in a manner different
    from RFC 6125.
    The host IP address is always matched against dNSName
    SANs as well as iPAddress SANs, and can be matched
    against the Common Name attribute if no relevant SANs exist.
   

  

  

   To allow server certificate verification, one or more root certificates
   must be placed in the file ~/.postgresql/root.crt
   in the user's home directory.  (On Microsoft Windows the file is named
   %APPDATA%\postgresql\root.crt.)  Intermediate
   certificates should also be added to the file if they are needed to link
   the certificate chain sent by the server to the root certificates
   stored on the client.
  

  

   Certificate Revocation List (CRL) entries are also checked
   if the file ~/.postgresql/root.crl exists
   (%APPDATA%\postgresql\root.crl on Microsoft
   Windows).
  

  

   The location of the root certificate file and the CRL can be changed by
   setting
   the connection parameters sslrootcert and sslcrl
   or the environment variables PGSSLROOTCERT and PGSSLCRL.
   sslcrldir or the environment variable PGSSLCRLDIR
   can also be used to specify a directory containing CRL files.
  

  
   

    For backwards compatibility with earlier versions of PostgreSQL, if a
    root CA file exists, the behavior of
    sslmode=require will be the same
    as that of verify-ca, meaning the server certificate
    is validated against the CA. Relying on this behavior is discouraged,
    and applications that need certificate validation should always use
    verify-ca or verify-full.
   

  
 

 

  
# Client Certificates

  

   If the server attempts to verify the identity of the
   client by requesting the client's leaf certificate,
   libpq will send the certificate(s) stored in
   file ~/.postgresql/postgresql.crt in the user's home
   directory.  The certificates must chain to the root certificate trusted
   by the server.  A matching
   private key file ~/.postgresql/postgresql.key must also
   be present.
   On Microsoft Windows these files are named
   %APPDATA%\postgresql\postgresql.crt and
   %APPDATA%\postgresql\postgresql.key.
   The location of the certificate and key files can be overridden by the
   connection parameters sslcert
   and sslkey, or by the
   environment variables PGSSLCERT and PGSSLKEY.
  

  

   On Unix systems, the permissions on the private key file must disallow
   any access to world or group; achieve this by a command such as
   chmod 0600 ~/.postgresql/postgresql.key.
   Alternatively, the file can be owned by root and have group read access
   (that is, 0640 permissions).  That setup is intended
   for installations where certificate and key files are managed by the
   operating system.  The user of libpq should
   then be made a member of the group that has access to those certificate
   and key files.  (On Microsoft Windows, there is no file permissions
   check, since the %APPDATA%\postgresql directory is
   presumed secure.)
  

  

   The first certificate in postgresql.crt must be the
   client's certificate because it must match the client's private key.
   Intermediate certificates can be optionally appended
   to the file &mdash; doing so avoids requiring storage of intermediate
   certificates on the server ().
  

  

   The certificate and key may be in PEM or ASN.1 DER format.
  

  

   The key may be
   stored in cleartext or encrypted with a passphrase using any algorithm
   supported by OpenSSL, like AES-128. If the key
   is stored encrypted, then the passphrase may be provided in the
    connection option. If an
   encrypted key is supplied and the sslpassword option
   is absent or blank, a password will be prompted for interactively by
   OpenSSL with a
   Enter PEM pass phrase: prompt if a TTY is available.
   Applications can override the client certificate prompt and the handling
   of the sslpassword parameter by supplying their own
   key password callback; see
   .
  

  

   For instructions on creating certificates, see .
  

 

 

  
# Protection Provided in Different Modes

  

   The different values for the sslmode parameter provide different
   levels of protection. SSL can provide
   protection against three types of attacks:

   

    

     
Eavesdropping

     

      
If a third party can examine the network traffic between the
       client and the server, it can read both connection information (including
       the user name and password) and the data that is passed. SSL
       uses encryption to prevent this.
      

     

    

    

     
Man-in-the-middle (MITM)

     

      
If a third party can modify the data while passing between the
       client and server, it can pretend to be the server and therefore see and
       modify data even if it is encrypted. The third party can then
       forward the connection information and data to the original server,
       making it impossible to detect this attack. Common vectors to do this
       include DNS poisoning and address hijacking, whereby the client is directed
       to a different server than intended. There are also several other
       attack methods that can accomplish this. SSL uses certificate
       verification to prevent this, by authenticating the server to the client.
      

     

    

    

     
Impersonation

     

      
If a third party can pretend to be an authorized client, it can
       simply access data it should not have access to. Typically this can
       happen through insecure password management. SSL uses
       client certificates to prevent this, by making sure that only holders
       of valid certificates can access the server.
      

     

    

   

  

  

   For a connection to be known SSL-secured, SSL usage must be configured
   on both the client and the server before the connection
   is made. If it is only configured on the server, the client may end up
   sending sensitive information (e.g., passwords) before
   it knows that the server requires high security. In libpq, secure
   connections can be ensured
   by setting the sslmode parameter to verify-full or
   verify-ca, and providing the system with a root certificate to
   verify against. This is analogous to using an https
   URL for encrypted web browsing.
  

  

   Once the server has been authenticated, the client can pass sensitive data.
   This means that up until this point, the client does not need to know if
   certificates will be used for authentication, making it safe to specify that
   only in the server configuration.
  

  

   All SSL options carry overhead in the form of encryption and
   key-exchange, so there is a trade-off that has to be made between performance
   and security. 
   illustrates the risks the different sslmode values
   protect against, and what statement they make about security and overhead.
  

  
   
# SSL Mode Descriptions

   
    
    
    
    
    
     
      sslmode
      Eavesdropping protection
      MITM protection
      Statement
     
    

    
     
      disable
      No
      No
      I don't care about security, and I don't want to pay the overhead
       of encryption.
      
     

     
      allow
      Maybe
      No
      I don't care about security, but I will pay the overhead of
       encryption if the server insists on it.
      
     

     
      prefer
      Maybe
      No
      I don't care about encryption, but I wish to pay the overhead of
       encryption if the server supports it.
      
     

     
      require
      Yes
      No
      I want my data to be encrypted, and I accept the overhead. I trust
       that the network will make sure I always connect to the server I want.
      
     

     
      verify-ca
      Yes
      Depends on CA policy
      I want my data encrypted, and I accept the overhead. I want to be
       sure that I connect to a server that I trust.
      
     

     
      verify-full
       Yes
       Yes
       I want my data encrypted, and I accept the overhead. I want to be
        sure that I connect to a server I trust, and that it's the one I
        specify.
       
      

    
   
  

  

   The difference between verify-ca and verify-full
   depends on the policy of the root CA. If a public
   CA is used, verify-ca allows connections to a server
   that somebody else may have registered with the CA.
   In this case, verify-full should always be used. If
   a local CA is used, or even a self-signed certificate, using
   verify-ca often provides enough protection.
  

  

   The default value for sslmode is prefer. As is shown
   in the table, this makes no sense from a security point of view, and it only
   promises performance overhead if possible. It is only provided as the default
   for backward compatibility, and is not recommended in secure deployments.
  

 

 

  
# SSL Client File Usage

  

    summarizes the files that are
   relevant to the SSL setup on the client.
  

  
   
# Libpq/Client SSL File Usage

   
    
     
      File
      Contents
      Effect
     
    

    

     
      ~/.postgresql/postgresql.crt
      client certificate
      sent to server
     

     
      ~/.postgresql/postgresql.key
      client private key
      proves client certificate sent by owner; does not indicate
      certificate owner is trustworthy
     

     
      ~/.postgresql/root.crt
      trusted certificate authorities
      checks that server certificate is signed by a trusted certificate
      authority
     

     
      ~/.postgresql/root.crl
      certificates revoked by certificate authorities
      server certificate must not be on this list
     

    
   
  
 

 

  
# SSL Library Initialization

  

   Applications which need to be compatible with older versions of
   PostgreSQL, using OpenSSL
   version 1.0.2 or older, need to initialize the SSL library before using it.
   Applications which initialize libssl and/or
   libcrypto libraries should call
    to tell libpq
   that the libssl and/or libcrypto libraries
   have been initialized by your application, so that
   libpq will not also initialize those libraries.
   However, this is unnecessary when using OpenSSL
   version 1.1.0 or later, as duplicate initializations are no longer problematic.
  

  

   Refer to the documentation for the version of PostgreSQL
   that you are targeting for details on their use.
  

  

   

    

     
PQinitOpenSSLPQinitOpenSSL

     

      

       Allows applications to select which security libraries to initialize.

void PQinitOpenSSL(int do_ssl, int do_crypto);

      

      

       This function is deprecated and only present for backwards compatibility,
       it does nothing.
      

     

    

    

     
PQinitSSLPQinitSSL

      

       Allows applications to select which security libraries to initialize.

void PQinitSSL(int do_ssl);

      

      

       This function is equivalent to
       PQinitOpenSSL(do_ssl, do_ssl).
       This function is deprecated and only present for backwards compatibility,
       it does nothing.
      

      

        and 
       are maintained for backwards compatibility, but are no longer required
       since PostgreSQL 18.
        has been present since
       PostgreSQL 8.0, while 
       was added in PostgreSQL 8.4, so 
       might be preferable for applications that need to work with older
       versions of libpq.
      

     

    

   

  

 

 

 

  
# OAuth Support

  

   libpq implements support for the OAuth v2 Device Authorization client flow,
   documented in
   RFC 8628,
   as an optional module. See the 
   installation documentation for information on how to enable support
   for Device Authorization as a builtin flow.
  

  

   When support is enabled and the optional module installed, libpq
   will use the builtin flow by default if the server
   requests a bearer token during
   authentication. This flow can be utilized even if the system running the
   client application does not have a usable web browser, for example when
   running a client via SSH.
  

  

   The builtin flow will, by default, print a URL to visit and a user code to
   enter there:

```
$ psql 'dbname=postgres oauth_issuer=https://example.com oauth_client_id=...'
Visit https://example.com/device and enter the code: ABCD-EFGH
```

   (This prompt may be
   customized.)
   The user will then log into their OAuth provider, which will ask whether
   to allow libpq and the server to perform actions on their behalf. It is always
   a good idea to carefully review the URL and permissions displayed, to ensure
   they match expectations, before continuing. Permissions should not be given
   to untrusted third parties.
  

  

   Client applications may implement their own flows to customize interaction
   and integration with applications. See 
   for more information on how add a custom flow to libpq.
  

  

   For an OAuth client flow to be usable, the connection string must at minimum
   contain  and
   . (These settings are
   determined by your organization's OAuth provider.) The builtin flow
   additionally requires the OAuth authorization server to publish a device
   authorization endpoint.
  

  
   

    The builtin Device Authorization flow is not currently supported on Windows.
    Custom client flows may still be implemented.
   

  

  

   
# Authdata Hooks

   

    The behavior of the OAuth flow may be modified or replaced by a client using
    the following hook API:

    

     

      
PQsetAuthDataHookPQsetAuthDataHook

      

       

        Sets the PGauthDataHook, overriding
        libpq's handling of one or more aspects of
        its OAuth client flow.

void PQsetAuthDataHook(PQauthDataHook_type hook);

        If hook is NULL, the
        default handler will be reinstalled. Otherwise, the application passes
        a pointer to a callback function with the signature:

```
int hook_fn(PGauthData type, PGconn *conn, void *data);
```

        which libpq will call when an action is
        required of the application. type describes
        the request being made, conn is the
        connection handle being authenticated, and data
        points to request-specific metadata. The contents of this pointer are
        determined by type; see
         for the supported
        list.
       

       

        Hooks can be chained together to allow cooperative and/or fallback
        behavior. In general, a hook implementation should examine the incoming
        type (and, potentially, the request metadata
        and/or the settings for the particular conn
        in use) to decide whether or not to handle a specific piece of authdata.
        If not, it should delegate to the previous hook in the chain
        (retrievable via PQgetAuthDataHook).
       

       

        Success is indicated by returning an integer greater than zero.
        Returning a negative integer signals an error condition and abandons the
        connection attempt. (A zero value is reserved for the default
        implementation.)
       

      

     

     

      
PQgetAuthDataHookPQgetAuthDataHook

      

       

        Retrieves the current value of PGauthDataHook.

PQauthDataHook_type PQgetAuthDataHook(void);

        At initialization time (before the first call to
        PQsetAuthDataHook), this function will return
        PQdefaultAuthDataHook.
       

      

     

    

   

   

    
# Hook Types

    

     The following PGauthData types and their corresponding
     data structures are defined:

     

      

       

        PQAUTHDATA_PROMPT_OAUTH_DEVICE
        PQAUTHDATA_PROMPT_OAUTH_DEVICE
       

       

        
Available in PostgreSQL 18 and later.

        

         Replaces the default user prompt during the builtin device
         authorization client flow. data points to
         an instance of PGpromptOAuthDevice:

typedef struct _PGpromptOAuthDevice
{
    const char *verification_uri;   /* verification URI to visit */
    const char *user_code;          /* user code to enter */
    const char *verification_uri_complete;  /* optional combination of URI and
                                             * code, or NULL */
    int         expires_in;         /* seconds until user code expires */
} PGpromptOAuthDevice;

        

        

         The OAuth Device Authorization flow which
         can be included
         in libpq
         requires the end user to visit a URL with a browser, then enter a code
         which permits libpq to connect to the server
         on their behalf. The default prompt simply prints the
         verification_uri and user_code
         on standard error. Replacement implementations may display this
         information using any preferred method, for example with a GUI.
        

        

         This callback is only invoked during the builtin device
         authorization flow. If the application installs a
         custom OAuth
         flow, or libpq was not built with
         support for the builtin flow, this authdata type will not be used.
        

        

         If a non-NULL verification_uri_complete is
         provided, it may optionally be used for non-textual verification (for
         example, by displaying a QR code). The URL and user code should still
         be displayed to the end user in this case, because the code will be
         manually confirmed by the provider, and the URL lets users continue
         even if they can't use the non-textual method. For more information,
         see section 3.3.1 in
         RFC 8628.
        

       

      

      

       

        PQAUTHDATA_OAUTH_BEARER_TOKEN
        PQAUTHDATA_OAUTH_BEARER_TOKEN
       

       

        
Available in PostgreSQL 18 and later.

        

         Adds a custom implementation of a flow, replacing the builtin flow if
         it is installed.
         The hook should either directly return a Bearer token for the current
         user/issuer/scope combination, if one is available without blocking, or
         else set up an asynchronous callback to retrieve one.
        

        
         

          For PostgreSQL releases 19 and later,
          applications should prefer
          PQAUTHDATA_OAUTH_BEARER_TOKEN_V2.
         

        
        

         data points to an instance
         of PGoauthBearerRequest, which should be filled in
         by the implementation:

typedef struct PGoauthBearerRequest
{
    /* Hook inputs (constant across all calls) */
    const char *openid_configuration; /* OIDC discovery URL */
    const char *scope;                /* required scope(s), or NULL */

    /* Hook outputs */

    /*
     * Callback implementing a custom asynchronous OAuth flow. The signature is
     * platform-dependent: PQ_SOCKTYPE is SOCKET on Windows, and int everywhere
     * else.
     */
    PostgresPollingStatusType (*async) (PGconn *conn,
                                        struct PGoauthBearerRequest *request,
                                        PQ_SOCKTYPE *altsock);

    /* Callback to clean up custom allocations. */
    void        (*cleanup) (PGconn *conn, struct PGoauthBearerRequest *request);

    char       *token;   /* acquired Bearer token */
    void       *user;    /* hook-defined allocated data */
} PGoauthBearerRequest;

        

        

         Two pieces of information are provided to the hook by
         libpq:
         openid_configuration contains the URL of an
         OAuth discovery document describing the authorization server's
         supported flows, and scope contains a
         (possibly empty) space-separated list of OAuth scopes which are
         required to access the server. Either or both may be
         NULL to indicate that the information was not
         discoverable. (In this case, implementations may be able to establish
         the requirements using some other preconfigured knowledge, or they may
         choose to fail.)
        

        

         The final output of the hook is token, which
         must point to a valid Bearer token for use on the connection. (This
         token should be issued by the
          and hold the requested
         scopes, or the connection will be rejected by the server's validator
         module.) The allocated token string must remain valid until
         libpq is finished connecting; the hook
         should set a cleanup callback which will be
         called when libpq no longer requires it.
        

        

         If an implementation cannot immediately produce a
         token during the initial call to the hook,
         it should set the async callback to handle
         nonblocking communication with the authorization server.
         
          

           Performing blocking operations during the
           PQAUTHDATA_OAUTH_BEARER_TOKEN hook callback will
           interfere with nonblocking connection APIs such as
           PQconnectPoll and prevent concurrent connections
           from making progress. Applications which only ever use the
           synchronous connection primitives, such as
           PQconnectdb, may synchronously retrieve a token
           during the hook instead of implementing the
           async callback, but they will necessarily
           be limited to one connection at a time.
          

         
         This will be called to begin the flow immediately upon return from the
         hook. When the callback cannot make further progress without blocking,
         it should return either PGRES_POLLING_READING or
         PGRES_POLLING_WRITING after setting
         *altsock to the file descriptor that will be marked
         ready to read/write when progress can be made again. (This descriptor
         is then provided to the top-level polling loop via
         PQsocket().) Return PGRES_POLLING_OK
         after setting token when the flow is
         complete, or PGRES_POLLING_FAILED to indicate failure.
        

        

         Implementations may wish to store additional data for bookkeeping
         across calls to the async and
         cleanup callbacks. The
         user pointer is provided for this purpose;
         libpq will not touch its contents and the
         application may use it at its convenience. (Remember to free any
         allocations during token cleanup.)
        

       

      

      

       

        PQAUTHDATA_OAUTH_BEARER_TOKEN_V2
        
         PQAUTHDATA_OAUTH_BEARER_TOKEN_V2
         PQAUTHDATA_OAUTH_BEARER_TOKEN
        
       

       

        
Available in PostgreSQL 19 and later.

        

         Provides all the functionality of
         PQAUTHDATA_OAUTH_BEARER_TOKEN,
         as well as the ability to set custom error messages and retrieve the
         OAuth issuer ID that the client has trusted.
        

        

         data points to an instance
         of PGoauthBearerRequestV2:

typedef struct
{
    PGoauthBearerRequest v1;    /* see the PGoauthBearerRequest struct, above */

    /* Hook inputs (constant across all calls) */
    const char *issuer;            /* the issuer identifier (RFC 9207) in use */

    /* Hook outputs */
    const char *error;             /* hook-defined error message */
} PGoauthBearerRequestV2;

        

        

         Applications must first use the v1 struct
         member to implement the base API, as described
         above.
         libpq additionally guarantees that the
         request pointer passed to the
         v1.async and v1.cleanup
         callbacks may be safely cast to (PGoauthBearerRequestV2&nbsp;*),
         to make use of the additional members described below.
        

        
         

          Casting to (PGoauthBearerRequestV2&nbsp;*) is
          only safe when the hook type is
          PQAUTHDATA_OAUTH_BEARER_TOKEN_V2. Applications may
          crash or misbehave if a hook implementation attempts to access v2
          members when handling a v1 (PQAUTHDATA_OAUTH_BEARER_TOKEN)
          hook request.
         

        
        

         In addition to the functionality of the version 1 API, the v2 struct
         provides an additional input and output for the hook:
        

        

         issuer contains the issuer identifier, as
         defined in RFC 9207,
         that is in use for the current connection. This identifier is
         derived from .
         To avoid mix-up attacks, custom flows should ensure that any discovery
         metadata provided by the authorization server matches this issuer ID.
        

        

         error may be set to point to a custom
         error message when a flow fails. The message will be included as part
         of . Hooks must free any error
         message allocations during the v1.cleanup
         callback.
        

       

      

     

    

   

  

  

   
# Debugging and Developer Settings

   

    While developing against a local authorization server, it may be helpful to
    make use of the  connection
    parameter (or the equivalent PGOAUTHCAFILE environment
    variable) in the client application.
   

   

    Debug features may be enabled by setting the PGOAUTHDEBUG
    environment variable. This functionality is provided for ease of local
    development and testing. The variable accepts a comma-separated list of
    debug options:

```
PGOAUTHDEBUG=option1,option2,...         for safe options only
PGOAUTHDEBUG=UNSAFE:option1,option2,...  when using unsafe options
PGOAUTHDEBUG=UNSAFE                      legacy format; enables all options
```

   

   

    Available debug options:

    

     

      
http (unsafe)

      

       

        Permits the use of unencrypted HTTP during the OAuth provider exchange.
        This allows OAuth credentials to be transmitted over unencrypted
        connections, which is extremely dangerous and should only be used for
        local testing.
       

      

     

     

      
trace (unsafe)

      

       

        Prints HTTP traffic to standard error during the OAuth flow. This output
        contains critical secrets including bearer tokens, client secrets, access
        tokens, and authorization codes. Never share this output with third
        parties.
       

      

     

     

      
dos-endpoint (unsafe)

      

       

        Permits the use of zero-second retry intervals instead of the normal
        minimum of one second. This speeds up tests, but in normal operation it
        will cause the client to busy-loop, consuming CPU and network resources.
       

      

     

     

      
call-count (safe)

      

       

        Prints the total number of calls to the flow plugin to standard error
        when the OAuth flow completes. This helps developers debug the async
        callback behavior.
       

      

     

     

      
plugin-errors (safe)

      

       

        Prints plugin loading errors to standard error. This helps developers
        and package maintainers debug issues when the OAuth plugin fails to load.
       

      

     

    

   

   

    Unsafe options (http, trace,
    dos-endpoint) require the UNSAFE: prefix.
    If unsafe options are specified without this prefix, or if an option name is
    unrecognized, a warning is printed to standard error and that option is
    ignored. Other valid options in the list continue to work. Safe options
    (call-count, plugin-errors) can be
    used without the prefix.
   

   

    Examples:

```
PGOAUTHDEBUG=call-count              safe options only
PGOAUTHDEBUG=UNSAFE:http,trace       enable HTTP and traffic logging
PGOAUTHDEBUG=UNSAFE:http,call-count  mix of unsafe and safe
```

   

   
    

     Never use unsafe debug options in production environments. They expose
     secrets and behaviors that can be used to attack your clients and servers.
     Do not share trace output with third parties.
    

   
  

 

 

  
# Behavior in Threaded Programs

  
   threads
   with libpq
  

  

   As of version 17, libpq is always reentrant and thread-safe.
   However, one restriction is that no two threads attempt to manipulate
   the same PGconn object at the same time. In particular,
   you cannot issue concurrent commands from different threads through
   the same connection object. (If you need to run concurrent commands,
   use multiple connections.)
  

  

   PGresult objects are normally read-only after creation,
   and so can be passed around freely between threads.  However, if you use
   any of the PGresult-modifying functions described in
    or , it's up
   to you to avoid concurrent operations on the same PGresult,
   too.
  

  

   In earlier versions, libpq could be compiled
   with or without thread support, depending on compiler options. This
   function allows the querying of libpq's
   thread-safe status:
  

  

   

    
PQisthreadsafePQisthreadsafe

    

     

      Returns the thread safety status of the
      libpq library.

int PQisthreadsafe();

     

     

      Returns 1 if the libpq is thread-safe
      and 0 if it is not. Always returns 1 on version 17 and above.
     

    

   

  

  

   The deprecated functions  and
    are not thread-safe and should not be
   used in multithread programs.  
   can be replaced by .
    can be replaced by
   .
  

  

   If you are using Kerberos inside your application (in addition to inside
   libpq), you will need to do locking around
   Kerberos calls because Kerberos functions are not thread-safe.  See
   function PQregisterThreadLock in the
   libpq source code for a way to do cooperative
   locking between libpq and your application. (Note
   that it is only safe to call PQregisterThreadLock when
   there are no open connections.) Clients may retrieve the current locking
   callback with PQgetThreadLock; if no custom callback
   has been registered, a default is used.
  

  

   Similarly, if you are using Curl inside your application,
   and you do not already
   initialize
   libcurl globally before starting new threads, you will need to
   cooperatively lock (again via PQregisterThreadLock)
   around any code that may initialize libcurl. This restriction is lifted for
   more recent versions of Curl that are built to support thread-safe
   initialization; those builds can be identified by the advertisement of a
   threadsafe feature in their version metadata.
  

 

 

  
# Building libpq Programs

  
   compiling
   libpq applications
  

  

   To build (i.e., compile and link) a program using
   libpq you need to do all of the following
   things:

   

    

     

      Include the libpq-fe.h header file:

```
#include <libpq-fe.h>
```

      If you failed to do that then you will normally get error messages
      from your compiler similar to:

```
foo.c: In function `main':
foo.c:34: `PGconn' undeclared (first use in this function)
foo.c:35: `PGresult' undeclared (first use in this function)
foo.c:54: `CONNECTION_BAD' undeclared (first use in this function)
foo.c:68: `PGRES_COMMAND_OK' undeclared (first use in this function)
foo.c:95: `PGRES_TUPLES_OK' undeclared (first use in this function)
```

     

    

    

     

      Point your compiler to the directory where the PostgreSQL header
      files were installed, by supplying the
      -Idirectory option
      to your compiler.  (In some cases the compiler will look into
      the directory in question by default, so you can omit this
      option.)  For instance, your compile command line could look
      like:

```
cc -c -I/usr/local/pgsql/include testprog.c
```

      If you are using makefiles then add the option to the
      CPPFLAGS variable:

```
CPPFLAGS += -I/usr/local/pgsql/include
```

     

     

      If there is any chance that your program might be compiled by
      other users then you should not hardcode the directory location
      like that.  Instead, you can run the utility
      pg_configpg_configwith libpq to find out where the header
      files are on the local system:

```
$ pg_config --includedir
/usr/local/include
```

     

     

      If you
      have pkg-configpkg-configwith
      libpq installed, you can run instead:

```
$ pkg-config --cflags libpq
-I/usr/local/include
```

      Note that this will already include the -I in front of
      the path.
     

     

      Failure to specify the correct option to the compiler will
      result in an error message such as:

```
testlibpq.c:8:22: libpq-fe.h: No such file or directory
```

     

    

    

     

      When linking the final program, specify the option
      -lpq so that the libpq
      library gets pulled in, as well as the option
      -Ldirectory to point
      the compiler to the directory where the
      libpq library resides.  (Again, the
      compiler will search some directories by default.)  For maximum
      portability, put the -L option before the
      -lpq option.  For example:

```
cc -o testprog testprog1.o testprog2.o -L/usr/local/pgsql/lib -lpq
```

     

     

      You can find out the library directory using
      pg_config as well:

```
$ pg_config --libdir
/usr/local/pgsql/lib
```

     

     

      Or again use pkg-config:

```
$ pkg-config --libs libpq
-L/usr/local/pgsql/lib -lpq
```

      Note again that this prints the full options, not only the path.
     

     

      Error messages that point to problems in this area could look like
      the following:

```
testlibpq.o: In function `main':
testlibpq.o(.text+0x60): undefined reference to `PQsetdbLogin'
testlibpq.o(.text+0x71): undefined reference to `PQstatus'
testlibpq.o(.text+0xa4): undefined reference to `PQerrorMessage'
```

      This means you forgot -lpq.

```
/usr/bin/ld: cannot find -lpq
```

      This means you forgot the -L option or did not
      specify the right directory.
     

    

   

  

 

 

  
# Example Programs

  

   These examples and others can be found in the
   directory src/test/examples in the source code
   distribution.
  

  
   
# libpq Example Program 1

```

#include 
#include "libpq-fe.h"

static void
exit_nicely(PGconn *conn)
{
    PQfinish(conn);
    exit(1);
}

int
main(int argc, char **argv)
{
    const char *conninfo;
    PGconn     *conn;
    PGresult   *res;
    int         nFields;
    int         i,
                j;

    /*
     * If the user supplies a parameter on the command line, use it as the
     * conninfo string; otherwise default to setting dbname=postgres and using
     * environment variables or defaults for all other connection parameters.
     */
    if (argc > 1)
        conninfo = argv[1];
    else
        conninfo = "dbname = postgres";

    /* Make a connection to the database */
    conn = PQconnectdb(conninfo);

    /* Check to see that the backend connection was successfully made */
    if (PQstatus(conn) != CONNECTION_OK)
    {
        fprintf(stderr, "%s", PQerrorMessage(conn));
        exit_nicely(conn);
    }

    /* Set always-secure search path, so malicious users can't take control. */
    res = PQexec(conn,
                 "SELECT pg_catalog.set_config('search_path', '', false)");
    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        fprintf(stderr, "SET failed: %s", PQerrorMessage(conn));
        PQclear(res);
        exit_nicely(conn);
    }

    /*
     * Should PQclear PGresult whenever it is no longer needed to avoid memory
     * leaks
     */
    PQclear(res);

    /*
     * Our test case here involves using a cursor, for which we must be inside
     * a transaction block.  We could do the whole thing with a single
     * PQexec() of "SELECT * FROM pg_database", but that's too trivial to make
     * a good example.
     */

    /* Start a transaction block */
    res = PQexec(conn, "BEGIN");
    if (PQresultStatus(res) != PGRES_COMMAND_OK)
    {
        fprintf(stderr, "BEGIN command failed: %s", PQerrorMessage(conn));
        PQclear(res);
        exit_nicely(conn);
    }
    PQclear(res);

    /*
     * Fetch rows from pg_database, the system catalog of databases
     */
    res = PQexec(conn, "DECLARE myportal CURSOR FOR SELECT * FROM pg_database");
    if (PQresultStatus(res) != PGRES_COMMAND_OK)
    {
        fprintf(stderr, "DECLARE CURSOR failed: %s", PQerrorMessage(conn));
        PQclear(res);
        exit_nicely(conn);
    }
    PQclear(res);

    res = PQexec(conn, "FETCH ALL in myportal");
    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        fprintf(stderr, "FETCH ALL failed: %s", PQerrorMessage(conn));
        PQclear(res);
        exit_nicely(conn);
    }

    /* first, print out the attribute names */
    nFields = PQnfields(res);
    for (i = 0; i 
```

  

  
   
# libpq Example Program 2

```

#endif
#include 
#include 
#include 
#include 
#include 
#include 
#include 

#include "libpq-fe.h"

static void
exit_nicely(PGconn *conn)
{
    PQfinish(conn);
    exit(1);
}

int
main(int argc, char **argv)
{
    const char *conninfo;
    PGconn     *conn;
    PGresult   *res;
    PGnotify   *notify;
    int         nnotifies;

    /*
     * If the user supplies a parameter on the command line, use it as the
     * conninfo string; otherwise default to setting dbname=postgres and using
     * environment variables or defaults for all other connection parameters.
     */
    if (argc > 1)
        conninfo = argv[1];
    else
        conninfo = "dbname = postgres";

    /* Make a connection to the database */
    conn = PQconnectdb(conninfo);

    /* Check to see that the backend connection was successfully made */
    if (PQstatus(conn) != CONNECTION_OK)
    {
        fprintf(stderr, "%s", PQerrorMessage(conn));
        exit_nicely(conn);
    }

    /* Set always-secure search path, so malicious users can't take control. */
    res = PQexec(conn,
                 "SELECT pg_catalog.set_config('search_path', '', false)");
    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        fprintf(stderr, "SET failed: %s", PQerrorMessage(conn));
        PQclear(res);
        exit_nicely(conn);
    }

    /*
     * Should PQclear PGresult whenever it is no longer needed to avoid memory
     * leaks
     */
    PQclear(res);

    /*
     * Issue LISTEN command to enable notifications from the rule's NOTIFY.
     */
    res = PQexec(conn, "LISTEN TBL2");
    if (PQresultStatus(res) != PGRES_COMMAND_OK)
    {
        fprintf(stderr, "LISTEN command failed: %s", PQerrorMessage(conn));
        PQclear(res);
        exit_nicely(conn);
    }
    PQclear(res);

    /* Quit after four notifies are received. */
    nnotifies = 0;
    while (nnotifies relname, notify->be_pid);
            PQfreemem(notify);
            nnotifies++;
            PQconsumeInput(conn);
        }
    }

    fprintf(stderr, "Done.\n");

    /* close the connection to the database and cleanup */
    PQfinish(conn);

    return 0;
}
]]>
```

  

  
   
# libpq Example Program 3

```

#endif

#include 
#include 
#include 
#include 
#include 
#include "libpq-fe.h"

/* for ntohl/htonl */
#include 
#include 

static void
exit_nicely(PGconn *conn)
{
    PQfinish(conn);
    exit(1);
}

/*
 * This function prints a query result that is a binary-format fetch from
 * a table defined as in the comment above.  We split it out because the
 * main() function uses it twice.
 */
static void
show_binary_results(PGresult *res)
{
    int         i,
                j;
    int         i_fnum,
                t_fnum,
                b_fnum;

    /* Use PQfnumber to avoid assumptions about field order in result */
    i_fnum = PQfnumber(res, "i");
    t_fnum = PQfnumber(res, "t");
    b_fnum = PQfnumber(res, "b");

    for (i = 0; i  1)
        conninfo = argv[1];
    else
        conninfo = "dbname = postgres";

    /* Make a connection to the database */
    conn = PQconnectdb(conninfo);

    /* Check to see that the backend connection was successfully made */
    if (PQstatus(conn) != CONNECTION_OK)
    {
        fprintf(stderr, "%s", PQerrorMessage(conn));
        exit_nicely(conn);
    }

    /* Set always-secure search path, so malicious users can't take control. */
    res = PQexec(conn, "SET search_path = testlibpq3");
    if (PQresultStatus(res) != PGRES_COMMAND_OK)
    {
        fprintf(stderr, "SET failed: %s", PQerrorMessage(conn));
        PQclear(res);
        exit_nicely(conn);
    }
    PQclear(res);

    /*
     * The point of this program is to illustrate use of PQexecParams() with
     * out-of-line parameters, as well as binary transmission of data.
     *
     * This first example transmits the parameters as text, but receives the
     * results in binary format.  By using out-of-line parameters we can avoid
     * a lot of tedious mucking about with quoting and escaping, even though
     * the data is text.  Notice how we don't have to do anything special with
     * the quote mark in the parameter value.
     */

    /* Here is our out-of-line parameter value */
    paramValues[0] = "joe's place";

    res = PQexecParams(conn,
                       "SELECT * FROM test1 WHERE t = $1",
                       1,       /* one param */
                       NULL,    /* let the backend deduce param type */
                       paramValues,
                       NULL,    /* don't need param lengths since text */
                       NULL,    /* default to all text params */
                       1);      /* ask for binary results */

    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);
        exit_nicely(conn);
    }

    show_binary_results(res);

    PQclear(res);

    /*
     * In this second example we transmit an integer parameter in binary form,
     * and again retrieve the results in binary form.
     *
     * Although we tell PQexecParams we are letting the backend deduce
     * parameter type, we really force the decision by casting the parameter
     * symbol in the query text.  This is a good safety measure when sending
     * binary parameters.
     */

    /* Convert integer value "2" to network byte order */
    binaryIntVal = htonl((uint32_t) 2);

    /* Set up parameter arrays for PQexecParams */
    paramValues[0] = (char *) &binaryIntVal;
    paramLengths[0] = sizeof(binaryIntVal);
    paramFormats[0] = 1;        /* binary */

    res = PQexecParams(conn,
                       "SELECT * FROM test1 WHERE i = $1::int4",
                       1,       /* one param */
                       NULL,    /* let the backend deduce param type */
                       paramValues,
                       paramLengths,
                       paramFormats,
                       1);      /* ask for binary results */

    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);
        exit_nicely(conn);
    }

    show_binary_results(res);

    PQclear(res);

    /* close the connection to the database and cleanup */
    PQfinish(conn);

    return 0;
}
]]>
```
