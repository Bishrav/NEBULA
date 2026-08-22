# Binary String Functions and Operators

   
    binary data
    functions
   

   

    This section describes functions and operators for examining and
    manipulating binary strings, that is values of type bytea.
    Many of these are equivalent, in purpose and syntax, to the
    text-string functions described in the previous section.
   

   

    SQL defines some string functions that use
    key words, rather than commas, to separate
    arguments.  Details are in
    .
    PostgreSQL also provides versions of these functions
    that use the regular function invocation syntax
    (see ).
   

   
    
# SQL Binary String Functions and Operators

    
     
      
       

        Function/Operator
       

       

        Description
       

       

        Example(s)
       

      
     

     
      
       

        
         binary string
         concatenation
        
        bytea || bytea
        bytea
       

       

        Concatenates the two binary strings.
       

       

        '\x123456'::bytea || '\x789a00bcde'::bytea
        \x123456789a00bcde
       

      

      
       

        
         bit_length
        
        bit_length ( bytea )
        integer
       

       

        Returns number of bits in the binary string (8
        times the octet_length).
       

       

        bit_length('\x123456'::bytea)
        24
       

      

      
       

        
         btrim
        
        btrim ( 
bytes
 bytea,
        
bytesremoved
 bytea )
        bytea
       

       

        Removes the longest string containing only bytes appearing in
        
bytesremoved
 from the start and end of
        
bytes
.
       

       

        btrim('\x1234567890'::bytea, '\x9012'::bytea)
        \x345678
       

      

       
        

         
          ltrim
         
         ltrim ( 
bytes
 bytea,
         
bytesremoved
 bytea )
         bytea
        

        

         Removes the longest string containing only bytes appearing in
         
bytesremoved
 from the start of
         
bytes
.
        

        

         ltrim('\x1234567890'::bytea, '\x9012'::bytea)
         \x34567890
        

       

      
       

        
         octet_length
        
        octet_length ( bytea )
        integer
       

       

        Returns number of bytes in the binary string.
       

       

        octet_length('\x123456'::bytea)
        3
       

      

      
       

        
         overlay
        
        overlay ( 
bytes
 bytea PLACING 
newsubstring
 bytea FROM 
start
 integer  FOR 
count
 integer  )
        bytea
       

       

        Replaces the substring of 
bytes
 that starts at
        the 
start
'th byte and extends
        for 
count
 bytes
        with 
newsubstring
.
        If 
count
 is omitted, it defaults to the length
        of 
newsubstring
.
       

       

        overlay('\x1234567890'::bytea PLACING '\002\003'::bytea FROM 2 FOR 3)
        \x12020390
       

      

      
       

        
         position
        
        position ( 
substring
 bytea IN 
bytes
 bytea )
        integer
       

       

        Returns first starting index of the specified
        
substring
 within
        
bytes
, or zero if it's not present.
       

       

        position('\x5678'::bytea IN '\x1234567890'::bytea)
        3
       

      

       
        

         
          rtrim
         
         rtrim ( 
bytes
 bytea,
         
bytesremoved
 bytea )
         bytea
        

        

         Removes the longest string containing only bytes appearing in
         
bytesremoved
 from the end of
         
bytes
.
        

        

         rtrim('\x1234567890'::bytea, '\x9012'::bytea)
         \x12345678
        

       

      
       

        
         substring
        
        substring ( 
bytes
 bytea  FROM 
start
 integer   FOR 
count
 integer  )
        bytea
       

       

        Extracts the substring of 
bytes
 starting at
        the 
start
'th byte if that is specified,
        and stopping after 
count
 bytes if that is
        specified.  Provide at least one of 
start

        and 
count
.
       

       

        substring('\x1234567890'::bytea FROM 3 FOR 2)
        \x5678
       

      

      
       

        
         trim
        
        trim (  LEADING | TRAILING | BOTH 
        
bytesremoved
 bytea FROM
        
bytes
 bytea )
        bytea
       

       

        Removes the longest string containing only bytes appearing in
        
