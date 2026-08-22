# Server Configuration

  
   configuration
   of the server
  

  

   There are many configuration parameters that affect the behavior of
   the database system. In the first section of this chapter we
   describe how to interact with configuration parameters. The subsequent sections
   discuss each parameter in detail.
  

  

   
# Setting Parameters

   GUC

   

    
# Parameter Names and Values

    

     All parameter names are case-insensitive. Every parameter takes a
     value of one of five types: boolean, string, integer, floating point,
     or enumerated (enum).  The type determines the syntax for setting the
     parameter:
    

    

     

      

       Boolean:
       Values can be written as
       on,
       off,
       true,
       false,
       yes,
       no,
       1,
       0
       (all case-insensitive) or any unambiguous prefix of one of these.
      

     

     

      

       String:
       In general, enclose the value in single quotes, doubling any single
       quotes within the value.  Quotes can usually be omitted if the value
       is a simple number or identifier, however.
       (Values that match an SQL keyword require quoting in some contexts.)
      

     

     

      

       Numeric (integer and floating point):
       Numeric parameters can be specified in the customary integer and
       floating-point formats; fractional values are rounded to the nearest
       integer if the parameter is of integer type.  Integer parameters
       additionally accept hexadecimal input (beginning
       with 0x) and octal input (beginning
       with 0), but these formats cannot have a fraction.
       Do not use thousands separators.
       Quotes are not required, except for hexadecimal input.
      

     

     

      

       Numeric with Unit:
       Some numeric parameters have an implicit unit, because they describe
       quantities of memory or time. The unit might be bytes, kilobytes, blocks
       (typically eight kilobytes), milliseconds, seconds, or minutes.
       An unadorned numeric value for one of these settings will use the
       setting's default unit, which can be learned from
       pg_settings.unit.
       For convenience, settings can be given with a unit specified explicitly,
       for example '120 ms' for a time value, and they will be
       converted to whatever the parameter's actual unit is.  Note that the
       value must be written as a string (with quotes) to use this feature.
       The unit name is case-sensitive, and there can be whitespace between
       the numeric value and the unit.

       

        

         

          Valid memory units are B (bytes),
          kB (kilobytes),
          MB (megabytes), GB
          (gigabytes), and TB (terabytes).
          The multiplier for memory units is 1024, not 1000.
         

        

        

         

          Valid time units are
          us (microseconds),
          ms (milliseconds),
          s (seconds), min (minutes),
          h (hours), and d (days).
         

        

       

       If a fractional value is specified with a unit, it will be rounded
       to a multiple of the next smaller unit if there is one.
       For example, 30.1 GB will be converted
       to 30822 MB not 32319628902 B.
       If the parameter is of integer type, a final rounding to integer
       occurs after any unit conversion.
      

     

     

      

       Enumerated:
       Enumerated-type parameters are written in the same way as string
       parameters, but are restricted to have one of a limited set of
       values.  The values allowable for such a parameter can be found from
       pg_settings.enumvals.
       Enum parameter values are case-insensitive.
      

     

    

   

   

    
# Parameter Interaction via the Configuration File

    

     The most fundamental way to set these parameters is to edit the file
     postgresql.confpostgresql.conf,
     which is normally kept in the data directory.  A default copy is
     installed when the database cluster directory is initialized.
     An example of what this file might look like is:

