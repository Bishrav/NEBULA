# String Functions and Operators

   

    This section describes functions and operators for examining and
    manipulating string values.  Strings in this context include values
    of the types character, character varying,
    and text.  Except where noted, these functions and operators
    are declared to accept and return type text.  They will
    interchangeably accept character varying arguments.
    Values of type character will be converted
    to text before the function or operator is applied, resulting
    in stripping any trailing spaces in the character value.
   

   

    SQL defines some string functions that use
    key words, rather than commas, to separate
    arguments.  Details are in
    .
    PostgreSQL also provides versions of these functions
    that use the regular function invocation syntax
    (see ).
   

   
    

     The string concatenation operator (||) will accept
     non-string input, so long as at least one input is of string type, as shown
     in .  For other cases, inserting an
     explicit coercion to text can be used to have non-string input
     accepted.
    

   

   
    
# SQL String Functions and Operators

    
     
      
       

        Function/Operator
       

       

        Description
       

       

        Example(s)
       

      
     

     
      
       

        
         character string
         concatenation
        
        text || text
        text
       

       

        Concatenates the two strings.
       

       

        'Post' || 'greSQL'
        PostgreSQL
       

      

      
       

        text || anynonarray
        text
       

       

        anynonarray || text
        text
       

       

        Converts the non-string input to text, then concatenates the two
        strings.  (The non-string input cannot be of an array type, because
        that would create ambiguity with the array ||
        operators.  If you want to concatenate an array's text equivalent,
        cast it to text explicitly.)
       

       

        'Value: ' || 42
        Value: 42
       

      

      
       

        
         btrim
        
        btrim ( 
string
 text
        , 
characters
 text  )
        text
       

       

        Removes the longest string containing only characters
        in 
characters
 (a space by default)
        from the start and end of 
string
.
       

       

        btrim('xyxtrimyyx', 'xyz')
        trim
       

      

      
       

        
         normalized
        
        
         Unicode normalization
        
         text IS NOT 
form
 NORMALIZED
        boolean
       

       

        Checks whether the string is in the specified Unicode normalization
        form.  The optional 
form
 key word specifies the
        form: NFC (the default), NFD,
        NFKC, or NFKD.  This expression can
        only be used when the server encoding is UTF8.  Note
        that checking for normalization using this expression is often faster
        than normalizing possibly already normalized strings.
       

       

        U&'\0061\0308bc' IS NFD NORMALIZED
        t
       

      

      
       

        
         bit_length
        
        bit_length ( text )
        integer
       

       

        Returns number of bits in the string (8
        times the octet_length).
       

       

        bit_length('jose')
        32
       

      

      
       

        
         char_length
        
        
         character string
         length
        
        
         length
         of a character string
         character string, length
        
        char_length ( text )
        integer
       

       

        
         character_length
        
        character_length ( text )
        integer
       

       

        Returns number of characters in the string.
       

       

        char_length('jos&eacute;')
        4
       

      

      
       

        
         lower
        
        lower ( text )
        text
       

       

        Converts the string to all lower case, according to the rules of the
        database's locale.
       

       

        lower('TOM')
        tom
       

      

      
       

        
         lpad
        
        lpad ( 
string
 text,
        
length
 integer
        , 
fill
 text  )
        text
       

       

        Extends the 
string
 to length
        
length
 by prepending the characters
        
fill
 (a space by default).  If the
        
string
 is already longer than
        
length
 then it is truncated (on the right).
       

       

        lpad('hi', 5, 'xy')
        xyxhi
       

      

      
       

        
         ltrim
        
        ltrim ( 
string
 text
        , 
characters
 text  )
        text
       

       

        Removes the longest string containing only characters in
        
characters
 (a space by default) from the start of
        
string
.
       

       

        ltrim('zzzytest', 'xyz')
        test
       

      

      
       

        
         normalize
        
        
         Unicode normalization
        
        normalize ( text
        , 
form
  )
        text
       

       

        Converts the string to the specified Unicode
        normalization form.  The optional 
