# Window Functions

  
   window function
   built-in
  

  

   Window functions provide the ability to perform
   calculations across sets of rows that are related to the current query
   row.  See  for an introduction to this
   feature, and  for syntax
   details.
  

  

   The built-in window functions are listed in
   .  Note that these functions
   must be invoked using window function syntax, i.e., an
   OVER clause is required.
  

  

   In addition to these functions, any built-in or user-defined
   ordinary aggregate (i.e., not ordered-set or hypothetical-set aggregates)
   can be used as a window function; see
    for a list of the built-in aggregates.
   Aggregate functions act as window functions only when an OVER
   clause follows the call; otherwise they act as plain aggregates
   and return a single row for the entire set.
  

   
    
# General-Purpose Window Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        
         row_number
        
        row_number ()
        bigint
       

       

        Returns the number of the current row within its partition, counting
        from 1.
       

      

      
       

        
         rank
        
        rank ()
        bigint
       

       

        Returns the rank of the current row, with gaps; that is,
        the row_number of the first row in its peer
        group.
       

      

      
       

        
         dense_rank
        
        dense_rank ()
        bigint
       

       

        Returns the rank of the current row, without gaps; this function
        effectively counts peer groups.
       

      

      
       

        
         percent_rank
        
        percent_rank ()
        double precision
       

       

        Returns the relative rank of the current row, that is
        (rank - 1) / (total partition rows - 1).
        The value thus ranges from 0 to 1 inclusive.
       

      

      
       

        
         cume_dist
        
        cume_dist ()
        double precision
       

       

        Returns the cumulative distribution, that is (number of partition rows
        preceding or peers with current row) / (total partition rows).
        The value thus ranges from 1/
N
 to 1.
       

      

      
       

        
         ntile
        
        ntile ( 
num_buckets
 integer )
        integer
       

       

        Returns an integer ranging from 1 to the argument value, dividing the
        partition as equally as possible.
       

      

      
       

        
         lag
        
        lag ( 
value
 anycompatible
          , 
offset
 integer
          , 
default
 anycompatible  )  
null treatment
 
        anycompatible
       

       

        Returns 
value
 evaluated at
        the row that is 
offset

        rows before the current row within the partition; if there is no such
        row, instead returns 
default

        (which must be of a type compatible with
        
value
).
        Both 
offset
 and
        
default
 are evaluated
        with respect to the current row.  If omitted,
        
offset
 defaults to 1 and
        
default
 to NULL.
       

      

      
       

        
         lead
        
        lead ( 
value
 anycompatible
          , 
offset
 integer
          , 
default
 anycompatible  )  
null treatment
 
        anycompatible
       

       

        Returns 
value
 evaluated at
        the row that is 
offset

        rows after the current row within the partition; if there is no such
        row, instead returns 
default

        (which must be of a type compatible with
        
value
).
        Both 
offset
 and
        
default
 are evaluated
        with respect to the current row.  If omitted,
        
offset
 defaults to 1 and
        
default
 to NULL.
       

      

      
       

        
         first_value
        
        first_value ( 
value
 anyelement )  
null treatment
 
        anyelement
       

       

        Returns 
value
 evaluated
        at the row that is the first row of the window frame.
       

      

      
       

        
         last_value
        
        last_value ( 
value
 anyelement )  
null treatment
 
        anyelement
       

       

        Returns 
value
 evaluated
        at the row that is the last row of the window frame.
       

      

      
       

        
         nth_value
        
        nth_value ( 
value
 anyelement, 
n
 integer )  
null treatment
 
        anyelement
       

       

        Returns 
value
 evaluated
        at the row that is the 
n
'th
        row of the window frame (counting from 1);
        returns NULL if there is no such row.
       

      
     
    
   

  

   All of the functions listed in
    depend on the sort ordering
   specified by the ORDER BY clause of the associated window
   definition.  Rows that are not distinct when considering only the
   ORDER BY columns are said to be peers.
   The four ranking functions (including cume_dist) are
   defined so that they give the same answer for all rows of a peer group.
  

  

   Note that first_value, last_value, and
   nth_value consider only the rows within the window
   frame, which by default contains the rows from the start of the
   partition through the last peer of the current row.  This is
   likely to give unhelpful results for last_value and
   sometimes also nth_value.  You can redefine the frame by
   adding a suitable frame specification (RANGE,
   ROWS or GROUPS) to
   the OVER clause.
   See  for more information
   about frame specifications.
  

  

   When an aggregate function is used as a window function, it aggregates
   over the rows within the current row's window frame.
   An aggregate used with ORDER BY and the default window frame
   definition produces a running sum type of behavior, which may or
   may not be what's wanted.  To obtain
   aggregation over the whole partition, omit ORDER BY or use
   ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING.
   Other frame specifications can be used to obtain other effects.
  

  

   The null treatment option must be one of:

RESPECT NULLS
IGNORE NULLS

   If unspecified, the default is RESPECT NULLS which includes NULL
   values in any result calculation. IGNORE NULLS ignores NULL values.
   This option is only allowed for the following functions: lag,
   lead, first_value, last_value,
   nth_value.
  

  
   

    The SQL standard defines a FROM FIRST or FROM LAST
    option for nth_value. This is not implemented in
    PostgreSQL: only the default FROM FIRST
    behavior is supported. (You can achieve the result of FROM LAST by
    reversing the ORDER BY
    ordering.)
