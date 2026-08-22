# Extending SQL

  
   extending SQL
  

  

   In  the  sections  that follow, we will discuss how you
   can extend the PostgreSQL
   SQL query language by adding:

   

    

     

      functions (starting in )
     

    

    

     

      aggregates (starting in )
     

    

    

     

      data types (starting in )
     

    

    

     

      operators (starting in )
     

    

    

     

      operator classes for indexes (starting in )
     

    

    

     

      packages of related objects (starting in )
     

    

   

  

  

   
# How Extensibility Works

   

    PostgreSQL is extensible because its operation  is
    catalog-driven.   If  you  are familiar with standard
    relational database systems, you know that  they  store  information
    about  databases,  tables,  columns,  etc., in what are
    commonly known as system catalogs.  (Some systems  call
    this  the data dictionary.)  The catalogs appear to the
    user as tables like any other, but  the  DBMS  stores
    its  internal  bookkeeping in them.  One key difference
    between PostgreSQL and  standard  relational database systems  is
    that PostgreSQL stores much more information in its
    catalogs: not only information about tables and  columns,
    but also information about data types, functions, access
    methods, and so on.  These tables can be  modified  by
    the  user, and since PostgreSQL bases its operation
    on these tables, this means that PostgreSQL can  be
    extended   by   users.    By  comparison,  conventional
    database systems can only be extended by changing hardcoded
    procedures in the source code or by loading modules
    specially written by the DBMS vendor.
   

   

    The PostgreSQL server can moreover
    incorporate user-written code into itself through dynamic loading.
    That is, the user can specify an object code file (e.g., a shared
    library) that implements a new type or function, and
    PostgreSQL will load it as required.
    Code written in SQL is even more trivial to add
    to the server.  This ability to modify its operation on the
    fly makes PostgreSQL uniquely
    suited for rapid prototyping of new applications and storage
    structures.
   

  

  

   
# The PostgreSQL Type System

   
    base type
   

   
    data type
    base
   

   
    composite type
   

   
    data type
    composite
   

   
    container type
   

   
    data type
    container
   

   

    PostgreSQL data types can be divided into base
    types, container types, domains, and pseudo-types.
   

   

    
# Base Types

    

     Base types are those, like integer, that are
     implemented below the level of the SQL language
     (typically in a low-level language such as C).  They generally
     correspond to what are often known as abstract data types.
     PostgreSQL can only operate on such
     types through functions provided by the user and only understands
     the behavior of such types to the extent that the user describes
     them.
     The built-in base types are described in .
    

    

     Enumerated (enum) types can be considered as a subcategory of base
     types.  The main difference is that they can be created using
     just SQL commands, without any low-level programming.
     Refer to  for more information.
    

   

   

    
# Container Types

    

     PostgreSQL has three kinds
     of container types, which are types that contain multiple
     values of other types.  These are arrays, composites, and ranges.
    

    

     Arrays can hold multiple values that are all of the same type.  An array
     type is automatically created for each base type, composite type, range
     type, and domain type.  But there are no arrays of arrays.  So far as
     the type system is concerned, multi-dimensional arrays are the same as
     one-dimensional arrays.  Refer to  for more
     information.
    

    

     Composite types, or row types, are created whenever the user
     creates a table. It is also possible to use  to
     define a stand-alone composite type with no associated
     table.  A composite type is simply a list of types with
     associated field names.  A value of a composite type is a row or
     record of field values.  Refer to 
     for more information.
    

    

     A range type can hold two values of the same type, which are the lower
     and upper bounds of the range.  Range types are user-created, although
     a few built-in ones exist.  Refer to 
     for more information.
    

   

   

    
# Domains

    

     A domain is based on a particular underlying type and for many purposes
     is interchangeable with its underlying type.  However, a domain can have
     constraints that restrict its valid values to a subset of what the
     underlying type would allow.  Domains are created using
     the SQL command .
     Refer to  for more information.
    

   

   

    
# Pseudo-Types

    

     There are a few pseudo-types for special purposes.
     Pseudo-types cannot appear as columns of tables or components of
     container types, but they can be used to declare the argument and
     result types of functions.  This provides a mechanism within the
     type system to identify special classes of functions.   lists the existing
     pseudo-types.
    

   

   

    
# Polymorphic Types

   
    polymorphic type
   

   
    polymorphic function
   

   
    data type
    polymorphic
   

   
    function
    polymorphic
   

    

     Some pseudo-types of special interest are the polymorphic
     types, which are used to declare polymorphic
     functions.  This powerful feature allows a single function
     definition to operate on many different data types, with the specific
     data type(s) being determined by the data types actually passed to it
     in a particular call.  The polymorphic types are shown in
     .  Some examples of
     their use appear in .
    

    
     
