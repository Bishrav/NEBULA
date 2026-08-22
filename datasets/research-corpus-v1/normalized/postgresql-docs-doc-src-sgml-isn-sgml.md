# isn &mdash; data types for international standard numbers (ISBN, EAN, UPC, etc.)

 
  isn
 

 

  The isn module provides data types for the following
  international product numbering standards: EAN13, UPC, ISBN (books), ISMN
  (music), and ISSN (serials).  Numbers are validated on input according to a
  hard-coded list of prefixes; this list of prefixes is also used to hyphenate
  numbers on output.  Since new prefixes are assigned from time to time, the
  list of prefixes may be out of date.  It is hoped that a future version of
  this module will obtain the prefix list from one or more tables that
  can be easily updated by users as needed; however, at present, the
  list can only be updated by modifying the source code and recompiling.
  Alternatively, prefix validation and hyphenation support may be
  dropped from a future version of this module.
 

 

  This module is considered trusted, that is, it can be
  installed by non-superusers who have CREATE privilege
  on the current database.
 

 

  
# Data Types

  

    shows the data types provided by
   the isn module.
  

  
   
# isn Data Types

   
    
    
    
     
      Data Type
      Description
     
    

    
     
      EAN13
      
       European Article Numbers, always displayed in the EAN13 display format
      
     

     
      ISBN13
      
       International Standard Book Numbers to be displayed in
       the new EAN13 display format
      
     

     
      ISMN13
      
       International Standard Music Numbers to be displayed in
       the new EAN13 display format
      
     
     
      ISSN13
      
       International Standard Serial Numbers to be displayed in the new
       EAN13 display format
      
     
     
      ISBN
      
       International Standard Book Numbers to be displayed in the old
       short display format
      
     
     
      ISMN
      
       International Standard Music Numbers to be displayed in the
       old short display format
      
     
     
      ISSN
      
       International Standard Serial Numbers to be displayed in the
       old short display format
      
     
     
      UPC
      
       Universal Product Codes
      
     
    
   
  

  

   Some notes:
  

  
   

    
ISBN13, ISMN13, ISSN13 numbers are all EAN13 numbers.

   

   

    
EAN13 numbers aren't always ISBN13, ISMN13 or ISSN13 (some
    are).

   

   

    
Some ISBN13 numbers can be displayed as ISBN.

   

   

    
Some ISMN13 numbers can be displayed as ISMN.

   

   

    
Some ISSN13 numbers can be displayed as ISSN.

   

   

    
UPC numbers are a subset of the EAN13 numbers (they are basically
    EAN13 without the first 0 digit).

   

   

    
All UPC, ISBN, ISMN and ISSN numbers can be represented as EAN13
    numbers.

   

  

  

   Internally, all these types use the same representation (a 64-bit
   integer), and all are interchangeable.  Multiple types are provided
   to control display formatting and to permit tighter validity checking
   of input that is supposed to denote one particular type of number.
  

  

   The ISBN, ISMN, and ISSN types will display the
   short version of the number (ISxN 10) whenever it's possible, and will show
   ISxN 13 format for numbers that do not fit in the short version.
   The EAN13, ISBN13, ISMN13 and
   ISSN13 types will always display the long version of the ISxN
   (EAN13).
  

 

 

  
# Casts

  

   The isn module provides the following pairs of type casts:
  

  

   

    

     ISBN13 <=> EAN13
    

   

   

    

     ISMN13 <=> EAN13
    

   

   

    

     ISSN13 <=> EAN13
    

   

   

    

     ISBN <=> EAN13
    

   

   

    

     ISMN <=> EAN13
    

   

   

    

     ISSN <=> EAN13
    

   

   

    

     UPC  <=> EAN13
    

   

   

    

     ISBN <=> ISBN13
    

   

   

    

     ISMN <=> ISMN13
    

   

   

    

     ISSN <=> ISSN13
    

   

  

  

   When casting from EAN13 to another type, there is a run-time
   check that the value is within the domain of the other type, and an error
   is thrown if not.  The other casts are simply relabelings that will
   always succeed.
  

 

 

  
# Functions and Operators

  

   The isn module provides the standard comparison operators,
   plus B-tree and hash indexing support for all these data types.  In
   addition, there are several specialized functions, shown in .
   In this table,
   isn means any one of the module's data types.
  

  
   
