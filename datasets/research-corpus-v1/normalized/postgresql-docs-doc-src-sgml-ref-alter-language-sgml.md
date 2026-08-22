ALTER LANGUAGE
 

 
  ALTER LANGUAGE
  7
  SQL - Language Statements
 

 
  ALTER LANGUAGE
  change the definition of a procedural language
 

 

ALTER [ PROCEDURAL ] LANGUAGE name RENAME TO new_name
ALTER [ PROCEDURAL ] LANGUAGE name OWNER TO { new_owner | CURRENT_ROLE | CURRENT_USER | SESSION_USER }

 

 
  
# Description

  

   ALTER LANGUAGE changes the definition of a
   procedural language.  The only functionality is to rename the language or
   assign a new owner.  You must be superuser or owner of the language to
   use ALTER LANGUAGE.
  

 

 
  
# Parameters

  

   

    
name

    

     

      Name of a language
     

    

   

   

    
new_name

    

     

      The new name of the language
     

    

   

   

    
new_owner

    

     

      The new owner of the language
     

    

   

  

 

 
  
# Compatibility

  

   There is no ALTER LANGUAGE statement in the SQL
   standard.
  

 

 
  
# See Also