# Polymorphic Types

     
      
      
      
      
       
        Name
        Family
        Description
       
      

      
       
        anyelement
        Simple
        Indicates that a function accepts any data type
       

       
        anyarray
        Simple
        Indicates that a function accepts any array data type
       

       
        anynonarray
        Simple
        Indicates that a function accepts any non-array data type
       

       
        anyenum
        Simple
        Indicates that a function accepts any enum data type
        (see )
        
       

       
        anyrange
        Simple
        Indicates that a function accepts any range data type
        (see )
        
       

       
        anymultirange
        Simple
        Indicates that a function accepts any multirange data type
        (see )
        
       

       
        anycompatible
        Common
        Indicates that a function accepts any data type,
        with automatic promotion of multiple arguments to a common data type
        
       

       
        anycompatiblearray
        Common
        Indicates that a function accepts any array data type,
        with automatic promotion of multiple arguments to a common data type
        
       

       
        anycompatiblenonarray
        Common
        Indicates that a function accepts any non-array data type,
        with automatic promotion of multiple arguments to a common data type
        
       

       
        anycompatiblerange
        Common
        Indicates that a function accepts any range data type,
        with automatic promotion of multiple arguments to a common data type
        
       

       
        anycompatiblemultirange
        Common
        Indicates that a function accepts any multirange data type,
        with automatic promotion of multiple arguments to a common data type
        
       
      
     
    

    

     Polymorphic arguments and results are tied to each other and are resolved
     to specific data types when a query calling a polymorphic function is
     parsed.  When there is more than one polymorphic argument, the actual
     data types of the input values must match up as described below.  If the
     function's result type is polymorphic, or it has output parameters of
     polymorphic types, the types of those results are deduced from the
     actual types of the polymorphic inputs as described below.
    

    

     For the simple family of polymorphic types, the
     matching and deduction rules work like this:
    

    

     Each position (either argument or return value) declared as
     anyelement is allowed to have any specific actual
     data type, but in any given call they must all be the
     same actual type. Each
     position declared as anyarray can have any array data type,
     but similarly they must all be the same type.  And similarly,
     positions declared as anyrange must all be the same range
     type.  Likewise for anymultirange.
    

    

     Furthermore, if there are
     positions declared anyarray and others declared
     anyelement, the actual array type in the
     anyarray positions must be an array whose elements are
     the same type appearing in the anyelement positions.
     anynonarray is treated exactly the same as anyelement,
     but adds the additional constraint that the actual type must not be
     an array type.
     anyenum is treated exactly the same as anyelement,
     but adds the additional constraint that the actual type must
     be an enum type.
    

    

     Similarly, if there are positions declared anyrange
     and others declared anyelement or anyarray,
     the actual range type in the anyrange positions must be a
     range whose subtype is the same type appearing in
     the anyelement positions and the same as the element type
     of the anyarray positions.
     If there are positions declared anymultirange,
     their actual multirange type must contain ranges matching parameters declared
     anyrange and base elements matching parameters declared
     anyelement and anyarray.
    

    

     Thus, when more than one argument position is declared with a polymorphic
     type, the net effect is that only certain combinations of actual argument
     types are allowed.  For example, a function declared as
     equal(anyelement, anyelement) will take any two input values,
     so long as they are of the same data type.
    

    

     When the return value of a function is declared as a polymorphic type,
     there must be at least one argument position that is also polymorphic,
     and the actual data type(s) supplied for the polymorphic arguments
     determine the actual
     result type for that call.  For example, if there were not already
     an array subscripting mechanism, one could define a function that
     implements subscripting as subscript(anyarray, integer)
     returns anyelement.  This declaration constrains the actual first
     argument to be an array type, and allows the parser to infer the correct
     result type from the actual first argument's type.  Another example
     is that a function declared as f(anyarray) returns anyenum
     will only accept arrays of enum types.
    

    

     In most cases, the parser can infer the actual data type for a
     polymorphic result type from arguments that are of a different
     polymorphic type in the same family; for example anyarray
     can be deduced from anyelement or vice versa.
     An exception is that a
     polymorphic result of type anyrange requires an argument
     of type anyrange; it cannot be deduced
     from anyarray or anyelement arguments.  This
     is because there could be multiple range types with the same subtype.
    

    

     Note that anynonarray and anyenum do not represent
     separate type variables; they are the same type as
     anyelement, just with an additional constraint.  For
     example, declaring a function as f(anyelement, anyenum)
     is equivalent to declaring it as f(anyenum, anyenum):
     both actual arguments have to be the same enum type.
    

    

     For the common family of polymorphic types, the
     matching and deduction rules work approximately the same as for
     the simple family, with one major difference: the
     actual types of the arguments need not be identical, so long as they
     can be implicitly cast to a single common type.  The common type is
     selected following the same rules as for UNION and
     related constructs (see ).
     Selection of the common type considers the actual types
     of anycompatible and anycompatiblenonarray
     inputs, the array element types of anycompatiblearray
     inputs, the range subtypes of anycompatiblerange inputs,
     and the multirange subtypes of anycompatiblemultirange
     inputs.  If anycompatiblenonarray is present then the
     common type is required to be a non-array type.  Once a common type is
     identified, arguments in anycompatible
     and anycompatiblenonarray positions are automatically
     cast to that type, and arguments in anycompatiblearray
     positions are automatically cast to the array type for that type.
    

    

     Since there is no way to select a range type knowing only its subtype,
     use of anycompatiblerange and/or
     anycompatiblemultirange requires that all arguments declared
     with that type have the same actual range and/or multirange type, and that
     that type's subtype agree with the selected common type, so that no casting
     of the range values is required.  As with anyrange and
     anymultirange, use of anycompatiblerange and
     anymultirange as a function result type requires that there be
     an anycompatiblerange or anycompatiblemultirange
     argument.
    

    

     Notice that there is no anycompatibleenum type.  Such a
     type would not be very useful, since there normally are not any
     implicit casts to enum types, meaning that there would be no way to
     resolve a common type for dissimilar enum inputs.
    

    

     The simple and common polymorphic
     families represent two independent sets of type variables.  Consider
     for example

```
CREATE FUNCTION myfunc(a anyelement, b anyelement,
                       c anycompatible, d anycompatible)
RETURNS anycompatible AS ...
```

     In an actual call of this function, the first two inputs must have
     exactly the same type.  The last two inputs must be promotable to a
     common type, but this type need not have anything to do with the type
     of the first two inputs.  The result will have the common type of the
     last two inputs.
    

    

     A variadic function (one taking a variable number of arguments, as in
     ) can be
     polymorphic: this is accomplished by declaring its last parameter as
     VARIADIC anyarray or
     VARIADIC anycompatiblearray.
     For purposes of argument
     matching and determining the actual result type, such a function behaves
     the same as if you had written the appropriate number of
     anynonarray or anycompatiblenonarray
     parameters.
    

   

  

  &xfunc;
  &xaggr;
  &xtypes;
  &xoper;
  &xindex;

  

   
