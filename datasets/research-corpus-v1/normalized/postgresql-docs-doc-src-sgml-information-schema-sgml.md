# The Information Schema

 
  information schema
 

 

  The information schema consists of a set of views that contain
  information about the objects defined in the current database.  The
  information schema is defined in the SQL standard and can therefore
  be expected to be portable and remain stable &mdash; unlike the system
  catalogs, which are specific to
  PostgreSQL and are modeled after
  implementation concerns.  The information schema views do not,
  however, contain information about
  PostgreSQL-specific features; to inquire
  about those you need to query the system catalogs or other
  PostgreSQL-specific views.
 

 
  

   When querying the database for constraint information, it is possible
   for a standard-compliant query that expects to return one row to
   return several.  This is because the SQL standard requires constraint
   names to be unique within a schema, but
   PostgreSQL does not enforce this
   restriction.  PostgreSQL
   automatically-generated constraint names avoid duplicates in the
   same schema, but users can specify such duplicate names.
  

  

   This problem can appear when querying information schema views such
   as check_constraint_routine_usage,
   check_constraints, domain_constraints, and
   referential_constraints.  Some other views have similar
   issues but contain the table name to help distinguish duplicate
   rows, e.g., constraint_column_usage,
   constraint_table_usage, table_constraints.
  

 

 

  
# The Schema

  

   The information schema itself is a schema named
   information_schema.  This schema automatically
   exists in all databases.  The owner of this schema is the initial
   database user in the cluster, and that user naturally has all the
   privileges on this schema, including the ability to drop it (but
   the space savings achieved by that are minuscule).
  

  

   By default, the information schema is not in the schema search
   path, so you need to access all objects in it through qualified
   names.  Since the names of some of the objects in the information
   schema are generic names that might occur in user applications, you
   should be careful if you want to put the information schema in the
   path.
  

 

 

  
# Data Types

  

   The columns of the information schema views use special data types
   that are defined in the information schema.  These are defined as
   simple domains over ordinary built-in types.  You should not use
   these types for work outside the information schema, but your
   applications must be prepared for them if they select from the
   information schema.
  

  

   These types are:

   

    

     
cardinal_number

     

      

       A nonnegative integer.
      

     

    

    

     
character_data

     

      

       A character string (without specific maximum length).
      

     

    

    

     
sql_identifier

     

      

       A character string.  This type is used for SQL identifiers, the
       type character_data is used for any other kind of
       text data.
      

     

    

    

     
time_stamp

     

      

       A domain over the type timestamp with time zone
      

     

    

    

     
yes_or_no

     

      

       A character string domain that contains
       either YES or NO.  This
       is used to represent Boolean (true/false) data in the
       information schema.  (The information schema was invented
       before the type boolean was added to the SQL
       standard, so this convention is necessary to keep the
       information schema backward compatible.)
      

     

    

   

   Every column in the information schema has one of these five types.
  

 

 

  
# information_schema_catalog_name

  

   information_schema_catalog_name is a table that
   always contains one row and one column containing the name of the
   current database (current catalog, in SQL terminology).
  

  
   
# information_schema_catalog_name Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       catalog_name sql_identifier
      

      

       Name of the database that contains this information schema
      

     
    
   
  
 

 

  
# administrable_role_&zwsp;authorizations

  

   The view administrable_role_authorizations
   identifies all roles that the current user has the admin option
   for.
  

  
   
# administrable_role_authorizations Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantee sql_identifier
      

      

       Name of the role to which this role membership was granted (can
       be the current user, or a different role in case of nested role
       memberships)
      

     

     
      

       role_name sql_identifier
      

      

       Name of a role
      

     

     
      

       is_grantable yes_or_no
      

      

       Always YES
      

     
    
   
  
 

 

  
# applicable_roles

  

   The view applicable_roles identifies all roles
   whose privileges the current user can use.  This means there is
   some chain of role grants from the current user to the role in
   question.  The current user itself is also an applicable role.  The
   set of applicable roles is generally used for permission checking.
   applicable role
   roleapplicable
  

  
   
# applicable_roles Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantee sql_identifier
      

      

       Name of the role to which this role membership was granted (can
       be the current user, or a different role in case of nested role
       memberships)
      

     

     
      

       role_name sql_identifier
      

      

       Name of a role
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the grantee has the admin option on
       the role, NO if not
      

     
    
   
  
 

 

  
# attributes

  

   The view attributes contains information about
   the attributes of composite data types defined in the database.
   (Note that the view does not give information about table columns,
   which are sometimes called attributes in PostgreSQL contexts.)
   Only those attributes are shown that the current user has access to (by way
   of being the owner of or having some privilege on the type).
  

  
   
# attributes Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       udt_catalog sql_identifier
      

      

       Name of the database containing the data type (always the current database)
      

     

     
      

       udt_schema sql_identifier
      

      

       Name of the schema containing the data type
      

     

     
      

       udt_name sql_identifier
      

      

       Name of the data type
      

     

     
      

       attribute_name sql_identifier
      

      

       Name of the attribute
      

     

     
      

       ordinal_position cardinal_number
      

      

       Ordinal position of the attribute within the data type (count starts at 1)
      

     

     
      

       attribute_default character_data
      

      

       Default expression of the attribute
      

     

     
      

       is_nullable yes_or_no
      

      

       YES if the attribute is possibly nullable,
       NO if it is known not nullable.
      

     

     
      

       data_type character_data
      

      

       Data type of the attribute, if it is a built-in type, or
       ARRAY if it is some array (in that case, see
       the view element_types), else
       USER-DEFINED (in that case, the type is
       identified in attribute_udt_name and
       associated columns).
      

     

     
      

       character_maximum_length cardinal_number
      

      

       If data_type identifies a character or bit
       string type, the declared maximum length; null for all other
       data types or if no maximum length was declared.
      

     

     
      

       character_octet_length cardinal_number
      

      

       If data_type identifies a character type,
       the maximum possible length in octets (bytes) of a datum; null
       for all other data types.  The maximum octet length depends on
       the declared character maximum length (see above) and the
       server encoding.
      

     

     
      

       character_set_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       collation_catalog sql_identifier
      

      

       Name of the database containing the collation of the attribute
       (always the current database), null if default or the data type
       of the attribute is not collatable
      

     

     
      

       collation_schema sql_identifier
      

      

       Name of the schema containing the collation of the attribute,
       null if default or the data type of the attribute is not
       collatable
      

     

     
      

       collation_name sql_identifier
      

      

       Name of the collation of the attribute, null if default or the
       data type of the attribute is not collatable
      

     

     
      

       numeric_precision cardinal_number
      

      

       If data_type identifies a numeric type, this
       column contains the (declared or implicit) precision of the
       type for this attribute.  The precision indicates the number of
       significant digits.  It can be expressed in decimal (base 10)
       or binary (base 2) terms, as specified in the column
       numeric_precision_radix.  For all other data
       types, this column is null.
      

     

     
      

       numeric_precision_radix cardinal_number
      

      

       If data_type identifies a numeric type, this
       column indicates in which base the values in the columns
       numeric_precision and
       numeric_scale are expressed.  The value is
       either 2 or 10.  For all other data types, this column is null.
      

     

     
      

       numeric_scale cardinal_number
      

      

       If data_type identifies an exact numeric
       type, this column contains the (declared or implicit) scale of
       the type for this attribute.  The scale indicates the number of
       significant digits to the right of the decimal point.  It can
       be expressed in decimal (base 10) or binary (base 2) terms, as
       specified in the column
       numeric_precision_radix.  For all other data
       types, this column is null.
      

     

     
      

       datetime_precision cardinal_number
      

      

       If data_type identifies a date, time,
       timestamp, or interval type, this column contains the (declared
       or implicit) fractional seconds precision of the type for this
       attribute, that is, the number of decimal digits maintained
       following the decimal point in the seconds value.  For all
       other data types, this column is null.
      

     

     
      

       interval_type character_data
      

      

       If data_type identifies an interval type,
       this column contains the specification which fields the
       intervals include for this attribute, e.g., YEAR TO
       MONTH, DAY TO SECOND, etc.  If no
       field restrictions were specified (that is, the interval
       accepts all fields), and for all other data types, this field
       is null.
      

     

     
      

       interval_precision cardinal_number
      

      

       Applies to a feature not available
       in PostgreSQL
       (see datetime_precision for the fractional
       seconds precision of interval type attributes)
      

     

     
      

       attribute_udt_catalog sql_identifier
      

      

       Name of the database that the attribute data type is defined in
       (always the current database)
      

     

     
      

       attribute_udt_schema sql_identifier
      

      

       Name of the schema that the attribute data type is defined in
      

     

     
      

       attribute_udt_name sql_identifier
      

      

       Name of the attribute data type
      

     

     
      

       scope_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       maximum_cardinality cardinal_number
      

      

       Always null, because arrays always have unlimited maximum cardinality in PostgreSQL
      

     

     
      

       dtd_identifier sql_identifier
      

      

       An identifier of the data type descriptor of the attribute, unique
       among the data type descriptors pertaining to the composite type.  This
       is mainly useful for joining with other instances of such
       identifiers.  (The specific format of the identifier is not
       defined and not guaranteed to remain the same in future
       versions.)
      

     

     
      

       is_derived_reference_attribute yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     
    
   
  

  

   See also under , a similarly
   structured view, for further information on some of the columns.
  

 

 

  
# character_sets

  

   The view character_sets identifies the character
   sets available in the current database.  Since PostgreSQL does not
   support multiple character sets within one database, this view only
   shows one, which is the database encoding.
  

  

   Take note of how the following terms are used in the SQL standard:
   

    

     
character repertoire

     

      

       An abstract collection of characters, for
       example UNICODE, UCS, or
       LATIN1.  Not exposed as an SQL object, but
       visible in this view.
      

     

    

    

     
character encoding form

     

      

       An encoding of some character repertoire.  Most older character
       repertoires only use one encoding form, and so there are no
       separate names for them (e.g., LATIN2 is an
       encoding form applicable to the LATIN2
       repertoire).  But for example Unicode has the encoding forms
       UTF8, UTF16, etc. (not
       all supported by PostgreSQL).  Encoding forms are not exposed
       as an SQL object, but are visible in this view.
      

     

    

    

     
character set

     

      

       A named SQL object that identifies a character repertoire, a
       character encoding, and a default collation.  A predefined
       character set would typically have the same name as an encoding
       form, but users could define other names.  For example, the
       character set UTF8 would typically identify
       the character repertoire UCS, encoding
       form UTF8, and some default collation.
      

     

    

   

   You can think of an encoding in PostgreSQL either as
   a character set or a character encoding form.  They will have the
   same name, and there can only be one in one database.
  

  
   
# character_sets Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       character_set_catalog sql_identifier
      

      

       Character sets are currently not implemented as schema objects, so this column is null.
      

     

     
      

       character_set_schema sql_identifier
      

      

       Character sets are currently not implemented as schema objects, so this column is null.
      

     

     
      

       character_set_name sql_identifier
      

      

       Name of the character set, currently implemented as showing the name of the database encoding
      

     

     
      

       character_repertoire sql_identifier
      

      

       Character repertoire, showing UCS if the encoding is UTF8, else just the encoding name
      

     

     
      

       form_of_use sql_identifier
      

      

       Character encoding form, same as the database encoding
      

     

     
      

       default_collate_catalog sql_identifier
      

      

       Name of the database containing the default collation (always the current database, if any collation is identified)
      

     

     
      

       default_collate_schema sql_identifier
      

      

       Name of the schema containing the default collation
      

     

     
      

       default_collate_name sql_identifier
      

      

       Name of the default collation.  The default collation is
       identified as the collation that matches
       the COLLATE and CTYPE
       settings of the current database.  If there is no such
       collation, then this column and the associated schema and
       catalog columns are null.
      

     
    
   
  
 

 

  
# check_constraint_routine_usage

  

   The view check_constraint_routine_usage
   identifies routines (functions and procedures) that are used by a
   check constraint.  Only those routines are shown that are owned by
   a currently enabled role.
  

  
   
# check_constraint_routine_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       constraint_catalog sql_identifier
      

      

       Name of the database containing the constraint (always the current database)
      

     

     
      

       constraint_schema sql_identifier
      

      

       Name of the schema containing the constraint
      

     

     
      

       constraint_name sql_identifier
      

      

       Name of the constraint
      

     

     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the function.  See  for more information.
      

     
    
   
  
 

 

  
