# pg_logicalinspect &mdash; logical decoding components inspection

 
  pg_logicalinspect
 

 

  The pg_logicalinspect module provides SQL functions
  that allow you to inspect the contents of logical decoding components. It
  allows the inspection of serialized logical snapshots of a running
  PostgreSQL database cluster, which is useful
  for debugging or educational purposes.
 

 

  By default, use of these functions is restricted to superusers and members of
  the pg_read_server_files role. Access may be granted by
  superusers to others using GRANT.
 

 

  
# Functions

  

   

    

     pg_get_logical_snapshot_meta(filename text) returns record
    

    

     

      Gets logical snapshot metadata about a snapshot file that is located in
      the server's pg_logical/snapshots directory.
      The filename argument represents the snapshot
      file name.
      For example:

```
postgres=# SELECT * FROM pg_ls_logicalsnapdir();
-[ RECORD 1 ]+-----------------------
name         | 0-40796E18.snap
size         | 152
modification | 2024-08-14 16:36:32+00

postgres=# SELECT * FROM pg_get_logical_snapshot_meta('0-40796E18.snap');
-[ RECORD 1 ]--------
magic    | 1369563137
checksum | 1028045905
version  | 6

postgres=# SELECT ss.name, meta.* FROM pg_ls_logicalsnapdir() AS ss,
pg_get_logical_snapshot_meta(ss.name) AS meta;
-[ RECORD 1 ]-------------
name     | 0-40796E18.snap
magic    | 1369563137
checksum | 1028045905
version  | 6
```

     

     

      If filename does not match a snapshot file, the
      function raises an error.
     

    

   

   

    

     pg_get_logical_snapshot_info(filename text) returns record
    

    

     

      Gets logical snapshot information about a snapshot file that is located in
      the server's pg_logical/snapshots directory.
      The filename argument represents the snapshot
      file name.
      For example:

```
postgres=# SELECT * FROM pg_ls_logicalsnapdir();
-[ RECORD 1 ]+-----------------------
name         | 0-40796E18.snap
size         | 152
modification | 2024-08-14 16:36:32+00

postgres=# SELECT * FROM pg_get_logical_snapshot_info('0-40796E18.snap');
-[ RECORD 1 ]------------+-----------
state                    | consistent
xmin                     | 751
xmax                     | 751
start_decoding_at        | 0/40796AF8
two_phase_at             | 0/40796AF8
initial_xmin_horizon     | 0
building_full_snapshot   | f
in_slot_creation         | f
last_serialized_snapshot | 0/00000000
next_phase_at            | 0
committed_count          | 0
committed_xip            |
catchange_count          | 2
catchange_xip            | {751,752}

postgres=# SELECT ss.name, info.* FROM pg_ls_logicalsnapdir() AS ss,
pg_get_logical_snapshot_info(ss.name) AS info;
-[ RECORD 1 ]------------+----------------
name                     | 0-40796E18.snap
state                    | consistent
xmin                     | 751
xmax                     | 751
start_decoding_at        | 0/40796AF8
two_phase_at             | 0/40796AF8
initial_xmin_horizon     | 0
building_full_snapshot   | f
in_slot_creation         | f
last_serialized_snapshot | 0/00000000
next_phase_at            | 0
committed_count          | 0
committed_xip            |
catchange_count          | 2
catchange_xip            | {751,752}
```

     

     

      If filename does not match a snapshot file, the
      function raises an error.
     

    

   

  

 

 

  
# Author

  

   Bertrand Drouvot bertranddrouvot.pg@gmail.com
