# Bit String Functions and Operators

   
    bit strings
    functions
   

   

    This section describes functions and operators for examining and
    manipulating bit strings, that is values of the types
    bit and bit varying.  (While only
    type bit is mentioned in these tables, values of
    type bit varying can be used interchangeably.)
    Bit strings support the usual comparison operators shown in
    , as well as the
    operators shown in .
   

   
    
# Bit String Operators

    
     
      
       

        Operator
       

       

        Description
       

       

        Example(s)
       

      
     

     
      
       

        bit || bit
        bit
       

       

        Concatenation
       

       

        B'10001' || B'011'
        10001011
       

      

      
       

        bit & bit
        bit
       

       

        Bitwise AND (inputs must be of equal length)
       

       

        B'10001' & B'01101'
        00001
       

      

      
       

        bit | bit
        bit
       

       

        Bitwise OR (inputs must be of equal length)
       

       

        B'10001' | B'01101'
        11101
       

      

      
       

        bit # bit
        bit
       

       

        Bitwise exclusive OR (inputs must be of equal length)
       

       

        B'10001' # B'01101'
        11100
       

      

      
       

        ~ bit
        bit
       

       

        Bitwise NOT
       

       

        ~ B'10001'
        01110
       

      

      
       

        bit << integer
        bit
       

       

        Bitwise shift left
        (string length is preserved)
       

       

        B'10001' << 3
        01000
       

      

      
       

        bit >> integer
        bit
       

       

        Bitwise shift right
        (string length is preserved)
       

       

        B'10001' >> 2
        00100
       

      
     
    
   

   

    Some of the functions available for binary strings are also available
    for bit strings, as shown in .
   

   
    
# Bit String Functions

    
     
      
       

        Function
       

       

        Description
       

       

        Example(s)
       

      
     

     
      
       

        
         bit_count
        
        bit_count ( bit )
        bigint
       

       

        Returns the number of bits set in the bit string (also known as
        popcount).
       

       

        bit_count(B'10111')
        4
       

      

      
       

        
         bit_length
        
        bit_length ( bit )
        integer
       

       

        Returns number of bits in the bit string.
       

       

        bit_length(B'10111')
        5
       

      

      
       

        
         length
        
        
         bit string
         length
        
        length ( bit )
        integer
       

       

        Returns number of bits in the bit string.
       

       

        length(B'10111')
        5
       

      

      
       

        
         octet_length
        
        octet_length ( bit )
        integer
       

       

        Returns number of bytes in the bit string.
       

       

        octet_length(B'1011111011')
        2
       

      

      
       

        
         overlay
        
        overlay ( 
bits
 bit PLACING 
newsubstring
 bit FROM 
start
 integer  FOR 
count
 integer  )
        bit
       

       

        Replaces the substring of 
bits
 that starts at
        the 
start
'th bit and extends
        for 
count
 bits
        with 
newsubstring
.
        If 
count
 is omitted, it defaults to the length
        of 
newsubstring
.
       

       

        overlay(B'01010101010101010' PLACING B'11111' FROM 2 FOR 3)
        0111110101010101010
       

      

      
       

        
         position
        
        position ( 
substring
 bit IN 
bits
 bit )
        integer
       

       

        Returns first starting index of the specified 
substring

        within 
bits
, or zero if it's not present.
       

       

        position(B'010' IN B'000001101011')
        8
       

      

      
       

        
         substring
        
        substring ( 
bits
 bit  FROM 
start
 integer   FOR 
count
 integer  )
        bit
       

       

        Extracts the substring of 
bits
 starting at
        the 
start
'th bit if that is specified,
        and stopping after 
count
 bits if that is
        specified.  Provide at least one of 
start

        and 
count
.
       

       

        substring(B'110010111111' FROM 3 FOR 2)
        00
       

      

      
       

        
         get_bit
        
        get_bit ( 
bits
 bit,
        
n
 integer )
        integer
       

       

        Extracts 
n
'th bit
        from bit string; the first (leftmost) bit is bit 0.
       

       

        get_bit(B'101010101010101010', 6)
        1
       

      

      
       

        
         set_bit
        
        set_bit ( 
bits
 bit,
        
n
 integer,
        
newvalue
 integer )
        bit
       

       

        Sets 
n
'th bit in
        bit string to 
newvalue
;
        the first (leftmost) bit is bit 0.
       

       

        set_bit(B'101010101010101010', 6, 0)
        101010001010101010
       

      
     
    
   

   

    In addition, it is possible to cast integral values to and from type
    bit.
    Casting an integer to bit(n) copies the rightmost
    n bits.  Casting an integer to a bit string width wider
    than the integer itself will sign-extend on the left.
    Some examples:

```
44::bit(10)                    0000101100
44::bit(3)                     100
cast(-44 AS bit(12))           111111010100
'1110'::bit(4)::integer        14
```

    Note that casting to just bit means casting to
    bit(1), and so will deliver only the least significant
    bit of the integer.
