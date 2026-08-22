# System Catalog Declarations and Initial Contents

 

  PostgreSQL uses many different system catalogs
  to keep track of the existence and properties of database objects, such as
  tables and functions.  Physically there is no difference between a system
  catalog and a plain user table, but the backend C code knows the structure
  and properties of each catalog, and can manipulate it directly at a low
  level.  Thus, for example, it is inadvisable to attempt to alter the
  structure of a catalog on-the-fly; that would break assumptions built into
  the C code about how rows of the catalog are laid out.  But the structure
  of the catalogs can change between major versions.
 

 

  The structures of the catalogs are declared in specially formatted C
  header files in the src/include/catalog/ directory of
  the source tree.  For each catalog there is a header file
  named after the catalog (e.g., pg_class.h
  for pg_class), which defines the set of columns
  the catalog has, as well as some other basic properties such as its OID.
 

 

  Many of the catalogs have initial data that must be loaded into them
  during the bootstrap phase
  of initdb, to bring the system up to a point
  where it is capable of executing SQL commands.  (For
  example, pg_class.h must contain an entry for itself,
  as well as one for each other system catalog and index.)  This
  initial data is kept in editable form in data files that are also stored
  in the src/include/catalog/ directory.  For example,
  pg_proc.dat describes all the initial rows that must
  be inserted into the pg_proc catalog.
 

 

  To create the catalog files and load this initial data into them, a
  backend running in bootstrap mode reads a BKI
  (Backend Interface) file containing commands and initial data.
  The postgres.bki file used in this mode is prepared
  from the aforementioned header and data files, while building
  a PostgreSQL distribution, by a Perl script
  named genbki.pl.
  Although it's specific to a particular PostgreSQL
  release, postgres.bki is platform-independent and is
  installed in the share subdirectory of the
  installation tree.
 

 

  genbki.pl also produces a derived header file for
  each catalog, for example pg_class_d.h for
  the pg_class catalog.  This file contains
  automatically-generated macro definitions, and may contain other macros,
  enum declarations, and so on that can be useful for client C code that
  reads a particular catalog.
 

 

  Most PostgreSQL developers don't need to be directly concerned with
  the BKI file, but almost any nontrivial feature
  addition in the backend will require modifying the catalog header files
  and/or initial data files.  The rest of this chapter gives some
  information about that, and for completeness describes
  the BKI file format.
 

 

  
