ABORT
 

 
  ABORT
  7
  SQL - Language Statements
 

 
  ABORT
  abort the current transaction
 

 

ABORT [ WORK | TRANSACTION ] [ AND [ NO ] CHAIN ]

 

 
  
# Description

  

   ABORT rolls back the current transaction and causes
   all the updates made by the transaction to be discarded.
   This command is identical
   in behavior to the standard SQL command
   ROLLBACK,
   and is present only for historical reasons.
  

 

 
  
# Parameters

  

   

    
WORK

    
TRANSACTION

    

     

      Optional key words. They have no effect.
     

    

   

   

    
AND CHAIN

    

     

      If AND CHAIN is specified, a new transaction is
      immediately started with the same transaction characteristics (see SET TRANSACTION) as the just finished one.  Otherwise,
      no new transaction is started.
     

    

   

  

 

 
  
# Notes

  

   Use COMMIT to
   successfully terminate a transaction.
  

  

   Issuing ABORT outside of a transaction block
   emits a warning and otherwise has no effect.
  

 

 
  
# Examples

  

   To abort all changes:

```
ABORT;
```

 

 
  
# Compatibility

  

   This command is a PostgreSQL extension
   present for historical reasons. ROLLBACK is the
   equivalent standard SQL command.
  

 

 
  
# See Also