```
# This is a comment
log_connections = all
log_destination = 'syslog'
search_path = '"$user", public'
shared_buffers = 128MB
```

     One parameter is specified per line. The equal sign between name and
     value is optional. Whitespace is insignificant (except within a quoted
     parameter value) and blank lines are
     ignored. Hash marks (#) designate the remainder
     of the line as a comment.  Parameter values that are not simple
     identifiers or numbers must be single-quoted.  To embed a single
     quote in a parameter value, write either two quotes (preferred)
     or backslash-quote.
     If the file contains multiple entries for the same parameter,
     all but the last one are ignored.
    

    

     Parameters set in this way provide default values for the cluster.
     The settings seen by active sessions will be these values unless they
     are overridden.  The following sections describe ways in which the
     administrator or user can override these defaults.
    

    

     
      SIGHUP
     
     The configuration file is reread whenever the main server process
     receives a SIGHUP signal; this signal is most easily
     sent by running pg_ctl reload from the command line or by
     calling the SQL function pg_reload_conf(). The main
     server process also propagates this signal to all currently running
     server processes, so that existing sessions also adopt the new values
     (this will happen after they complete any currently-executing client
     command).  Alternatively, you can
     send the signal to a single server process directly.  Some parameters
     can only be set at server start; any changes to their entries in the
     configuration file will be ignored until the server is restarted.
     Invalid parameter settings in the configuration file are likewise
     ignored (but logged) during SIGHUP processing.
    

    

     In addition to postgresql.conf,
     a PostgreSQL data directory contains a file
     postgresql.auto.confpostgresql.auto.conf,
     which has the same format as postgresql.conf but
     is intended to be edited automatically, not manually.  This file holds
     settings provided through the ALTER SYSTEM command.
     This file is read whenever postgresql.conf is,
     and its settings take effect in the same way.  Settings
     in postgresql.auto.conf override those
     in postgresql.conf.
    

    

     External tools may also
     modify postgresql.auto.conf.  It is not
     recommended to do this while the server is running unless  is set to off, since a
     concurrent ALTER SYSTEM command could overwrite
     such changes.  Such tools might simply append new settings to the end,
     or they might choose to remove duplicate settings and/or comments
     (as ALTER SYSTEM will).
    

    

     The system view
     pg_file_settings
     can be helpful for pre-testing changes to the configuration files, or for
     diagnosing problems if a SIGHUP signal did not have the
     desired effects.
    

   

   

    
# Parameter Interaction via SQL

     

      PostgreSQL provides three SQL
      commands to establish configuration defaults.
      The already-mentioned ALTER SYSTEM command
      provides an SQL-accessible means of changing global defaults; it is
      functionally equivalent to editing postgresql.conf.
      In addition, there are two commands that allow setting of defaults
      on a per-database or per-role basis:
     

     

     

      

       The ALTER DATABASE command allows global
       settings to be overridden on a per-database basis.
      

     

     

      

       The ALTER ROLE command allows both global and
       per-database settings to be overridden with user-specific values.
      

     

    

     

      Values set with ALTER DATABASE and ALTER ROLE
      are applied only when starting a fresh database session.  They
      override values obtained from the configuration files or server
      command line, and constitute defaults for the rest of the session.
      Note that some settings cannot be changed after server start, and
      so cannot be set with these commands (or the ones listed below).
    

     

      Once a client is connected to the database, PostgreSQL
      provides two additional SQL commands (and equivalent functions) to
      interact with session-local configuration settings:
    

    

     

     

      The SHOW command allows inspection of the
      current value of any parameter.  The corresponding SQL function is
      current_setting(setting_name text)
      (see ).
     

     

     

      

       The SET command allows modification of the
       current value of those parameters that can be set locally to a
       session; it has no effect on other sessions.
       Many parameters can be set this way by any user, but some can
       only be set by superusers and users who have been
       granted SET privilege on that parameter.
       The corresponding SQL function is
       set_config(setting_name, new_value, is_local)
       (see ).
      

     

    

    

     In addition, the system view pg_settings can be
     used to view and change session-local values:
    

    

     

      

       Querying this view is similar to using SHOW ALL but
       provides more detail.  It is also more flexible, since it's possible
       to specify filter conditions or join against other relations.
      

     

     

      

       Using UPDATE on this view, specifically
       updating the setting column, is the equivalent
       of issuing SET commands.  For example, the equivalent of

```
SET configuration_parameter TO DEFAULT;
```

       is:

```
UPDATE pg_settings SET setting = reset_val WHERE name = 'configuration_parameter';
```

      

     

    

   

   

    
# Parameter Interaction via the Shell

     

      In addition to setting global defaults or attaching
      overrides at the database or role level, you can pass settings to
      PostgreSQL via shell facilities.
      Both the server and libpq client library
      accept parameter values via the shell.
     

     

      

      

       During server startup, parameter settings can be
       passed to the postgres command via the
       -c name=value command-line parameter, or its equivalent
       --name=value variation.  For example,

```
postgres -c log_connections=all --log-destination='syslog'
```

       Settings provided in this way override those set via
       postgresql.conf or ALTER SYSTEM,
       so they cannot be changed globally without restarting the server.
     

    

    

     

      When starting a client session via libpq,
      parameter settings can be
      specified using the PGOPTIONS environment variable.
      Settings established in this way constitute defaults for the life
      of the session, but do not affect other sessions.
      For historical reasons, the format of PGOPTIONS is
      similar to that used when launching the postgres
      command; specifically, the -c, or prepended
      --, before the name must be specified. For example,

```
env PGOPTIONS="-c geqo=off --statement-timeout=5min" psql
```

     

     

      Other clients and libraries might provide their own mechanisms,
      via the shell or otherwise, that allow the user to alter session
      settings without direct use of SQL commands.
     

    

   

   

   

    
# Managing Configuration File Contents

     

      PostgreSQL provides several features for breaking
      down complex postgresql.conf files into sub-files.
      These features are especially useful when managing multiple servers
      with related, but not identical, configurations.
     

     

      
       include
       in configuration file
      
      In addition to individual parameter settings,
      the postgresql.conf file can contain include
      directives, which specify another file to read and process as if
      it were inserted into the configuration file at this point.  This
      feature allows a configuration file to be divided into physically
      separate parts.  Include directives simply look like:

```
include 'filename'
```

      If the file name is not an absolute path, it is taken as relative to
      the directory containing the referencing configuration file.
      Inclusions can be nested.
     

     

      
       include_if_exists
       in configuration file
      
      There is also an include_if_exists directive, which acts
      the same as the include directive, except
      when the referenced file does not exist or cannot be read.  A regular
      include will consider this an error condition, but
      include_if_exists merely logs a message and continues
      processing the referencing configuration file.
     

     

      
       include_dir
       in configuration file
      
      The postgresql.conf file can also contain
      include_dir directives, which specify an entire
      directory of configuration files to include.  These look like

```
include_dir 'directory'
```

      Non-absolute directory names are taken as relative to the directory
      containing the referencing configuration file.  Within the specified
      directory, only non-directory files whose names end with the
      suffix .conf will be included.  File names that
      start with the . character are also ignored, to
      prevent mistakes since such files are hidden on some platforms.  Multiple
      files within an include directory are processed in file name order
      (according to C locale rules, i.e., numbers before letters, and
      uppercase letters before lowercase ones).
     

     

      Include files or directories can be used to logically separate portions
      of the database configuration, rather than having a single large
      postgresql.conf file.  Consider a company that has two
      database servers, each with a different amount of memory.  There are
      likely elements of the configuration both will share, for things such
      as logging.  But memory-related parameters on the server will vary
      between the two.  And there might be server specific customizations,
      too.  One way to manage this situation is to break the custom
      configuration changes for your site into three files.  You could add
      this to the end of your postgresql.conf file to include
      them:

```
include 'shared.conf'
include 'memory.conf'
include 'server.conf'
```

      All systems would have the same shared.conf.  Each
      server with a particular amount of memory could share the
      same memory.conf; you might have one for all servers
      with 8GB of RAM, another for those having 16GB.  And
      finally server.conf could have truly server-specific
      configuration information in it.
     

     

      Another possibility is to create a configuration file directory and
      put this information into files there. For example, a conf.d
      directory could be referenced at the end of postgresql.conf:

```
include_dir 'conf.d'
```

      Then you could name the files in the conf.d directory
      like this:

```
00shared.conf
01memory.conf
02server.conf
```

       This naming convention establishes a clear order in which these
       files will be loaded.  This is important because only the last
       setting encountered for a particular parameter while the server is
       reading configuration files will be used.  In this example,
       something set in conf.d/02server.conf would override a
       value set in conf.d/01memory.conf.
     

     

      You might instead use this approach to naming the files
      descriptively:

```
00shared.conf
01memory-8GB.conf
02server-foo.conf
```

      This sort of arrangement gives a unique name for each configuration file
      variation.  This can help eliminate ambiguity when several servers have
      their configurations all stored in one place, such as in a version
      control repository.  (Storing database configuration files under version
      control is another good practice to consider.)
     

    

   

   

    
# File Locations

     

      In addition to the postgresql.conf file
      already mentioned, PostgreSQL uses
      three other manually-edited configuration files, which control
      client authentication (their use is discussed in ) and SSL host configuration.
      By default, all four
      configuration files are stored in the database cluster's data
      directory.  The parameters described in this section allow the
      configuration files to be placed elsewhere.  (Doing so can ease
      administration.  In particular it is often easier to ensure that
      the configuration files are properly backed-up when they are
      kept separate.)
     

     

     

      
data_directory (string)
      
       data_directory configuration parameter
      
      

      

       

         Specifies the directory to use for data storage.
         This parameter can only be set at server start.
       

      

     

     

      
config_file (string)
      
       config_file configuration parameter
      
      

      

       

         Specifies the main server configuration file
         (customarily called postgresql.conf).
         This parameter can only be set on the postgres command line.
       

      

     

     

      
hba_file (string)
      
       hba_file configuration parameter
      
      

      

       

         Specifies the configuration file for host-based authentication
         (customarily called pg_hba.conf).
         This parameter can only be set at server start.
       

      

     

     

      
ident_file (string)
      
       ident_file configuration parameter
      
      

      

       

         Specifies the configuration file for user name mapping
         (customarily called pg_ident.conf).
         This parameter can only be set at server start.
         See also .
       

      

     

     

      
hosts_file (string)
      
       hosts_file configuration parameter
      
      

      

       

         Specifies the configuration file for host-based SSL configuration
         (customarily called pg_hosts.conf).
         This parameter can only be set at server start. See also
         .
       

      

     

     

      
external_pid_file (string)
      
       external_pid_file configuration parameter
      
      

      

       

        Specifies the name of an additional process-ID (PID) file that the
        server should create for use by server administration programs.
        This parameter can only be set at server start.
       

      

     

     

     

      In a default installation, none of the above parameters are set
      explicitly.  Instead, the
      data directory is specified by the -D command-line
      option or the PGDATA environment variable, and the
      configuration files are all found within the data directory.
     

     

      If you wish to keep the configuration files elsewhere than the
      data directory, the postgres -D
      command-line option or PGDATA environment variable
      must point to the directory containing the configuration files,
      and the data_directory parameter must be set in
      postgresql.conf (or on the command line) to show
      where the data directory is actually located.  Notice that
      data_directory overrides -D and
      PGDATA for the location
      of the data directory, but not for the location of the configuration
      files.
     

     

      If you wish, you can specify the configuration file names and locations
      individually using the parameters config_file,
      hba_file, ident_file and/or
      hosts_file.
      config_file can only be specified on the
      postgres command line, but the others can be
      set within the main configuration file.  If all four parameters plus
      data_directory are explicitly set, then it is not necessary
      to specify -D or PGDATA.
     

     

      When setting any of these parameters, a relative path will be interpreted
      with respect to the directory in which postgres
      is started.
     

   

   

    
# Connections and Authentication

    

     
# Connection Settings

     

     

      
listen_addresses (string)
      
       listen_addresses configuration parameter
      
      

      

       

         Specifies the TCP/IP address(es) on which the server is
         to listen for connections from client applications.
         The value takes the form of a comma-separated list of host names
         and/or numeric IP addresses.  The special entry *
         corresponds to all available IP interfaces.  The entry
         0.0.0.0 allows listening for all IPv4 addresses and
         :: allows listening for all IPv6 addresses.
         If the list is empty, the server does not listen on any IP interface
         at all, in which case only Unix-domain sockets can be used to connect
         to it.  If the list is not empty, the server will start if it
         can listen on at least one TCP/IP address.  A warning will be
         emitted for any TCP/IP address which cannot be opened.
         The default value is localhost,
         which allows only local TCP/IP loopback connections to be
         made.
       

       

         While client authentication () allows fine-grained control
         over who can access the server, listen_addresses
         controls which interfaces accept connection attempts, which
         can help prevent repeated malicious connection requests on
         insecure network interfaces.  This parameter can only be set
         at server start.
       

      

     

     

      
port (integer)
      
       port configuration parameter
      
      

      

       

        The TCP port the server listens on; 5432 by default.  Note that the
        same port number is used for all IP addresses the server listens on.
        This parameter can only be set at server start.
       

      

     

     

      
max_connections (integer)
      
       max_connections configuration parameter
      
      

      

       

        Determines the maximum number of concurrent connections to the
        database server. The default is typically 100 connections, but
        might be less if your kernel settings will not support it (as
        determined during initdb).  This parameter can
        only be set at server start.
       

       

        PostgreSQL sizes certain resources based directly on the value of
        max_connections. Increasing its value leads to
        higher allocation of those resources, including shared memory.
       

       

        When running a standby server, you must set this parameter to the
        same or higher value than on the primary server. Otherwise, queries
        will not be allowed on the standby server.
       

      

     

     

      
reserved_connections (integer)
      
       reserved_connections configuration parameter
      
      

      

       

        Determines the number of connection slots that are
        reserved for connections by roles with privileges of the
        
        role.  Whenever the number of free connection slots is greater than
         but less than or
        equal to the sum of superuser_reserved_connections
        and reserved_connections, new connections will be
        accepted only for superusers and roles with privileges of
        pg_use_reserved_connections.  If
        superuser_reserved_connections or fewer connection
        slots are available, new connections will be accepted only for
        superusers.
       

       

        The default value is zero connections.  The value must be less than
        max_connections minus
        superuser_reserved_connections.  This parameter can
        only be set at server start.
       

      

     

     

      
superuser_reserved_connections
      (integer)
      
       superuser_reserved_connections configuration parameter
      
      

      

       

        Determines the number of connection slots that
        are reserved for connections by PostgreSQL
        superusers.  At most 
        connections can ever be active simultaneously.  Whenever the
        number of active concurrent connections is at least
        max_connections minus
        superuser_reserved_connections, new
        connections will be accepted only for superusers.  The connection slots
        reserved by this parameter are intended as final reserve for emergency
        use after the slots reserved by
         have been exhausted.
       

       

        The default value is three connections. The value must be less
        than max_connections minus
        reserved_connections.
        This parameter can only be set at server start.
       

      

     

     

      
unix_socket_directories (string)
      
       unix_socket_directories configuration parameter
      
      

      

       

        Specifies the directory of the Unix-domain socket(s) on which the
        server is to listen for connections from client applications.
        Multiple sockets can be created by listing multiple directories
        separated by commas.  Whitespace between entries is
        ignored; surround a directory name with double quotes if you need
        to include whitespace or commas in the name.
        An empty value
        specifies not listening on any Unix-domain sockets, in which case
        only TCP/IP sockets can be used to connect to the server.
       

       

        A value that starts with @ specifies that a
        Unix-domain socket in the abstract namespace should be created
        (currently supported on Linux only).  In that case, this value
        does not specify a directory but a prefix from which
        the actual socket name is computed in the same manner as for the
        file-system namespace.  While the abstract socket name prefix can be
        chosen freely, since it is not a file-system location, the convention
        is to nonetheless use file-system-like values such as
        @/tmp.
       

       

        The default value is normally
        /tmp, but that can be changed at build time.
        On Windows, the default is empty, which means no Unix-domain socket is
        created by default.
        This parameter can only be set at server start.
       

       

        In addition to the socket file itself, which is named
        .s.PGSQL.nnnn where
        nnnn is the server's port number, an ordinary file
        named .s.PGSQL.nnnn.lock will be
        created in each of the unix_socket_directories directories.
        Neither file should ever be removed manually.
        For sockets in the abstract namespace, no lock file is created.
       

      

     

     

      
unix_socket_group (string)
      
       unix_socket_group configuration parameter
      
      

      

       

        Sets the owning group of the Unix-domain socket(s).  (The owning
        user of the sockets is always the user that starts the
        server.)  In combination with the parameter
        unix_socket_permissions this can be used as
        an additional access control mechanism for Unix-domain connections.
        By default this is the empty string, which uses the default
        group of the server user.  This parameter can only be set at
        server start.
       

       

        This parameter is not supported on Windows.  Any setting will be
        ignored.  Also, sockets in the abstract namespace have no file owner,
        so this setting is also ignored in that case.
       

      

     

     

      
unix_socket_permissions (integer)
      
       unix_socket_permissions configuration parameter
      
      

      

       

        Sets the access permissions of the Unix-domain socket(s).  Unix-domain
        sockets use the usual Unix file system permission set.
        The parameter value is expected to be a numeric mode
        specified in the format accepted by the
        chmod and umask
        system calls.  (To use the customary octal format the number
        must start with a 0 (zero).)
       

       

        The default permissions are 0777, meaning
        anyone can connect. Reasonable alternatives are
        0770 (only user and group, see also
        unix_socket_group) and 0700
        (only user). (Note that for a Unix-domain socket, only write
        permission matters, so there is no point in setting or revoking
        read or execute permissions.)
       

       

        This access control mechanism is independent of the one
        described in .
       

       

        This parameter can only be set at server start.
       

       

        This parameter is irrelevant on systems, notably Solaris as of Solaris
        10, that ignore socket permissions entirely.  There, one can achieve a
        similar effect by pointing unix_socket_directories to a
        directory having search permission limited to the desired audience.
       

       

        Sockets in the abstract namespace have no file permissions, so this
        setting is also ignored in that case.
       

      

     

     

      
bonjour (boolean)
      
       bonjour configuration parameter
      
      

      

       

        Enables advertising the server's existence via
        Bonjour.  The default is off.
        This parameter can only be set at server start.
       

      

     

     

      
bonjour_name (string)
      
       bonjour_name configuration parameter
      
      

      

       

        Specifies the Bonjour service
        name.  The computer name is used if this parameter is set to the
        empty string '' (which is the default).  This parameter is
        ignored if the server was not compiled with
        Bonjour support.
        This parameter can only be set at server start.
       

      

     

     

     

     

     
# TCP Settings

     

     

      
tcp_keepalives_idle (integer)
      
       tcp_keepalives_idle configuration parameter
      
      

      

       

        Specifies the amount of time with no network activity after which
        the operating system should send a TCP keepalive message to the client.
        If this value is specified without units, it is taken as seconds.
        A value of 0 (the default) selects the operating system's default.
        On Windows, setting a value of 0 will set this parameter to 2 hours,
        since Windows does not provide a way to read the system default value.
        This parameter is supported only on systems that support
        TCP_KEEPIDLE or an equivalent socket option, and on
        Windows; on other systems, it must be zero.
        In sessions connected via a Unix-domain socket, this parameter is
        ignored and always reads as zero.
       

      

     

     

      
tcp_keepalives_interval (integer)
      
       tcp_keepalives_interval configuration parameter
      
      

      

       

        Specifies the amount of time after which a TCP keepalive message
        that has not been acknowledged by the client should be retransmitted.
        If this value is specified without units, it is taken as seconds.
        A value of 0 (the default) selects the operating system's default.
        On Windows, setting a value of 0 will set this parameter to 1 second,
        since Windows does not provide a way to read the system default value.
        This parameter is supported only on systems that support
        TCP_KEEPINTVL or an equivalent socket option, and on
        Windows; on other systems, it must be zero.
        In sessions connected via a Unix-domain socket, this parameter is
        ignored and always reads as zero.
       

      

     

     

      
tcp_keepalives_count (integer)
      
       tcp_keepalives_count configuration parameter
      
      

      

       

        Specifies the number of TCP keepalive messages that can be lost before
        the server's connection to the client is considered dead.
        A value of 0 (the default) selects the operating system's default.
        This parameter is supported only on systems that support
        TCP_KEEPCNT or an equivalent socket option (which does not include Windows);
        on other systems, it must be zero.
        In sessions connected via a Unix-domain socket, this parameter is
        ignored and always reads as zero.
       

      

     

     

      
tcp_user_timeout (integer)
      
       tcp_user_timeout configuration parameter
      
      

      

       

        Specifies the amount of time that transmitted data may
        remain unacknowledged before the TCP connection is forcibly closed.
        If this value is specified without units, it is taken as milliseconds.
        A value of 0 (the default) selects the operating system's default.
        This parameter is supported only on systems that support
        TCP_USER_TIMEOUT (which does not include Windows); on other systems, it must be zero.
        In sessions connected via a Unix-domain socket, this parameter is
        ignored and always reads as zero.
       

      

     

     

      
client_connection_check_interval (integer)
      
       client_connection_check_interval configuration parameter
      
      

      

       

        Sets the time interval between optional checks that the client is still
        connected, while running queries.  The check is performed by polling
        the socket, and allows long running queries to be aborted sooner if
        the kernel reports that the connection is closed.
       

       

        This option relies on kernel events exposed by Linux, macOS, illumos
        and the BSD family of operating systems, and is not currently available
        on other systems.
       

       

        If the value is specified without units, it is taken as milliseconds.
        The default value is 0, which disables connection
        checks.  Without connection checks, the server will detect the loss of
        the connection only at the next interaction with the socket, when it
        waits for, receives or sends data.
       

       

        For the kernel itself to detect lost TCP connections reliably and within
        a known timeframe in all scenarios including network failure, it may
        also be necessary to adjust the TCP keepalive settings of the operating
        system, or the ,
         and
         settings of
        PostgreSQL.
       

      

     

     

     

     

     
# Authentication

     

     

      
authentication_timeout (integer)
      timeoutclient authentication
      client authenticationtimeout during
      
       authentication_timeout configuration parameter
      
      

      

       

        Maximum amount of time allowed to complete client authentication. If a
        would-be client has not completed the authentication protocol in
        this much time, the server closes the connection. This prevents
        hung clients from occupying a connection indefinitely.
        If this value is specified without units, it is taken as seconds.
        The default is one minute (1m).
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
password_encryption (enum)
      
       password_encryption configuration parameter
      
      

      

       

        When a password is specified in  or
        , this parameter determines the
        algorithm to use to encrypt the password.  Possible values are
        scram-sha-256, which will encrypt the password with
        SCRAM-SHA-256, and md5, which stores the password
        as an MD5 hash.  The default is scram-sha-256.
       

       

        Note that older clients might lack support for the SCRAM authentication
        mechanism, and hence not work with passwords encrypted with
        SCRAM-SHA-256.  See  for more details.
       

       
        

         Support for MD5-encrypted passwords is deprecated and will be removed
         in a future release of PostgreSQL.  Refer
         to  for details about migrating to
         another password type.
        

       
      

     

     

      
scram_iterations (integer)
      
       scram_iterations configuration parameter
      
      

      

       

        The number of computational iterations to be performed when encrypting
        a password using SCRAM-SHA-256. The default is 4096.
        A higher number of iterations provides additional protection against
        brute-force attacks on stored passwords, but makes authentication
        slower. Changing the value has no effect on existing passwords
        encrypted with SCRAM-SHA-256 as the iteration count is fixed at the
        time of encryption. In order to make use of a changed value, a new
        password must be set.
       

       
        

         If a role password was created with a different iteration count than
         the value of scram_iterations specified in the
         postgresql.conf file or on the server command
         line, an unauthenticated user can discern the existence of the role by
         observing discrepancies in the server's responses to connection
         attempts.  If you find this concerning, ensure that all role passwords
         are created with scram_iterations set to the value
         specified in the postgresql.conf file or on the
         server command line.
        

       
      

     

     

      
password_expiration_warning_threshold (integer)
      
       password_expiration_warning_threshold configuration parameter
      
      

      

       

        When this parameter is greater than zero, the server will emit a
        WARNING upon successful password authentication if
        less than this amount of time remains until the authenticated role's
        password expires.  Note that a role's password only expires if a date
        was specified in a VALID UNTIL clause for
        CREATE ROLE or ALTER ROLE.  If
        this value is specified without units, it is taken as seconds.  The
        default is 7 days.  This parameter can only be set in the
        postgresql.conf file or on the server command
        line.
       

      

     

     

      
md5_password_warnings (boolean)
      
       md5_password_warnings configuration parameter
      
      

      

       

        Controls whether a WARNING about MD5 password
        deprecation is produced upon successful authentication using an
        MD5-encrypted password or
        when a CREATE ROLE or
        ALTER ROLE statement sets an MD5-encrypted password.
        The default value is on.
       

      

     

     

      
krb_server_keyfile (string)
      
       krb_server_keyfile configuration parameter
      
      

      

       

        Sets the location of the server's Kerberos key file.  The default is
        FILE:/usr/local/pgsql/etc/krb5.keytab
        (where the directory part is whatever was specified
        as sysconfdir at build time; use
        pg_config --sysconfdir to determine that).
        If this parameter is set to an empty string, it is ignored and a
        system-dependent default is used.
        This parameter can only be set in the
        postgresql.conf file or on the server command line.
        See  for more information.
       

      

     

     

      
krb_caseins_users (boolean)
      
       krb_caseins_users configuration parameter
      
      

      

       

        Sets whether GSSAPI user names should be treated
        case-insensitively.
        The default is off (case sensitive). This parameter can only be
        set in the postgresql.conf file or on the server command line.
       

      

     

     

      
gss_accept_delegation (boolean)
      
       gss_accept_delegation configuration parameter
      
      

      

       

        Sets whether GSSAPI delegation should be accepted from the client.
        The default is off meaning credentials from the client will
        not be accepted.  Changing this to on will make the server
        accept credentials delegated to it from the client. This parameter can only be
        set in the postgresql.conf file or on the server command line.
       

      

     

     

      
oauth_validator_libraries (string)
      
       oauth_validator_libraries configuration parameter
      
      

      

       

        Sets the library/libraries to use for validating OAuth connection tokens. If
        only one validator library is provided, it will be used by default for
        any OAuth connections; otherwise, all
        oauth HBA entries
        must explicitly set a validator chosen from this
        list. If set to an empty string (the default), OAuth connections will be
        refused. This parameter can only be set in the
        postgresql.conf file.
       

       

        Validator modules must be implemented/obtained separately;
        PostgreSQL does not ship with any default
        implementations. For more information on implementing OAuth validators,
        see .
       

      

     

     

     

     

     
# SSL

     

      See  for more information about setting up
      SSL. The configuration parameters for controlling
      transfer encryption using TLS protocols are named
      ssl for historic reasons, even though support for
      the SSL protocol has been deprecated.
      SSL is in this context used interchangeably with
      TLS.
     

     

     

      
ssl (boolean)
      
       ssl configuration parameter
      
      

      

       

        Enables SSL connections.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
        The default is off.
       

      

     

     

      
ssl_ca_file (string)
      
       ssl_ca_file configuration parameter
      
      

      

       

        Specifies the name of the file containing the SSL server certificate
        authority (CA).
        Relative paths are relative to the data directory.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
        The default is empty, meaning no CA file is loaded,
        and client certificate verification is not performed.
       

      

     

     

      
ssl_cert_file (string)
      
       ssl_cert_file configuration parameter
      
      

      

       

        Specifies the name of the file containing the SSL server certificate.
        Relative paths are relative to the data directory.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
        The default is server.crt.
       

      

     

     

      
ssl_crl_file (string)
      
       ssl_crl_file configuration parameter
      
      

      

       

        Specifies the name of the file containing the SSL client certificate
        revocation list (CRL).
        Relative paths are relative to the data directory.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
        The default is empty, meaning no CRL file is loaded (unless
         is set).
       

      

     

     

      
ssl_crl_dir (string)
      
       ssl_crl_dir configuration parameter
      
      

      

       

        Specifies the name of the directory containing the SSL client
        certificate revocation list (CRL).  Relative paths are relative to the
        data directory.  This parameter can only be set in
        the postgresql.conf file or on the server command
        line.  The default is empty, meaning no CRLs are used (unless
         is set).
       

       

        The directory needs to be prepared with the
        OpenSSL command
        openssl rehash or c_rehash.  See
        its documentation for details.
       

       

        When using this setting, CRLs in the specified directory are loaded
        on-demand at connection time.  New CRLs can be added to the directory
        and will be used immediately.  This is unlike , which causes the CRL in the file to be
        loaded at server start time or when the configuration is reloaded.
        Both settings can be used together.
       

      

     

     

      
ssl_key_file (string)
      
       ssl_key_file configuration parameter
      
      

      

       

        Specifies the name of the file containing the SSL server private key.
        Relative paths are relative to the data directory.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
        The default is server.key.
       

      

     

     

      
ssl_tls13_ciphers (string)
      
       ssl_tls13_ciphers configuration parameter
      
      

      

       

        Specifies a list of cipher suites that are allowed by connections using
        TLS version 1.3.  Multiple cipher suites can be
        specified by using a colon-separated list. If left blank, the default
        set of cipher suites in OpenSSL will be used.
       

       

        This parameter can only be set in the
        postgresql.conf file or on the server command
        line.
       

      

     

     

      
ssl_ciphers (string)
      
       ssl_ciphers configuration parameter
      
      

      

       

        Specifies a list of SSL ciphers that are allowed by
        connections using TLS version 1.2 and lower, see
         for TLS version 1.3 connections. See
        the ciphers
        manual page in the OpenSSL package for the
        syntax of this setting and a list of supported values.  The default value
        is HIGH:MEDIUM:+3DES:!aNULL.  The default is usually a
        reasonable choice unless you have specific security requirements.
       

       

        This parameter can only be set in the
        postgresql.conf file or on the server command
        line.
       

       

        Explanation of the default value:
        

         

          
HIGH

          

           

            Cipher suites that use ciphers from HIGH group (e.g.,
            AES, Camellia, 3DES)
           

          

         

         

          
MEDIUM

          

           

            Cipher suites that use ciphers from MEDIUM group
            (e.g., RC4, SEED)
           

          

         

         

          
+3DES

          

           

            The OpenSSL default order for
            HIGH is problematic because it orders 3DES
            higher than AES128.  This is wrong because 3DES offers less
            security than AES128, and it is also much slower.
            +3DES reorders it after all other
            HIGH and MEDIUM ciphers.
           

          

         

         

          
!aNULL

          

           

            Disables anonymous cipher suites that do no authentication.  Such
            cipher suites are vulnerable to MITM attacks and
            therefore should not be used.
           

          

         

        

       

       

        Available cipher suite details will vary across
        OpenSSL versions.  Use the command
        openssl ciphers -v 'HIGH:MEDIUM:+3DES:!aNULL' to
        see actual details for the currently installed
        OpenSSL version.  Note that this list is
        filtered at run time based on the server key type.
       

      

     

     

      
ssl_prefer_server_ciphers (boolean)
      
       ssl_prefer_server_ciphers configuration parameter
      
      

      

       

        Specifies whether to use the server's SSL cipher preferences, rather
        than the client's.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
        The default is on.
       

       

        PostgreSQL versions before 9.4 do not have
        this setting and always use the client's preferences.  This setting is
        mainly for backward compatibility with those versions.  Using the
        server's preferences is usually better because it is more likely that
        the server is appropriately configured.
       

      

     

     

      
ssl_groups (string)
      
       ssl_groups configuration parameter
      
      

      

       

        Specifies the named group to use for TLS key
        exchange.  It needs to be supported by all clients that
        connect.  Multiple groups can be specified by using a colon-separated
        list.  It does not need to match the key type used by the server
        certificate.  This parameter can only be set in the
        postgresql.conf file or on the server command line.
        The default is X25519:prime256v1.
       

       
        

         X25519 is not allowed when
         OpenSSL is configured for FIPS mode and
         must be removed from the server configuration when FIPS mode is
         enabled.
        

       

       

        OpenSSL names for the most common groups
        are:
        prime256v1 (NIST P-256),
        secp384r1 (NIST P-384),
        secp521r1 (NIST P-521).
        An incomplete list of available groups can be shown with the command
        openssl ecparam -list_curves.  Not all of them are
        usable with TLS though, and many supported group
        names and aliases are omitted.
       

       

        In PostgreSQL versions before 18.0 this
        setting was named ssl_ecdh_curve and only accepted
        a single value.
       

      

     

     

      
ssl_min_protocol_version (enum)
      
       ssl_min_protocol_version configuration parameter
      
      

      

       

        Sets the minimum SSL/TLS protocol version to use.  Valid values are
        currently: TLSv1, TLSv1.1,
        TLSv1.2, TLSv1.3.  Older
        versions of the OpenSSL library do not
        support all values; an error will be raised if an unsupported setting
        is chosen.  Protocol versions before TLS 1.0, namely SSL version 2 and
        3, are always disabled.
       

       

        The default is TLSv1.2, which satisfies industry
        best practices as of this writing.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
ssl_max_protocol_version (enum)
      
       ssl_max_protocol_version configuration parameter
      
      

      

       

        Sets the maximum SSL/TLS protocol version to use.  Valid values are as
        for , with addition of
        an empty string, which allows any protocol version.  The default is to
        allow any version.  Setting the maximum protocol version is mainly
        useful for testing or if some component has issues working with a
        newer protocol.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
ssl_dh_params_file (string)
      
       ssl_dh_params_file configuration parameter
      
      

      

       

        Specifies the name of the file containing Diffie-Hellman parameters
        used for so-called ephemeral DH family of SSL ciphers. The default is
        empty, in which case compiled-in default DH parameters used. Using
        custom DH parameters reduces the exposure if an attacker manages to
        crack the well-known compiled-in DH parameters. You can create your own
        DH parameters file with the command
        openssl dhparam -out dhparams.pem 2048.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
ssl_passphrase_command (string)
      
       ssl_passphrase_command configuration parameter
      
      

      

       

        Sets an external command to be invoked when a passphrase for
        decrypting an SSL file such as a private key needs to be obtained.  By
        default, this parameter is empty, which means the built-in prompting
        mechanism is used.
       

       

        The command must print the passphrase to the standard output and exit
        with code 0.  In the parameter value, %p is
        replaced by a prompt string.  (Write %% for a
        literal %.)  Note that the prompt string will
        probably contain whitespace, so be sure to quote adequately.  A single
        newline is stripped from the end of the output if present.
       

       

        The command does not actually have to prompt the user for a
        passphrase.  It can read it from a file, obtain it from a keychain
        facility, or similar.  It is up to the user to make sure the chosen
        mechanism is adequately secure.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
ssl_passphrase_command_supports_reload (boolean)
      
       ssl_passphrase_command_supports_reload configuration parameter
      
      

      

       

        This parameter determines whether the passphrase command set by
        ssl_passphrase_command will also be called during a
        configuration reload if a key file needs a passphrase.  If this
        parameter is off (the default), then
        ssl_passphrase_command will be ignored during a
        reload and the SSL configuration will not be reloaded if a passphrase
        is needed.  That setting is appropriate for a command that requires a
        TTY for prompting, which might not be available when the server is
        running.  Setting this parameter to on might be appropriate if the
        passphrase is obtained from a file, for example.
       

       

        This parameter must be set to on when running on
        Windows since all connections
        will perform a configuration reload due to the different process model
        of that platform.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
ssl_sni (boolean)
      
       ssl_sni configuration parameter
      
      

      

       

        Enables SNI configuration for SSL connections.  When set to on
        host configuration from  is used, see
         for more details.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.  The default is off.
       

      

     

    

    

   

   

    
# Resource Consumption

    

     
# Memory

     

     

      
shared_buffers (integer)
      
       shared_buffers configuration parameter
      
      

      

       

        Sets the amount of memory the database server uses for shared
        memory buffers.  The default is typically 128 megabytes
        (128MB), but might be less if your kernel settings will
        not support it (as determined during initdb).
        This setting must be at least 128 kilobytes.  However,
        settings significantly higher than the minimum are usually needed
        for good performance.
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        (Non-default values of BLCKSZ change the minimum
        value.)
        This parameter can only be set at server start.
       

       

        If you have a dedicated database server with 1GB or more of RAM, a
        reasonable starting value for shared_buffers is 25%
        of the memory in your system.  There are some workloads where even
        larger settings for shared_buffers are effective, but
        because PostgreSQL also relies on the
        operating system cache, it is unlikely that an allocation of more than
        40% of RAM to shared_buffers will work better than a
        smaller amount.  Larger settings for shared_buffers
        usually require a corresponding increase in
        max_wal_size, in order to spread out the
        process of writing large quantities of new or changed data over a
        longer period of time.
       

       

        On systems with less than 1GB of RAM, a smaller percentage of RAM is
        appropriate, so as to leave adequate space for the operating system.
       

      

     

     

      
huge_pages (enum)
      
       huge_pages configuration parameter
      
      

      

       

        Controls whether huge pages are requested for the main shared memory
        area. Valid values are try (the default),
        on, and off.
        This parameter can only be set at server start. With
        huge_pages set to try, the
        server will try to request huge pages, but fall back to the default if
        that fails. With on, failure to request huge pages
        will prevent the server from starting up. With off,
        huge pages will not be requested. The actual state of huge pages is
        indicated by the server variable
        .
       

       

        At present, this setting is supported only on Linux and Windows. The
        setting is ignored on other systems when set to
        try.  On Linux, it is only supported when
        shared_memory_type is set to mmap
        (the default).
       

       

        The use of huge pages results in smaller page tables and less CPU time
        spent on memory management, increasing performance. For more details about
        using huge pages on Linux, see .
       

       

        Huge pages are known as large pages on Windows.  To use them, you need to
        assign the user right Lock pages in memory to the Windows user account
        that runs PostgreSQL.
        You can use Windows Group Policy tool (gpedit.msc) to assign the user right
        Lock pages in memory.
        To start the database server on the command prompt as a standalone process,
        not as a Windows service, the command prompt must be run as an administrator or
        User Access Control (UAC) must be disabled. When the UAC is enabled, the normal
        command prompt revokes the user right Lock pages in memory when started.
       

       

        Note that this setting only affects the main shared memory area.
        Operating systems such as Linux, FreeBSD, and Illumos can also use
        huge pages (also known as super pages or
        large pages) automatically for normal memory
        allocation, without an explicit request from
        PostgreSQL. On Linux, this is called
        transparent huge pagestransparent
        huge pages (THP). That feature has been known to
        cause performance degradation with
        PostgreSQL for some users on some Linux
        versions, so its use is currently discouraged (unlike explicit use of
        huge_pages).
       

      

     

     

      
huge_page_size (integer)
      
       huge_page_size configuration parameter
      
      

      

       

        Controls the size of huge pages, when they are enabled with
        .
        The default is zero (0).
        When set to 0, the default huge page size on the
        system will be used. This parameter can only be set at server start.
       

       

        Some commonly available page sizes on modern 64 bit server architectures include:
        2MB and 1GB (Intel and AMD), 16MB and
        16GB (IBM POWER), and 64kB, 2MB,
        32MB and 1GB (ARM). For more information
        about usage and support, see .
       

       

        Non-default settings are currently supported only on Linux.
       

      

     

     

      
temp_buffers (integer)
      
       temp_buffers configuration parameter
      
      

      

       

        Sets the maximum amount of memory used for temporary buffers within
        each database session.  These are session-local buffers used only
        for access to temporary tables.
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default is eight megabytes (8MB).
        (If BLCKSZ is not 8kB, the default value scales
        proportionally to it.)
        This setting can be changed within individual
        sessions, but only before the first use of temporary tables
        within the session; subsequent attempts to change the value will
        have no effect on that session.
       

       

        A session will allocate temporary buffers as needed up to the limit
        given by temp_buffers.  The cost of setting a large
        value in sessions that do not actually need many temporary
        buffers is only a buffer descriptor, or about 64 bytes, per
        increment in temp_buffers.  However if a buffer is
        actually used an additional 8192 bytes will be consumed for it
        (or in general, BLCKSZ bytes).
       

      

     

     

      
max_prepared_transactions (integer)
      
       max_prepared_transactions configuration parameter
      
      

      

       

        Sets the maximum number of transactions that can be in the
        prepared state simultaneously (see ).
        Setting this parameter to zero (which is the default)
        disables the prepared-transaction feature.
        This parameter can only be set at server start.
       

       

        If you are not planning to use prepared transactions, this parameter
        should be set to zero to prevent accidental creation of prepared
        transactions.  If you are using prepared transactions, you will
        probably want max_prepared_transactions to be at
        least as large as , so that every
        session can have a prepared transaction pending.
       

       

        When running a standby server, you must set this parameter to the
        same or higher value than on the primary server. Otherwise, queries
        will not be allowed on the standby server.
       

      

     

     

      
work_mem (integer)
      
       work_mem configuration parameter
      
      

      

       

        Sets the base maximum amount of memory to be used by a query operation
        (such as a sort or hash table) before writing to temporary disk files.
        If this value is specified without units, it is taken as kilobytes.
        The default value is four megabytes (4MB).
        Note that a complex query might perform several sort and hash
        operations at the same time, with each operation generally being
        allowed to use as much memory as this value specifies before
        it starts
        to write data into temporary files.  Also, several running
        sessions could be doing such operations concurrently.
        Therefore, the total memory used could be many times the value
        of work_mem; it is necessary to keep this
        fact in mind when choosing the value.  Sort operations are used
        for ORDER BY, DISTINCT,
        and merge joins.
        Hash tables are used in hash joins, hash-based aggregation, memoize
        nodes and hash-based processing of IN subqueries.
       

       

        Hash-based operations are generally more sensitive to memory
        availability than equivalent sort-based operations.  The
        memory limit for a hash table is computed by multiplying
        work_mem by
        hash_mem_multiplier.  This makes it
        possible for hash-based operations to use an amount of memory
        that exceeds the usual work_mem base
        amount.
       

      

     

     

      
hash_mem_multiplier (floating point)
      
       hash_mem_multiplier configuration parameter
      
      

      

       

        Used to compute the maximum amount of memory that hash-based
        operations can use.  The final limit is determined by
        multiplying work_mem by
        hash_mem_multiplier.  The default value is
        2.0, which makes hash-based operations use twice the usual
        work_mem base amount.
       

       

        Consider increasing hash_mem_multiplier in
        environments where spilling by query operations is a regular
        occurrence, especially when simply increasing
        work_mem results in memory pressure (memory
        pressure typically takes the form of intermittent out of
        memory errors).  The default setting of 2.0 is often effective with
        mixed workloads.  Higher settings in the range of 2.0 - 8.0 or
        more may be effective in environments where
        work_mem has already been increased to 40MB
        or more.
       

      

     

     

      
maintenance_work_mem (integer)
      
       maintenance_work_mem configuration parameter
      
      

      

       

        Specifies the maximum amount of memory to be used by maintenance
        operations, such as VACUUM, CREATE
        INDEX, and ALTER TABLE ADD FOREIGN KEY.
        If this value is specified without units, it is taken as kilobytes.
        It defaults
        to 64 megabytes (64MB).  Since only one of these
        operations can be executed at a time by a database session, and
        an installation normally doesn't have many of them running
        concurrently, it's safe to set this value significantly larger
        than work_mem.  Larger settings might improve
        performance for vacuuming and for restoring database dumps.
       

       

        Note that when autovacuum runs, up to
         times this memory
        may be allocated, so be careful not to set the default value
        too high.  It may be useful to control for this by separately
        setting .
       

      

     

     

      
autovacuum_work_mem (integer)
      
       autovacuum_work_mem configuration parameter
      
      

      

       

        Specifies the maximum amount of memory to be used by each
        autovacuum worker process.
        If this value is specified without units, it is taken as kilobytes.
        It defaults to -1, indicating that
        the value of  should
        be used instead.  The setting has no effect on the behavior of
        VACUUM when run in other contexts.
        This parameter can only be set in the
        postgresql.conf file or on the server command
        line.
       

      

     

     

      

       vacuum_buffer_usage_limit (integer)
       
        vacuum_buffer_usage_limit configuration parameter
       
      

      

       

        Specifies the size of the
        Buffer Access Strategy
        used by the VACUUM and ANALYZE
        commands.  A setting of 0 will allow the operation
        to use any number of shared_buffers.  Otherwise
        valid sizes range from 128 kB to
        16 GB.  If the specified size would exceed 1/8 the
        size of shared_buffers, the size is silently capped
        to that value.  The default value is 2MB.  If
        this value is specified without units, it is taken as kilobytes.  This
        parameter can be set at any time.  It can be overridden for
         and 
        when passing the BUFFER_USAGE_LIMIT option.  Higher
        settings can allow VACUUM and
        ANALYZE to run more quickly, but having too large a
        setting may cause too many other useful pages to be evicted from
        shared buffers.
       

      

     

     

      
logical_decoding_work_mem (integer)
      
       logical_decoding_work_mem configuration parameter
      
      

      

       

        Specifies the maximum amount of memory to be used by logical decoding,
        before some of the decoded changes are written to local disk. This
        limits the amount of memory used by streaming logical replication
        connections. It defaults to 64 megabytes (64MB).
        Since each replication connection only uses a single buffer of this size,
        and an installation normally doesn't have many such connections
        concurrently (as limited by max_wal_senders), it's
        safe to set this value significantly higher than work_mem,
        reducing the amount of decoded changes written to disk.
       

      

     

     

      
commit_timestamp_buffers (integer)
      
       commit_timestamp_buffers configuration parameter
      
      

      

       

        Specifies the amount of memory to use to cache the contents of
        pg_commit_ts (see
        ).
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default value is 0, which requests
        shared_buffers/512 up to 1024 blocks,
        but not fewer than 16 blocks.
        This parameter can only be set at server start.
       

      

     

     

      
multixact_member_buffers (integer)
      
       multixact_member_buffers configuration parameter
      
      

      

       

        Specifies the amount of shared memory to use to cache the contents
        of pg_multixact/members (see
        ).
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default value is 32.
        This parameter can only be set at server start.
       

      

     

     

      
multixact_offset_buffers (integer)
      
       multixact_offset_buffers configuration parameter
      
      

      

       

        Specifies the amount of shared memory to use to cache the contents
        of pg_multixact/offsets (see
        ).
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default value is 16.
        This parameter can only be set at server start.
       

      

     

     

      
notify_buffers (integer)
      
       notify_buffers configuration parameter
      
      

      

       

        Specifies the amount of shared memory to use to cache the contents
        of pg_notify (see
        ).
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default value is 16.
        This parameter can only be set at server start.
       

      

     

     

      
serializable_buffers (integer)
      
       serializable_buffers configuration parameter
      
      

      

       

        Specifies the amount of shared memory to use to cache the contents
        of pg_serial (see
        ).
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default value is 32.
        This parameter can only be set at server start.
       

      

     

     

      
subtransaction_buffers (integer)
      
       subtransaction_buffers configuration parameter
      
      

      

       

        Specifies the amount of shared memory to use to cache the contents
        of pg_subtrans (see
        ).
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default value is 0, which requests
        shared_buffers/512 up to 1024 blocks,
        but not fewer than 16 blocks.
        This parameter can only be set at server start.
       

      

     

     

      
transaction_buffers (integer)
      
       transaction_buffers configuration parameter
      
      

      

       

        Specifies the amount of shared memory to use to cache the contents
        of pg_xact (see
        ).
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default value is 0, which requests
        shared_buffers/512 up to 1024 blocks,
        but not fewer than 16 blocks.
        This parameter can only be set at server start.
       

      

     

     

      
max_stack_depth (integer)
      
       max_stack_depth configuration parameter
      
      

      

       

        Specifies the maximum safe depth of the server's execution stack.
        The ideal setting for this parameter is the actual stack size limit
        enforced by the kernel (as set by ulimit -s or local
        equivalent), less a safety margin of a megabyte or so.  The safety
        margin is needed because the stack depth is not checked in every
        routine in the server, but only in key potentially-recursive routines.
        If this value is specified without units, it is taken as kilobytes.
        The default setting is two megabytes (2MB), which
        is conservatively small and unlikely to risk crashes.  However,
        it might be too small to allow execution of complex functions.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        Setting max_stack_depth higher than
        the actual kernel limit will mean that a runaway recursive function
        can crash an individual backend process.  On platforms where
        PostgreSQL can determine the kernel limit,
        the server will not allow this variable to be set to an unsafe
        value.  However, not all platforms provide the information,
        so caution is recommended in selecting a value.
       

      

     

     

      
shared_memory_type (enum)
      
       shared_memory_type configuration parameter
      
      

      

       

        Specifies the shared memory implementation that the server
        should use for the main shared memory region that holds
        PostgreSQL's shared buffers and other
        shared data.  Possible values are mmap (for
        anonymous shared memory allocated using mmap),
        sysv (for System V shared memory allocated via
        shmget) and windows (for Windows
        shared memory).  Not all values are supported on all platforms; the
        first supported option is the default for that platform.  The use of
        the sysv option, which is not the default on any
        platform, is generally discouraged because it typically requires
        non-default kernel settings to allow for large allocations (see ).
        This parameter can only be set at server start.
       

      

     

     

      
dynamic_shared_memory_type (enum)
      
       dynamic_shared_memory_type configuration parameter
      
      

      

       

        Specifies the dynamic shared memory implementation that the server
        should use.  Possible values are posix (for POSIX shared
        memory allocated using shm_open), sysv
        (for System V shared memory allocated via shmget),
        windows (for Windows shared memory),
        and mmap (to simulate shared memory using
        memory-mapped files stored in the data directory).
        Not all values are supported on all platforms; the first supported
        option is usually the default for that platform.  The use of the
        mmap option, which is not the default on any platform,
        is generally discouraged because the operating system may write
        modified pages back to disk repeatedly, increasing system I/O load;
        however, it may be useful for debugging, when the
        pg_dynshmem directory is stored on a RAM disk, or when
        other shared memory facilities are not available.
        This parameter can only be set at server start.
       

      

     

     

      
min_dynamic_shared_memory (integer)
      
       min_dynamic_shared_memory configuration parameter
      
      

      

       

        Specifies the amount of memory that should be allocated at server
        startup for use by parallel queries.  When this memory region is
        insufficient or exhausted by concurrent queries, new parallel queries
        try to allocate extra shared memory temporarily from the operating
        system using the method configured with
        dynamic_shared_memory_type, which may be slower due
        to memory management overheads.  Memory that is allocated at startup
        with min_dynamic_shared_memory is affected by
        the huge_pages setting on operating systems where
        that is supported, and may be more likely to benefit from larger pages
        on operating systems where that is managed automatically.
        The default value is 0 (none). This parameter can
        only be set at server start.
       

      

     

     

     

     

     
# Disk

     

     

      
temp_file_limit (integer)
      
       temp_file_limit configuration parameter
      
      

      

       

        Specifies the maximum amount of disk space that a process can use
        for temporary files, such as sort and hash temporary files, or the
        storage file for a held cursor.  A transaction attempting to exceed
        this limit will be canceled.
        If this value is specified without units, it is taken as kilobytes.
        -1 (the default) means no limit.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        This setting constrains the total space used at any instant by all
        temporary files used by a given PostgreSQL process.
        It should be noted that disk space used for explicit temporary
        tables, as opposed to temporary files used behind-the-scenes in query
        execution, does not count against this limit.
       

      

     

     

      
file_copy_method (enum)
      
       file_copy_method configuration parameter
      
      

      

       

        Specifies the method used to copy files.
        Possible values are COPY (default) and
        CLONE (if operating support is available).
       

       

       This parameter affects:
       

       

        

        

         CREATE DATABASE ... STRATEGY=FILE_COPY
        

        

        

        

         ALTER DATABASE ... SET TABLESPACE ...
        

        

       

       

        CLONE uses the copy_file_range()
        (Linux, FreeBSD) or copyfile
        (macOS) system calls, giving the kernel the opportunity to share disk
        blocks or push work down to lower layers on some file systems.
       

      

     

     

      
file_extend_method (enum)
      
       file_extend_method configuration parameter
      
      

      

       

        Specifies the method used to extend data files during bulk operations
        such as COPY.  The first available option is used as
        the default, depending on the operating system:
        

         

          

           posix_fallocate (Unix) uses the standard POSIX
            interface for allocating disk space, but is missing on some systems.
            If it is present but the underlying file system doesn't support it,
            this option silently falls back to write_zeros.
            Current versions of BTRFS are known to disable compression when
            this option is used.
            This is the default on systems that have the function.
           

         

         

          

           write_zeros extends files by writing out blocks
            of zero bytes.  This is the default on systems that don't have the
            function posix_fallocate.
          

         

        

        The write_zeros method is always used when data
        files are extended by 8 blocks or fewer.
       

      

     

     

      
max_notify_queue_pages (integer)
      
       max_notify_queue_pages configuration parameter
      
      

      

       

        Specifies the maximum amount of allocated pages for
         /  queue.
        The default value is 1048576. For 8 KB pages it allows to consume
        up to 8 GB of disk space.
        This parameter can only be set at server start.
       

      

     

     

     

     

     
# Kernel Resource Usage

     

     

      
max_files_per_process (integer)
      
       max_files_per_process configuration parameter
      
      

      

       

        Sets the maximum number of files each server subprocess is
        allowed to have open simultaneously;  files already opened in the
        postmaster are not counted toward this limit. The default is one
        thousand files.
       

       

        If the kernel is enforcing
        a safe per-process limit, you don't need to worry about this setting.
        But on some platforms (notably, most BSD systems), the kernel will
        allow individual processes to open many more files than the system
        can actually support if many processes all try to open
        that many files. If you find yourself seeing Too many open
        files failures, try reducing this setting.
        This parameter can only be set at server start.
       

      

     

     

    

    

     
# Timing

     

      

       
timing_clock_source (enum)
       
        timing_clock_source configuration parameter
       
       

       

        

         Selects the method for making timing measurements using the OS or
         specialized CPU instructions. Possible values are:
         

          

           

            auto (automatically chooses TSC
            clock source on supported x86-64 CPUs, otherwise uses the OS system
            clock)
           

          

          

           

            system (measures timing using the OS system clock)
           

          

          

           

            tsc (measures timing with a CPU instruction, e.g.
            using RDTSC/RDTSCP on x86-64)
           

          

         

         The default is auto. Only superusers can change this
         setting. Changing the setting during query execution is not recommended
         and may cause interval timings to jump significantly or produce negative
         values.
        

        

         
          Time-Stamp Counter
          TSC
         
         TSC
         If enabled, the TSC clock source, named after the
         Time-Stamp Counter on x86-64, will use specialized CPU instructions when
         measuring time intervals. This lowers timing overhead compared to reading
         the OS system clock, and reduces the measurement error on top of the
         actual runtime, for example with EXPLAIN ANALYZE.
        

        

         RDTSC
         On x86-64 CPUs the TSC clock source utilizes the
         RDTSC instruction for EXPLAIN ANALYZE.
         For timings that require higher precision the RDTSCP
         instruction is used, which avoids inaccuracies due to CPU instruction
         re-ordering. Use of the TSC clock source is not
         supported on older x86-64 CPUs and other architectures, and is not
         advised on systems that utilize an emulated TSC, as it
         is likely slower than the system clock source.
        

       

        To help decide which clock source to use you can run the
         utility to check TSC
        availability, and perform timing measurements.
       

       

      

     

    

    

     
# Background Writer

     

      There is a separate server
      process called the background writer, whose function
      is to issue writes of dirty (new or modified) shared
      buffers.  When the number of clean shared buffers appears to be
      insufficient, the background writer writes some dirty buffers to the
      file system and marks them as clean.  This reduces the likelihood
      that server processes handling user queries will be unable to find
      clean buffers and have to write dirty buffers themselves.
      However, the background writer does cause a net overall
      increase in I/O load, because while a repeatedly-dirtied page might
      otherwise be written only once per checkpoint interval, the
      background writer might write it several times as it is dirtied
      in the same interval.  The parameters discussed in this subsection
      can be used to tune the behavior for local needs.
     

     

      

       
bgwriter_delay (integer)
       
        bgwriter_delay configuration parameter
       
       

       

        

         Specifies the delay between activity rounds for the
         background writer.  In each round the writer issues writes
         for some number of dirty buffers (controllable by the
         following parameters).  It then sleeps for
         the length of bgwriter_delay, and repeats.
         When there are no dirty buffers in the
         buffer pool, though, it goes into a longer sleep regardless of
         bgwriter_delay.
         If this value is specified without units, it is taken as milliseconds.
         The default value is 200
         milliseconds (200ms). Note that on some systems, the
         effective resolution of sleep delays is 10 milliseconds; setting
         bgwriter_delay to a value that is not a multiple of 10
         might have the same results as setting it to the next higher multiple
         of 10.  This parameter can only be set in the
         postgresql.conf file or on the server command line.
        

       

      

      

       
bgwriter_lru_maxpages (integer)
       
        bgwriter_lru_maxpages configuration parameter
       
       

       

        

         In each round, no more than this many buffers will be written
         by the background writer.  Setting this to zero disables
         background writing.  (Note that checkpoints, which are managed by
         a separate, dedicated auxiliary process, are unaffected.)
         The default value is 100 buffers.
         This parameter can only be set in the postgresql.conf
         file or on the server command line.
        

       

      

      

       
bgwriter_lru_multiplier (floating point)
       
        bgwriter_lru_multiplier configuration parameter
       
       

       

        

         The number of dirty buffers written in each round is based on the
         number of new buffers that have been needed by server processes
         during recent rounds.  The average recent need is multiplied by
         bgwriter_lru_multiplier to arrive at an estimate of the
         number of buffers that will be needed during the next round.  Dirty
         buffers are written until there are that many clean, reusable buffers
         available.  (However, no more than bgwriter_lru_maxpages
         buffers will be written per round.)
         Thus, a setting of 1.0 represents a just in time policy
         of writing exactly the number of buffers predicted to be needed.
         Larger values provide some cushion against spikes in demand,
         while smaller values intentionally leave writes to be done by
         server processes.
         The default is 2.0.
         This parameter can only be set in the postgresql.conf
         file or on the server command line.
        

       

      

      

       
bgwriter_flush_after (integer)
       
        bgwriter_flush_after configuration parameter
       
       

       

        

         Whenever more than this amount of data has
         been written by the background writer, attempt to force the OS to issue these
         writes to the underlying storage.  Doing so will limit the amount of
         dirty data in the kernel's page cache, reducing the likelihood of
         stalls when an fsync is issued at the end of a checkpoint, or when
         the OS writes data back in larger batches in the background.  Often
         that will result in greatly reduced transaction latency, but there
         also are some cases, especially with workloads that are bigger than
         , but smaller than the OS's page
         cache, where performance might degrade.  This setting may have no
         effect on some platforms.
         If this value is specified without units, it is taken as blocks,
         that is BLCKSZ bytes, typically 8kB.
         The valid range is between
         0, which disables forced writeback, and
         2MB.  The default is 512kB on Linux,
         0 elsewhere.  (If BLCKSZ is not 8kB,
         the default and maximum values scale proportionally to it.)
         This parameter can only be set in the postgresql.conf
         file or on the server command line.
        

       

      

     

     

      Smaller values of bgwriter_lru_maxpages and
      bgwriter_lru_multiplier reduce the extra I/O load
      caused by the background writer, but make it more likely that server
      processes will have to issue writes for themselves, delaying interactive
      queries.
     

    

    

     
# I/O

     

      

       
backend_flush_after (integer)
       
        backend_flush_after configuration parameter
       
       

       

        

         Whenever more than this amount of data has
         been written by a single backend, attempt to force the OS to issue
         these writes to the underlying storage.  Doing so will limit the
         amount of dirty data in the kernel's page cache, reducing the
         likelihood of stalls when an fsync is issued at the end of a
         checkpoint, or when the OS writes data back in larger batches in the
         background.  Often that will result in greatly reduced transaction
         latency, but there also are some cases, especially with workloads
         that are bigger than , but smaller
         than the OS's page cache, where performance might degrade.  This
         setting may have no effect on some platforms.
         If this value is specified without units, it is taken as blocks,
         that is BLCKSZ bytes, typically 8kB.
         The valid range is
         between 0, which disables forced writeback,
         and 2MB.  The default is 0, i.e., no
         forced writeback.  (If BLCKSZ is not 8kB,
         the maximum value scales proportionally to it.)
        

       

      

      

       
effective_io_concurrency (integer)
       
        effective_io_concurrency configuration parameter
       
       

       

        

         Sets the number of concurrent storage I/O operations that
         PostgreSQL expects can be executed
         simultaneously.  Raising this value will increase the number of I/O
         operations that any individual PostgreSQL
         session attempts to initiate in parallel.  The allowed range is
         1 to 1000, or
         0 to disable issuance of asynchronous I/O requests.
         The default is 16.
        

        

         Higher values will have the most impact on higher latency storage
         where queries otherwise experience noticeable I/O stalls and on
         devices with high IOPs. Unnecessarily high values may increase I/O
         latency for all queries on the system.
        

        

         On systems with prefetch advice support,
         effective_io_concurrency also controls the
         prefetch distance.
        

        

         This value can be overridden for tables in a particular tablespace by
         setting the tablespace parameter of the same name (see ).
        

       

      

      

       
maintenance_io_concurrency (integer)
       
        maintenance_io_concurrency configuration parameter
       
       

       

        

         Similar to effective_io_concurrency, but used
         for maintenance work that is done on behalf of many client sessions.
        

        

         The default is 16.  This value can be overridden
         for tables in a particular tablespace by setting the tablespace
         parameter of the same name (see ).
        

       

      

      

       
io_max_combine_limit (integer)
       
        io_max_combine_limit configuration parameter
       
       

       

        

         Controls the largest I/O size in operations that combine I/O, and silently
         limits the user-settable parameter io_combine_limit.
         This parameter can only be set at server start.
         If this value is specified without units, it is taken as blocks,
         that is BLCKSZ bytes, typically 8kB.
         The maximum possible size depends on the operating system and block
         size, but is typically 1MB on Unix and 128kB on Windows.
         The default is 128kB.
        

       

      

      

       
io_combine_limit (integer)
       
        io_combine_limit configuration parameter
       
       

       

        

         Controls the largest I/O size in operations that combine I/O.  If set
         higher than the io_max_combine_limit parameter, the
         lower value will silently be used instead, so both may need to be raised
         to increase the I/O size.
         If this value is specified without units, it is taken as blocks,
         that is BLCKSZ bytes, typically 8kB.
         The maximum possible size depends on the operating system and block
         size, but is typically 1MB on Unix and 128kB on Windows.
         The default is 128kB.
        

       

      

      

       
io_max_concurrency (integer)
       
        io_max_concurrency configuration parameter
       
       

       

        

         Controls the maximum number of I/O operations that one process can
         execute simultaneously.
        

        

         The default setting of -1 selects a number based
         on  and the maximum number of
         processes (, ,  and ), but not more than
         64.
        

        

         This parameter can only be set at server start.
        

       

      

      

       
io_method (enum)
       
        io_method configuration parameter
       
       

       

        

         Selects the method for executing asynchronous I/O.
         Possible values are:
         

          

           

            worker (execute asynchronous I/O using worker processes)
           

          

          

           

            io_uring (execute asynchronous I/O using
            io_uring, requires a build with
            --with-liburing /
            -Dliburing)
           

          

          

           

            sync (execute asynchronous-eligible I/O synchronously)
           

          

         

         The default is worker.
        

        

         This parameter can only be set at server start.
        

       

      

      

       
io_min_workers (integer)
       
        io_min_workers configuration parameter
       
       

       

        

         Sets the minimum number of I/O worker processes. The default is
         2. This parameter can only be set in the
         postgresql.conf file or on the server command
         line.
        

        

         Only has an effect if  is set to
         worker.
        

       

      

      

       
io_max_workers (integer)
       
        io_max_workers configuration parameter
       
       

       

        

         Sets the maximum number of I/O worker processes. The default is
         8. This parameter can only be set in the
         postgresql.conf file or on the server command
         line.
        

        

         Only has an effect if  is set to
         worker.
        

       

      

      

       
io_worker_idle_timeout (integer)
       
        io_worker_idle_timeout configuration parameter
       
       

       

        

         Sets the time after which entirely idle I/O worker processes exit, reducing the
         size of pool to match demand.  The default is 1 minute.  This
         parameter can only be set in the
         postgresql.conf file or on the server command
         line.
        

        

         Only has an effect if  is set to
         worker.
        

       

      

      

       
io_worker_launch_interval (integer)
       
        io_worker_launch_interval configuration parameter
       
       

       

        

         Sets the minimum time before another I/O worker can be launched.  This avoids
         creating too many for an unsustained burst of activity.  The default is 100ms.
         This parameter can only be set in the
         postgresql.conf file or on the server command
         line.
        

        

         Only has an effect if  is set to
         worker.
        

       

      

     

    

    

     
# Worker Processes

     

      

       
max_worker_processes (integer)
       
        max_worker_processes configuration parameter
       
       

       

        

         Sets the maximum number of background processes that the cluster
         can support.  This parameter can only be set at server start.  The
         default is 8.
        

        

         When running a standby server, you must set this parameter to the
         same or higher value than on the primary server. Otherwise, queries
         will not be allowed on the standby server.
        

        

         When changing this value, consider also adjusting
         ,
         ,
         , and
         .
        

       

      

      

       
max_parallel_workers_per_gather (integer)
       
        max_parallel_workers_per_gather configuration parameter
       
       

       

        

         Sets the maximum number of workers that can be started by a single
         Gather or Gather Merge node.
         Parallel workers are taken from the pool of processes established by
         , limited by
         .  Note that the requested
         number of workers may not actually be available at run time.  If this
         occurs, the plan will run with fewer workers than expected, which may
         be inefficient.  The default value is 2.  Setting this value to 0
         disables parallel query execution.
        

        

         Note that parallel queries may consume very substantially more
         resources than non-parallel queries, because each worker process is
         a completely separate process which has roughly the same impact on the
         system as an additional user session.  This should be taken into
         account when choosing a value for this setting, as well as when
         configuring other settings that control resource utilization, such
         as .  Resource limits such as
         work_mem are applied individually to each worker,
         which means the total utilization may be much higher across all
         processes than it would normally be for any single process.
         For example, a parallel query using 4 workers may use up to 5 times
         as much CPU time, memory, I/O bandwidth, and so forth as a query which
         uses no workers at all.
        

        

         For more information on parallel query, see
         .
        

       

      

      

       
max_parallel_maintenance_workers (integer)
       
        max_parallel_maintenance_workers configuration parameter
       
       

       

        

         Sets the maximum number of parallel workers that can be
         started by a single utility command.  Currently, the parallel
         utility commands that support the use of parallel workers are
         CREATE INDEX when building a B-tree,
         GIN, or BRIN index,
         and VACUUM without FULL
         option.  Parallel workers are taken from the pool of processes
         established by , limited
         by .  Note that the requested
         number of workers may not actually be available at run time.
         If this occurs, the utility operation will run with fewer
         workers than expected.  The default value is 2.  Setting this
         value to 0 disables the use of parallel workers by utility
         commands.
        

        

         Note that parallel utility commands should not consume
         substantially more memory than equivalent non-parallel
         operations.  This strategy differs from that of parallel
         query, where resource limits generally apply per worker
         process.  Parallel utility commands treat the resource limit
         maintenance_work_mem as a limit to be applied to
         the entire utility command, regardless of the number of
         parallel worker processes.  However, parallel utility
         commands may still consume substantially more CPU resources
         and I/O bandwidth.
        

       

      

      

       
max_parallel_workers (integer)
       
        max_parallel_workers configuration parameter
       
       

       

        

         Sets the maximum number of workers that the cluster can support for
         parallel operations.  The default value is 8.  When increasing or
         decreasing this value, consider also adjusting
          and
         .
         Also, note that a setting for this value which is higher than
          will have no effect,
         since parallel workers are taken from the pool of worker processes
         established by that setting.
        

       

      

      

       

       parallel_leader_participation (boolean)
       
        parallel_leader_participation configuration parameter
       
       

       

        

         Allows the leader process to execute the query plan under
         Gather and Gather Merge nodes
         instead of waiting for worker processes.  The default is
         on.  Setting this value to off
         reduces the likelihood that workers will become blocked because the
         leader is not reading tuples fast enough, but requires the leader
         process to wait for worker processes to start up before the first
         tuples can be produced.  The degree to which the leader can help or
         hinder performance depends on the plan type, number of workers and
         query duration.
        

       

      

     

    

   

   

    
# Write Ahead Log

   

    For additional information on tuning these settings,
    see .
   

    

     
# Settings

     

     

      
wal_level (enum)
      
       wal_level configuration parameter
      
      

      

       

        wal_level determines how much information is written to
        the WAL. The default value is replica, which writes enough
        data to support WAL archiving and replication, including running
        read-only queries on a standby server. minimal removes all
        logging except the information required to recover from a crash or
        immediate shutdown.  Finally,
        logical adds information necessary to support logical
        decoding.  Each level includes the information logged at all lower
        levels.  This parameter can only be set at server start.
       

       

        The minimal level generates the least WAL
        volume.  It logs no row information for permanent relations
        in transactions that create or
        rewrite them.  This can make operations much faster (see
        ).  Operations that initiate this
        optimization include:
        
         ALTER ... SET TABLESPACE
         CLUSTER
         CREATE TABLE
         REFRESH MATERIALIZED VIEW
         (without CONCURRENTLY)
         REINDEX
         TRUNCATE
        
        However, minimal WAL does not contain sufficient information for
        point-in-time recovery, so replica or
        higher must be used to enable continuous archiving
        () and streaming binary replication.
        In fact, the server will not even start in this mode if
        max_wal_senders is non-zero.
        Note that changing wal_level to
        minimal makes previous base backups unusable
        for point-in-time recovery and standby servers.
       

       

        In logical level, the same information is logged as
        with replica, plus information needed to
        extract logical change sets from the WAL. Using a level of
        logical will increase the WAL volume, particularly if many
        tables are configured for REPLICA IDENTITY FULL and
        many UPDATE and DELETE statements are
        executed.
       

       

        It is important to note that when wal_level is set to
        replica, the effective WAL level can automatically change
        based on the presence of 
        logical replication slots. The system automatically increases the
        effective WAL level to logical when creating the first
        logical replication slot, and decreases it back to replica
        when dropping or invalidating the last logical replication slot. The current
        effective WAL level can be monitored through
         parameter.
       

       

        In releases prior to 9.6, this parameter also allowed the
        values archive and hot_standby.
        These are still accepted but mapped to replica.
       

      

     

     

      
fsync (boolean)
      
       fsync configuration parameter
      
      

      

       

        If this parameter is on, the PostgreSQL server
        will try to make sure that updates are physically written to
        disk, by issuing fsync() system calls or various
        equivalent methods (see ).
        This ensures that the database cluster can recover to a
        consistent state after an operating system or hardware crash.
       

       

        While turning off fsync is often a performance
        benefit, this can result in unrecoverable data corruption in
        the event of a power failure or system crash.  Thus it
        is only advisable to turn off fsync if
        you can easily recreate your entire database from external
        data.
       

       

        Examples of safe circumstances for turning off
        fsync include the initial loading of a new
        database cluster from a backup file, using a database cluster
        for processing a batch of data after which the database
        will be thrown away and recreated,
        or for a read-only database clone which
        gets recreated frequently and is not used for failover.  High
        quality hardware alone is not a sufficient justification for
        turning off fsync.
       

       

        For reliable recovery when changing fsync
        off to on, it is necessary to force all modified buffers in the
        kernel to durable storage.  This can be done while the cluster
        is shutdown or while fsync is on by running initdb
        --sync-only, running sync, unmounting the
        file system, or rebooting the server.
       

       

        In many situations, turning off 
        for noncritical transactions can provide much of the potential
        performance benefit of turning off fsync, without
        the attendant risks of data corruption.
       

       

        fsync can only be set in the postgresql.conf
        file or on the server command line.
        If you turn this parameter off, also consider turning off
        .
       

      

     

     

      
synchronous_commit (enum)
      
       synchronous_commit configuration parameter
      
      

      

       

        Specifies how much WAL processing must complete before
        the database server returns a success
        indication to the client.  Valid values are
        remote_apply, on
        (the default), remote_write,
        local, and off.
       

       

        If synchronous_standby_names is empty,
        the only meaningful settings are on and
        off;  remote_apply,
        remote_write and local
        all provide the same local synchronization level
        as on.  The local behavior of all
        non-off modes is to wait for local flush of WAL
        to disk.  In off mode, there is no waiting,
        so there can be a delay between when success is reported to the
        client and when the transaction is later guaranteed to be safe
        against a server crash.  (The maximum
        delay is three times .)  Unlike
        , setting this parameter to off
        does not create any risk of database inconsistency: an operating
        system or database crash might
        result in some recent allegedly-committed transactions being lost, but
        the database state will be just the same as if those transactions had
        been aborted cleanly.  So, turning synchronous_commit off
        can be a useful alternative when performance is more important than
        exact certainty about the durability of a transaction.  For more
        discussion see .
       

       

        If  is non-empty,
        synchronous_commit also controls whether
        transaction commits will wait for their WAL records to be
        processed on the standby server(s).
       

       

        When set to remote_apply, commits will wait
        until replies from the current synchronous standby(s) indicate they
        have received the commit record of the transaction and applied
        it, so that it has become visible to queries on the standby(s),
        and also written to durable storage on the standbys.  This will
        cause much larger commit delays than previous settings since
        it waits for WAL replay.  When set to on,
        commits wait until replies
        from the current synchronous standby(s) indicate they have received
        the commit record of the transaction and flushed it to durable storage.  This
        ensures the transaction will not be lost unless both the primary and
        all synchronous standbys suffer corruption of their database storage.
        When set to remote_write, commits will wait until replies
        from the current synchronous standby(s) indicate they have
        received the commit record of the transaction and written it to
        their file systems. This setting ensures data preservation if a standby instance of
        PostgreSQL crashes, but not if the standby
        suffers an operating-system-level crash because the data has not
        necessarily reached durable storage on the standby.
        The setting local causes commits to wait for
        local flush to disk, but not for replication.  This is usually not
        desirable when synchronous replication is in use, but is provided for
        completeness.
       

       

        This parameter can be changed at any time; the behavior for any
        one transaction is determined by the setting in effect when it
        commits.  It is therefore possible, and useful, to have some
        transactions commit synchronously and others asynchronously.
        For example, to make a single multistatement transaction commit
        asynchronously when the default is the opposite, issue SET
        LOCAL synchronous_commit TO OFF within the transaction.
       

       

         summarizes the
        capabilities of the synchronous_commit settings.
       

       
        
# synchronous_commit Modes

        
         
         
         
         
         
         
          
           synchronous_commit setting
           local durable commit
           standby durable commit after PG crash
           standby durable commit after OS crash
           standby query consistency
          
         

         

          
           remote_apply
           &bull;
           &bull;
           &bull;
           &bull;
          

          
           on
           &bull;
           &bull;
           &bull;
           
          

          
           remote_write
           &bull;
           &bull;
           
           
          

          
           local
           &bull;
           
           
           
          

          
           off
           
           
           
           
          

         
        
       

      

     

     

      
wal_sync_method (enum)
      
       wal_sync_method configuration parameter
      
      

      

       

        Method used for forcing WAL updates out to disk.
        If fsync is off then this setting is irrelevant,
        since WAL file updates will not be forced out at all.
        Possible values are:
       

       

        

        

         open_datasync (write WAL files with open() option O_DSYNC)
        

        

        

        

         fdatasync (call fdatasync() at each commit)
        

        

        

        

         fsync (call fsync() at each commit)
        

        

        

        

         fsync_writethrough (call fsync() at each commit, forcing write-through of any disk write cache)
        

        

        

        

         open_sync (write WAL files with open() option O_SYNC)
        

        

       

       

        Not all of these choices are available on all platforms.
        The default is the first method in the above list that is supported
        by the platform, except that fdatasync is the default on
        Linux and FreeBSD.  The default is not necessarily ideal; it might be
        necessary to change this setting or other aspects of your system
        configuration in order to create a crash-safe configuration or
        achieve optimal performance.
        These aspects are discussed in .
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
full_page_writes (boolean)
      
       full_page_writes configuration parameter
      
      

      

       

        When this parameter is on, the PostgreSQL server
        writes the entire content of each disk page to WAL during the
        first modification of that page after a checkpoint.
        This is needed because
        a page write that is in process during an operating system crash might
        be only partially completed, leading to an on-disk page
        that contains a mix of old and new data.  The row-level change data
        normally stored in WAL will not be enough to completely restore
        such a page during post-crash recovery.  Storing the full page image
        guarantees that the page can be correctly restored, but at the price
        of increasing the amount of data that must be written to WAL.
        (Because WAL replay always starts from a checkpoint, it is sufficient
        to do this during the first change of each page after a checkpoint.
        Therefore, one way to reduce the cost of full-page writes is to
        increase the checkpoint interval parameters.)
       

       

        Turning this parameter off speeds normal operation, but
        might lead to either unrecoverable data corruption, or silent
        data corruption, after a system failure. The risks are similar to turning off
        fsync, though smaller, and it should be turned off
        only based on the same circumstances recommended for that parameter.
       

       

        Turning off this parameter does not affect use of
        WAL archiving for point-in-time recovery (PITR)
        (see ).
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
        The default is on.
       

      

     

     

      
wal_log_hints (boolean)
      
       wal_log_hints configuration parameter
      
      

      

       

        When this parameter is on, the PostgreSQL
        server writes the entire content of each disk page to WAL during the
        first modification of that page after a checkpoint, even for
        non-critical modifications of so-called hint bits.
       

       

        If data checksums are enabled, hint bit updates are always WAL-logged
        and this setting is ignored. You can use this setting to test how much
        extra WAL-logging would occur if your database had data checksums
        enabled.
       

       

        This parameter can only be set at server start. The default value is off.
       

      

     

     

      
wal_compression (enum)
      
       wal_compression configuration parameter
      
      

      

       

        This parameter enables compression of WAL using the specified
        compression method.
        When enabled, the PostgreSQL
        server compresses full page images written to WAL (e.g. when
         is on, during a base backup,
        etc.).
        A compressed page image will be decompressed during WAL replay.
        The supported methods are off, on,
        zstd (if PostgreSQL was compiled with --with-zstd),
        lz4 (if PostgreSQL was compiled with --with-lz4), and
        pglz.
        The value on selects the first of zstd,
        lz4, pglz that is available.
        The default value is off.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        Enabling compression can reduce the WAL volume without
        increasing the risk of unrecoverable data corruption,
        but at the cost of some extra CPU spent on the compression during
        WAL logging and on the decompression during WAL replay.
       

      

     

     

      
wal_init_zero (boolean)
      
       wal_init_zero configuration parameter
      
      

      

       

        If set to on (the default), this option causes new
        WAL files to be filled with zeroes.  On some file systems, this ensures
        that space is allocated before we need to write WAL records.  However,
        Copy-On-Write (COW) file systems may not benefit
        from this technique, so the option is given to skip the unnecessary
        work.  If set to off, only the final byte is written
        when the file is created so that it has the expected size.
       

      

     

     

      
wal_recycle (boolean)
      
       wal_recycle configuration parameter
      
      

      

       

        If set to on (the default), this option causes WAL
        files to be recycled by renaming them, avoiding the need to create new
        ones.  On COW file systems, it may be faster to create new ones, so the
        option is given to disable this behavior.
       

      

     

     

      
wal_buffers (integer)
      
       wal_buffers configuration parameter
      
      

      

       

        The amount of shared memory used for WAL data that has not yet been
        written to disk.  The default setting of -1 selects a size equal to
        1/32nd (about 3%) of , but not less
        than 64kB nor more than the size of one WAL
        segment, typically 16MB.  This value can be set
        manually if the automatic choice is too large or too small,
        but any positive value less than 32kB will be
        treated as 32kB.
        If this value is specified without units, it is taken as WAL blocks,
        that is XLOG_BLCKSZ bytes, typically 8kB.
        This parameter can only be set at server start.
       

       

        The contents of the WAL buffers are written out to disk at every
        transaction commit, so extremely large values are unlikely to
        provide a significant benefit.  However, setting this value to at
        least a few megabytes can improve write performance on a busy
        server where many clients are committing at once.  The auto-tuning
        selected by the default setting of -1 should give reasonable
        results in most cases.
       

      

     

     

      
wal_writer_delay (integer)
      
       wal_writer_delay configuration parameter
      
      

      

      

        Specifies how often the WAL writer flushes WAL, in time terms.
        After flushing WAL the writer sleeps for the length of time given
        by wal_writer_delay, unless woken up sooner
        by an asynchronously committing transaction. If the last flush
        happened less than wal_writer_delay ago and less
        than wal_writer_flush_after worth of WAL has been
        produced since, then WAL is only written to the operating system, not
        flushed to disk.
        If this value is specified without units, it is taken as milliseconds.
        The default value is 200 milliseconds (200ms).  Note that
        on some systems, the effective resolution of sleep delays is 10
        milliseconds; setting wal_writer_delay to a value that is
        not a multiple of 10 might have the same results as setting it to the
        next higher multiple of 10. This parameter can only be set in the
        postgresql.conf file or on the server command line.
       

      

     

     

      
wal_writer_flush_after (integer)
      
       wal_writer_flush_after configuration parameter
      
      

      

      

        Specifies how often the WAL writer flushes WAL, in volume terms.
        If the last flush happened less
        than wal_writer_delay ago and less
        than wal_writer_flush_after worth of WAL has been
        produced since, then WAL is only written to the operating system, not
        flushed to disk.  If wal_writer_flush_after is set
        to 0 then WAL data is always flushed immediately.
        If this value is specified without units, it is taken as WAL blocks,
        that is XLOG_BLCKSZ bytes, typically 8kB.
        The default is 1MB.
        This parameter can only be set in the
        postgresql.conf file or on the server command line.
       

      

     

     

      
wal_skip_threshold (integer)
      
       wal_skip_threshold configuration parameter
      
      

      

       

        When wal_level is minimal and a
        transaction commits after creating or rewriting a permanent relation,
        this setting determines how to persist the new data.  If the data is
        smaller than this setting, write it to the WAL log; otherwise, use an
        fsync of affected files.  Depending on the properties of your storage,
        raising or lowering this value might help if such commits are slowing
        concurrent transactions.  If this value is specified without units, it
        is taken as kilobytes.  The default is two megabytes
        (2MB).
       

      

     

     

      
commit_delay (integer)
      
       commit_delay configuration parameter
      
      

      

       

        Setting commit_delay adds a time delay
        before a WAL flush is initiated.  This can improve
        group commit throughput by allowing a larger number of transactions
        to commit via a single WAL flush, if system load is high enough
        that additional transactions become ready to commit within the
        given interval.  However, it also increases latency by up to the
        commit_delay for each WAL
        flush.  Because the delay is just wasted if no other transactions
        become ready to commit, a delay is only performed if at least
        commit_siblings other transactions are active
        when a flush is about to be initiated.  Also, no delays are
        performed if fsync is disabled.
        If this value is specified without units, it is taken as microseconds.
        The default commit_delay is zero (no delay).
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        In PostgreSQL releases prior to 9.3,
        commit_delay behaved differently and was much
        less effective: it affected only commits, rather than all WAL flushes,
        and waited for the entire configured delay even if the WAL flush
        was completed sooner.  Beginning in PostgreSQL 9.3,
        the first process that becomes ready to flush waits for the configured
        interval, while subsequent processes wait only until the leader
        completes the flush operation.
       

      

     

     

      
commit_siblings (integer)
      
       commit_siblings configuration parameter
      
      

      

       

        Minimum number of concurrent open transactions to require
        before performing the commit_delay delay. A larger
        value makes it more probable that at least one other
        transaction will become ready to commit during the delay
        interval. The default is five transactions.
       

      

     

     

     

     

     
# Checkpoints

    

     

      
checkpoint_timeout (integer)
      
       checkpoint_timeout configuration parameter
      
      

      

       

        Maximum time between automatic WAL checkpoints.
        If this value is specified without units, it is taken as seconds.
        The valid range is between 30 seconds and one day.
        The default is five minutes (5min).
        Increasing this parameter can increase the amount of time needed
        for crash recovery.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
checkpoint_completion_target (floating point)
      
       checkpoint_completion_target configuration parameter
      
      

      

       

        Specifies the target of checkpoint completion, as a fraction of
        total time between checkpoints. The default is 0.9, which spreads the
        checkpoint across almost all of the available interval, providing fairly
        consistent I/O load while also leaving some time for checkpoint
        completion overhead.  Reducing this parameter is not recommended because
        it causes the checkpoint to complete faster.  This results in a higher
        rate of I/O during the checkpoint followed by a period of less I/O between
        the checkpoint completion and the next scheduled checkpoint.  This
        parameter can only be set in the postgresql.conf file
        or on the server command line.
       

      

     

     

      
checkpoint_flush_after (integer)
      
       checkpoint_flush_after configuration parameter
      
      

      

       

        Whenever more than this amount of data has been
        written while performing a checkpoint, attempt to force the
        OS to issue these writes to the underlying storage.  Doing so will
        limit the amount of dirty data in the kernel's page cache, reducing
        the likelihood of stalls when an fsync is issued at the end of the
        checkpoint, or when the OS writes data back in larger batches in the
        background.  Often that will result in greatly reduced transaction
        latency, but there also are some cases, especially with workloads
        that are bigger than , but smaller
        than the OS's page cache, where performance might degrade.  This
        setting may have no effect on some platforms.
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The valid range is
        between 0, which disables forced writeback,
        and 2MB.  The default is 256kB on
        Linux, 0 elsewhere.  (If BLCKSZ is not
        8kB, the default and maximum values scale proportionally to it.)
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
checkpoint_warning (integer)
      
       checkpoint_warning configuration parameter
      
      

      

       

        Write a message to the server log if checkpoints caused by
        the filling of WAL segment files happen closer together
        than this amount of time (which suggests that
        max_wal_size ought to be raised).
        If this value is specified without units, it is taken as seconds.
        The default is 30 seconds (30s).
        Zero disables the warning.
        No warnings will be generated if checkpoint_timeout
        is less than checkpoint_warning.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
max_wal_size (integer)
      
       max_wal_size configuration parameter
      
      

      

       

        Maximum size to let the WAL grow during automatic
        checkpoints. This is a soft limit; WAL size can exceed
        max_wal_size under special circumstances, such as
        heavy load, a failing archive_command or archive_library, or a high
        wal_keep_size setting.
        If this value is specified without units, it is taken as megabytes.
        The default is 1 GB.
        Increasing this parameter can increase the amount of time needed for
        crash recovery.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
min_wal_size (integer)
      
       min_wal_size configuration parameter
      
      

      

       

        As long as WAL disk usage stays below this setting, old WAL files are
        always recycled for future use at a checkpoint, rather than removed.
        This can be used to ensure that enough WAL space is reserved to
        handle spikes in WAL usage, for example when running large batch
        jobs.
        If this value is specified without units, it is taken as megabytes.
        The default is 80 MB.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

     

     

     
# Archiving

    

     

      
archive_mode (enum)
      
       archive_mode configuration parameter
      
      

      

       

        When archive_mode is enabled, completed WAL segments
        are sent to archive storage by setting
         or
        . In addition to off,
        to disable, there are two modes: on, and
        always. During normal operation, there is no
        difference between the two modes, but when set to always
        the WAL archiver is enabled also during archive recovery or standby
        mode. In always mode, all files restored from the archive
        or streamed with streaming physical replication will be archived (again). See
         for details.
       

       

        archive_mode is a separate setting from
        archive_command and
        archive_library so that
        archive_command and
        archive_library can be changed without leaving
        archiving mode.
        This parameter can only be set at server start.
        archive_mode cannot be enabled when
        wal_level is set to minimal.
       

      

     

     

      
archive_command (string)
      
       archive_command configuration parameter
      
      

      

       

        The local shell command to execute to archive a completed WAL file
        segment.  Any %p in the string is
        replaced by the path name of the file to archive, and any
        %f is replaced by only the file name.
        (The path name is relative to the working directory of the server,
        i.e., the cluster's data directory.)
        Use %% to embed an actual % character in the
        command.  It is important for the command to return a zero
        exit status only if it succeeds. For more information see
        .
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.  It is only used if
        archive_mode was enabled at server start and
        archive_library is set to an empty string.  If both
        archive_command and archive_library
        are set, an error will be raised.
        If archive_command is an empty string (the default) while
        archive_mode is enabled (and archive_library
        is set to an empty string), WAL archiving is temporarily
        disabled, but the server continues to accumulate WAL segment files in
        the expectation that a command will soon be provided.  Setting
        archive_command to a command that does nothing but
        return true, e.g., /bin/true (REM on
        Windows), effectively disables
        archiving, but also breaks the chain of WAL files needed for
        archive recovery, so it should only be used in unusual circumstances.
       

      

     

     

      
archive_library (string)
      
       archive_library configuration parameter
      
      

      

       

        The library to use for archiving completed WAL file segments.  If set to
        an empty string (the default), archiving via shell is enabled, and
         is used.  If both
        archive_command and archive_library
        are set, an error will be raised.  Otherwise, the specified
        shared library is used for archiving. The WAL archiver process is
        restarted by the postmaster when this parameter changes. For more
        information, see  and
        .
       

       

        This parameter can only be set in the
        postgresql.conf file or on the server command line.
       

      

     

     

      
archive_timeout (integer)
      
       archive_timeout configuration parameter
      
      

      

       

        The  or  is only invoked for
        completed WAL segments. Hence, if your server generates little WAL
        traffic (or has slack periods where it does so), there could be a
        long delay between the completion of a transaction and its safe
        recording in archive storage.  To limit how old unarchived
        data can be, you can set archive_timeout to force the
        server to switch to a new WAL segment file periodically.  When this
        parameter is greater than zero, the server will switch to a new
        segment file whenever this amount of time has elapsed since the last
        segment file switch, and there has been any database activity,
        including a single checkpoint (checkpoints are skipped if there is
        no database activity).  Note that archived files that are closed
        early due to a forced switch are still the same length as completely
        full files.  Therefore, it is unwise to use a very short
        archive_timeout &mdash; it will bloat your archive
        storage.  archive_timeout settings of a minute or so are
        usually reasonable.  You should consider using streaming physical replication,
        instead of archiving, if you want data to be copied off the primary
        server more quickly than that.
        If this value is specified without units, it is taken as seconds.
        This parameter can only be set in the
        postgresql.conf file or on the server command line.
       

      

     

     

    

   

    
# Recovery

     
      configuration
      of recovery
      general settings
     

    

     This section describes the settings that apply to recovery in general,
     affecting crash recovery, streaming physical replication and archive-based
     replication.
    

    

     

      
recovery_prefetch (enum)
      
       recovery_prefetch configuration parameter
      
      

      

       

        Whether to try to prefetch blocks that are referenced in the WAL that
        are not yet in the buffer pool, during recovery.  Valid values are
        off, on and
        try (the default).  The setting
        try enables
        prefetching only if the operating system provides support for issuing
        read-ahead advice.
       

       

        Prefetching blocks that will soon be needed can reduce I/O wait times
        during recovery with some workloads.
        See also the  and
         settings, which limit
        prefetching activity.
       

      

     

     

      
wal_decode_buffer_size (integer)
      
       wal_decode_buffer_size configuration parameter
      
      

      

       

        A limit on how far ahead the server can look in the WAL, to find
        blocks to prefetch.  If this value is specified without units, it is
        taken as bytes.
        The default is 512kB.
        This parameter can only be set at server start.
       

      

     

    

   

  

    
# Archive Recovery

    
     configuration
     of recovery
     of a standby server
    

    

     This section describes the settings that apply only for the duration of
     the recovery.  They must be reset for any subsequent recovery you wish to
     perform.
    

    

     Recovery covers using the server as a standby or for
     executing a targeted recovery.  Typically, standby mode would be used to
     provide high availability and/or read scalability, whereas a targeted
     recovery is used to recover from data loss.
    

    

     To start the server in standby mode, create a file called
     standby.signalstandby.signal
     in the data directory.  The server will enter recovery and will not stop
     recovery when the end of archived WAL is reached, but will keep trying to
     continue recovery by connecting to the sending server as specified by the
     primary_conninfo setting and/or by fetching new WAL
     segments using restore_command.  For this mode, the
     parameters from this section and  are of interest.
     Parameters from  will
     also be applied but are typically not useful in this mode.
    

    

     To start the server in targeted recovery mode, create a file called
     recovery.signalrecovery.signal
     in the data directory.  If both standby.signal and
     recovery.signal files are created, standby mode
     takes precedence.  Targeted recovery mode ends when the archived WAL is
     fully replayed, or when recovery_target is reached.
     In this mode, the parameters from both this section and  will be used.
    

    

     

      
restore_command (string)
      
        restore_command configuration parameter
      
      

      

       

        The local shell command to execute to retrieve an archived segment of
        the WAL file series. This parameter is required for archive recovery,
        but optional for streaming physical replication.
        Any %f in the string is
        replaced by the name of the file to retrieve from the archive,
        and any %p is replaced by the copy destination path name
        on the server.
        (The path name is relative to the current working directory,
        i.e., the cluster's data directory.)
        Any %r is replaced by the name of the file containing the
        last valid restartpoint. That is the earliest file that must be kept
        to allow a restore to be restartable, so this information can be used
        to truncate the archive to just the minimum required to support
        restarting from the current restore. %r is typically only
        used by warm-standby configurations
        (see ).
        Write %% to embed an actual % character.
       

       

        It is important for the command to return a zero exit status
        only if it succeeds.  The command will be asked for file
        names that are not present in the archive; it must return nonzero
        when so asked.  Examples:

```
restore_command = 'cp "/mnt/server/archivedir/%f" "%p"'
restore_command = 'copy "C:\\server\\archivedir\\%f" "%p"'  # Windows
```

        An exception is that if the command was terminated by a signal (other
        than SIGTERM, which is used as part of a
        database server shutdown) or an error by the shell (such as command
        not found), then recovery will abort and the server will not start up.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
archive_cleanup_command (string)
      
        archive_cleanup_command configuration parameter
      
      

      

       

        This optional parameter specifies a shell command that will be executed
        at every restartpoint.  The purpose of
        archive_cleanup_command is to provide a mechanism for
        cleaning up old archived WAL files that are no longer needed by the
        standby server.
        Any %r is replaced by the name of the file containing the
        last valid restartpoint.
        That is the earliest file that must be kept to allow a
        restore to be restartable, and so all files earlier than %r
        may be safely removed.
        This information can be used to truncate the archive to just the
        minimum required to support restart from the current restore.
        The  module
        is often used in archive_cleanup_command for
        single-standby configurations, for example:

```
archive_cleanup_command = 'pg_archivecleanup /mnt/server/archivedir "%r"'
```

        Note however that if multiple standby servers are restoring from the
        same archive directory, you will need to ensure that you do not delete
        WAL files until they are no longer needed by any of the servers.
        archive_cleanup_command would typically be used in a
        warm-standby configuration (see ).
        Write %% to embed an actual % character in the
        command.
       

       

        If the command returns a nonzero exit status then a warning log
        message will be written.  An exception is that if the command was
        terminated by a signal or an error by the shell (such as command not
        found), a fatal error will be raised.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
recovery_end_command (string)
      
        recovery_end_command configuration parameter
      
      

      

       

        This parameter specifies a shell command that will be executed once only
        at the end of recovery. This parameter is optional. The purpose of the
        recovery_end_command is to provide a mechanism for cleanup
        following replication or recovery.
        Any %r is replaced by the name of the file containing the
        last valid restartpoint, like in .
       

       

        If the command returns a nonzero exit status then a warning log
        message will be written and the database will proceed to start up
        anyway.  An exception is that if the command was terminated by a
        signal or an error by the shell (such as command not found), the
        database will not proceed with startup.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

    

  

  

    
# Recovery Target

     

      By default, recovery will recover to the end of the WAL log. The
      following parameters can be used to specify an earlier stopping point.
      At most one of recovery_target,
      recovery_target_lsn, recovery_target_name,
      recovery_target_time, or recovery_target_xid
      can be used; if more than one of these is specified in the configuration
      file, an error will be raised.
      These parameters can only be set at server start.
     

     

     

      
recovery_target = 'immediate'
      
        recovery_target configuration parameter
      
      

      

       

        This parameter specifies that recovery should end as soon as a
        consistent state is reached, i.e., as early as possible. When restoring
        from an online backup, this means the point where taking the backup
        ended.
       

       

        Technically, this is a string parameter, but 'immediate'
        is currently the only allowed value.
       

      

     

     

      
recovery_target_name (string)
      
        recovery_target_name configuration parameter
      
      

      

       

        This parameter specifies the named restore point (created with
        pg_create_restore_point()) to which recovery will proceed.
       

      

     

     

      
recovery_target_time (timestamp)
      
        recovery_target_time configuration parameter
      
      

      

       

        This parameter specifies the time stamp up to which recovery
        will proceed.
        The precise stopping point is also influenced by
        .
       

       

        The value of this parameter is a time stamp in the same format
        accepted by the timestamp with time zone data type,
        except that you cannot use a time zone abbreviation (unless the
         variable has been set
        earlier in the configuration file).  Preferred style is to use a
        numeric offset from UTC, or you can write a full time zone name,
        e.g., Europe/Helsinki not EEST.
       

      

     

     

      
recovery_target_xid (string)
      
        recovery_target_xid configuration parameter
      
      

      

       

        This parameter specifies the transaction ID up to which recovery
        will proceed. Keep in mind
        that while transaction IDs are assigned sequentially at transaction
        start, transactions can complete in a different numeric order.
        The transactions that will be recovered are those that committed
        before (and optionally including) the specified one.
        The precise stopping point is also influenced by
        .
       

       

        The value can be specified as either a 32-bit transaction ID or a 64-bit
        transaction ID (consisting of an epoch and a 32-bit ID), such as the
        value returned by pg_current_xact_id(). When a
        64-bit transaction ID is provided, only its 32-bit transaction ID
        portion is used as the recovery target. For example, the values
        4294968296 (epoch 1) and 8589935592 (epoch 2) both refer to the same
        32-bit transaction ID, 1000.
       

       

        The effective transaction ID (the 32-bit portion) must be greater than
        or equal to 3.
       

      

     

     

      
recovery_target_lsn (pg_lsn)
      
        recovery_target_lsn configuration parameter
      
      

      

       

        This parameter specifies the LSN of the write-ahead log location up
        to which recovery will proceed. The precise stopping point is also
        influenced by . This
        parameter is parsed using the system data type
        pg_lsn.
       

      

     

     

     

       The following options further specify the recovery target, and affect
       what happens when the target is reached:
     

     

     

      
recovery_target_inclusive (boolean)
      
        recovery_target_inclusive configuration parameter
      
      

      

       

        Specifies whether to stop just after the specified recovery target
        (on), or just before the recovery target
        (off).
        Applies when ,
        , or
         is specified.
        This setting controls whether transactions
        having exactly the target WAL location (LSN), commit time, or transaction ID, respectively, will
        be included in the recovery.  Default is on.
       

      

     

     

      
recovery_target_timeline (string)
      
        recovery_target_timeline configuration parameter
      
      

      

       

        Specifies recovering into a particular timeline.  The value can be a
        numeric timeline ID or a special value.  The value
        current recovers along the same timeline that was
        current when the base backup was taken.  The
        value latest recovers
        to the latest timeline found in the archive, which is useful in
        a standby server.  latest is the default.
       

       

        To specify a timeline ID in hexadecimal (for example, if extracted
        from a WAL file name or history file), prefix it with a
        0x.  For instance, if the WAL file name is
        00000011000000A10000004F, then the timeline ID is
        0x11 (or 17 decimal).
       

       

        You usually only need to set this parameter
        in complex re-recovery situations, where you need to return to
        a state that itself was reached after a point-in-time recovery.
        See  for discussion.
       

      

     

     

      
recovery_target_action (enum)
      
        recovery_target_action configuration parameter
      
      

      

       

        Specifies what action the server should take once the recovery target is
        reached. The default is pause, which means recovery will
        be paused. promote means the recovery process will finish
        and the server will start to accept connections.
        Finally shutdown will stop the server after reaching the
        recovery target.
       

       

        The intended use of the pause setting is to allow queries
        to be executed against the database to check if this recovery target
        is the most desirable point for recovery.
        The paused state can be resumed by
        using pg_wal_replay_resume() (see
        ), which then
        causes recovery to end. If this recovery target is not the
        desired stopping point, then shut down the server, change the
        recovery target settings to a later target and restart to
        continue recovery.
       

       

        The shutdown setting is useful to have the instance ready
        at the exact replay point desired.  The instance will still be able to
        replay more WAL records (and in fact will have to replay WAL records
        since the last checkpoint next time it is started).
       

       

        Note that because recovery.signal will not be
        removed when recovery_target_action is set to shutdown,
        any subsequent start will end with immediate shutdown unless the
        configuration is changed or the recovery.signal
        file is removed manually.
       

       

        This setting has no effect if no recovery target is set.
        If  is not enabled, a setting of
        pause will act the same as shutdown.
        If the recovery target is reached while a promotion is ongoing,
        a setting of pause will act the same as
        promote.
       

       

        In any case, if a recovery target is configured but the archive
        recovery ends before the target is reached, the server will shut down
        with a fatal error.
       

      

     

    

   

   

    
# WAL Summarization

    

     These settings control WAL summarization, a feature which must be
     enabled in order to perform an
     incremental backup.
    

    

     

      
summarize_wal (boolean)
      
       summarize_wal configuration parameter
      
      

     

      

       Enables the WAL summarizer process. Note that WAL summarization can
       be enabled either on a primary or on a standby.  This parameter can only
       be set in the postgresql.conf file or on the server
       command line.  The default is off.
      

      

       The server cannot be started with summarize_wal=on
       if wal_level is set to minimal. If
       summarize_wal=on is configured after server startup
       while wal_level=minimal, the summarizer will run
       but refuse to generate summary files for any WAL generated with
       wal_level=minimal.
      

      

     

     

      
wal_summary_keep_time (integer)
      
       wal_summary_keep_time configuration parameter
      
      

     

      

       Configures the amount of time after which the WAL summarizer
       automatically removes old WAL summaries. The file timestamp is used to
       determine which files are old enough to remove. Typically, you should set
       this comfortably higher than the time that could pass between a backup
       and a later incremental backup that depends on it.  WAL summaries must
       be available for the entire range of WAL records between the preceding
       backup and the new one being taken; if not, the incremental backup will
       fail. If this parameter is set to zero, WAL summaries will not be
       automatically deleted, but it is safe to manually remove files that you
       know will not be required for future incremental backups.
       This parameter can only be set in the
       postgresql.conf file or on the server command line.
       If this value is specified without units, it is taken as minutes.
       The default is 10 days. If summarize_wal = off,
       existing WAL summaries will not be removed regardless of the value of
       this parameter, because the WAL summarizer will not run.
      

      

     

    

   

   

   

    
# Replication

    

     These settings control the behavior of
     streaming replication,
     both physical replication
     (see ) and
     logical replication (see
     ).
    

    

     For physical replication, servers will be either a
     primary or a standby server.  Primaries can send data, while standbys
     are always receivers of replicated data.  When cascading replication
     (see ) is used, standby servers
     can also be senders, as well as receivers.
     Parameters are mainly for sending and standby servers, though some
     parameters have meaning only on the primary server.  Settings may vary
     across the cluster without problems if that is required.
    

    

     For logical replication, publishers
     (servers that do CREATE PUBLICATION)
     replicate data to subscribers
     (servers that do CREATE SUBSCRIPTION).
     Servers can also be publishers and subscribers at the same time. Note,
     the following sections refer to publishers as "senders". For more details
     about logical replication configuration settings refer to
     .
    

    

     
# Sending Servers

     

      These parameters can be set on any server that is
      to send replication data to one or more standby servers.
      The primary is always a sending server, so these parameters must
      always be set on the primary.
      The role and meaning of these parameters does not change after a
      standby becomes the primary.
     

     

      

       
max_wal_senders (integer)
       
        max_wal_senders configuration parameter
       
       

       

       

        Specifies the maximum number of concurrent connections from standby
        servers or streaming base backup clients (i.e., the maximum number of
        simultaneously running WAL sender processes). The default is
        10.  The value 0 means
        replication is disabled.  Abrupt disconnection of a streaming client might
        leave an orphaned connection slot behind until a timeout is reached,
        so this parameter should be set slightly higher than the maximum
        number of expected clients so disconnected clients can immediately
        reconnect.  This parameter can only be set at server start.  Also,
        wal_level must be set to
        replica or higher to allow connections from standby
        servers.
       

       

         When running a standby server, you must set this parameter to the
         same or higher value than on the primary server. Otherwise, queries
         will not be allowed on the standby server.
        

       

      

      

       
max_repack_replication_slots (integer)
       
        max_repack_replication_slots configuration parameter
       
       

       

        

         Specifies the maximum number of replication slots for use of
         the REPACK command.  The default is 5.
         This parameter can only be set at server start.
        

       

      

      

       
max_replication_slots (integer)
       
        max_replication_slots configuration parameter
       
       

       

        

         Specifies the maximum number of replication slots
         (see ) that the server
         can support. The default is 10.  This parameter can only be set at
         server start.
         Setting it to a lower value than the number of currently
         existing replication slots will prevent the server from starting.
         Also, wal_level must be set
         to replica or higher to allow replication slots to
         be used.
        

       

      

      

       
output_plugin_libraries (string)
       
        output_plugin_libraries configuration parameter
       
       

       

        

         Lists the libraries installed in 
         that are also trusted for use as logical output plugins by replication
         clients. Any logical decoding
         or replication requests for
         other libraries will be refused. All users are subject to this
         restriction. The default is 'pgoutput, test_decoding',
         which are the two logical output plugins included in the standard
         PostgreSQL distribution.
        

        

         The format is a comma-separated list of library names, where each name
         is interpreted as for the LOAD
         command (but logical decoding clients must specify a plugin name that
         exactly matches an entry in the list, without
         variations in case or path structure). Whitespace between entries is
         ignored; surround a library name with double quotes if you need to
         include whitespace or commas in the name.
        

        

         It is the responsibility of the server administrator to ensure that
         libraries added to this list do not unintentionally give additional
         privileges to non-superusers when they are loaded into the server.
        

        
         

          When updating the server from a version that does not have the
          output_plugin_libraries parameter, the following
          query can help construct the list of plugins that are required by all
          persistent logical replication slots:

```
SELECT DISTINCT plugin FROM pg_replication_slots WHERE plugin IS NOT NULL;
```

          Review the list carefully for safety before adjusting
          output_plugin_libraries.
         

         

          The above query can only display plugins which were successfully
          added to replication slots at some point in the past. Newly refused
          requests will appear in the logs with a message similar to

```
ERROR:  library "..." may not be used as an output plugin
DETAIL:  The configuration parameter "output_plugin_libraries" (currently 'pgoutput, test_decoding') does not name this library as a trusted output plugin.
HINT:  If it is safe for all REPLICATION users to use this library as an output plugin, add it to "output_plugin_libraries" and reload the server configuration.
```

         

        
       

      

      

       
wal_keep_size (integer)
       
        wal_keep_size configuration parameter
       
       

       

       

        Specifies the minimum size of past WAL files kept in the
        pg_wal
        directory, in case a standby server needs to fetch them for streaming
        replication. If a standby
        server connected to the sending server falls behind by more than
        wal_keep_size megabytes, the sending server might
        remove a WAL segment still needed by the standby, in which case the
        replication connection will be terminated.  Downstream connections
        will also eventually fail as a result.  (However, the standby
        server can recover by fetching the segment from archive, if WAL
        archiving is in use.)
       

       

        This sets only the minimum size of segments retained in
        pg_wal; the system might need to retain more segments
        for WAL archival or to recover from a checkpoint. If
        wal_keep_size is zero (the default), the system
        doesn't keep any extra segments for standby purposes, so the number
        of old WAL segments available to standby servers is a function of
        the location of the previous checkpoint and status of WAL
        archiving.
        If this value is specified without units, it is taken as megabytes.
        This parameter can only be set in the
        postgresql.conf file or on the server command line.
       

       

      

      

       
max_slot_wal_keep_size (integer)
       
        max_slot_wal_keep_size configuration parameter
       
       

       

       

        Specify the maximum size of WAL files
        that replication
        slots are allowed to retain in the pg_wal
        directory at checkpoint time.
        If max_slot_wal_keep_size is -1 (the default),
        replication slots may retain an unlimited amount of WAL files.  Otherwise, if
        restart_lsn of a replication slot falls behind the current LSN by more
        than the given size, the standby using the slot may no longer be able
        to continue replication due to removal of required WAL files. You
        can see the WAL availability of replication slots
        in pg_replication_slots.
        If this value is specified without units, it is taken as megabytes.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

       

      

     

      
idle_replication_slot_timeout (integer)
      
       idle_replication_slot_timeout configuration parameter
      
      

      

       

        Invalidate replication slots that have remained inactive (not used by
        a replication connection)
        for longer than this duration.
        If this value is specified without units, it is taken as seconds.
        A value of zero (the default) disables the idle timeout
        invalidation mechanism.  This parameter can only be set in the
        postgresql.conf file or on the server command
        line.
       

       

        Slot invalidation due to idle timeout occurs during checkpoint.
        Because checkpoints happen at checkpoint_timeout
        intervals, there can be some lag between when the
        idle_replication_slot_timeout was exceeded and when
        the slot invalidation is triggered at the next checkpoint.
        To avoid such lags, users can force a checkpoint to promptly invalidate
        inactive slots. The duration of slot inactivity is calculated using the
        slot's pg_replication_slots.inactive_since
        value.
       

       

        Note that the idle timeout invalidation mechanism is not applicable
        for slots that do not reserve WAL or for slots on the standby server
        that are being synced from the primary server (i.e., standby slots
        having pg_replication_slots.synced
        value true). Synced slots are always considered to
        be inactive because they don't perform logical decoding to produce
        changes.
       

      

     

     

      
wal_sender_timeout (integer)
      
       wal_sender_timeout configuration parameter
      
      

      

       

        Terminate replication connections that are inactive for longer
        than this amount of time. This is useful for
        the sending server to detect a standby crash or network outage.
        If this value is specified without units, it is taken as milliseconds.
        The default value is 60 seconds.
        A value of zero disables the timeout mechanism.
       

       

        With a cluster distributed across multiple geographic
        locations, using different values per location brings more flexibility
        in the cluster management. A smaller value is useful for faster
        failure detection with a standby having a low-latency network
        connection, and a larger value helps in judging better the health
        of a standby if located on a remote location, with a high-latency
        network connection.
       

      

     

     

      
wal_sender_shutdown_timeout (integer)
      
       wal_sender_shutdown_timeout configuration parameter
      
      

      

       

        Specifies the maximum time the server waits during shutdown for all
        WAL data to be replicated to the receiver.  If this value is specified
        without units, it is taken as milliseconds.  A value of
        -1 (the default) disables the timeout mechanism,
        allowing the WAL sender to wait as long as necessary for the receiver
        to catch up.  A value of 0 causes the WAL sender to
        terminate without waiting for the receiver to catch up.
       

       

        When replication is in use, the sending server normally waits until
        all WAL data has been transferred to the receiver before completing
        shutdown.  This helps keep sender and receiver in sync after shutdown,
        which is especially important for physical replication switchovers,
        but it can delay shutdown.
       

       

        If this parameter is set to zero or a positive value, the server stops
        waiting and completes shutdown when the timeout expires.  This can
        shorten shutdown time, for example, when replication is slow on
        high-latency networks or when a logical replication apply worker is
        blocked waiting for locks.  However, in this case the sender and
        receiver may be out of sync after shutdown.  Care should be taken to
        select a value high enough to allow all WAL data to be replicated to
        the receiver under normal circumstances.
       

       

        This parameter can be set in primary_conninfo and
        in the CONNECTION clause of
        CREATE SUBSCRIPTION (for example, include
        options=-cwal_sender_shutdown_timeout=10s in the
        connection string), allowing different timeouts per replication
        connection.  For example, when both physical and logical replication
        are used, it can be disabled for physical replication (e.g., for
        switchovers) while enabled for logical replication to limit shutdown
        time.
       

      

     

     

      
track_commit_timestamp (boolean)
      
       track_commit_timestamp configuration parameter
      
      

      

       

        Record commit time of transactions.
        This parameter can only be set at server start.
        The default value is off.
       

      

     

     

    

    

     
# Primary Server

     

      These parameters can be set on the primary server that is
      to send replication data to one or more standby servers.
      Note that in addition to these parameters,
       must be set appropriately on the primary
      server, and optionally WAL archiving can be enabled as
      well (see ).
      The values of these parameters on standby servers are irrelevant,
      although you may wish to set them there in preparation for the
      possibility of a standby becoming the primary.
     

    

     

      
synchronous_standby_names (string)
      
       synchronous_standby_names configuration parameter
      
      

      

       

        Specifies a list of standby servers that can support
        synchronous replication, as described in
        .
        There will be one or more active synchronous standbys;
        transactions waiting for commit will be allowed to proceed after
        these standby servers confirm receipt of their data.
        The synchronous standbys will be those whose names appear
        in this list, and
        that are both currently connected and streaming data in real-time
        (as shown by a state of streaming in the
        
        pg_stat_replication view).
        Specifying more than one synchronous standby can allow for very high
        availability and protection against data loss.
       

       

        The name of a standby server for this purpose is the
        application_name setting of the standby, as set in the
        standby's connection information.  In case of a physical replication
        standby, this should be set in the primary_conninfo
        setting; the default is the setting of 
        if set, else walreceiver.
        For logical replication, this can be set in the connection
        information of the subscription, and it defaults to the
        subscription name.  For other replication stream consumers,
        consult their documentation.
       

       

        This parameter specifies a list of standby servers using
        either of the following syntaxes:

[FIRST] num_sync ( standby_name [, ...] )
ANY num_sync ( standby_name [, ...] )
standby_name [, ...]

        where num_sync is
        the number of synchronous standbys that transactions need to
        wait for replies from,
        and standby_name
        is the name of a standby server.
        num_sync
        must be an integer value greater than zero.
        FIRST and ANY specify the method to choose
        synchronous standbys from the listed servers.
       

       

        The keyword FIRST, coupled with
        num_sync, specifies a
        priority-based synchronous replication and makes transaction commits
        wait until their WAL records are replicated to
        num_sync synchronous
        standbys chosen based on their priorities. For example, a setting of
        FIRST 3 (s1, s2, s3, s4) will cause each commit to wait for
        replies from three higher-priority standbys chosen from standby servers
        s1, s2, s3 and s4.
        The standbys whose names appear earlier in the list are given higher
        priority and will be considered as synchronous. Other standby servers
        appearing later in this list represent potential synchronous standbys.
        If any of the current synchronous standbys disconnects for whatever
        reason, it will be replaced immediately with the next-highest-priority
        standby. The keyword FIRST is optional.
       

       

        The keyword ANY, coupled with
        num_sync, specifies a
        quorum-based synchronous replication and makes transaction commits
        wait until their WAL records are replicated to at least
        num_sync listed standbys.
        For example, a setting of ANY 3 (s1, s2, s3, s4) will cause
        each commit to proceed as soon as at least any three standbys of
        s1, s2, s3 and s4
        reply.
       

       

        FIRST and ANY are case-insensitive. If these
        keywords are used as the name of a standby server,
        its standby_name must
        be double-quoted.
       

       

        The third syntax was used before PostgreSQL
        version 9.6 and is still supported. It's the same as the first syntax
        with FIRST and
        num_sync equal to 1.
        For example, FIRST 1 (s1, s2) and s1, s2 have
        the same meaning: either s1 or s2 is chosen
        as a synchronous standby.
       

       

        The special entry * matches any standby name.
       

       

        There is no mechanism to enforce uniqueness of standby names.  In case
        of duplicates one of the matching standbys will be considered as
        higher priority, though exactly which one is indeterminate.
       

       
        

         Each standby_name
         should have the form of a valid SQL identifier, unless it
         is *.  You can use double-quoting if necessary.  But note
         that standby_names are
         compared to standby application names case-insensitively, whether
         double-quoted or not.
        

       
       

        If no synchronous standby names are specified here, then synchronous
        replication is not enabled and transaction commits will not wait for
        replication.  This is the default configuration.  Even when
        synchronous replication is enabled, individual transactions can be
        configured not to wait for replication by setting the
         parameter to
        local or off.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
synchronized_standby_slots (string)
      
       synchronized_standby_slots configuration parameter
      
      

      

       

        A comma-separated list of streaming replication standby server slot names
        that logical WAL sender processes will wait for. Logical WAL sender processes
        will send decoded changes to plugins only after the specified replication
        slots confirm receiving WAL. This guarantees that logical replication
        failover slots do not consume changes until those changes are received
        and flushed to corresponding physical standbys. If a
        logical replication connection is meant to switch to a physical standby
        after the standby is promoted, the physical replication slot for the
        standby should be listed here. Note that logical replication will not
        proceed if the slots specified in the
        synchronized_standby_slots do not exist or are invalidated.
        Additionally, the replication management functions
        
        pg_replication_slot_advance,
        
        pg_logical_slot_get_changes, and
        
        pg_logical_slot_peek_changes,
        when used with logical failover slots, will block until all
        physical slots specified in synchronized_standby_slots have
        confirmed WAL receipt.
       

       

        The standbys corresponding to the physical replication slots in
        synchronized_standby_slots must configure
        sync_replication_slots = true so they can receive
        logical failover slot changes from the primary.
       

      

     

     

    

    

     
# Standby Servers

     

      These settings control the behavior of a
      standby server
      that is
      to receive physical replication data.  Their values on the primary server
      are irrelevant.
     

    

       

        
primary_conninfo (string)
        
          primary_conninfo configuration parameter
        
        

        

         

          Specifies a connection string to be used for the standby server
          to connect with a sending server. This string is in the format
          described in . If any option is
          unspecified in this string, then the corresponding environment
          variable (see ) is checked. If the
          environment variable is not set either, then
          defaults are used.
         

         

          The connection string should specify the host name (or address)
          of the sending server, as well as the port number if it is not
          the same as the standby server's default.
          Also specify a user name corresponding to a suitably-privileged role
          on the sending server (see
          ).
          A password needs to be provided too, if the sender demands password
          authentication.  It can be provided in the
          primary_conninfo string, or in a separate
          ~/.pgpass file on the standby server (use
          replication as the database name).
         

         

          For replication slot synchronization (see
          ),
          it is also necessary to specify a valid dbname
          in the primary_conninfo string. This will only be
          used for slot synchronization. It is ignored for streaming.
         

         

          This parameter can only be set in the postgresql.conf
          file or on the server command line.
          If this parameter is changed while the WAL receiver process is
          running, that process is signaled to shut down and expected to
          restart with the new setting (except if primary_conninfo
          is an empty string).
          This setting has no effect if the server is not in standby mode.
         

        

       

       

        
primary_slot_name (string)
        
          primary_slot_name configuration parameter
        
        

        

         

          Optionally specifies an existing replication slot to be used when
          connecting to the sending server via streaming replication to control
          resource removal on the upstream node
          (see ).
          This parameter can only be set in the postgresql.conf
          file or on the server command line.
          If this parameter is changed while the WAL receiver process is running,
          that process is signaled to shut down and expected to restart with the
          new setting.
          This setting has no effect if primary_conninfo is not
          set or the server is not in standby mode.
          The name cannot be pg_conflict_detection as it is
          reserved for the conflict detection slot.
         

        

       

     

      
hot_standby (boolean)
      
       hot_standby configuration parameter
      
      

      

       

        Specifies whether or not you can connect and run queries during
        recovery, as described in .
        The default value is on.
        This parameter can only be set at server start. It only has effect
        during archive recovery or in standby mode.
       

      

     

     

      
max_standby_archive_delay (integer)
      
       max_standby_archive_delay configuration parameter
      
      

      

       

        When hot standby is active, this parameter determines how long the
        standby server should wait before canceling standby queries that
        conflict with about-to-be-applied WAL entries, as described in
        .
        max_standby_archive_delay applies when WAL data is
        being read from WAL archive (and is therefore not current).
        If this value is specified without units, it is taken as milliseconds.
        The default is 30 seconds.
        A value of -1 allows the standby to wait forever for conflicting
        queries to complete.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

       

        Note that max_standby_archive_delay is not the same as the
        maximum length of time a query can run before cancellation; rather it
        is the maximum total time allowed to apply any one WAL segment's data.
        Thus, if one query has resulted in significant delay earlier in the
        WAL segment, subsequent conflicting queries will have much less grace
        time.
       

      

     

     

      
max_standby_streaming_delay (integer)
      
       max_standby_streaming_delay configuration parameter
      
      

      

       

        When hot standby is active, this parameter determines how long the
        standby server should wait before canceling standby queries that
        conflict with about-to-be-applied WAL entries, as described in
        .
        max_standby_streaming_delay applies when WAL data is
        being received via streaming physical replication.
        If this value is specified without units, it is taken as milliseconds.
        The default is 30 seconds.
        A value of -1 allows the standby to wait forever for conflicting
        queries to complete.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

       

        Note that max_standby_streaming_delay is not the same as
        the maximum length of time a query can run before cancellation; rather
        it is the maximum total time allowed to apply WAL data once it has
        been received from the primary server.  Thus, if one query has
        resulted in significant delay, subsequent conflicting queries will
        have much less grace time until the standby server has caught up
        again.
       

      

     

     

      
wal_receiver_create_temp_slot (boolean)
      
       wal_receiver_create_temp_slot configuration parameter
      
      

      

       

        Specifies whether the WAL receiver process should create a temporary replication
        slot on the remote instance when no permanent replication slot to use
        has been configured (using ).
        The default is off.  This parameter can only be set in the
        postgresql.conf file or on the server command line.
        If this parameter is changed while the WAL receiver process is running,
        that process is signaled to shut down and expected to restart with
        the new setting.
       

      

     

     

      
wal_receiver_status_interval (integer)
      
       wal_receiver_status_interval configuration parameter
      
      

      

      

       Specifies the minimum frequency for the WAL receiver
       process on the standby to send information about replication progress
       to the primary or upstream standby, where it can be seen using the
       
       pg_stat_replication
       view.  The standby will report
       the last write-ahead log location it has written, the last position it
       has flushed to disk, and the last position it has applied.
       This parameter's value is the maximum amount of time between reports.
       Updates are sent each time the write or flush positions change, or as
       often as specified by this parameter if set to a non-zero value.
       There are additional cases where updates are sent while ignoring this
       parameter; for example, when processing of the existing WAL completes
       or when synchronous_commit is set to
       remote_apply.
       Thus, the apply position may lag slightly behind the true position.
       If this value is specified without units, it is taken as seconds.
       The default value is 10 seconds. This parameter can only be set in
       the postgresql.conf file or on the server
       command line.
      

      

     

     

      
hot_standby_feedback (boolean)
      
       hot_standby_feedback configuration parameter
      
      

      

       

        Specifies whether or not a hot standby will send feedback to the primary
        or upstream standby
        about queries currently executing on the standby. This parameter can
        be used to eliminate query cancels caused by cleanup records, but
        can cause database bloat on the primary for some workloads.
        Feedback messages will not be sent more frequently than once per
        wal_receiver_status_interval. The default value is
        off. This parameter can only be set in the
        postgresql.conf file or on the server command line.
       

       

        If cascaded replication is in use the feedback is passed upstream
        until it eventually reaches the primary.  Standbys make no other use
        of feedback they receive other than to pass upstream.
       

       

        Note that if the clock on standby is moved ahead or backward, the
        feedback message might not be sent at the required interval.  In
        extreme cases, this can lead to a prolonged risk of not removing dead
        rows on the primary for extended periods, as the feedback mechanism is
        based on timestamps.
       

      

     

     

      
wal_receiver_timeout (integer)
      
       wal_receiver_timeout configuration parameter
      
      

      

       

        Terminate replication connections that are inactive for longer
        than this amount of time. This is useful for
        the receiving standby server to detect a primary node crash or network
        outage.
        If this value is specified without units, it is taken as milliseconds.
        The default value is 60 seconds.
        A value of zero disables the timeout mechanism.
       

      

     

     

      
wal_retrieve_retry_interval (integer)
      
       wal_retrieve_retry_interval configuration parameter
      
      

      

       

        Specifies how long the standby server should wait when WAL data is not
        available from any sources (streaming physical replication,
        local pg_wal or WAL archive) before trying
        again to retrieve WAL data.
        If this value is specified without units, it is taken as milliseconds.
        The default value is 5 seconds.
        This parameter can only be set in
        the postgresql.conf file or on the server
        command line.
       

       

        This parameter is useful in configurations where a node in recovery
        needs to control the amount of time to wait for new WAL data to be
        available. For example, in archive recovery, it is possible to
        make the recovery more responsive in the detection of a new WAL
        file by reducing the value of this parameter. On a system with
        low WAL activity, increasing it reduces the amount of requests necessary
        to access WAL archives, something useful for example in cloud
        environments where the number of times an infrastructure is accessed
        is taken into account.
       

       

        In logical replication, this parameter also limits how often a failing
        replication apply worker or table/sequence synchronization worker will
        be respawned.
       

      

     

     

      
recovery_min_apply_delay (integer)
      
        recovery_min_apply_delay configuration parameter
      
      

      

       

        By default, a standby server restores WAL records from the
        sending server as soon as possible. It may be useful to have a time-delayed
        copy of the data, offering opportunities to correct data loss errors.
        This parameter allows you to delay recovery by a specified amount
        of time.  For example, if
        you set this parameter to 5min, the standby will
        replay each transaction commit only when the system time on the standby
        is at least five minutes past the commit time reported by the primary.
        If this value is specified without units, it is taken as milliseconds.
        The default is zero, adding no delay.
       

       

        It is possible that the replication delay between servers exceeds the
        value of this parameter, in which case no delay is added.
        Note that the delay is calculated between the WAL time stamp as written
        on primary and the current time on the standby. Delays in transfer
        because of network lag or cascading replication configurations
        may reduce the actual wait time significantly. If the system
        clocks on primary and standby are not synchronized, this may lead to
        recovery applying records earlier than expected; but that is not a
        major issue because useful settings of this parameter are much larger
        than typical time deviations between servers.
       

       

        The delay occurs only on WAL records for transaction commits.
        Other records are replayed as quickly as possible, which
        is not a problem because MVCC visibility rules ensure their effects
        are not visible until the corresponding commit record is applied.
       

       

        The delay occurs once the database in recovery has reached a consistent
        state, until the standby is promoted or triggered. After that the standby
        will end recovery without further waiting.
       

       

        WAL records must be kept on the standby until they are ready to be
        applied. Therefore, longer delays will result in a greater accumulation
        of WAL files, increasing disk space requirements for the standby's
        pg_wal directory.
       

       

        This parameter is intended for use with streaming physical replication deployments;
        however, if the parameter is specified it will be honored in all cases
        except crash recovery.

        hot_standby_feedback will be delayed by use of this feature
        which could lead to bloat on the primary; use both together with care.

        
         

          Synchronous replication is affected by this setting when synchronous_commit
          is set to remote_apply; every COMMIT
          will need to wait to be applied.
         

        
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
sync_replication_slots (boolean)
      
       sync_replication_slots configuration parameter
      
      

      

       

        It enables a physical standby to synchronize logical failover slots
        from the primary server so that logical subscribers can resume
        replication from the new primary server after failover.
       

       

        It is disabled by default. This parameter can only be set in the
        postgresql.conf file or on the server command line.
       

      

     

     

    

    

     
# Subscribers

     

      These settings control the behavior of a logical replication subscriber.
      Their values on the publisher are irrelevant.
      See  for more details.
     

     

     

      
max_active_replication_origins (integer)
       
        max_active_replication_origins configuration parameter
       
      

      

       

        Specifies how many replication origins (see
        ) can be tracked simultaneously,
        effectively limiting how many logical replication subscriptions can
        be created on the server. Setting it to a lower value than the current
        number of tracked replication origins (reflected in
        pg_replication_origin_status)
        will prevent the server from starting. It defaults to 10. This parameter
        can only be set at server start.

        max_active_replication_origins must be set to at least the
        number of subscriptions that will be added to the subscriber, plus some
        reserve for table synchronization.
       

      

     

     

      
max_logical_replication_workers (integer)
      
       max_logical_replication_workers configuration parameter
      
      

      

       

        Specifies maximum number of logical replication workers. This includes
        leader apply workers, parallel apply workers, and table/sequence
        synchronization workers.
       

       

        Logical replication workers are taken from the pool defined by
        max_worker_processes.
       

       

        The default value is 4. This parameter can only be set at server
        start.
       

      

     

     

      
max_sync_workers_per_subscription (integer)
      
       max_sync_workers_per_subscription configuration parameter
      
      

      

       

        Maximum number of synchronization workers per subscription. This
        parameter controls the amount of parallelism of the initial data copy
        for tables during the subscription initialization or when new tables
        are added. One additional worker is also needed for sequence
        synchronization.
       

       

        Currently, there can be only one table synchronization worker per table
        and one sequence synchronization worker to synchronize per
        subscription.
       

       

        The synchronization workers are taken from the pool defined by
        max_logical_replication_workers.
       

       

        The default value is 2. This parameter can only be set in the
        postgresql.conf file or on the server command
        line.
       

      

     

     

      
max_parallel_apply_workers_per_subscription (integer)
      
       max_parallel_apply_workers_per_subscription configuration parameter
      
      

      

       

        Maximum number of parallel apply workers per subscription. This
        parameter controls the amount of parallelism for streaming of
        in-progress transactions with subscription parameter
        streaming = parallel.
       

       

        The parallel apply workers are taken from the pool defined by
        max_logical_replication_workers.
       

       

        The default value is 2. This parameter can only be set in the
        postgresql.conf file or on the server command
        line.
       

      

     

     

    

   

   

    
# Query Planning

    

     
# Planner Method Configuration

      

       These configuration parameters provide a crude method of
       influencing the query plans chosen by the query optimizer. If
       the default plan chosen by the optimizer for a particular query
       is not optimal, a temporary solution is to use one
       of these configuration parameters to force the optimizer to
       choose a different plan.
       Better ways to improve the quality of the
       plans chosen by the optimizer include adjusting the planner cost
       constants (see ),
       running ANALYZE manually, increasing
       the value of the  configuration parameter,
       and increasing the amount of statistics collected for
       specific columns using ALTER TABLE SET
       STATISTICS.
      

     

     

      
enable_async_append (boolean)
      
       enable_async_append configuration parameter
      
      

      

       

        Enables or disables the query planner's use of async-aware
        append plan types. The default is on.
       

      

     

     

      
enable_bitmapscan (boolean)
      
       bitmap scan
      
      
       enable_bitmapscan configuration parameter
      
      

      

       

        Enables or disables the query planner's use of bitmap-scan plan
        types. The default is on.
       

      

     

     

      
enable_distinct_reordering (boolean)
      
       enable_distinct_reordering configuration parameter
      
      

      

       

        Enables or disables the query planner's ability to reorder DISTINCT
        keys to match the input path's pathkeys. The default is on.
       

      

     

     

      
enable_eager_aggregate (boolean)
      
       enable_eager_aggregate configuration parameter
      
      

      

       

        Enables or disables the query planner's ability to partially push
        aggregation past a join, and finalize it once all the relations are
        joined. The default is on.
       

      

     

     

      
enable_gathermerge (boolean)
      
       enable_gathermerge configuration parameter
      
      

      

       

        Enables or disables the query planner's use of gather
        merge plan types. The default is on.
       

      

     

     

      
enable_group_by_reordering (boolean)
      
       enable_group_by_reordering configuration parameter
      
      

      

       

        Controls if the query planner will produce a plan which will provide
        GROUP BY keys sorted in the order of keys of
        a child node of the plan, such as an index scan.  When disabled, the
        query planner will produce a plan with GROUP BY
        keys only sorted to match the ORDER BY clause,
        if any. When enabled, the planner will try to produce a more
        efficient plan. The default value is on.
       

      

     

     

      
enable_groupagg (boolean)
      
       enable_groupagg configuration parameter
      
      

      

       

        Enables or disables the query planner's use of sort-based grouping
        and aggregation plan types. The default is on.
       

      

     

     

      
enable_hashagg (boolean)
      
       enable_hashagg configuration parameter
      
      

      

       

        Enables or disables the query planner's use of hashed
        aggregation plan types. The default is on.
       

      

     

     

      
enable_hashjoin (boolean)
      
       enable_hashjoin configuration parameter
      
      

      

       

        Enables or disables the query planner's use of hash-join plan
        types. The default is on.
       

      

     

     

      
enable_incremental_sort (boolean)
      
       enable_incremental_sort configuration parameter
      
      

      

       

        Enables or disables the query planner's use of incremental sort steps.
        The default is on.
       

      

     

     

      
enable_indexscan (boolean)
      
       index scan
      
      
       enable_indexscan configuration parameter
      
      

      

       

        Enables or disables the query planner's use of index-scan and
        index-only-scan plan types.  The default is on.
        Also see .
       

      

     

     

      
enable_indexonlyscan (boolean)
      
       enable_indexonlyscan configuration parameter
      
      

      

       

        Enables or disables the query planner's use of index-only-scan plan
        types (see ).
        The default is on.  The
         setting must also be
        enabled to have the query planner consider index-only-scans.
       

      

     

     

      
enable_material (boolean)
      
       enable_material configuration parameter
      
      

      

       

        Enables or disables the query planner's use of materialization.
        It is impossible to suppress materialization entirely,
        but turning this variable off prevents the planner from inserting
        materialize nodes except in cases where it is required for correctness.
        The default is on.
       

      

     

     

      
enable_memoize (boolean)
      
       enable_memoize configuration parameter
      
      

      

       

        Enables or disables the query planner's use of memoize plans for
        caching results from parameterized scans inside nested-loop joins.
        This plan type allows scans to the underlying plans to be skipped when
        the results for the current parameters are already in the cache.  Less
        commonly looked up results may be evicted from the cache when more
        space is required for new entries. The default is
        on.
       

      

     

     

      
enable_mergejoin (boolean)
      
       enable_mergejoin configuration parameter
      
      

      

       

        Enables or disables the query planner's use of merge-join plan
        types. The default is on.
       

      

     

     

      
enable_nestloop (boolean)
      
       enable_nestloop configuration parameter
      
      

      

       

        Enables or disables the query planner's use of nested-loop join
        plans. It is impossible to suppress nested-loop joins entirely,
        but turning this variable off discourages the planner from using
        one if there are other methods available. The default is
        on.
       

      

     

     

      
enable_parallel_append (boolean)
      
       enable_parallel_append configuration parameter
      
      

      

       

        Enables or disables the query planner's use of parallel-aware
        append plan types. The default is on.
       

      

     

     

      
enable_parallel_hash (boolean)
       
        enable_parallel_hash configuration parameter
       
      

      

       

        Enables or disables the query planner's use of hash-join plan
        types with parallel hash. Has no effect if hash-join plans are not
        also enabled. The default is on.
       

      

     

     

      
enable_partition_pruning (boolean)
       
        enable_partition_pruning configuration parameter
       
      

      

       

        Enables or disables the query planner's ability to eliminate a
        partitioned table's partitions from query plans.  This also controls
        the planner's ability to generate query plans which allow the query
        executor to remove (ignore) partitions during query execution.  The
        default is on.
        See  for details.
       

      

     

     

      
enable_partitionwise_join (boolean)
      
       enable_partitionwise_join configuration parameter
      
      

      

       

        Enables or disables the query planner's use of partitionwise join,
        which allows a join between partitioned tables to be performed by
        joining the matching partitions.  Partitionwise join currently applies
        only when the join conditions include all the partition keys, which
        must be of the same data type and have one-to-one matching sets of
        child partitions.  With this setting enabled, the number of nodes
        whose memory usage is restricted by work_mem
        appearing in the final plan can increase linearly according to the
        number of partitions being scanned.  This can result in a large
        increase in overall memory consumption during the execution of the
        query.  Query planning also becomes significantly more expensive in
        terms of memory and CPU.  The default value is off.
       

      

     

     

      
enable_partitionwise_aggregate (boolean)
      
       enable_partitionwise_aggregate configuration parameter
      
      

      

       

        Enables or disables the query planner's use of partitionwise grouping
        or aggregation, which allows grouping or aggregation on partitioned
        tables to be performed separately for each partition.  If the
        GROUP BY clause does not include the partition
        keys, only partial aggregation can be performed on a per-partition
        basis, and finalization must be performed later.  With this setting
        enabled, the number of nodes whose memory usage is restricted by
        work_mem appearing in the final plan can increase
        linearly according to the number of partitions being scanned.  This
        can result in a large increase in overall memory consumption during
        the execution of the query.  Query planning also becomes significantly
        more expensive in terms of memory and CPU.  The default value is
        off.
       

      

     

     

      
enable_presorted_aggregate (boolean)
      
       enable_presorted_aggregate configuration parameter
      
      

      

       

        Controls if the query planner will produce a plan which will provide
        rows which are presorted in the order required for the query's
        ORDER BY / DISTINCT aggregate
        functions.  When disabled, the query planner will produce a plan which
        will always require the executor to perform a sort before performing
        aggregation of each aggregate function containing an
        ORDER BY or DISTINCT clause.
        When enabled, the planner will try to produce a more efficient plan
        which provides input to the aggregate functions which is presorted in
        the order they require for aggregation.  The default value is
        on.
       

      

     

     

      
enable_self_join_elimination (boolean)
      
       enable_self_join_elimination configuration parameter
      
      

      

       

        Enables or disables the query planner's optimization which analyses
        the query tree and replaces self joins with semantically equivalent
        single scans.  Takes into consideration only plain tables.
        The default is on.
       

      

     

     

      
enable_seqscan (boolean)
      
       sequential scan
      
      
       enable_seqscan configuration parameter
      
      

      

       

        Enables or disables the query planner's use of sequential scan
        plan types. It is impossible to suppress sequential scans
        entirely, but turning this variable off discourages the planner
        from using one if there are other methods available. The
        default is on.
       

      

     

     

      
enable_sort (boolean)
      
       enable_sort configuration parameter
      
      

      

       

        Enables or disables the query planner's use of explicit sort
        steps. It is impossible to suppress explicit sorts entirely,
        but turning this variable off discourages the planner from
        using one if there are other methods available. The default
        is on.
       

      

     

     

      
enable_tidscan (boolean)
      
       enable_tidscan configuration parameter
      
      

      

       

        Enables or disables the query planner's use of TID
        scan plan types. The default is on.
       

      

     

     

     

     

     
# Planner Cost Constants

    

     The cost variables described in this section are measured
     on an arbitrary scale.  Only their relative values matter, hence
     scaling them all up or down by the same factor will result in no change
     in the planner's choices.  By default, these cost variables are based on
     the cost of sequential page fetches; that is,
     seq_page_cost is conventionally set to 1.0
     and the other cost variables are set with reference to that.  But
     you can use a different scale if you prefer, such as actual execution
     times in milliseconds on a particular machine.
    

   
    

     Unfortunately, there is no well-defined method for determining ideal
     values for the cost variables.  They are best treated as averages over
     the entire mix of queries that a particular installation will receive.  This
     means that changing them on the basis of just a few experiments is very
     risky.
    

   

     

     

      
seq_page_cost (floating point)
      
       seq_page_cost configuration parameter
      
      

      

       

        Sets the planner's estimate of the cost of a disk page fetch
        that is part of a series of sequential fetches.  The default is 1.0.
        This value can be overridden for tables and indexes in a particular
        tablespace by setting the tablespace parameter of the same name
        (see ).
       

      

     

     

      
random_page_cost (floating point)
      
       random_page_cost configuration parameter
      
      

      

       

        Sets the planner's estimate of the cost of a
        non-sequentially-fetched disk page.  The default is 4.0.
        This value can be overridden for tables and indexes in a particular
        tablespace by setting the tablespace parameter of the same name
        (see ).
       

       

        Reducing this value relative to seq_page_cost
        will cause the system to prefer index scans; raising it will
        make index scans look relatively more expensive.  You can raise
        or lower both values together to change the importance of disk I/O
        costs relative to CPU costs, which are described by the following
        parameters.
       

       

        Random access to durable storage is normally much more expensive
        than four times sequential access.  However, a lower default is
        used (4.0) because the majority of random accesses to storage,
        such as indexed reads, are assumed to be in cache.  Also, the
        latency of network-attached storage tends to reduce the relative
        overhead of random access.
       

       

        If you believe caching is less frequent than the default
        value reflects, and network latency is minimal, you can increase
        random_page_cost to better reflect the true cost of random storage
        reads.  Storage that has a higher random read cost relative to
        sequential, like magnetic disks, might also be better modeled with
        a higher value for random_page_cost.  Correspondingly, if your data
        is likely to be completely in cache, such as when the database
        is smaller than the total server memory, or network latency is
        high, decreasing random_page_cost might be appropriate.
       

       
        

         Although the system will let you set random_page_cost to
         less than seq_page_cost, it is not physically sensible
         to do so.  However, setting them equal makes sense if the database
         is entirely cached in RAM, since in that case there is no penalty
         for touching pages out of sequence.  Also, in a heavily-cached
         database you should lower both values relative to the CPU parameters,
         since the cost of fetching a page already in RAM is much smaller
         than it would normally be.
        

       
      

     

     

      
cpu_tuple_cost (floating point)
      
       cpu_tuple_cost configuration parameter
      
      

      

       

        Sets the planner's estimate of the cost of processing
        each row during a query.
        The default is 0.01.
       

      

     

     

      
cpu_index_tuple_cost (floating point)
      
       cpu_index_tuple_cost configuration parameter
      
      

      

       

        Sets the planner's estimate of the cost of processing
        each index entry during an index scan.
        The default is 0.005.
       

      

     

     

      
cpu_operator_cost (floating point)
      
       cpu_operator_cost configuration parameter
      
      

      

       

        Sets the planner's estimate of the cost of processing each
        operator or function executed during a query.
        The default is 0.0025.
       

      

     

     

      
parallel_setup_cost (floating point)
      
       parallel_setup_cost configuration parameter
      
      

      

       

        Sets the planner's estimate of the cost of launching parallel worker
        processes.
        The default is 1000.
       

      

     

     

      
parallel_tuple_cost (floating point)
      
       parallel_tuple_cost configuration parameter
      
      

      

       

        Sets the planner's estimate of the cost of transferring one tuple
        from a parallel worker process to another process.
        The default is 0.1.
       

      

     

     

      
min_parallel_table_scan_size (integer)
      
       min_parallel_table_scan_size configuration parameter
      
      

      

       

        Sets the minimum amount of table data that must be scanned in order
        for a parallel scan to be considered.  For a parallel sequential scan,
        the amount of table data scanned is always equal to the size of the
        table, but when indexes are used the amount of table data
        scanned will normally be less.
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default is 8 megabytes (8MB).
       

      

     

     

      
min_parallel_index_scan_size (integer)
      
       min_parallel_index_scan_size configuration parameter
      
      

      

       

        Sets the minimum amount of index data that must be scanned in order
        for a parallel scan to be considered.  Note that a parallel index scan
        typically won't touch the entire index; it is the number of pages
        which the planner believes will actually be touched by the scan which
        is relevant.  This parameter is also used to decide whether a
        particular index can participate in a parallel vacuum.  See
        .
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default is 512 kilobytes (512kB).
       

      

     

     

      
effective_cache_size (integer)
      
       effective_cache_size configuration parameter
      
      

      

       

        Sets the planner's assumption about the effective size of the
        disk cache that is available to a single query.  This is
        factored into estimates of the cost of using an index; a
        higher value makes it more likely index scans will be used, a
        lower value makes it more likely sequential scans will be
        used. When setting this parameter you should consider both
        PostgreSQL's shared buffers and the
        portion of the kernel's disk cache that will be used for
        PostgreSQL data files, though some
        data might exist in both places. Also, take
        into account the expected number of concurrent queries on different
        tables, since they will have to share the available
        space.  This parameter has no effect on the size of shared
        memory allocated by PostgreSQL, nor
        does it reserve kernel disk cache; it is used only for estimation
        purposes.  The system also does not assume data remains in
        the disk cache between queries.
        If this value is specified without units, it is taken as blocks,
        that is BLCKSZ bytes, typically 8kB.
        The default is 4 gigabytes (4GB).
        (If BLCKSZ is not 8kB, the default value scales
        proportionally to it.)
       

      

     

     

      
min_eager_agg_group_size (floating point)
      
       min_eager_agg_group_size configuration parameter
      
      

      

       

        Sets the minimum average group size required to consider applying
        eager aggregation. This helps avoid the overhead of eager
        aggregation when it does not offer significant row count reduction.
        The default is 8.
       

      

     

     

      
jit_above_cost (floating point)
      
       jit_above_cost configuration parameter
      
      

      

       

        Sets the query cost above which JIT compilation is activated, if
        enabled (see ).
        Performing JIT costs planning time but can
        accelerate query execution.
        Setting this to -1 disables JIT compilation.
        The default is 100000.
       

      

     

     

      
jit_inline_above_cost (floating point)
      
       jit_inline_above_cost configuration parameter
      
      

      

       

        Sets the query cost above which JIT compilation attempts to inline
        functions and operators.  Inlining adds planning time, but can
        improve execution speed.  It is not meaningful to set this to less
        than jit_above_cost.
        Setting this to -1 disables inlining.
        The default is 500000.
       

      

     

     

      
jit_optimize_above_cost (floating point)
      
       jit_optimize_above_cost configuration parameter
      
      

      

       

        Sets the query cost above which JIT compilation applies expensive
        optimizations.  Such optimization adds planning time, but can improve
        execution speed.  It is not meaningful to set this to less
        than jit_above_cost, and it is unlikely to be
        beneficial to set it to more
        than jit_inline_above_cost.
        Setting this to -1 disables expensive optimizations.
        The default is 500000.
       

      

     

     

    

     

     
# Genetic Query Optimizer

     

      The genetic query optimizer (GEQO) is an algorithm that does query
      planning using heuristic searching.  This reduces planning time for
      complex queries (those joining many relations), at the cost of producing
      plans that are sometimes inferior to those found by the normal
      exhaustive-search algorithm.
      For more information see .
     

     

     

      
geqo (boolean)
      
       genetic query optimization
      
      
       GEQO
       genetic query optimization
      
      
       geqo configuration parameter
      
      

      

       

        Enables or disables genetic query optimization.
        This is on by default.  It is usually best not to turn it off in
        production; the geqo_threshold variable provides
        more granular control of GEQO.
       

      

     

     

      
geqo_threshold (integer)
      
       geqo_threshold configuration parameter
      
      

      

       

        Use genetic query optimization to plan queries with at least
        this many FROM items involved. (Note that a
        FULL OUTER JOIN construct counts as only one FROM
        item.) The default is 12. For simpler queries it is usually best
        to use the regular, exhaustive-search planner, but for queries with
        many tables the exhaustive search takes too long, often
        longer than the penalty of executing a suboptimal plan.  Thus,
        a threshold on the size of the query is a convenient way to manage
        use of GEQO.
       

      

     

     

      
geqo_effort (integer)
      
       geqo_effort configuration parameter
      
      

      

       

        Controls the trade-off between planning time and query plan
        quality in GEQO. This variable must be an integer in the
        range from 1 to 10. The default value is five. Larger values
        increase the time spent doing query planning, but also
        increase the likelihood that an efficient query plan will be
        chosen.
       

       

        geqo_effort doesn't actually do anything
        directly; it is only used to compute the default values for
        the other variables that influence GEQO behavior (described
        below). If you prefer, you can set the other parameters by
        hand instead.
       

      

     

     

      
geqo_pool_size (integer)
      
       geqo_pool_size configuration parameter
      
      

      

       

        Controls the pool size used by GEQO, that is the
        number of individuals in the genetic population.  It must be
        at least two, and useful values are typically 100 to 1000.  If
        it is set to zero (the default setting) then a suitable
        value is chosen based on geqo_effort and
        the number of tables in the query.
       

      

     

     

      
geqo_generations (integer)
      
       geqo_generations configuration parameter
      
      

      

       

        Controls the number of generations used by GEQO, that is
        the number of iterations of the algorithm.  It must
        be at least one, and useful values are in the same range as
        the pool size.  If it is set to zero (the default setting)
        then a suitable value is chosen based on
        geqo_pool_size.
       

      

     

     

      
geqo_selection_bias (floating point)
      
       geqo_selection_bias configuration parameter
      
      

      

       

        Controls the selection bias used by GEQO. The selection bias
        is the selective pressure within the population. Values can be
        from 1.50 to 2.00; the latter is the default.
       

      

     

     

      
geqo_seed (floating point)
      
       geqo_seed configuration parameter
      
      

      

       

        Controls the initial value of the random number generator used
        by GEQO to select random paths through the join order search space.
        The value can range from zero (the default) to one.  Varying the
        value changes the set of join paths explored, and may result in a
        better or worse best path being found.
       

      

     

     

    

     

     
# Other Planner Options

     

     

      
default_statistics_target (integer)
      
       default_statistics_target configuration parameter
      
      

      

       

        Sets the default statistics target for table columns without
        a column-specific target set via ALTER TABLE
        SET STATISTICS.  Larger values increase the time needed to
        do ANALYZE, but might improve the quality of the
        planner's estimates. The default is 100. For more information
        on the use of statistics by the PostgreSQL
        query planner, refer to .
       

      

     

     

      
constraint_exclusion (enum)
      
       constraint exclusion
      
      
       constraint_exclusion configuration parameter
      
      

      

       

        Controls the query planner's use of table constraints to
        optimize queries.
        The allowed values of constraint_exclusion are
        on (examine constraints for all tables),
        off (never examine constraints), and
        partition (examine constraints only for inheritance
        child tables and UNION ALL subqueries).
        partition is the default setting.
        It is often used with traditional inheritance trees to improve
        performance.
      

       

        When this parameter allows it for a particular table, the planner
        compares query conditions with the table's CHECK
        constraints, and omits scanning tables for which the conditions
        contradict the constraints.  For example:

```
CREATE TABLE parent(key integer, ...);
CREATE TABLE child1000(CHECK (key BETWEEN 1000 AND 1999)) INHERITS(parent);
CREATE TABLE child2000(CHECK (key BETWEEN 2000 AND 2999)) INHERITS(parent);
...
SELECT * FROM parent WHERE key = 2400;
```

        With constraint exclusion enabled, this SELECT
        will not scan child1000 at all, improving performance.
       

       

        Currently, constraint exclusion is enabled by default
        only for cases that are often used to implement table partitioning via
        inheritance trees.  Turning it on for all tables imposes extra
        planning overhead that is quite noticeable on simple queries, and most
        often will yield no benefit for simple queries.  If you have no
        tables that are partitioned using traditional inheritance, you might
        prefer to turn it off entirely.  (Note that the equivalent feature for
        partitioned tables is controlled by a separate parameter,
        .)
       

       

        Refer to  for
        more information on using constraint exclusion to implement
        partitioning.
       

      

     

     

      
cursor_tuple_fraction (floating point)
      
       cursor_tuple_fraction configuration parameter
      
      

      

       

        Sets the planner's estimate of the fraction of a cursor's rows that
        will be retrieved.  The default is 0.1.  Smaller values of this
        setting bias the planner towards using fast start plans
        for cursors, which will retrieve the first few rows quickly while
        perhaps taking a long time to fetch all rows.  Larger values
        put more emphasis on the total estimated time.  At the maximum
        setting of 1.0, cursors are planned exactly like regular queries,
        considering only the total estimated time and not how soon the
        first rows might be delivered.
       

      

     

     

      
from_collapse_limit (integer)
      
       from_collapse_limit configuration parameter
      
      

      

       

        The planner will merge sub-queries into upper queries if the
        resulting FROM list would have no more than
        this many items.  Smaller values reduce planning time but might
        yield inferior query plans.  The default is eight.
        For more information see .
       

       

        Setting this value to  or more
        may trigger use of the GEQO planner, resulting in non-optimal
        plans.  See .
       

      

     

     

      
jit (boolean)
      
       jit configuration parameter
      
      

      

       

        Determines whether JIT compilation may be used by
        PostgreSQL, if available (see ).
        The default is off.
       

      

     

     

      
join_collapse_limit (integer)
      
       join_collapse_limit configuration parameter
      
      

      

       

        The planner will rewrite explicit JOIN
        constructs (except FULL JOINs) into lists of
        FROM items whenever a list of no more than this many items
        would result.  Smaller values reduce planning time but might
        yield inferior query plans.
       

       

        By default, this variable is set the same as
        from_collapse_limit, which is appropriate
        for most uses. Setting it to 1 prevents any reordering of
        explicit JOINs. Thus, the explicit join order
        specified in the query will be the actual order in which the
        relations are joined. Because the query planner does not always choose
        the optimal join order, advanced users can elect to
        temporarily set this variable to 1, and then specify the join
        order they desire explicitly.
        For more information see .
       

       

        Setting this value to  or more
        may trigger use of the GEQO planner, resulting in non-optimal
        plans.  See .
       

      

     

     

      
plan_cache_mode (enum)
      
       plan_cache_mode configuration parameter
      
      

      

       

        Prepared statements (either explicitly prepared or implicitly
        generated, for example by PL/pgSQL) can be executed using custom or
        generic plans.  Custom plans are made afresh for each execution
        using its specific set of parameter values, while generic plans do
        not rely on the parameter values and can be re-used across
        executions.  Thus, use of a generic plan saves planning time, but if
        the ideal plan depends strongly on the parameter values then a
        generic plan may be inefficient.  The choice between these options
        is normally made automatically, but it can be overridden
        with plan_cache_mode.
        The allowed values are auto (the default),
        force_custom_plan and
        force_generic_plan.
        This setting is considered when a cached plan is to be executed,
        not when it is prepared.
        For more information see .
       

      

     

     

      
recursive_worktable_factor (floating point)
      
       recursive_worktable_factor configuration parameter
      
      

      

       

        Sets the planner's estimate of the average size of the working
        table of a recursive
        query, as a multiple of the estimated size of the initial
        non-recursive term of the query.  This helps the planner choose
        the most appropriate method for joining the working table to the
        query's other tables.
        The default value is 10.0.  A smaller value
        such as 1.0 can be helpful when the recursion
        has low fan-out from one step to the next, as for
        example in shortest-path queries.  Graph analytics queries may
        benefit from larger-than-default values.
       

      

     

     

    

   

   

    
# Error Reporting and Logging

    
     server log
    

    

     
# Where to Log

     
      where to log
     

     
       current_logfiles
       and the log_destination configuration parameter
     

     

     

      
log_destination (string)
      
       log_destination configuration parameter
      
      

      

       

        PostgreSQL supports several methods
         for logging server messages, including
         stderr, csvlog,
         jsonlog, and
         syslog. On Windows,
         eventlog is also supported. Set this
         parameter to a list of desired log destinations separated by
         commas. The default is to log to stderr
         only.
         This parameter can only be set in the postgresql.conf
         file or on the server command line.
       

       

        If csvlog is included in log_destination,
        log entries are output in comma-separated
        value (CSV) format, which is convenient for
        loading logs into programs.
        See  for details.
         must be enabled to generate
        CSV-format log output.
       

       

        If jsonlog is included in
        log_destination, log entries are output in
        JSON format, which is convenient for loading logs
        into programs.
        See  for details.
         must be enabled to generate
        JSON-format log output.
       

       

        When either stderr,
        csvlog or jsonlog are
        included, the file current_logfiles is created to
        record the location of the log file(s) currently in use by the logging
        collector and the associated logging destination. This provides a
        convenient way to find the logs currently in use by the instance. Here
        is an example of this file's content:

```
stderr log/postgresql.log
csvlog log/postgresql.csv
jsonlog log/postgresql.json
```

        current_logfiles is recreated when a new log file
        is created as an effect of rotation, and
        when log_destination is reloaded.  It is removed when
        none of stderr,
        csvlog or jsonlog are
        included in log_destination, and when the logging
        collector is disabled.
       

       
        

         On most Unix systems, you will need to alter the configuration of
         your system's syslog daemon in order
         to make use of the syslog option for
         log_destination.  PostgreSQL
         can log to syslog facilities
         LOCAL0 through LOCAL7 (see ), but the default
         syslog configuration on most platforms
         will discard all such messages.  You will need to add something like:

```
local0.*    /var/log/postgresql
```

         to the  syslog daemon's configuration file
         to make it work.
        

        

         On Windows, when you use the eventlog
         option for log_destination, you should
         register an event source and its library with the operating
         system so that the Windows Event Viewer can display event
         log messages cleanly.
         See  for details.
        

       
      

     

     

      
logging_collector (boolean)
      
       logging_collector configuration parameter
      
      

      

       

         This parameter enables the logging collector, which
         is a background process that captures log messages
         sent to stderr and redirects them into log files.
         This approach is often more useful than
         logging to syslog, since some types of messages
         might not appear in syslog output.  (One common
         example is dynamic-linker failure messages; another is error messages
         produced by scripts such as archive_command.)
         This parameter can only be set at server start.
       

       
        

         It is possible to log to stderr without using the
         logging collector; the log messages will just go to wherever the
         server's stderr is directed.  However, that method is
         only suitable for low log volumes, since it provides no convenient
         way to rotate log files.  Also, on some platforms not using the
         logging collector can result in lost or garbled log output, because
         multiple processes writing concurrently to the same log file can
         overwrite each other's output.
        

       

       
        

          The logging collector is designed to avoid dropping messages.  This means
          that in case of extremely high load, server processes could be
          blocked while trying to send additional log messages when the
          collector has fallen behind.  In contrast, syslog
          prefers to drop messages if it cannot write them, which means it
          may fail to log some messages in such cases but it will not block
          the rest of the system.
        

        

         The logging collector does not guarantee that log messages have
         reached durable storage.  It can still lose messages due to a system
         crash, power loss, or an error while writing the log file.
        

       

      

     

     

      
log_directory (string)
      
       log_directory configuration parameter
      
      

      

       

        When logging_collector is enabled,
        this parameter determines the directory in which log files will be created.
        It can be specified as an absolute path, or relative to the
        cluster data directory.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
        The default is log.
       

      

     

     

      
log_filename (string)
      
       log_filename configuration parameter
      
      

      

       

        When logging_collector is enabled,
        this parameter sets the file names of the created log files.  The value
        is treated as a strftime pattern,
        so %-escapes can be used to specify time-varying
        file names.  (Note that if there are
        any time-zone-dependent %-escapes, the computation
        is done in the zone specified
        by .)
        The supported %-escapes are similar to those
        listed in the Open Group's strftime
         specification.
        Note that the system's strftime is not used
        directly, so platform-specific (nonstandard) extensions do not work.
        The default is postgresql-%Y-%m-%d_%H%M%S.log.
       

       

        If you specify a file name without escapes, you should plan to
        use a log rotation utility to avoid eventually filling the
        entire disk.  In releases prior to 8.4, if
        no % escapes were
        present, PostgreSQL would append
        the epoch of the new log file's creation time, but this is no
        longer the case.
       

       

        If CSV-format output is enabled in log_destination,
        .csv will be appended to the timestamped
        log file name to create the file name for CSV-format output.
        (If log_filename ends in .log, the suffix is
        replaced instead.)
       

       

        If JSON-format output is enabled in log_destination,
        .json will be appended to the timestamped
        log file name to create the file name for JSON-format output.
        (If log_filename ends in .log, the suffix is
        replaced instead.)
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
log_file_mode (integer)
      
       log_file_mode configuration parameter
      
      

      

       

        On Unix systems this parameter sets the permissions for log files
        when logging_collector is enabled. (On Microsoft
        Windows this parameter is ignored.)
        The parameter value is expected to be a numeric mode
        specified in the format accepted by the
        chmod and umask
        system calls.  (To use the customary octal format the number
        must start with a 0 (zero).)
       

       

        The default permissions are 0600, meaning only the
        server owner can read or write the log files.  The other commonly
        useful setting is 0640, allowing members of the owner's
        group to read the files.  Note however that to make use of such a
        setting, you'll need to alter  to
        store the files somewhere outside the cluster data directory.  In
        any case, it's unwise to make the log files world-readable, since
        they might contain sensitive data.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
log_rotation_age (integer)
      
       log_rotation_age configuration parameter
      
      

      

       

        When logging_collector is enabled,
        this parameter determines the maximum amount of time to use an
        individual log file, after which a new log file will be created.
        If this value is specified without units, it is taken as minutes.
        The default is 24 hours.
        Set to zero to disable time-based creation of new log files.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
log_rotation_size (integer)
      
       log_rotation_size configuration parameter
      
      

      

       

        When logging_collector is enabled,
        this parameter determines the maximum size of an individual log file.
        After this amount of data has been emitted into a log file,
        a new log file will be created.
        If this value is specified without units, it is taken as kilobytes.
        The default is 10 megabytes.
        Set to zero to disable size-based creation of new log files.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
log_truncate_on_rotation (boolean)
      
       log_truncate_on_rotation configuration parameter
      
      

      

       

        When logging_collector is enabled,
        this parameter will cause PostgreSQL to truncate (overwrite),
        rather than append to, any existing log file of the same name.
        However, truncation will occur only when a new file is being opened
        due to time-based rotation, not during server startup or size-based
        rotation.  When off, pre-existing files will be appended to in
        all cases.  For example, using this setting in combination with
        a log_filename like postgresql-%H.log
        would result in generating twenty-four hourly log files and then
        cyclically overwriting them.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

       

        Example:  To keep 7 days of logs, one log file per day named
        server_log.Mon, server_log.Tue,
        etc., and automatically overwrite last week's log with this week's log,
        set log_filename to server_log.%a,
        log_truncate_on_rotation to on, and
        log_rotation_age to 1440.
       

       

        Example: To keep 24 hours of logs, one log file per hour, but
        also rotate sooner if the log file size exceeds 1GB, set
        log_filename to server_log.%H%M,
        log_truncate_on_rotation to on,
        log_rotation_age to 60, and
        log_rotation_size to 1000000.
        Including %M in log_filename allows
        any size-driven rotations that might occur to select a file name
        different from the hour's initial file name.
       

      

     

     

      
syslog_facility (enum)
      
       syslog_facility configuration parameter
      
      

      

       

        When logging to syslog is enabled, this parameter
        determines the syslog
        facility to be used.  You can choose
        from LOCAL0, LOCAL1,
        LOCAL2, LOCAL3, LOCAL4,
        LOCAL5, LOCAL6, LOCAL7;
        the default is LOCAL0. See also the
        documentation of your system's
        syslog daemon.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
syslog_ident (string)
      
       syslog_ident configuration parameter
      
      

       

        

         When logging to syslog is enabled, this parameter
         determines the program name used to identify
         PostgreSQL messages in
         syslog logs. The default is
         postgres.
         This parameter can only be set in the postgresql.conf
         file or on the server command line.
        

       

      

      

       
syslog_sequence_numbers (boolean)
        
         syslog_sequence_numbers configuration parameter
        
       

       

        

         When logging to syslog and this is on (the
         default), then each message will be prefixed by an increasing
         sequence number (such as [2]).  This circumvents
         the --- last message repeated N times --- suppression
         that many syslog implementations perform by default.  In more modern
         syslog implementations, repeated message suppression can be configured
         (for example, $RepeatedMsgReduction
         in rsyslog), so this might not be
         necessary.  Also, you could turn this off if you actually want to
         suppress repeated messages.
        

        

         This parameter can only be set in the postgresql.conf
         file or on the server command line.
        

      

     

     

      
syslog_split_messages (boolean)
      
       syslog_split_messages configuration parameter
      
      

      

       

        When logging to syslog is enabled, this parameter
        determines how messages are delivered to syslog.  When on (the
        default), messages are split by lines, and long lines are split so
        that they will fit into 1024 bytes, which is a typical size limit for
        traditional syslog implementations.  When off, PostgreSQL server log
        messages are delivered to the syslog service as is, and it is up to
        the syslog service to cope with the potentially bulky messages.
       

       

        If syslog is ultimately logging to a text file, then the effect will
        be the same either way, and it is best to leave the setting on, since
        most syslog implementations either cannot handle large messages or
        would need to be specially configured to handle them.  But if syslog
        is ultimately writing into some other medium, it might be necessary or
        more useful to keep messages logically together.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
event_source (string)
      
       event_source configuration parameter
      
      

      

       

        When logging to event log is enabled, this parameter
        determines the program name used to identify
        PostgreSQL messages in
        the log. The default is PostgreSQL.
        This parameter can only be set at server start.
       

      

     

      

    

     

     
# When to Log

     

     

      
log_min_messages (string)
      
       log_min_messages configuration parameter
      
      

      

       

        Controls which
        message levels
        are written to the server log.  The value is a comma-separated
        list of zero or more
        process type:level
        entries and exactly one mandatory
        level entry,
        which becomes the default for process types not listed.
        Valid process types are listed in the table below.
        
         archiver
         autovacuum
         backend
         bgworker
         bgwriter
         checkpointer
         checksums
         ioworker
         postmaster
         slotsyncworker
         startup
         syslogger
         walreceiver
         walsender
         walsummarizer
         walwriter
        
        Valid level values are DEBUG5,
        DEBUG4, DEBUG3, DEBUG2,
        DEBUG1, INFO, NOTICE,
        WARNING, ERROR, LOG,
        FATAL, and PANIC.  Each level includes
        all the levels that follow it.  The later the level, the fewer messages are sent
        to the log.  The default is WARNING, which
        applies that level to all process types.
        Note that LOG has a different rank here than in
        .
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        Example: To log walsender and autovacuum
        at level DEBUG1 and everything else at ERROR,
        set log_min_messages to error, walsender:debug1, autovacuum:debug1.
       

      

     

     

      
log_min_error_statement (enum)
      
       log_min_error_statement configuration parameter
      
      

      

       

        Controls which SQL statements that cause an error
        condition are recorded in the server log.  The current
        SQL statement is included in the log entry for any message of
        the specified
        severity
        or higher.
        Valid values are DEBUG5,
        DEBUG4, DEBUG3,
        DEBUG2, DEBUG1,
        INFO, NOTICE,
        WARNING, ERROR,
        LOG,
        FATAL, and PANIC.
        The default is ERROR, which means statements
        causing errors, log messages, fatal errors, or panics will be logged.
        To effectively turn off logging of failing statements,
        set this parameter to PANIC.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
log_min_duration_statement (integer)
      
       log_min_duration_statement configuration parameter
      
      

       

        

         Causes the duration of each completed statement to be logged
         if the statement ran for at least the specified amount of time.
         For example, if you set it to 250ms
         then all SQL statements that run 250ms or longer will be
         logged.  Enabling this parameter can be helpful in tracking down
         unoptimized queries in your applications.
         If this value is specified without units, it is taken as milliseconds.
         Setting this to zero prints all statement durations.
         -1 (the default) disables logging statement
         durations.
         Only superusers and users with the appropriate SET
         privilege can change this setting.
        

        

         This overrides ,
         meaning that queries with duration exceeding this setting are not
         subject to sampling and are always logged.
        

        

         For clients using extended query protocol, durations of the Parse,
         Bind, and Execute steps are logged independently.
        

       
        

         When using this option together with
         ,
         the text of statements that are logged because of
         log_statement will not be repeated in the
         duration log message.
         If you are not using syslog, it is recommended
         that you log the PID or session ID using
         
         so that you can link the statement message to the later
         duration message using the process ID or session ID.
        

       
       

      

     

      
log_min_duration_sample (integer)
      
       log_min_duration_sample configuration parameter
      
      

       

        

         Allows sampling the duration of completed statements that ran for
         at least the specified amount of time.  This produces the same
         kind of log entries as
         , but only for a
         subset of the executed statements, with sample rate controlled by
         .
         For example, if you set it to 100ms then all
         SQL statements that run 100ms or longer will be considered for
         sampling.  Enabling this parameter can be helpful when the
         traffic is too high to log all queries.
         If this value is specified without units, it is taken as milliseconds.
         Setting this to zero samples all statement durations.
         -1 (the default) disables sampling statement
         durations.
         Only superusers and users with the appropriate SET
         privilege can change this setting.
        

        

         This setting has lower priority
         than log_min_duration_statement, meaning that
         statements with durations
         exceeding log_min_duration_statement are not
         subject to sampling and are always logged.
        

        

         Other notes for log_min_duration_statement
         apply also to this setting.
        

       

     

     

      
log_statement_sample_rate (floating point)
      
       log_statement_sample_rate configuration parameter
      
      

       

        

         Determines the fraction of statements with duration exceeding
          that will be logged.
         Sampling is stochastic, for example 0.5 means
         there is statistically one chance in two that any given statement
         will be logged.
         The default is 1.0, meaning to log all sampled
         statements.
         Setting this to zero disables sampled statement-duration logging,
         the same as setting
         log_min_duration_sample to
         -1.
         Only superusers and users with the appropriate SET
         privilege can change this setting.
        

       

     

     

      
log_transaction_sample_rate (floating point)
      
       log_transaction_sample_rate configuration parameter
      
      

       

        

         Sets the fraction of transactions whose statements are all logged,
         in addition to statements logged for other reasons.  It applies to
         each new transaction regardless of its statements' durations.
         Sampling is stochastic, for example 0.1 means
         there is statistically one chance in ten that any given transaction
         will be logged.
         log_transaction_sample_rate can be helpful to
         construct a sample of transactions.
         The default is 0, meaning not to log
         statements from any additional transactions.  Setting this
         to 1 logs all statements of all transactions.
         Only superusers and users with the appropriate SET
         privilege can change this setting.
        

       
        

         Like all statement-logging options, this option can add significant
         overhead.
        

       
       

     

     

      
log_startup_progress_interval (integer)
      
       log_startup_progress_interval configuration parameter
      
      

       

        

         Sets the amount of time after which the startup process will log
         a message about a long-running operation that is still in progress,
         as well as the interval between further progress messages for that
         operation. The default is 10 seconds. A setting of 0
         disables the feature.  If this value is specified without units,
         it is taken as milliseconds.  This setting is applied separately to
         each operation.
         This parameter can only be set in the postgresql.conf
         file or on the server command line.
        

        

         For example, if syncing the data directory takes 25 seconds and
         thereafter resetting unlogged relations takes 8 seconds, and if this
         setting has the default value of 10 seconds, then a messages will be
         logged for syncing the data directory after it has been in progress
         for 10 seconds and again after it has been in progress for 20 seconds,
         but nothing will be logged for resetting unlogged relations.
        

       

     

     

    

      explains the message
     severity levels used by PostgreSQL.  If logging output
     is sent to syslog or Windows'
     eventlog, the severity levels are translated
     as shown in the table.
    

    
     
# Message Severity Levels

     
      
      
      
      
      
       
        Severity
        Usage
        syslog
        eventlog
       
      

      
       
        DEBUG1 .. DEBUG5
        Provides successively-more-detailed information for use by
         developers.
        DEBUG
        INFORMATION
       

       
        INFO
        Provides information implicitly requested by the user,
         e.g., output from VACUUM VERBOSE.
        INFO
        INFORMATION
       

       
        NOTICE
        Provides information that might be helpful to users, e.g.,
         notice of truncation of long identifiers.
        NOTICE
        INFORMATION
       

       
        WARNING
        Provides warnings of likely problems, e.g., COMMIT
         outside a transaction block.
        NOTICE
        WARNING
       

       
        ERROR
        Reports an error that caused the current command to
         abort.
        WARNING
        ERROR
       

       
        LOG
        Reports information of interest to administrators, e.g.,
         checkpoint activity.
        INFO
        INFORMATION
       

       
        FATAL
        Reports an error that caused the current session to
         abort.
        ERR
        ERROR
       

       
        PANIC
        Reports an error that caused all database sessions to abort.
        CRIT
        ERROR
       
      
     
    

    

     

     
# What to Log

     
      

       What you choose to log can have security implications;  see
       .
      

     

     

     

      
application_name (string)
      
       application_name configuration parameter
      
      

      

       

        The application_name can be any string of less than
        NAMEDATALEN characters (64 characters in a standard build).
        It is typically set by an application upon connection to the server.
        The name will be displayed in the pg_stat_activity view
        and included in CSV log entries.  It can also be included in regular
        log entries via the  parameter.
        Only printable ASCII characters may be used in the
        application_name value.
        Other characters are replaced with C-style hexadecimal escapes.
       

      

     

     

      
debug_print_raw_parse (boolean)
      
       debug_print_raw_parse configuration parameter
      
      

      
debug_print_parse (boolean)
      
       debug_print_parse configuration parameter
      
      

      
debug_print_rewritten (boolean)
      
       debug_print_rewritten configuration parameter
      
      

      
debug_print_plan (boolean)
      
       debug_print_plan configuration parameter
      
      

      

       

        These parameters enable various debugging output to be emitted.
        When set, they print the resulting raw parse tree, the parse tree, the query
        rewriter output, or the execution plan for each executed query.
        These messages are emitted at LOG message level, so by
        default they will appear in the server log but will not be sent to the
        client.  You can change that by adjusting
         and/or
        .
        These parameters are off by default.
       

      

     

     

      
debug_pretty_print (boolean)
      
       debug_pretty_print configuration parameter
      
      

      

       

        When set, debug_pretty_print indents the messages
        produced by debug_print_raw_parse,
        debug_print_parse,
        debug_print_rewritten, or
        debug_print_plan.  This results in more readable
        but much longer output than the compact format used when
        it is off.  It is on by default.
       

      

     

     

      
log_autovacuum_min_duration (integer)
      
       log_autovacuum_min_duration
       configuration parameter
      
      

      

       

        Causes vacuum action executed by autovacuum to be logged if it ran for at
        least the specified amount of time.  Setting this to zero logs
        all vacuum actions by autovacuum. -1 disables logging
        vacuum actions by autovacuum. If this value is specified without units,
        it is taken as milliseconds. For example, if you set this to
        250ms then all automatic vacuums that run
        250ms or longer will be logged.  In addition, when this parameter is
        set to any value other than -1, a message will be
        logged if a vacuum action by autovacuum is skipped due to a conflicting lock or a
        concurrently dropped relation. The default is 10min.
        Enabling this parameter can be helpful in tracking vacuum activity by autovacuum.
        This parameter can only be set in the postgresql.conf
        file or on the server command line; but the setting can be overridden for
        individual tables by changing table storage parameters.
       

      

     

     

      
log_autoanalyze_min_duration (integer)
      
       log_autoanalyze_min_duration
       configuration parameter
      
      

      

       

        Causes analyze action executed by autovacuum to be logged if it ran for at
        least the specified amount of time.  Setting this to zero logs
        all analyze actions by autovacuum. -1 disables logging
        analyze actions by autovacuum. If this value is specified without units,
        it is taken as milliseconds. For example, if you set this to
        250ms then all automatic analyzes that run
        250ms or longer will be logged.  In addition, when this parameter is
        set to any value other than -1, a message will be
        logged if an analyze action by autovacuum is skipped due to a conflicting lock or a
        concurrently dropped relation. The default is 10min.
        Enabling this parameter can be helpful in tracking analyze activity by autovacuum.
        This parameter can only be set in the postgresql.conf
        file or on the server command line; but the setting can be overridden for
        individual tables by changing table storage parameters.
       

      

     

     

      
log_checkpoints (boolean)
      
       log_checkpoints configuration parameter
      
      

      

       

        Causes checkpoints and restartpoints to be logged in the server log.
        Some statistics are included in the log messages, including the number
        of buffers written and the time spent writing them.
        This parameter can only be set in the postgresql.conf
        file or on the server command line. The default is on.
       

      

     

     

      
log_connections (string)
      
       log_connections configuration parameter
      
      

      

       

        Causes aspects of each connection to the server to be logged.
        The default is the empty string, '', which
        disables all connection logging. The following options may be
        specified alone or in a comma-separated list:
       

       
        
# Log Connection Options

        
         
         
         
          
           Name
           Description
          
         
         
          
           receipt
           Logs receipt of a connection.
          

          
           authentication
           
            Logs the original identity used by an authentication method
            to identify a user. In most cases, the identity string
            matches the PostgreSQL username,
            but some third-party authentication methods may alter the
            original user identifier before the server stores it. Failed
            authentication is always logged regardless of the value of
            this setting.
           
          

          
           authorization
           
            Logs successful completion of authorization. At this point
            the connection has been established but the backend is not
            yet fully set up. The log message includes the authorized
            username as well as the database name and application name,
            if applicable.
           
          

          
           setup_durations
           
            Logs the time spent establishing the connection and setting up the
            backend until the connection is ready to execute its first
            query. The log message includes three durations:  the total
            setup duration (starting from the postmaster accepting the
            incoming connection and ending when the connection is ready
            for query), the time it took to fork the new backend, and
            the time it took to authenticate the user.
           
          

          
           all
           
            A convenience alias equivalent to specifying all options. If
            all is specified in a list of other
            options, all connection aspects will be logged.
           
          

         
        
       

       

        Disconnection logging is separately controlled by .
       

       

        For the purposes of backwards compatibility, on,
        off, true,
        false, yes,
        no, 1, and 0
        are still supported. The positive values are equivalent to specifying
        the receipt, authentication, and
        authorization options.
       

       

        Only superusers and users with the appropriate SET
        privilege can change this parameter at session start,
        and it cannot be changed at all within a session.
       

       
        

         Some client programs, like psql, attempt
         to connect twice while determining if a password is required, so
         duplicate connection received messages do not
         necessarily indicate a problem.
        

       
      

     

     

      
log_disconnections (boolean)
      
       log_disconnections configuration parameter
      
      

      

       

        Causes session terminations to be logged.  The log output
        provides information similar to log_connections,
        plus the duration of the session.
        Only superusers and users with the appropriate SET
        privilege can change this parameter at session start,
        and it cannot be changed at all within a session.
        The default is off.
       

      

     

     

      
log_duration (boolean)
      
       log_duration configuration parameter
      
      

      

       

        Causes the duration of every completed statement to be logged.
        The default is off.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        For clients using extended query protocol, durations of the Parse,
        Bind, and Execute steps are logged independently.
       

       
        

         The difference between enabling log_duration and setting
          to zero is that
         exceeding log_min_duration_statement forces the text of
         the query to be logged, but this option doesn't.  Thus, if
         log_duration is on and
         log_min_duration_statement has a positive value, all
         durations are logged but the query text is included only for
         statements exceeding the threshold.  This behavior can be useful for
         gathering statistics in high-load installations.
        

       
      

     

     

      
log_error_verbosity (enum)
      
       log_error_verbosity configuration parameter
      
      

      

       

        Controls the amount of detail written in the server log for each
        message that is logged.  Valid values are TERSE,
        DEFAULT, and VERBOSE, each adding more
        fields to displayed messages.  TERSE excludes
        the logging of DETAIL, HINT,
        QUERY, and CONTEXT error information.
        VERBOSE output includes the SQLSTATE error
        code (see also ) and the source code file name, function name,
        and line number that generated the error.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
log_hostname (boolean)
      
       log_hostname configuration parameter
      
      

      

       

        By default, connection log messages only show the IP address of the
        connecting host. Turning this parameter on causes logging of the
        host name as well.  Note that depending on your host name resolution
        setup this might impose a non-negligible performance penalty.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
log_line_prefix (string)
      
       log_line_prefix configuration parameter
      
      

      

       

         This is a printf-style string that is output at the
         beginning of each log line.
         % characters begin escape sequences
         that are replaced with status information as outlined below.
         Unrecognized escapes are ignored. Other
         characters are copied straight to the log line. Some escapes are
         only recognized by session processes, and will be treated as empty by
         background processes such as the main server process. Status
         information may be aligned either left or right by specifying a
         numeric literal after the % and before the option. A negative
         value will cause the status information to be padded on the
         right with spaces to give it a minimum width, whereas a positive
         value will pad on the left. Padding can be useful to aid human
         readability in log files.
       

       

         This parameter can only be set in the postgresql.conf
         file or on the server command line. The default is
         '%m [%p] ' which logs a time stamp and the process ID.
       

         
          
           
            
             Escape
             Effect
             Session only
             
            
           
            
             %a
             Application name
             yes
            
            
             %u
             User name
             yes
            
            
             %d
             Database name
             yes
            
            
             %r
             Remote host name or IP address, and remote port
             yes
            
            
             %h
             Remote host name or IP address
             yes
            
            
             %L
             Local address (the IP address on the server that the
             client connected to)
             yes
            
            
             %b
             Backend type
             no
            
            
             %p
             Process ID
             no
            
            
             %P
             Process ID of the parallel group leader, if this process
              is a parallel query worker
             no
            
            
             %t
             Time stamp without milliseconds
             no
            
            
             %m
             Time stamp with milliseconds
             no
            
            
             %n
             Time stamp with milliseconds (as a Unix epoch)
             no
            
            
             %i
             Command tag: type of session's current command
             yes
            
            
             %e
             SQLSTATE error code
             no
            
            
             %c
             Session ID: see below
             no
            
            
             %l
             Number of the log line for each session or process, starting at 1
             no
            
            
             %s
             Process start time stamp
             no
            
            
             %v
             Virtual transaction ID (procNumber/localXID);  see
             
             no
            
            
             %x
             Transaction ID (0 if none is assigned);  see
             
             no
            
            
             %q
             Produces no output, but tells non-session
             processes to stop at this point in the string; ignored by
             session processes
             no
            
            
             %Q
             Query identifier of the current query.  Query
             identifiers are not computed by default, so this field
             will be zero unless 
             parameter is enabled or a third-party module that computes
             query identifiers is configured.
             yes
            
            
             %%
             Literal %
             no
            
           
          
         

         

          The backend type corresponds to the column
          backend_type in the view
          
          pg_stat_activity,
          but additional types can appear
          in the log that don't show in that view.
         

         

         The %c escape prints a quasi-unique session identifier,
         consisting of two 4-byte hexadecimal numbers (without leading zeros)
         separated by a dot.  The numbers are the process start time and the
         process ID, so %c can also be used as a space saving way
         of printing those items.  For example, to generate the session
         identifier from pg_stat_activity, use this query:

```
SELECT to_hex(trunc(EXTRACT(EPOCH FROM backend_start))::integer) || '.' ||
       to_hex(pid)
FROM pg_stat_activity;
```

       

       
        

         If you set a nonempty value for log_line_prefix,
         you should usually make its last character be a space, to provide
         visual separation from the rest of the log line.  A punctuation
         character can be used too.
        

       

       
        

         Syslog produces its own
         time stamp and process ID information, so you probably do not want to
         include those escapes if you are logging to syslog.
        

       

       
        

         The %q escape is useful when including information that is
         only available in session (backend) context like user or database
         name.  For example:

```
log_line_prefix = '%m [%p] %q%u@%d/%a '
```

        

       

       
        

         The %Q escape always reports a zero identifier
         for lines output by  because
         log_statement generates output before an
         identifier can be calculated, including invalid statements for
         which an identifier cannot be calculated.
        

       
      

     

     

      
log_lock_waits (boolean)
      
       log_lock_waits configuration parameter
      
      

      

       

        Controls whether a log message is produced when a session waits
        longer than  to acquire a
        lock.  This is useful in determining if lock waits are causing
        poor performance.  The default is on.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
log_lock_failures (boolean)
      
       log_lock_failures configuration parameter
      
      

      

       

        Controls whether a detailed log message is produced
        when a lock acquisition fails.  This is useful for analyzing
        the causes of lock failures.  Currently, only lock failures
        due to SELECT NOWAIT is supported.
        The default is off.  Only superusers and
        users with the appropriate SET privilege
        can change this setting.
       

      

     

     

      
log_recovery_conflict_waits (boolean)
      
       log_recovery_conflict_waits configuration parameter
      
      

      

       

        Controls whether a log message is produced when the startup process
        waits longer than deadlock_timeout
        for recovery conflicts.  This is useful in determining if recovery
        conflicts prevent the recovery from applying WAL.
       

       

        The default is off.  This parameter can only be set
        in the postgresql.conf file or on the server
        command line.
       

      

     

     

      
log_parameter_max_length (integer)
      
       log_parameter_max_length configuration parameter
      
      

      

       

        If greater than zero, each bind parameter value logged with a
        non-error statement-logging message is trimmed to this many bytes.
        Zero disables logging of bind parameters for non-error statement logs.
        -1 (the default) allows bind parameters to be
        logged in full.
        If this value is specified without units, it is taken as bytes.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        This setting only affects log messages printed as a result of
        ,
        , and related settings.
        Non-zero values of this setting add some overhead, particularly
        if parameters are sent in binary form, since then conversion to
        text is required.
       

      

     

     

      
log_parameter_max_length_on_error (integer)
      
       log_parameter_max_length_on_error configuration parameter
      
      

      

       

        If greater than zero, each bind parameter value reported in error
        messages is trimmed to this many bytes.
        Zero (the default) disables including bind parameters in error
        messages.
        -1 allows bind parameters to be printed in full.
        If this value is specified without units, it is taken as bytes.
       

       

        Non-zero values of this setting add overhead, as
        PostgreSQL will need to store textual
        representations of parameter values in memory at the start of each
        statement, whether or not an error eventually occurs.  The overhead
        is greater when bind parameters are sent in binary form than when
        they are sent as text, since the former case requires data
        conversion while the latter only requires copying the string.
       

      

     

     

      
log_statement (enum)
      
       log_statement configuration parameter
      
      

      

       

        Controls which SQL statements are logged. Valid values are
        none (off), ddl, mod, and
        all (all statements). ddl logs all data definition
        statements, such as CREATE, ALTER, and
        DROP statements. mod logs all
        ddl statements, plus data-modifying statements
        such as INSERT,
        UPDATE, DELETE, TRUNCATE,
        and COPY FROM.
        PREPARE, EXECUTE, and
        EXPLAIN ANALYZE statements are also logged if their
        contained command is of an appropriate type.  For clients using
        extended query protocol, logging occurs when an Execute message
        is received, and values of the Bind parameters are included
        (with any embedded single-quote marks doubled).
       

       

        The default is none.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       
        

         Statements that contain simple syntax errors are not logged
         even by the log_statement = all setting,
         because the log message is emitted only after basic parsing has
         been done to determine the statement type.  In the case of extended
         query protocol, this setting likewise does not log statements that
         fail before the Execute phase (i.e., during parse analysis or
         planning).  Set log_min_error_statement to
         ERROR (or lower) to log such statements.
        

        

         Logged statements might reveal sensitive data and even contain
         plaintext passwords.
        

       
      

     

     

      
log_statement_max_length (integer)
      
       log_statement_max_length configuration parameter
      
      

      

       

        If greater than zero, each statement logged by
        ,
        ,
        , or
        
        has its statement text truncated to at most this many bytes.
        When a statement is truncated, an ellipsis (...)
        is appended to indicate that truncation has occurred.
        A value of zero causes statements to be logged as only an ellipsis.
        -1 (the default) logs statements in full.
        If this value is specified without units, it is taken as bytes.
        This setting does not affect statements logged because of
        .
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
log_replication_commands (boolean)
      
       log_replication_commands configuration parameter
      
      

      

       

        Causes each replication command and walsender
        process's replication slot acquisition/release to be logged in the
        server log. See  for more
        information about replication command. The default value is
        off. Only superusers and users with the appropriate
        SET privilege can change this setting.
       

      

     

     

      
log_temp_files (integer)
      
       log_temp_files configuration parameter
      
      

      

       

        Controls logging of temporary file names and sizes.
        Temporary files can be
        created for sorts, hashes, and temporary query results.
        If enabled by this setting, a log entry is emitted for each
        temporary file, with the file size specified in bytes, when it is deleted.
        A value of zero logs all temporary file information, while positive
        values log only files whose size is greater than or equal to
        the specified amount of data.
        If this value is specified without units, it is taken as kilobytes.
        The default setting is -1, which disables such logging.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
log_timezone (string)
      
       log_timezone configuration parameter
      
      

      

       

        Sets the time zone used for timestamps written in the server log.
        Unlike , this value is cluster-wide,
        so that all sessions will report timestamps consistently.
        The built-in default is GMT, but that is typically
        overridden in postgresql.conf; initdb
        will install a setting there corresponding to its system environment.
        See  for more information.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

    

     

     
# Using CSV-Format Log Output

       

        Including csvlog in the log_destination list
        provides a convenient way to import log files into a database table.
        This option emits log lines in comma-separated-values
        (CSV) format,
        with these columns:
        time stamp with milliseconds,
        user name,
        database name,
        process ID,
        client host:port number,
        session ID,
        per-session line number,
        command tag,
        session start time,
        virtual transaction ID,
        regular transaction ID,
        error severity,
        SQLSTATE code,
        error message,
        error message detail,
        hint,
        internal query that led to the error (if any),
        character count of the error position therein,
        error context,
        user query that led to the error (if any and enabled by
        log_min_error_statement),
        character count of the error position therein,
        location of the error in the PostgreSQL source code
        (if log_error_verbosity is set to verbose),
        application name, backend type, process ID of parallel group leader,
        and query id.
        Here is a sample table definition for storing CSV-format log output:

```
CREATE TABLE postgres_log
(
  log_time timestamp(3) with time zone,
  user_name text,
  database_name text,
  process_id integer,
  connection_from text,
  session_id text,
  session_line_num bigint,
  command_tag text,
  session_start_time timestamp with time zone,
  virtual_transaction_id text,
  transaction_id bigint,
  error_severity text,
  sql_state_code text,
  message text,
  detail text,
  hint text,
  internal_query text,
  internal_query_pos integer,
  context text,
  query text,
  query_pos integer,
  location text,
  application_name text,
  backend_type text,
  leader_pid integer,
  query_id bigint,
  PRIMARY KEY (session_id, session_line_num)
);
```

       

       

        To import a log file into this table, use the COPY FROM
        command:

```
COPY postgres_log FROM '/full/path/to/logfile.csv' WITH csv;
```

        It is also possible to access the file as a foreign table, using
        the supplied  module.
       

       

       There are a few things you need to do to simplify importing CSV log
       files:

       
         

           

            Set log_filename and
            log_rotation_age to provide a consistent,
            predictable naming scheme for your log files.  This lets you
            predict what the file name will be and know when an individual log
            file is complete and therefore ready to be imported.
         

        

        

           

            Set log_rotation_size to 0 to disable
            size-based log rotation, as it makes the log file name difficult
            to predict.
           

        

        

          

           Set log_truncate_on_rotation to on so
           that old log data isn't mixed with the new in the same file.
          

        

        

          

           The table definition above includes a primary key specification.
           This is useful to protect against accidentally importing the same
           information twice.  The COPY command commits all of the
           data it imports at one time, so any error will cause the entire
           import to fail.  If you import a partial log file and later import
           the file again when it is complete, the primary key violation will
           cause the import to fail.  Wait until the log is complete and
           closed before importing.  This procedure will also protect against
           accidentally importing a partial line that hasn't been completely
           written, which would also cause COPY to fail.
          

        

        
      

    

     

     
# Using JSON-Format Log Output

     

      Including jsonlog in the
      log_destination list provides a convenient way to
      import log files into many different programs. This option emits log
      lines in JSON format.
     

     

      String fields with null values are excluded from output.
      Additional fields may be added in the future. User applications that
      process jsonlog output should ignore unknown fields.
     

     

      Each log line is serialized as a JSON object with the set of keys and
      their associated values shown in .
     

     
      
# Keys and Values of JSON Log Entries

      
       
        
         Key name
         Type
         Description
        
       
       
        
         timestamp
         string
         Time stamp with milliseconds
        
        
         user
         string
         User name
        
        
         dbname
         string
         Database name
        
        
         pid
         number
         Process ID
        
        
         remote_host
         string
         Client host
        
        
         remote_port
         number
         Client port
        
        
         session_id
         string
         Session ID
        
        
         line_num
         number
         Per-session line number
        
        
         ps
         string
         Current ps display
        
        
         session_start
         string
         Session start time
        
        
         vxid
         string
         Virtual transaction ID
        
        
         txid
         string
         Regular transaction ID
        
        
         error_severity
         string
         Error severity
        
        
         state_code
         string
         SQLSTATE code
        
        
         message
         string
         Error message
        
        
         detail
         string
         Error message detail
        
        
         hint
         string
         Error message hint
        
        
         internal_query
         string
         Internal query that led to the error
        
        
         internal_position
         number
         Cursor index into internal query
        
        
         context
         string
         Error context
        
        
         statement
         string
         Client-supplied query string
        
        
         cursor_position
         number
         Cursor index into query string
        
        
         func_name
         string
         Error location function name
        
        
         file_name
         string
         File name of error location
        
        
         file_line_num
         number
         File line number of the error location
        
        
         application_name
         string
         Client application name
        
        
         backend_type
         string
         Type of backend
        
        
         leader_pid
         number
         Process ID of leader for active parallel workers
        
        
         query_id
         number
         Query ID
        
       
      
     
    

   

    
# Process Title

    

     These settings control how process titles of server processes are
     modified.  Process titles are typically viewed using programs like
     ps or, on Windows, Process Explorer.
     See  for details.
    

    

     

      
cluster_name (string)
      
       cluster_name configuration parameter
      
      

      

       

        Sets a name that identifies this database cluster (instance) for
        various purposes.  The cluster name appears in the process title for
        all server processes in this cluster.  Moreover, it is the default
        application name for a standby connection (see ).
       

       

        The name can be any string of less
        than NAMEDATALEN characters (64 characters in a standard
        build). Only printable ASCII characters may be used in the
        cluster_name value.
        Other characters are replaced with C-style hexadecimal escapes.
        No name is shown if this parameter is set to the empty string
        '' (which is the default).
        This parameter can only be set at server start.
       

      

     

     

      
update_process_title (boolean)
      
       update_process_title configuration parameter
      
      

      

       

        Enables updating of the process title every time a new SQL command
        is received by the server.
        This setting defaults to on on most platforms, but it
        defaults to off on Windows due to that platform's larger
        overhead for updating the process title.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

    

   

  

   

    
# Run-time Statistics

    

     
# Cumulative Query and Index Statistics

     

      These parameters control the server-wide cumulative statistics system.
      When enabled, the data that is collected can be accessed via the
      pg_stat and pg_statio
      family of system views.  Refer to  for more
      information.
     

     

     

      
track_activities (boolean)
      
       track_activities configuration parameter
      
      

      

       

        Enables the collection of information on the currently
        executing command of each session, along with its identifier and the
        time when that command began execution. This parameter is on by
        default. Note that even when enabled, this information is only
        visible to superusers, roles with privileges of the
        pg_read_all_stats role and the user owning the
        sessions being reported on (including sessions belonging to a role they
        have the privileges of), so it should not represent a security risk.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
track_activity_query_size (integer)
      
       track_activity_query_size configuration parameter
      
      

      

       

       Specifies the amount of memory reserved to store the text of the
       currently executing command for each active session, for the
       pg_stat_activity.query field.
       If this value is specified without units, it is taken as bytes.
       The default value is 1024 bytes.
       This parameter can only be set at server start.
       

      

     

     

      
track_counts (boolean)
      
       track_counts configuration parameter
      
      

      

       

        Enables collection of statistics on database activity.
        This parameter is on by default, because the autovacuum
        daemon needs the collected information.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
track_cost_delay_timing (boolean)
      
       track_cost_delay_timing configuration parameter
      
      

      

       

        Enables timing of cost-based vacuum delay (see
        ).  This parameter
        is off by default, as it will repeatedly query the operating system for
        the current time, which may cause significant overhead on some
        platforms.  You can use the  tool to
        measure the overhead of timing on your system.  Cost-based vacuum delay
        timing information is displayed in
        pg_stat_progress_vacuum,
        pg_stat_progress_analyze,
        in the output of  and
         when the
        VERBOSE option is used, and by autovacuum for
        auto-vacuums and auto-analyzes when
         is set.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
track_io_timing (boolean)
      
       track_io_timing configuration parameter
      
      

      

       

        Enables timing of database I/O waits.  This parameter is off by
        default, as it will repeatedly query the operating system for
        the current time, which may cause significant overhead on some
        platforms.  You can use the  tool to
        measure the overhead of timing on your system.
        I/O timing information is
        displayed in 
        pg_stat_database,
        
        pg_stat_io (if object
        is not wal), in the output of the
        
        pg_stat_get_backend_io() function (if
        object is not wal), in the
        output of  when the BUFFERS
        option is used, in the output of  when
        the VERBOSE option is used, by autovacuum
        for auto-vacuums and auto-analyzes, when  is set and by
        .
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
track_wal_io_timing (boolean)
      
       track_wal_io_timing configuration parameter
      
      

      

       

        Enables timing of WAL I/O waits. This parameter is off by default,
        as it will repeatedly query the operating system for the current time,
        which may cause significant overhead on some platforms.
        You can use the pg_test_timing tool to
        measure the overhead of timing on your system.
        I/O timing information is displayed in
        
        pg_stat_io for the
        object wal and in the output of
        the 
        pg_stat_get_backend_io() function for the
        object wal.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
track_functions (enum)
      
       track_functions configuration parameter
      
      

      

       

        Enables tracking of function call counts and time used. Specify
        pl to track only procedural-language functions,
        all to also track SQL and C language functions.
        The default is none, which disables function
        statistics tracking.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       
        

         SQL-language functions that are simple enough to be inlined
         into the calling query will not be tracked, regardless of this
         setting.
        

       
      

     

     

      
stats_fetch_consistency (enum)
      
       stats_fetch_consistency configuration parameter
      
      

      

       

        Determines the behavior when cumulative statistics are accessed
        multiple times within a transaction. When set to
        none, each access re-fetches counters from shared
        memory. When set to cache, the first access to
        statistics for an object caches those statistics until the end of the
        transaction unless pg_stat_clear_snapshot() is
        called. When set to snapshot, the first statistics
        access caches all statistics accessible in the current database, until
        the end of the transaction unless
        pg_stat_clear_snapshot() is called. Changing this
        parameter in a transaction discards the statistics snapshot.
        The default is cache.
       

       
        

         none is most suitable for monitoring systems. If
         values are only accessed once, it is the most
         efficient. cache ensures repeat accesses yield the
         same values, which is important for queries involving
         e.g. self-joins. snapshot can be useful when
         interactively inspecting statistics, but has higher overhead,
         particularly if many database objects exist.
        

       
      

     

     

    

    

     
# Statistics Monitoring

     

     

      
compute_query_id (enum)
      
       compute_query_id configuration parameter
      
      

      

       

        Enables in-core computation of a query identifier.
        Query identifiers can be displayed in the pg_stat_activity
        view, using EXPLAIN, or emitted in the log if
        configured via the  parameter.
        The  extension also requires a query
        identifier to be computed.  Note that an external module can
        alternatively be used if the in-core query identifier computation
        method is not acceptable.  In this case, in-core computation
        must be always disabled.
        Valid values are off (always disabled),
        on (always enabled), auto,
        which lets modules such as 
        automatically enable it, and regress which
        has the same effect as auto, except that the
        query identifier is not shown in the EXPLAIN output
        in order to facilitate automated regression testing.
        The default is auto.
       

       
        

         To ensure that only one query identifier is calculated and
         displayed, extensions that calculate query identifiers should
         throw an error if a query identifier has already been computed.
        

       
      

     

     

      
log_statement_stats (boolean)
      
       log_statement_stats configuration parameter
      
      

      
log_parser_stats (boolean)
      
       log_parser_stats configuration parameter
      
      

      
log_planner_stats (boolean)
      
       log_planner_stats configuration parameter
      
      

      
log_executor_stats (boolean)
      
       log_executor_stats configuration parameter
      
      

      

       

        For each query, output performance statistics of the respective
        module to the server log. This is a crude profiling
        instrument, similar to the Unix getrusage() operating
        system facility.  log_statement_stats reports total
        statement statistics, while the others report per-module statistics.
        log_statement_stats cannot be enabled together with
        any of the per-module options.  All of these options are disabled by
        default.
        Only superusers and users with the appropriate SET
        privilege can change these settings.
       

      

     

     

    

   

   

    
# Vacuuming

    
     vacuum
     configuration parameters
    

    

     These parameters control vacuuming behavior. For more information on the
     purpose and responsibilities of vacuum, see .
    

    

     
# Automatic Vacuuming

     

      These settings control the behavior of the autovacuum
      feature.  Refer to  for more information.
      Note that many of these settings can be overridden on a per-table
      basis; see .
     

     

      

       
autovacuum (boolean)
       
        autovacuum configuration parameter
       
       

       

        

         Controls whether the server should run the
         autovacuum launcher daemon.  This is on by default; however,
          must also be enabled for
         autovacuum to work.
         This parameter can only be set in the postgresql.conf
         file or on the server command line; however, autovacuuming can be
         disabled for individual tables by changing table storage parameters.
        

        

         Note that even when this parameter is disabled, the system
         will launch autovacuum processes if necessary to
         prevent transaction ID wraparound.  See  for more information.
        

       

      

      

       
autovacuum_worker_slots (integer)
       
        autovacuum_worker_slots configuration parameter
       
       

       

        

         Specifies the number of backend slots to reserve for autovacuum worker
         processes.  The default is typically 16 slots, but might be less if
         your kernel settings will not support it (as determined during initdb).
         This parameter can only be set at server start.
        

        

         When changing this value, consider also adjusting
         .
        

       

      

      

       
autovacuum_max_workers (integer)
       
        autovacuum_max_workers configuration parameter
       
       

       

        

         Specifies the maximum number of autovacuum processes (other than the
         autovacuum launcher) that may be running at any one time.  The default
         is 3.  This parameter can only be set in the
         postgresql.conf file or on the server command line.
        

        

         Note that a setting for this value which is higher than
          will have no effect,
         since autovacuum workers are taken from the pool of slots established
         by that setting.
        

       

      

      

       
autovacuum_naptime (integer)
       
        autovacuum_naptime configuration parameter
       
       

       

        

         Specifies the minimum delay between autovacuum runs on any given
         database.  In each round the daemon examines the
         database and issues VACUUM and ANALYZE commands
         as needed for tables in that database.
         If this value is specified without units, it is taken as seconds.
         The default is one minute (1min).
         This parameter can only be set in the postgresql.conf
         file or on the server command line.
        

       

      

      

       
autovacuum_vacuum_threshold (integer)
       
        autovacuum_vacuum_threshold
        configuration parameter
       
       

       

        

         Specifies the minimum number of updated or deleted tuples needed
         to trigger a VACUUM in any one table.
         The default is 50 tuples.
         This parameter can only be set in the postgresql.conf
         file or on the server command line;
         but the setting can be overridden for individual tables by
         changing table storage parameters.
        

       

      

      

       
autovacuum_vacuum_insert_threshold (integer)
       
        autovacuum_vacuum_insert_threshold
        configuration parameter
       
       

       

        

         Specifies the number of inserted tuples needed to trigger a
         VACUUM in any one table.
         The default is 1000 tuples.  If -1 is specified, autovacuum will not
         trigger a VACUUM operation on any tables based on
         the number of inserts.
         This parameter can only be set in the postgresql.conf
         file or on the server command line;
         but the setting can be overridden for individual tables by
         changing table storage parameters.
        

       

      

      

       
autovacuum_analyze_threshold (integer)
       
        autovacuum_analyze_threshold
        configuration parameter
       
       

       

        

         Specifies the minimum number of inserted, updated or deleted tuples
         needed to trigger an ANALYZE in any one table.
         The default is 50 tuples.
         This parameter can only be set in the postgresql.conf
         file or on the server command line;
         but the setting can be overridden for individual tables by
         changing table storage parameters.
        

       

      

      

       
autovacuum_vacuum_scale_factor (floating point)
       
        autovacuum_vacuum_scale_factor
        configuration parameter
       
       

       

        

         Specifies a fraction of the table size to add to
         autovacuum_vacuum_threshold
         when deciding whether to trigger a VACUUM.
         The default is 0.2 (20% of table size).
         This parameter can only be set in the postgresql.conf
         file or on the server command line;
         but the setting can be overridden for individual tables by
         changing table storage parameters.
        

       

      

      

       
autovacuum_vacuum_insert_scale_factor (floating point)
       
        autovacuum_vacuum_insert_scale_factor
        configuration parameter
       
       

       

        

         Specifies a fraction of the unfrozen pages in the table to add to
         autovacuum_vacuum_insert_threshold when deciding
         whether to trigger a VACUUM. The default is
         0.2 (20% of unfrozen pages in table). This
         parameter can only be set in the postgresql.conf
         file or on the server command line; but the setting can be overridden
         for individual tables by changing table storage parameters.
        

       

      

      

       
autovacuum_analyze_scale_factor (floating point)
       
        autovacuum_analyze_scale_factor
        configuration parameter
       
       

       

        

         Specifies a fraction of the table size to add to
         autovacuum_analyze_threshold
         when deciding whether to trigger an ANALYZE.
         The default is 0.1 (10% of table size).
         This parameter can only be set in the postgresql.conf
         file or on the server command line;
         but the setting can be overridden for individual tables by
         changing table storage parameters.
        

       

      

      

       
autovacuum_vacuum_max_threshold (integer)
       
        autovacuum_vacuum_max_threshold
        configuration parameter
       
       

       

        

         Specifies the maximum number of updated or deleted tuples needed to
         trigger a VACUUM in any one table, i.e., a limit on
         the value calculated with
         autovacuum_vacuum_threshold and
         autovacuum_vacuum_scale_factor.  The default is
         100,000,000 tuples.  If -1 is specified, autovacuum will not enforce a
         maximum number of updated or deleted tuples that will trigger a
         VACUUM operation.  This parameter can only be set
         in the postgresql.conf file or on the server
         command line; but the setting can be overridden for individual tables
         by changing storage parameters.
        

       

      

      

       
autovacuum_freeze_max_age (integer)
       
        autovacuum_freeze_max_age
        configuration parameter
       
       

       

        

         Specifies the maximum age (in transactions) that a table's
         pg_class.relfrozenxid field can
         attain before a VACUUM operation is forced
         to prevent transaction ID wraparound within the table.
         Note that the system will launch autovacuum processes to
         prevent wraparound even when autovacuum is otherwise disabled.
        

        

         Vacuum also allows removal of old files from the
         pg_xact subdirectory, which is why the default
         is a relatively low 200 million transactions.
         This parameter can only be set at server start, but the setting
         can be reduced for individual tables by
         changing table storage parameters.
         For more information see .
        

       

      

      

       
autovacuum_multixact_freeze_max_age (integer)
       
        autovacuum_multixact_freeze_max_age
        configuration parameter
       
       

       

        

         Specifies the maximum age (in multixacts) that a table's
         pg_class.relminmxid field can
         attain before a VACUUM operation is forced to
         prevent multixact ID wraparound within the table.
         Note that the system will launch autovacuum processes to
         prevent wraparound even when autovacuum is otherwise disabled.
        

        

         Vacuuming multixacts also allows removal of old files from the
         pg_multixact/members and pg_multixact/offsets
         subdirectories, which is why the default is a relatively low
         400 million multixacts.
         This parameter can only be set at server start, but the setting can
         be reduced for individual tables by changing table storage parameters.
         For more information see .
        

       

      

      

       
autovacuum_vacuum_cost_delay (floating point)
       
        autovacuum_vacuum_cost_delay
        configuration parameter
       
       

       

        

         Specifies the cost delay value that will be used in automatic
         VACUUM operations.  If -1 is specified, the regular
          value will be used.
         If this value is specified without units, it is taken as milliseconds.
         The default value is 2 milliseconds.
         This parameter can only be set in the postgresql.conf
         file or on the server command line;
         but the setting can be overridden for individual tables by
         changing table storage parameters.
        

       

      

      

       
autovacuum_vacuum_cost_limit (integer)
       
        autovacuum_vacuum_cost_limit
        configuration parameter
       
       

       

        

         Specifies the cost limit value that will be used in automatic
         VACUUM operations.  If -1
         is specified (which is the default), the regular
          value will be used.  Note that
         the value is distributed proportionally among the running autovacuum
         workers, if there is more than one, so that the sum of the limits for
         each worker does not exceed the value of this variable.
         This parameter can only be set in the postgresql.conf
         file or on the server command line;
         but the setting can be overridden for individual tables by
         changing table storage parameters.
        

       

      

      

       
autovacuum_freeze_score_weight (floating point)
       
        autovacuum_freeze_score_weight
        configuration parameter
       
       

       

        

         Specifies the scaling factor of the transaction ID age component of
         the score used by autovacuum for prioritization purposes.  The default
         is 1.0.  This parameter can only be set in the
         postgresql.conf file or on the server command
         line.  See  for more information.
        

       

      

      

       
autovacuum_multixact_freeze_score_weight (floating point)
       
        autovacuum_multixact_freeze_score_weight
        configuration parameter
       
       

       

        

         Specifies the scaling factor of the multixact ID age component of the
         score used by autovacuum for prioritization purposes.  The default is
         1.0.  This parameter can only be set in the
         postgresql.conf file or on the server command
         line.  See  for more information.
        

       

      

      

       
autovacuum_vacuum_score_weight (floating point)
       
        autovacuum_vacuum_score_weight
        configuration parameter
       
       

       

        

         Specifies the scaling factor of the vacuum threshold component of the
         score used by autovacuum for prioritization purposes.  The default is
         1.0.  This parameter can only be set in the
         postgresql.conf file or on the server command
         line.  See  for more information.
        

       

      

      

       
autovacuum_vacuum_insert_score_weight (floating point)
       
        autovacuum_vacuum_insert_score_weight
        configuration parameter
       
       

       

        

         Specifies the scaling factor of the vacuum insert threshold component
         of the score used by autovacuum for prioritization purposes.  The
         default is 1.0.  This parameter can only be set in
         the postgresql.conf file or on the server command
         line.  See  for more information.
        

       

      

      

       
autovacuum_analyze_score_weight (floating point)
       
        autovacuum_analyze_score_weight
        configuration parameter
       
       

       

        

         Specifies the scaling factor of the analyze threshold component of the
         score used by autovacuum for prioritization purposes.  The default is
         1.0.  This parameter can only be set in the
         postgresql.conf file or on the server command
         line.  See  for more information.
        

       

      

      

       
autovacuum_max_parallel_workers (integer)
       
        autovacuum_max_parallel_workers
        configuration parameter
       
       

       

        

         Sets the maximum number of parallel workers that can be used by a
         single autovacuum worker to process indexes. This limit applies
         specifically to the index vacuuming and index cleanup phases (for the
         details of each autovacuum phase, please refer to ).
         The actual number of parallel workers is further limited by
         . This is the
         per-autovacuum worker equivalent of the PARALLEL
         option of the VACUUM
         command. Setting this value to 0 disables parallel vacuum during autovacuum.
         The default is 0.
        

       

     

     

    

    

     
# Cost-based Vacuum Delay

     

      During the execution of 
      and 
      commands, the system maintains an
      internal counter that keeps track of the estimated cost of the
      various I/O operations that are performed.  When the accumulated
      cost reaches a limit (specified by
      vacuum_cost_limit), the process performing
      the operation will sleep for a short period of time, as specified by
      vacuum_cost_delay. Then it will reset the
      counter and continue execution.
     

     

      The intent of this feature is to allow administrators to reduce
      the I/O impact of these commands on concurrent database
      activity. There are many situations where it is not
      important that maintenance commands like
      VACUUM and ANALYZE finish
      quickly; however, it is usually very important that these
      commands do not significantly interfere with the ability of the
      system to perform other database operations. Cost-based vacuum
      delay provides a way for administrators to achieve this.
     

     

      This feature is disabled by default for manually issued
      VACUUM commands. To enable it, set the
      vacuum_cost_delay variable to a nonzero
      value.
     

     

      

       
vacuum_cost_delay (floating point)
       
        vacuum_cost_delay configuration parameter
       
       

       

        

         The amount of time that the process will sleep when the cost
         limit has been exceeded. If this value is specified without
         units, it is taken as milliseconds. The default value is
         0, which disables the cost-based vacuum delay
         feature.  Positive values enable cost-based vacuuming.
        

        

         When using cost-based vacuuming, appropriate values for
         vacuum_cost_delay are usually quite small, perhaps
         less than 1 millisecond.  While vacuum_cost_delay
         can be set to fractional-millisecond values, such delays may not be
         measured accurately on older platforms.  On such platforms,
         increasing VACUUM's throttled resource consumption
         above what you get at 1ms will require changing the other vacuum cost
         parameters.  You should, nonetheless,
         keep vacuum_cost_delay as small as your platform
         will consistently measure; large delays are not helpful.
        

       

      

      

       
vacuum_cost_page_hit (integer)
       
        vacuum_cost_page_hit configuration parameter
       
       

       

        

         The estimated cost for vacuuming a buffer found in the shared
         buffer cache. It represents the cost to lock the buffer pool,
         lookup the shared hash table and scan the content of the page.
         The default value is 1.
        

       

      

      

       
vacuum_cost_page_miss (integer)
       
        vacuum_cost_page_miss configuration parameter
       
       

       

        

         The estimated cost for vacuuming a buffer that has to be read from
         disk.  This represents the effort to lock the buffer pool,
         lookup the shared hash table, read the desired block in from
         the disk and scan its content. The default value is
         2.
        

       

      

      

       
vacuum_cost_page_dirty (integer)
       
        vacuum_cost_page_dirty configuration parameter
       
       

       

        

         The estimated cost charged when vacuum modifies a block that was
         previously clean. It represents the extra I/O required to
         flush the dirty block out to disk again. The default value is
         20.
        

       

      

      

       
vacuum_cost_limit (integer)
       
        vacuum_cost_limit configuration parameter
       
       

       

        

         This is the accumulated cost that will cause the vacuuming
         process to sleep for vacuum_cost_delay.  The
         default is 200.
        

       

      

     

     
      

       There are certain operations that hold critical locks and should
       therefore complete as quickly as possible.  Cost-based vacuum
       delays do not occur during such operations.  Therefore it is
       possible that the cost accumulates far higher than the specified
       limit.  To avoid uselessly long delays in such cases, the actual
       delay is calculated as vacuum_cost_delay *
       accumulated_balance /
       vacuum_cost_limit with a maximum of
       vacuum_cost_delay * 4.
      

     
    

    

     
# Default Behavior

     

      

       
vacuum_truncate (boolean)
       
        vacuum_truncate
        configuration parameter
       
       

       

        

         Enables or disables vacuum to try to truncate off any empty pages at
         the end of the table.  The default value is true.
         If true, VACUUM and autovacuum
         do the truncation and the disk space for the truncated pages is
         returned to the operating system.  Note that the truncation requires
         an ACCESS EXCLUSIVE lock on the table.  The
         TRUNCATE parameter of
         VACUUM, if
         specified, overrides the value of this parameter.  The setting can
         also be overridden for individual tables by changing table storage
         parameters.
        

       

      

     

    

    

     
# Freezing

     

      To maintain correctness even after transaction IDs wrap around,
      PostgreSQL marks rows that are sufficiently
      old as frozen. These rows are visible to everyone;
      other transactions do not need to examine their inserting XID to
      determine visibility. VACUUM is responsible for
      marking rows as frozen. The following settings control
      VACUUM's freezing behavior and should be tuned based
      on the XID consumption rate of the system and data access patterns of the
      dominant workloads. See  for more
      information on transaction ID wraparound and tuning these parameters.
     

     

      

       
vacuum_freeze_table_age (integer)
       
        vacuum_freeze_table_age configuration parameter
       
       

       

        

         VACUUM performs an aggressive scan if the table's
         pg_class.relfrozenxid field has reached
         the age specified by this setting.  An aggressive scan differs from
         a regular VACUUM in that it visits every page that might
         contain unfrozen XIDs or MXIDs, not just those that might contain dead
         tuples.  The default is 150 million transactions.  Although users can
         set this value anywhere from zero to two billion, VACUUM
         will silently limit the effective value to 95% of
         , so that a
         periodic manual VACUUM has a chance to run before an
         anti-wraparound autovacuum is launched for the table. For more
         information see
         .
        

       

      

      

       
vacuum_freeze_min_age (integer)
       
        vacuum_freeze_min_age configuration parameter
       
       

       

        

         Specifies the cutoff age (in transactions) that
         VACUUM should use to decide whether to
         trigger freezing of pages that have an older XID.
         The default is 50 million transactions.  Although
         users can set this value anywhere from zero to one billion,
         VACUUM will silently limit the effective value to half
         the value of , so
         that there is not an unreasonably short time between forced
         autovacuums.  For more information see .
        

       

      

      

       
vacuum_failsafe_age (integer)
       
        vacuum_failsafe_age configuration parameter
       
       

       

        

         Specifies the maximum age (in transactions) that a table's
         pg_class.relfrozenxid
         field can attain before VACUUM takes
         extraordinary measures to avoid system-wide transaction ID
         wraparound failure.  This is VACUUM's
         strategy of last resort.  The failsafe typically triggers
         when an autovacuum to prevent transaction ID wraparound has
         already been running for some time, though it's possible for
         the failsafe to trigger during any VACUUM.
        

        

         When the failsafe is triggered, any cost-based delay that is
         in effect will no longer be applied, further non-essential
         maintenance tasks (such as index vacuuming) are bypassed, and any
         Buffer Access Strategy
         in use will be disabled resulting in VACUUM being
         free to make use of all of
         shared buffers.
        

        

         The default is 1.6 billion transactions.  Although users can
         set this value anywhere from zero to 2.1 billion,
         VACUUM will silently adjust the effective
         value to no less than 105% of .
        

       

      

      

       
vacuum_multixact_freeze_table_age (integer)
       
        vacuum_multixact_freeze_table_age configuration parameter
       
       

       

        

         VACUUM performs an aggressive scan if the table's
         pg_class.relminmxid field has reached
         the age specified by this setting.  An aggressive scan differs from
         a regular VACUUM in that it visits every page that might
         contain unfrozen XIDs or MXIDs, not just those that might contain dead
         tuples.  The default is 150 million multixacts.
         Although users can set this value anywhere from zero to two billion,
         VACUUM will silently limit the effective value to 95% of
         , so that a
         periodic manual VACUUM has a chance to run before an
         anti-wraparound is launched for the table.
         For more information see .
        

       

      

      

       
vacuum_multixact_freeze_min_age (integer)
       
        vacuum_multixact_freeze_min_age configuration parameter
       
       

       

        

         Specifies the cutoff age (in multixacts) that VACUUM
         should use to decide whether to trigger freezing of pages with
         an older multixact ID.  The default is 5 million multixacts.
         Although users can set this value anywhere from zero to one billion,
         VACUUM will silently limit the effective value to half
         the value of ,
         so that there is not an unreasonably short time between forced
         autovacuums.
         For more information see .
        

       

      

      

       
vacuum_multixact_failsafe_age (integer)
       
        vacuum_multixact_failsafe_age configuration parameter
       
       

       

        

         Specifies the maximum age (in multixacts) that a table's
         pg_class.relminmxid
         field can attain before VACUUM takes
         extraordinary measures to avoid system-wide multixact ID
         wraparound failure.  This is VACUUM's
         strategy of last resort.  The failsafe typically triggers when
         an autovacuum to prevent transaction ID wraparound has already
         been running for some time, though it's possible for the
         failsafe to trigger during any VACUUM.
        

        

         When the failsafe is triggered, any cost-based delay that is
         in effect will no longer be applied, and further non-essential
         maintenance tasks (such as index vacuuming) are bypassed.
        

        

         The default is 1.6 billion multixacts.  Although users can set
         this value anywhere from zero to 2.1 billion,
         VACUUM will silently adjust the effective
         value to no less than 105% of .
        

       

      

     

      
vacuum_max_eager_freeze_failure_rate (floating point)
      
       vacuum_max_eager_freeze_failure_rate
       configuration parameter
      
      

      

       

        Specifies the maximum number of pages (as a fraction of total pages in
        the relation) that VACUUM may scan and
        fail to set all-frozen in the visibility map
        before disabling eager scanning. A value of 0
        disables eager scanning altogether. The default is
        0.03 (3%).
       

       

        Note that when eager scanning is enabled, only freeze failures
        count against the cap, not successful freezing. Successful page
        freezes are capped internally at 20% of the all-visible but not
        all-frozen pages in the relation. Capping successful page freezes helps
        amortize the overhead across multiple normal vacuums and limits the
        potential downside of wasted eager freezes of pages that are modified
        again before the next aggressive vacuum.
       

       

        This parameter can only be set in the
        postgresql.conf file or on the server command
        line; but the setting can be overridden for individual tables by
        changing the
        
        corresponding table storage parameter.
        For more information on tuning vacuum's freezing behavior,
        see .
       

      

     

     

    

   

   

    
# Client Connection Defaults

    

     
# Statement Behavior

     

     

      
client_min_messages (enum)
      
       client_min_messages configuration parameter
      
      

      

       

        Controls which
        message levels
        are sent to the client.
        Valid values are DEBUG5,
        DEBUG4, DEBUG3, DEBUG2,
        DEBUG1, LOG, NOTICE,
        WARNING, and ERROR.
        Each level includes all the levels that follow it.  The later the level,
        the fewer messages are sent.  The default is
        NOTICE.  Note that LOG has a different
        rank here than in .
       

       

        INFO level messages are always sent to the client.
       

      

     

     

      
search_path (string)
      
       search_path configuration parameter
      
      pathfor schemas
      

      

       

        This variable specifies the order in which schemas are searched
        when an object (table, data type, function, etc.) is referenced by a
        simple name with no schema specified.  When there are objects of
        identical names in different schemas, the one found first
        in the search path is used.  An object that is not in any of the
        schemas in the search path can only be referenced by specifying
        its containing schema with a qualified (dotted) name.
       

       

        The value for search_path must be a comma-separated
        list of schema names.  Any name that is not an existing schema, or is
        a schema for which the user does not have USAGE
        permission, is silently ignored.
       

       

        If one of the list items is the special name
        $user, then the schema having the name returned by
        CURRENT_USER is substituted, if there is such a schema
        and the user has USAGE permission for it.
        (If not, $user is ignored.)
       

       

        The system catalog schema, pg_catalog, is always
        searched, whether it is mentioned in the path or not.  If it is
        mentioned in the path then it will be searched in the specified
        order.  If pg_catalog is not in the path then it will
        be searched before searching any of the path items.
       

       
       

        Likewise, the current session's temporary-table schema,
        pg_temp_nnn, is always searched if it
        exists.  It can be explicitly listed in the path by using the
        alias pg_temppg_temp.  If it is not listed in the path then
        it is searched first (even before pg_catalog).  However,
        the temporary schema is only searched for relation (table, view,
        sequence, etc.) and data type names.  It is never searched for
        function or operator names.
       

       

        When objects are created without specifying a particular target
        schema, they will be placed in the first valid schema named in
        search_path.  An error is reported if the search
        path is empty.
       

       

        The default value for this parameter is
        "$user", public.
        This setting supports shared use of a database (where no users
        have private schemas, and all share use of public),
        private per-user schemas, and combinations of these.  Other
        effects can be obtained by altering the default search path
        setting, either globally or per-user.
       

       

        For more information on schema handling, see
        .  In particular, the default
        configuration is suitable only when the database has a single user or
        a few mutually-trusting users.
       

       

        The current effective value of the search path can be examined
        via the SQL function
        current_schemas
        (see ).
        This is not quite the same as
        examining the value of search_path, since
        current_schemas shows how the items
        appearing in search_path were resolved.
       

      

     

     

      
row_security (boolean)
      
       row_security configuration parameter
      
      

      

       

        This variable controls whether to raise an error in lieu of applying a
        row security policy.  When set to on, policies apply
        normally.  When set to off, queries fail which would
        otherwise apply at least one policy.  The default is on.
        Change to off where limited row visibility could cause
        incorrect results; for example, pg_dump makes that
        change by default.  This variable has no effect on roles which bypass
        every row security policy, to wit, superusers and roles with
        the BYPASSRLS attribute.
       

       

        For more information on row security policies,
        see .
       

      

     

     

      
default_table_access_method (string)
      
       default_table_access_method configuration parameter
      
      

      

       

        This parameter specifies the default table access method to use when
        creating tables or materialized views if the CREATE
        command does not explicitly specify an access method, or when
        SELECT ... INTO is used, which does not allow
        specifying a table access method. The default is heap.
       

      

     

     

      
default_tablespace (string)
      
       default_tablespace configuration parameter
      
      tablespacedefault
      

      

       

        This variable specifies the default tablespace in which to create
        objects (tables and indexes) when a CREATE command does
        not explicitly specify a tablespace.
       

       

        The value is either the name of a tablespace, or an empty string
        to specify using the default tablespace of the current database.
        If the value does not match the name of any existing tablespace,
        PostgreSQL will automatically use the default
        tablespace of the current database.  If a nondefault tablespace
        is specified, the user must have CREATE privilege
        for it, or creation attempts will fail.
       

       

        This variable is not used for temporary tables; for them,
         is consulted instead.
       

       

        This variable is also not used when creating databases.
        By default, a new database inherits its tablespace setting from
        the template database it is copied from.
       

       

        If this parameter is set to a value other than the empty string
        when a partitioned table is created, the partitioned table's
        tablespace will be set to that value, which will be used as
        the default tablespace for partitions created in the future,
        even if default_tablespace has changed since then.
       

       

        For more information on tablespaces,
        see .
       

      

     

     

      
default_toast_compression (enum)
      
       default_toast_compression configuration parameter
      
      

      

       

        This variable sets the default
        TOAST
        compression method for values of compressible columns.
        (This can be overridden for individual columns by setting
        the COMPRESSION column option in
        CREATE TABLE or
        ALTER TABLE.)
        The supported compression methods are pglz and
        (if PostgreSQL was compiled with
        --with-lz4) lz4.
        The default is lz4 (if available); otherwise,
        pglz.
       

      

     

     

      
temp_tablespaces (string)
      
       temp_tablespaces configuration parameter
      
      tablespacetemporary
      

      

       

        This variable specifies tablespaces in which to create temporary
        objects (temp tables and indexes on temp tables) when a
        CREATE command does not explicitly specify a tablespace.
        Temporary files for purposes such as sorting large data sets
        are also created in these tablespaces.
       

       

        The value is a list of names of tablespaces.  When there is more than
        one name in the list, PostgreSQL chooses a random
        member of the list each time a temporary object is to be created;
        except that within a transaction, successively created temporary
        objects are placed in successive tablespaces from the list.
        If the selected element of the list is an empty string,
        PostgreSQL will automatically use the default
        tablespace of the current database instead.
       

       

        When temp_tablespaces is set interactively, specifying a
        nonexistent tablespace is an error, as is specifying a tablespace for
        which the user does not have CREATE privilege.  However,
        when using a previously set value, nonexistent tablespaces are
        ignored, as are tablespaces for which the user lacks
        CREATE privilege.  In particular, this rule applies when
        using a value set in postgresql.conf.
       

       

        The default value is an empty string, which results in all temporary
        objects being created in the default tablespace of the current
        database.
       

       

        See also .
       

      

     

     

      
check_function_bodies (boolean)
      
       check_function_bodies configuration parameter
      
      

      

       

        This parameter is normally on. When set to off, it
        disables validation of the routine body string during  and .  Disabling validation avoids side
        effects of the validation process, in particular preventing false
        positives due to problems such as forward references.
        Set this parameter
        to off before loading functions on behalf of other
        users; pg_dump does so automatically.
       

      

     

     

      
default_transaction_isolation (enum)
      
       transaction isolation level
       setting default
      
      
       default_transaction_isolation configuration parameter
      
      

      

       

        Each SQL transaction has an isolation level, which can be
        either read uncommitted, read
        committed, repeatable read, or
        serializable.  This parameter controls the
        default isolation level of each new transaction. The default
        is read committed.
       

       

        Consult  and  for more information.
       

      

     

     

      
default_transaction_read_only (boolean)
      
       read-only transaction
       setting default
      
      
       default_transaction_read_only configuration parameter
      
      

      

       

        A read-only SQL transaction cannot alter non-temporary tables.
        This parameter controls the default read-only status of each new
        transaction. The default is off (read/write).
       

       

        Consult  for more information.
       

      

     

     

      
default_transaction_deferrable (boolean)
      
       deferrable transaction
       setting default
      
      
       default_transaction_deferrable configuration parameter
      
      

      

       

        When running at the serializable isolation level,
        a deferrable read-only SQL transaction may be delayed before
        it is allowed to proceed.  However, once it begins executing
        it does not incur any of the overhead required to ensure
        serializability; so serialization code will have no reason to
        force it to abort because of concurrent updates, making this
        option suitable for long-running read-only transactions.
        

        

        This parameter controls the default deferrable status of each
        new transaction.  It currently has no effect on read-write
        transactions or those operating at isolation levels lower
        than serializable. The default is off.
       

       

        Consult  for more information.
       

      

     

     

      
transaction_isolation (enum)
      
       transaction isolation level
      
      
       transaction_isolation configuration parameter
      
      

      

       

        This parameter reflects the current transaction's isolation level.
        At the beginning of each transaction, it is set to the current value
        of .
        Any subsequent attempt to change it is equivalent to a  command.
       

      

     

     

      
transaction_read_only (boolean)
      
       read-only transaction
      
      
       transaction_read_only configuration parameter
      
      

      

       

        This parameter reflects the current transaction's read-only status.
        At the beginning of each transaction, it is set to the current value
        of .
        Any subsequent attempt to change it is equivalent to a  command.
       

      

     

     

      
transaction_deferrable (boolean)
      
       deferrable transaction
      
      
       transaction_deferrable configuration parameter
      
      

      

       

        This parameter reflects the current transaction's deferrability status.
        At the beginning of each transaction, it is set to the current value
        of .
        Any subsequent attempt to change it is equivalent to a  command.
       

      

     

     

      
session_replication_role (enum)
      
       session_replication_role configuration parameter
      
      

      

       

        Controls firing of replication-related triggers and rules for the
        current session.
        Possible values are origin (the default),
        replica and local.
        Setting this parameter results in discarding any previously cached
        query plans.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        The intended use of this setting is that logical replication systems
        set it to replica when they are applying replicated
        changes.  The effect of that will be that triggers and rules (that
        have not been altered from their default configuration) will not fire
        on the replica.  See the ALTER TABLE clauses
        ENABLE TRIGGER and ENABLE RULE
        for more information.
       

       

        PostgreSQL treats the settings origin and
        local the same internally.  Third-party replication
        systems may use these two values for their internal purposes, for
        example using local to designate a session whose
        changes should not be replicated.
       

       

        Since foreign keys are implemented as triggers, setting this parameter
        to replica also disables all foreign key checks,
        which can leave data in an inconsistent state if improperly used.
       

      

     

     

      
statement_timeout (integer)
      
       statement_timeout configuration parameter
      
      

      

       

        Abort any statement that takes more than the specified amount of time.
        If log_min_error_statement is set
        to ERROR or lower, the statement that timed out
        will also be logged.
        If this value is specified without units, it is taken as milliseconds.
        A value of zero (the default) disables the timeout.
       

       

        The timeout is measured from the time a command arrives at the
        server until it is completed by the server.  If multiple SQL
        statements appear in a single simple-query message, the timeout
        is applied to each statement separately.
        (PostgreSQL versions before 13 usually
        treated the timeout as applying to the whole query string.)
        In extended query protocol, the timeout starts running when any
        query-related message (Parse, Bind, Execute, Describe) arrives, and
        it is canceled by completion of an Execute or Sync message.
       

       

        Setting statement_timeout in
        postgresql.conf is not recommended because it would
        affect all sessions.
       

      

     

     

      
transaction_timeout (integer)
      
       transaction_timeout configuration parameter
      
      

      

       

        Terminate any session that spans longer than the specified amount of
        time in a transaction. The limit applies both to explicit transactions
        (started with BEGIN) and to an implicitly started
        transaction corresponding to a single statement.
        If this value is specified without units, it is taken as milliseconds.
        A value of zero (the default) disables the timeout.
       

       

        If transaction_timeout is shorter or equal to
        idle_in_transaction_session_timeout or statement_timeout
        then the longer timeout is ignored.
       

       

        Setting transaction_timeout in
        postgresql.conf is not recommended because it would
        affect all sessions.
       

       
        

         Prepared transactions are not subject to this timeout.
        

       
      

     

     

      
lock_timeout (integer)
      
       lock_timeout configuration parameter
      
      

      

       

        Abort any statement that waits longer than the specified amount of
        time while attempting to acquire a lock on a table, index,
        row, or other database object.  The time limit applies separately to
        each lock acquisition attempt.  The limit applies both to explicit
        locking requests (such as LOCK TABLE, or SELECT
        FOR UPDATE without NOWAIT) and to implicitly-acquired
        locks.
        If this value is specified without units, it is taken as milliseconds.
        A value of zero (the default) disables the timeout.
       

       

        Unlike statement_timeout, this timeout can only occur
        while waiting for locks.  Note that if statement_timeout
        is nonzero, it is rather pointless to set lock_timeout to
        the same or larger value, since the statement timeout would always
        trigger first.  If log_min_error_statement is set to
        ERROR or lower, the statement that timed out will be
        logged.
       

       

        Setting lock_timeout in
        postgresql.conf is not recommended because it would
        affect all sessions.
       

      

     

     

      
idle_in_transaction_session_timeout (integer)
      
       idle_in_transaction_session_timeout configuration parameter
      
      

      

       

        Terminate any session that has been idle (that is, waiting for a
        client query) within an open transaction for longer than the
        specified amount of time.
        If this value is specified without units, it is taken as milliseconds.
        A value of zero (the default) disables the timeout.
       

       

        This option can be used to ensure that idle sessions do not hold
        locks for an unreasonable amount of time.  Even when no significant
        locks are held, an open transaction prevents vacuuming away
        recently-dead tuples that may be visible only to this transaction;
        so remaining idle for a long time can contribute to table bloat.
        See  for more details.
       

      

     

     

      
idle_session_timeout (integer)
      
       idle_session_timeout configuration parameter
      
      

      

       

        Terminate any session that has been idle (that is, waiting for a
        client query), but not within an open transaction, for longer than
        the specified amount of time.
        If this value is specified without units, it is taken as milliseconds.
        A value of zero (the default) disables the timeout.
       

       

        Unlike the case with an open transaction, an idle session without a
        transaction imposes no large costs on the server, so there is less
        need to enable this timeout
        than idle_in_transaction_session_timeout.
       

       

        Be wary of enforcing this timeout on connections made through
        connection-pooling software or other middleware, as such a layer
        may not react well to unexpected connection closure.  It may be
        helpful to enable this timeout only for interactive sessions,
        perhaps by applying it only to particular users.
       

      

     

     

      
bytea_output (enum)
      
       bytea_output configuration parameter
      
      

      

       

        Sets the output format for values of type bytea.
        Valid values are hex (the default)
        and escape (the traditional PostgreSQL
        format).  See  for more
        information.  The bytea type always
        accepts both formats on input, regardless of this setting.
       

      

     

     

      
xmlbinary (enum)
      
       xmlbinary configuration parameter
      
      

      

       

        Sets how binary values are to be encoded in XML.  This applies
        for example when bytea values are converted to
        XML by the functions xmlelement or
        xmlforest.  Possible values are
        base64 and hex, which
        are both defined in the XML Schema standard.  The default is
        base64.  For further information about
        XML-related functions, see .
       

       

        The actual choice here is mostly a matter of taste,
        constrained only by possible restrictions in client
        applications.  Both methods support all possible values,
        although the hex encoding will be somewhat larger than the
        base64 encoding.
       

      

     

     

      
xmloption (enum)
      
       xmloption configuration parameter
      
      
       SET XML OPTION
      
      
       XML option
      
      

      

       

        Sets whether DOCUMENT or
        CONTENT is implicit when converting between
        XML and character string values.  See  for a description of this.  Valid
        values are DOCUMENT and
        CONTENT.  The default is
        CONTENT.
       

       

        According to the SQL standard, the command to set this option is

SET XML OPTION { DOCUMENT | CONTENT };

        This syntax is also available in PostgreSQL.
       

      

     

     

      
gin_pending_list_limit (integer)
      
       gin_pending_list_limit
       configuration parameter
      
      

      

       

        Sets the maximum size of a GIN index's pending list, which is used
        when fastupdate is enabled. If the list grows
        larger than this maximum size, it is cleaned up by moving
        the entries in it to the index's main GIN data structure in bulk.
        If this value is specified without units, it is taken as kilobytes.
        The default is four megabytes (4MB). This setting
        can be overridden for individual GIN indexes by changing
        index storage parameters.
         See  and 
         for more information.
       

      

     

     

      
createrole_self_grant (string)
      
       createrole_self_grant
       configuration parameter
      
      

      

       

        If a user who has CREATEROLE but not
        SUPERUSER creates a role, and if this
        is set to a non-empty value, the newly-created role will be granted
        to the creating user with the options specified. The value must be
        set, inherit, or a
        comma-separated list of these. The default value is an empty string,
        which disables the feature.
       

       

        The purpose of this option is to allow a CREATEROLE
        user who is not a superuser to automatically inherit, or automatically
        gain the ability to SET ROLE to, any created users.
        Since a CREATEROLE user is always implicitly granted
        ADMIN OPTION on created roles, that user could
        always execute a GRANT statement that would achieve
        the same effect as this setting. However, it can be convenient for
        usability reasons if the grant happens automatically. A superuser
        automatically inherits the privileges of every role and can always
        SET ROLE to any role, and this setting can be used
        to produce a similar behavior for CREATEROLE users
        for users which they create.
       

      

     

     

      
event_triggers (boolean)
      
       event_triggers
       configuration parameter
     
     

     

      

       Allow temporarily disabling execution of event triggers in order to
       troubleshoot and repair faulty event triggers. All event triggers will
       be disabled by setting it to false. Setting the value
       to true allows all event triggers to fire, this
       is the default value. Only superusers and users with the appropriate
       SET privilege can change this setting.
      

     

     

     

      
restrict_nonsystem_relation_kind (string)
      
       restrict_nonsystem_relation_kind
       configuration parameter
     
     

     

      

       Set relation kinds for which access to non-system relations is prohibited.
       The value takes the form of a comma-separated list of relation kinds.
       Currently, the supported relation kinds are view and
       foreign-table.
      

     

     

     

    

     

     
# Locale and Formatting

     

     

      
DateStyle (string)
      
       DateStyle configuration parameter
      
      

      

       

        Sets the display format for date and time values, as well as the
        rules for interpreting ambiguous date input values. For
        historical reasons, this variable contains two independent
        components: the output format specification (ISO,
        Postgres, SQL, or German)
        and the input/output specification for year/month/day ordering
        (DMY, MDY, or YMD). These
        can be set separately or together. The keywords Euro
        and European are synonyms for DMY; the
        keywords US, NonEuro, and
        NonEuropean are synonyms for MDY. See
         for more information. The
        built-in default is ISO, MDY, but
        initdb will initialize the
        configuration file with a setting that corresponds to the
        behavior of the chosen lc_time locale.
       

      

     

     

      
IntervalStyle (enum)
      
       IntervalStyle configuration parameter
      
      

      

       

        Sets the display format for interval values.
        The value sql_standard will produce
        output matching SQL standard interval literals.
        The value postgres (which is the default) will produce
        output matching PostgreSQL releases prior to 8.4
        when the 
        parameter was set to ISO.
        The value postgres_verbose will produce output
        matching PostgreSQL releases prior to 8.4
        when the DateStyle
        parameter was set to non-ISO output.
        The value iso_8601 will produce output matching the time
        interval format with designators defined in section
        4.4.3.2 of ISO 8601.
       

       

        The IntervalStyle parameter also affects the
        interpretation of ambiguous interval input.  See
         for more information.
       

      

     

     

      
TimeZone (string)
      
       TimeZone configuration parameter
      
      time zone
      

      

       

        Sets the time zone for displaying and interpreting time stamps.
        The built-in default is GMT, but that is typically
        overridden in postgresql.conf; initdb
        will install a setting there corresponding to its system environment.
        See  for more information.
       

      

     

     

      
timezone_abbreviations (string)
      
       timezone_abbreviations configuration parameter
      
      time zone names
      

      

       

        Sets the collection of additional time zone abbreviations that
        will be accepted by the server for datetime input (beyond any
        abbreviations defined by the current TimeZone
        setting).  The default is 'Default',
        which is a collection that works in most of the world; there are
        also 'Australia' and 'India',
        and other collections can be defined for a particular installation.
        See  for more information.
       

      

     

     

      
extra_float_digits (integer)
      
       significant digits
      
      
       floating-point
       display
      
      
       extra_float_digits configuration parameter
      
      

      

       

        This parameter adjusts the number of digits used for textual output of
        floating-point values, including float4, float8,
        and geometric data types.
       

       

        If the value is 1 (the default) or above, float values are output in
        shortest-precise format; see . The
        actual number of digits generated depends only on the value being
        output, not on the value of this parameter. At most 17 digits are
        required for float8 values, and 9 for float4
        values. This format is both fast and precise, preserving the original
        binary float value exactly when correctly read. For historical
        compatibility, values up to 3 are permitted.
       

       

        If the value is zero or negative, then the output is rounded to a
        given decimal precision. The precision used is the standard number of
        digits for the type (FLT_DIG
        or DBL_DIG as appropriate) reduced according to the
        value of this parameter. (For example, specifying -1 will cause
        float4 values to be output rounded to 5 significant
        digits, and float8 values
        rounded to 14 digits.) This format is slower and does not preserve all
        the bits of the binary float value, but may be more human-readable.
       

       
        

         The meaning of this parameter, and its default value, changed
         in PostgreSQL 12;
         see  for further discussion.
        

       
      

     

     

      
client_encoding (string)
      
       client_encoding configuration parameter
      
      character set
      

      

       

        Sets the client-side encoding (character set).
        The default is to use the database encoding.
        The character sets supported by the PostgreSQL
        server are described in .
       

      

     

     

      
lc_messages (string)
      
       lc_messages configuration parameter
      
      

      

       

        Sets the language in which messages are displayed.  Acceptable
        values are system-dependent; see  for
        more information.  If this variable is set to the empty string
        (which is the default) then the value is inherited from the
        execution environment of the server in a system-dependent way.
       

       

        On some systems, this locale category does not exist.  Setting
        this variable will still work, but there will be no effect.
        Also, there is a chance that no translated messages for the
        desired language exist.  In that case you will continue to see
        the English messages.
       

       

        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
lc_monetary (string)
      
       lc_monetary configuration parameter
      
      

      

       

        Sets the locale to use for formatting monetary amounts, for
        example with the to_char family of
        functions.  Acceptable values are system-dependent; see  for more information.  If this variable is
        set to the empty string (which is the default) then the value
        is inherited from the execution environment of the server in a
        system-dependent way.
       

      

     

     

      
lc_numeric (string)
      
       lc_numeric configuration parameter
      
      

      

       

        Sets the locale to use for formatting numbers, for example
        with the to_char family of
        functions. Acceptable values are system-dependent; see  for more information.  If this variable is
        set to the empty string (which is the default) then the value
        is inherited from the execution environment of the server in a
        system-dependent way.
       

      

     

     

      
lc_time (string)
      
       lc_time configuration parameter
      
      

      

       

        Sets the locale to use for formatting dates and times, for example
        with the to_char family of
        functions. Acceptable values are system-dependent; see  for more information.  If this variable is
        set to the empty string (which is the default) then the value
        is inherited from the execution environment of the server in a
        system-dependent way.
       

      

     

     

      
icu_validation_level (enum)
      
       icu_validation_level configuration parameter
      
      

      

       

        When ICU locale validation problems are encountered, controls which
        message level is
        used to report the problem. Valid values are
        DISABLED, DEBUG5,
        DEBUG4, DEBUG3,
        DEBUG2, DEBUG1,
        INFO, NOTICE,
        WARNING, ERROR, and
        LOG.
       

       

        If set to DISABLED, does not report validation
        problems at all. Otherwise reports problems at the given message
        level. The default is WARNING.
       

      

     

     

      
default_text_search_config (string)
      
       default_text_search_config configuration parameter
      
      

      

       

        Selects the text search configuration that is used by those variants
        of the text search functions that do not have an explicit argument
        specifying the configuration.
        See  for further information.
        The built-in default is pg_catalog.simple, but
        initdb will initialize the
        configuration file with a setting that corresponds to the
        chosen lc_ctype locale, if a configuration
        matching that locale can be identified.
       

      

     

     

    

    

     
# Shared Library Preloading

     

      Several settings are available for preloading shared libraries into the
      server, in order to load additional functionality or achieve performance
      benefits.  For example, a setting of
      '$libdir/mylib' would cause
      mylib.so (or on some platforms,
      mylib.sl) to be preloaded from the installation's standard
      library directory.  The differences between the settings are when they
      take effect and what privileges are required to change them.
     

     

      PostgreSQL procedural language libraries can
      be preloaded in this way, typically by using the
      syntax '$libdir/plXXX' where
      XXX is pgsql, perl,
      tcl, or python.
     

     

      Only shared libraries specifically intended to be used with PostgreSQL
      can be loaded this way.  Every PostgreSQL-supported library has
      a magic block that is checked to guarantee compatibility.  For
      this reason, non-PostgreSQL libraries cannot be loaded in this way.  You
      might be able to use operating-system facilities such
      as LD_PRELOAD for that.
     

     

      In general, refer to the documentation of a specific module for the
      recommended way to load that module.
     

     

     

      
local_preload_libraries (string)
      
       local_preload_libraries configuration parameter
      
      
       $libdir/plugins
      
      

      

       

        This variable specifies one or more shared libraries that are to be
        preloaded at connection start.
        It contains a comma-separated list of library names, where each name
        is interpreted as for the LOAD command.
        Whitespace between entries is ignored; surround a library name with
        double quotes if you need to include whitespace or commas in the name.
        The parameter value only takes effect at the start of the connection.
        Subsequent changes have no effect.  If a specified library is not
        found, the connection attempt will fail.
       

       

        This option can be set by any user.  Because of that, the libraries
        that can be loaded are restricted to those appearing in the
        plugins subdirectory of the installation's
        standard library directory.  (It is the database administrator's
        responsibility to ensure that only safe libraries
        are installed there.)  Entries in local_preload_libraries
        can specify this directory explicitly, for example
        $libdir/plugins/mylib, or just specify
        the library name &mdash; mylib would have
        the same effect as $libdir/plugins/mylib.
       

       

        The intent of this feature is to allow unprivileged users to load
        debugging or performance-measurement libraries into specific sessions
        without requiring an explicit LOAD command.  To that end,
        it would be typical to set this parameter using
        the PGOPTIONS environment variable on the client or by
        using
        ALTER ROLE SET.
       

       

        However, unless a module is specifically designed to be used in this way by
        non-superusers, this is usually not the right setting to use.  Look
        at  instead.
       

      

     

     

      
session_preload_libraries (string)
      
       session_preload_libraries configuration parameter
      
      

      

       

        This variable specifies one or more shared libraries that are to be
        preloaded at connection start.
        It contains a comma-separated list of library names, where each name
        is interpreted as for the LOAD command.
        Whitespace between entries is ignored; surround a library name with
        double quotes if you need to include whitespace or commas in the name.
        The parameter value only takes effect at the start of the connection.
        Subsequent changes have no effect.  If a specified library is not
        found, the connection attempt will fail.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        The intent of this feature is to allow debugging or
        performance-measurement libraries to be loaded into specific sessions
        without an explicit
        LOAD command being given.  For
        example,  could be enabled for all
        sessions under a given user name by setting this parameter
        with ALTER ROLE SET.  Also, this parameter can be changed
        without restarting the server (but changes only take effect when a new
        session is started), so it is easier to add new modules this way, even
        if they should apply to all sessions.
       

       

        Unlike , there is no large
        performance advantage to loading a library at session start rather than
        when it is first used.  There is some advantage, however, when
        connection pooling is used.
       

      

     

     

      
shared_preload_libraries (string)
      
       shared_preload_libraries configuration parameter
      
      

      

       

        This variable specifies one or more shared libraries to be preloaded at
        server start.
        It contains a comma-separated list of library names, where each name
        is interpreted as for the LOAD command.
        Whitespace between entries is ignored; surround a library name with
        double quotes if you need to include whitespace or commas in the name.
        This parameter can only be set at server start.  If a specified
        library is not found, the server will fail to start.
       

       

        Some libraries need to perform certain operations that can only take
        place at postmaster start, such as allocating shared memory, reserving
        light-weight locks, or starting background workers.  Those libraries
        must be loaded at server start through this parameter.  See the
        documentation of each library for details.
       

       

        Other libraries can also be preloaded.  By preloading a shared library,
        the library startup time is avoided when the library is first used.
        However, the time to start each new server process might increase
        slightly, even if that process never uses the library.  So this
        parameter is recommended only for libraries that will be used in most
        sessions.  Also, changing this parameter requires a server restart, so
        this is not the right setting to use for short-term debugging tasks,
        say.  Use  for that
        instead.
       

      
       

        On Windows hosts, preloading a library at server start will not reduce
        the time required to start each new server process; each server process
        will re-load all preload libraries.  However, shared_preload_libraries
         is still useful on Windows hosts for libraries that need to
        perform operations at postmaster start time.
       

      
      

     

     

      
jit_provider (string)
       
        jit_provider configuration parameter
       
      

      

       

        This variable is the name of the JIT provider library to be used
        (see ).
        The default is llvmjit.
        This parameter can only be set at server start.
       

       

        If set to a non-existent library, JIT will not be
        available, but no error will be raised. This allows JIT support to be
        installed separately from the main
        PostgreSQL package.
       

      

     

    

   

     

     
# Other Defaults

     

     

      
dynamic_library_path (string)
      
       dynamic_library_path configuration parameter
      
      dynamic loading
      

      

       

        If a dynamically loadable module needs to be opened and the
        file name specified in the CREATE FUNCTION or
        LOAD command
        does not have a directory component (i.e., the
        name does not contain a slash), the system will search this
        path for the required file.
       

       

        The value for dynamic_library_path must be a
        list of absolute directory paths separated by colons (or semi-colons
        on Windows).  If a list element starts
        with the special string $libdir, the
        compiled-in PostgreSQL package
        library directory is substituted for $libdir; this
        is where the modules provided by the standard
        PostgreSQL distribution are installed.
        (Use pg_config --pkglibdir to find out the name of
        this directory.) For example:

```
dynamic_library_path = '/usr/local/lib/postgresql:/home/my_project/lib:$libdir'
```

        or, in a Windows environment:

```
dynamic_library_path = 'C:\tools\postgresql;H:\my_project\lib;$libdir'
```

       

       

        The default value for this parameter is
        '$libdir'. If the value is set to an empty
        string, the automatic path search is turned off.
       

       

        This parameter can be changed at run time by superusers and users
        with the appropriate SET privilege, but a
        setting done that way will only persist until the end of the
        client connection, so this method should be reserved for
        development purposes. The recommended way to set this parameter
        is in the postgresql.conf configuration
        file.
       

      

     

     

      
extension_control_path (string)
      
       extension_control_path configuration parameter
      
      

      

       

        A path to search for extensions, specifically extension control files
        (name.control).  The
        remaining extension script and secondary control files are then loaded
        from the same directory where the primary control file was found.
        See  for details.
       

       

        The value for extension_control_path must be a
        list of absolute directory paths separated by colons (or semi-colons
        on Windows).  If a list element starts
        with the special string $system, the
        compiled-in PostgreSQL extension
        directory is substituted for $system; this
        is where the extensions provided by the standard
        PostgreSQL distribution are installed.
        (Use pg_config --sharedir to find out the name of
        this directory.) For example:

```
extension_control_path = '/usr/local/share/postgresql:/home/my_project/share:$system'
```

        or, in a Windows environment:

```
extension_control_path = 'C:\tools\postgresql;H:\my_project\share;$system'
```

        Note that the specified paths elements are expected to have a
        subdirectory extension which will contain the
        .control and .sql files; the
        extension suffix is automatically appended to
        each path element.
       

       

        The default value for this parameter is
        '$system'. If the value is set to an empty
        string, the default '$system' is also assumed.
       

       

        If extensions with equal names are present in multiple directories in
        the configured path, only the instance found first in the path will be
        used.
       

       

        This parameter can be changed at run time by superusers and users
        with the appropriate SET privilege, but a
        setting done that way will only persist until the end of the
        client connection, so this method should be reserved for
        development purposes. The recommended way to set this parameter
        is in the postgresql.conf configuration
        file.
       

       

        Note that if you set this parameter to be able to load extensions from
        nonstandard locations, you will most likely also need to set  to a correspondent location, for
        example,

```
extension_control_path = '/usr/local/share/postgresql:$system'
dynamic_library_path = '/usr/local/lib/postgresql:$libdir'
```

       

      

     

     

      
gin_fuzzy_search_limit (integer)
      
       gin_fuzzy_search_limit configuration parameter
      
      

      

       

        Soft upper limit of the size of the set returned by GIN index scans. For more
        information see .
       

      

     

     

    

   

   

    
# Lock Management

     

     

      
deadlock_timeout (integer)
      
       deadlock
       timeout during
      
      
       timeout
       deadlock
      
      
       deadlock_timeout configuration parameter
      
      

      

       

        This is the amount of time to wait on a lock
        before checking to see if there is a deadlock condition. The
        check for deadlock is relatively expensive, so the server doesn't run
        it every time it waits for a lock. We optimistically assume
        that deadlocks are not common in production applications and
        just wait on the lock for a while before checking for a
        deadlock. Increasing this value reduces the amount of time
        wasted in needless deadlock checks, but slows down reporting of
        real deadlock errors.
        If this value is specified without units, it is taken as milliseconds.
        The default is one second (1s),
        which is probably about the smallest value you would want in
        practice. On a heavily loaded server you might want to raise it.
        Ideally the setting should exceed your typical transaction time,
        so as to improve the odds that a lock will be released before
        the waiter decides to check for deadlock.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        When  is set,
        this parameter also determines the amount of time to wait before
        a log message is issued about the lock wait.  If you are trying
        to investigate locking delays you might want to set a shorter than
        normal deadlock_timeout.
       

      

     

     

      
max_locks_per_transaction (integer)
      
       max_locks_per_transaction configuration parameter
      
      

      

       

        The shared lock table has space for
        max_locks_per_transaction objects
        (e.g., tables) per server process or prepared transaction;
        hence, no more than this many distinct objects can be locked at
        any one time.  This parameter limits the average number of object
        locks used by each transaction; individual transactions
        can lock more objects as long as the locks of all transactions
        fit in the lock table.  This is not the number of
        rows that can be locked; that value is unlimited.  The default,
        128, has historically proven sufficient, but you might need to
        raise this value if you have queries that touch many different
        tables in a single transaction, e.g., query of a parent table with
        many children.  This parameter can only be set at server start.
       

       

        This parameter also determines the number of per-backend slots
        available for fast-path
        locking.
       

       

        When running a standby server, you must set this parameter to have the
        same or higher value as on the primary server. Otherwise, queries
        will not be allowed on the standby server.
       

      

     

     

      
max_pred_locks_per_transaction (integer)
      
       max_pred_locks_per_transaction configuration parameter
      
      

      

       

        The shared predicate lock table has space for
        max_pred_locks_per_transaction objects
        (e.g., tables) per server process or prepared transaction;
        hence, no more than this many distinct objects can be locked at
        any one time.  This parameter limits the average number of object
        locks used by each transaction; individual transactions
        can lock more objects as long as the locks of all transactions
        fit in the lock table.  This is not the number of
        rows that can be locked; that value is unlimited.  The default,
        64, has historically proven sufficient, but you might need to
        raise this value if you have clients that touch many different
        tables in a single serializable transaction. This parameter can
        only be set at server start.
       

      

     

     

      
max_pred_locks_per_relation (integer)
      
       max_pred_locks_per_relation configuration parameter
      
      

      

       

        This controls how many pages or tuples of a single relation can be
        predicate-locked before the lock is promoted to covering the whole
        relation.  Values greater than or equal to zero mean an absolute
        limit, while negative values
        mean  divided by
        the absolute value of this setting.  The default is -2, which keeps
        the behavior from previous versions of PostgreSQL.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
max_pred_locks_per_page (integer)
      
       max_pred_locks_per_page configuration parameter
      
      

      

       

        This controls how many rows on a single page can be predicate-locked
        before the lock is promoted to covering the whole page.  The default
        is 2.  This parameter can only be set in
        the postgresql.conf file or on the server command line.
       

      

     

     

   

   

    
# Version and Platform Compatibility

    

     
# Previous PostgreSQL Versions

     

     

      
array_nulls (boolean)
      
       array_nulls configuration parameter
      
      

      

       

        This controls whether the array input parser recognizes
        unquoted NULL as specifying a null array element.
        By default, this is on, allowing array values containing
        null values to be entered.  However, PostgreSQL versions
        before 8.2 did not support null values in arrays, and therefore would
        treat NULL as specifying a normal array element with
        the string value NULL.  For backward compatibility with
        applications that require the old behavior, this variable can be
        turned off.
       

       

        Note that it is possible to create array values containing null values
        even when this variable is off.
       

      

     

     

      
backslash_quote (enum)
      stringsbackslash quotes
      
       backslash_quote configuration parameter
      
      

      

       

        This parameter controls whether a quote mark can be represented by
        \' in the escape string syntax
        (E'...').  The preferred, SQL-standard way
        to represent a quote mark is by doubling it ('') but
        PostgreSQL has historically also accepted
        \'. However, use of \' creates security risks
        because in some client character set encodings, there are multibyte
        characters in which the last byte is numerically equivalent to ASCII
        \.  If client-side code does escaping incorrectly then an
        SQL-injection attack is possible.  This risk can be prevented by
        making the server reject queries in which a quote mark appears to be
        escaped by a backslash.
        The allowed values of backslash_quote are
        on (allow \' always),
        off (reject always), and
        safe_encoding (allow only if client encoding does not
        allow ASCII \ within a multibyte character).
        safe_encoding is the default setting.
       

       

        Note that in an ordinary string literal, \ just
        means \ anyway.  This parameter only affects
        the handling of escape string syntax.
       

      

     

     

      
lo_compat_privileges (boolean)
      
       lo_compat_privileges configuration parameter
      
      

      

       

        In PostgreSQL releases prior to 9.0, large objects
        did not have access privileges and were, therefore, always readable
        and writable by all users.  Setting this variable to on
        disables the new privilege checks, for compatibility with prior
        releases.  The default is off.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

       

        Setting this variable does not disable all security checks related to
        large objects &mdash; only those for which the default behavior has
        changed in PostgreSQL 9.0.
       

      

     

    

      
quote_all_identifiers (boolean)
      
       quote_all_identifiers configuration parameter
      
      

      

       

        When the database generates SQL, force all identifiers to be quoted,
        even if they are not (currently) keywords.  This will affect the
        output of EXPLAIN as well as the results of functions
        like pg_get_viewdef.  See also the
        --quote-all-identifiers option of
         and .
       

      

     

     

      
standard_conforming_strings (boolean)
      stringsstandard conforming
      
       standard_conforming_strings configuration parameter
      
      

      

       

        Beginning in PostgreSQL 19, this
        parameter is always on.  String literals are
        always parsed as specified in the SQL standard (that is,
        backslashes are ordinary characters within a string literal).
        This parameter continues to exist because applications may consult
        it; but it cannot be set to off.
        Escape string syntax ()
        should be used if an application desires
        backslashes to be treated as escape characters.
       

      

     

     

      
synchronize_seqscans (boolean)
      
       synchronize_seqscans configuration parameter
      
      

      

       

        This allows sequential scans of large tables to synchronize with each
        other, so that concurrent scans read the same block at about the
        same time and hence share the I/O workload.  When this is enabled,
        a scan might start in the middle of the table and then wrap
        around the end to cover all rows, so as to synchronize with the
        activity of scans already in progress.  This can result in
        unpredictable changes in the row ordering returned by queries that
        have no ORDER BY clause.  Setting this parameter to
        off ensures the pre-8.3 behavior in which a sequential
        scan always starts from the beginning of the table.  The default
        is on.
       

      

     

     

    

    

     
# Platform and Client Compatibility

     

     

      
transform_null_equals (boolean)
      IS NULL
      
       transform_null_equals configuration parameter
      
      

      

       

        When on, expressions of the form expr =
        NULL (or NULL =
        expr) are treated as
        expr IS NULL, that is, they
        return true if expr evaluates to the null value,
        and false otherwise. The correct SQL-spec-compliant behavior of
        expr = NULL is to always
        return null (unknown). Therefore this parameter defaults to
        off.
       

       

        However, filtered forms in Microsoft
        Access generate queries that appear to use
        expr = NULL to test for
        null values, so if you use that interface to access the database you
        might want to turn this option on.  Since expressions of the
        form expr = NULL always
        return the null value (using the SQL standard interpretation), they are not
        very useful and do not appear often in normal applications so
        this option does little harm in practice.  But new users are
        frequently confused about the semantics of expressions
        involving null values, so this option is off by default.
       

       

        Note that this option only affects the exact form = NULL,
        not other comparison operators or other expressions
        that are computationally equivalent to some expression
        involving the equals operator (such as IN).
        Thus, this option is not a general fix for bad programming.
       

       

        Refer to  for related information.
       

      

     

     

      
allow_alter_system (boolean)
      
       allow_alter_system configuration parameter
      
      

      

       

        When allow_alter_system is set to
        off, an error is returned if the ALTER
        SYSTEM command is executed. This parameter can only be set in
        the postgresql.conf file or on the server command
        line. The default value is on.
       

       

        Note that this setting must not be regarded as a security feature. It
        only disables the ALTER SYSTEM command. It does not
        prevent a superuser from changing the configuration using other SQL
        commands. A superuser has many ways of executing shell commands at
        the operating system level, and can therefore modify
        postgresql.auto.conf regardless of the value of
        this setting.
       

       

        Turning this setting off is intended for environments where the
        configuration of PostgreSQL is managed by
        some external tool.
        In such environments, a well-intentioned superuser might
        mistakenly use ALTER SYSTEM
        to change the configuration instead of using the external tool.
        This might result in unintended behavior, such as the external tool
        overwriting the change at some later point in time when it updates the
        configuration.
        Setting this parameter to off can
        help avoid such mistakes.
       

       

        This parameter only controls the use of ALTER SYSTEM.
        The settings stored in postgresql.auto.conf
        take effect even if allow_alter_system is set to
        off.
       

      

     

     

    

   

   

    
# Error Handling

    

     

      
exit_on_error (boolean)
      
       exit_on_error configuration parameter
      
      

      

       

        If on, any error will terminate the current session.  By default,
        this is set to off, so that only FATAL errors will terminate the
        session.
       

      

     

     

      
restart_after_crash (boolean)
      
       restart_after_crash configuration parameter
      
      

      

       

        When set to on, which is the default, PostgreSQL
        will automatically reinitialize after a backend crash.  Leaving this
        value set to on is normally the best way to maximize the availability
        of the database.  However, in some circumstances, such as when
        PostgreSQL is being invoked by clusterware, it may be
        useful to disable the restart so that the clusterware can gain
        control and take any actions it deems appropriate.
       

       

        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
data_sync_retry (boolean)
      
       data_sync_retry configuration parameter
      
      

      

       

        When set to off, which is the default, PostgreSQL
        will raise a PANIC-level error on failure to flush modified data files
        to the file system.  This causes the database server to crash.  This
        parameter can only be set at server start.
       

       

        On some operating systems, the status of data in the kernel's page
        cache is unknown after a write-back failure.  In some cases it might
        have been entirely forgotten, making it unsafe to retry; the second
        attempt may be reported as successful, when in fact the data has been
        lost.  In these circumstances, the only way to avoid data loss is to
        recover from the WAL after any failure is reported, preferably
        after investigating the root cause of the failure and replacing any
        faulty hardware.
       

       

        If set to on, PostgreSQL will instead
        report an error but continue to run so that the data flushing
        operation can be retried in a later checkpoint.  Only set it to on
        after investigating the operating system's treatment of buffered data
        in case of write-back failure.
       

      

     

     

      
recovery_init_sync_method (enum)
       
        recovery_init_sync_method configuration parameter
       
      

      

       

        When set to fsync, which is the default,
        PostgreSQL will recursively open and
        synchronize all files in the data directory before crash recovery
        begins.  The search for files will follow symbolic links for the WAL
        directory and each configured tablespace (but not any other symbolic
        links).  This is intended to make sure that all WAL and data files are
        durably stored on disk before replaying changes.  This applies whenever
        starting a database cluster that did not shut down cleanly, including
        copies created with pg_basebackup.
       

       

        On Linux, syncfs may be used instead, to ask the
        operating system to synchronize the file systems that contain the
        data directory, the WAL files and each tablespace (but not any other
        file systems that may be reachable through symbolic links).  This may
        be a lot faster than the fsync setting, because it
        doesn't need to open each file one by one.  On the other hand, it may
        be slower if a file system is shared by other applications that
        modify a lot of files, since those files will also be written to disk.
        Furthermore, on versions of Linux before 5.8, I/O errors encountered
        while writing data to disk may not be reported to
        PostgreSQL, and relevant error messages may
        appear only in kernel logs.
       

       

        This parameter can only be set in the
        postgresql.conf file or on the server command line.
       

      

     

    

   

   

    
# Preset Options

    

     The following parameters are read-only.
     As such, they have been excluded from the sample
     postgresql.conf file.  These options report
     various aspects of PostgreSQL behavior
     that might be of interest to certain applications, particularly
     administrative front-ends.
     Most of them are determined when PostgreSQL
     is compiled or when it is installed.
    

    

     

      
block_size (integer)
      
       block_size configuration parameter
      
      

      

       

        Reports the size of a disk block.  It is determined by the value
        of BLCKSZ when building the server. The default
        value is 8192 bytes.  The meaning of some configuration
        variables (such as ) is
        influenced by block_size. See  for information.
       

      

     

     

      
data_checksums (enum)
      
       data_checksums configuration parameter
      
      

      

       

        Reports the state of data checksums for this cluster.
        Possible values are on, off,
        inprogress-on and inprogress-off.
        See  for more information.
       

      

     

     

      
data_directory_mode (integer)
      
       data_directory_mode configuration parameter
      
      

      

       

        On Unix systems this parameter reports the permissions the data
        directory (defined by )
        had at server startup.
        (On Microsoft Windows this parameter will always display
        0700.) See
        the
        initdb -g option
        for more information.
       

      

     

     

      
debug_assertions (boolean)
      
       debug_assertions configuration parameter
      
      

      

       

        Reports whether PostgreSQL has been built
        with assertions enabled. That is the case if the
        macro USE_ASSERT_CHECKING is defined
        when PostgreSQL is built (accomplished
        e.g., by the configure option
        --enable-cassert). By
        default PostgreSQL is built without
        assertions.
       

      

     

     

      
debug_exec_backend (boolean)
      
       debug_exec_backend configuration parameter
      
      

      

       

        Reports whether PostgreSQL has been built
        with EXEC_BACKEND enabled. That is the case on
        Windows or if the
        macro EXEC_BACKEND is defined
        when PostgreSQL is built.
       

      

     

     

      
effective_wal_level (enum)
      
       effective_wal_level configuration parameter
      
      

      

       

        Reports the actual WAL logging level currently in effect in the
        system. This parameter shares the same set of values as
        , but reflects the operational WAL
        level rather than the configured setting. For descriptions of
        possible values, refer to the wal_level
        parameter documentation.
       

       

        The effective WAL level can differ from the configured
        wal_level in certain situations. For example,
        when wal_level is set to replica
        and the system has one or more logical replication slots,
        effective_wal_level will show logical
        to indicate that the system is maintaining WAL records at
        logical level equivalent.
       

       

        On standby servers, effective_wal_level matches
        the value of effective_wal_level from the most
        upstream server in the replication chain.
       

      

     

     

      
huge_pages_status (enum)
      
       huge_pages_status configuration parameter
      
      

      

       

        Reports the state of huge pages in the current instance:
        on, off, or
        unknown (if displayed with
        postgres -C).
        This parameter is useful to determine whether allocation of huge pages
        was successful under huge_pages=try.
        See  for more information.
       

      

     

     

      
integer_datetimes (boolean)
      
       integer_datetimes configuration parameter
      
      

      

       

        Reports whether PostgreSQL was built with support for
        64-bit-integer dates and times.  As of PostgreSQL 10,
        this is always on.
       

      

     

     

      
in_hot_standby (boolean)
      
       in_hot_standby configuration parameter
      
      

      

       

        Reports whether the server is currently in hot standby mode.  When
        this is on, all transactions are forced to be
        read-only.  Within a session, this can change only if the server is
        promoted to be primary.  See  for more
        information.
       

      

     

     

      
max_function_args (integer)
      
       max_function_args configuration parameter
      
      

      

       

        Reports the maximum number of function arguments. It is determined by
        the value of FUNC_MAX_ARGS when building the server. The
        default value is 100 arguments.
       

      

     

     

      
max_identifier_length (integer)
      
       max_identifier_length configuration parameter
      
      

      

       

        Reports the maximum identifier length. It is determined as one
        less than the value of NAMEDATALEN when building
        the server. The default value of NAMEDATALEN is
        64; therefore the default
        max_identifier_length is 63 bytes, which
        can be less than 63 characters when using multibyte encodings.
       

      

     

     

      
max_index_keys (integer)
      
       max_index_keys configuration parameter
      
      

      

       

        Reports the maximum number of index keys. It is determined by
        the value of INDEX_MAX_KEYS when building the server. The
        default value is 32 keys.
       

      

     

     

      
num_os_semaphores (integer)
      
       num_os_semaphores configuration parameter
      
      

      

       

        Reports the number of semaphores that are needed for the server based
        on the configured number of allowed connections
        (), allowed autovacuum worker
        processes (), allowed WAL
        sender processes (), allowed
        background processes (), etc.
       

      

     

     

      
segment_size (integer)
      
       segment_size configuration parameter
      
      

      

       

        Reports the number of blocks (pages) that can be stored within a file
        segment.  It is determined by the value of RELSEG_SIZE
        when building the server.  The maximum size of a segment file in bytes
        is equal to segment_size multiplied by
        block_size; by default this is 1GB.
       

      

     

     

      
server_encoding (string)
      
       server_encoding configuration parameter
      
      character set
      

      

       

        Reports the database encoding (character set).
        It is determined when the database is created.  Ordinarily,
        clients need only be concerned with the value of .
       

      

     

     

      
server_version (string)
      
       server_version configuration parameter
      
      

      

       

        Reports the version number of the server. It is determined by the
        value of PG_VERSION when building the server.
       

      

     

     

      
server_version_num (integer)
      
       server_version_num configuration parameter
      
      

      

       

        Reports the version number of the server as an integer. It is determined
        by the value of PG_VERSION_NUM when building the server.
       

      

     

     

      
shared_memory_size (integer)
      
       shared_memory_size configuration parameter
      
      

      

       

        Reports the size of the main shared memory area, rounded up to the
        nearest megabyte.
       

      

     

     

      
shared_memory_size_in_huge_pages (integer)
      
       shared_memory_size_in_huge_pages configuration parameter
      
      

      

       

        Reports the number of huge pages that are needed for the main shared
        memory area based on the specified .
        If huge pages are not supported, this will be -1.
       

       

        This setting is supported only on Linux.  It
        is always set to -1 on other platforms.  For more
        details about using huge pages on Linux, see
        .
       

      

     

     

      
ssl_library (string)
      
       ssl_library configuration parameter
      
      

      

       

        Reports the name of the SSL library that this
        PostgreSQL server was built with (even if
        SSL is not currently configured or in use on this instance), for
        example OpenSSL, or an empty string if none.
       

      

     

     

      
wal_block_size (integer)
      
       wal_block_size configuration parameter
      
      

      

       

        Reports the size of a WAL disk block.  It is determined by the value
        of XLOG_BLCKSZ when building the server. The default value
        is 8192 bytes.
       

      

     

     

      
wal_segment_size (integer)
      
       wal_segment_size configuration parameter
      
      

      

       

        Reports the size of write ahead log segments.  The default value is
        16MB. See  for more information.
       

      

     

    

   

   

    
# Customized Options

    

     This feature was designed to allow parameters not normally known to
     PostgreSQL to be added by add-on modules
     (such as procedural languages).  This allows extension modules to be
     configured in the standard ways.
    

    

     Custom options have two-part names: an extension name, then a dot, then
     the parameter name proper, much like qualified names in SQL.  An example
     is plpgsql.variable_conflict.
    

    

     Because custom options may need to be set in processes that have not
     loaded the relevant extension module, PostgreSQL
     will accept a setting for any two-part parameter name.  Such variables
     are treated as placeholders and have no function until the module that
     defines them is loaded. When an extension module is loaded, it will add
     its variable definitions and convert any placeholder values according to
     those definitions.  If there are any unrecognized placeholders
     that begin with its extension name, warnings are issued and those
     placeholders are removed.
    

   

   

    
# Developer Options

    

     The following parameters are intended for developer testing, and
     should never be used on a production database.  However, some of
     them can be used to assist with the recovery of severely damaged
     databases.  As such, they have been excluded from the sample
     postgresql.conf file.  Note that many of these
     parameters require special source compilation flags to work at all.
    

    

     

      
allow_in_place_tablespaces (boolean)
      
        allow_in_place_tablespaces configuration parameter
      
      

      

       

        Allows tablespaces to be created as directories inside
        pg_tblspc, when an empty location string
        is provided to the CREATE TABLESPACE command.  This
        is intended to allow testing replication scenarios where primary and
        standby servers are running on the same machine.  Such directories
        are likely to confuse backup tools that expect to find only symbolic
        links in that location.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
allow_system_table_mods (boolean)
      
        allow_system_table_mods configuration parameter
      
      

      

       

        Allows modification of the structure of system tables as well as
        certain other risky actions on system tables.  This is otherwise not
        allowed even for superusers.  Ill-advised use of this setting can
        cause irretrievable data loss or seriously corrupt the database
        system.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
backtrace_functions (string)
      
        backtrace_functions configuration parameter
      
      

      

       

        This parameter contains a comma-separated list of C function names.
        If an error is raised and the name of the internal C function where
        the error happens matches a value in the list, then a backtrace is
        written to the server log together with the error message.  This can
        be used to debug specific areas of the source code.
       

       

        Backtrace support is not available on all platforms, and the quality
        of the backtraces depends on compilation options.
       

       

        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
debug_copy_parse_plan_trees (boolean)
      
       debug_copy_parse_plan_trees configuration parameter
      
      

      

       

        Enabling this forces all parse and plan trees to be passed through
        copyObject(), to facilitate catching errors and
        omissions in copyObject().  The default is off.
       

       

        This parameter is only available when
        DEBUG_NODE_TESTS_ENABLED was defined at compile time
        (which happens automatically when using the
        configure option
        --enable-cassert).
       

      

     

     

      
debug_discard_caches (integer)
      
       debug_discard_caches configuration parameter
      
      

      

       

        When set to 1, each system catalog cache entry is
        invalidated at the first possible opportunity, whether or not
        anything that would render it invalid really occurred.  Caching of
        system catalogs is effectively disabled as a result, so the server
        will run extremely slowly.  Higher values run the cache invalidation
        recursively, which is even slower and only useful for testing
        the caching logic itself.  The default value of 0
        selects normal catalog caching behavior.
       

       

        This parameter can be very helpful when trying to trigger
        hard-to-reproduce bugs involving concurrent catalog changes, but it
        is otherwise rarely needed.  See the source code files
        inval.c and
        pg_config_manual.h for details.
       

       

        This parameter is supported when
        DISCARD_CACHES_ENABLED was defined at compile time
        (which happens automatically when using the
        configure option
        --enable-cassert).  In production builds, its value
        will always be 0 and attempts to set it to another
        value will raise an error.
       

      

     

     

      
debug_io_direct (string)
      
        debug_io_direct configuration parameter
      
      

      

       

        Ask the kernel to minimize caching effects for relation data and WAL
        files using O_DIRECT (most Unix-like systems),
        F_NOCACHE (macOS) or
        FILE_FLAG_NO_BUFFERING (Windows).
       

       

        May be set to an empty string (the default) to disable use of direct
        I/O, or a comma-separated list of operations that should use direct I/O.
        The valid options are data for
        main data files, wal for WAL files, and
        wal_init for WAL files when being initially
        allocated.
        This parameter can only be set at server start.
       

       

        Some operating systems and file systems do not support direct I/O, so
        non-default settings may be rejected at startup or cause errors.
       

       

        Currently this feature reduces performance, and is intended for
        developer testing only.
       

      

     

     

      
debug_parallel_query (enum)
      
       debug_parallel_query configuration parameter
      
      

      

       

        Allows the use of parallel queries for testing purposes even in cases
        where no performance benefit is expected.
        The allowed values of debug_parallel_query are
        off (use parallel mode only when it is expected to improve
        performance), on (force parallel query for all queries
        for which it is thought to be safe), and regress (like
        on, but with additional behavior changes as explained
        below).
       

       

        More specifically, setting this value to on will add
        a Gather node to the top of any query plan for which this
        appears to be safe, so that the query runs inside of a parallel worker.
        Even when a parallel worker is not available or cannot be used,
        operations such as starting a subtransaction that would be prohibited
        in a parallel query context will be prohibited unless the planner
        believes that this will cause the query to fail.  If failures or
        unexpected results occur when this option is set, some functions used
        by the query may need to be marked PARALLEL UNSAFE
        (or, possibly, PARALLEL RESTRICTED).
       

       

        Setting this value to regress has all of the same effects
        as setting it to on plus some additional effects that are
        intended to facilitate automated regression testing.  Normally,
        messages from a parallel worker include a context line indicating that,
        but a setting of regress suppresses this line so that the
        output is the same as in non-parallel execution.  Also,
        the Gather nodes added to plans by this setting are hidden
        in EXPLAIN output so that the output matches what
        would be obtained if this setting were turned off.
       

      

     

     

      
debug_raw_expression_coverage_test (boolean)
      
       debug_raw_expression_coverage_test configuration parameter
      
      

      

       

        Enabling this forces all raw parse trees for DML statements to be
        scanned by raw_expression_tree_walker(), to
        facilitate catching errors and omissions in that function.  The
        default is off.
       

       

        This parameter is only available when
        DEBUG_NODE_TESTS_ENABLED was defined at compile time
        (which happens automatically when using the
        configure option
        --enable-cassert).
       

      

     

     

      
debug_write_read_parse_plan_trees (boolean)
      
       debug_write_read_parse_plan_trees configuration parameter
      
      

      

       

        Enabling this forces all parse and plan trees to be passed through
        outfuncs.c/readfuncs.c, to
        facilitate catching errors and omissions in those modules.  The
        default is off.
       

       

        This parameter is only available when
        DEBUG_NODE_TESTS_ENABLED was defined at compile time
        (which happens automatically when using the
        configure option
        --enable-cassert).
       

      

     

     

      
ignore_system_indexes (boolean)
      
        ignore_system_indexes configuration parameter
      
      

      

       

        Ignore system indexes when reading system tables (but still
        update the indexes when modifying the tables).  This is useful
        when recovering from damaged system indexes.
        This parameter cannot be changed after session start.
       

      

     

     

      
post_auth_delay (integer)
      
       post_auth_delay configuration parameter
      
      

      

       

        The amount of time to delay when a new
        server process is started, after it conducts the
        authentication procedure.  This is intended to give developers an
        opportunity to attach to the server process with a debugger.
        If this value is specified without units, it is taken as seconds.
        A value of zero (the default) disables the delay.
        This parameter cannot be changed after session start.
       

      

     

     

      
pre_auth_delay (integer)
      
       pre_auth_delay configuration parameter
      
      

      

       

        The amount of time to delay just after a
        new server process is forked, before it conducts the
        authentication procedure.  This is intended to give developers an
        opportunity to attach to the server process with a debugger to
        trace down misbehavior in authentication.
        If this value is specified without units, it is taken as seconds.
        A value of zero (the default) disables the delay.
        This parameter can only be set in the postgresql.conf
        file or on the server command line.
       

      

     

     

      
trace_notify (boolean)
      
       trace_notify configuration parameter
      
      

      

       

        Generates a great amount of debugging output for the
        LISTEN and NOTIFY
        commands.   or
         must be
        DEBUG1 or lower to send this output to the
        client or server logs, respectively.
       

      

     

     

      
trace_sort (boolean)
      
       trace_sort configuration parameter
      
      

      

       

        If on, emit information about resource usage during sort operations.
       

      

     

     

      
trace_locks (boolean)
      
       trace_locks configuration parameter
      
      

      

       

        If on, emit information about lock usage.  Information dumped
        includes the type of lock operation, the type of lock and the unique
        identifier of the object being locked or unlocked.  Also included
        are bit masks for the lock types already granted on this object as
        well as for the lock types awaited on this object.  For each lock
        type a count of the number of granted locks and waiting locks is
        also dumped as well as the totals.  An example of the log file output
        is shown here:

```
LOG:  LockAcquire: new: lock(0xb7acd844) id(24688,24696,0,0,0,1)
      grantMask(0) req(0,0,0,0,0,0,0)=0 grant(0,0,0,0,0,0,0)=0
      wait(0) type(AccessShareLock)
LOG:  GrantLock: lock(0xb7acd844) id(24688,24696,0,0,0,1)
      grantMask(2) req(1,0,0,0,0,0,0)=1 grant(1,0,0,0,0,0,0)=1
      wait(0) type(AccessShareLock)
LOG:  UnGrantLock: updated: lock(0xb7acd844) id(24688,24696,0,0,0,1)
      grantMask(0) req(0,0,0,0,0,0,0)=0 grant(0,0,0,0,0,0,0)=0
      wait(0) type(AccessShareLock)
LOG:  CleanUpLock: deleting: lock(0xb7acd844) id(24688,24696,0,0,0,1)
      grantMask(0) req(0,0,0,0,0,0,0)=0 grant(0,0,0,0,0,0,0)=0
      wait(0) type(INVALID)
```

        Details of the structure being dumped may be found in
        src/include/storage/lock.h.
       

       

        This parameter is only available if the LOCK_DEBUG
        macro was defined when PostgreSQL was
        compiled.
       

      

     

     

      
trace_lwlocks (boolean)
      
       trace_lwlocks configuration parameter
      
      

      

       

        If on, emit information about lightweight lock usage.  Lightweight
        locks are intended primarily to provide mutual exclusion of access
        to shared-memory data structures.
       

       

        This parameter is only available if the LOCK_DEBUG
        macro was defined when PostgreSQL was
        compiled.
       

      

     

     

      
trace_userlocks (boolean)
      
       trace_userlocks configuration parameter
      
      

      

       

        If on, emit information about user lock usage.  Output is the same
        as for trace_locks, only for advisory locks.
       

       

        This parameter is only available if the LOCK_DEBUG
        macro was defined when PostgreSQL was
        compiled.
       

      

     

     

      
trace_lock_oidmin (integer)
      
       trace_lock_oidmin configuration parameter
      
      

      

       

        If set, do not trace locks for tables below this OID (used to avoid
        output on system tables).
       

       

        This parameter is only available if the LOCK_DEBUG
        macro was defined when PostgreSQL was
        compiled.
       

      

     

     

      
trace_lock_table (integer)
      
       trace_lock_table configuration parameter
      
      

      

       

        Unconditionally trace locks on this table (OID).
       

       

        This parameter is only available if the LOCK_DEBUG
        macro was defined when PostgreSQL was
        compiled.
       

      

     

     

      
debug_deadlocks (boolean)
      
       debug_deadlocks configuration parameter
      
      

      

       

        If set, dumps information about all current locks when a
        deadlock timeout occurs.
       

       

        This parameter is only available if the LOCK_DEBUG
        macro was defined when PostgreSQL was
        compiled.
       

      

     

     

      
log_btree_build_stats (boolean)
      
       log_btree_build_stats configuration parameter
      
      

      

       

        If set, logs system resource usage statistics (memory and CPU) on
        various B-tree operations.
       

       

        This parameter is only available if the BTREE_BUILD_STATS
        macro was defined when PostgreSQL was
        compiled.
       

      

     

     

      
wal_consistency_checking (string)
      
       wal_consistency_checking configuration parameter
      
      

      

       

        This parameter is intended to be used to check for bugs in the WAL
        redo routines.  When enabled, full-page images of any buffers modified
        in conjunction with the WAL record are added to the record.
        If the record is subsequently replayed, the system will first apply
        each record and then test whether the buffers modified by the record
        match the stored images.  In certain cases (such as hint bits), minor
        variations are acceptable, and will be ignored.  Any unexpected
        differences will result in a fatal error, terminating recovery.
       

       

        The default value of this setting is the empty string, which disables
        the feature.  It can be set to all to check all
        records, or to a comma-separated list of resource managers to check
        only records originating from those resource managers.  Currently,
        the supported resource managers are heap,
        heap2, btree, hash,
        gin, gist, sequence,
        spgist, brin, and generic.
        Extensions may define additional resource managers. Only superusers and users with
        the appropriate SET privilege can change this setting.
       

      

     

     

      
wal_debug (boolean)
      
       wal_debug configuration parameter
      
      

      

       

        If on, emit WAL-related debugging output. This parameter is
        only available if the WAL_DEBUG macro was
        defined when PostgreSQL was
        compiled.
       

      

     

    

      
ignore_checksum_failure (boolean)
      
       ignore_checksum_failure configuration parameter
      
      

      

       

        Only has effect if data checksums are enabled.
       

       

        Detection of a checksum failure during a read normally causes
        PostgreSQL to report an error, aborting the current
        transaction.  Setting ignore_checksum_failure to on causes
        the system to ignore the failure (but still report a warning), and
        continue processing.  This behavior may cause crashes, propagate
        or hide corruption, or other serious problems.  However, it may allow
        you to get past the error and retrieve undamaged tuples that might still be
        present in the table if the block header is still sane. If the header is
        corrupt an error will be reported even if this option is enabled. The
        default setting is off.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

    

      
zero_damaged_pages (boolean)
      
       zero_damaged_pages configuration parameter
      
      

      

       

        Detection of a damaged page header normally causes
        PostgreSQL to report an error, aborting the current
        transaction.  Setting zero_damaged_pages to on causes
        the system to instead report a warning, zero out the damaged
        page in memory, and continue processing.  This behavior will destroy data,
        namely all the rows on the damaged page.  However, it does allow you to get
        past the error and retrieve rows from any undamaged pages that might
        be present in the table.  It is useful for recovering data if
        corruption has occurred due to a hardware or software error.  You should
        generally not set this on until you have given up hope of recovering
        data from the damaged pages of a table.  Zeroed-out pages are not
        forced to disk so it is recommended to recreate the table or
        the index before turning this parameter off again.  The
        default setting is off.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
ignore_invalid_pages (boolean)
      
       ignore_invalid_pages configuration parameter
      
      

      

       

        If set to off (the default), detection of
        WAL records having references to invalid pages during
        recovery causes PostgreSQL to
        raise a PANIC-level error, aborting the recovery. Setting
        ignore_invalid_pages to on
        causes the system to ignore invalid page references in WAL records
        (but still report a warning), and continue the recovery.
        This behavior may cause crashes, data loss,
        propagate or hide corruption, or other serious problems.
        However, it may allow you to get past the PANIC-level error,
        to finish the recovery, and to cause the server to start up.
        The parameter can only be set at server start. It only has effect
        during recovery or in standby mode.
       

      

     

     

      
jit_debugging_support (boolean)
      
       jit_debugging_support configuration parameter
      
      

      

       

        If LLVM has the required functionality, register generated functions
        with GDB.  This makes debugging easier.
        The default setting is off.
        Only superusers and users with the appropriate SET
        privilege can change this parameter at session start,
        and it cannot be changed at all within a session.
       

      

     

     

      
jit_dump_bitcode (boolean)
      
       jit_dump_bitcode configuration parameter
      
      

      

       

        Writes the generated LLVM IR out to the
        file system, inside . This is only
        useful for working on the internals of the JIT implementation.
        The default setting is off.
        Only superusers and users with the appropriate SET
        privilege can change this setting.
       

      

     

     

      
jit_expressions (boolean)
      
       jit_expressions configuration parameter
      
      

      

       

        Determines whether expressions are JIT compiled, when JIT compilation
        is activated (see ).  The default is
        on.
       

      

     

     

      
jit_profiling_support (boolean)
      
       jit_profiling_support configuration parameter
      
      

      

       

        If LLVM has the required functionality, emit the data needed to allow
        perf to profile functions generated by JIT.
        This writes out files to ~/.debug/jit/; the
        user is responsible for performing cleanup when desired.
        The default setting is off.
        Only superusers and users with the appropriate SET
        privilege can change this parameter at session start,
        and it cannot be changed at all within a session.
       

      

     

     

      
jit_tuple_deforming (boolean)
      
       jit_tuple_deforming configuration parameter
      
      

      

       

        Determines whether tuple deforming is JIT compiled, when JIT
        compilation is activated (see ).
        The default is on.
       

      

     

     

      
remove_temp_files_after_crash (boolean)
      
       remove_temp_files_after_crash configuration parameter
      
      

      

       

        When set to on, which is the default,
        PostgreSQL will automatically remove
        temporary files after a backend crash. If disabled, the files will be
        retained and may be used for debugging, for example. Repeated crashes
        may however result in accumulation of useless files. This parameter
        can only be set in the postgresql.conf file or on
        the server command line.
       

      

     

     

      
send_abort_for_crash (boolean)
      
       send_abort_for_crash configuration parameter
      
      

      

       

        By default, after a backend crash the postmaster will stop remaining
        child processes by sending them SIGQUIT
        signals, which permits them to exit more-or-less gracefully.  When
        this option is set to on,
        SIGABRT is sent instead.  That normally
        results in production of a core dump file for each such child
        process.
        This can be handy for investigating the states of other processes
        after a crash.  It can also consume lots of disk space in the event
        of repeated crashes, so do not enable this on systems you are not
        monitoring carefully.
        Beware that no support exists for cleaning up the core file(s)
        automatically.
        This parameter can only be set in
        the postgresql.conf file or on the server
        command line.
       

      

     

     

      
send_abort_for_kill (boolean)
      
       send_abort_for_kill configuration parameter
      
      

      

       

        By default, after attempting to stop a child process with
        SIGQUIT, the postmaster will wait five
        seconds and then send SIGKILL to force
        immediate termination.  When this option is set
        to on, SIGABRT is sent
        instead of SIGKILL.  That normally results
        in production of a core dump file for each such child process.
        This can be handy for investigating the states
        of stuck child processes.  It can also consume lots
        of disk space in the event of repeated crashes, so do not enable
        this on systems you are not monitoring carefully.
        Beware that no support exists for cleaning up the core file(s)
        automatically.
        This parameter can only be set in
        the postgresql.conf file or on the server
        command line.
       

      

     

     

      
debug_logical_replication_streaming (enum)
      
       debug_logical_replication_streaming configuration parameter
      
      

      

       

        The allowed values are buffered and
        immediate. The default is buffered.
        This parameter is intended to be used to test logical decoding and
        replication of large transactions.  The effect of
        debug_logical_replication_streaming is different for the
        publisher and subscriber:
       

       

        On the publisher side, debug_logical_replication_streaming
        allows streaming or serializing changes immediately in logical decoding.
        When set to immediate, stream each change if the
        streaming
        option of
        CREATE SUBSCRIPTION
        is enabled, otherwise, serialize each change.  When set to
        buffered, the decoding will stream or serialize
        changes when logical_decoding_work_mem is reached.
       

       

        On the subscriber side, if the streaming option is set to
        parallel, debug_logical_replication_streaming
        can be used to direct the leader apply worker to send changes to the
        shared memory queue or to serialize all changes to the file.  When set to
        buffered, the leader sends changes to parallel apply
        workers via a shared memory queue.  When set to
        immediate, the leader serializes all changes to files
        and notifies the parallel apply workers to read and apply them at the
        end of the transaction.
       

      

     

    

  

  

   
# Short Options

   

    For convenience there are also single letter command-line option
    switches available for some parameters.  They are described in
    .  Some of these
    options exist for historical reasons, and their presence as a
    single-letter option does not necessarily indicate an endorsement
    to use the option heavily.
   

    
     
# Short Option Key

     
      
      
      
       
        Short Option
        Equivalent
       
      

      
       
        -B x
        shared_buffers = x
       
       
        -d x
        log_min_messages = DEBUGx
       
       
        -e
        datestyle = euro
       
       
        
          -fb, -fh, -fi,
          -fm, -fn, -fo,
          -fs, -ft
         
         
          enable_bitmapscan = off,
          enable_hashjoin = off,
          enable_indexscan = off,
          enable_mergejoin = off,
          enable_nestloop = off,
          enable_indexonlyscan = off,
          enable_seqscan = off,
          enable_tidscan = off
         
       
       
        -F
        fsync = off
       
       
        -h x
        listen_addresses = x
       
       
        -i
        listen_addresses = '*'
       
       
        -k x
        unix_socket_directories = x
       
       
        -l
        ssl = on
       
       
        -N x
        max_connections = x
       
       
        -O
        allow_system_table_mods = on
       
       
        -p x
        port = x
       
       
        -P
        ignore_system_indexes = on
       
       
        -s
        log_statement_stats = on
       
       
        -S x
        work_mem = x
       
       
        -tpa, -tpl, -te
        log_parser_stats = on,
        log_planner_stats = on,
        log_executor_stats = on
       
       
        -W x
        post_auth_delay = x