# check_constraints

  

   The view check_constraints contains all check
   constraints, either defined on a table or on a domain, that are
   owned by a currently enabled role.  (The owner of the table or
   domain is the owner of the constraint.)
  

  

   The SQL standard considers not-null constraints to be check constraints
   with a CHECK (column_name IS NOT
   NULL) expression.  So not-null constraints are also included here
   and don't have a separate view.
  

  
   
# check_constraints Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       constraint_catalog sql_identifier
      

      

       Name of the database containing the constraint (always the current database)
      

     

     
      

       constraint_schema sql_identifier
      

      

       Name of the schema containing the constraint
      

     

     
      

       constraint_name sql_identifier
      

      

       Name of the constraint
      

     

     
      

       check_clause character_data
      

      

       The check expression of the check constraint
      

     
    
   
  
 

 

  
# collations

  

   The view collations contains the collations
   available in the current database.
  

  
   
# collations Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       collation_catalog sql_identifier
      

      

       Name of the database containing the collation (always the current database)
      

     

     
      

       collation_schema sql_identifier
      

      

       Name of the schema containing the collation
      

     

     
      

       collation_name sql_identifier
      

      

       Name of the default collation
      

     

     
      

       pad_attribute character_data
      

      

       Always NO PAD (The alternative PAD
       SPACE is not supported by PostgreSQL.)
      

     
    
   
  
 

 
 
  
# collation_character_set_&zwsp;applicability

  

   The view collation_character_set_applicability
   identifies which character set the available collations are
   applicable to.  In PostgreSQL, there is only one character set per
   database (see explanation
   in ), so this view does
   not provide much useful information.
  

  
   
# collation_character_set_applicability Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       collation_catalog sql_identifier
      

      

       Name of the database containing the collation (always the current database)
      

     

     
      

       collation_schema sql_identifier
      

      

       Name of the schema containing the collation
      

     

     
      

       collation_name sql_identifier
      

      

       Name of the default collation
      

     

     
      

       character_set_catalog sql_identifier
      

      

       Character sets are currently not implemented as schema objects, so this column is null
      

     

     
      

       character_set_schema sql_identifier
      

      

       Character sets are currently not implemented as schema objects, so this column is null
      

     

     
      

       character_set_name sql_identifier
      

      

       Name of the character set
      

     
    
   
  
 

 

  
# column_column_usage

  

   The view column_column_usage identifies all generated
   columns that depend on another base column in the same table.  Only tables
   owned by a currently enabled role are included.
  

  
   
