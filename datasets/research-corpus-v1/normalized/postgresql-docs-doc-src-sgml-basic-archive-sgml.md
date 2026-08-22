# basic_archive &mdash; an example WAL archive module

 
  basic_archive
 

 

  basic_archive is an example of an archive module.  This
  module copies completed WAL segment files to the specified directory.  This
  may not be especially useful, but it can serve as a starting point for
  developing your own archive module.  For more information about archive
  modules, see .
 

 

  In order to function, this module must be loaded via
  , and 
  must be enabled.
 

 

  
# Configuration Parameters

  

   

    

     basic_archive.archive_directory (string)
     
      basic_archive.archive_directory configuration parameter
     
    

    

     

      The directory where the server should copy WAL segment files.  This
      directory must already exist.  The default is an empty string, which
      effectively halts WAL archiving, but if 
      is enabled, the server will accumulate WAL segment files in the
      expectation that a value will soon be provided.
     

    

   

  

  

   These parameters must be set in postgresql.conf.
   Typical usage might be:
  

```
# postgresql.conf
archive_mode = 'on'
archive_library = 'basic_archive'
basic_archive.archive_directory = '/path/to/archive/directory'
```

 

 

  
# Notes

  

   Server crashes may leave temporary files with the prefix
   archtemp in the archive directory.  It is recommended to
   delete such files before restarting the server after a crash.  It is safe to
   remove such files while the server is running as long as they are unrelated
   to any archiving still in progress, but users should use extra caution when
   doing so.
  

 

 

  
# Author

  

   Nathan Bossart