form
 key word
        specifies the form: NFC (the default),
        NFD, NFKC, or
        NFKD.  This function can only be used when the
        server encoding is UTF8.
       

       

        normalize(U&'\0061\0308bc', NFC)
        U&'\00E4bc'
       

      

      
       

        
         octet_length
        
        octet_length ( text )
        integer
       

       

        Returns number of bytes in the string.
       

       

        octet_length('jos&eacute;')
        5 (if server encoding is UTF8)
       

      

      
       

        
         octet_length
        
        octet_length ( character )
        integer
       

       

        Returns number of bytes in the string.  Since this version of the
        function accepts type character directly, it will not
        strip trailing spaces.
       

       

        octet_length('abc '::character(4))
        4
       

      

      
       

        
         overlay
        
        overlay ( 
string
 text PLACING 
newsubstring
 text FROM 
start
 integer  FOR 
count
 integer  )
        text
       

       

        Replaces the substring of 
string
 that starts at
        the 
start
'th character and extends
        for 
count
 characters
        with 
newsubstring
.
        If 
count
 is omitted, it defaults to the length
        of 
newsubstring
.
       

       

        overlay('Txxxxas' PLACING 'hom' FROM 2 FOR 4)
        Thomas
       

      

      
       

        
         position
        
        position ( 
substring
 text IN 
string
 text )
        integer
       

       

        Returns first starting index of the specified
        
substring
 within
        
string
, or zero if it's not present.
       

       

        position('om' IN 'Thomas')
        3
       

      

      
       

        
         rpad
        
        rpad ( 
string
 text,
        
length
 integer
        , 
fill
 text  )
        text
       

       

        Extends the 
string
 to length
        
length
 by appending the characters
        
fill
 (a space by default).  If the
        
string
 is already longer than
        
length
 then it is truncated.
       

       

        rpad('hi', 5, 'xy')
        hixyx
       

      

      
       

        
         rtrim
        
        rtrim ( 
string
 text
         , 
characters
 text  )
        text
       

       

        Removes the longest string containing only characters in
        
characters
 (a space by default) from the end of
        
string
.
       

       

        rtrim('testxxzx', 'xyz')
        test
       

      

      
       

        
         substring
        
        substring ( 
string
 text  FROM 
start
 integer   FOR 
count
 integer  )
        text
       

       

        Extracts the substring of 
string
 starting at
        the 
start
'th character if that is specified,
        and stopping after 
count
 characters if that is
        specified.  Provide at least one of 
start

        and 
count
.
       

       

        substring('Thomas' FROM 2 FOR 3)
        hom
       

       

        substring('Thomas' FROM 3)
        omas
       

       

        substring('Thomas' FOR 2)
        Th
       

      

      
       

        substring ( 
string
 text FROM 
pattern
 text )
        text
       

       

        Extracts the first substring matching POSIX regular expression; see
        .
       

       

        substring('Thomas' FROM '...$')
        mas
       

      

      
       

        substring ( 
string
 text SIMILAR 
pattern
 text ESCAPE 
escape
 text )
        text
       

       

        substring ( 
string
 text FROM 
pattern
 text FOR 
escape
 text )
        text
       

       

        Extracts the first substring matching SQL regular expression;
        see .  The first form has
        been specified since SQL:2003; the second form was only in SQL:1999
        and should be considered obsolete.
       

       

        substring('Thomas' SIMILAR '%#"o_a#"_' ESCAPE '#')
        oma
       

      

      
       

        
         trim
        
        trim (  LEADING | TRAILING | BOTH 
         
characters
 text  FROM
        
string
 text )
        text
       

       

        Removes the longest string containing only characters in
        
characters
 (a space by default) from the
        start, end, or both ends (BOTH is the default)
        of 