# Packaging Related Objects into an Extension

   
    extension
   

   

    A useful extension to PostgreSQL typically includes
    multiple SQL objects; for example, a new data type will require new
    functions, new operators, and probably new index operator classes.
    It is helpful to collect all these objects into a single package
    to simplify database management.  PostgreSQL calls
    such a package an extension.  To define an extension,
    you need at least a script file that contains the
    SQL commands to create the extension's objects, and a
    control file that specifies a few basic properties
    of the extension itself.  If the extension includes C code, there
    will typically also be a shared library file into which the C code
    has been built.  Once you have these files, a simple
    CREATE EXTENSION command loads the objects into
    your database.
   

   

    The main advantage of using an extension, rather than just running the
    SQL script to load a bunch of loose objects
    into your database, is that PostgreSQL will then
    understand that the objects of the extension go together.  You can
    drop all the objects with a single DROP EXTENSION
    command (no need to maintain a separate uninstall script).
    Even more useful, pg_dump knows that it should not
    dump the individual member objects of the extension &mdash; it will
    just include a CREATE EXTENSION command in dumps, instead.
    This vastly simplifies migration to a new version of the extension
    that might contain more or different objects than the old version.
    Note however that you must have the extension's control, script, and
    other files available when loading such a dump into a new database.
   

   

    PostgreSQL will not let you drop an individual object
    contained in an extension, except by dropping the whole extension.
    Also, while you can change the definition of an extension member object
    (for example, via CREATE OR REPLACE FUNCTION for a
    function), bear in mind that the modified definition will not be dumped
    by pg_dump.  Such a change is usually only sensible if
    you concurrently make the same change in the extension's script file.
    (But there are special provisions for tables containing configuration
    data; see .)
    In production situations, it's generally better to create an extension
    update script to perform changes to extension member objects.
   

   

    The extension script may set privileges on objects that are part of the
    extension, using GRANT and REVOKE
    statements.  The final set of privileges for each object (if any are set)
    will be stored in the
    pg_init_privs
    system catalog.  When pg_dump is used, the
    CREATE EXTENSION command will be included in the dump, followed
    by the set of GRANT and REVOKE
    statements necessary to set the privileges on the objects to what they were
    at the time the dump was taken.
   

   

    PostgreSQL does not currently support extension scripts
    issuing CREATE POLICY or SECURITY LABEL
    statements.  These are expected to be set after the extension has been
    created.  All RLS policies and security labels on extension objects will be
    included in dumps created by pg_dump.
   

   

    The extension mechanism also has provisions for packaging modification
    scripts that adjust the definitions of the SQL objects contained in an
    extension.  For example, if version 1.1 of an extension adds one function
    and changes the body of another function compared to 1.0, the extension
    author can provide an update script that makes just those
    two changes.  The ALTER EXTENSION UPDATE command can then
    be used to apply these changes and track which version of the extension
    is actually installed in a given database.
   

   

    The kinds of SQL objects that can be members of an extension are shown in
    the description of ALTER EXTENSION.  Notably, objects
    that are database-cluster-wide, such as databases, roles, and tablespaces,
    cannot be extension members since an extension is only known within one
    database.  (Although an extension script is not prohibited from creating
    such objects, if it does so they will not be tracked as part of the
    extension.)  Also notice that while a table can be a member of an
    extension, its subsidiary objects such as indexes are not directly
    considered members of the extension.
    Another important point is that schemas can belong to extensions, but not
    vice versa: an extension as such has an unqualified name and does not
    exist within any schema.  The extension's member objects,
    however, will belong to schemas whenever appropriate for their object
    types.  It may or may not be appropriate for an extension to own the
    schema(s) its member objects are within.
   

   

    If an extension's script creates any temporary objects (such as temp
    tables), those objects are treated as extension members for the
    remainder of the current session, but are automatically dropped at
    session end, as any temporary object would be.  This is an exception
    to the rule that extension member objects cannot be dropped without
    dropping the whole extension.
   

   

    
# Extension Files

   
    control file
   

    

     The CREATE EXTENSION command relies on a control
     file for each extension, which must be named the same as the extension
     with a suffix of .control, and must be placed in the
     installation's SHAREDIR/extension directory.  There
     must also be at least one SQL script file, which follows the
     naming pattern
     extension--version.sql
     (for example, foo--1.0.sql for version 1.0 of
     extension foo).  By default, the script file(s) are also
     placed in the SHAREDIR/extension directory; but the
     control file can specify a different directory for the script file(s).
    

    

     Additional locations for extension control files can be configured using
     the parameter .
    

    

     The file format for an extension control file is the same as for the
     postgresql.conf file, namely a list of
     parameter_name = value
     assignments, one per line.  Blank lines and comments introduced by
     # are allowed.  Be sure to quote any value that is not
     a single word or number.
    

    

     A control file can set the following parameters:
    

    

     

      
directory (string)

      

       

        The directory containing the extension's SQL script
        file(s).  Unless an absolute path is given, the name is relative to
        the directory where the control file was found.  By default,
        the script files are looked for in the same directory where the
        control file was found.
       

      

     

     

      
default_version (string)

      

       

        The default version of the extension (the one that will be installed
        if no version is specified in CREATE EXTENSION).  Although
        this can be omitted, that will result in CREATE EXTENSION
        failing if no VERSION option appears, so you generally
        don't want to do that.
       

      

     

     

      
comment (string)

      

       

        A comment (any string) about the extension.  The comment is applied
        when initially creating an extension, but not during extension updates
        (since that might override user-added comments).  Alternatively,
        the extension's comment can be set by writing
        a  command in the script file.
       

      

     

     

      
encoding (string)

      

       

        The character set encoding used by the script file(s).  This should
        be specified if the script files contain any non-ASCII characters.
        Otherwise the files will be assumed to be in the database encoding.
       

      

     

     

      
module_pathname (string)

      

       

        The value of this parameter will be substituted for each occurrence
        of MODULE_PATHNAME in the script file(s).  If it is not
        set, no substitution is made.  Typically, this is set to just
        shared_library_name and
        then MODULE_PATHNAME is used in CREATE
        FUNCTION commands for C-language functions, so that the script
        files do not need to hard-wire the name of the shared library.
       

      

     

     

      