# System Catalog Declaration Rules

  

   The key part of a catalog header file is a C structure definition
   describing the layout of each row of the catalog.  This begins with
   a CATALOG macro, which so far as the C compiler is
   concerned is just shorthand for typedef struct
   FormData_catalogname.
   Each field in the struct gives rise to a catalog column.
   Fields can be annotated using the BKI property macros described
   in genbki.h, for example to define a default value
   for a field or mark it as nullable or not nullable.
   The CATALOG line can also be annotated, with some
   other BKI property macros described in genbki.h, to
   define other properties of the catalog as a whole, such as whether
   it is a shared relation.
  

  

   The system catalog cache code (and most catalog-munging code in general)
   assumes that the fixed-length portions of all system catalog tuples are
   in fact present, because it maps this C struct declaration onto them.
   Thus, all variable-length fields and nullable fields must be placed at
   the end, and they cannot be accessed as struct fields.
   For example, if you tried to
   set pg_type.typrelid
   to be NULL, it would fail when some piece of code tried to reference
   typetup->typrelid (or worse,
   typetup->typelem, because that follows
   typrelid).  This would result in
   random errors or even segmentation violations.
  

  

   As a partial guard against this type of error, variable-length or
   nullable fields should not be made directly visible to the C compiler.
   This is accomplished by wrapping them in #ifdef
   CATALOG_VARLEN ... #endif (where
   CATALOG_VARLEN is a symbol that is never defined).
   This prevents C code from carelessly trying to access fields that might
   not be there or might be at some other offset.
   As an independent guard against creating incorrect rows, we
   require all columns that should be non-nullable to be marked so
   in pg_attribute.  The bootstrap code will
   automatically mark catalog columns as NOT NULL
   if they are fixed-width and are not preceded by any nullable or
   variable-width column.
   Where this rule is inadequate, you can force correct marking by using
   BKI_FORCE_NOT_NULL
   and BKI_FORCE_NULL annotations as needed.
  

  

   Frontend code should not include any pg_xxx.h
   catalog header file, as these files may contain C code that won't compile
   outside the backend.  (Typically, that happens because these files also
   contain declarations for functions
   in src/backend/catalog/ files.)
   Instead, frontend code may include the corresponding
   generated pg_xxx_d.h header, which will contain
   OID #defines and any other data that might be of use
   on the client side.  If you want macros or other code in a catalog header
   to be visible to frontend code, write #ifdef
   EXPOSE_TO_CLIENT_CODE ... #endif around that
   section to instruct genbki.pl to copy that section
   to the pg_xxx_d.h header.
  

  

   A few of the catalogs are so fundamental that they can't even be created
   by the BKI create command that's
   used for most catalogs, because that command needs to write information
   into these catalogs to describe the new catalog.  These are
   called bootstrap catalogs, and defining one takes
   a lot of extra work: you have to manually prepare appropriate entries for
   them in the pre-loaded contents of pg_class
   and pg_type, and those entries will need to be
   updated for subsequent changes to the catalog's structure.
   (Bootstrap catalogs also need pre-loaded entries
   in pg_attribute, but
   fortunately genbki.pl handles that chore nowadays.)
   Avoid making new catalogs be bootstrap catalogs if at all possible.
  

 

 

  
# System Catalog Initial Data

  

   Each catalog that has any manually-created initial data (some do not)
   has a corresponding .dat file that contains its
   initial data in an editable format.
  

  

   
# Data File Format

   

    Each .dat file contains Perl data structure literals
    that are simply eval'd to produce an in-memory data structure consisting
    of an array of hash references, one per catalog row.
    A slightly modified excerpt from pg_database.dat
    will demonstrate the key features:
   