string
.
       

       

        trim(both 'xyz' from 'yxTomxx')
        Tom
       

      

      
       

        trim (  LEADING | TRAILING | BOTH   FROM 
        
string
 text ,
        
characters
 text  )
        text
       

       

        This is a non-standard syntax for trim().
       

       

        trim(both from 'yxTomxx', 'xyz')
        Tom
       

      

      
       

        
         unicode_assigned
        
        unicode_assigned ( text )
        boolean
       

       

        Returns true if all characters in the string are
        assigned Unicode codepoints; false otherwise. This
        function can only be used when the server encoding is
        UTF8.
       

      

      
       

        
         upper
        
        upper ( text )
        text
       

       

        Converts the string to all upper case, according to the rules of the
        database's locale.
       

       

        upper('tom')
        TOM
       

      
     
    
   

   

    Additional string manipulation functions and operators are available
    and are listed in .  (Some of
    these are used internally to implement
    the SQL-standard string functions listed in
    .)
    There are also pattern-matching operators, which are described in
    , and operators for full-text
    search, which are described in .
   

   
    
# Other String Functions and Operators

    
     
      
       

        Function/Operator
       

       

        Description
       

       

        Example(s)
       

      
     

     
      
       

        
         character string
         prefix test
        
        text ^@ text
        boolean
       

       

        Returns true if the first string starts with the second string
        (equivalent to the starts_with() function).
       

       

        'alphabet' ^@ 'alph'
        t
       

      

      
       

        
         ascii
        
        ascii ( text )
        integer
       

       

        Returns the numeric code of the first character of the argument.
        In UTF8 encoding, returns the Unicode code point
        of the character.  In other multibyte encodings, the argument must
        be an ASCII character.
       

       

        ascii('x')
        120
       

      

      
       

        
         chr
        
        chr ( integer )
        text
       

       

        Returns the character with the given code. In UTF8
        encoding the argument is treated as a Unicode code point. In other
        multibyte encodings the argument must designate
        an ASCII character.  chr(0) is
        disallowed because text data types cannot store that character.
      

      

        chr(65)
        A
       

      

      
       

        
         concat
        
        concat ( 
val1
 "any"
         , 
val2
 "any" , ...  )
        text
       

       

        Concatenates the text representations of all the arguments.
        NULL arguments are ignored.
       

       

        concat('abcde', 2, NULL, 22)
        abcde222
       

      

      
       

        
         concat_ws
        
        concat_ws ( 
sep
 text,
        
val1
 "any"
        , 
val2
 "any" , ...  )
        text
       

       

        Concatenates all but the first argument, with separators. The first
        argument is used as the separator string, and should not be NULL.
        Other NULL arguments are ignored.
       

       

        concat_ws(',', 'abcde', 2, NULL, 22)
        abcde,2,22
       

      

      
       

        
         format
        
        format ( 
formatstr
 text
        , 
formatarg
 "any" , ...  )
        text
       

       

         Formats arguments according to a format string;
         see .
         This function is similar to the C function sprintf.
       

       

        format('Hello %s, %1$s', 'World')
        Hello World, World
       

      

      
       

        
         initcap
        
        initcap ( text )
        text
       

       

        Converts the first letter of each word to upper case (or title case
        if the letter is a digraph and locale is ICU or
        builtin PG_UNICODE_FAST)
        and the rest to lower case. When using the libc or
        builtin locale provider, words are sequences of
        alphanumeric characters separated by non-alphanumeric characters;
        when using the ICU locale provider, words are separated according to
        u_strToTitle ICU function.
       

       

        This function is primarily used for convenient
        display, and the specific result should not be relied upon because of
        the differences between locale providers and between different
        ICU versions. If specific word boundary rules are desired,
        it is recommended to write a custom function.
       

       

        initcap('hi THOMAS')
        Hi Thomas
       

      

      
       

        
         casefold
        
        casefold ( text )
        text
       

       

        Performs case folding of the input string according to the collation.
        Case folding is similar to case conversion, but the purpose of case
        folding is to facilitate case-insensitive matching of strings,
        whereas the purpose of case conversion is to convert to a particular
        cased form.  This function can only be used when the server encoding
        is UTF8.
       

       

        Ordinarily, case folding simply converts to lowercase, but there may
        be exceptions depending on the collation.  For instance, some
        characters have more than two lowercase variants, or fold to uppercase.
       

       

        Case folding may change the length of the string.  For instance, in
        the PG_UNICODE_FAST collation, ß
        (U+00DF) folds to ss.
       

       

        casefold can be used for Unicode Default Caseless
        Matching.  It does not always preserve the normalized form of the
        input string (see ).
       

       

        The libc provider doesn't support case folding, so
        casefold is identical to .
       

      

      
       

        
         getdatabaseencoding
        
        getdatabaseencoding ( )
        name
       

       

        Returns current database encoding name.
       

       

        getdatabaseencoding()
        UTF8
       

      

      
       

        
         left
        
        left ( 
string
 text,
        
n
 integer )
        text
       

       

        Returns first 