bytesremoved
 from the start,
        end, or both ends (BOTH is the default)
        of 
bytes
.
       

       

        trim('\x9012'::bytea from '\x1234567890'::bytea)
        \x345678
       

      

      
       

        trim (  LEADING | TRAILING | BOTH   FROM 
        
bytes
 bytea,
        
bytesremoved
 bytea )
        bytea
       

       

        This is a non-standard syntax for trim().
       

       

        trim(both from '\x1234567890'::bytea, '\x9012'::bytea)
        \x345678
       

      
     
    
   

   

    Additional binary string manipulation functions are available and
    are listed in .  Some
    of them are used internally to implement the
    SQL-standard string functions listed in .
   

   
    
# Other Binary String Functions

    
     
      
       

        Function
       

       

        Description
       

       

        Example(s)
       

      
     

     
      
       

        
         bit_count
        
        
         popcount
         bit_count
        
        bit_count ( 
bytes
 bytea )
        bigint
       

       

        Returns the number of bits set in the binary string (also known as
        popcount).
       

       

        bit_count('\x1234567890'::bytea)
        15
       

      

      
       

        
         crc32
        
        crc32 ( bytea )
        bigint
       

       

        Computes the CRC-32 value of the binary string.
       

       

        crc32('abc'::bytea)
        891568578
       

      

      
       

        
         crc32c
        
        crc32c ( bytea )
        bigint
       

       

        Computes the CRC-32C value of the binary string.
       

       

        crc32c('abc'::bytea)
        910901175
       

      

      
       

        
         get_bit
        
        get_bit ( 
bytes
 bytea,
        
n
 bigint )
        integer
       

       

        Extracts n'th bit
        from binary string.
       

       

        get_bit('\x1234567890'::bytea, 30)
        1
       

      

      
       

        
         get_byte
        
        get_byte ( 
bytes
 bytea,
        
n
 integer )
        integer
       

       

        Extracts n'th byte
        from binary string.
       

       

        get_byte('\x1234567890'::bytea, 4)
        144
       

      

      
       

        
         length
        
        
         binary string
         length
        
        
         length
         of a binary string
         binary strings, length
        
        length ( bytea )
        integer
       

       

        Returns the number of bytes in the binary string.
       

       

        length('\x1234567890'::bytea)
        5
       

      

      
       

        length ( 
bytes
 bytea,
        
encoding
 name )
        integer
       

       

        Returns the number of characters in the binary string, assuming
        that it is text in the given 
encoding
.
       

       

        length('jose'::bytea, 'UTF8')
        4
       

      

      
       

        
         md5
        
        md5 ( bytea )
        text
       

       

        Computes the MD5 hash of
        the binary string, with the result written in hexadecimal.
       

       

        md5('Th\000omas'::bytea)
        8ab2d3c9689aaf18&zwsp;b4958c334c82d8b1
       

      

      
       

        
         reverse
        
        reverse ( bytea )
        bytea
       

       

        Reverses the order of the bytes in the binary string.
       

       

        reverse('\xabcd'::bytea)
        \xcdab
       

      

      
       

        
         set_bit
        
        set_bit ( 
bytes
 bytea,
        
n
 bigint,
        
newvalue
 integer )
        bytea
       

       

        Sets n'th bit in
        binary string to 
newvalue
.
       

       

        set_bit('\x1234567890'::bytea, 30, 0)
        \x1234563890
       

      

      
       

        
         set_byte
        
        set_byte ( 
bytes
 bytea,
        
n
 integer,
        
newvalue
 integer )
        bytea
       

       

        Sets n'th byte in
        binary string to 