```
 '1', oid_symbol => 'Template1DbOid',
  descr => 'database\'s default template',
  datname => 'template1', encoding => 'ENCODING',
  datlocprovider => 'LOCALE_PROVIDER', datistemplate => 't',
  datallowconn => 't', dathasloginevt => 'f', datconnlimit => '-1', datfrozenxid => '0',
  datminmxid => '1', dattablespace => 'pg_default', datcollate => 'LC_COLLATE',
  datctype => 'LC_CTYPE', datlocale => 'DATLOCALE', datacl => '_null_' },

]
]]>
```

   

    Points to note:
   

   

    

     

      The overall file layout is: open square bracket, one or more sets of
      curly braces each of which represents a catalog row, close square
      bracket.  Write a comma after each closing curly brace.
     

    

    

     

      Within each catalog row, write comma-separated
      key =>
      value pairs.  The
      allowed keys are the names of the catalog's
      columns, plus the metadata keys oid,
      oid_symbol,
      array_type_oid, and descr.
      (The use of oid and oid_symbol
      is described in  below,
      while array_type_oid is described in
      .
      descr supplies a description string for the object,
      which will be inserted into pg_description
      or pg_shdescription as appropriate.)
      While the metadata keys are optional, the catalog's defined columns
      must all be provided, except when the catalog's .h
      file specifies a default value for the column.
      (In the example above, the datdba field has
      been omitted because pg_database.h supplies a
      suitable default value for it.)
     

    

    

     

      All values must be single-quoted.  Escape single quotes used within a
      value with a backslash.  Backslashes meant as data can, but need not,
      be doubled; this follows Perl's rules for simple quoted literals.
      Note that backslashes appearing as data will be treated as escapes by
      the bootstrap scanner, according to the same rules as for escape string
      constants (see ); for
      example \t converts to a tab character.  If you
      actually want a backslash in the final value, you will need to write
      four of them: Perl strips two, leaving \\ for the
      bootstrap scanner to see.
     

    

    

     

      Null values are represented by _null_.
      (Note that there is no way to create a value that is just that
      string.)
     

    

    

     

      Comments are preceded by #, and must be on their
      own lines.
     

    

    

     

      Field values that are OIDs of other catalog entries should be
      represented by symbolic names rather than actual numeric OIDs.
      (In the example above, dattablespace
      contains such a reference.)
      This is described in 
      below.
     

    

    

     

      There is a special case for values of the
      pg_proc.proargdefaults
      field, which is of type pg_node_tree.  The real
      contents of that type are too complex for hand-written entries,
      but what we need for proargdefaults is
      typically just a list of Const nodes.  Therefore, the bootstrap
      backend will interpret a value given for that field according to
      text array syntax, and then feed the array element values to the
      datatype input routines for the corresponding input parameters' data
      types, and finally build Const nodes from the datums.
     

    

    

     

      Since hashes are unordered data structures, field order and line
      layout aren't semantically significant.  However, to maintain a
      consistent appearance, we set a few rules that are applied by the
      formatting script reformat_dat_file.pl:

      

       

        

         Within each pair of curly braces, the metadata
         fields oid, oid_symbol,
         array_type_oid, and descr
         (if present) come first, in that order, then the catalog's own
         fields appear in their defined order.
        

       

       

        

         Newlines are inserted between fields as needed to limit line length
         to 80 characters, if possible.  A newline is also inserted between
         the metadata fields and the regular fields.
        

       

       

        

         If the catalog's .h file specifies a default
         value for a column, and a data entry has that same
         value, reformat_dat_file.pl will omit it from
         the data file.  This keeps the data representation compact.
        

       

       

        

         reformat_dat_file.pl preserves blank lines
         and comment lines as-is.
        

       

      

      It's recommended to run reformat_dat_file.pl
      before submitting catalog data patches.  For convenience, you can
      simply change to src/include/catalog/ and
      run make reformat-dat-files.
     

    

    

     

      If you want to add a new method of making the data representation
      smaller, you must implement it
      in reformat_dat_file.pl and also
      teach Catalog::ParseData() how to expand the
      data back into the full representation.
     

    

   

  

  

   
