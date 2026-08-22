# TID Functions

  
   TID
   functions
  

  
   tid_block
  

  
   tid_offset
  

  

    lists functions for
   the tid data type (tuple identifier).
  

  
   
# TID Functions

   
    
     
      

       Function
      

      

       Description
      

      

       Example(s)
      

     
    

    
     
      

       tid_block ( tid )
       bigint
      

      

       Extracts the block number from a tuple identifier.
      

      

       tid_block('(42,7)'::tid)
       42
      

     

     
      

       tid_offset ( tid )
       integer
      

      

       Extracts the tuple offset within the block from a tuple identifier.
      

      

       tid_offset('(42,7)'::tid)
       7
