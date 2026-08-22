# System Information Functions and Operators

   

    The functions described in this section are used to obtain various
    information about a PostgreSQL installation.
   

  

   
# Session Information Functions

  

    shows several
   functions that extract session and system information.
  

  

   In addition to the functions listed in this section, there are a number of
   functions related to the statistics system that also provide system
   information. See  for more
   information.
  

   
    
# Session Information Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         current_catalog
        
        current_catalog
        name
       

       

        
         current_database
        
        current_database ()
        name
       

       

        Returns the name of the current database.  (Databases are
        called catalogs in the SQL standard,
        so current_catalog is the standard's
        spelling.)
       

      

      
       

        
         current_query
        
        current_query ()
        text
       

       

        Returns the text of the currently executing query, as submitted
        by the client (which might contain more than one statement).
       

      

      
       

        
         current_role
        
        current_role
        name
       

       

        This is equivalent to current_user.
       

      

      
       

        
         current_schema
        
        
         schema
         current
        
        current_schema
        name
       

       

        current_schema ()
        name
       

       

        Returns the name of the schema that is first in the search path (or a
        null value if the search path is empty).  This is the schema that will
        be used for any tables or other named objects that are created without
        specifying a target schema.
       

      

      
       

        
         current_schemas
        
        
         search path
         current
        
        current_schemas ( 
include_implicit
 boolean )
        name[]
       

       

        Returns an array of the names of all schemas presently in the
        effective search path, in their priority order.  (Items in the current
         setting that do not correspond to
        existing, searchable schemas are omitted.)  If the Boolean argument
        is true, then implicitly-searched system schemas
        such as pg_catalog are included in the result.
       

      

      
       

        
         current_user
        
        
         user
         current
        
        current_user
        name
       

       

        Returns the user name of the current execution context.
       

      

      
       

        
         inet_client_addr
        
        inet_client_addr ()
        inet
       

       

        Returns the IP address of the current client,
        or NULL if the current connection is via a
        Unix-domain socket.
       

      

      
       

        
         inet_client_port
        
        inet_client_port ()
        integer
       

       

        Returns the IP port number of the current client,
        or NULL if the current connection is via a
        Unix-domain socket.
       

      

      
       

        
         inet_server_addr
        
        inet_server_addr ()
        inet
       

       

        Returns the IP address on which the server accepted the current
        connection,
        or NULL if the current connection is via a
        Unix-domain socket.
       

      

      
       

        
         inet_server_port
        
        inet_server_port ()
        integer
       

       

        Returns the IP port number on which the server accepted the current
        connection,
        or NULL if the current connection is via a
        Unix-domain socket.
       

      

      
       

        
         pg_backend_pid
        
        pg_backend_pid ()
        integer
       

       

        Returns the process ID of the server process attached to the current
        session.
       

      

      
       

        
         pg_blocking_pids
        
        pg_blocking_pids ( integer )
        integer[]
       

       

        Returns an array of the process ID(s) of the sessions that are
        blocking the server process with the specified process ID from
        acquiring a lock, or an empty array if there is no such server process
        or it is not blocked.
       

       

        One server process blocks another if it either holds a lock that
        conflicts with the blocked process's lock request (hard block), or is
        waiting for a lock that would conflict with the blocked process's lock
        request and is ahead of it in the wait queue (soft block).  When using
        parallel queries the result always lists client-visible process IDs
        (that is, pg_backend_pid results) even if the
        actual lock is held or awaited by a child worker process.  As a result
        of that, there may be duplicated PIDs in the result.  Also note that
        when a prepared transaction holds a conflicting lock, it will be
        represented by a zero process ID.
       

       

        Frequent calls to this function could have some impact on database
        performance, because it needs exclusive access to the lock manager's
        shared state for a short time.
       

      

      
       

        
         pg_conf_load_time
        
        pg_conf_load_time ()
        timestamp with time zone
       

       

        Returns the time when the server configuration files were last loaded.
        If the current session was alive at the time, this will be the time
        when the session itself re-read the configuration files (so the
        reading will vary a little in different sessions).  Otherwise it is
        the time when the postmaster process re-read the configuration files.
       

      

      
       

        
         pg_current_logfile
        
        
         Logging
         pg_current_logfile function
        
        
          current_logfiles
          and the pg_current_logfile function
        
        
         Logging
         current_logfiles file and the pg_current_logfile
         function
        
        pg_current_logfile (  text  )
        text
       

       

        Returns the path name of the log file currently in use by the logging
        collector.  The path includes the 
        directory and the individual log file name.  The result
        is NULL if the logging collector is disabled.
        When multiple log files exist, each in a different
        format, pg_current_logfile without an argument
        returns the path of the file having the first format found in the
        ordered list: stderr,
        csvlog, jsonlog.
        NULL is returned if no log file has any of these
        formats.
        To request information about a specific log file format, supply
        either csvlog, jsonlog or
        stderr as the
        value of the optional parameter. The result is NULL
        if the log format requested is not configured in
        .
        The result reflects the contents of
        the current_logfiles file.
       

       

        This function is restricted to superusers and roles with privileges of
        the pg_monitor role by default, but other users can
        be granted EXECUTE to run the function.
       

      

      
       

        
         pg_get_loaded_modules
        
        pg_get_loaded_modules ()
        setof record
        ( 
module_name
 text,
        
version
 text,
        
file_name
 text )
       

       

        Returns a list of the loadable modules that are loaded into the
        current server session.  The 
module_name

        and 
version
 fields are NULL unless the
        module author supplied values for them using
        the PG_MODULE_MAGIC_EXT macro.
        The 