# OID Assignment

   

    A catalog row appearing in the initial data can be given a
    manually-assigned OID by writing an oid
    => nnnn metadata field.
    Furthermore, if an OID is assigned, a C macro for that OID can be
    created by writing an oid_symbol
    => name metadata field.
   

   

    Pre-loaded catalog rows must have preassigned OIDs if there are OID
    references to them in other pre-loaded rows.  A preassigned OID is
    also needed if the row's OID must be referenced from C code.
    If neither case applies, the oid metadata field can
    be omitted, in which case the bootstrap code assigns an OID
    automatically.
    In practice we usually preassign OIDs for all or none of the pre-loaded
    rows in a given catalog, even if only some of them are actually
    cross-referenced.
   

   

    Writing the actual numeric value of any OID in C code is considered
    very bad form; always use a macro, instead.  Direct references
    to pg_proc OIDs are common enough that there's
    a special mechanism to create the necessary macros automatically;
    see src/backend/utils/Gen_fmgrtab.pl.  Similarly
    &mdash; but, for historical reasons, not done the same way &mdash;
    there's an automatic method for creating macros
    for pg_type
    OIDs.  oid_symbol entries are therefore not
    necessary in those two catalogs.  Likewise, macros for
    the pg_class OIDs of system catalogs and
    indexes are set up automatically.  For all other system catalogs, you
    have to manually specify any macros you need
    via oid_symbol entries.
   

   

    To find an available OID for a new pre-loaded row, run the
    script src/include/catalog/unused_oids.
    It prints inclusive ranges of unused OIDs (e.g., the output
    line 45-900 means OIDs 45 through 900 have not been
    allocated yet).  Currently, OIDs 1&ndash;9999 are reserved for manual
    assignment; the unused_oids script simply looks
    through the catalog headers and .dat files
    to see which ones do not appear.  You can also use
    the duplicate_oids script to check for mistakes.
    (genbki.pl will assign OIDs for any rows that
    didn't get one hand-assigned to them, and it will also detect duplicate
    OIDs at compile time.)
   

   

    When choosing OIDs for a patch that is not expected to be committed
    immediately, best practice is to use a group of more-or-less
    consecutive OIDs starting with some random choice in the range
    8000&mdash;9999.  This minimizes the risk of OID collisions with other
    patches being developed concurrently.  To keep the 8000&mdash;9999
    range free for development purposes, after a patch has been committed
    to the master git repository its OIDs should be renumbered into
    available space below that range.  Typically, this will be done
    near the end of each development cycle, moving all OIDs consumed by
    patches committed in that cycle at the same time.  The script
    renumber_oids.pl can be used for this purpose.
    If an uncommitted patch is found to have OID conflicts with some
    recently-committed patch, renumber_oids.pl may
    also be useful for recovering from that situation.
   

   

    Because of this convention of possibly renumbering OIDs assigned by
    patches, the OIDs assigned by a patch should not be considered stable
    until the patch has been included in an official release.  We do not
    change manually-assigned object OIDs once released, however, as that
    would create assorted compatibility problems.
   

   

    If genbki.pl needs to assign an OID to a catalog
    entry that does not have a manually-assigned OID, it will use a value in
    the range 10000&mdash;11999.  The server's OID counter is set to 10000
    at the start of a bootstrap run, so that any objects created on-the-fly
    during bootstrap processing also receive OIDs in this range.  (The
    usual OID assignment mechanism takes care of preventing any conflicts.)
   

   

    Objects with OIDs below FirstUnpinnedObjectId (12000)
    are considered pinned, preventing them from being
    deleted.  (There are a small number of exceptions, which are
    hard-wired into IsPinnedObject().)
    initdb forces the OID counter up
    to FirstUnpinnedObjectId as soon as it's ready to
    create unpinned objects.  Thus objects created during the later phases
    of initdb, such as objects created while
    running the information_schema.sql script, will
    not be pinned, while all objects known
    to genbki.pl will be.
   

   

    OIDs assigned during normal database operation are constrained to be
    16384 or higher.  This ensures that the range 10000&mdash;16383 is free
    for OIDs assigned automatically by genbki.pl or
    during initdb.  These
    automatically-assigned OIDs are not considered stable, and may change
    from one installation to another.
   

  

  

   
