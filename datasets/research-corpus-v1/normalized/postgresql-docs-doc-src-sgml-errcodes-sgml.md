# PostgreSQL Error Codes

 
  error codes
  list of
 

 

  All messages emitted by the PostgreSQL
  server are assigned five-character error codes that follow the SQL
  standard's conventions for SQLSTATE codes.  Applications
  that need to know which error condition has occurred should usually
  test the error code, rather than looking at the textual error
  message.  The error codes are less likely to change across
  PostgreSQL releases, and also are not subject to
  change due to localization of error messages. Note that some, but
  not all, of the error codes produced by PostgreSQL
  are defined by the SQL standard; some additional error codes for
  conditions not defined by the standard have been invented or
  borrowed from other databases.
 

 

  According to the standard, the first two characters of an error code
  denote a class of errors, while the last three characters indicate
  a specific condition within that class.  Thus, an application that
  does not recognize the specific error code might still be able to infer
  what to do from the error class.
 

 

   lists all the error codes defined in
  PostgreSQL &version;.  (Some are not actually
  used at present, but are defined by the SQL standard.)
  The error classes are also shown.  For each error class there is a
  standard error code having the last three characters
  000.  This code is used only for error conditions that fall
  within the class but do not have any more-specific code assigned.
 

 

  The symbol shown in the column Condition Name is
  the condition name to use in PL/pgSQL.  Condition
  names can be written in either upper or lower case.  (Note that
  PL/pgSQL does not recognize warning, as opposed to error,
  condition names; those are classes 00, 01, and 02.)
 

 

  For some types of errors, the server reports the name of a database object
  (a table, table column, data type, or constraint) associated with the error;
  for example, the name of the unique constraint that caused a
  unique_violation error.  Such names are supplied in separate
  fields of the error report message so that applications need not try to
  extract them from the possibly-localized human-readable text of the message.
  As of PostgreSQL 9.3, complete coverage for this feature
  exists only for errors in SQLSTATE class 23 (integrity constraint
  violation), but this is likely to be expanded in future.
 

 
# PostgreSQL Error Codes

 
  
  
  

  
   
    Error Code
    Condition Name
   
  

  

    &errcodes-table;
