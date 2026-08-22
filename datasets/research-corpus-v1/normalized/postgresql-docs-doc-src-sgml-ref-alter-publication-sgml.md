ALTER PUBLICATION
 

 
  ALTER PUBLICATION
  7
  SQL - Language Statements
 

 
  ALTER PUBLICATION
  change the definition of a publication
 

 

ALTER PUBLICATION name ADD publication_object [, ...]
ALTER PUBLICATION name DROP publication_drop_object [, ...]
ALTER PUBLICATION name SET { publication_object [, ...] | publication_all_object [, ... ] }
ALTER PUBLICATION name SET ( publication_parameter [= value] [, ... ] )
ALTER PUBLICATION name OWNER TO { new_owner | CURRENT_ROLE | CURRENT_USER | SESSION_USER }
ALTER PUBLICATION name RENAME TO new_name

where publication_object is one of:

    TABLE table_and_columns [, ... ]
    TABLES IN SCHEMA { schema_name | CURRENT_SCHEMA } [, ... ]

and publication_all_object is one of:

    ALL TABLES [ EXCEPT ( except_table_object [, ... ] ) ]
    ALL SEQUENCES

and publication_drop_object is one of:

    TABLE table_object [, ... ]
    TABLES IN SCHEMA { schema_name | CURRENT_SCHEMA } [, ... ]

and table_and_columns is:

    table_object [ ( column_name [, ... ] ) ] [ WHERE ( expression ) ]

and except_table_object is:

    TABLE table_object [, ... ]

and table_object is:

   [ ONLY ] table_name [ * ]

 

 
  
# Description

  

   The command ALTER PUBLICATION can change the attributes
   of a publication.
  

  

   The first two variants modify which tables/schemas are part of the
   publication. The ADD and DROP clauses
   will add and remove one or more tables/schemas from the publication.
  

  

   The third variant either modifies the included tables/schemas
   or marks the publication as FOR ALL SEQUENCES or
   FOR ALL TABLES, optionally using
   EXCEPT to exclude specific tables. The
   SET ALL TABLES clause can transform an empty publication,
   or one defined for ALL SEQUENCES (or both
   ALL TABLES and ALL SEQUENCES), into
   a publication defined for ALL TABLES. Likewise,
   SET ALL SEQUENCES can convert an empty publication, or
   one defined for ALL TABLES (or both
   ALL TABLES and ALL SEQUENCES), into a
   publication defined for ALL SEQUENCES. In addition,
   SET ALL TABLES can be used to update the tables specified
   in the EXCEPT clause of a
   FOR ALL TABLES publication. If EXCEPT
   is specified with a list of tables, the existing exclusion list is replaced
   with the specified tables. If EXCEPT is omitted, the
   existing exclusion list is cleared. The SET clause, when
   used with a publication defined with FOR TABLE or
   FOR TABLES IN SCHEMA, replaces the list of tables/schemas
   in the publication with the specified list; the existing tables or schemas
   that were present in the publication will be removed.
  

  

   Note that adding, dropping, or setting tables or schemas in a publication
   that is already subscribed to will require an
   
   ALTER SUBSCRIPTION ... REFRESH PUBLICATION action
   on the subscribing side in order to become effective. Likewise altering a
   publication to set ALL TABLES, to change the
   EXCEPT list, or to set ALL SEQUENCES
   also requires the subscriber to refresh the publication. Note also that
   DROP TABLES IN SCHEMA will not drop any schema tables
   that were specified using
   FOR TABLE/
   ADD TABLE.
  

  

   The fourth variant of this command listed in the synopsis can change
   all of the publication properties specified in
   .  Properties not mentioned in the
   command retain their previous settings.
  

  

   The remaining variants change the owner and the name of the publication.
  

  

   You must own the publication to use ALTER PUBLICATION.
   Adding a table to a publication additionally requires owning that table.
   The ADD TABLES IN SCHEMA,
   SET TABLES IN SCHEMA, SET ALL TABLES,
   and SET ALL SEQUENCES to a publication requires the
   invoking user to be a superuser.
   To alter the owner, you must be able to SET ROLE to the
   new owning role, and that role must have CREATE
   privilege on the database.
   Also, the new owner of a
   FOR TABLES IN SCHEMA
   or FOR ALL TABLES
   or FOR ALL SEQUENCES
   publication must be a superuser. However, a superuser can
   change the ownership of a publication regardless of these restrictions.
  

  

   Adding/Setting any schema when the publication also publishes a table with a
   column list, and vice versa is not supported.
  

 

 
  
# Parameters

  

   

    
name

    

     

      The name of an existing publication whose definition is to be altered.
     

    

   

   

    
table_name

    

     

      Name of an existing table.  If ONLY is specified before the
      table_name, only that table
      is affected.  If ONLY is not specified, the table and
      all its descendant tables (if any) are affected.  Optionally,
      * can be specified after the
      table_name to explicitly
      indicate that descendant tables are included.
     

     

      Optionally, a column list can be specified.  See  for details. Note that a subscription
      having several publications in which the same table has been published
      with different column lists is not supported. See
       for details of
      potential problems when altering column lists.
     

     

      If the optional WHERE clause is specified, rows for
      which the expression
      evaluates to false or null will not be published. Note that parentheses
      are required around the expression. The
      expression is evaluated with
      the role used for the replication connection.
     

    

   

   

    
column_name

    

     

      Name of an existing column of
      table_name.
     

    

   

   

    
schema_name

    

     

      Name of an existing schema.
     

    

   

   

    
SET ( publication_parameter [= value] [, ... ] )

    

     

      This clause alters publication parameters originally set by
      .  See there for more information.
      This clause is not applicable to sequences.
     

     
      

       Altering the publish_via_partition_root parameter can
       lead to data loss or duplication at the subscriber because it changes
       the identity and schema of the published tables. Note this happens only
       when a partition root table is specified as the replication target.
      

      

       This problem can be avoided by refraining from modifying partition leaf
       tables after the ALTER PUBLICATION ... SET until the
       ALTER SUBSCRIPTION ... REFRESH PUBLICATION
       is executed and by only refreshing using the copy_data = off
       option.
      

     
    

   

   

    
new_owner

    

     

      The user name of the new owner of the publication.
     

    

   

   

    
new_name

    

     

      The new name for the publication.
     

    

   

  

 

 
  
# Examples

  

   Change the publication to publish only deletes and updates:

```
ALTER PUBLICATION noinsert SET (publish = 'update, delete');
```

  

  

   Add some tables to the publication:

```
ALTER PUBLICATION mypublication ADD TABLE users (user_id, firstname), departments;
```

  

   Change the set of columns published for a table:

```
ALTER PUBLICATION mypublication SET TABLE users (user_id, firstname, lastname), TABLE departments;
```

  

   Replace the table list in the publication's EXCEPT clause:

```
ALTER PUBLICATION mypublication SET ALL TABLES EXCEPT (TABLE users, departments);
```

  

   Reset the publication to be a FOR ALL TABLES publication
   with no excluded tables:

```
ALTER PUBLICATION mypublication SET ALL TABLES;
```

  

   Add schemas marketing and
   sales to the publication
   sales_publication:

```
ALTER PUBLICATION sales_publication ADD TABLES IN SCHEMA marketing, sales;
```

  

  

   Add tables users,
   departments and schema
   production to the publication
   production_publication:

```
ALTER PUBLICATION production_publication ADD TABLE users, departments, TABLES IN SCHEMA production;
```

 

 
  
# Compatibility

  

   ALTER PUBLICATION is a PostgreSQL
   extension.
  

 

 
  
# See Also