# isn Functions

    
     
      
       

        Function
       

       

        Description
       

      
     

     
      
       

        make_valid
        make_valid ( isn )
        isn
       

       

        Clears the invalid-check-digit flag of the value.
       

      

      
       

        is_valid
        is_valid ( isn )
        boolean
       

       

        Checks for the presence of the invalid-check-digit flag.
       

      

      
       

        isn_weak
        isn_weak ( boolean )
        boolean
       

       

        Sets the weak input mode, and returns the new setting.
        This function is retained for backward compatibility.
        The recommended way to set weak mode is via
        the isn.weak configuration parameter.
       

      

      
       

        isn_weak ()
        boolean
       

       

        Returns the current status of the weak mode.
        This function is retained for backward compatibility.
        The recommended way to check weak mode is via
        the isn.weak configuration parameter.
       

      
     
    
  
 

 

  
# Configuration Parameters

  

   

    

     isn.weak (boolean)
     
      isn.weak configuration parameter
     
    

    

     

      isn.weak enables the weak input mode, which allows
      ISN input values to be accepted even when their check digit is wrong.
      The default is false, which rejects invalid check
      digits.
     

    

   

  

  

   Why would you want to use the weak mode? Well, it could be that
   you have a huge collection of ISBN numbers, and that there are so many of
   them that for weird reasons some have the wrong check digit (perhaps the
   numbers were scanned from a printed list and the OCR got the numbers wrong,
   perhaps the numbers were manually captured... who knows). Anyway, the point
   is you might want to clean the mess up, but you still want to be able to
   have all the numbers in your database and maybe use an external tool to
   locate the invalid numbers in the database so you can verify the
   information and validate it more easily; so for example you'd want to
   select all the invalid numbers in the table.
  

  

   When you insert invalid numbers in a table using the weak mode, the number
   will be inserted with the corrected check digit, but it will be displayed
   with an exclamation mark (!) at the end, for example
   0-11-000322-5!.  This invalid marker can be checked with
   the is_valid function and cleared with the
   make_valid function.
  

  

   You can also force the insertion of marked-as-invalid numbers even when not
   in the weak mode, by appending the ! character at the
   end of the number.
  

  

   Another special feature is that during input, you can write
   ? in place of the check digit, and the correct check digit
   will be inserted automatically.
  

 

 

  
# Examples

```
--Using the types directly:
SELECT isbn('978-0-393-04002-9');
SELECT isbn13('0901690546');
SELECT issn('1436-4522');

--Casting types:
-- note that you can only cast from ean13 to another type when the
-- number would be valid in the realm of the target type;
-- thus, the following will NOT work: SELECT isbn(ean13('0220356483481'));
-- but these will:
SELECT upc(ean13('0220356483481'));
SELECT ean13(upc('220356483481'));

--Create a table with a single column to hold ISBN numbers:
CREATE TABLE test (id isbn);
INSERT INTO test VALUES('9780393040029');

--Automatically calculate check digits (observe the '?'):
INSERT INTO test VALUES('220500896?');
INSERT INTO test VALUES('978055215372?');

SELECT issn('3251231?');
SELECT ismn('979047213542?');

--Using the weak mode:
SET isn.weak TO true;
INSERT INTO test VALUES('978-0-11-000533-4');
INSERT INTO test VALUES('9780141219307');
INSERT INTO test VALUES('2-205-00876-X');
SET isn.weak TO false;

SELECT id FROM test WHERE NOT is_valid(id);
UPDATE test SET id = make_valid(id) WHERE id = '2-205-00876-X!';

SELECT * FROM test;

SELECT isbn13(id) FROM test;
```

 

 

  
# Bibliography

  

   The information to implement this module was collected from
   several sites, including:
   

    

    

    

    

   

   The prefixes used for hyphenation were also compiled from:
   

    

    

    

    

    

   

   Care was taken during the creation of the algorithms and they
   were meticulously verified against the suggested algorithms
   in the official ISBN, ISMN, ISSN User Manuals.
  

 

 

  
# Author

  

   Germ&aacute;n M&eacute;ndez Bravo (Kronuz), 2004&ndash;2006
  

  

   This module was inspired by Garrett A. Wollman's
   isbn_issn code.