newvalue
.
       

       

        set_byte('\x1234567890'::bytea, 4, 64)
        \x1234567840
       

      

      
       

        
         sha224
        
        sha224 ( bytea )
        bytea
       

       

        Computes the SHA-224 hash
        of the binary string.
       

       

        sha224('abc'::bytea)
        \x23097d223405d8228642a477bda2&zwsp;55b32aadbce4bda0b3f7e36c9da7
       

      

      
       

        
         sha256
        
        sha256 ( bytea )
        bytea
       

       

        Computes the SHA-256 hash
        of the binary string.
       

       

        sha256('abc'::bytea)
        \xba7816bf8f01cfea414140de5dae2223&zwsp;b00361a396177a9cb410ff61f20015ad
       

      

      
       

        
         sha384
        
        sha384 ( bytea )
        bytea
       

       

        Computes the SHA-384 hash
        of the binary string.
       

       

        sha384('abc'::bytea)
        \xcb00753f45a35e8bb5a03d699ac65007&zwsp;272c32ab0eded1631a8b605a43ff5bed&zwsp;8086072ba1e7cc2358baeca134c825a7
       

      

      
       

        
         sha512
        
        sha512 ( bytea )
        bytea
       

       

        Computes the SHA-512 hash
        of the binary string.
       

       

        sha512('abc'::bytea)
        \xddaf35a193617abacc417349ae204131&zwsp;12e6fa4e89a97ea20a9eeee64b55d39a&zwsp;2192992a274fc1a836ba3c23a3feebbd&zwsp;454d4423643ce80e2a9ac94fa54ca49f
       

      

      
       

        
         substr
        
        substr ( 
bytes
 bytea, 
start
 integer , 
count
 integer  )
        bytea
       

       

        Extracts the substring of 
bytes
 starting at
        the 
start
'th byte,
        and extending for 
count
 bytes if that is
        specified.  (Same
        as substring(
bytes

        from 
start

        for 
count
).)
       

       

        substr('\x1234567890'::bytea, 3, 2)
        \x5678
       

      
    
   
  

  

   Functions get_byte and set_byte
   number the first byte of a binary string as byte 0.
   Functions get_bit and set_bit
   number bits from the right within each byte; for example bit 0 is the least
   significant bit of the first byte, and bit 15 is the most significant bit
   of the second byte.
  

  

   For historical reasons, the function md5
   returns a hex-encoded value of type text whereas the SHA-2
   functions return type bytea.  Use the functions
   encode
   and decode to
   convert between the two.  For example write encode(sha256('abc'),
   'hex') to get a hex-encoded text representation,
   or decode(md5('abc'), 'hex') to get
   a bytea value.
  

  

   
    character string
    converting to binary string
   
   
    binary string
    converting to character string
   
   Functions for converting strings between different character sets
   (encodings), and for representing arbitrary binary data in textual
   form, are shown in
   .  For these
   functions, an argument or result of type text is expressed
   in the database's default encoding, while arguments or results of
   type bytea are in an encoding named by another argument.
  

  
   
# Text/Binary String Conversion Functions

   
    
     
      

       Function
      

      

       Description
      

      

       Example(s)
      

     
    

    
     
      

       
        convert
       
       convert ( 
bytes
 bytea,
       
src_encoding
 name,
       
dest_encoding
 name )
       bytea
      

      

       Converts a binary string representing text in
       encoding 
src_encoding

       to a binary string in encoding 
dest_encoding

       (see  for
       available conversions).
      

      

       convert('text_in_utf8', 'UTF8', 'LATIN1')
       \x746578745f696e5f75746638
      

     

     
      

       
        convert_from
       
       convert_from ( 
bytes
 bytea,
       
src_encoding
 name )
       text
      

      

       Converts a binary string representing text in
       encoding 
src_encoding

       to text in the database encoding
       (see  for
       available conversions).
      

      

       convert_from('text_in_utf8', 'UTF8')
       text_in_utf8
      

     

     
      

       
        convert_to
       
       convert_to ( 
string
 text,
       
dest_encoding
 name )
       bytea
      

      

       Converts a text string (in the database encoding) to a
       binary string encoded in encoding 