# OID Reference Lookup

   

    In principle, cross-references from one initial catalog row to another
    could be written just by writing the preassigned OID of the referenced
    row in the referencing field.  However, that is against project
    policy, because it is error-prone, hard to read, and subject to
    breakage if a newly-assigned OID is renumbered.  Therefore
    genbki.pl provides mechanisms to write
    symbolic references instead.
    The rules are as follows:
   

   

    

     

      Use of symbolic references is enabled in a particular catalog column
      by attaching BKI_LOOKUP(lookuprule)
      to the column's definition, where lookuprule
      is the name of the referenced catalog, e.g., pg_proc.
      BKI_LOOKUP can be attached to columns of
      type Oid, regproc, oidvector,
      or Oid[]; in the latter two cases it implies performing a
      lookup on each element of the array.
     

    

    

     

      It's also permissible to attach BKI_LOOKUP(encoding)
      to integer columns to reference character set encodings, which are
      not currently represented as catalog OIDs, but have a set of values
      known to genbki.pl.
     

    

    

     

      In some catalog columns, it's allowed for entries to be zero instead
      of a valid reference.  If this is allowed, write
      BKI_LOOKUP_OPT instead
      of BKI_LOOKUP.  Then you can
      write 0 for an entry.  (If the column is
      declared regproc, you can optionally
      write - instead of 0.)
      Except for this special case, all entries in
      a BKI_LOOKUP column must be symbolic references.
      genbki.pl will warn about unrecognized names.
     

    

    

     

      Most kinds of catalog objects are simply referenced by their names.
      Note that type names must exactly match the
      referenced pg_type
      entry's typname; you do not get to use
      any aliases such as integer
      for int4.
     

    

    

     

      A function can be represented by
      its proname, if that is unique among
      the pg_proc.dat entries (this works like regproc
      input).  Otherwise, write it
      as proname(argtypename,argtypename,...),
      like regprocedure.  The argument type names must be spelled exactly as
      they are in the pg_proc.dat entry's
      proargtypes field.  Do not insert any
      spaces.
     

    

    

     

      Operators are represented
      by oprname(lefttype,righttype),
      writing the type names exactly as they appear in
      the pg_operator.dat
      entry's oprleft
      and oprright fields.
      (Write 0 for the omitted operand of a unary
      operator.)
     

    

    

     

      The names of opclasses and opfamilies are only unique within an
      access method, so they are represented
      by access_method_name/object_name.
     

    

    

     

      In none of these cases is there any provision for
      schema-qualification; all objects created during bootstrap are
      expected to be in the pg_catalog schema.
     

    

   

   

    genbki.pl resolves all symbolic references while it
    runs, and puts simple numeric OIDs into the emitted BKI file.  There is
    therefore no need for the bootstrap backend to deal with symbolic
    references.
   

   

    It's desirable to mark OID reference columns
    with BKI_LOOKUP or BKI_LOOKUP_OPT
    even if the catalog has no initial data that requires lookup.  This
    allows genbki.pl to record the foreign key
    relationships that exist in the system catalogs.  That information is
    used in the regression tests to check for incorrect entries.  See also
    the macros DECLARE_FOREIGN_KEY,
    DECLARE_FOREIGN_KEY_OPT,
    DECLARE_ARRAY_FOREIGN_KEY,
    and DECLARE_ARRAY_FOREIGN_KEY_OPT, which are
    used to declare foreign key relationships that are too complex
    for BKI_LOOKUP (typically, multi-column foreign
    keys).
   

  

  

   
# Automatic Creation of Array Types

   

    Most scalar data types should have a corresponding array type (that is,
    a standard varlena array type whose element type is the scalar type, and
    which is referenced by the typarray field of
    the scalar type's pg_type
    entry).  genbki.pl is able to generate
    the pg_type entry for the array type
    automatically in most cases.
   

   

    To use this facility, just write an array_type_oid
    => nnnn metadata field in the
    scalar type's pg_type entry, specifying the OID
    to use for the array type.  You may then omit
    the typarray field, since it will be filled
    automatically with that OID.
   

   

    The generated array type's name is the scalar type's name with an
    underscore prepended.  The array entry's other fields are filled from
    BKI_ARRAY_DEFAULT(value)
    annotations in pg_type.h, or if there isn't one,
    copied from the scalar type.  (There's also a special case
    for typalign.)  Then
    the typelem
    and typarray fields of the two entries are
    set to cross-reference each other.
   

  

  

   
# Recipes for Editing Data Files

   

    Here are some suggestions about the easiest ways to perform common tasks
    when updating catalog data files.
   

   
    
# Add a new column with a default to a catalog:

    

     Add the column to the header file with
     a BKI_DEFAULT(value)
     annotation.  The data file need only be adjusted by adding the field
     in existing rows where a non-default value is needed.
    

   

   
    
# Add a default value to an existing column that doesn't have
     one:

    

     Add a BKI_DEFAULT annotation to the header file,
     then run make reformat-dat-files to remove
     now-redundant field entries.
    

   

   
    
# Remove a column, whether it has a default or not:

    

     Remove the column from the header, then run make
     reformat-dat-files to remove now-useless field entries.
    

   

   
    
