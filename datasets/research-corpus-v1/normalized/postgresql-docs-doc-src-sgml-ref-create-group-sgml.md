CREATE GROUP
 

 
  CREATE GROUP
  7
  SQL - Language Statements
 

 
  CREATE GROUP
  define a new database role
 

 

CREATE GROUP name [ [ WITH ] option [ ... ] ]

where option can be:

      SUPERUSER | NOSUPERUSER
    | CREATEDB | NOCREATEDB
    | CREATEROLE | NOCREATEROLE
    | INHERIT | NOINHERIT
    | LOGIN | NOLOGIN
    | REPLICATION | NOREPLICATION
    | BYPASSRLS | NOBYPASSRLS
    | CONNECTION LIMIT connlimit
    | [ ENCRYPTED ] PASSWORD 'password' | PASSWORD NULL
    | VALID UNTIL 'timestamp'
    | IN ROLE role_name [, ...]
    | ROLE role_name [, ...]
    | ADMIN role_name [, ...]
    | SYSID uid

 

 
  
# Description

  

   CREATE GROUP is now an alias for
   .
  

 

 
  
# Compatibility

  

   There is no CREATE GROUP statement in the SQL
   standard.
  

 

 
  
# See Also
