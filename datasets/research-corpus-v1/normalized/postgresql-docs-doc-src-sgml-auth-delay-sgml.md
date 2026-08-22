# auth_delay &mdash; pause on authentication failure

 
  auth_delay
 

 

  auth_delay causes the server to pause briefly before
  reporting authentication failure, to make brute-force attacks on database
  passwords more difficult.  Note that it does nothing to prevent
  denial-of-service attacks, and may even exacerbate them, since processes
  that are waiting before reporting authentication failure will still consume
  connection slots.
 

 

  In order to function, this module must be loaded via
   in postgresql.conf.
 

 

  
# Configuration Parameters

  

   

    

     auth_delay.milliseconds (integer)
     
      auth_delay.milliseconds configuration parameter
     
    

    

     

      The number of milliseconds to wait before reporting an authentication
      failure.  The default is 0.
     

    

   

  

  

   These parameters must be set in postgresql.conf.
   Typical usage might be:
  

```
# postgresql.conf
shared_preload_libraries = 'auth_delay'

auth_delay.milliseconds = '500'
```

 

 

  
# Author

  

   KaiGai Kohei kaigai@ak.jp.nec.com