# Change or remove an existing default value:

    

     You cannot simply change the header file, since that will cause the
     current data to be interpreted incorrectly.  First run make
     expand-dat-files to rewrite the data files with all
     default values inserted explicitly, then change or remove
     the BKI_DEFAULT annotation, then run make
     reformat-dat-files to remove superfluous fields again.
    

   

   
    
# Ad-hoc bulk editing:

    

     reformat_dat_file.pl can be adapted to perform
     many kinds of bulk changes.  Look for its block comments showing where
     one-off code can be inserted.  In the following example, we are going
     to consolidate two Boolean fields in pg_proc
     into a char field:

     
      

       

        Add the new column, with a default,
        to pg_proc.h:

```
+    /* see PROKIND_ categories below */
+    char        prokind BKI_DEFAULT(f);
```

       

      

      

       

        Create a new script based on reformat_dat_file.pl
        to insert appropriate values on-the-fly:

```
-           # At this point we have the full row in memory as a hash
-           # and can do any operations we want. As written, it only
-           # removes default values, but this script can be adapted to
-           # do one-off bulk-editing.
+           # One-off change to migrate to prokind
+           # Default has already been filled in by now, so change to other
+           # values as appropriate
+           if ($values{proisagg} eq 't')
+           {
+               $values{prokind} = 'a';
+           }
+           elsif ($values{proiswindow} eq 't')
+           {
+               $values{prokind} = 'w';
+           }
```

       

      

      

       

        Run the new script:

```
$ cd src/include/catalog
$ perl  rewrite_dat_with_prokind.pl  pg_proc.dat
```

        At this point pg_proc.dat has all three
        columns, prokind,
        proisagg,
        and proiswindow, though they will appear
        only in rows where they have non-default values.
       

      

      

       

        Remove the old columns from pg_proc.h:

```
-    /* is it an aggregate? */
-    bool        proisagg BKI_DEFAULT(f);
-
-    /* is it a window function? */
-    bool        proiswindow BKI_DEFAULT(f);
```

       

      

      

       

        Finally, run make reformat-dat-files to remove
        the useless old entries from pg_proc.dat.
       

      

     

     For further examples of scripts used for bulk editing, see
     convert_oid2name.pl
     and remove_pg_type_oid_symbols.pl attached to this
     message:
     
    

   
  

 

 

  
# BKI File Format

  

   This section describes how the PostgreSQL
   backend interprets BKI files.  This description
   will be easier to understand if the postgres.bki
   file is at hand as an example.
  

  

   BKI input consists of a sequence of commands.  Commands are made up
   of a number of tokens, depending on the syntax of the command.
   Tokens are usually separated by whitespace, but need not be if
   there is no ambiguity.  There is no special command separator; the
   next token that syntactically cannot belong to the preceding
   command starts a new one.  (Usually you would put a new command on
   a new line, for clarity.)  Tokens can be certain key words, special
   characters (parentheses, commas, etc.), identifiers, numbers, or
   single-quoted strings.  Everything is case sensitive.
  

  

   Lines starting with # are ignored.
  

 

 

  
