# pg_stash_advice &mdash; store and automatically apply plan advice

 
  pg_stash_advice
 

 

  The pg_stash_advice extension allows you to stash
  plan advice strings in dynamic
  shared memory where they can be automatically applied. An
  advice stash is a mapping from
  query identifiers to plan advice
  strings. Whenever a session is asked to plan a query whose query ID appears
  in the relevant advice stash, the plan advice string is automatically applied
  to guide planning. Note that advice stashes are stored in dynamically
  allocated shared memory. This means that it is important to be mindful
  of memory consumption when deciding how much plan advice to stash.
  Optionally, advice stashes and their contents can automatically be persisted
  to disk and reloaded from disk; see
  pg_stash_advice.persist, below.
 

 

  In order to use this module, you will need to execute
  CREATE EXTENSION pg_stash_advice in at least
  one database, so that you have access to the SQL functions to manage
  advice stashes. You will also need the pg_stash_advice
  module to be loaded in all sessions where you want this module to
  automatically apply advice. It will usually be best to do this by adding
  pg_stash_advice to
   and restarting the server.
 

 

  Once you have met the above criteria, you can create advice stashes
  using the pg_create_advice_stash function described
  below and set the plan advice for a given query ID in a given stash using
  the pg_set_stashed_advice function. Then, you need
  only configure pg_stash_advice.stash_name to point
  to the chosen advice stash name. For some use cases, rather than setting
  this on a system-wide basis, you may find it helpful to use
  ALTER DATABASE ... SET or
  ALTER ROLE ... SET to configure values that will apply
  only to a database or only to a certain role. Likewise, it may sometimes
  be better to set the stash name in a particular session using
  SET.
 

 

  Because pg_stash_advice works on the basis of query
  identifiers, you will need to determine the query identifier for each query
  whose plan you wish to control. You will also need to determine the advice
  string that you wish to store for each query. One way to do this is to use
  EXPLAIN: the VERBOSE option will
  show the query ID, and the PLAN_ADVICE option will
  show plan advice.  Query identifiers can also be obtained through tools
  such as  or
  , but these tools
  will not provide plan advice strings. Note that
   must be enabled for query
  identifiers to be computed; if set to auto, loading
  pg_stash_advice will enable it automatically.
 

 

  Generally, the fact that the planner is able to change query plans as
  the underlying distribution of data changes is a feature, not a bug.
  Moreover, applying plan advice can have a noticeable performance cost even
  when it does not result in a change to the query plan. Therefore, it is
  a good idea to use this feature only when and to the extent needed.
  Plan advice strings can be trimmed down to mention only those aspects
  of the plan that need to be controlled, and used only for queries where
  there is believed to be a significant risk of planner error.
 

 

  Note that pg_stash_advice currently lacks a sophisticated
  security model. Only the superuser, or a user to whom the superuser has
  granted EXECUTE permission on the relevant functions,
  may create advice stashes or alter their contents, but any user may set
  pg_stash_advice.stash_name for their session, and this
  may reveal the contents of any advice stash with that name. Users should
  assume that information embedded in stashed advice strings may become visible
  to non-privileged users.
 

 

  
# Functions

  

   

    

     pg_create_advice_stash(stash_name text) returns void
     
      pg_create_advice_stash
     
    

    

     

      Creates a new, empty advice stash with the given name.
     

    

   

   

    

     pg_drop_advice_stash(stash_name text) returns void
     
      pg_drop_advice_stash
     
    

    

     

      Drops the named advice stash and all of its entries.
     

    

   

   

    

     pg_set_stashed_advice(stash_name text, query_id bigint,
       advice_string text) returns void
     
      pg_set_stashed_advice
     
    

    

     

      Stores an advice string in the named advice stash, associated with
      the given query identifier. If an entry for that query identifier
      already exists in the stash, it is replaced. If
      
advice_string
 is NULL,
      any existing entry for that query identifier is removed.
     

    

   

   

    

     pg_get_advice_stashes() returns setof (stash_name text,
       num_entries bigint)
     
      pg_get_advice_stashes
     
    

    

     

      Returns one row for each advice stash, showing the stash name and
      the number of entries it contains.
     

    

   

   

    

     pg_get_advice_stash_contents(stash_name text) returns setof
       (stash_name text, query_id bigint, advice_string text)
     
      pg_get_advice_stash_contents
     
    

    

     

      Returns one row for each entry in the named advice stash. If
      
stash_name
 is NULL, returns
      entries from all stashes.
     

    

   

   

    

     pg_start_stash_advice_worker() returns void
     
      pg_start_stash_advice_worker
     
    

    

     

      Starts the background worker, so that advice stash contents can be
      automatically persisted to disk.  If this module is included in
       at startup time with
      pg_stash_advice.persist = true, the worker will be
      started automatically. When started manually, the worker will not load
      anything from disk, but it will still persist data to disk. You can then
      configure the server to start the worker automatically after the next
      restart, preserving any stashed advice you add now.
     

    

   

  

 

 

  
# Configuration Parameters

  

   

    

     pg_stash_advice.persist (boolean)
     
      pg_stash_advice.persist configuration parameter
     
    

    

     

      Controls whether the advice stashes and stash entries should be
      persisted to disk. This is on by default. If any stashes are persisted,
      a file named pg_stash_advice.tsv will be created in
      the data directory. Stashes are loaded and saved using a background
      worker process.  This parameter can only be set at server start.
     

    

   

   

    

     pg_stash_advice.persist_interval (integer)
     
      pg_stash_advice.persist_interval configuration parameter
     
    

    

     

      Specifies the interval, in seconds, between checks for changes that
      need to be written to pg_stash_advice.tsv. If set to
      zero, changes are only written when the server shuts down. The default
      value is 30. This parameter can only be set in the
      postgresql.conf file or on the server command line.
     

    

   

   

    

     pg_stash_advice.stash_name (string)
     
      pg_stash_advice.stash_name configuration parameter
     
    

    

     

      Specifies the name of the advice stash to consult during query
      planning. The default value is the empty string, which disables
      this module.
     

    

   

  

 

 

  
# Author

  

   Robert Haas rhaas@postgresql.org