# column_column_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       table_catalog sql_identifier
      

      

       Name of the database containing the table (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema containing the table
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table
      

     

     
      

       column_name sql_identifier
      

      

       Name of the base column that a generated column depends on
      

     

     
      

       dependent_column sql_identifier
      

      

       Name of the generated column
      

     
    
   
  
 

 

  
# column_domain_usage

  

   The view column_domain_usage identifies all
   columns (of a table or a view) that make use of some domain defined
   in the current database and owned by a currently enabled role.
  

  
   
# column_domain_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       domain_catalog sql_identifier
      

      

       Name of the database containing the domain (always the current database)
      

     

     
      

       domain_schema sql_identifier
      

      

       Name of the schema containing the domain
      

     

     
      

       domain_name sql_identifier
      

      

       Name of the domain
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database containing the table (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema containing the table
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column
      

     
    
   
  
 

 

  
# column_options

  

   The view column_options contains all the
   options defined for foreign table columns in the current database.  Only
   those foreign table columns are shown that the current user has access to
   (by way of being the owner or having some privilege).
  

  
   
# column_options Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the foreign table (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the foreign table
      

     

     
      

       table_name sql_identifier
      

      

       Name of the foreign table
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column
      

     

     
      

       option_name sql_identifier
      

      

       Name of an option
      

     

     
      

       option_value character_data
      

      

       Value of the option
      

     
    
   
  
 

 

  
# column_privileges

  

   The view column_privileges identifies all
   privileges granted on columns to a currently enabled role or by a
   currently enabled role.  There is one row for each combination of
   column, grantor, and grantee.
  

  

   If a privilege has been granted on an entire table, it will show up in
   this view as a grant for each column, but only for the
   privilege types where column granularity is possible:
   SELECT, INSERT,
   UPDATE, REFERENCES.
  

  
   
# column_privileges Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       Name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       Name of the role that the privilege was granted to
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table that contains the column (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table that contains the column
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table that contains the column
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column
      

     

     
      

       privilege_type character_data
      

      

       Type of the privilege: SELECT,
       INSERT, UPDATE, or
       REFERENCES
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     
    
   
  
 

 

  
# column_udt_usage

  

   The view column_udt_usage identifies all columns
   that use data types owned by a currently enabled role.  Note that in
   PostgreSQL, built-in data types behave
   like user-defined types, so they are included here as well.  See
   also  for details.
  

  
   
# column_udt_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       udt_catalog sql_identifier
      

      

       Name of the database that the column data type (the underlying
       type of the domain, if applicable) is defined in (always the
       current database)
      

     

     
      

       udt_schema sql_identifier
      

      

       Name of the schema that the column data type (the underlying
       type of the domain, if applicable) is defined in
      

     

     
      

       udt_name sql_identifier
      

      

       Name of the column data type (the underlying type of the
       domain, if applicable)
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database containing the table (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema containing the table
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column
      

     
    
   
  
 

 

  
# columns

  

   The view columns contains information about all
   table columns (or view columns) in the database.  System columns
   (ctid, etc.) are not included.  Only those columns are
   shown that the current user has access to (by way of being the
   owner or having some privilege).
  

  
   
# columns Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       table_catalog sql_identifier
      

      

       Name of the database containing the table (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema containing the table
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column
      

     

     
      

       ordinal_position cardinal_number
      

      

       Ordinal position of the column within the table (count starts at 1)
      

     

     
      

       column_default character_data
      

      

       Default expression of the column
      

     

     
      

       is_nullable yes_or_no
      

      

       YES if the column is possibly nullable,
       NO if it is known not nullable.  A not-null
       constraint is one way a column can be known not nullable, but
       there can be others.
      

     

     
      

       data_type character_data
      

      

       Data type of the column, if it is a built-in type, or
       ARRAY if it is some array (in that case, see
       the view element_types), else
       USER-DEFINED (in that case, the type is
       identified in udt_name and associated
       columns).  If the column is based on a domain, this column
       refers to the type underlying the domain (and the domain is
       identified in domain_name and associated
       columns).
      

     

     
      

       character_maximum_length cardinal_number
      

      

       If data_type identifies a character or bit
       string type, the declared maximum length; null for all other
       data types or if no maximum length was declared.
      

     

     
      

       character_octet_length cardinal_number
      

      

       If data_type identifies a character type,
       the maximum possible length in octets (bytes) of a datum; null
       for all other data types.  The maximum octet length depends on
       the declared character maximum length (see above) and the
       server encoding.
      

     

     
      

       numeric_precision cardinal_number
      

      

       If data_type identifies a numeric type, this
       column contains the (declared or implicit) precision of the
       type for this column.  The precision indicates the number of
       significant digits.  It can be expressed in decimal (base 10)
       or binary (base 2) terms, as specified in the column
       numeric_precision_radix.  For all other data
       types, this column is null.
      

     

     
      

       numeric_precision_radix cardinal_number
      

      

       If data_type identifies a numeric type, this
       column indicates in which base the values in the columns
       numeric_precision and
       numeric_scale are expressed.  The value is
       either 2 or 10.  For all other data types, this column is null.
      

     

     
      

       numeric_scale cardinal_number
      

      

       If data_type identifies an exact numeric
       type, this column contains the (declared or implicit) scale of
       the type for this column.  The scale indicates the number of
       significant digits to the right of the decimal point.  It can
       be expressed in decimal (base 10) or binary (base 2) terms, as
       specified in the column
       numeric_precision_radix.  For all other data
       types, this column is null.
      

     

     
      

       datetime_precision cardinal_number
      

      

       If data_type identifies a date, time,
       timestamp, or interval type, this column contains the (declared
       or implicit) fractional seconds precision of the type for this
       column, that is, the number of decimal digits maintained
       following the decimal point in the seconds value.  For all
       other data types, this column is null.
      

     

     
      

       interval_type character_data
      

      

       If data_type identifies an interval type,
       this column contains the specification which fields the
       intervals include for this column, e.g., YEAR TO
       MONTH, DAY TO SECOND, etc.  If no
       field restrictions were specified (that is, the interval
       accepts all fields), and for all other data types, this field
       is null.
      

     

     
      

       interval_precision cardinal_number
      

      

       Applies to a feature not available
       in PostgreSQL
       (see datetime_precision for the fractional
       seconds precision of interval type columns)
      

     

     
      

       character_set_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       collation_catalog sql_identifier
      

      

       Name of the database containing the collation of the column
       (always the current database), null if default or the data type
       of the column is not collatable
      

     

     
      

       collation_schema sql_identifier
      

      

       Name of the schema containing the collation of the column, null
       if default or the data type of the column is not collatable
      

     

     
      

       collation_name sql_identifier
      

      

       Name of the collation of the column, null if default or the
       data type of the column is not collatable
      

     

     
      

       domain_catalog sql_identifier
      

      

       If the column has a domain type, the name of the database that
       the domain is defined in (always the current database), else
       null.
      

     

     
      

       domain_schema sql_identifier
      

      

       If the column has a domain type, the name of the schema that
       the domain is defined in, else null.
      

     

     
      

       domain_name sql_identifier
      

      

       If the column has a domain type, the name of the domain, else null.
      

     

     
      

       udt_catalog sql_identifier
      

      

       Name of the database that the column data type (the underlying
       type of the domain, if applicable) is defined in (always the
       current database)
      

     

     
      

       udt_schema sql_identifier
      

      

       Name of the schema that the column data type (the underlying
       type of the domain, if applicable) is defined in
      

     

     
      

       udt_name sql_identifier
      

      

       Name of the column data type (the underlying type of the
       domain, if applicable)
      

     

     
      

       scope_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       maximum_cardinality cardinal_number
      

      

       Always null, because arrays always have unlimited maximum cardinality in PostgreSQL
      

     

     
      

       dtd_identifier sql_identifier
      

      

       An identifier of the data type descriptor of the column, unique
       among the data type descriptors pertaining to the table.  This
       is mainly useful for joining with other instances of such
       identifiers.  (The specific format of the identifier is not
       defined and not guaranteed to remain the same in future
       versions.)
      

     

     
      

       is_self_referencing yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       is_identity yes_or_no
      

      

       If the column is an identity column, then YES,
       else NO.
      

     

     
      

       identity_generation character_data
      

      

       If the column is an identity column, then ALWAYS
       or BY DEFAULT, reflecting the definition of the
       column.
      

     

     
      

       identity_start character_data
      

      

       If the column is an identity column, then the start value of the
       internal sequence, else null.
      

     

     
      

       identity_increment character_data
      

      

       If the column is an identity column, then the increment of the internal
       sequence, else null.
      

     

     
      

       identity_maximum character_data
      

      

       If the column is an identity column, then the maximum value of the
       internal sequence, else null.
      

     

     
      

       identity_minimum character_data
      

      

       If the column is an identity column, then the minimum value of the
       internal sequence, else null.
      

     

     
      

       identity_cycle yes_or_no
      

      

       If the column is an identity column, then YES if the
       internal sequence cycles or NO if it does not;
       otherwise null.
      

     

     
      

       is_generated character_data
      

      

       If the column is a generated column, then ALWAYS,
       else NEVER.
      

     

     
      

       generation_expression character_data
      

      

       If the column is a generated column, then the generation expression,
       else null.
      

     

     
      

       is_updatable yes_or_no
      

      

       YES if the column is updatable,
       NO if not (Columns in base tables are always
       updatable, columns in views not necessarily)
      

     
    
   
  

  

   Since data types can be defined in a variety of ways in SQL, and
   PostgreSQL contains additional ways to
   define data types, their representation in the information schema
   can be somewhat difficult.  The column data_type
   is supposed to identify the underlying built-in type of the column.
   In PostgreSQL, this means that the type
   is defined in the system catalog schema
   pg_catalog.  This column might be useful if the
   application can handle the well-known built-in types specially (for
   example, format the numeric types differently or use the data in
   the precision columns).  The columns udt_name,
   udt_schema, and udt_catalog
   always identify the underlying data type of the column, even if the
   column is based on a domain.  (Since
   PostgreSQL treats built-in types like
   user-defined types, built-in types appear here as well.  This is an
   extension of the SQL standard.)  These columns should be used if an
   application wants to process data differently according to the
   type, because in that case it wouldn't matter if the column is
   really based on a domain.  If the column is based on a domain, the
   identity of the domain is stored in the columns
   domain_name, domain_schema,
   and domain_catalog.  If you want to pair up
   columns with their associated data types and treat domains as
   separate types, you could write coalesce(domain_name,
   udt_name), etc.
  

 

 

  
# constraint_column_usage

  

   The view constraint_column_usage identifies all
   columns in the current database that are used by some constraint.
   Only those columns are shown that are contained in a table owned by
   a currently enabled role.  For a check constraint, this view
   identifies the columns that are used in the check expression.  For a
   not-null constraint, this view identifies the column that the constraint is
   defined on.  For
   a foreign key constraint, this view identifies the columns that the
   foreign key references.  For a unique or primary key constraint,
   this view identifies the constrained columns.
  

  
   
# constraint_column_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table that contains the
       column that is used by some constraint (always the current
       database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table that contains the
       column that is used by some constraint
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table that contains the column that is used by some
       constraint
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column that is used by some constraint
      

     

     
      

       constraint_catalog sql_identifier
      

      

       Name of the database that contains the constraint (always the current database)
      

     

     
      

       constraint_schema sql_identifier
      

      

       Name of the schema that contains the constraint
      

     

     
      

       constraint_name sql_identifier
      

      

       Name of the constraint
      

     
    
   
  
 

 

  
# constraint_table_usage

  

   The view constraint_table_usage identifies all
   tables in the current database that are used by some constraint and
   are owned by a currently enabled role.  (This is different from the
   view table_constraints, which identifies all
   table constraints along with the table they are defined on.)  For a
   foreign key constraint, this view identifies the table that the
   foreign key references.  For a unique or primary key constraint,
   this view simply identifies the table the constraint belongs to.
   Check constraints and not-null constraints are not included in this
   view.
  

  
   
# constraint_table_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table that is used by
       some constraint (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table that is used by some
       constraint
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table that is used by some constraint
      

     

     
      

       constraint_catalog sql_identifier
      

      

       Name of the database that contains the constraint (always the current database)
      

     

     
      

       constraint_schema sql_identifier
      

      

       Name of the schema that contains the constraint
      

     

     
      

       constraint_name sql_identifier
      

      

       Name of the constraint
      

     
    
   
  
 

 

  
# data_type_privileges

  

   The view data_type_privileges identifies all
   data type descriptors that the current user has access to, by way
   of being the owner of the described object or having some privilege
   for it.  A data type descriptor is generated whenever a data type
   is used in the definition of a table column, a domain, or a
   function (as parameter or return type) and stores some information
   about how the data type is used in that instance (for example, the
   declared maximum length, if applicable).  Each data type
   descriptor is assigned an arbitrary identifier that is unique
   among the data type descriptor identifiers assigned for one object
   (table, domain, function).  This view is probably not useful for
   applications, but it is used to define some other views in the
   information schema.
  

  
   
# data_type_privileges Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       object_catalog sql_identifier
      

      

       Name of the database that contains the described object (always the current database)
      

     

     
      

       object_schema sql_identifier
      

      

       Name of the schema that contains the described object
      

     

     
      

       object_name sql_identifier
      

      

       Name of the described object
      

     

     
      

       object_type character_data
      

      

       The type of the described object: one of
       TABLE (the data type descriptor pertains to
       a column of that table), DOMAIN (the data
       type descriptors pertains to that domain),
       ROUTINE (the data type descriptor pertains
       to a parameter or the return data type of that function).
      

     

     
      

       dtd_identifier sql_identifier
      

      

       The identifier of the data type descriptor, which is unique
       among the data type descriptors for that same object.
      

     
    
   
  
 

 

  
# domain_constraints

  

   The view domain_constraints contains all constraints
   belonging to domains defined in the current database.  Only those domains
   are shown that the current user has access to (by way of being the owner or
   having some privilege).
  

  
   
# domain_constraints Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       constraint_catalog sql_identifier
      

      

       Name of the database that contains the constraint (always the current database)
      

     

     
      

       constraint_schema sql_identifier
      

      

       Name of the schema that contains the constraint
      

     

     
      

       constraint_name sql_identifier
      

      

       Name of the constraint
      

     

     
      

       domain_catalog sql_identifier
      

      

       Name of the database that contains the domain (always the current database)
      

     

     
      

       domain_schema sql_identifier
      

      

       Name of the schema that contains the domain
      

     

     
      

       domain_name sql_identifier
      

      

       Name of the domain
      

     

     
      

       is_deferrable yes_or_no
      

      

       YES if the constraint is deferrable, NO if not
      

     

     
      

       initially_deferred yes_or_no
      

      

       YES if the constraint is deferrable and initially deferred, NO if not
      

     
    
   
  
 

 

  
# domain_udt_usage

  

   The view domain_udt_usage identifies all domains
   that are based on data types owned by a currently enabled role.
   Note that in PostgreSQL, built-in data
   types behave like user-defined types, so they are included here as
   well.
  

  
   
# domain_udt_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       udt_catalog sql_identifier
      

      

       Name of the database that the domain data type is defined in (always the current database)
      

     

     
      

       udt_schema sql_identifier
      

      

       Name of the schema that the domain data type is defined in
      

     

     
      

       udt_name sql_identifier
      

      

       Name of the domain data type
      

     

     
      

       domain_catalog sql_identifier
      

      

       Name of the database that contains the domain (always the current database)
      

     

     
      

       domain_schema sql_identifier
      

      

       Name of the schema that contains the domain
      

     

     
      

       domain_name sql_identifier
      

      

       Name of the domain
      

     
    
   
  
 

 

  
# domains

  

   The view domains contains all
   domains defined in the
   current database.  Only those domains are shown that the current user has
   access to (by way of being the owner or having some privilege).
  

  
   
# domains Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       domain_catalog sql_identifier
      

      

       Name of the database that contains the domain (always the current database)
      

     

     
      

       domain_schema sql_identifier
      

      

       Name of the schema that contains the domain
      

     

     
      

       domain_name sql_identifier
      

      

       Name of the domain
      

     

     
      

       data_type character_data
      

      

       Data type of the domain, if it is a built-in type, or
       ARRAY if it is some array (in that case, see
       the view element_types), else
       USER-DEFINED (in that case, the type is
       identified in udt_name and associated
       columns).
      

     

     
      

       character_maximum_length cardinal_number
      

      

       If the domain has a character or bit string type, the declared
       maximum length; null for all other data types or if no maximum
       length was declared.
      

     

     
      

       character_octet_length cardinal_number
      

      

       If the domain has a character type, the maximum possible length
       in octets (bytes) of a datum; null for all other data types.
       The maximum octet length depends on the declared character
       maximum length (see above) and the server encoding.
      

     

     
      

       character_set_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       collation_catalog sql_identifier
      

      

       Name of the database containing the collation of the domain
       (always the current database), null if default or the data type
       of the domain is not collatable
      

     

     
      

       collation_schema sql_identifier
      

      

       Name of the schema containing the collation of the domain, null
       if default or the data type of the domain is not collatable
      

     

     
      

       collation_name sql_identifier
      

      

       Name of the collation of the domain, null if default or the
       data type of the domain is not collatable
      

     

     
      

       numeric_precision cardinal_number
      

      

       If the domain has a numeric type, this column contains the
       (declared or implicit) precision of the type for this domain.
       The precision indicates the number of significant digits.  It
       can be expressed in decimal (base 10) or binary (base 2) terms,
       as specified in the column
       numeric_precision_radix.  For all other data
       types, this column is null.
      

     

     
      

       numeric_precision_radix cardinal_number
      

      

       If the domain has a numeric type, this column indicates in
       which base the values in the columns
       numeric_precision and
       numeric_scale are expressed.  The value is
       either 2 or 10.  For all other data types, this column is null.
      

     

     
      

       numeric_scale cardinal_number
      

      

       If the domain has an exact numeric type, this column contains
       the (declared or implicit) scale of the type for this domain.
       The scale indicates the number of significant digits to the
       right of the decimal point.  It can be expressed in decimal
       (base 10) or binary (base 2) terms, as specified in the column
       numeric_precision_radix.  For all other data
       types, this column is null.
      

     

     
      

       datetime_precision cardinal_number
      

      

       If data_type identifies a date, time,
       timestamp, or interval type, this column contains the (declared
       or implicit) fractional seconds precision of the type for this
       domain, that is, the number of decimal digits maintained
       following the decimal point in the seconds value.  For all
       other data types, this column is null.
      

     

     
      

       interval_type character_data
      

      

       If data_type identifies an interval type,
       this column contains the specification which fields the
       intervals include for this domain, e.g., YEAR TO
       MONTH, DAY TO SECOND, etc.  If no
       field restrictions were specified (that is, the interval
       accepts all fields), and for all other data types, this field
       is null.
      

     

     
      

       interval_precision cardinal_number
      

      

       Applies to a feature not available
       in PostgreSQL
       (see datetime_precision for the fractional
       seconds precision of interval type domains)
      

     

     
      

       domain_default character_data
      

      

       Default expression of the domain
      

     

     
      

       udt_catalog sql_identifier
      

      

       Name of the database that the domain data type is defined in (always the current database)
      

     

     
      

       udt_schema sql_identifier
      

      

       Name of the schema that the domain data type is defined in
      

     

     
      

       udt_name sql_identifier
      

      

       Name of the domain data type
      

     

     
      

       scope_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       maximum_cardinality cardinal_number
      

      

       Always null, because arrays always have unlimited maximum cardinality in PostgreSQL
      

     

     
      

       dtd_identifier sql_identifier
      

      

       An identifier of the data type descriptor of the domain, unique
       among the data type descriptors pertaining to the domain (which
       is trivial, because a domain only contains one data type
       descriptor).  This is mainly useful for joining with other
       instances of such identifiers.  (The specific format of the
       identifier is not defined and not guaranteed to remain the same
       in future versions.)
      

     
    
   
  
 

 

  
# element_types

  

   The view element_types contains the data type
   descriptors of the elements of arrays.  When a table column, composite-type attribute,
   domain, function parameter, or function return value is defined to
   be of an array type, the respective information schema view only
   contains ARRAY in the column
   data_type.  To obtain information on the element
   type of the array, you can join the respective view with this view.
   For example, to show the columns of a table with data types and
   array element types, if applicable, you could do:

```
SELECT c.column_name, c.data_type, e.data_type AS element_type
FROM information_schema.columns c LEFT JOIN information_schema.element_types e
     ON ((c.table_catalog, c.table_schema, c.table_name, 'TABLE', c.dtd_identifier)
       = (e.object_catalog, e.object_schema, e.object_name, e.object_type, e.collection_type_identifier))
WHERE c.table_schema = '...' AND c.table_name = '...'
ORDER BY c.ordinal_position;
```

   This view only includes objects that the current user has access
   to, by way of being the owner or having some privilege.
  

  
   
# element_types Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       object_catalog sql_identifier
      

      

       Name of the database that contains the object that uses the
       array being described (always the current database)
      

     

     
      

       object_schema sql_identifier
      

      

       Name of the schema that contains the object that uses the array
       being described
      

     

     
      

       object_name sql_identifier
      

      

       Name of the object that uses the array being described
      

     

     
      

       object_type character_data
      

      

       The type of the object that uses the array being described: one
       of TABLE (the array is used by a column of
       that table), USER-DEFINED TYPE (the array is
       used by an attribute of that composite type),
       DOMAIN (the array is used by that domain),
       ROUTINE (the array is used by a parameter or
       the return data type of that function).
      

     

     
      

       collection_type_identifier sql_identifier
      

      

       The identifier of the data type descriptor of the array being
       described.  Use this to join with the
       dtd_identifier columns of other information
       schema views.
      

     

     
      

       data_type character_data
      

      

       Data type of the array elements, if it is a built-in type, else
       USER-DEFINED (in that case, the type is
       identified in udt_name and associated
       columns).
      

     

     
      

       character_maximum_length cardinal_number
      

      

       Always null, since this information is not applied to array element data types in PostgreSQL
      

     

     
      

       character_octet_length cardinal_number
      

      

       Always null, since this information is not applied to array element data types in PostgreSQL
      

     

     
      

       character_set_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       collation_catalog sql_identifier
      

      

       Name of the database containing the collation of the element
       type (always the current database), null if default or the data
       type of the element is not collatable
      

     

     
      

       collation_schema sql_identifier
      

      

       Name of the schema containing the collation of the element
       type, null if default or the data type of the element is not
       collatable
      

     

     
      

       collation_name sql_identifier
      

      

       Name of the collation of the element type, null if default or
       the data type of the element is not collatable
      

     

     
      

       numeric_precision cardinal_number
      

      

       Always null, since this information is not applied to array element data types in PostgreSQL
      

     

     
      

       numeric_precision_radix cardinal_number
      

      

       Always null, since this information is not applied to array element data types in PostgreSQL
      

     

     
      

       numeric_scale cardinal_number
      

      

       Always null, since this information is not applied to array element data types in PostgreSQL
      

     

     
      

       datetime_precision cardinal_number
      

      

       Always null, since this information is not applied to array element data types in PostgreSQL
      

     

     
      

       interval_type character_data
      

      

       Always null, since this information is not applied to array element data types in PostgreSQL
      

     

     
      

       interval_precision cardinal_number
      

      

       Always null, since this information is not applied to array element data types in PostgreSQL
      

     

     
      

       udt_catalog sql_identifier
      

      

       Name of the database that the data type of the elements is
       defined in (always the current database)
      

     

     
      

       udt_schema sql_identifier
      

      

       Name of the schema that the data type of the elements is
       defined in
      

     

     
      

       udt_name sql_identifier
      

      

       Name of the data type of the elements
      

     

     
      

       scope_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       maximum_cardinality cardinal_number
      

      

       Always null, because arrays always have unlimited maximum cardinality in PostgreSQL
      

     

     
      

       dtd_identifier sql_identifier
      

      

       An identifier of the data type descriptor of the element.  This
       is currently not useful.
      

     
    
   
  
 

 

  
# enabled_roles

  

   The view enabled_roles identifies the currently
   enabled roles.  The enabled roles are recursively
   defined as the current user together with all roles that have been
   granted to the enabled roles with automatic inheritance.  In other
   words, these are all roles that the current user has direct or
   indirect, automatically inheriting membership in.
   enabled role
   roleenabled
  

  

   For permission checking, the set of applicable roles
   is applied, which can be broader than the set of enabled roles.  So
   generally, it is better to use the view
   applicable_roles instead of this one; See
    for details on
   applicable_roles view.
  

  
   
# enabled_roles Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       role_name sql_identifier
      

      

       Name of a role
      

     
    
   
  
 

 

  
# foreign_data_wrapper_options

  

   The view foreign_data_wrapper_options contains
   all the options defined for foreign-data wrappers in the current
   database.  Only those foreign-data wrappers are shown that the
   current user has access to (by way of being the owner or having
   some privilege).
  

  
   
# foreign_data_wrapper_options Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       foreign_data_wrapper_catalog sql_identifier
      

      

       Name of the database that the foreign-data wrapper is defined in (always the current database)
      

     

     
      

       foreign_data_wrapper_name sql_identifier
      

      

       Name of the foreign-data wrapper
      

     

     
      

       option_name sql_identifier
      

      

       Name of an option
      

     

     
      

       option_value character_data
      

      

       Value of the option
      

     
    
   
  
 

 

  
# foreign_data_wrappers

  

   The view foreign_data_wrappers contains all
   foreign-data wrappers defined in the current database.  Only those
   foreign-data wrappers are shown that the current user has access to
   (by way of being the owner or having some privilege).
  

  
   
# foreign_data_wrappers Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       foreign_data_wrapper_catalog sql_identifier
      

      

       Name of the database that contains the foreign-data
       wrapper (always the current database)
      

     

     
      

       foreign_data_wrapper_name sql_identifier
      

      

       Name of the foreign-data wrapper
      

     

     
      

       authorization_identifier sql_identifier
      

      

       Name of the owner of the foreign server
      

     

     
      

       library_name character_data
      

      

       File name of the library that implementing this foreign-data wrapper
      

     

     
      

       foreign_data_wrapper_language character_data
      

      

       Language used to implement this foreign-data wrapper
      

     
    
   
  
 

 

  
# foreign_server_options

  

   The view foreign_server_options contains all the
   options defined for foreign servers in the current database.  Only
   those foreign servers are shown that the current user has access to
   (by way of being the owner or having some privilege).
  

  
   
# foreign_server_options Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       foreign_server_catalog sql_identifier
      

      

       Name of the database that the foreign server is defined in (always the current database)
      

     

     
      

       foreign_server_name sql_identifier
      

      

       Name of the foreign server
      

     

     
      

       option_name sql_identifier
      

      

       Name of an option
      

     

     
      

       option_value character_data
      

      

       Value of the option
      

     
    
   
  
 

 

  
# foreign_servers

  

   The view foreign_servers contains all foreign
   servers defined in the current database.  Only those foreign
   servers are shown that the current user has access to (by way of
   being the owner or having some privilege).
  

  
   
# foreign_servers Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       foreign_server_catalog sql_identifier
      

      

       Name of the database that the foreign server is defined in (always the current database)
      

     

     
      

       foreign_server_name sql_identifier
      

      

       Name of the foreign server
      

     

     
      

       foreign_data_wrapper_catalog sql_identifier
      

      

       Name of the database that contains the foreign-data
       wrapper used by the foreign server (always the current database)
      

     

     
      

       foreign_data_wrapper_name sql_identifier
      

      

       Name of the foreign-data wrapper used by the foreign server
      

     

     
      

       foreign_server_type character_data
      

      

       Foreign server type information, if specified upon creation
      

     

     
      

       foreign_server_version character_data
      

      

       Foreign server version information, if specified upon creation
      

     

     
      

       authorization_identifier sql_identifier
      

      

       Name of the owner of the foreign server
      

     
    
   
  
 

 

  
# foreign_table_options

  

   The view foreign_table_options contains all the
   options defined for foreign tables in the current database.  Only
   those foreign tables are shown that the current user has access to
   (by way of being the owner or having some privilege).
  

  
   
# foreign_table_options Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       foreign_table_catalog sql_identifier
      

      

       Name of the database that contains the foreign table (always the current database)
      

     

     
      

       foreign_table_schema sql_identifier
      

      

       Name of the schema that contains the foreign table
      

     

     
      

       foreign_table_name sql_identifier
      

      

       Name of the foreign table
      

     

     
      

       option_name sql_identifier
      

      

       Name of an option
      

     

     
      

       option_value character_data
      

      

       Value of the option
      

     
    
   
  
 

 

  
# foreign_tables

  

   The view foreign_tables contains all foreign
   tables defined in the current database.  Only those foreign
   tables are shown that the current user has access to (by way of
   being the owner or having some privilege).
  

  
   
# foreign_tables Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       foreign_table_catalog sql_identifier
      

      

       Name of the database that the foreign table is defined in (always the current database)
      

     

     
      

       foreign_table_schema sql_identifier
      

      

       Name of the schema that contains the foreign table
      

     

     
      

       foreign_table_name sql_identifier
      

      

       Name of the foreign table
      

     

     
      

       foreign_server_catalog sql_identifier
      

      

       Name of the database that the foreign server is defined in (always the current database)
      

     

     
      

       foreign_server_name sql_identifier
      

      

       Name of the foreign server
      

     
    
   
  
 

 

  
# key_column_usage

  

   The view key_column_usage identifies all columns
   in the current database that are restricted by some unique, primary
   key, or foreign key constraint.  Check constraints are not included
   in this view.  Only those columns are shown that the current user
   has access to, by way of being the owner or having some privilege.
  

  
   
# key_column_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       constraint_catalog sql_identifier
      

      

       Name of the database that contains the constraint (always the current database)
      

     

     
      

       constraint_schema sql_identifier
      

      

       Name of the schema that contains the constraint
      

     

     
      

       constraint_name sql_identifier
      

      

       Name of the constraint
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table that contains the
       column that is restricted by this constraint (always the
       current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table that contains the
       column that is restricted by this constraint
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table that contains the column that is restricted
       by this constraint
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column that is restricted by this constraint
      

     

     
      

       ordinal_position cardinal_number
      

      

       Ordinal position of the column within the constraint key (count
       starts at 1)
      

     

     
      

       position_in_unique_constraint cardinal_number
      

      

       For a foreign-key constraint, ordinal position of the referenced
       column within its unique constraint (count starts at 1);
       otherwise null
      

     
    
   
  
 

 

  
# parameters

  

   The view parameters contains information about
   the parameters (arguments) of all functions in the current database.
   Only those functions are shown that the current user has access to
   (by way of being the owner or having some privilege).
  

  
   
# parameters Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the function.  See  for more information.
      

     

     
      

       ordinal_position cardinal_number
      

      

       Ordinal position of the parameter in the argument list of the
       function (count starts at 1)
      

     

     
      

       parameter_mode character_data
      

      

       IN for input parameter,
       OUT for output parameter,
       and INOUT for input/output parameter.
      

     

     
      

       is_result yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       as_locator yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       parameter_name sql_identifier
      

      

       Name of the parameter, or null if the parameter has no name
      

     

     
      

       data_type character_data
      

      

       Data type of the parameter, if it is a built-in type, or
       ARRAY if it is some array (in that case, see
       the view element_types), else
       USER-DEFINED (in that case, the type is
       identified in udt_name and associated
       columns).
      

     

     
      

       character_maximum_length cardinal_number
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       character_octet_length cardinal_number
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       character_set_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       collation_catalog sql_identifier
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       collation_schema sql_identifier
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       collation_name sql_identifier
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       numeric_precision cardinal_number
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       numeric_precision_radix cardinal_number
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       numeric_scale cardinal_number
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       datetime_precision cardinal_number
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       interval_type character_data
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       interval_precision cardinal_number
      

      

       Always null, since this information is not applied to parameter data types in PostgreSQL
      

     

     
      

       udt_catalog sql_identifier
      

      

       Name of the database that the data type of the parameter is
       defined in (always the current database)
      

     

     
      

       udt_schema sql_identifier
      

      

       Name of the schema that the data type of the parameter is
       defined in
      

     

     
      

       udt_name sql_identifier
      

      

       Name of the data type of the parameter
      

     

     
      

       scope_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       maximum_cardinality cardinal_number
      

      

       Always null, because arrays always have unlimited maximum cardinality in PostgreSQL
      

     

     
      

       dtd_identifier sql_identifier
      

      

       An identifier of the data type descriptor of the parameter,
       unique among the data type descriptors pertaining to the
       function.  This is mainly useful for joining with other
       instances of such identifiers.  (The specific format of the
       identifier is not defined and not guaranteed to remain the same
       in future versions.)
      

     

     
      

       parameter_default character_data
      

      

       The default expression of the parameter, or null if none or if the
       function is not owned by a currently enabled role.
      

     
    
   
  
 

 

  
# pg_edge_table_components

  

   The view pg_edge_table_components identifies which
   columns are part of the source or destination vertex keys, as well as their
   corresponding columns in the vertex tables being linked to, in the edge
   tables of property graphs defined in the current database.  Only those
   property graphs are shown that the current user has access to (by way of
   being the owner or having some privilege).
  

  

   The source and destination vertex links of edge tables are specified in
   CREATE PROPERTY GRAPH and default to foreign keys in
   certain cases.
  

  
   
# pg_edge_table_components Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       property_graph_catalog sql_identifier
      

      

       Name of the database that contains the property graph (always the current database)
      

     

     
      

       property_graph_schema sql_identifier
      

      

       Name of the schema that contains the property graph
      

     

     
      

       property_graph_name sql_identifier
      

      

       Name of the property graph
      

     

     
      

       edge_table_alias sql_identifier
      

      

       The element table alias of the edge table being described
      

     

     
      

       vertex_table_alias sql_identifier
      

      

       The element table alias of the source or destination vertex table being linked to
      

     

     
      

       edge_end character_data
      

      

       Either SOURCE or DESTINATION;
       specifies which edge link is being described.
      

     

     
      

       edge_table_column_name sql_identifier
      

      

       Name of the column that is part of the source or destination vertex key in this edge table
      

     

     
      

       vertex_table_column_name sql_identifier
      

      

       Name of the column that is part of the key in the source or destination
       vertex table being linked to
      

     

     
      

       ordinal_position cardinal_number
      

      

       Ordinal position of the columns within the key (count starts at 1)
      

     
    
   
  
 

 

  
# pg_element_table_key_columns

  

   The view pg_element_table_key_columns identifies which
   columns are part of the keys of the element tables of property graphs defined
   in the current database.  Only those property graphs are shown that the
   current user has access to (by way of being the owner or having some
   privilege).
  

  

   The key of an element table uniquely identifies the rows in it.  It is
   either specified using the KEY clause in CREATE
   PROPERTY GRAPH or defaults to the primary key.
  

  
   
# pg_element_table_key_columns Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       property_graph_catalog sql_identifier
      

      

       Name of the database that contains the property graph (always the current database)
      

     

     
      

       property_graph_schema sql_identifier
      

      

       Name of the schema that contains the property graph
      

     

     
      

       property_graph_name sql_identifier
      

      

       Name of the property graph
      

     

     
      

       element_table_alias sql_identifier
      

      

       Element table alias (unique identifier of an element table within a
       property graph)
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column that is part of the key
      

     

     
      

       ordinal_position cardinal_number
      

      

       Ordinal position of the column within the key (count starts at 1)
      

     
    
   
  
 

 

  
# pg_element_table_labels

  

   The view pg_element_table_labels shows which labels are
   defined on the element tables of property graphs defined in the current
   database.  Only those property graphs are shown that the current user has
   access to (by way of being the owner or having some privilege).
  

  
   
# pg_element_table_labels Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       property_graph_catalog sql_identifier
      

      

       Name of the database that contains the property graph (always the current database)
      

     

     
      

       property_graph_schema sql_identifier
      

      

       Name of the schema that contains the property graph
      

     

     
      

       property_graph_name sql_identifier
      

      

       Name of the property graph
      

     

     
      

       element_table_alias sql_identifier
      

      

       Element table alias (unique identifier of an element table within a
       property graph)
      

     

     
      

       label_name sql_identifier
      

      

       Name of the label
      

     
    
   
  
 

 

  
# pg_element_table_properties

  

   The view pg_element_table_properties shows the definitions
   of the properties for the element tables of property graphs defined in the
   current database.  Only those property graphs are shown that the current user
   has access to (by way of being the owner or having some privilege).
  

  
   
# pg_element_table_properties Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       property_graph_catalog sql_identifier
      

      

       Name of the database that contains the property graph (always the current database)
      

     

     
      

       property_graph_schema sql_identifier
      

      

       Name of the schema that contains the property graph
      

     

     
      

       property_graph_name sql_identifier
      

      

       Name of the property graph
      

     

     
      

       element_table_alias sql_identifier
      

      

       Element table alias (unique identifier of an element table within a
       property graph)
      

     

     
      

       property_name sql_identifier
      

      

       Name of the property
      

     

     
      

       property_expression character_data
      

      

       Expression of the property definition for this element table
      

     
    
   
  
 

 

  
# pg_element_tables

  

   The view pg_element_tables contains information about
   the element tables of property graphs defined in the current database.
   Only those property graphs are shown that the current user has access to
   (by way of being the owner or having some privilege).
  

  
   
# pg_element_tables Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       property_graph_catalog sql_identifier
      

      

       Name of the database that contains the property graph (always the current database)
      

     

     
      

       property_graph_schema sql_identifier
      

      

       Name of the schema that contains the property graph
      

     

     
      

       property_graph_name sql_identifier
      

      

       Name of the property graph
      

     

     
      

       element_table_alias sql_identifier
      

      

       Element table alias (unique identifier of an element table within a
       property graph)
      

     

     
      

       element_table_kind character_data
      

      

       The kind of the element table: EDGE or VERTEX
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the referenced table (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the referenced table
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table being referenced by the element table definition
      

     

     
      

       element_table_definition character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     
    
   
  
 

 

  
# pg_label_properties

  

   The view pg_label_properties shows which properties are
   defined on labels defined in property graphs defined in the current
   database.  Only those property graphs are shown that the current user has
   access to (by way of being the owner or having some privilege).
  

  
   
# pg_label_properties Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       property_graph_catalog sql_identifier
      

      

       Name of the database that contains the property graph (always the current database)
      

     

     
      

       property_graph_schema sql_identifier
      

      

       Name of the schema that contains the property graph
      

     

     
      

       property_graph_name sql_identifier
      

      

       Name of the property graph
      

     

     
      

       label_name sql_identifier
      

      

       Name of the label
      

     

     
      

       property_name sql_identifier
      

      

       Name of the property
      

     
    
   
  
 

 

  
# pg_labels

  

   The view pg_labels contains all the labels defined in
   property graphs defined in the current database.  Only those property
   graphs are shown that the current user has access to (by way of being the
   owner or having some privilege).
  

  
   
# pg_labels Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       property_graph_catalog sql_identifier
      

      

       Name of the database that contains the property graph (always the current database)
      

     

     
      

       property_graph_schema sql_identifier
      

      

       Name of the schema that contains the property graph
      

     

     
      

       property_graph_name sql_identifier
      

      

       Name of the property graph
      

     

     
      

       label_name sql_identifier
      

      

       Name of the label
      

     
    
   
  
 

 

  
# pg_property_data_types

  

   The view pg_property_data_types shows the data types of
   the properties in property graphs defined in the current database.  Only
   those property graphs are shown that the current user has access to (by way
   of being the owner or having some privilege).
  

  
   
# pg_property_data_types Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       property_graph_catalog sql_identifier
      

      

       Name of the database that contains the property graph (always the current database)
      

     

     
      

       property_graph_schema sql_identifier
      

      

       Name of the schema that contains the property graph
      

     

     
      

       property_graph_name sql_identifier
      

      

       Name of the property graph
      

     

     
      

       property_name sql_identifier
      

      

       Name of the property
      

     

     
      

       data_type character_data
      

      

       Data type of the property, if it is a built-in type, or
       ARRAY if it is some array (in that case, see the
       view element_types), else
       USER-DEFINED (in that case, the type is identified
       in attribute_udt_name and associated columns).
       

     

     
      

       character_maximum_length cardinal_number
      

      

       If data_type identifies a character or bit
       string type, the declared maximum length; null for all other
       data types or if no maximum length was declared.
      

     

     
      

       character_octet_length cardinal_number
      

      

       If data_type identifies a character type,
       the maximum possible length in octets (bytes) of a datum; null
       for all other data types.  The maximum octet length depends on
       the declared character maximum length (see above) and the
       server encoding.
      

     

     
      

       character_set_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       collation_catalog sql_identifier
      

      

       Name of the database containing the collation of the property (always
       the current database), null if default or the data type of the
       property is not collatable
      

     

     
      

       collation_schema sql_identifier
      

      

       Name of the schema containing the collation of the property, null if
       default or the data type of the property is not collatable
      

     

     
      

       collation_name sql_identifier
      

      

       Name of the collation of the property, null if default or the data type
       of the property is not collatable
      

     

     
      

       numeric_precision cardinal_number
      

      

       If data_type identifies a numeric type, this
       column contains the (declared or implicit) precision of the
       type for this attribute.  The precision indicates the number of
       significant digits.  It can be expressed in decimal (base 10)
       or binary (base 2) terms, as specified in the column
       numeric_precision_radix.  For all other data
       types, this column is null.
      

     

     
      

       numeric_precision_radix cardinal_number
      

      

       If data_type identifies a numeric type, this
       column indicates in which base the values in the columns
       numeric_precision and
       numeric_scale are expressed.  The value is
       either 2 or 10.  For all other data types, this column is null.
      

     

     
      

       numeric_scale cardinal_number
      

      

       If data_type identifies an exact numeric
       type, this column contains the (declared or implicit) scale of
       the type for this attribute.  The scale indicates the number of
       significant digits to the right of the decimal point.  It can
       be expressed in decimal (base 10) or binary (base 2) terms, as
       specified in the column
       numeric_precision_radix.  For all other data
       types, this column is null.
      

     

     
      

       datetime_precision cardinal_number
      

      

       If data_type identifies a date, time,
       timestamp, or interval type, this column contains the (declared
       or implicit) fractional seconds precision of the type for this
       attribute, that is, the number of decimal digits maintained
       following the decimal point in the seconds value.  For all
       other data types, this column is null.
      

     

     
      

       interval_type character_data
      

      

       If data_type identifies an interval type,
       this column contains the specification which fields the
       intervals include for this attribute, e.g., YEAR TO
       MONTH, DAY TO SECOND, etc.  If no
       field restrictions were specified (that is, the interval
       accepts all fields), and for all other data types, this field
       is null.
      

     

     
      

       interval_precision cardinal_number
      

      

       Applies to a feature not available
       in PostgreSQL
       (see datetime_precision for the fractional
       seconds precision of interval type properties)
      

     

     
      

       user_defined_type_catalog sql_identifier
      

      

       Name of the database that the property data type is defined in
       (always the current database)
      

     

     
      

       user_defined_type_schema sql_identifier
      

      

       Name of the schema that the property data type is defined in
      

     

     
      

       user_defined_type_name sql_identifier
      

      

       Name of the property data type
      

     

     
      

       scope_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       maximum_cardinality cardinal_number
      

      

       Always null, because arrays always have unlimited maximum cardinality in PostgreSQL
      

     

     
      

       dtd_identifier sql_identifier
      

      

       An identifier of the data type descriptor of the property, unique
       among the data type descriptors pertaining to the property graph.  This
       is mainly useful for joining with other instances of such
       identifiers.  (The specific format of the identifier is not
       defined and not guaranteed to remain the same in future
       versions.)
      

     
    
   
  
 

 

  
# pg_property_graph_privileges

  

   The view pg_property_graph_privileges identifies all
   privileges granted on property graphs to a currently enabled role or by a
   currently enabled role.  There is one row for each combination of property
   graph, grantor, and grantee.
  

  
   
# pg_property_graph_privileges Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       Name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       Name of the role that the privilege was granted to
      

     

     
      

       property_graph_catalog sql_identifier
      

      

       Name of the database that contains the property graph (always the current database)
      

     

     
      

       property_graph_schema sql_identifier
      

      

       Name of the schema that contains the property graph
      

     

     
      

       property_graph_name sql_identifier
      

      

       Name of the property graph
      

     

     
      

       privilege_type character_data
      

      

       Type of the privilege: SELECT is the only privilege
       type applicable to property graphs.
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     
    
   
  
 

 

  
# property_graphs

  

   The view property_graphs contains all property graphs
   defined in the current database.  Only those property graphs are shown that
   the current user has access to (by way of being the owner or having some
   privilege).
  

  
   
# property_graphs Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       property_graph_catalog sql_identifier
      

      

       Name of the database that contains the property graph (always the current database)
      

     

     
      

       property_graph_schema sql_identifier
      

      

       Name of the schema that contains the property graph
      

     

     
      

       property_graph_name sql_identifier
      

      

       Name of the property graph
      

     
    
   
  
 

 

  
# referential_constraints

  

   The view referential_constraints contains all
   referential (foreign key) constraints in the current database.
   Only those constraints are shown for which the current user has
   write access to the referencing table (by way of being the
   owner or having some privilege other than SELECT).
  

  
   
# referential_constraints Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       constraint_catalog sql_identifier
      

      

       Name of the database containing the constraint (always the current database)
      

     

     
      

       constraint_schema sql_identifier
      

      

       Name of the schema containing the constraint
      

     

     
      

       constraint_name sql_identifier
      

      

       Name of the constraint
      

     

     
      

       unique_constraint_catalog sql_identifier
      

      

       Name of the database that contains the unique or primary key
       constraint that the foreign key constraint references (always
       the current database)
      

     

     
      

       unique_constraint_schema sql_identifier
      

      

       Name of the schema that contains the unique or primary key
       constraint that the foreign key constraint references
      

     

     
      

       unique_constraint_name sql_identifier
      

      

       Name of the unique or primary key constraint that the foreign
       key constraint references
      

     

     
      

       match_option character_data
      

      

       Match option of the foreign key constraint:
       FULL, PARTIAL, or
       NONE.
      

     

     
      

       update_rule character_data
      

      

       Update rule of the foreign key constraint:
       CASCADE, SET NULL,
       SET DEFAULT, RESTRICT, or
       NO ACTION.
      

     

     
      

       delete_rule character_data
      

      

       Delete rule of the foreign key constraint:
       CASCADE, SET NULL,
       SET DEFAULT, RESTRICT, or
       NO ACTION.
      

     
    
   
  

 

 

  
# role_column_grants

  

   The view role_column_grants identifies all
   privileges granted on columns where the grantor or grantee is a
   currently enabled role.  Further information can be found under
   column_privileges.  The only effective
   difference between this view
   and column_privileges is that this view omits
   columns that have been made accessible to the current user by way
   of a grant to PUBLIC.
  

  
   
# role_column_grants Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       Name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       Name of the role that the privilege was granted to
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table that contains the column (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table that contains the column
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table that contains the column
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column
      

     

     
      

       privilege_type character_data
      

      

       Type of the privilege: SELECT,
       INSERT, UPDATE, or
       REFERENCES
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     
    
   
  
 

 

  
# role_routine_grants

  

   The view role_routine_grants identifies all
   privileges granted on functions where the grantor or grantee is a
   currently enabled role.  Further information can be found under
   routine_privileges.  The only effective
   difference between this view
   and routine_privileges is that this view omits
   functions that have been made accessible to the current user by way
   of a grant to PUBLIC.
  

  
   
# role_routine_grants Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       Name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       Name of the role that the privilege was granted to
      

     

     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the function.  See  for more information.
      

     

     
      

       routine_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       routine_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       routine_name sql_identifier
      

      

       Name of the function (might be duplicated in case of overloading)
      

     

     
      

       privilege_type character_data
      

      

       Always EXECUTE (the only privilege type for functions)
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     
    
   
  
 

 

  
# role_table_grants

  

   The view role_table_grants identifies all
   privileges granted on tables or views where the grantor or grantee
   is a currently enabled role.  Further information can be found
   under table_privileges.  The only effective
   difference between this view
   and table_privileges is that this view omits
   tables that have been made accessible to the current user by way of
   a grant to PUBLIC.
  

  
   
# role_table_grants Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       Name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       Name of the role that the privilege was granted to
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table
      

     

     
      

       privilege_type character_data
      

      

       Type of the privilege: SELECT,
       INSERT, UPDATE,
       DELETE, TRUNCATE,
       REFERENCES, or TRIGGER
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     

     
      

       with_hierarchy yes_or_no
      

      

       In the SQL standard, WITH HIERARCHY OPTION
       is a separate (sub-)privilege allowing certain operations on
       table inheritance hierarchies.  In PostgreSQL, this is included
       in the SELECT privilege, so this column
       shows YES if the privilege
       is SELECT, else NO.
      

     
    
   
  
 

 

  
# role_udt_grants

  

   The view role_udt_grants is intended to identify
   USAGE privileges granted on user-defined types
   where the grantor or grantee is a currently enabled role.  Further
   information can be found under
   udt_privileges.  The only effective difference
   between this view and udt_privileges is that
   this view omits objects that have been made accessible to the
   current user by way of a grant to PUBLIC.  Since
   data types do not have real privileges in PostgreSQL, but only an
   implicit grant to PUBLIC, this view is empty.
  

  
   
# role_udt_grants Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       The name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       The name of the role that the privilege was granted to
      

     

     
      

       udt_catalog sql_identifier
      

      

       Name of the database containing the type (always the current database)
      

     

     
      

       udt_schema sql_identifier
      

      

       Name of the schema containing the type
      

     

     
      

       udt_name sql_identifier
      

      

       Name of the type
      

     

     
      

       privilege_type character_data
      

      

       Always TYPE USAGE
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     
    
   
  
 

 

  
# role_usage_grants

  

   The view role_usage_grants identifies
   USAGE privileges granted on various kinds of
   objects where the grantor or grantee is a currently enabled role.
   Further information can be found under
   usage_privileges.  The only effective difference
   between this view and usage_privileges is that
   this view omits objects that have been made accessible to the
   current user by way of a grant to PUBLIC.
  

  
   
# role_usage_grants Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       The name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       The name of the role that the privilege was granted to
      

     

     
      

       object_catalog sql_identifier
      

      

       Name of the database containing the object (always the current database)
      

     

     
      

       object_schema sql_identifier
      

      

       Name of the schema containing the object, if applicable,
       else an empty string
      

     

     
      

       object_name sql_identifier
      

      

       Name of the object
      

     

     
      

       object_type character_data
      

      

       COLLATION or DOMAIN or FOREIGN DATA WRAPPER or FOREIGN SERVER or SEQUENCE
      

     

     
      

       privilege_type character_data
      

      

       Always USAGE
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     
    
   
  
 

 

  
# routine_column_usage

  

   The view routine_column_usage identifies all columns
   that are used by a function or procedure, either in the SQL body or in
   parameter default expressions.  (This only works for unquoted SQL bodies,
   not quoted bodies or functions in other languages.)  A column is only
   included if its table is owned by a currently enabled role.
  

  
   
# routine_column_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the function.  See  for more information.
      

     

     
      

       routine_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       routine_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       routine_name sql_identifier
      

      

       Name of the function (might be duplicated in case of overloading)
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table that is used by the
       function (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table that is used by the function
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table that is used by the function
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column that is used by the function
      

     
    
   
  
 

 

  
# routine_privileges

  

   The view routine_privileges identifies all
   privileges granted on functions to a currently enabled role or by a
   currently enabled role.  There is one row for each combination of function,
   grantor, and grantee.
  

  
   
# routine_privileges Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       Name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       Name of the role that the privilege was granted to
      

     

     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the function.  See  for more information.
      

     

     
      

       routine_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       routine_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       routine_name sql_identifier
      

      

       Name of the function (might be duplicated in case of overloading)
      

     

     
      

       privilege_type character_data
      

      

       Always EXECUTE (the only privilege type for functions)
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     
    
   
  
 

 

  
# routine_routine_usage

  

   The view routine_routine_usage identifies all functions
   or procedures that are used by another (or the same) function or procedure,
   either in the SQL body or in parameter default expressions.  (This only
   works for unquoted SQL bodies, not quoted bodies or functions in other
   languages.)  An entry is included here only if the used function is owned
   by a currently enabled role.  (There is no such restriction on the using
   function.)
  

  

   Note that the entries for both functions in the view refer to the
   specific name of the routine, even though the column names
   are used in a way that is inconsistent with other information schema views
   about routines.  This is per SQL standard, although it is arguably a
   misdesign.  See  for more information
   about specific names.
  

  
   
# routine_routine_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the using function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the using function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the using function.
      

     

     
      

       routine_catalog sql_identifier
      

      

       Name of the database that contains the function that is used by the
       first function (always the current database)
      

     

     
      

       routine_schema sql_identifier
      

      

       Name of the schema that contains the function that is used by the first
       function
      

     

     
      

       routine_name sql_identifier
      

      

       The specific name of the function that is used by the
       first function.
      

     
    
   
  
 

 

  
# routine_sequence_usage

  

   The view routine_sequence_usage identifies all sequences
   that are used by a function or procedure, either in the SQL body or in
   parameter default expressions.  (This only works for unquoted SQL bodies,
   not quoted bodies or functions in other languages.)  A sequence is only
   included if that sequence is owned by a currently enabled role.
  

  
   
# routine_sequence_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the function.  See  for more information.
      

     

     
      

       routine_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       routine_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       routine_name sql_identifier
      

      

       Name of the function (might be duplicated in case of overloading)
      

     

     
      

       schema_catalog sql_identifier
      

      

       Name of the database that contains the sequence that is used by the
       function (always the current database)
      

     

     
      

       sequence_schema sql_identifier
      

      

       Name of the schema that contains the sequence that is used by the function
      

     

     
      

       sequence_name sql_identifier
      

      

       Name of the sequence that is used by the function
      

     
    
   
  
 

 

  
# routine_table_usage

  

   The view routine_table_usage is meant to identify all
   tables that are used by a function or procedure.  This information is
   currently not tracked by PostgreSQL.
  

  
   
# routine_table_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the function.  See  for more information.
      

     

     
      

       routine_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       routine_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       routine_name sql_identifier
      

      

       Name of the function (might be duplicated in case of overloading)
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table that is used by the
       function (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table that is used by the function
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table that is used by the function
      

     
    
   
  
 

 

  
# routines

  

   The view routines contains all functions and procedures in the
   current database.  Only those functions and procedures are shown that the current
   user has access to (by way of being the owner or having some
   privilege).
  

  
   
# routines Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the function.  This is a
       name that uniquely identifies the function in the schema, even
       if the real name of the function is overloaded.  The format of
       the specific name is not defined, it should only be used to
       compare it to other instances of specific routine names.
      

     

     
      

       routine_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       routine_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       routine_name sql_identifier
      

      

       Name of the function (might be duplicated in case of overloading)
      

     

     
      

       routine_type character_data
      

      

       FUNCTION for a
       function, PROCEDURE for a procedure
      

     

     
      

       module_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       module_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       module_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       udt_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       udt_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       udt_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       data_type character_data
      

      

       Return data type of the function, if it is a built-in type, or
       ARRAY if it is some array (in that case, see
       the view element_types), else
       USER-DEFINED (in that case, the type is
       identified in type_udt_name and associated
       columns).  Null for a procedure.
      

     

     
      

       character_maximum_length cardinal_number
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       character_octet_length cardinal_number
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       character_set_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       collation_catalog sql_identifier
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       collation_schema sql_identifier
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       collation_name sql_identifier
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       numeric_precision cardinal_number
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       numeric_precision_radix cardinal_number
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       numeric_scale cardinal_number
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       datetime_precision cardinal_number
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       interval_type character_data
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       interval_precision cardinal_number
      

      

       Always null, since this information is not applied to return data types in PostgreSQL
      

     

     
      

       type_udt_catalog sql_identifier
      

      

       Name of the database that the return data type of the function
       is defined in (always the current database).  Null for a procedure.
      

     

     
      

       type_udt_schema sql_identifier
      

      

       Name of the schema that the return data type of the function is
       defined in.  Null for a procedure.
      

     

     
      

       type_udt_name sql_identifier
      

      

       Name of the return data type of the function.  Null for a procedure.
      

     

     
      

       scope_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       scope_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       maximum_cardinality cardinal_number
      

      

       Always null, because arrays always have unlimited maximum cardinality in PostgreSQL
      

     

     
      

       dtd_identifier sql_identifier
      

      

       An identifier of the data type descriptor of the return data
       type of this function, unique among the data type descriptors
       pertaining to the function.  This is mainly useful for joining
       with other instances of such identifiers.  (The specific format
       of the identifier is not defined and not guaranteed to remain
       the same in future versions.)
      

     

     
      

       routine_body character_data
      

      

       If the function is an SQL function, then
       SQL, else EXTERNAL.
      

     

     
      

       routine_definition character_data
      

      

       The source text of the function (null if the function is not
       owned by a currently enabled role).  (According to the SQL
       standard, this column is only applicable if
       routine_body is SQL, but
       in PostgreSQL it will contain
       whatever source text was specified when the function was
       created.)
      

     

     
      

       external_name character_data
      

      

       If this function is a C function, then the external name (link
       symbol) of the function; else null.  (This works out to be the
       same value that is shown in
       routine_definition.)
      

     

     
      

       external_language character_data
      

      

       The language the function is written in
      

     

     
      

       parameter_style character_data
      

      

       Always GENERAL (The SQL standard defines
       other parameter styles, which are not available in PostgreSQL.)
      

     

     
      

       is_deterministic yes_or_no
      

      

       If the function is declared immutable (called deterministic in
       the SQL standard), then YES, else
       NO.  (You cannot query the other volatility
       levels available in PostgreSQL through the information schema.)
      

     

     
      

       sql_data_access character_data
      

      

       Always MODIFIES, meaning that the function
       possibly modifies SQL data.  This information is not useful for
       PostgreSQL.
      

     

     
      

       is_null_call yes_or_no
      

      

       If the function automatically returns null if any of its
       arguments are null, then YES, else
       NO.  Null for a procedure.
      

     

     
      

       sql_path character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       schema_level_routine yes_or_no
      

      

       Always YES (The opposite would be a method
       of a user-defined type, which is a feature not available in
       PostgreSQL.)
      

     

     
      

       max_dynamic_result_sets cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       is_user_defined_cast yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       is_implicitly_invocable yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       security_type character_data
      

      

       If the function runs with the privileges of the current user,
       then INVOKER, if the function runs with the
       privileges of the user who defined it, then
       DEFINER.
      

     

     
      

       to_sql_specific_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       to_sql_specific_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       to_sql_specific_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       as_locator yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       created time_stamp
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       last_altered time_stamp
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       new_savepoint_level yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       is_udt_dependent yes_or_no
      

      

       Currently always NO.  The alternative
       YES applies to a feature not available in
       PostgreSQL.
      

     

     
      

       result_cast_from_data_type character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_as_locator yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_char_max_length cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_char_octet_length cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_char_set_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_char_set_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_char_set_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_collation_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_collation_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_collation_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_numeric_precision cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_numeric_precision_radix cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_numeric_scale cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_datetime_precision cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_interval_type character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_interval_precision cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_type_udt_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_type_udt_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_type_udt_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_scope_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_scope_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_scope_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_maximum_cardinality cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       result_cast_dtd_identifier sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     
    
   
  
 

 

  
# schemata

  

   The view schemata contains all schemas in the current
   database that the current user has access to (by way of being the owner or
   having some privilege).
  

  
   
# schemata Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       catalog_name sql_identifier
      

      

       Name of the database that the schema is contained in (always the current database)
      

     

     
      

       schema_name sql_identifier
      

      

       Name of the schema
      

     

     
      

       schema_owner sql_identifier
      

      

       Name of the owner of the schema
      

     

     
      

       default_character_set_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       default_character_set_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       default_character_set_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       sql_path character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     
    
   
  
 

 

  
# sequences

  

   The view sequences contains all sequences
   defined in the current database.  Only those sequences are shown
   that the current user has access to (by way of being the owner or
   having some privilege).
  

  
   
# sequences Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       sequence_catalog sql_identifier
      

      

       Name of the database that contains the sequence (always the current database)
      

     

     
      

       sequence_schema sql_identifier
      

      

       Name of the schema that contains the sequence
      

     

     
      

       sequence_name sql_identifier
      

      

       Name of the sequence
      

     

     
      

       data_type character_data
      

      

       The data type of the sequence.
      

     

     
      

       numeric_precision cardinal_number
      

      

       This column contains the (declared or implicit) precision of
       the sequence data type (see above).  The precision indicates
       the number of significant digits.  It can be expressed in
       decimal (base 10) or binary (base 2) terms, as specified in the
       column numeric_precision_radix.
      

     

     
      

       numeric_precision_radix cardinal_number
      

      

       This column indicates in which base the values in the columns
       numeric_precision and
       numeric_scale are expressed.  The value is
       either 2 or 10.
      

     

     
      

       numeric_scale cardinal_number
      

      

       This column contains the (declared or implicit) scale of the
       sequence data type (see above).  The scale indicates the number
       of significant digits to the right of the decimal point.  It
       can be expressed in decimal (base 10) or binary (base 2) terms,
       as specified in the column
       numeric_precision_radix.
      

     

     
      

       start_value character_data
      

      

       The start value of the sequence
      

     

     
      

       minimum_value character_data
      

      

       The minimum value of the sequence
      

     

     
      

       maximum_value character_data
      

      

       The maximum value of the sequence
      

     

     
      

       increment character_data
      

      

       The increment of the sequence
      

     

     
      

       cycle_option yes_or_no
      

      

       YES if the sequence cycles, else NO
      

     
    
   
  

  

   Note that in accordance with the SQL standard, the start, minimum,
   maximum, and increment values are returned as character strings.
  

 

 

  
# sql_features

  

   The table sql_features contains information
   about which formal features defined in the SQL standard are
   supported by PostgreSQL.  This is the
   same information that is presented in .
   There you can also find some additional background information.
  

  
   
# sql_features Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       feature_id character_data
      

      

       Identifier string of the feature
      

     

     
      

       feature_name character_data
      

      

       Descriptive name of the feature
      

     

     
      

       sub_feature_id character_data
      

      

       Identifier string of the subfeature, or a zero-length string if not a subfeature
      

     

     
      

       sub_feature_name character_data
      

      

       Descriptive name of the subfeature, or a zero-length string if not a subfeature
      

     

     
      

       is_supported yes_or_no
      

      

       YES if the feature is fully supported by the
       current version of PostgreSQL, NO if not
      

     

     
      

       is_verified_by character_data
      

      

       Always null, since the PostgreSQL development group does not
       perform formal testing of feature conformance
      

     

     
      

       comments character_data
      

      

       Possibly a comment about the supported status of the feature
      

     
    
   
  
 

 

  
# sql_implementation_info

  

   The table sql_implementation_info contains
   information about various aspects that are left
   implementation-defined by the SQL standard.  This information is
   primarily intended for use in the context of the ODBC interface;
   users of other interfaces will probably find this information to be
   of little use.  For this reason, the individual implementation
   information items are not described here; you will find them in the
   description of the ODBC interface.
  

  
   
# sql_implementation_info Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       implementation_info_id character_data
      

      

       Identifier string of the implementation information item
      

     

     
      

       implementation_info_name character_data
      

      

       Descriptive name of the implementation information item
      

     

     
      

       integer_value cardinal_number
      

      

       Value of the implementation information item, or null if the
       value is contained in the column
       character_value
      

     

     
      

       character_value character_data
      

      

       Value of the implementation information item, or null if the
       value is contained in the column
       integer_value
      

     

     
      

       comments character_data
      

      

       Possibly a comment pertaining to the implementation information item
      

     
    
   
  
 

 

  
# sql_parts

  

   The table sql_parts contains information about
   which of the several parts of the SQL standard are supported by
   PostgreSQL.
  

  
   
# sql_parts Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       feature_id character_data
      

      

       An identifier string containing the number of the part
      

     

     
      

       feature_name character_data
      

      

       Descriptive name of the part
      

     

     
      

       is_supported yes_or_no
      

      

       YES if the part is fully supported by the
       current version of PostgreSQL,
       NO if not
      

     

     
      

       is_verified_by character_data
      

      

       Always null, since the PostgreSQL development group does not
       perform formal testing of feature conformance
      

     

     
      

       comments character_data
      

      

       Possibly a comment about the supported status of the part
      

     
    
   
  
 

 

  
# sql_sizing

  

   The table sql_sizing contains information about
   various size limits and maximum values in
   PostgreSQL.  This information is
   primarily intended for use in the context of the ODBC interface;
   users of other interfaces will probably find this information to be
   of little use.  For this reason, the individual sizing items are
   not described here; you will find them in the description of the
   ODBC interface.
  

  
   
# sql_sizing Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       sizing_id cardinal_number
      

      

       Identifier of the sizing item
      

     

     
      

       sizing_name character_data
      

      

       Descriptive name of the sizing item
      

     

     
      

       supported_value cardinal_number
      

      

       Value of the sizing item, or 0 if the size is unlimited or
       cannot be determined, or null if the features for which the
       sizing item is applicable are not supported
      

     

     
      

       comments character_data
      

      

       Possibly a comment pertaining to the sizing item
      

     
    
   
  
 

 

  
# table_constraints

  

   The view table_constraints contains all
   constraints belonging to tables that the current user owns or has
   some privilege other than SELECT on.
  

  
   
# table_constraints Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       constraint_catalog sql_identifier
      

      

       Name of the database that contains the constraint (always the current database)
      

     

     
      

       constraint_schema sql_identifier
      

      

       Name of the schema that contains the constraint
      

     

     
      

       constraint_name sql_identifier
      

      

       Name of the constraint
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table
      

     

     
      

       constraint_type character_data
      

      

       Type of the constraint: CHECK (includes not-null constraints),
       FOREIGN KEY, PRIMARY KEY,
       or UNIQUE
      

     

     
      

       is_deferrable yes_or_no
      

      

       YES if the constraint is deferrable, NO if not
      

     

     
      

       initially_deferred yes_or_no
      

      

       YES if the constraint is deferrable and initially deferred, NO if not
      

     

     
      

       enforced yes_or_no
      

      

       YES if the constraint is enforced, NO if not
      

     

     
      

       nulls_distinct yes_or_no
      

      

       If the constraint is a unique constraint, then YES
       if the constraint treats nulls as distinct or NO if
       it treats nulls as not distinct, otherwise null for other types of
       constraints.
      

     
    
   
  
 

 

  
# table_privileges

  

   The view table_privileges identifies all
   privileges granted on tables or views to a currently enabled role
   or by a currently enabled role.  There is one row for each
   combination of table, grantor, and grantee.
  

  
   
# table_privileges Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       Name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       Name of the role that the privilege was granted to
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table
      

     

     
      

       privilege_type character_data
      

      

       Type of the privilege: SELECT,
       INSERT, UPDATE,
       DELETE, TRUNCATE,
       REFERENCES, or TRIGGER
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     

     
      

       with_hierarchy yes_or_no
      

      

       In the SQL standard, WITH HIERARCHY OPTION
       is a separate (sub-)privilege allowing certain operations on
       table inheritance hierarchies.  In PostgreSQL, this is included
       in the SELECT privilege, so this column
       shows YES if the privilege
       is SELECT, else NO.
      

     
    
   
  
 

 

  
# tables

  

   The view tables contains all tables and views
   defined in the current database.  Only those tables and views are
   shown that the current user has access to (by way of being the
   owner or having some privilege).
  

  
   
# tables Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table
      

     

     
      

       table_type character_data
      

      

       Type of the table: BASE TABLE for a
       persistent base table (the normal table type),
       VIEW for a view, FOREIGN
       for a foreign table, or
       LOCAL TEMPORARY for a temporary table
      

     

     
      

       self_referencing_column_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       reference_generation character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       user_defined_type_catalog sql_identifier
      

      

       If the table is a typed table, the name of the database that
       contains the underlying data type (always the current
       database), else null.
      

     

     
      

       user_defined_type_schema sql_identifier
      

      

       If the table is a typed table, the name of the schema that
       contains the underlying data type, else null.
      

     

     
      

       user_defined_type_name sql_identifier
      

      

       If the table is a typed table, the name of the underlying data
       type, else null.
      

     

     
      

       is_insertable_into yes_or_no
      

      

       YES if the table is insertable into,
       NO if not (Base tables are always insertable
       into, views not necessarily.)
      

     

     
      

       is_typed yes_or_no
      

      

       YES if the table is a typed table, NO if not
      

     

     
      

       commit_action character_data
      

      

       Not yet implemented
      

     
    
   
  
 

 

  
# transforms

  

   The view transforms contains information about the
   transforms defined in the current database.  More precisely, it contains a
   row for each function contained in a transform (the from SQL
   or to SQL function).
  

  
   
# transforms Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       udt_catalog sql_identifier
      

      

       Name of the database that contains the type the transform is for (always the current database)
      

     

     
      

       udt_schema sql_identifier
      

      

       Name of the schema that contains the type the transform is for
      

     

     
      

       udt_name sql_identifier
      

      

       Name of the type the transform is for
      

     

     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the function.  See  for more information.
      

     

     
      

       group_name sql_identifier
      

      

       The SQL standard allows defining transforms in groups,
       and selecting a group at run time.  PostgreSQL does not support this.
       Instead, transforms are specific to a language.  As a compromise, this
       field contains the language the transform is for.
      

     

     
      

       transform_type character_data
      

      

       FROM SQL or TO SQL
      

     
    
   
  
 

 

  
# triggered_update_columns

  

   For triggers in the current database that specify a column list
   (like UPDATE OF column1, column2), the
   view triggered_update_columns identifies these
   columns.  Triggers that do not specify a column list are not
   included in this view.  Only those columns are shown that the
   current user owns or has some privilege other than
   SELECT on.
  

  
   
# triggered_update_columns Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       trigger_catalog sql_identifier
      

      

       Name of the database that contains the trigger (always the current database)
      

     

     
      

       trigger_schema sql_identifier
      

      

       Name of the schema that contains the trigger
      

     

     
      

       trigger_name sql_identifier
      

      

       Name of the trigger
      

     

     
      

       event_object_catalog sql_identifier
      

      

       Name of the database that contains the table that the trigger
       is defined on (always the current database)
      

     

     
      

       event_object_schema sql_identifier
      

      

       Name of the schema that contains the table that the trigger is defined on
      

     

     
      

       event_object_table sql_identifier
      

      

       Name of the table that the trigger is defined on
      

     

     
      

       event_object_column sql_identifier
      

      

       Name of the column that the trigger is defined on
      

     
    
   
  
 

 

  
# triggers

  

   The view triggers contains all triggers defined
   in the current database on tables and views that the current user owns
   or has some privilege other than SELECT on.
  

  
   
# triggers Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       trigger_catalog sql_identifier
      

      

       Name of the database that contains the trigger (always the current database)
      

     

     
      

       trigger_schema sql_identifier
      

      

       Name of the schema that contains the trigger
      

     

     
      

       trigger_name sql_identifier
      

      

       Name of the trigger
      

     

     
      

       event_manipulation character_data
      

      

       Event that fires the trigger (INSERT,
       UPDATE, or DELETE)
      

     

     
      

       event_object_catalog sql_identifier
      

      

       Name of the database that contains the table that the trigger
       is defined on (always the current database)
      

     

     
      

       event_object_schema sql_identifier
      

      

       Name of the schema that contains the table that the trigger is defined on
      

     

     
      

       event_object_table sql_identifier
      

      

       Name of the table that the trigger is defined on
      

     

     
      

       action_order cardinal_number
      

      

       Firing order among triggers on the same table having the same
       event_manipulation,
       action_timing, and
       action_orientation.  In
       PostgreSQL, triggers are fired in name
       order, so this column reflects that.
      

     

     
      

       action_condition character_data
      

      

       WHEN condition of the trigger, null if none
       (also null if the table is not owned by a currently enabled
       role)
      

     

     
      

       action_statement character_data
      

      

       Statement that is executed by the trigger (currently always
       EXECUTE FUNCTION
       function(...))
      

     

     
      

       action_orientation character_data
      

      

       Identifies whether the trigger fires once for each processed
       row or once for each statement (ROW or
       STATEMENT)
      

     

     
      

       action_timing character_data
      

      

       Time at which the trigger fires (BEFORE,
       AFTER, or INSTEAD OF)
      

     

     
      

       action_reference_old_table sql_identifier
      

      

       Name of the old transition table, or null if none
      

     

     
      

       action_reference_new_table sql_identifier
      

      

       Name of the new transition table, or null if none
      

     

     
      

       action_reference_old_row sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       action_reference_new_row sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       created time_stamp
      

      

       Applies to a feature not available in PostgreSQL
      

     
    
   
  

  

   Triggers in PostgreSQL have two
   incompatibilities with the SQL standard that affect the
   representation in the information schema.  First, trigger names are
   local to each table in PostgreSQL, rather
   than being independent schema objects.  Therefore there can be duplicate
   trigger names defined in one schema, so long as they belong to
   different tables.  (trigger_catalog and
   trigger_schema are really the values pertaining
   to the table that the trigger is defined on.)  Second, triggers can
   be defined to fire on multiple events in
   PostgreSQL (e.g., ON INSERT OR
   UPDATE), whereas the SQL standard only allows one.  If a
   trigger is defined to fire on multiple events, it is represented as
   multiple rows in the information schema, one for each type of
   event.  As a consequence of these two issues, the primary key of
   the view triggers is really
   (trigger_catalog, trigger_schema, event_object_table,
   trigger_name, event_manipulation) instead of
   (trigger_catalog, trigger_schema, trigger_name),
   which is what the SQL standard specifies.  Nonetheless, if you
   define your triggers in a manner that conforms with the SQL
   standard (trigger names unique in the schema and only one event
   type per trigger), this will not affect you.
  

  
   

    Prior to PostgreSQL 9.1, this view's columns
    action_timing,
    action_reference_old_table,
    action_reference_new_table,
    action_reference_old_row, and
    action_reference_new_row
    were named
    condition_timing,
    condition_reference_old_table,
    condition_reference_new_table,
    condition_reference_old_row, and
    condition_reference_new_row
    respectively.
    That was how they were named in the SQL:1999 standard.
    The new naming conforms to SQL:2003 and later.
   

  
 

 

  
# udt_privileges

  

   The view udt_privileges identifies
   USAGE privileges granted on user-defined types to a
   currently enabled role or by a currently enabled role.  There is one row for
   each combination of type, grantor, and grantee.  This view shows only
   composite types (see under 
   for why); see
    for domain privileges.
  

  
   
# udt_privileges Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       Name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       Name of the role that the privilege was granted to
      

     

     
      

       udt_catalog sql_identifier
      

      

       Name of the database containing the type (always the current database)
      

     

     
      

       udt_schema sql_identifier
      

      

       Name of the schema containing the type
      

     

     
      

       udt_name sql_identifier
      

      

       Name of the type
      

     

     
      

       privilege_type character_data
      

      

       Always TYPE USAGE
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     
    
   
  
 

 

  
# usage_privileges

  

   The view usage_privileges identifies
   USAGE privileges granted on various kinds of
   objects to a currently enabled role or by a currently enabled role.
   In PostgreSQL, this currently applies to
   collations, domains, foreign-data wrappers, foreign servers, and sequences.  There is one
   row for each combination of object, grantor, and grantee.
  

  

   Since collations do not have real privileges
   in PostgreSQL, this view shows implicit
   non-grantable USAGE privileges granted by the
   owner to PUBLIC for all collations.  The other
   object types, however, show real privileges.
  

  

   In PostgreSQL, sequences also support SELECT
   and UPDATE privileges in addition to
   the USAGE privilege.  These are nonstandard and therefore
   not visible in the information schema.
  

  
   
# usage_privileges Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       grantor sql_identifier
      

      

       Name of the role that granted the privilege
      

     

     
      

       grantee sql_identifier
      

      

       Name of the role that the privilege was granted to
      

     

     
      

       object_catalog sql_identifier
      

      

       Name of the database containing the object (always the current database)
      

     

     
      

       object_schema sql_identifier
      

      

       Name of the schema containing the object, if applicable,
       else an empty string
      

     

     
      

       object_name sql_identifier
      

      

       Name of the object
      

     

     
      

       object_type character_data
      

      

       COLLATION or DOMAIN or FOREIGN DATA WRAPPER or FOREIGN SERVER or SEQUENCE
      

     

     
      

       privilege_type character_data
      

      

       Always USAGE
      

     

     
      

       is_grantable yes_or_no
      

      

       YES if the privilege is grantable, NO if not
      

     
    
   
  
 

 

  
# user_defined_types

  

   The view user_defined_types currently contains
   all composite types defined in the current database.
   Only those types are shown that the current user has access to (by way
   of being the owner or having some privilege).
  

  

   SQL knows about two kinds of user-defined types: structured types
   (also known as composite types
   in PostgreSQL) and distinct types (not
   implemented in PostgreSQL).  To be
   future-proof, use the
   column user_defined_type_category to
   differentiate between these.  Other user-defined types such as base
   types and enums, which are PostgreSQL
   extensions, are not shown here.  For domains,
   see  instead.
  

  
   
# user_defined_types Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       user_defined_type_catalog sql_identifier
      

      

       Name of the database that contains the type (always the current database)
      

     

     
      

       user_defined_type_schema sql_identifier
      

      

       Name of the schema that contains the type
      

     

     
      

       user_defined_type_name sql_identifier
      

      

       Name of the type
      

     

     
      

       user_defined_type_category character_data
      

      

       Currently always STRUCTURED
      

     

     
      

       is_instantiable yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       is_final yes_or_no
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       ordering_form character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       ordering_category character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       ordering_routine_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       ordering_routine_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       ordering_routine_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       reference_type character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       data_type character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_maximum_length cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_octet_length cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       character_set_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       collation_catalog sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       collation_schema sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       collation_name sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       numeric_precision cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       numeric_precision_radix cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       numeric_scale cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       datetime_precision cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       interval_type character_data
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       interval_precision cardinal_number
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       source_dtd_identifier sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     

     
      

       ref_dtd_identifier sql_identifier
      

      

       Applies to a feature not available in PostgreSQL
      

     
    
   
  
 

 

  
# user_mapping_options

  

   The view user_mapping_options contains all the
   options defined for user mappings in the current database.  Only
   those user mappings are shown where the current user has access to
   the corresponding foreign server (by way of being the owner or
   having some privilege).
  

  
   
# user_mapping_options Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       authorization_identifier sql_identifier
      

      

       Name of the user being mapped,
       or PUBLIC if the mapping is public
      

     

     
      

       foreign_server_catalog sql_identifier
      

      

       Name of the database that the foreign server used by this
       mapping is defined in (always the current database)
      

     

     
      

       foreign_server_name sql_identifier
      

      

       Name of the foreign server used by this mapping
      

     

     
      

       option_name sql_identifier
      

      

       Name of an option
      

     

     
      

       option_value character_data
      

      

       Value of the option.  This column will show as null
       unless the current user is the user being mapped, or the mapping
       is for PUBLIC and the current user is the
       server owner, or the current user is a superuser.  The intent is
       to protect password information stored as user mapping
       option.
      

     
    
   
  
 

 

  
# user_mappings

  

   The view user_mappings contains all user
   mappings defined in the current database.  Only those user mappings
   are shown where the current user has access to the corresponding
   foreign server (by way of being the owner or having some
   privilege).
  

  
   
# user_mappings Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       authorization_identifier sql_identifier
      

      

       Name of the user being mapped,
       or PUBLIC if the mapping is public
      

     

     
      

       foreign_server_catalog sql_identifier
      

      

       Name of the database that the foreign server used by this
       mapping is defined in (always the current database)
      

     

     
      

       foreign_server_name sql_identifier
      

      

       Name of the foreign server used by this mapping
      

     
    
   
  
 

 

  
# view_column_usage

  

   The view view_column_usage identifies all
   columns that are used in the query expression of a view (the
   SELECT statement that defines the view).  A
   column is only included if the table that contains the column is
   owned by a currently enabled role.
  

  
   

    Columns of system tables are not included.  This should be fixed
    sometime.
   

  

  
   
# view_column_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       view_catalog sql_identifier
      

      

       Name of the database that contains the view (always the current database)
      

     

     
      

       view_schema sql_identifier
      

      

       Name of the schema that contains the view
      

     

     
      

       view_name sql_identifier
      

      

       Name of the view
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table that contains the
       column that is used by the view (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table that contains the
       column that is used by the view
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table that contains the column that is used by the
       view
      

     

     
      

       column_name sql_identifier
      

      

       Name of the column that is used by the view
      

     
    
   
  
 

 

  
# view_routine_usage

  

   The view view_routine_usage identifies all
   routines (functions and procedures) that are used in the query
   expression of a view (the SELECT statement that
   defines the view).  A routine is only included if that routine is
   owned by a currently enabled role.
  

  
   
# view_routine_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       table_catalog sql_identifier
      

      

       Name of the database containing the view (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema containing the view
      

     

     
      

       table_name sql_identifier
      

      

       Name of the view
      

     

     
      

       specific_catalog sql_identifier
      

      

       Name of the database containing the function (always the current database)
      

     

     
      

       specific_schema sql_identifier
      

      

       Name of the schema containing the function
      

     

     
      

       specific_name sql_identifier
      

      

       The specific name of the function.  See  for more information.
      

     
    
   
  
 

 

  
# view_table_usage

  

   The view view_table_usage identifies all tables
   that are used in the query expression of a view (the
   SELECT statement that defines the view).  A
   table is only included if that table is owned by a currently
   enabled role.
  

  
   

    System tables are not included.  This should be fixed sometime.
   

  

  
   
# view_table_usage Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       view_catalog sql_identifier
      

      

       Name of the database that contains the view (always the current database)
      

     

     
      

       view_schema sql_identifier
      

      

       Name of the schema that contains the view
      

     

     
      

       view_name sql_identifier
      

      

       Name of the view
      

     

     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the table that is
       used by the view (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the table that is used by the
       view
      

     

     
      

       table_name sql_identifier
      

      

       Name of the table that is used by the view
      

     
    
   
  
 

 

  
# views

  

   The view views contains all views defined in the
   current database.  Only those views are shown that the current user
   has access to (by way of being the owner or having some privilege).
  

  
   
# views Columns

   
    
     
      

       Column Type
      

      

       Description
      

     
    

    
     
      

       table_catalog sql_identifier
      

      

       Name of the database that contains the view (always the current database)
      

     

     
      

       table_schema sql_identifier
      

      

       Name of the schema that contains the view
      

     

     
      

       table_name sql_identifier
      

      

       Name of the view
      

     

     
      

       view_definition character_data
      

      

       Query expression defining the view (null if the view is not
       owned by a currently enabled role)
      

     

     
      

       check_option character_data
      

      

       CASCADED or LOCAL if the view
       has a CHECK OPTION defined on it,
       NONE if not
      

     

     
      

       is_updatable yes_or_no
      

      

       YES if the view is updatable (allows
       UPDATE and DELETE),
       NO if not
      

     

     
      

       is_insertable_into yes_or_no
      

      

       YES if the view is insertable into (allows
       INSERT), NO if not
      

     

     
      

       is_trigger_updatable yes_or_no
      

      

       YES if the view has an INSTEAD OF
       UPDATE trigger defined on it, NO if not
      

     

     
      

       is_trigger_deletable yes_or_no
      

      

       YES if the view has an INSTEAD OF
       DELETE trigger defined on it, NO if not
      

     

     
      

       is_trigger_insertable_into yes_or_no
      

      

       YES if the view has an INSTEAD OF
       INSERT trigger defined on it, NO if not