n
 characters in the
        string, or when 
n
 is negative, returns
        all but last |
n
| characters.
       

       

        left('abcde', 2)
        ab
       

      

      
       

        
         length
        
        length ( text )
        integer
       

       

        Returns the number of characters in the string.
       

       

        length('jose')
        4
       

      

      
       

        
         md5
        
        md5 ( text )
        text
       

       

        Computes the MD5 hash of
        the argument, with the result written in hexadecimal.
       

       

        md5('abc')
        900150983cd24fb0&zwsp;d6963f7d28e17f72
       

      

      
       

        
         parse_ident
        
        parse_ident ( 
qualified_identifier
 text
        , 
strict_mode
 boolean DEFAULT true  )
        text[]
       

       

        Splits 
qualified_identifier
 into an array of
        identifiers, removing any quoting of individual identifiers.  By
        default, extra characters after the last identifier are considered an
        error; but if the second parameter is false, then such
        extra characters are ignored. (This behavior is useful for parsing
        names for objects like functions.) Note that this function does not
        truncate over-length identifiers. If you want truncation you can cast
        the result to name[].
       

       

        parse_ident('"SomeSchema".someTable')
        {SomeSchema,sometable}
       

      

      
       

        
         pg_client_encoding
        
        pg_client_encoding ( )
        name
       

       

        Returns current client encoding name.
       

       

        pg_client_encoding()
        UTF8
       

      

      
       

        
         quote_ident
        
        quote_ident ( text )
        text
       

       

        Returns the given string suitably quoted to be used as an identifier
        in an SQL statement string.
        Quotes are added only if necessary (i.e., if the string contains
        non-identifier characters or would be case-folded).
        Embedded quotes are properly doubled.
        See also .
       

       

        quote_ident('Foo bar')
        "Foo bar"
       

      

      
       

        
         quote_literal
        
        quote_literal ( text )
        text
       

       

        Returns the given string suitably quoted to be used as a string literal
        in an SQL statement string.
        Embedded single-quotes and backslashes are properly doubled.
        Note that quote_literal returns null on null
        input; if the argument might be null,
        quote_nullable is often more suitable.
        See also .
       

       

        quote_literal(E'O\'Reilly')
        'O''Reilly'
       

      

      
       

        quote_literal ( anyelement )
        text
       

       

        Converts the given value to text and then quotes it as a literal.
        Embedded single-quotes and backslashes are properly doubled.
       

       

        quote_literal(42.5)
        '42.5'
       

      

      
       

        
         quote_nullable
        
        quote_nullable ( text )
        text
       

       

        Returns the given string suitably quoted to be used as a string literal
        in an SQL statement string; or, if the argument
        is null, returns NULL.
        Embedded single-quotes and backslashes are properly doubled.
        See also .
       

       

        quote_nullable(NULL)
        NULL
       

      

      
       

        quote_nullable ( anyelement )
        text
       

       

        Converts the given value to text and then quotes it as a literal;
        or, if the argument is null, returns NULL.
        Embedded single-quotes and backslashes are properly doubled.
       

       

        quote_nullable(42.5)
        '42.5'
       

      

      
       

        
         regexp_count
        
        regexp_count ( 
string
 text, 
pattern
 text
         , 
start
 integer
         , 
flags
 text   )
        integer
       

       

        Returns the number of times the POSIX regular
        expression 
pattern
 matches in
        the 