requires (string)

      

       

        A list of names of extensions that this extension depends on,
        for example requires = 'foo, bar'.  Those
        extensions must be installed before this one can be installed.
       

      

     

     

      
no_relocate (string)

      

       

        A list of names of extensions that this extension depends on that
        should be barred from changing their schemas via ALTER
        EXTENSION ... SET SCHEMA.
        This is needed if this extension's script references the name
        of a required extension's schema (using
        the @extschema:name@
        syntax) in a way that cannot track renames.
       

      

     

     

      
superuser (boolean)

      

       

        If this parameter is true (which is the default),
        only superusers can create the extension or update it to a new
        version (but see also trusted, below).
        If it is set to false, just the privileges
        required to execute the commands in the installation or update script
        are required.
        This should normally be set to true if any of the
        script commands require superuser privileges.  (Such commands would
        fail anyway, but it's more user-friendly to give the error up front.)
       

      

     

     

      
trusted (boolean)

      

       

        This parameter, if set to true (which is not the
        default), allows some non-superusers to install an extension that
        has superuser set to true.
        Specifically, installation will be permitted for anyone who has
        CREATE privilege on the current database.
        When the user executing CREATE EXTENSION is not
        a superuser but is allowed to install by virtue of this parameter,
        then the installation or update script is run as the bootstrap
        superuser, not as the calling user.
        This parameter is irrelevant if superuser is
        false.
        Generally, this should not be set true for extensions that could
        allow access to otherwise-superuser-only abilities, such as
        file system access.
        Also, marking an extension trusted requires significant extra effort
        to write the extension's installation and update script(s) securely;
        see .
       

      

     

     

      
relocatable (boolean)

      

       

        An extension is relocatable if it is possible to move
        its contained objects into a different schema after initial creation
        of the extension.  The default is false, i.e., the
        extension is not relocatable.
        See  for more information.
       

      

     

     

      
schema (string)

      

       

        This parameter can only be set for non-relocatable extensions.
        It forces the extension to be loaded into exactly the named schema
        and not any other.
        The schema parameter is consulted only when
        initially creating an extension, not during extension updates.
        See  for more information.
       

      

     

    

    

     In addition to the primary control file
     extension.control,
     an extension can have secondary control files named in the style
     extension--version.control.
     If supplied, these must be located in the script file directory.
     Secondary control files follow the same format as the primary control
     file.  Any parameters set in a secondary control file override the
     primary control file when installing or updating to that version of
     the extension.  However, the parameters directory and
     default_version cannot be set in a secondary control file.
    

    

     An extension's SQL script files can contain any SQL commands,
     except for transaction control commands (BEGIN,
     COMMIT, etc.) and commands that cannot be executed inside a
     transaction block (such as VACUUM).  This is because the
     script files are implicitly executed within a transaction block.
    

    

     An extension's SQL script files can also contain lines
     beginning with \echo, which will be ignored (treated as
     comments) by the extension mechanism.  This provision is commonly used
     to throw an error if the script file is fed to psql
     rather than being loaded via CREATE EXTENSION (see example
     script in ).
     Without that, users might accidentally load the
     extension's contents as loose objects rather than as an
     extension, a state of affairs that's a bit tedious to recover from.
    

    

     If the extension script contains the
     string @extowner@, that string is replaced with the
     (suitably quoted) name of the user calling CREATE
     EXTENSION or ALTER EXTENSION.  Typically
     this feature is used by extensions that are marked trusted to assign
     ownership of selected objects to the calling user rather than the
     bootstrap superuser.  (One should be careful about doing so, however.
     For example, assigning ownership of a C-language function to a
     non-superuser would create a privilege escalation path for that user.)
    

    

     While the script files can contain any characters allowed by the specified
     encoding, control files should contain only plain ASCII, because there
     is no way for PostgreSQL to know what encoding a
     control file is in.  In practice this is only an issue if you want to
     use non-ASCII characters in the extension's comment.  Recommended
     practice in that case is to not use the control file comment
     parameter, but instead use COMMENT ON EXTENSION
     within a script file to set the comment.
    

   

   

    
# Extension Relocatability

    

     Users often wish to load the objects contained in an extension into a
     different schema than the extension's author had in mind.  There are
     three supported levels of relocatability:
    

    

     

      

       A fully relocatable extension can be moved into another schema
       at any time, even after it's been loaded into a database.
       This is done with the ALTER EXTENSION SET SCHEMA
       command, which automatically renames all the member objects into
       the new schema.  Normally, this is only possible if the extension
       contains no internal assumptions about what schema any of its
       objects are in.  Also, the extension's objects must all be in one
       schema to begin with (ignoring objects that do not belong to any
       schema, such as procedural languages).  Mark a fully relocatable
       extension by setting relocatable = true in its control
       file.
      

     

     

      

       An extension might be relocatable during installation but not
       afterwards.  This is typically the case if the extension's script
       file needs to reference the target schema explicitly, for example
       in setting search_path properties for SQL functions.
       For such an extension, set relocatable = false in its
       control file, and use @extschema@ to refer to the target
       schema in the script file.  All occurrences of this string will be
       replaced by the actual target schema's name (double-quoted if
       necessary) before the script is executed.  The user can set the
       target schema using the
       SCHEMA option of CREATE EXTENSION.
      

     

     

      

       If the extension does not support relocation at all, set
       relocatable = false in its control file, and also set
       schema to the name of the intended target schema.  This
       will prevent use of the SCHEMA option of CREATE
       EXTENSION, unless it specifies the same schema named in the control
       file.  This choice is typically necessary if the extension contains
       internal assumptions about its schema name that can't be replaced by
       uses of @extschema@.  The @extschema@
       substitution mechanism is available in this case too, although it is
       of limited use since the schema name is determined by the control file.
      

     

    

    

     In all cases, the script file will be executed with
      initially set to point to the target
     schema; that is, CREATE EXTENSION does the equivalent of
     this:

