# Functions and Operators

  
   function
  

  
   operator
  

  

   PostgreSQL provides a large number of
   functions and operators for the built-in data types.  This chapter
   describes most of them, although additional special-purpose functions
   appear in relevant sections of the manual.  Users can also
   define their own functions and operators, as described in
   .  The
   psql commands \df and
   \do can be used to list all
   available functions and operators, respectively.
  

  

   The notation used throughout this chapter to describe the argument and
   result data types of a function or operator is like this:

repeat ( text, integer ) text

   which says that the function repeat takes one text and
   one integer argument and returns a result of type text.  The right arrow
   is also used to indicate the result of an example, thus:

```
repeat('Pg', 4) PgPgPgPg
```

  

  

   If you are concerned about portability then note that most of
   the functions and operators described in this chapter, with the
   exception of the most trivial arithmetic and comparison operators
   and some explicitly marked functions, are not specified by the
   SQL standard. Some of this extended functionality
   is present in other SQL database management
   systems, and in many cases this functionality is compatible and
   consistent between the various implementations.
  

&func-logical;
&func-comparison;
&func-math;
&func-string;
&func-binarystring;
&func-bitstring;
&func-matching;
&func-formatting;
&func-datetime;
&func-enum;
&func-geometry;
&func-net;
&func-textsearch;
&func-tid;
&func-uuid;
&func-xml;
&func-json;
&func-sequence;
&func-conditional;
&func-array;
&func-range;
&func-aggregate;
&func-window;
&func-merge-support;
&func-subquery;
&func-comparisons;
&func-srf;
&func-info;
&func-admin;
&func-trigger;
&func-event-triggers;
&func-statistics;