string
; see
        .
       

       

        regexp_count('123456789012', '\d\d\d', 2)
        3
       

      

      
       

        
         regexp_instr
        
        regexp_instr ( 
string
 text, 
pattern
 text
         , 
start
 integer
         , 
N
 integer
         , 
endoption
 integer
         , 
flags
 text
         , 
subexpr
 integer      )
        integer
       

       

        Returns the position within 
string
 where
        the 
N
'th match of the POSIX regular
        expression 
pattern
 occurs, or zero if there is
        no such match; see .
       

       

        regexp_instr('ABCDEF', 'c(.)(..)', 1, 1, 0, 'i')
        3
       

       

        regexp_instr('ABCDEF', 'c(.)(..)', 1, 1, 0, 'i', 2)
        5
       

      

      
       

        
         regexp_like
        
        regexp_like ( 
string
 text, 
pattern
 text
         , 
flags
 text  )
        boolean
       

       

        Checks whether a match of the POSIX regular
        expression 
pattern
 occurs
        within 
string
; see
        .
       

       

        regexp_like('Hello World', 'world$', 'i')
        t
       

      

      
       

        
         regexp_match
        
        regexp_match ( 
string
 text, 
pattern
 text , 
flags
 text  )
        text[]
       

       

        Returns substrings within the first match of the POSIX regular
        expression 
pattern
 to
        the 
string
; see
        .
       

       

        regexp_match('foobarbequebaz', '(bar)(beque)')
        {bar,beque}
       

      

      
       

        
         regexp_matches
        
        regexp_matches ( 
string
 text, 
pattern
 text , 
flags
 text  )
        setof text[]
       

       

        Returns substrings within the first match of the POSIX regular
        expression 
pattern
 to
        the 
string
, or substrings within all
        such matches if the g flag is used;
        see .
       

       

        regexp_matches('foobarbequebaz', 'ba.', 'g')
        

```
{bar}
 {baz}
```

       

      

      
       

        
         regexp_replace
        
        regexp_replace ( 
string
 text, 
pattern
 text, 
replacement
 text
         , 
flags
 text  )
        text
       

       

        Replaces the substring that is the first match to the POSIX
        regular expression 
pattern
, or all such
        matches if the g flag is used; see
        .
       

       

        regexp_replace('Thomas', '.[mN]a.', 'M')
        ThM
       

      

      
       

        regexp_replace ( 
string
 text, 
pattern
 text, 
replacement
 text,
         
start
 integer
         , 
N
 integer
         , 
flags
 text   )
        text
       

       

        Replaces the substring that is the 
N
'th
        match to the POSIX regular expression 
pattern
,
        or all such matches if 
N
 is zero, with the
        search beginning at the 
start
'th character
        of 
string
.  If 
N
 is
        omitted, it defaults to 1.  See
        .
       

       

        regexp_replace('Thomas', '.', 'X', 3, 2)
        ThoXas
       

       

        regexp_replace(string=>'hello world', pattern=>'l', replacement=>'XX', start=>1, "N"=>2)
        helXXo world
       

      

      
       

        
         regexp_split_to_array
        
        regexp_split_to_array ( 
string
 text, 
pattern
 text , 
flags
 text  )
        text[]
       

       

        Splits 
string
 using a POSIX regular
        expression as the delimiter, producing an array of results; see
        .
       

       

        regexp_split_to_array('hello world', '\s+')
        {hello,world}
       

      

      
       

        
         regexp_split_to_table
        
        regexp_split_to_table ( 
string
 text, 
pattern
 text , 
flags
 text  )
        setof text
       

       

        Splits 
string
 using a POSIX regular
        expression as the delimiter, producing a set of results; see
        .
       

       

        regexp_split_to_table('hello world', '\s+')
        

```
hello
 world
```

       

      

      
       

        
         regexp_substr
        
        regexp_substr ( 
string
 text, 
pattern
 text
         , 
start
 integer
         , 
N
 integer
         , 
flags
 text
         , 
subexpr
 integer     )
        text
       

       

        Returns the substring within 
string
 that
        matches the 
N
'th occurrence of the POSIX
        regular expression 