dest_encoding

       (see  for
       available conversions).
      

      

       convert_to('some_text', 'UTF8')
       \x736f6d655f74657874
      

     

     
      

       
        encode
       
       encode ( 
bytes
 bytea,
       
format
 text )
       text
      

      

       Encodes binary data into a textual representation; supported
       
format
 values are:
       base32hex,
       base64,
       base64url,
       escape,
       hex.
      

      

       encode('123\000\001', 'base64')
       MTIzAAE=
      

     

     
      

       
        decode
       
       decode ( 
string
 text,
       
format
 text )
       bytea
      

      

       Decodes binary data from a textual representation; supported
       
format
 values are the same as
       for encode.
      

      

       decode('MTIzAAE=', 'base64')
       \x3132330001
      

     
    
   
  

  

   The encode and decode
   functions support the following textual formats:

   

    

     
base32hex
      
       base32hex format
      

     

      

       The base32hex format is that of
       
       RFC 4648 Section 7.  It uses the extended hex alphabet
       (0-9 and
       A-V) which preserves the sort order of
       the encoded data when compared byte-wise. The encode function
       produces output padded with '=', while decode
       accepts both padded and unpadded input. Decoding is case-insensitive and ignores
       whitespace characters.
      

      

       This format is useful for encoding UUIDs in a compact, byte-wise sortable format:
       rtrim(encode(uuid_value::bytea, 'base32hex'), '=')
       produces a 26-character string compared to the standard 36-character
       UUID representation.
      

      
       

        To maintain the lexicographical sort order of the encoded data,
        ensure that the text is sorted using the C collation
        (e.g., using COLLATE "C"). Natural language
        collations may sort characters differently and break the ordering.
       

      
     

    

    

     
base64
     
      base64 format
     

     

      

       The base64 format is that
       of RFC
       2045 Section 6.8.  As per the RFC, encoded lines are
       broken at 76 characters.  However instead of the MIME CRLF
       end-of-line marker, only a newline is used for end-of-line.
       The decode function ignores carriage-return,
       newline, space, and tab characters.  Otherwise, an error is
       raised when decode is supplied invalid
       base64 data &mdash; including when trailing padding is incorrect.
      

     

    

    

     
base64url
      
       base64url format
      

     

      

       The base64url format is that of
       
       RFC 4648 Section 5, a base64 variant safe to
       use in filenames and URLs.  The base64url alphabet
       uses '-' instead of '+' and
       '_' instead of '/' and also omits
       the '=' padding character.
      

     

    

    

     
escape
     
      escape format
     

     

      

       The escape format converts zero bytes and
       bytes with the high bit set into octal escape sequences
       (\nnn), and it doubles
       backslashes.  Other byte values are represented literally.
       The decode function will raise an error if a
       backslash is not followed by either a second backslash or three
       octal digits; it accepts other byte values unchanged.
      

     

    

    

     
hex
     
      hex format
     

     

      

       The hex format represents each 4 bits of
       data as one hexadecimal digit, 0
       through f, writing the higher-order digit of
       each byte first.  The encode function outputs
       the a-f hex digits in lower
       case.  Because the smallest unit of data is 8 bits, there are
       always an even number of characters returned
       by encode.
       The decode function
       accepts the a-f characters in
       either upper or lower case.  An error is raised
       when decode is given invalid hex data
       &mdash; including when given an odd number of characters.
      

     

    

   

  

  

   In addition, it is possible to cast integral values to and from type
   bytea. Casting an integer to bytea produces
   2, 4, or 8 bytes, depending on the width of the integer type. The result
   is the two's complement representation of the integer, with the most
   significant byte first. Some examples:

```
1234::smallint::bytea          \x04d2
cast(1234 AS bytea)            \x000004d2
cast(-1234 AS bytea)           \xfffffb2e
'\x8000'::bytea::smallint      -32768
'\x8000'::bytea::integer       32768
```

   Casting a bytea to an integer will raise an error if the
   length of the bytea exceeds the width of the integer type.
  

  

   See also the aggregate function string_agg in
    and the large object functions
   in .
