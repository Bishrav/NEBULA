ALTER EVENT TRIGGER
 

 
  ALTER EVENT TRIGGER
  7
  SQL - Language Statements
 

 
  ALTER EVENT TRIGGER
  change the definition of an event trigger
 

 

ALTER EVENT TRIGGER name DISABLE
ALTER EVENT TRIGGER name ENABLE [ REPLICA | ALWAYS ]
ALTER EVENT TRIGGER name OWNER TO { new_owner | CURRENT_ROLE | CURRENT_USER | SESSION_USER }
ALTER EVENT TRIGGER name RENAME TO new_name

 

 
  
# Description

  

   ALTER EVENT TRIGGER changes properties of an
   existing event trigger.
  

  

   You must be superuser to alter an event trigger.
  

 

 
  
# Parameters

  

   

    
name

    

     

      The name of an existing trigger to alter.
     

    

   

   

    
new_owner

    

     

      The user name of the new owner of the event trigger.
     

    

   

   

    
new_name

    

     

      The new name of the event trigger.
     

    

   

   

    
DISABLE/ENABLE [ REPLICA | ALWAYS ]

    

     

      These forms configure the firing of event triggers.  A disabled trigger
      is still known to the system, but is not executed when its triggering
      event occurs.  See also .
     

    

   

  

 

 
  
# Compatibility

  

   There is no ALTER EVENT TRIGGER statement in the
   SQL standard.
  

 

 
  
# See Also