pattern
,
        or NULL if there is no such match; see
        .
       

       

        regexp_substr('ABCDEF', 'c(.)(..)', 1, 1, 'i')
        CDEF
       

       

        regexp_substr('ABCDEF', 'c(.)(..)', 1, 1, 'i', 2)
        EF
       

      

      
       

        
         repeat
        
        repeat ( 
string
 text, 
number
 integer )
        text
       

       

        Repeats 
string
 the specified
        
number
 of times.
       

       

        repeat('Pg', 4)
        PgPgPgPg
       

      

      
       

        
         replace
        
        replace ( 
string
 text,
        
from
 text,
        
to
 text )
        text
       

       

        Replaces all occurrences in 
string
 of
        substring 
from
 with
        substring 
to
.
       

       

        replace('abcdefabcdef', 'cd', 'XX')
        abXXefabXXef
       

      

      
       

        
         reverse
        
        reverse ( text )
        text
       

       

        Reverses the order of the characters in the string.
       

       

        reverse('abcde')
        edcba
       

      

      
       

        
         right
        
        right ( 
string
 text,
         
n
 integer )
        text
       

       

        Returns last 
n
 characters in the string,
        or when 
n
 is negative, returns all but
        first |
n
| characters.
       

       

        right('abcde', 2)
        de
       

      

      
       

        
         split_part
        
        split_part ( 
string
 text,
        
delimiter
 text,
        
n
 integer )
        text
       

       

        Splits 
string
 at occurrences
        of 
delimiter
 and returns
        the 
n
'th field (counting from one),
        or when 
n
 is negative, returns
        the |
n
|'th-from-last field.
       

       

        split_part('abc~@~def~@~ghi', '~@~', 2)
        def
       

       

        split_part('abc,def,ghi,jkl', ',', -2)
        ghi
       

      

      
       

        
         starts_with
        
        starts_with ( 
string
 text, 
prefix
 text )
        boolean
       

       

        Returns true if 
string
 starts
        with 
prefix
.
       

       

        starts_with('alphabet', 'alph')
        t
       

      

      
       

        
         string_to_array
        
        string_to_array ( 
string
 text, 
delimiter
 text , 
null_string
 text  )
        text[]
       

       

        Splits the 
string
 at occurrences
        of 
delimiter
 and forms the resulting fields
        into a text array.
        If 
delimiter
 is NULL,
        each character in the 
string
 will become a
        separate element in the array.
        If 
delimiter
 is an empty string, then
        the 
string
 is treated as a single field.
        If 
null_string
 is supplied and is
        not NULL, fields matching that string are
        replaced by NULL.
        See also array_to_string.
       

       

        string_to_array('xx~~yy~~zz', '~~', 'yy')
        {xx,NULL,zz}
       

      

      
       

        
         string_to_table
        
        string_to_table ( 
string
 text, 
delimiter
 text , 
null_string
 text  )
        setof text
       

       

        Splits the 
string
 at occurrences
        of 
delimiter
 and returns the resulting fields
        as a set of text rows.
        If 
delimiter
 is NULL,
        each character in the 
string
 will become a
        separate row of the result.
        If 
delimiter
 is an empty string, then
        the 
string
 is treated as a single field.
        If 
null_string
 is supplied and is
        not NULL, fields matching that string are
        replaced by NULL.
       

       

        string_to_table('xx~^~yy~^~zz', '~^~', 'yy')
        

```
xx
 NULL
 zz
```

       

      

      
       

        
         strpos
        
        strpos ( 
string
 text, 
substring
 text )
        integer
       

       

        Returns first starting index of the specified 
substring

        within 
string
, or zero if it's not present.
        (Same as position(
substring
 in
        
string
), but note the reversed
        argument order.)
       

       

        strpos('high', 'ig')
        2
       

      

      
       

        
         substr
        
        substr ( 
string
 text, 
start
 integer , 
count
 integer  )
        text
       

       

        Extracts the substring of 
string
 starting at
        the 
start
'th character,
        and extending for 
