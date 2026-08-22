CLUSTER
 

 
  CLUSTER
  7
  SQL - Language Statements
 

 
  CLUSTER
  cluster a table according to an index
 

 

CLUSTER [ ( option [, ...] ) ] [ table_name [ USING index_name ] ]

where option can be one of:

    VERBOSE [ boolean ]

 

 
  
# Description

  

   The CLUSTER command is equivalent to
    with a USING INDEX
   clause.  See there for more details.
  

 

 
  
# Parameters

  

   

    
table_name

    

     

      The name (possibly schema-qualified) of a table.
     

    

   

   

    
index_name

    

     

      The name of an index.
     

    

   

   

    
VERBOSE

    

     

      Prints a progress report as each table is clustered
      at INFO level.
     

    

   

   

    
boolean

    

     

      Specifies whether the selected option should be turned on or off.
      You can write TRUE, ON, or
      1 to enable the option, and FALSE,
      OFF, or 0 to disable it.  The
      boolean value can also
      be omitted, in which case TRUE is assumed.
     

    

   

  

 

 
  
# Notes

   

    To cluster a table, one must have the MAINTAIN privilege
    on the table.
   

   

    While CLUSTER is running, the  is temporarily changed to pg_catalog,
    pg_temp.
   

   

    Because CLUSTER remembers which indexes are clustered,
    one can cluster the tables one wants clustered manually the first time,
    then set up a periodic maintenance script that executes
    CLUSTER without any parameters, so that the desired tables
    are periodically reclustered.
   

  

    Each backend running CLUSTER will report its progress
    in the pg_stat_progress_cluster view. See
     for details.
  

   

    Clustering a partitioned table clusters each of its partitions using the
    partition of the specified partitioned index.  When clustering a partitioned
    table, the index may not be omitted.  CLUSTER on a
    partitioned table cannot be executed inside a transaction block.
   

 

 
  
# Examples

  

   Cluster the table employees on the basis of
   its index employees_ind:

```
CLUSTER employees USING employees_ind;
```

  

  

   Cluster the employees table using the same
   index that was used before:

```
CLUSTER employees;
```

  

  

   Cluster all tables in the database that have previously been clustered:

```
CLUSTER;
```

 

 
  
# Compatibility

  

   There is no CLUSTER statement in the SQL standard.
  

  

   The following syntax was used before PostgreSQL
   17 and is still supported:

CLUSTER [ VERBOSE ] [ table_name [ USING index_name ] ]

  

  

   The following syntax was used before PostgreSQL
   8.3 and is still supported:

CLUSTER index_name ON table_name

  

 

 
  
# See Also
