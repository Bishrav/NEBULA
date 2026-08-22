# Additional Supplied Modules and Extensions

 

  This appendix and the next one contain information on the
  optional components
  found in the contrib directory of the
  PostgreSQL distribution.
  These include porting tools, analysis utilities,
  and plug-in features that are not part of the core PostgreSQL system.
  They are separate mainly
  because they address a limited audience or are too experimental
  to be part of the main source tree.  This does not preclude their
  usefulness.
 

 

  This appendix covers extensions and other server plug-in module
  libraries found in
  contrib.   covers utility
  programs.
 

 

  When building from the source distribution, these optional
  components are not built
  automatically, unless you build the "world" target
  (see ).
  You can build and install all of them by running:

```
make
make install
```

  in the contrib directory of a configured source tree;
  or to build and install
  just one selected module, do the same in that module's subdirectory.
  Many of the modules have regression tests, which can be executed by
  running:

```
make check
```

  before installation or

```
make installcheck
```

  once you have a PostgreSQL server running.
 

 

  If you are using a pre-packaged version of PostgreSQL,
  these components are typically made available as a separate subpackage,
  such as postgresql-contrib.
 

 

  Many components supply new user-defined functions, operators, or types,
  packaged as extensions.
  To make use of one of these extensions, after you have installed the code
  you need to register the new SQL objects in the database system.
  This is done by executing
  a  command.  In a fresh database,
  you can simply do

```
CREATE EXTENSION extension_name;
```

  This command registers the new SQL objects in the current database only,
  so you need to run it in every database in which you want
  the extension's facilities to be available.  Alternatively, run it in
  database template1 so that the extension will be copied into
  subsequently-created databases by default.
 

 

  For all extensions, the CREATE EXTENSION command must be
  run by a database superuser, unless the extension is
  considered trusted.  Trusted extensions can be run by any
  user who has CREATE privilege on the current
  database.  Extensions that are trusted are identified as such in the
  sections that follow.  Generally, trusted extensions are ones that cannot
  provide access to outside-the-database functionality.
 

 

  The following extensions are trusted in a default installation:

  
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
  
 

 

  Many extensions allow you to install their objects in a schema of your
  choice.  To do that, add SCHEMA
  schema_name to the CREATE EXTENSION
  command.  By default, the objects will be placed in your current creation
  target schema, which in turn defaults to public.
 

 

  Note, however, that some of these components are not extensions
  in this sense, but are loaded into the server in some other way, for instance
  by way of
  .  See the documentation of each
  component for details.
 

 &amcheck;
 &auth-delay;
 &auto-explain;
 &basebackup-to-shell;
 &basic-archive;
 &bloom;
 &btree-gin;
 &btree-gist;
 &citext;
 &cube;
 &dblink;
 &dict-int;
 &dict-xsyn;
 &earthdistance;
 &file-fdw;
 &fuzzystrmatch;
 &hstore;
 &intagg;
 &intarray;
 &isn;
 &lo;
 &ltree;
 &pageinspect;
 &passwordcheck;
 &pgbuffercache;
 &pgcrypto;
 &pgfreespacemap;
 &pglogicalinspect;
 &pgoverexplain;
 &pgplanadvice;
 &pgprewarm;
 &pgrowlocks;
 &pgstashadvice;
 &pgstatstatements;
 &pgstattuple;
 &pgsurgery;
 &pgtrgm;
 &pgvisibility;
 &pgwalinspect;
 &postgres-fdw;
 &seg;
 &sepgsql;
 &contrib-spi;
 &sslinfo;
 &tablefunc;
 &tcn;
 &test-decoding;
 &tsm-system-rows;
 &tsm-system-time;
 &unaccent;
 &uuid-ossp;
 &xml2;

 
# Additional Supplied Programs

 

  This appendix and the previous one contain information regarding the modules that
  can be found in the contrib directory of the
  PostgreSQL distribution.  See  for
  more information about the contrib section in general and
  server extensions and plug-ins found in contrib
  specifically.
 

 

  This appendix covers utility programs found in contrib.
  Once installed, either from source or a packaging system, they are found in
  the bin directory of the
  PostgreSQL installation and can be used like any
  other program.
 

 

  
# Client Applications

  

   This section covers PostgreSQL client
   applications in contrib.  They can be run from anywhere,
   independent of where the database server resides.  See
   also  for information about client
   applications that are part of the core PostgreSQL
   distribution.
  

 &oid2name;
 &vacuumlo;
 

 

  
# Server Applications

  

   Some applications run on the PostgreSQL server
   itself.  Currently, no such applications are included in the
   contrib directory.  See also  for information about server applications that
   are part of the core PostgreSQL distribution.
