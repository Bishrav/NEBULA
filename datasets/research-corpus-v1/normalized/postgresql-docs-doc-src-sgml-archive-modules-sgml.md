# Archive Modules

 
  Archive Modules
 

 

  PostgreSQL provides infrastructure to create custom modules for continuous
  archiving (see ).  While archiving via
  a shell command (i.e., ) is much
  simpler, a custom archive module will often be considerably more robust and
  performant.
 

 

  When a custom  is configured, PostgreSQL
  will submit completed WAL files to the module, and the server will avoid
  recycling or removing these WAL files until the module indicates that the files
  were successfully archived.  It is ultimately up to the module to decide what
  to do with each WAL file, but many recommendations are listed at
  .
 

 

  Archiving modules must at least consist of an initialization function (see
  ) and the required callbacks (see
  ).  However, archive modules are
  also permitted to do much more (e.g., declare GUCs and register background
  workers).
 

 

  The contrib/basic_archive module contains a working
  example, which demonstrates some useful techniques.
 

 

  
# Initialization Functions

  
   _PG_archive_module_init
  
  

   An archive library is loaded by dynamically loading a shared library with the
   's name as the library base name.  The
   normal library search path is used to locate the library.  To provide the
   required archive module callbacks and to indicate that the library is
   actually an archive module, it needs to provide a function named
   _PG_archive_module_init.  The result of the function
   must be a pointer to a struct of type
   ArchiveModuleCallbacks, which contains everything
   that the core code needs to know to make use of the archive module.  The
   return value needs to be of server lifetime, which is typically achieved by
   defining it as a static const variable in global scope.

```
typedef struct ArchiveModuleCallbacks
{
    ArchiveStartupCB startup_cb;
    ArchiveCheckConfiguredCB check_configured_cb;
    ArchiveFileCB archive_file_cb;
    ArchiveShutdownCB shutdown_cb;
} ArchiveModuleCallbacks;
typedef const ArchiveModuleCallbacks *(*ArchiveModuleInit) (void);
```

   Only the archive_file_cb callback is required.  The
   others are optional.
  

 

 

  
# Archive Module Callbacks

  

   The archive callbacks define the actual archiving behavior of the module.
   The server will call them as required to process each individual WAL file.
  

  

   
# Startup Callback

   

    The startup_cb callback is called shortly after the
    module is loaded.  This callback can be used to perform any additional
    initialization required.  If the archive module has any state, it can use
    state->private_data to store it.

```
typedef void (*ArchiveStartupCB) (ArchiveModuleState *state);
```

   

  

  

   
# Check Callback

   

    The check_configured_cb callback is called to determine
    whether the module is fully configured and ready to accept WAL files (e.g.,
    its configuration parameters are set to valid values).  If no
    check_configured_cb is defined, the server always
    assumes the module is configured.

```
typedef bool (*ArchiveCheckConfiguredCB) (ArchiveModuleState *state);
```

    If true is returned, the server will proceed with
    archiving the file by calling the archive_file_cb
    callback.  If false is returned, archiving will not
    proceed, and the archiver will emit the following message to the server log:

```
WARNING:  archive_mode enabled, yet archiving is not configured
```

    In the latter case, the server will periodically call this function, and
    archiving will proceed only when it returns true.
   

   
    

     When returning false, it may be useful to append some
     additional information to the generic warning message.  To do that, provide
     a message to the arch_module_check_errdetail macro
     before returning false.  Like
     errdetail(), this macro accepts a format string
     followed by an optional list of arguments.  The resulting string will be
     emitted as the DETAIL line of the warning message.
    

   
  

  

   
# Archive Callback

   

    The archive_file_cb callback is called to archive a
    single WAL file.

```
typedef bool (*ArchiveFileCB) (ArchiveModuleState *state, const char *file, const char *path);
```

    If true is returned, the server proceeds as if the file
    was successfully archived, which may include recycling or removing the
    original WAL file.  If false is returned or an error is thrown, the server will
    keep the original WAL file and retry archiving later.
    file will contain just the file name of the WAL
    file to archive, while path contains the full
    path of the WAL file (including the file name).
   

   
    

     The archive_file_cb callback is called in a
     short-lived memory context that will be reset between invocations.  If you
     need longer-lived storage, create a memory context in the module's
     startup_cb callback.
    

   
  

  

   
# Shutdown Callback

   

    The shutdown_cb callback is called when the archiver
    process exits (e.g., after an error) or the value of
     changes.  If no
    shutdown_cb is defined, no special action is taken in
    these situations.  If the archive module has any state, this callback should
    free it to avoid leaks.

```
typedef void (*ArchiveShutdownCB) (ArchiveModuleState *state);
```