```
SET LOCAL search_path TO @extschema@, pg_temp;
```

     This allows the objects created by the script file to go into the target
     schema.  The script file can change search_path if it wishes,
     but that is generally undesirable.  search_path is restored
     to its previous setting upon completion of CREATE EXTENSION.
    

    

     The target schema is determined by the schema parameter in
     the control file if that is given, otherwise by the SCHEMA
     option of CREATE EXTENSION if that is given, otherwise the
     current default object creation schema (the first one in the caller's
     search_path).  When the control file schema
     parameter is used, the target schema will be created if it doesn't
     already exist, but in the other two cases it must already exist.
    

    

     If any prerequisite extensions are listed in requires
     in the control file, their target schemas are added to the initial
     setting of search_path, following the new
     extension's target schema.  This allows their objects to be visible to
     the new extension's script file.
    

    

     For security, pg_temp is automatically appended to
     the end of search_path in all cases.
    

    

     Although a non-relocatable extension can contain objects spread across
     multiple schemas, it is usually desirable to place all the objects meant
     for external use into a single schema, which is considered the extension's
     target schema.  Such an arrangement works conveniently with the default
     setting of search_path during creation of dependent
     extensions.
    

    

     If an extension references objects belonging to another extension,
     it is recommended to schema-qualify those references.  To do that,
     write @extschema:name@
     in the extension's script file, where name
     is the name of the other extension (which must be listed in this
     extension's requires list).  This string will be
     replaced by the name (double-quoted if necessary) of that extension's
     target schema.
     Although this notation avoids the need to make hard-wired assumptions
     about schema names in the extension's script file, its use may embed
     the other extension's schema name into the installed objects of this
     extension.  (Typically, that happens
     when @extschema:name@ is
     used inside a string literal, such as a function body or
     a search_path setting.  In other cases, the object
     reference is reduced to an OID during parsing and does not require
     subsequent lookups.)  If the other extension's schema name is so
     embedded, you should prevent the other extension from being relocated
     after yours is installed, by adding the name of the other extension to
     this one's no_relocate list.
    

   

   

    
# Extension Configuration Tables

    

     Some extensions include configuration tables, which contain data that
     might be added or changed by the user after installation of the
     extension.  Ordinarily, if a table is part of an extension, neither
     the table's definition nor its content will be dumped by
     pg_dump.  But that behavior is undesirable for a
     configuration table; any data changes made by the user need to be
     included in dumps, or the extension will behave differently after a dump
     and restore.
    

   
    pg_extension_config_dump
   

    

     To solve this problem, an extension's script file can mark a table
     or a sequence it has created as a configuration relation, which will
     cause pg_dump to include the table's or the sequence's
     contents (not its definition) in dumps.  To do that, call the function
     pg_extension_config_dump(regclass, text) after creating the
     table or the sequence, for example

```
CREATE TABLE my_config (key text, value text);
CREATE SEQUENCE my_config_seq;

SELECT pg_catalog.pg_extension_config_dump('my_config', '');
SELECT pg_catalog.pg_extension_config_dump('my_config_seq', '');
```

     Any number of tables or sequences can be marked this way. Sequences
     associated with serial or bigserial columns can
     be marked as well.
    

    

     When the second argument of pg_extension_config_dump is
     an empty string, the entire contents of the table are dumped by
     pg_dump.  This is usually only correct if the table
     is initially empty as created by the extension script.  If there is
     a mixture of initial data and user-provided data in the table,
     the second argument of pg_extension_config_dump provides
     a WHERE condition that selects the data to be dumped.
     For example, you might do

```
CREATE TABLE my_config (key text, value text, standard_entry boolean);

SELECT pg_catalog.pg_extension_config_dump('my_config', 'WHERE NOT standard_entry');
```

     and then make sure that standard_entry is true only
     in the rows created by the extension's script.
    

    

     For sequences, the second argument of pg_extension_config_dump
     has no effect.
    

    

     More complicated situations, such as initially-provided rows that might
     be modified by users, can be handled by creating triggers on the
     configuration table to ensure that modified rows are marked correctly.
    

    

     You can alter the filter condition associated with a configuration table
     by calling pg_extension_config_dump again.  (This would
     typically be useful in an extension update script.)  The only way to mark
     a table as no longer a configuration table is to dissociate it from the
     extension with ALTER EXTENSION ... DROP TABLE.
    

    

     Note that foreign key relationships between these tables will dictate the
     order in which the tables are dumped out by pg_dump.  Specifically, pg_dump
     will attempt to dump the referenced-by table before the referencing table.
     As the foreign key relationships are set up at CREATE EXTENSION time (prior
     to data being loaded into the tables) circular dependencies are not
     supported.  When circular dependencies exist, the data will still be dumped
     out but the dump will not be able to be restored directly and user
     intervention will be required.
    

    

     Sequences associated with serial or bigserial columns
     need to be directly marked to dump their state. Marking their parent
     relation is not enough for this purpose.
    

   

   

    
# Extension Updates

    

     One advantage of the extension mechanism is that it provides convenient
     ways to manage updates to the SQL commands that define an extension's
     objects.  This is done by associating a version name or number with
     each released version of the extension's installation script.
     In addition, if you want users to be able to update their databases
     dynamically from one version to the next, you should provide
     update scripts that make the necessary changes to go from
     one version to the next.  Update scripts have names following the pattern
     extension--old_version--target_version.sql
     (for example, foo--1.0--1.1.sql contains the commands to modify
     version 1.0 of extension foo into version
     1.1).
    

    

     Given that a suitable update script is available, the command
     ALTER EXTENSION UPDATE will update an installed extension
     to the specified new version.  The update script is run in the same
     environment that CREATE EXTENSION provides for installation
     scripts: in particular, search_path is set up in the same
     way, and any new objects created by the script are automatically added
     to the extension.  Also, if the script chooses to drop extension member
     objects, they are automatically dissociated from the extension.
    

    

     If an extension has secondary control files, the control parameters
     that are used for an update script are those associated with the script's
     target (new) version.
    

    

     ALTER EXTENSION is able to execute sequences of update
     script files to achieve a requested update.  For example, if only
     foo--1.0--1.1.sql and foo--1.1--2.0.sql are
     available, ALTER EXTENSION will apply them in sequence if an
     update to version 2.0 is requested when 1.0 is
     currently installed.
    

    

     PostgreSQL doesn't assume anything about the properties
     of version names: for example, it does not know whether 1.1
     follows 1.0.  It just matches up the available version names
     and follows the path that requires applying the fewest update scripts.
     (A version name can actually be any string that doesn't contain
     -- or leading or trailing -.)
    

    

     Sometimes it is useful to provide downgrade scripts, for
     example foo--1.1--1.0.sql to allow reverting the changes
     associated with version 1.1.  If you do that, be careful
     of the possibility that a downgrade script might unexpectedly
     get applied because it yields a shorter path.  The risky case is where
     there is a fast path update script that jumps ahead several
     versions as well as a downgrade script to the fast path's start point.
     It might take fewer steps to apply the downgrade and then the fast
     path than to move ahead one version at a time.  If the downgrade script
     drops any irreplaceable objects, this will yield undesirable results.
    

    

     To check for unexpected update paths, use this command:

```
SELECT * FROM pg_extension_update_paths('extension_name');
```

     This shows each pair of distinct known version names for the specified
     extension, together with the update path sequence that would be taken to
     get from the source version to the target version, or NULL if
     there is no available update path.  The path is shown in textual form
     with -- separators.  You can use
     regexp_split_to_array(path,'--') if you prefer an array
     format.
    

   

   

    
# Installing Extensions Using Update Scripts

    

     An extension that has been around for awhile will probably exist in
     several versions, for which the author will need to write update scripts.
     For example, if you have released a foo extension in
     versions 1.0, 1.1, and 1.2, there
     should be update scripts foo--1.0--1.1.sql
     and foo--1.1--1.2.sql.
     Before PostgreSQL 10, it was necessary to also create
     new script files foo--1.1.sql and foo--1.2.sql
     that directly build the newer extension versions, or else the newer
     versions could not be installed directly, only by
     installing 1.0 and then updating.  That was tedious and
     duplicative, but now it's unnecessary, because CREATE
     EXTENSION can follow update chains automatically.
     For example, if only the script
     files foo--1.0.sql, foo--1.0--1.1.sql,
     and foo--1.1--1.2.sql are available then a request to
     install version 1.2 is honored by running those three
     scripts in sequence.  The processing is the same as if you'd first
     installed 1.0 and then updated to 1.2.
     (As with ALTER EXTENSION UPDATE, if multiple pathways are
     available then the shortest is preferred.)  Arranging an extension's
     script files in this style can reduce the amount of maintenance effort
     needed to produce small updates.
    

    

     If you use secondary (version-specific) control files with an extension
     maintained in this style, keep in mind that each version needs a control
     file even if it has no stand-alone installation script, as that control
     file will determine how the implicit update to that version is performed.
     For example, if foo--1.0.control specifies requires
     = 'bar' but foo's other control files do not, the
     extension's dependency on bar will be dropped when updating
     from 1.0 to another version.
    

   

   

    
# Security Considerations for Extensions

    

     Widely-distributed extensions should assume little about the database
     they occupy.  Therefore, it's appropriate to write functions provided
     by an extension in a secure style that cannot be compromised by
     search-path-based attacks.
    

    

     An extension that has the superuser property set to
     true must also consider security hazards for the actions taken within
     its installation and update scripts.  It is not terribly difficult for
     a malicious user to create trojan-horse objects that will compromise
     later execution of a carelessly-written extension script, allowing that
     user to acquire superuser privileges.
    

    

     If an extension is marked trusted, then its
     installation schema can be selected by the installing user, who might
     intentionally use an insecure schema in hopes of gaining superuser
     privileges.  Therefore, a trusted extension is extremely exposed from a
     security standpoint, and all its script commands must be carefully
     examined to ensure that no compromise is possible.
    

    

     Advice about writing functions securely is provided in
      below, and advice
     about writing installation scripts securely is provided in
     .
    

    

     
# Security Considerations for Extension Functions

     

      SQL-language and PL-language functions provided by extensions are at
      risk of search-path-based attacks when they are executed, since
      parsing of these functions occurs at execution time not creation time.
     

     

      The CREATE
      FUNCTION reference page contains advice about
      writing SECURITY DEFINER functions safely.  It's
      good practice to apply those techniques for any function provided by
      an extension, since the function might be called by a high-privilege
      user.
     

     
     

      If you cannot set the search_path to contain only
      secure schemas, assume that each unqualified name could resolve to an
      object that a malicious user has defined.  Beware of constructs that
      depend on search_path implicitly; for
      example, IN
      and CASE expression WHEN
      always select an operator using the search path.  In their place, use
      OPERATOR(schema.=) ANY
      and CASE WHEN expression.
     

     

      A general-purpose extension usually should not assume that it's been
      installed into a secure schema, which means that even schema-qualified
      references to its own objects are not entirely risk-free.  For
      example, if the extension has defined a
      function myschema.myfunc(bigint) then a call such
      as myschema.myfunc(42) could be captured by a
      hostile function myschema.myfunc(integer).  Be
      careful that the data types of function and operator parameters exactly
      match the declared argument types, using explicit casts where necessary.
     

    

    

     
# Security Considerations for Extension Scripts

     

      An extension installation or update script should be written to guard
      against search-path-based attacks occurring when the script executes.
      If an object reference in the script can be made to resolve to some
      other object than the script author intended, then a compromise might
      occur immediately, or later when the mis-defined extension object is
      used.
     

     

      DDL commands such as CREATE FUNCTION
      and CREATE OPERATOR CLASS are generally secure,
      but beware of any command having a general-purpose expression as a
      component.  For example, CREATE VIEW needs to be
      vetted, as does a DEFAULT expression
      in CREATE FUNCTION.
     

     

      Sometimes an extension script might need to execute general-purpose
      SQL, for example to make catalog adjustments that aren't possible via
      DDL.  Be careful to execute such commands with a
      secure search_path; do not
      trust the path provided by CREATE/ALTER EXTENSION
      to be secure.  Best practice is to temporarily
      set search_path to pg_catalog,
      pg_temp and insert references to the extension's
      installation schema explicitly where needed.  (This practice might
      also be helpful for creating views.)  Examples can be found in
      the contrib modules in
      the PostgreSQL source code distribution.
     

     

      Secure cross-extension references typically require schema-qualification
      of the names of the other extension's objects, using the
      @extschema:name@
      syntax, in addition to careful matching of argument types for functions
      and operators.
     

    

   

   

    
# Extension Example

    

     Here is a complete example of an SQL-only
     extension, a two-element composite type that can store any type of value
     in its slots, which are named k and v.  Non-text
     values are automatically coerced to text for storage.
    

    

     The script file pair--1.0.sql looks like this:

```
 (LEFTARG = text, RIGHTARG = text, FUNCTION = pair);

-- "SET search_path" is easy to get right, but qualified names perform better.
CREATE FUNCTION lower(pair)
RETURNS pair LANGUAGE SQL
AS 'SELECT ROW(lower($1.k), lower($1.v))::@extschema@.pair;'
SET search_path = pg_temp;

CREATE FUNCTION pair_concat(pair, pair)
RETURNS pair LANGUAGE SQL
AS 'SELECT ROW($1.k OPERATOR(pg_catalog.||) $2.k,
               $1.v OPERATOR(pg_catalog.||) $2.v)::@extschema@.pair;';
]]>
```

    

    

     The control file pair.control looks like this:

```
# pair extension
comment = 'A key/value pair data type'
default_version = '1.0'
# cannot be relocatable because of use of @extschema@
relocatable = false
```

    

    

     While you hardly need a makefile to install these two files into the
     correct directory, you could use a Makefile containing this:

```
EXTENSION = pair
DATA = pair--1.0.sql

PG_CONFIG = pg_config
PGXS := $(shell $(PG_CONFIG) --pgxs)
include $(PGXS)
```

     This makefile relies on PGXS, which is described
     in .  The command make install
     will install the control and script files into the correct
     directory as reported by pg_config.
    

    

     Once the files are installed, use the
     CREATE EXTENSION command to load the objects into
     any particular database.
    

   

  

  

   
# Extension Building Infrastructure

   
    pgxs
   

   

    If you are thinking about distributing your
    PostgreSQL extension modules, setting up a
    portable build system for them can be fairly difficult.  Therefore
    the PostgreSQL installation provides a build
    infrastructure for extensions, called PGXS, so
    that simple extension modules can be built simply against an
    already installed server.  PGXS is mainly intended
    for extensions that include C code, although it can be used for
    pure-SQL extensions too.  Note that PGXS is not
    intended to be a universal build system framework that can be used
    to build any software interfacing to PostgreSQL;
    it simply automates common build rules for simple server extension
    modules.  For more complicated packages, you might need to write your
    own build system.
   

   

    To use the PGXS infrastructure for your extension,
    you must write a simple makefile.
    In the makefile, you need to set some variables
    and include the global PGXS makefile.
    Here is an example that builds an extension module named
    isbn_issn, consisting of a shared library containing
    some C code, an extension control file, an SQL script, an include file
    (only needed if other modules might need to access the extension functions
    without going via SQL), and a documentation text file:

```
MODULES = isbn_issn
EXTENSION = isbn_issn
DATA = isbn_issn--1.0.sql
DOCS = README.isbn_issn
HEADERS_isbn_issn = isbn_issn.h

PG_CONFIG = pg_config
PGXS := $(shell $(PG_CONFIG) --pgxs)
include $(PGXS)
```

    The last three lines should always be the same.  Earlier in the
    file, you assign variables or add custom
    make rules.
   

   

    Set one of these three variables to specify what is built:

    

     

      
MODULES

      

       

        list of shared-library objects to be built from source files with same
        stem (do not include library suffixes in this list)
       

      

     

     

      
MODULE_big

      

       

        a shared library to build from multiple source files
        (list object files in OBJS)
       

      

     

     

      
PROGRAM

      

       

        an executable program to build
        (list object files in OBJS)
       

      

     

    

    The following variables can also be set:

    

     

      
EXTENSION

      

       

        extension name(s); for each name you must provide an
        extension.control file,
        which will be installed into
        prefix/share/extension
       

      

     

     

      
MODULEDIR

      

       

        subdirectory of prefix/share
        into which DATA and DOCS files should be installed
        (if not set, default is extension if
        EXTENSION is set,
        or contrib if not)
       

      

     

     

      
DATA

      

       

        random files to install into prefix/share/$MODULEDIR
       

      

     

     

      
DATA_built

      

       

        random files to install into
        prefix/share/$MODULEDIR,
        which need to be built first
       

      

     

     

      
DATA_TSEARCH

      

       

        random files to install under
        prefix/share/tsearch_data
       

      

     

     

      
DOCS

      

       

        random files to install under
        prefix/doc/$MODULEDIR
       

      

     

     

      
HEADERS

      
HEADERS_built

      

       

        Files to (optionally build and) install under
        prefix/include/server/$MODULEDIR/$MODULE_big.
       

       

        Unlike DATA_built, files in HEADERS_built
        are not removed by the clean target; if you want them removed,
        also add them to EXTRA_CLEAN or add your own rules to do it.
       

      

     

     

      
HEADERS_$MODULE

      
HEADERS_built_$MODULE

      

       

        Files to install (after building if specified) under
        prefix/include/server/$MODULEDIR/$MODULE,
        where $MODULE must be a module name used
        in MODULES or MODULE_big.
       

       

        Unlike DATA_built, files in HEADERS_built_$MODULE
        are not removed by the clean target; if you want them removed,
        also add them to EXTRA_CLEAN or add your own rules to do it.
       

       

        It is legal to use both variables for the same module, or any
        combination, unless you have two module names in the
        MODULES list that differ only by the presence of a
        prefix built_, which would cause ambiguity. In
        that (hopefully unlikely) case, you should use only the
        HEADERS_built_$MODULE variables.
       

      

     

     

      
SCRIPTS

      

       

        script files (not binaries) to install into
        prefix/bin
       

      

     

     

      
SCRIPTS_built

      

       

        script files (not binaries) to install into
        prefix/bin,
        which need to be built first
       

      

     

     

      
REGRESS

      

       

        list of regression test cases (without suffix), see below
       

      

     

     

      
REGRESS_OPTS

      

       

        additional switches to pass to pg_regress
       

      

     

     

      
ISOLATION

      

       

        list of isolation test cases, see below for more details
       

      

     

     

      
ISOLATION_OPTS

      

       

        additional switches to pass to
        pg_isolation_regress
       

      

     

     

      
TAP_TESTS

      

       

        switch defining if TAP tests need to be run, see below
       

      

     

     

      
NO_INSTALL

      

       

        don't define an install target, useful for test
        modules that don't need their build products to be installed
       

      

     

     

      
NO_INSTALLCHECK

      

       

        don't define an installcheck target, useful e.g., if tests require special configuration, or don't use pg_regress
       

      

     

     

      
EXTRA_CLEAN

      

       

        extra files to remove in make clean
       

      

     

     

      
PG_CPPFLAGS

      

       

        will be prepended to CPPFLAGS
       

      

     

     

      
PG_CFLAGS

      

       

        will be appended to CFLAGS
       

      

     

     

      
PG_CXXFLAGS

      

       

        will be appended to CXXFLAGS
       

      

     

     

      
PG_LDFLAGS

      

       

        will be prepended to LDFLAGS
       

      

     

     

      
PG_LIBS

      

       

        will be added to PROGRAM link line
       

      

     

     

      
SHLIB_LINK

      

       

        will be added to MODULE_big link line
       

      

     

     

      
PG_CONFIG

      

       

        path to pg_config program for the
        PostgreSQL installation to build against
        (typically just pg_config to use the first one in your
        PATH)
       

      

     

    

   

   

    Put this makefile as Makefile in the directory
    which holds your extension. Then you can do
    make to compile, and then make
    install to install your module.  By default, the extension is
    compiled and installed for the
    PostgreSQL installation that
    corresponds to the first pg_config program
    found in your PATH.  You can use a different installation by
    setting PG_CONFIG to point to its
    pg_config program, either within the makefile
    or on the make command line.
   

   

    You can select a separate directory prefix in which to install your
    extension's files, by setting the make variable
    prefix when executing make install
    like so:

```
make install prefix=/usr/local/postgresql
```

    This will install the extension control and SQL files into
    /usr/local/postgresql/share and the shared modules into
    /usr/local/postgresql/lib.  If the prefix does not
    include the strings postgres or
    pgsql, such as

```
make install prefix=/usr/local/extras
```

    then postgresql will be appended to the directory
    names, installing the control and SQL files into
    /usr/local/extras/share/postgresql/extension and the
    shared modules into /usr/local/extras/lib/postgresql.
    Either way, you'll need to set  and  to enable the
    PostgreSQL server to find the files:

```
extension_control_path = '/usr/local/extras/share/postgresql:$system'
dynamic_library_path = '/usr/local/extras/lib/postgresql:$libdir'
```

   

   

    You can also run make in a directory outside the source
    tree of your extension, if you want to keep the build directory separate.
    This procedure is also called a
    VPATHVPATH
    build.  Here's how:

```
mkdir build_dir
cd build_dir
make -f /path/to/extension/source/tree/Makefile
make -f /path/to/extension/source/tree/Makefile install
```

   

   

    Alternatively, you can set up a directory for a VPATH build in a similar
    way to how it is done for the core code. One way to do this is using the
    core script config/prep_buildtree. Once this has been done
    you can build by setting the make variable
    VPATH like this:

```
make VPATH=/path/to/extension/source/tree
make VPATH=/path/to/extension/source/tree install
```

    This procedure can work with a greater variety of directory layouts.
   

   

    The scripts listed in the REGRESS variable are used for
    regression testing of your module, which can be invoked by make
    installcheck after doing make install.  For this to
    work you must have a running PostgreSQL server.
    The script files listed in REGRESS must appear in a
    subdirectory named sql/ in your extension's directory.
    These files must have extension .sql, which must not be
    included in the REGRESS list in the makefile.  For each
    test there should also be a file containing the expected output in a
    subdirectory named expected/, with the same stem and
    extension .out.  make installcheck
    executes each test script with psql, and compares the
    resulting output to the matching expected file.  Any differences will be
    written to the file regression.diffs in diff
    -c format.  Note that trying to run a test that is missing its
    expected file will be reported as trouble, so make sure you
    have all expected files.
   

   

    The scripts listed in the ISOLATION variable are used
    for tests stressing behavior of concurrent session with your module, which
    can be invoked by make installcheck after doing
    make install.  For this to work you must have a
    running PostgreSQL server.  The script files
    listed in ISOLATION must appear in a subdirectory
    named specs/ in your extension's directory.  These files
    must have extension .spec, which must not be included
    in the ISOLATION list in the makefile.  For each test
    there should also be a file containing the expected output in a
    subdirectory named expected/, with the same stem and
    extension .out.  make installcheck
    executes each test script, and compares the resulting output to the
    matching expected file.  Any differences will be written to the file
    output_iso/regression.diffs in
    diff -c format.  Note that trying to run a test that is
    missing its expected file will be reported as trouble, so
    make sure you have all expected files.
   

   

    TAP_TESTS enables the use of TAP tests.  Data from each
    run is present in a subdirectory named tmp_check/.
    See also  for more details.
   

   
    

     The easiest way to create the expected files is to create empty files,
     then do a test run (which will of course report differences).  Inspect
     the actual result files found in the results/
     directory (for tests in REGRESS), or
     output_iso/results/ directory (for tests in
     ISOLATION), then copy them to
     expected/ if they match what you expect from the test.