count
 characters if that is
        specified.  (Same
        as substring(
string

        from 
start

        for 
count
).)
       

       

        substr('alphabet', 3)
        phabet
       

       

        substr('alphabet', 3, 2)
        ph
       

      

      
       

        
         to_ascii
        
        to_ascii ( 
string
 text )
        text
       

       

        to_ascii ( 
string
 text,
        
encoding
 name )
        text
       

       

        to_ascii ( 
string
 text,
        
encoding
 integer )
        text
       

       

        Converts 
string
 to ASCII
        from another encoding, which may be identified by name or number.
        If 
encoding
 is omitted the database encoding
        is assumed (which in practice is the only useful case).
        The conversion consists primarily of dropping accents.
        Conversion is only supported
        from LATIN1, LATIN2,
        LATIN9, and WIN1250 encodings.
        (See the  module for another, more flexible
        solution.)
       

       

        to_ascii('Kar&eacute;l')
        Karel
       

      

      
       

        
         to_bin
        
        to_bin ( integer )
        text
       

       

        to_bin ( bigint )
        text
       

       

        Converts the number to its equivalent two's complement binary
        representation.
       

       

        to_bin(2147483647)
        1111111111111111111111111111111
       

       

        to_bin(-1234)
        11111111111111111111101100101110
       

      

      
       

        
         to_hex
        
        to_hex ( integer )
        text
       

       

        to_hex ( bigint )
        text
       

       

        Converts the number to its equivalent two's complement hexadecimal
        representation.
       

       

        to_hex(2147483647)
        7fffffff
       

       

        to_hex(-1234)
        fffffb2e
       

      

      
       

        
         to_oct
        
        to_oct ( integer )
        text
       

       

        to_oct ( bigint )
        text
       

       

        Converts the number to its equivalent two's complement octal
        representation.
       

       

        to_oct(2147483647)
        17777777777
       

       

        to_oct(-1234)
        37777775456
       

      

      
       

        
         translate
        
        translate ( 
string
 text,
        
from
 text,
        
to
 text )
        text
       

       

        Replaces each character in 
string
 that
        matches a character in the 
from
 set with the
        corresponding character in the 
to

        set. If 
from
 is longer than
        
to
, occurrences of the extra characters in
        
from
 are deleted.
       

       

        translate('12345', '143', 'ax')
        a2x5
       

      

      
       

        
         unistr
        
        unistr ( text )
        text
       

       

        Evaluate escaped Unicode characters in the argument.  Unicode characters
        can be specified as
        \XXXX (4 hexadecimal
        digits), \+XXXXXX (6
        hexadecimal digits),
        \uXXXX (4 hexadecimal
        digits), or \UXXXXXXXX
        (8 hexadecimal digits).  To specify a backslash, write two
        backslashes.  All other characters are taken literally.
       

       

        If the server encoding is not UTF-8, the Unicode code point identified
        by one of these escape sequences is converted to the actual server
        encoding; an error is reported if that's not possible.
       

       

        This function provides a (non-standard) alternative to string
        constants with Unicode escapes (see ).
       

       

        unistr('d\0061t\+000061')
        data
       

       

        unistr('d\u0061t\U00000061')
        data
       

      

     
    
   

   

    The concat, concat_ws and
    format functions are variadic, so it is possible to
    pass the values to be concatenated or formatted as an array marked with
    the VARIADIC keyword (see ).  The array's elements are
    treated as if they were separate ordinary arguments to the function.
    If the variadic array argument is NULL, concat
    and concat_ws return NULL, but
    format treats a NULL as a zero-element array.
   

   

    See also the aggregate function string_agg in
    , and the functions for
    converting between strings and the bytea type in
    .
   

   

    
# format

    
     format
    

    

     The function format produces output formatted according to
     a format string, in a style similar to the C function
     sprintf.
    

    

format(
formatstr
 text , 
formatarg
 "any" , ... )

     
formatstr
 is a format string that specifies how the
     result should be formatted.  Text in the format string is copied
     directly to the result, except where format specifiers are
     used.  Format specifiers act as placeholders in the string, defining how
     subsequent function arguments should be formatted and inserted into the
     result.  Each 
formatarg
 argument is converted to text
     according to the usual output rules for its data type, and then formatted
     and inserted into the result string according to the format specifier(s).
    

    

     Format specifiers are introduced by a % character and have
     the form