file_name
 field gives the file
        name of the module (shared library).
       

      

      
       

        
         pg_my_temp_schema
        
        pg_my_temp_schema ()
        oid
       

       

        Returns the OID of the current session's temporary schema, or zero if
        it has none (because it has not created any temporary tables).
       

      

      
       

        
         pg_is_other_temp_schema
        
        pg_is_other_temp_schema ( oid )
        boolean
       

       

        Returns true if the given OID is the OID of another session's
        temporary schema.  (This can be useful, for example, to exclude other
        sessions' temporary tables from a catalog display.)
       

      

      
       

        
         pg_jit_available
        
        pg_jit_available ()
        boolean
       

       

        Returns true if a JIT compiler extension is
        available (see ) and the
         configuration parameter is set to
        on.
       

      

      
       

        
         pg_numa_available
        
        pg_numa_available ()
        boolean
       

       

        Returns true if the server has been compiled with NUMA support.
       

      

      
       

        
         pg_listening_channels
        
        pg_listening_channels ()
        setof text
       

       

        Returns the set of names of asynchronous notification channels that
        the current session is listening to.
       

      

      
       

        
         pg_notification_queue_usage
        
        pg_notification_queue_usage ()
        double precision
       

       

        Returns the fraction (0&ndash;1) of the asynchronous notification
        queue's maximum size that is currently occupied by notifications that
        are waiting to be processed.
        See  and 
        for more information.
       

      

      
       

        
         pg_postmaster_start_time
        
        pg_postmaster_start_time ()
        timestamp with time zone
       

       

        Returns the time when the server started.
       

      

      
       

        
         pg_safe_snapshot_blocking_pids
        
        pg_safe_snapshot_blocking_pids ( integer )
        integer[]
       

       

        Returns an array of the process ID(s) of the sessions that are blocking
        the server process with the specified process ID from acquiring a safe
        snapshot, or an empty array if there is no such server process or it
        is not blocked.
       

       

        A session running a SERIALIZABLE transaction blocks
        a SERIALIZABLE READ ONLY DEFERRABLE transaction
        from acquiring a snapshot until the latter determines that it is safe
        to avoid taking any predicate locks.  See
         for more information about
        serializable and deferrable transactions.
       

       

        Frequent calls to this function could have some impact on database
        performance, because it needs access to the predicate lock manager's
        shared state for a short time.
       

      

      
       

        
         pg_trigger_depth
        
        pg_trigger_depth ()
        integer
       

       

        Returns the current nesting level
        of PostgreSQL triggers (0 if not called,
        directly or indirectly, from inside a trigger).
       

      

      
       

        
         session_user
        
        session_user
        name
       

       

        Returns the session user's name.
       

      

      
       

        
         system_user
        
        system_user
        text
       

       

        Returns the authentication method and the identity (if any) that the
        user presented during the authentication cycle before they were
        assigned a database role. It is represented as
        auth_method:identity or
        NULL if the user has not been authenticated (for
        example if Trust authentication has
        been used).
       

      

      
       

        
         user
        
        user
        name
       

       

        This is equivalent to current_user.
       

      
     
    
   

   
    

     current_catalog,
     current_role,
     current_schema,
     current_user,
     session_user,
     and user have special syntactic status
     in SQL: they must be called without trailing
     parentheses.  In PostgreSQL, parentheses can optionally be used with
     current_schema, but not with the others.
    

   

   

    The session_user is normally the user who initiated
    the current database connection; but superusers can change this setting
    with .
    The current_user is the user identifier
    that is applicable for permission checking. Normally it is equal
    to the session user, but it can be changed with
    .
    It also changes during the execution of
    functions with the attribute SECURITY DEFINER.
    In Unix parlance, the session user is the real user and
    the current user is the effective user.
    current_role and user are
    synonyms for current_user.  (The SQL standard draws
    a distinction between current_role
    and current_user, but PostgreSQL
    does not, since it unifies users and roles into a single kind of entity.)
   

  

  

   
# Access Privilege Inquiry Functions

  
   privilege
   querying
  

  

    lists functions that
   allow querying object access privileges programmatically.
   (See  for more information about
   privileges.)
   In these functions, the user whose privileges are being inquired about
   can be specified by name or by OID
   (pg_authid.oid), or if
   the name is given as public then the privileges of the
   PUBLIC pseudo-role are checked.  Also, the 
user

   argument can be omitted entirely, in which case
   the current_user is assumed.
   The object that is being inquired about can be specified either by name or
   by OID, too.  When specifying by name, a schema name can be included if
   relevant.
   The access privilege of interest is specified by a text string, which must
   evaluate to one of the appropriate privilege keywords for the object's type
   (e.g., SELECT).  Optionally, WITH GRANT
   OPTION can be added to a privilege type to test whether the
   privilege is held with grant option. Also, multiple privilege types can be
   listed separated by commas, in which case the result will be true if any of
   the listed privileges is held. (Case of the privilege string is not
   significant, and extra whitespace is allowed between but not within
   privilege names.)
   Some examples:

```
SELECT has_table_privilege('myschema.mytable', 'select');
SELECT has_table_privilege('joe', 'mytable', 'INSERT, SELECT WITH GRANT OPTION');
```

  

   
    
# Access Privilege Inquiry Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         has_any_column_privilege
        
        has_any_column_privilege (
           
user
 name or oid, 
          
table
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for any column of table?
        This succeeds either if the privilege is held for the whole table, or
        if there is a column-level grant of the privilege for at least one
        column.
        Allowable privilege types are
        SELECT, INSERT,
        UPDATE, and REFERENCES.
       

      

      
       

        
         has_column_privilege
        
        has_column_privilege (
           
user
 name or oid, 
          
table
 text or oid,
          
column
 text or smallint,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for the specified table column?
        This succeeds either if the privilege is held for the whole table, or
        if there is a column-level grant of the privilege for the column.
        The column can be specified by name or by attribute number
        (pg_attribute.attnum).
        Allowable privilege types are
        SELECT, INSERT,
        UPDATE, and REFERENCES.
       

      

      
       

        
         has_database_privilege
        
        has_database_privilege (
           
user
 name or oid, 
          
database
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for database?
        Allowable privilege types are
        CREATE,
        CONNECT,
        TEMPORARY, and
        TEMP (which is equivalent to
        TEMPORARY).
       

      

      
       

        
         has_foreign_data_wrapper_privilege
        
        has_foreign_data_wrapper_privilege (
           
user
 name or oid, 
          
fdw
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for foreign-data wrapper?
        The only allowable privilege type is USAGE.
       

      

      
       

        
         has_function_privilege
        
        has_function_privilege (
           
user
 name or oid, 
          
function
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for function?
        The only allowable privilege type is EXECUTE.
       

       

        When specifying a function by name rather than by OID, the allowed
        input is the same as for the regprocedure data type (see
        ).
        An example is:

```
SELECT has_function_privilege('joeuser', 'myfunc(int, text)', 'execute');
```

       

      

      
       

        
         has_language_privilege
        
        has_language_privilege (
           
user
 name or oid, 
          
language
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for language?
        The only allowable privilege type is USAGE.
       

      

      
       

        
         has_largeobject_privilege
        
        has_largeobject_privilege (
           
user
 name or oid, 
          
largeobject
 oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for large object?
        Allowable privilege types are
        SELECT and UPDATE.
       

      

      
       

        
         has_parameter_privilege
        
        has_parameter_privilege (
           
user
 name or oid, 
          
parameter
 text,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for configuration parameter?
        The parameter name is case-insensitive.
        Allowable privilege types are SET
        and ALTER SYSTEM.
       

      

      
       

        
         has_schema_privilege
        
        has_schema_privilege (
           
user
 name or oid, 
          
schema
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for schema?
        Allowable privilege types are
        CREATE and
        USAGE.
       

      

      
       

        
         has_sequence_privilege
        
        has_sequence_privilege (
           
user
 name or oid, 
          
sequence
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for sequence?
        Allowable privilege types are
        USAGE,
        SELECT, and
        UPDATE.
       

      

      
       

        
         has_server_privilege
        
        has_server_privilege (
           
user
 name or oid, 
          
server
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for foreign server?
        The only allowable privilege type is USAGE.
       

      

      
       

        
         has_table_privilege
        
        has_table_privilege (
           
user
 name or oid, 
          
table
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for table?
        Allowable privilege types
        are SELECT, INSERT,
        UPDATE, DELETE,
        TRUNCATE, REFERENCES,
        TRIGGER, and MAINTAIN.
       

      

      
       

        
         has_tablespace_privilege
        
        has_tablespace_privilege (
           
user
 name or oid, 
          
tablespace
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for tablespace?
        The only allowable privilege type is CREATE.
       

      

      
       

        
         has_type_privilege
        
        has_type_privilege (
           
user
 name or oid, 
          
type
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for data type?
        The only allowable privilege type is USAGE.
        When specifying a type by name rather than by OID, the allowed input
        is the same as for the regtype data type (see
        ).
       

      

      
       

        
         pg_has_role
        
        pg_has_role (
           
user
 name or oid, 
          
role
 text or oid,
          
privilege
 text )
        boolean
       

       

        Does user have privilege for role?
        Allowable privilege types are
        MEMBER, USAGE,
        and SET.
        MEMBER denotes direct or indirect membership in
        the role without regard to what specific privileges may be conferred.
        USAGE denotes whether the privileges of the role
        are immediately available without doing SET ROLE,
        while SET denotes whether it is possible to change
        to the role using the SET ROLE command.
        WITH ADMIN OPTION or WITH GRANT
        OPTION can be added to any of these privilege types to
        test whether the ADMIN privilege is held (all
        six spellings test the same thing).
        This function does not allow the special case of
        setting 
user
 to public,
        because the PUBLIC pseudo-role can never be a member of real roles.
       

      

      
       

        
         row_security_active
        
        row_security_active (
          
table
 text or oid )
        boolean
       

       

        Is row-level security active for the specified table in the context of
        the current user and current environment?
       

      
     
    
   

  

    shows the operators
   available for the aclitem type, which is the catalog
   representation of access privileges.  See 
   for information about how to read access privilege values.
  

    
     
# aclitem Operators

     
      
       
        

         Operator
        

        

         Description
        

        

         Example(s)
        

       
      

      
       
        

         
          aclitemeq
         
         aclitem = aclitem
         boolean
        

        

         Are aclitems equal?  (Notice that
         type aclitem lacks the usual set of comparison
         operators; it has only equality.  In turn, aclitem
         arrays can only be compared for equality.)
        

        

         'calvin=r*w/hobbes'::aclitem = 'calvin=r*w*/hobbes'::aclitem
         f
        

       

       
        

         
          aclcontains
         
         aclitem[] @> aclitem
         boolean
        

        

         Does array contain the specified privileges?  (This is true if there
         is an array entry that matches the aclitem's grantee and
         grantor, and has at least the specified set of privileges.)
        

        

         '{calvin=r*w/hobbes,hobbes=r*w*/postgres}'::aclitem[] @> 'calvin=r*/hobbes'::aclitem
         t
        

       
      
     
    

   

     shows some additional
    functions to manage the aclitem type.
   

   
    
# aclitem Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         acldefault
        
        acldefault (
          
type
 "char",
          
ownerId
 oid )
        aclitem[]
       

       

        Constructs an aclitem array holding the default access
        privileges for an object of type 
type
 belonging
        to the role with OID 
ownerId
.  This represents
        the access privileges that will be assumed when an object's
        ACL entry is null. (The default access privileges
        are described in .)
        The 
type
 parameter must be one of
        'c' for COLUMN,
        'r' for TABLE and table-like objects,
        's' for SEQUENCE,
        'd' for DATABASE,
        'f' for FUNCTION or PROCEDURE,
        'l' for LANGUAGE,
        'L' for LARGE OBJECT,
        'n' for SCHEMA,
        'p' for PARAMETER,
        't' for TABLESPACE,
        'F' for FOREIGN DATA WRAPPER,
        'S' for FOREIGN SERVER,
        or
        'T' for TYPE or DOMAIN.
       

      

      
       

        
         aclexplode
        
        aclexplode ( aclitem[] )
        setof record
        ( 
grantor
 oid,
        
grantee
 oid,
        
privilege_type
 text,
        
is_grantable
 boolean )
       

       

        Returns the aclitem array as a set of rows.
        If the grantee is the pseudo-role PUBLIC, it is represented by zero in
        the 
grantee
 column.  Each granted privilege is
        represented as SELECT, INSERT,
        etc (see  for a full list).
        Note that each privilege is broken out as a separate row, so
        only one keyword appears in the 
privilege_type

        column.
       

      

      
       

        
         makeaclitem
        
        makeaclitem (
          
grantee
 oid,
          
grantor
 oid,
          
privileges
 text,
          
is_grantable
 boolean )
        aclitem
       

       

        Constructs an aclitem with the given properties.
        
privileges
 is a comma-separated list of
        privilege names such as SELECT,
        INSERT, etc, all of which are set in the
        result.  (Case of the privilege string is not significant, and
        extra whitespace is allowed between but not within privilege
        names.)
       

      
     
    
   

  

  

   
# Schema Visibility Inquiry Functions

  

    shows functions that
   determine whether a certain object is visible in the
   current schema search path.
   For example, a table is said to be visible if its
   containing schema is in the search path and no table of the same
   name appears earlier in the search path.  This is equivalent to the
   statement that the table can be referenced by name without explicit
   schema qualification.  Thus, to list the names of all visible tables:

```
SELECT relname FROM pg_class WHERE pg_table_is_visible(oid);
```

   For functions and operators, an object in the search path is said to be
   visible if there is no object of the same name and argument data
   type(s) earlier in the path.  For operator classes and families,
   both the name and the associated index access method are considered.
  

   
    search path
    object visibility
   

   
    
# Schema Visibility Inquiry Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         pg_collation_is_visible
        
        pg_collation_is_visible ( 
collation
 oid )
        boolean
       

       

        Is collation visible in search path?
       

      

      
       

        
         pg_conversion_is_visible
        
        pg_conversion_is_visible ( 
conversion
 oid )
        boolean
       

       

        Is conversion visible in search path?
       

      

      
       

        
         pg_function_is_visible
        
        pg_function_is_visible ( 
function
 oid )
        boolean
       

       

        Is function visible in search path?
        (This also works for procedures and aggregates.)
       

      

      
       

        
         pg_opclass_is_visible
        
        pg_opclass_is_visible ( 
opclass
 oid )
        boolean
       

       

        Is operator class visible in search path?
       

      

      
       

        
         pg_operator_is_visible
        
        pg_operator_is_visible ( 
operator
 oid )
        boolean
       

       

        Is operator visible in search path?
       

      

      
       

        
         pg_opfamily_is_visible
        
        pg_opfamily_is_visible ( 
opclass
 oid )
        boolean
       

       

        Is operator family visible in search path?
       

      

      
       

        
         pg_statistics_obj_is_visible
        
        pg_statistics_obj_is_visible ( 
stat
 oid )
        boolean
       

       

        Is statistics object visible in search path?
       

      

      
       

        
         pg_table_is_visible
        
        pg_table_is_visible ( 
table
 oid )
        boolean
       

       

        Is table visible in search path?
        (This works for all types of relations, including views, materialized
        views, indexes, sequences and foreign tables.)
       

      

      
       

        
         pg_ts_config_is_visible
        
        pg_ts_config_is_visible ( 
config
 oid )
        boolean
       

       

        Is text search configuration visible in search path?
       

      

      
       

        
         pg_ts_dict_is_visible
        
        pg_ts_dict_is_visible ( 
dict
 oid )
        boolean
       

       

        Is text search dictionary visible in search path?
       

      

      
       

        
         pg_ts_parser_is_visible
        
        pg_ts_parser_is_visible ( 
parser
 oid )
        boolean
       

       

        Is text search parser visible in search path?
       

      

      
       

        
         pg_ts_template_is_visible
        
        pg_ts_template_is_visible ( 
template
 oid )
        boolean
       

       

        Is text search template visible in search path?
       

      

      
       

        
         pg_type_is_visible
        
        pg_type_is_visible ( 
type
 oid )
        boolean
       

       

        Is type (or domain) visible in search path?
       

      
     
    
   

   

    All these functions require object OIDs to identify the object to be
    checked.  If you want to test an object by name, it is convenient to use
    the OID alias types (regclass, regtype,
    regprocedure, regoperator, regconfig,
    or regdictionary),
    for example:

```
SELECT pg_type_is_visible('myschema.widget'::regtype);
```

    Note that it would not make much sense to test a non-schema-qualified
    type name in this way &mdash; if the name can be recognized at all, it must be visible.
   

  

  

   
# System Catalog Information Functions

  

    lists functions that
   extract information from the system catalogs.
  

   
    
# System Catalog Information Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         format_type
        
        format_type ( 
type
 oid, 
typemod
 integer )
        text
       

       

        Returns the SQL name for a data type that is identified by its type
        OID and possibly a type modifier.  Pass NULL for the type modifier if
        no specific modifier is known.
       

      

      
       

        
         pg_basetype
        
        pg_basetype ( regtype )
        regtype
       

       

        Returns the OID of the base type of a domain identified by its
        type OID.  If the argument is the OID of a non-domain type,
        returns the argument as-is.  Returns NULL if the argument is
        not a valid type OID.  If there's a chain of domain dependencies,
        it will recurse until finding the base type.
       

       

        Assuming CREATE DOMAIN mytext AS text:
       

       

        pg_basetype('mytext'::regtype)
        text
       

      

      
       

        
         pg_char_to_encoding
        
        pg_char_to_encoding ( 
encoding
 name )
        integer
       

       

        Converts the supplied encoding name into an integer representing the
        internal identifier used in some system catalog tables.
        Returns -1 if an unknown encoding name is provided.
       

      

      
       

        
         pg_encoding_to_char
        
        pg_encoding_to_char ( 
encoding
 integer )
        name
       

       

        Converts the integer used as the internal identifier of an encoding in some
        system catalog tables into a human-readable string.
        Returns an empty string if an invalid encoding number is provided.
       

      

      
       

        
         pg_get_catalog_foreign_keys
        
        pg_get_catalog_foreign_keys ()
        setof record
        ( 
fktable
 regclass,
          
fkcols
 text[],
          
pktable
 regclass,
          
pkcols
 text[],
          
is_array
 boolean,
          
is_opt
 boolean )
       

       

        Returns a set of records describing the foreign key relationships
        that exist within the PostgreSQL system
        catalogs.
        The 
fktable
 column contains the name of the
        referencing catalog, and the 
fkcols
 column
        contains the name(s) of the referencing column(s).  Similarly,
        the 
pktable
 column contains the name of the
        referenced catalog, and the 
pkcols
 column
        contains the name(s) of the referenced column(s).
        If 
is_array
 is true, the last referencing
        column is an array, each of whose elements should match some entry
        in the referenced catalog.
        If 
is_opt
 is true, the referencing column(s)
        are allowed to contain zeroes instead of a valid reference.
       

      

      
       

        
         pg_get_constraintdef
        
        pg_get_constraintdef ( 
constraint
 oid , 
pretty
 boolean  )
        text
       

       

        Reconstructs the creating command for a constraint.
        (This is a decompiled reconstruction, not the original text
        of the command.)
       

      

      
       

        
         pg_get_expr
        
        pg_get_expr ( 
expr
 pg_node_tree, 
relation
 oid , 
pretty
 boolean  )
        text
       

       

        Decompiles the internal form of an expression stored in the system
        catalogs, such as the default value for a column.  If the expression
        might contain Vars, specify the OID of the relation they refer to as
        the second parameter; if no Vars are expected, passing zero is
        sufficient.
       

      

      
       

        
         pg_get_functiondef
        
        pg_get_functiondef ( 
func
 oid )
        text
       

       

        Reconstructs the creating command for a function or procedure.
        (This is a decompiled reconstruction, not the original text
        of the command.)
        The result is a complete CREATE OR REPLACE FUNCTION
        or CREATE OR REPLACE PROCEDURE statement.
       

      

      
       

        
         pg_get_function_arguments
        
        pg_get_function_arguments ( 
func
 oid )
        text
       

       

        Reconstructs the argument list of a function or procedure, in the form
        it would need to appear in within CREATE FUNCTION
        (including default values).
       

      

      
       

        
         pg_get_function_identity_arguments
        
        pg_get_function_identity_arguments ( 
func
 oid )
        text
       

       

        Reconstructs the argument list necessary to identify a function or
        procedure, in the form it would need to appear in within commands such
        as ALTER FUNCTION.  This form omits default values.
       

      

      
       

        
         pg_get_function_result
        
        pg_get_function_result ( 
func
 oid )
        text
       

       

        Reconstructs the RETURNS clause of a function, in
        the form it would need to appear in within CREATE
        FUNCTION.  Returns NULL for a procedure.
       

      

      
       

        
         pg_get_indexdef
        
        pg_get_indexdef ( 
index
 oid , 
column
 integer, 
pretty
 boolean  )
        text
       

       

        Reconstructs the creating command for an index.
        (This is a decompiled reconstruction, not the original text
        of the command.)  If 
column
 is supplied and is
        not zero, only the definition of that column is reconstructed.
       

      

      
       

        
         pg_get_keywords
        
        pg_get_keywords ()
        setof record
        ( 
word
 text,
        
catcode
 "char",
        
barelabel
 boolean,
        
catdesc
 text,
        
baredesc
 text )
       

       

        Returns a set of records describing the SQL keywords recognized by the
        server.  The 
word
 column contains the
        keyword.  The 
catcode
 column contains a
        category code: U for an unreserved
        keyword, C for a keyword that can be a column
        name, T for a keyword that can be a type or
        function name, or R for a fully reserved keyword.
        The 
barelabel
 column
        contains true if the keyword can be used as
        a bare column label in SELECT lists,
        or false if it can only be used
        after AS.
        The 
catdesc
 column contains a
        possibly-localized string describing the keyword's category.
        The 
baredesc
 column contains a
        possibly-localized string describing the keyword's column label status.
       

      

      
       

        
         pg_get_partition_constraintdef
        
        pg_get_partition_constraintdef ( 
table
 oid )
        text
       

       

        Reconstructs the definition of a partition constraint.
        (This is a decompiled reconstruction, not the original text
        of the command.)
       

      

      
       

        
         pg_get_partkeydef
        
        pg_get_partkeydef ( 
table
 oid )
        text
       

       

        Reconstructs the definition of a partitioned table's partition
        key, in the form it would have in the PARTITION
        BY clause of CREATE TABLE.
        (This is a decompiled reconstruction, not the original text
        of the command.)
       

      

      
       

        
         pg_get_propgraphdef
        
        pg_get_propgraphdef ( 
propgraph
 oid )
        text
       

       

        Reconstructs the creating command for a property graph.
        (This is a decompiled reconstruction, not the original text
        of the command.)
       

      

      
       

        
         pg_get_ruledef
        
        pg_get_ruledef ( 
rule
 oid , 
pretty
 boolean  )
        text
       

       

        Reconstructs the creating command for a rule.
        (This is a decompiled reconstruction, not the original text
        of the command.)
       

      

      
       

        
         pg_get_serial_sequence
        
        pg_get_serial_sequence ( 
table
 text, 
column
 text )
        text
       

       

        Returns the name of the sequence associated with a column,
        or NULL if no sequence is associated with the column.
        If the column is an identity column, the associated sequence is the
        sequence internally created for that column.
        For columns created using one of the serial types
        (serial, smallserial, bigserial),
        it is the sequence created for that serial column definition.
        In the latter case, the association can be modified or removed
        with ALTER SEQUENCE OWNED BY.
        (This function probably should have been
        called pg_get_owned_sequence; its current name
        reflects the fact that it has historically been used with serial-type
        columns.)  The first parameter is a table name with optional
        schema, and the second parameter is a column name.  Because the first
        parameter potentially contains both schema and table names, it is
        parsed per usual SQL rules, meaning it is lower-cased by default.
        The second parameter, being just a column name, is treated literally
        and so has its case preserved.  The result is suitably formatted
        for passing to the sequence functions (see
        ).
       

       

        A typical use is in reading the current value of the sequence for an
        identity or serial column, for example:

```
SELECT currval(pg_get_serial_sequence('sometable', 'id'));
```

       

      

      
       

        
         pg_get_statisticsobjdef
        
        pg_get_statisticsobjdef ( 
statobj
 oid )
        text
       

       

        Reconstructs the creating command for an extended statistics object.
        (This is a decompiled reconstruction, not the original text
        of the command.)
       

      

      
       

        
         pg_get_triggerdef
        
pg_get_triggerdef ( 
trigger
 oid , 
pretty
 boolean  )
        text
       

       

        Reconstructs the creating command for a trigger.
        (This is a decompiled reconstruction, not the original text
        of the command.)
       

      

      
       

        
         pg_get_userbyid
        
        pg_get_userbyid ( 
role
 oid )
        name
       

       

        Returns a role's name given its OID.
       

      

      
       

        
         pg_get_viewdef
        
        pg_get_viewdef ( 
view
 oid , 
pretty
 boolean  )
        text
       

       

        Reconstructs the underlying SELECT command for a
        view or materialized view.  (This is a decompiled reconstruction, not
        the original text of the command.)
       

      

      
       

        pg_get_viewdef ( 
view
 oid, 
wrap_column
 integer )
        text
       

       

        Reconstructs the underlying SELECT command for a
        view or materialized view.  (This is a decompiled reconstruction, not
        the original text of the command.)  In this form of the function,
        pretty-printing is always enabled, and long lines are wrapped to try
        to keep them shorter than the specified number of columns.
       

      

      
       

        pg_get_viewdef ( 
view
 text , 
pretty
 boolean  )
        text
       

       

        Reconstructs the underlying SELECT command for a
        view or materialized view, working from a textual name for the view
        rather than its OID.  (This is deprecated; use the OID variant
        instead.)
       

      

      
       

        
         pg_index_column_has_property
        
        pg_index_column_has_property ( 
index
 regclass, 
column
 integer, 
property
 text )
        boolean
       

       

        Tests whether an index column has the named property.
        Common index column properties are listed in
        .
        (Note that extension access methods can define additional property
        names for their indexes.)
        NULL is returned if the property name is not known
        or does not apply to the particular object, or if the OID or column
        number does not identify a valid object.
       

      

      
       

        
         pg_index_has_property
        
        pg_index_has_property ( 
index
 regclass, 
property
 text )
        boolean
       

       

        Tests whether an index has the named property.
        Common index properties are listed in
        .
        (Note that extension access methods can define additional property
        names for their indexes.)
        NULL is returned if the property name is not known
        or does not apply to the particular object, or if the OID does not
        identify a valid object.
       

      

      
       

        
         pg_indexam_has_property
        
        pg_indexam_has_property ( 
am
 oid, 
property
 text )
        boolean
       

       

        Tests whether an index access method has the named property.
        Access method properties are listed in
        .
        NULL is returned if the property name is not known
        or does not apply to the particular object, or if the OID does not
        identify a valid object.
       

      

      
       

        
         pg_options_to_table
        
        pg_options_to_table ( 
options_array
 text[] )
        setof record
        ( 
option_name
 text,
        
option_value
 text )
       

       

        Returns the set of storage options represented by a value from
        pg_class.reloptions or
        pg_attribute.attoptions.
       

      

      
       

        
         pg_settings_get_flags
        
        pg_settings_get_flags ( 
guc
 text )
        text[]
       

       

        Returns an array of the flags associated with the given GUC, or
        NULL if it does not exist. The result is
        an empty array if the GUC exists but there are no flags to show.
        Only the most useful flags listed in
         are exposed.
       

      

      
       

        
         pg_tablespace_databases
        
        pg_tablespace_databases ( 
tablespace
 oid )
        setof oid
       

       

        Returns the set of OIDs of databases that have objects stored in the
        specified tablespace.  If this function returns any rows, the
        tablespace is not empty and cannot be dropped.  To identify the specific
        objects populating the tablespace, you will need to connect to the
        database(s) identified by pg_tablespace_databases
        and query their pg_class catalogs.
       

      

      
       

        
         pg_tablespace_location
        
        pg_tablespace_location ( 
tablespace
 oid )
        text
       

       

        Returns the file system path that this tablespace is located in.
       

      

      
       

        
         pg_typeof
        
        pg_typeof ( "any" )
        regtype
       

       

        Returns the OID of the data type of the value that is passed to it.
        This can be helpful for troubleshooting or dynamically constructing
        SQL queries.  The function is declared as
        returning regtype, which is an OID alias type (see
        ); this means that it is the same as an
        OID for comparison purposes but displays as a type name.
       

       

        pg_typeof(33)
        integer
       

      

      
       

        
         COLLATION FOR
        
        COLLATION FOR ( "any" )
        text
       

       

        Returns the name of the collation of the value that is passed to it.
        The value is quoted and schema-qualified if necessary.  If no
        collation was derived for the argument expression,
        then NULL is returned.  If the argument is not of a
        collatable data type, then an error is raised.
       

       

        COLLATION FOR ('foo'::text)
        "default"
       

       

        COLLATION FOR ('foo' COLLATE "de_DE")
        "de_DE"
       

      

      
       

        
         to_regclass
        
        to_regclass ( text )
        regclass
       

       

        Translates a textual relation name to its OID.  A similar result is
        obtained by casting the string to type regclass (see
        ); however, this function will return
        NULL rather than throwing an error if the name is
        not found.
       

      

      
       

        
         to_regdatabase
        
        to_regdatabase ( text )
        regdatabase
       

       

        Translates a textual database name to its OID.  A similar result is
        obtained by casting the string to type regdatabase (see
        ); however, this function will return
        NULL rather than throwing an error if the name is
        not found.
       

      

      
       

        
         to_regcollation
        
        to_regcollation ( text )
        regcollation
       

       

        Translates a textual collation name to its OID.  A similar result is
        obtained by casting the string to type regcollation (see
        ); however, this function will return
        NULL rather than throwing an error if the name is
        not found.
       

      

      
       

        
         to_regnamespace
        
        to_regnamespace ( text )
        regnamespace
       

       

        Translates a textual schema name to its OID.  A similar result is
        obtained by casting the string to type regnamespace (see
        ); however, this function will return
        NULL rather than throwing an error if the name is
        not found.
       

      

      
       

        
         to_regoper
        
        to_regoper ( text )
        regoper
       

       

        Translates a textual operator name to its OID.  A similar result is
        obtained by casting the string to type regoper (see
        ); however, this function will return
        NULL rather than throwing an error if the name is
        not found or is ambiguous.
       

      

      
       

        
         to_regoperator
        
        to_regoperator ( text )
        regoperator
       

       

        Translates a textual operator name (with parameter types) to its OID.  A similar result is
        obtained by casting the string to type regoperator (see
        ); however, this function will return
        NULL rather than throwing an error if the name is
        not found.
       

      

      
       

        
         to_regproc
        
        to_regproc ( text )
        regproc
       

       

        Translates a textual function or procedure name to its OID.  A similar result is
        obtained by casting the string to type regproc (see
        ); however, this function will return
        NULL rather than throwing an error if the name is
        not found or is ambiguous.
       

      

      
       

        
         to_regprocedure
        
        to_regprocedure ( text )
        regprocedure
       

       

        Translates a textual function or procedure name (with argument types) to its OID.  A similar result is
        obtained by casting the string to type regprocedure (see
        ); however, this function will return
        NULL rather than throwing an error if the name is
        not found.
       

      

      
       

        
         to_regrole
        
        to_regrole ( text )
        regrole
       

       

        Translates a textual role name to its OID.  A similar result is
        obtained by casting the string to type regrole (see
        ); however, this function will return
        NULL rather than throwing an error if the name is
        not found.
       

      

      
       

        
         to_regtype
        
        to_regtype ( text )
        regtype
       

       

        Parses a string of text, extracts a potential type name from it,
        and translates that name into a type OID.  A syntax error in the
        string will result in an error; but if the string is a
        syntactically valid type name that happens not to be found in the
        catalogs, the result is NULL.  A similar result
        is obtained by casting the string to type regtype
        (see ), except that that will throw
        error for name not found.
       

      

      
       

        
         to_regtypemod
        
        to_regtypemod ( text )
        integer
       

       

        Parses a string of text, extracts a potential type name from it,
        and translates its type modifier, if any.  A syntax error in the
        string will result in an error; but if the string is a
        syntactically valid type name that happens not to be found in the
        catalogs, the result is NULL.  The result is
        -1 if no type modifier is present.
       

       

        to_regtypemod can be combined with
         to produce appropriate inputs for
        , allowing a string representing a
        type name to be canonicalized.
       

       

        format_type(to_regtype('varchar(32)'), to_regtypemod('varchar(32)'))
        character varying(32)
       

      
     
    
   

  

   Most of the functions that reconstruct (decompile) database objects
   have an optional 
pretty
 flag, which
   if true causes the result to
   be pretty-printed.  Pretty-printing suppresses unnecessary
   parentheses and adds whitespace for legibility.
   The pretty-printed format is more readable, but the default format
   is more likely to be interpreted the same way by future versions of
   PostgreSQL; so avoid using pretty-printed output
   for dump purposes.  Passing false for
   the 
pretty
 parameter yields the same result as
   omitting the parameter.
  

  
   
# Index Column Properties

   
    
    
    
     NameDescription
    
    
     
      asc
      Does the column sort in ascending order on a forward scan?
      
     
     
      desc
      Does the column sort in descending order on a forward scan?
      
     
     
      nulls_first
      Does the column sort with nulls first on a forward scan?
      
     
     
      nulls_last
      Does the column sort with nulls last on a forward scan?
      
     
     
      orderable
      Does the column possess any defined sort ordering?
      
     
     
      distance_orderable
      Can the column be scanned in order by a distance
      operator, for example ORDER BY col <-> constant ?
      
     
     
      returnable
      Can the column value be returned by an index-only scan?
      
     
     
      search_array
      Does the column natively support col = ANY(array)
      searches?
      
     
     
      search_nulls
      Does the column support IS NULL and
      IS NOT NULL searches?
      
     
    
   
  

  
   
# Index Properties

   
    
    
    
     NameDescription
    
    
     
      clusterable
      Can the index be used in a CLUSTER command?
      
     
     
      index_scan
      Does the index support plain (non-bitmap) scans?
      
     
     
      bitmap_scan
      Does the index support bitmap scans?
      
     
     
      backward_scan
      Can the scan direction be changed in mid-scan (to
             support FETCH BACKWARD on a cursor without
             needing materialization)?
      
     
    
   
  

  
   
# Index Access Method Properties

   
    
    
    
     NameDescription
    
    
     
      can_order
      Does the access method support ASC,
      DESC and related keywords in
      CREATE INDEX?
      
     
     
      can_unique
      Does the access method support unique indexes?
      
     
     
      can_multi_col
      Does the access method support indexes with multiple columns?
      
     
     
      can_exclude
      Does the access method support exclusion constraints?
      
     
     
      can_include
      Does the access method support the INCLUDE
        clause of CREATE INDEX?
      
     
    
   
  

  
   
# GUC Flags

   
    
    
    
     FlagDescription
    
    
     
      EXPLAIN
      Parameters with this flag are included in
       EXPLAIN (SETTINGS) commands.
      
     
     
      NO_SHOW_ALL
      Parameters with this flag are excluded from
       SHOW ALL commands.
      
     
     
      NO_RESET
      Parameters with this flag do not support
      RESET commands.
      
     
     
      NO_RESET_ALL
      Parameters with this flag are excluded from
       RESET ALL commands.
      
     
     
      NOT_IN_SAMPLE
      Parameters with this flag are not included in
       postgresql.conf by default.
      
     
     
      RUNTIME_COMPUTED
      Parameters with this flag are runtime-computed ones.
      
     
    
   
  

  

  

   
# Object Information and Addressing Functions

  

    lists functions related to
   database object identification and addressing.
  

   
    
# Object Information and Addressing Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         pg_get_acl
        
        pg_get_acl ( 
classid
 oid, 
objid
 oid, 
objsubid
 integer )
        aclitem[]
       

       

        Returns the ACL for a database object, specified
        by catalog OID, object OID and sub-object ID. This function returns
        NULL values for undefined objects.
       

      

      
       

        
         pg_describe_object
        
        pg_describe_object ( 
classid
 oid, 
objid
 oid, 
objsubid
 integer )
        text
       

       

        Returns a textual description of a database object identified by
        catalog OID, object OID, and sub-object ID (such as a column number
        within a table; the sub-object ID is zero when referring to a whole
        object).  This description is intended to be human-readable, and might
        be translated, depending on server configuration.  This is especially
        useful to determine the identity of an object referenced in the
        pg_depend catalog. This function returns
        NULL values for undefined objects.
       

      

      
       

        
         pg_identify_object
        
        pg_identify_object ( 
classid
 oid, 
objid
 oid, 
objsubid
 integer )
        record
        ( 
type
 text,
        
schema
 text,
        
name
 text,
        
identity
 text )
       

       

        Returns a row containing enough information to uniquely identify the
        database object specified by catalog OID, object OID and sub-object
        ID.
        This information is intended to be machine-readable, and is never
        translated.
        
type
 identifies the type of database object;
        
schema
 is the schema name that the object
        belongs in, or NULL for object types that do not
        belong to schemas;
        
name
 is the name of the object, quoted if
        necessary, if the name (along with schema name, if pertinent) is
        sufficient to uniquely identify the object,
        otherwise NULL;
        
identity
 is the complete object identity, with
        the precise format depending on object type, and each name within the
        format being schema-qualified and quoted as necessary. Undefined
        objects are identified with NULL values.
       

      

      
       

        
         pg_identify_object_as_address
        
        pg_identify_object_as_address ( 
classid
 oid, 
objid
 oid, 
objsubid
 integer )
        record
        ( 
type
 text,
        
object_names
 text[],
        
object_args
 text[] )
       

       

        Returns a row containing enough information to uniquely identify the
        database object specified by catalog OID, object OID and sub-object
        ID.
        The returned information is independent of the current server, that
        is, it could be used to identify an identically named object in
        another server.
        
type
 identifies the type of database object;
        
object_names
 and
        
object_args

        are text arrays that together form a reference to the object.
        These three values can be passed
        to pg_get_object_address to obtain the internal
        address of the object.
       

      

      
       

        
         pg_get_object_address
        
        pg_get_object_address ( 
type
 text, 
object_names
 text[], 
object_args
 text[] )
        record
        ( 
classid
 oid,
        
objid
 oid,
        
objsubid
 integer )
       

       

        Returns a row containing enough information to uniquely identify the
        database object specified by a type code and object name and argument
        arrays.
        The returned values are the ones that would be used in system catalogs
        such as pg_depend; they can be passed to
        other system functions such as pg_describe_object
        or pg_identify_object.
        
classid
 is the OID of the system catalog
        containing the object;
        
objid
 is the OID of the object itself, and
        
objsubid
 is the sub-object ID, or zero if none.
        This function is the inverse
        of pg_identify_object_as_address.
        Undefined objects are identified with NULL values.
       

      
     
    
   

   

    pg_get_acl is useful for retrieving and inspecting
    the privileges associated with database objects without looking at
    specific catalogs. For example, to retrieve all the granted privileges
    on objects in the current database:

```
postgres=# SELECT
    (pg_identify_object(s.classid,s.objid,s.objsubid)).*,
    pg_catalog.pg_get_acl(s.classid,s.objid,s.objsubid) AS acl
FROM pg_catalog.pg_shdepend AS s
JOIN pg_catalog.pg_database AS d
    ON d.datname = current_database() AND
       d.oid = s.dbid
JOIN pg_catalog.pg_authid AS a
    ON a.oid = s.refobjid AND
       s.refclassid = 'pg_authid'::regclass
WHERE s.deptype = 'a';
-[ RECORD 1 ]-----------------------------------------
type     | table
schema   | public
name     | testtab
identity | public.testtab
acl      | {postgres=arwdDxtm/postgres,foo=r/postgres}
```

   

  

  

   
# Comment Information Functions

   
    comment
    about database objects
   

   

    The functions shown in 
    extract comments previously stored with the 
    command.  A null value is returned if no
    comment could be found for the specified parameters.
   

   
    
# Comment Information Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         col_description
        
        col_description ( 
table
 oid, 
column
 integer )
        text
       

       

        Returns the comment for a table column, which is specified by the OID
        of its table and its column number.
        (obj_description cannot be used for table
        columns, since columns do not have OIDs of their own.)
       

      

      
       

        
         obj_description
        
        obj_description ( 
object
 oid, 
catalog
 name )
        text
       

       

        Returns the comment for a database object specified by its OID and the
        name of the containing system catalog.  For
        example, obj_description(123456, 'pg_class') would
        retrieve the comment for the table with OID 123456.
       

      

      
       

        obj_description ( 
object
 oid )
        text
       

       

        Returns the comment for a database object specified by its OID alone.
        This is deprecated since there is no guarantee
        that OIDs are unique across different system catalogs; therefore, the
        wrong comment might be returned.
       

      

      
       

        
         shobj_description
        
        shobj_description ( 
object
 oid, 
catalog
 name )
        text
       

       

        Returns the comment for a shared database object specified by its OID
        and the name of the containing system catalog.  This is just
        like obj_description except that it is used for
        retrieving comments on shared objects (that is, databases, roles, and
        tablespaces).  Some system catalogs are global to all databases within
        each cluster, and the descriptions for objects in them are stored
        globally as well.
       

      
     
    
   

  

  

   
# Data Validity Checking Functions

   

    The functions shown in 
    can be helpful for checking validity of proposed input data.
   

   
    
# Data Validity Checking Functions

    
     
      
       

        Function
       

       

        Description
       

       

        Example(s)
       

      
     

     
      
       

        
         pg_input_is_valid
        
        pg_input_is_valid (
          
string
 text,
          
type
 text
        )
        boolean
       

       

        Tests whether the given 
string
 is valid
        input for the specified data type, returning true or false.
       

       

        This function will only work as desired if the data type's input
        function has been updated to report invalid input as
        a soft error.  Otherwise, invalid input will abort
        the transaction, just as if the string had been cast to the type
        directly.
        

        

         pg_input_is_valid('42', 'integer')
         t
        

        

         pg_input_is_valid('42000000000', 'integer')
         f
        

        

         pg_input_is_valid('1234.567', 'numeric(7,4)')
         f
       

      
      
       

        
         pg_input_error_info
        
        pg_input_error_info (
          
string
 text,
          
type
 text
        )
        record
        ( 
message
 text,
        
detail
 text,
        
hint
 text,
        
sql_error_code
 text )
       

       

        Tests whether the given 
string
 is valid
        input for the specified data type; if not, return the details of
        the error that would have been thrown.  If the input is valid, the
        results are NULL.  The inputs are the same as
        for pg_input_is_valid.
       

       

        This function will only work as desired if the data type's input
        function has been updated to report invalid input as
        a soft error.  Otherwise, invalid input will abort
        the transaction, just as if the string had been cast to the type
        directly.
       

       

        SELECT * FROM pg_input_error_info('42000000000', 'integer')
        

```
message                        | detail | hint | sql_error_code
------------------------------------------------------+--------+------+----------------
 value "42000000000" is out of range for type integer |        |      | 22003
```

       

      
     
    
   

  

  

   
# Transaction ID and Snapshot Information Functions

   

    The functions shown in 
    provide server transaction information in an exportable form.  The main
    use of these functions is to determine which transactions were committed
    between two snapshots.
   

   
    
# Transaction ID and Snapshot Information Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         age
        
        age  ( xid )
        integer
       

       

        Returns the number of transactions between the supplied
        transaction id and the current transaction counter.
       

      

      
       

        
         mxid_age
        
        mxid_age  ( xid )
        integer
       

       

        Returns the number of multixacts IDs between the supplied
        multixact ID and the current multixacts counter.
       

      

      
       

        
         pg_current_xact_id
        
        pg_current_xact_id ()
        xid8
       

       

        Returns the current transaction's ID.  It will assign a new one if the
        current transaction does not have one already (because it has not
        performed any database updates);  see  for details.  If executed in a
        subtransaction, this will return the top-level transaction ID;
        see  for details.
       

      

      
       

        
         pg_current_xact_id_if_assigned
        
        pg_current_xact_id_if_assigned ()
        xid8
       

       

        Returns the current transaction's ID, or NULL if no
        ID is assigned yet.  (It's best to use this variant if the transaction
        might otherwise be read-only, to avoid unnecessary consumption of an
        XID.)
        If executed in a subtransaction, this will return the top-level
        transaction ID.
       

      

      
       

        
         pg_xact_status
        
        pg_xact_status ( xid8 )
        text
       

       

        Reports the commit status of a recent transaction.
        The result is one of in progress,
        committed, or aborted,
        provided that the transaction is recent enough that the system retains
        the commit status of that transaction.
        If it is old enough that no references to the transaction survive in
        the system and the commit status information has been discarded, the
        result is NULL.
        Applications might use this function, for example, to determine
        whether their transaction committed or aborted after the application
        and database server become disconnected while
        a COMMIT is in progress.
        Note that prepared transactions are reported as in
        progress; applications must check pg_prepared_xacts
        if they need to determine whether a transaction ID belongs to a
        prepared transaction.
       

      

      
       

        
         pg_current_snapshot
        
        pg_current_snapshot ()
        pg_snapshot
       

       

        Returns a current snapshot, a data structure
        showing which transaction IDs are now in-progress.
        Only top-level transaction IDs are included in the snapshot;
        subtransaction IDs are not shown;  see 
        for details.
       

      

      
       

        
         pg_snapshot_xip
        
        pg_snapshot_xip ( pg_snapshot )
        setof xid8
       

       

        Returns the set of in-progress transaction IDs contained in a snapshot.
       

      

      
       

        
         pg_snapshot_xmax
        
        pg_snapshot_xmax ( pg_snapshot )
        xid8
       

       

        Returns the xmax of a snapshot.
       

      

      
       

        
         pg_snapshot_xmin
        
        pg_snapshot_xmin ( pg_snapshot )
        xid8
       

       

        Returns the xmin of a snapshot.
       

      

      
       

        
         pg_visible_in_snapshot
        
        pg_visible_in_snapshot ( xid8, pg_snapshot )
        boolean
       

       

        Is the given transaction ID visible according
        to this snapshot (that is, was it completed before the snapshot was
        taken)?  Note that this function will not give the correct answer for
        a subtransaction ID (subxid);  see  for
        details.
       

      

      
       

        
         pg_get_multixact_members
        
        pg_get_multixact_members ( 
multixid
 xid )
        setof record
        ( 
xid
 xid,
        
mode
 text )
       

       

        Returns the transaction ID and lock mode for each member of the
        specified multixact ID.  The lock modes forupd,
        fornokeyupd, sh, and
        keysh correspond to the row-level locks
        FOR UPDATE, FOR NO KEY UPDATE,
        FOR SHARE, and FOR KEY SHARE,
        respectively, as described in .  Two
        additional modes are specific to multixacts:
        nokeyupd, used by updates that do not modify key
        columns, and upd, used by updates or deletes that
        modify key columns.
       

      

      
       

        
         pg_get_multixact_stats
        
        pg_get_multixact_stats ()
        record
        ( 
num_mxids
 integer,
          
num_members
 bigint,
          
members_size
 bigint,
          
oldest_multixact
 xid )
       

       

        Returns statistics about current multixact usage:
        num_mxids is the total number of multixact IDs
        currently present in the system, num_members is
        the total number of multixact member entries currently present in
        the system, members_size is the storage occupied
        by num_members in the
        pg_multixact/members directory,
        oldest_multixact is the oldest multixact ID still
        in use.
       

       

        The function reports statistics at the time it is invoked. Values may
        vary between calls, even within a single transaction.
       

       

        By default, all columns are shown as NULL unless
        the user has privileges of the pg_read_all_stats
        role.
       

      
     
    
   

   

    The internal transaction ID type xid is 32 bits wide and
    wraps around every 4 billion transactions.  However,
    the functions shown in , except
    age, mxid_age,
    pg_get_multixact_members, and
    pg_get_multixact_stats, use a
    64-bit type xid8 that does not wrap around during the life
    of an installation and can be converted to xid by casting if
    required;  see  for details.
    The data type pg_snapshot stores information about
    transaction ID visibility at a particular moment in time.  Its components
    are described in .
    pg_snapshot's textual representation is
    xmin:xmax:xip_list.
    For example 10:20:10,14,15 means
    xmin=10, xmax=20, xip_list=10, 14, 15.
   

   
    
# Snapshot Components

    
    
    
     
      
       Name
       Description
      
     

     
      
       xmin
       
         Lowest transaction ID that was still active.  All transaction IDs
         less than xmin are either committed and visible,
         or rolled back and dead.
       
      

      
       xmax
       
         One past the highest completed transaction ID.  All transaction IDs
         greater than or equal to xmax had not yet
         completed as of the time of the snapshot, and thus are invisible.
       
      

      
       xip_list
       
        Transactions in progress at the time of the snapshot.  A transaction
        ID that is xmin <= X <
        xmax and not in this list was already completed at the time
        of the snapshot, and thus is either visible or dead according to its
        commit status.  This list does not include the transaction IDs of
        subtransactions (subxids).
       
      
     
    
   

   

    In releases of PostgreSQL before 13 there was
    no xid8 type, so variants of these functions were provided
    that used bigint to represent a 64-bit XID, with a
    correspondingly distinct snapshot data type txid_snapshot.
    These older functions have txid in their names.  They
    are still supported for backward compatibility, but may be removed from a
    future release. See .
   

   
    
# Deprecated Transaction ID and Snapshot Information Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     

      
       

        
         txid_current
        
        txid_current ()
        bigint
       

       

        See pg_current_xact_id().
       

      

      
       

        
         txid_current_if_assigned
        
        txid_current_if_assigned ()
        bigint
       

       

        See pg_current_xact_id_if_assigned().
       

      

      
       

        
         txid_current_snapshot
        
        txid_current_snapshot ()
        txid_snapshot
       

       

        See pg_current_snapshot().
       

      

      
       

        
         txid_snapshot_xip
        
        txid_snapshot_xip ( txid_snapshot )
        setof bigint
       

       

        See pg_snapshot_xip().
       

      

      
       

        
         txid_snapshot_xmax
        
        txid_snapshot_xmax ( txid_snapshot )
        bigint
       

       

        See pg_snapshot_xmax().
       

      

      
       

        
         txid_snapshot_xmin
        
        txid_snapshot_xmin ( txid_snapshot )
        bigint
       

       

        See pg_snapshot_xmin().
       

      

      
       

        
         txid_visible_in_snapshot
        
        txid_visible_in_snapshot ( bigint, txid_snapshot )
        boolean
       

       

        See pg_visible_in_snapshot().
       

      

      
       

        
         txid_status
        
        txid_status ( bigint )
        text
       

       

        See pg_xact_status().
       

      
     
    
   

  

  

   
# Committed Transaction Information Functions

   

    The functions shown in 
    provide information about when past transactions were committed.
    They only provide useful data when the
     configuration option is
    enabled, and only for transactions that were committed after it was
    enabled.  Commit timestamp information is routinely removed during
    vacuum.
   

   
    
# Committed Transaction Information Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         pg_xact_commit_timestamp
        
        pg_xact_commit_timestamp ( xid )
        timestamp with time zone
       

       

        Returns the commit timestamp of a transaction.
       

      

      
       

        
         pg_xact_commit_timestamp_origin
        
        pg_xact_commit_timestamp_origin ( xid )
        record
        ( 
timestamp
 timestamp with time zone,
         
roident
 oid)
       

       

         Returns the commit timestamp and replication origin of a transaction.
       

      

      
       

        
         pg_last_committed_xact
        
        pg_last_committed_xact ()
        record
        ( 
xid
 xid,
        
timestamp
 timestamp with time zone,
        
roident
 oid )
       

       

        Returns the transaction ID, commit timestamp and replication origin
        of the latest committed transaction.
       

      
     
    
   

  

  

   
# Control Data Functions

   

    The functions shown in 
    print information initialized during initdb, such
    as the catalog version. They also show information about write-ahead
    logging and checkpoint processing. This information is cluster-wide,
    not specific to any one database. These functions provide most of the same
    information, from the same source, as the
     application.
   

   
    
# Control Data Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         pg_control_checkpoint
        
        pg_control_checkpoint ()
        record
       

       

        Returns information about current checkpoint state, as shown in
        .
       

      

      
       

        
         pg_control_system
        
        pg_control_system ()
        record
       

       

        Returns information about current control file state, as shown in
        .
       

      

      
       

        
         pg_control_init
        
        pg_control_init ()
        record
       

       

        Returns information about cluster initialization state, as shown in
        .
       

      

      
       

        
         pg_control_recovery
        
        pg_control_recovery ()
        record
       

       

        Returns information about recovery state, as shown in
        .
       

      
     
    
   

   
    
# pg_control_checkpoint Output Columns

    
     
      
       Column Name
       Data Type
      
     

     

      
       checkpoint_lsn
       pg_lsn
      

      
       redo_lsn
       pg_lsn
      

      
       redo_wal_file
       text
      

      
       timeline_id
       integer
      

      
       prev_timeline_id
       integer
      

      
       full_page_writes
       boolean
      

      
       logical_decoding
       boolean
      

      
       next_xid
       text
      

      
       next_oid
       oid
      

      
       next_multixact_id
       xid
      

      
       next_multi_offset
       xid
      

      
       oldest_xid
       xid
      

      
       oldest_xid_dbid
       oid
      

      
       oldest_active_xid
       xid
      

      
       oldest_multi_xid
       xid
      

      
       oldest_multi_dbid
       oid
      

      
       oldest_commit_ts_xid
       xid
      

      
       newest_commit_ts_xid
       xid
      

      
       data_page_checksum_version
       integer
      

      
       checkpoint_time
       timestamp with time zone
      

     
    
   

   
    
# pg_control_system Output Columns

    
     
      
       Column Name
       Data Type
      
     

     

      
       pg_control_version
       integer
      

      
       catalog_version_no
       integer
      

      
       system_identifier
       bigint
      

      
       pg_control_last_modified
       timestamp with time zone
      

     
    
   

   
    
# pg_control_init Output Columns

    
     
      
       Column Name
       Data Type
      
     

     

      
       max_data_alignment
       integer
      

      
       database_block_size
       integer
      

      
       blocks_per_segment
       integer
      

      
       wal_block_size
       integer
      

      
       bytes_per_wal_segment
       integer
      

      
       max_identifier_length
       integer
      

      
       max_index_columns
       integer
      

      
       max_toast_chunk_size
       integer
      

      
       large_object_chunk_size
       integer
      

      
       float8_pass_by_value
       boolean
      

      
       data_page_checksum_version
       integer
      

      
       default_char_signedness
       boolean
      

     
    
   

   
    
# pg_control_recovery Output Columns

    
     
      
       Column Name
       Data Type
      
     

     

      
       min_recovery_end_lsn
       pg_lsn
      

      
       min_recovery_end_timeline
       integer
      

      
       backup_start_lsn
       pg_lsn
      

      
       backup_end_lsn
       pg_lsn
      

      
       end_of_backup_record_required
       boolean
      

     
    
   

  

  

   
# Version Information Functions

   

    The functions shown in 
    print version information.
   

   
    
# Version Information Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         version
        
        version ()
        text
       

       

        Returns a string describing the PostgreSQL
        server's version.  You can also get this information from
        , or for a machine-readable
        version use .  Software
        developers should use server_version_num (available
        since 8.2) or  instead of
        parsing the text version.
       

      

      
       

        
         unicode_version
        
        unicode_version ()
        text
       

       

        Returns a string representing the version of Unicode used by
        PostgreSQL.
       

      
      
       

        
         icu_unicode_version
        
        icu_unicode_version ()
        text
       

       

        Returns a string representing the version of Unicode used by ICU, if
        the server was built with ICU support; otherwise returns
        NULL 

      
     
    
   

  

  

   
# WAL Summarization Information Functions

   

    The functions shown in 
    print information about the status of WAL summarization.
    See .
   

   
    
# WAL Summarization Information Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         pg_available_wal_summaries
        
        pg_available_wal_summaries ()
        setof record
        ( 
tli
 bigint,
        
start_lsn
 pg_lsn,
        
end_lsn
 pg_lsn )
       

       

        Returns information about the WAL summary files present in the
        data directory, under pg_wal/summaries.
        One row will be returned per WAL summary file. Each file summarizes
        WAL on the indicated TLI within the indicated LSN range. This function
        might be useful to determine whether enough WAL summaries are present
        on the server to take an incremental backup based on some prior
        backup whose start LSN is known.
       

      

      
       

        
         pg_wal_summary_contents
        
        pg_wal_summary_contents ( 
tli
 bigint, 
start_lsn
 pg_lsn, 
end_lsn
 pg_lsn )
        setof record
        ( 
relfilenode
 oid,
        
reltablespace
 oid,
        
reldatabase
 oid,
        
relforknumber
 smallint,
        
relblocknumber
 bigint,
        
is_limit_block
 boolean )
       

       

        Returns one information about the contents of a single WAL summary file
        identified by TLI and starting and ending LSNs. Each row with
        is_limit_block false indicates that the block
        identified by the remaining output columns was modified by at least
        one WAL record within the range of records summarized by this file.
        Each row with is_limit_block true indicates either
        that (a) the relation fork was truncated to the length given by
        relblocknumber within the relevant range of WAL
        records or (b) that the relation fork was created or dropped within
        the relevant range of WAL records; in such cases,
        relblocknumber will be zero.
       

      

      
       

        
         pg_get_wal_summarizer_state
        
        pg_get_wal_summarizer_state ()
        record
        ( 
summarized_tli
 bigint,
        
summarized_lsn
 pg_lsn,
        
pending_lsn
 pg_lsn,
        
summarizer_pid
 int )
       

       

        Returns information about the progress of the WAL summarizer. If the
        WAL summarizer has never run since the instance was started, then
        summarized_tli and summarized_lsn
        will be 0 and 0/00000000 respectively;
        otherwise, they will be the TLI and ending LSN of the last WAL summary
        file written to disk. If the WAL summarizer is currently running,
        pending_lsn will be the ending LSN of the last
        record that it has consumed, which must always be greater than or
        equal to summarized_lsn; if the WAL summarizer is
        not running, it will be equal to summarized_lsn.
        summarizer_pid is the PID of the WAL summarizer
        process, if it is running, and otherwise NULL.
       

       

        As a special exception, the WAL summarizer will refuse to generate
        WAL summary files if run on WAL generated under
        wal_level=minimal, since such summaries would be
        unsafe to use as the basis for an incremental backup. In this case,
        the fields above will continue to advance as if summaries were being
        generated, but nothing will be written to disk. Once the summarizer
        reaches WAL generated while wal_level was set
        to replica or higher, it will resume writing
        summaries to disk.
       

      
     
    
   

  

  

   
# Get Object DDL Functions

   

    The functions shown in 
    reconstruct DDL statements for various global database objects.
    Each function returns a set of text rows, one SQL statement per row.
    (This is a decompiled reconstruction, not the original text of the
    command.)
   

   
    
# Get Object DDL Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         pg_get_database_ddl
        
        pg_get_database_ddl
        ( 
database
 regdatabase
        , 
pretty
 boolean
        DEFAULT false
        , 
owner
 boolean
        DEFAULT true
        , 
tablespace
 boolean
        DEFAULT true )
        setof text
       

       

        Reconstructs the CREATE
        DATABASE statement for the specified database,
        followed by ALTER DATABASE
         statements for connection limit, template status,
        and configuration settings.  Each statement is returned as a separate
        row. When 
pretty
 is true, the output is
        pretty-printed.  When 
owner
 is false, the
        OWNER clause is omitted.  When
        
tablespace
 is false, the
        TABLESPACE clause is omitted.
       

      
      
       

        
         pg_get_role_ddl
        
        pg_get_role_ddl
        ( 
role
 regrole
        , 
pretty
 boolean
        DEFAULT false
        , 
memberships
 boolean
        DEFAULT true )
        setof text
       

       

        Reconstructs the CREATE ROLE
         statement and any 
        ALTER ROLE ... SET statements for the given
        role.  Each statement is returned as a separate row.
        Password information is never included in the output.
        When 
pretty
 is true, the output is
        pretty-printed.  When 
memberships
 is false,
        GRANT statements
        for role memberships are omitted.
       

      
      
       

        
         pg_get_tablespace_ddl
        
        pg_get_tablespace_ddl
        ( 
tablespace
 oid
        , 
pretty
 boolean
        DEFAULT false
        , 
owner
 boolean
        DEFAULT true )
        setof text
       

       

        pg_get_tablespace_ddl
        ( 
tablespace
 name
        , 
pretty
 boolean
        DEFAULT false
        , 
owner
 boolean
        DEFAULT true )
        setof text
       

       

        Reconstructs the CREATE
        TABLESPACE statement for the specified tablespace (by
        OID or name).  If the tablespace has options set, an
        ALTER TABLESPACE ... SET
         statement is also returned.  Each statement is
        returned as a separate row.  When 
pretty
 is
        true, the output is pretty-printed.  When 
owner

        is false, the OWNER clause is omitted.
