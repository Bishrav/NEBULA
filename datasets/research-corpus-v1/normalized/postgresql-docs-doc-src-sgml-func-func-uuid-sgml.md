# UUID Functions

  
   UUID
   generating
  

  
   gen_random_uuid
  

  
   uuidv4
  

  
   uuidv7
  

  
   uuid_extract_timestamp
  

  
   uuid_extract_version
  

  

    shows the PostgreSQL
   functions that can be used to generate UUIDs.
  

  
   
# UUID Generation Functions

   
    
     
      

        Function
       

       

        Description
        

       

        Example(s)
       

     
    

    
     
      

        gen_random_uuid ( )
        uuid
       

       

        uuidv4 ( )
        uuid
       

       

        Generates a version 4 (random) UUID
       

       

        gen_random_uuid()
        5b30857f-0bfa-48b5-ac0b-5c64e28078d1
       

       

        uuidv4()
        b42410ee-132f-42ee-9e4f-09a6485c95b8
       

     
     
      

        uuidv7
        (  
shift
 interval  )
        uuid
       

       

        Generates a version 7 (time-ordered) UUID. The timestamp is
        computed using UNIX timestamp with millisecond precision +
        sub-millisecond timestamp + random. The optional
        parameter 
shift
 will shift the computed
        timestamp by the given interval.
        Infinite interval values are not accepted.
        The shifted timestamp must fall within the range supported by
        UUID version 7's 48-bit millisecond timestamp field: from
        1970-01-01 00:00:00 UTC to approximately year 10889.
        An error is raised if the resulting timestamp is outside this
        range.
       

       

        uuidv7()
        019535d9-3df7-79fb-b466-fa907fa17f9e
       

     
    
   
  

  
   

    The  module provides additional functions that
    implement other standard algorithms for generating UUIDs.
   

  

  

    shows the PostgreSQL
   functions that can be used to extract information from UUIDs.
  

  
   
# UUID Extraction Functions

   
    
     
      

        Function
       

       

        Description
       

       

        Example(s)
       

     
    

    
     
      

        uuid_extract_timestamp
        ( uuid )
        timestamp with time zone
       

       

        Extracts a timestamp with time zone from a UUID of
        version 1, 6, or 7.  For other versions, this function returns null.
        Note that the extracted timestamp is not necessarily exactly equal
        to the time the UUID was generated; this depends on the
        implementation that generated the UUID.
       

       

        uuid_extract_timestamp('019535d9-3df7-79fb-b466-&zwsp;fa907fa17f9e'::uuid)
         2025-02-23 21:46:24.503-05
       

     
     
      

        uuid_extract_version
        ( uuid )
        smallint
       

       

        Extracts the version from a UUID of one of the variants described by
        RFC
        9562.  For other variants, this function returns null.
        For example, for a UUID generated
        by gen_random_uuid(), this function will
        return 4.
       

       

        uuid_extract_version('41db1265-8bc1-4ab3-992f-&zwsp;885799a4af1d'::uuid)
        4
       

       

        uuid_extract_version('019535d9-3df7-79fb-b466-&zwsp;fa907fa17f9e'::uuid)
        7
       

     
    
   
  

  

   PostgreSQL also provides the usual comparison
   operators shown in  for
   UUIDs.
  

  

   See  for details on the data type
   uuid in PostgreSQL.