# BKI Commands

  

   

    

     create
     tablename
     tableoid
     bootstrap
     shared_relation
     rowtype_oid oid
     (name1 =
     type1
     FORCE NOT NULL | FORCE NULL  ,
     name2 =
     type2
     FORCE NOT NULL | FORCE NULL ,
     ...)
    

    

     

      Create a table named tablename, and having the OID
      tableoid,
      with the columns given in parentheses.
     

     

      The following column types are supported directly by
      bootstrap.c: bool,
      bytea, char (1 byte),
      int2, int4, int8,
      float4, float8,
      name, regproc, text,
      jsonb, oid, pg_node_tree,
      int2vector, oidvector,
      _int4 (array), _text (array),
      _oid (array), _char (array),
      _aclitem (array).  Although it is possible to create
      tables containing columns of other types, this cannot be done until
      after pg_type has been created and filled with
      appropriate entries.  (That effectively means that only these
      column types can be used in bootstrap catalogs, but non-bootstrap
      catalogs can contain any built-in type.)
     

     

      When bootstrap is specified,
      the table will only be created on disk; nothing is entered into
      pg_class,
      pg_attribute, etc., for it.  Thus the
      table will not be accessible by ordinary SQL operations until
      such entries are made the hard way (with insert
      commands).  This option is used for creating
      pg_class etc. themselves.
     

     

      The table is created as shared if shared_relation is
      specified.
      The table's row type OID (pg_type OID) can optionally
      be specified via the rowtype_oid clause; if not specified,
      an OID is automatically generated for it.  (The rowtype_oid
      clause is useless if bootstrap is specified, but it can be
      provided anyway for documentation.)
     

    

   

   

    

     open tablename
    

    

     

      Open the table named
      tablename
      for insertion of data.  Any currently open table is closed.
     

    

   

   

    

     close tablename
    

    

     

      Close the open table.  The name of the table must be given as a
      cross-check.
     

    

   

   

    

     insert ( value1 value2 ... )
    

    

     

      Insert a new row into the open table using value1, value2, etc., for its column
      values.
     

     

      NULL values can be specified using the special key word
      _null_.  Values that do not look like
      identifiers or digit strings must be single-quoted.
      (To include a single quote in a value, write it twice.
      Escape-string-style backslash escapes are allowed in the string, too.)
     

     

      In most cases a value
      string is simply fed to the datatype input routine for the column's
      data type, after de-quoting if needed.  However there are exceptions
      for certain fields, as detailed previously.
     

    

   

   

    

     declare unique
     index indexname
     indexoid
     on tablename
     using amname
     ( opclass1
     name1
     , ... )
    

    

     

      Create an index named indexname, having OID
      indexoid,
      on the table named
      tablename, using the
      amname access
      method.  The fields to index are called name1, name2 etc., and the operator
      classes to use are opclass1, opclass2 etc., respectively.
      The index file is created and appropriate catalog entries are
      made for it, but the index contents are not initialized by this command.
     

    

   

   

    

     declare toast
     toasttableoid
     toastindexoid
     on tablename
    

    

     

      Create a TOAST table for the table named
      tablename.
      The TOAST table is assigned OID
      toasttableoid
      and its index is assigned OID
      toastindexoid.
      As with declare index, filling of the index
      is postponed.
     

    

   

   

    
build indices

    

     

      Fill in the indices that have previously been declared.
     

    

   

  

 

 

  
# Structure of the Bootstrap BKI File

  

   The open command cannot be used until the tables it uses
   exist and have entries for the table that is to be opened.
   (These minimum tables are pg_class,
   pg_attribute, pg_proc, and
   pg_type.)   To allow those tables themselves to be filled,
   create with the bootstrap option implicitly opens
   the created table for data insertion.
  

  

   Also, the declare index and declare toast
   commands cannot be used until the system catalogs they need have been
   created and filled in.
  

  

   Thus, the structure of the postgres.bki file has to
   be:
   
    

     

      create bootstrap one of the critical tables
     

    

    

     

      insert data describing at least the critical tables
     

    

    

     

      close
     

    

    

     

      Repeat for the other critical tables.
     

    

    

     

      create (without bootstrap) a noncritical table
     

    

    

     

      open
     

    

    

     

      insert desired data
     

    

    

     

      close
     

    

    

     

      Repeat for the other noncritical tables.
     

    

    

     

      Define indexes and TOAST tables.
     

    

    

     

      build indices
     

    

   
  

  

   There are doubtless other, undocumented ordering dependencies.
  

 

 

  
# BKI Example

  

   The following sequence of commands will create the table
   test_table with OID 420, having three columns
   oid, cola and colb
   of type oid, int4 and text,
   respectively, and insert two rows into the table:

```
create test_table 420 (oid = oid, cola = int4, colb = text)
open test_table
insert ( 421 1 'value 1' )
insert ( 422 2 _null_ )
close test_table
```