%[
position
][
flags
][
width
]
type

     where the component fields are:

     

      

       

position
 (optional)

       

        

         A string of the form 
n
$ where
         
n
 is the index of the argument to print.
         Index 1 means the first argument after
         
formatstr
.  If the 
position
 is
         omitted, the default is to use the next argument in sequence.
        

       

      

      

       

flags
 (optional)

       

        

         Additional options controlling how the format specifier's output is
         formatted.  Currently the only supported flag is a minus sign
         (-) which will cause the format specifier's output to be
         left-justified.  This has no effect unless the 
width

         field is also specified.
        

       

      

      

       

width
 (optional)

       

        

         Specifies the minimum number of characters to use to
         display the format specifier's output.  The output is padded on the
         left or right (depending on the - flag) with spaces as
         needed to fill the width.  A too-small width does not cause
         truncation of the output, but is simply ignored.  The width may be
         specified using any of the following: a positive integer; an
         asterisk (*) to use the next function argument as the
         width; or a string of the form *
n
$ to
         use the 
n
th function argument as the width.
        

        

         If the width comes from a function argument, that argument is
         consumed before the argument that is used for the format specifier's
         value.  If the width argument is negative, the result is left
         aligned (as if the - flag had been specified) within a
         field of length abs(
width
).
        

       

      

      

       

type
 (required)

       

        

         The type of format conversion to use to produce the format
         specifier's output.  The following types are supported:
         

          

           

            s formats the argument value as a simple
            string.  A null value is treated as an empty string.
           

          

          

           

            I treats the argument value as an SQL
            identifier, double-quoting it if necessary.
            It is an error for the value to be null (equivalent to
            quote_ident).
           

          

          

           

            L quotes the argument value as an SQL literal.
            A null value is displayed as the string NULL, without
            quotes (equivalent to quote_nullable).
           

          

         

        

       

      

     

    

    

     In addition to the format specifiers described above, the special sequence
     %% may be used to output a literal % character.
    

    

     Here are some examples of the basic format conversions:

```
SELECT format('Hello %s', 'World');
Result: Hello World

SELECT format('Testing %s, %s, %s, %%', 'one', 'two', 'three');
Result: Testing one, two, three, %

SELECT format('INSERT INTO %I VALUES(%L)', 'Foo bar', E'O\'Reilly');
Result: INSERT INTO "Foo bar" VALUES('O''Reilly')

SELECT format('INSERT INTO %I VALUES(%L)', 'locations', 'C:\Program Files');
Result: INSERT INTO locations VALUES('C:\Program Files')
```

    

    

     Here are examples using 
width
 fields
     and the - flag:

```
SELECT format('|%10s|', 'foo');
Result: |       foo|

SELECT format('|%-10s|', 'foo');
Result: |foo       |

SELECT format('|%*s|', 10, 'foo');
Result: |       foo|

SELECT format('|%*s|', -10, 'foo');
Result: |foo       |

SELECT format('|%-*s|', 10, 'foo');
Result: |foo       |

SELECT format('|%-*s|', -10, 'foo');
Result: |foo       |
```

    

    

     These examples show use of 
position
 fields:

```
SELECT format('Testing %3$s, %2$s, %1$s', 'one', 'two', 'three');
Result: Testing three, two, one

SELECT format('|%*2$s|', 'foo', 10, 'bar');
Result: |       bar|

SELECT format('|%1$*2$s|', 'foo', 10, 'bar');
Result: |       foo|
```

    

    

     Unlike the standard C function sprintf,
     PostgreSQL's format function allows format
     specifiers with and without 
position
 fields to be mixed
     in the same format string.  A format specifier without a
     
position
 field always uses the next argument after the
     last argument consumed.
     In addition, the format function does not require all
     function arguments to be used in the format string.
     For example:

```
SELECT format('Testing %3$s, %2$s, %s', 'one', 'two', 'three');
Result: Testing three, two, three
```

    

    

     The %I and %L format specifiers are particularly
     useful for safely constructing dynamic SQL statements.  See
     .
