# Data Types

  
   data type
  

  
   type
   data type
  

  

   PostgreSQL has a rich set of native data
   types available to users.  Users can add new types to
   PostgreSQL using the  command.
  

  

    shows all the built-in general-purpose data
   types. Most of the alternative names listed in the
   Aliases column are the names used internally by
   PostgreSQL for historical reasons.  In
   addition, some internally used or deprecated types are available,
   but are not listed here.
  

   
    
# Data Types

    
     
     
     
     
      
       Name
       Aliases
       Description
      
     

     
      
       bigint
       int8
       signed eight-byte integer
      

      
       bigserial
       serial8
       autoincrementing eight-byte integer
      

      
       bit [ (n) ]
       
       fixed-length bit string
      

      
       bit varying [ (n) ]
       varbit [ (n) ]
       variable-length bit string
      

      
       boolean
       bool
       logical Boolean (true/false)
      

      
       box
       
       rectangular box on a plane
      

      
       bytea
       
       binary data (byte array)
      

      
       character [ (n) ]
       char [ (n) ]
       fixed-length character string
      

      
       character varying [ (n) ]
       varchar [ (n) ]
       variable-length character string
      

      
       cidr
       
       IPv4 or IPv6 network address
      

      
       circle
       
       circle on a plane
      

      
       date
       
       calendar date (year, month, day)
      

      
       double precision
       float, float8
       double precision floating-point number (8 bytes)
      

      
       inet
       
       IPv4 or IPv6 host address
      

      
       integer
       int, int4
       signed four-byte integer
      

      
       interval [ fields ] [ (p) ]
       
       time span
      

      
       json
       
       textual JSON data
      

      
       jsonb
       
       binary JSON data, decomposed
      

      
       line
       
       infinite line on a plane
      

      
       lseg
       
       line segment on a plane
      

      
       macaddr
       
       MAC (Media Access Control) address
      

      
       macaddr8
       
       MAC (Media Access Control) address (EUI-64 format)
      

      
       money
       
       currency amount
      

      
       numeric [ (p,
         s) ]
       decimal [ (p,
         s) ]
       exact numeric of selectable precision
      

      
       path
       
       geometric path on a plane
      

      
       pg_lsn
       
       PostgreSQL Log Sequence Number
      

      
       pg_snapshot
       
       user-level transaction ID snapshot
      

      
       point
       
       geometric point on a plane
      

      
       polygon
       
       closed geometric path on a plane
      

      
       real
       float4
       single precision floating-point number (4 bytes)
      

      
       smallint
       int2
       signed two-byte integer
      

      
       smallserial
       serial2
       autoincrementing two-byte integer
      

      
       serial
       serial4
       autoincrementing four-byte integer
      

      
       text
       
       variable-length character string
      

      
       time [ (p) ] [ without time zone ]
       
       time of day (no time zone)
      

      
       time [ (p) ] with time zone
       timetz
       time of day, including time zone
      

      
       timestamp [ (p) ] [ without time zone ]
       
       date and time (no time zone conversion)
      

      
       timestamp [ (p) ] with time zone
       timestamptz
       date and time, including time zone conversion
      

      
       tsquery
       
       text search query
      

      
       tsvector
       
       text search document
      

      
       txid_snapshot
       
       user-level transaction ID snapshot (deprecated; see pg_snapshot)
      

      
       uuid
       
       universally unique identifier
      

      
       xml
       
       XML data
      
     
    
   

  
   
# Compatibility

   

    The following types (or spellings thereof) are specified by
    SQL: bigint, bit, bit
    varying, boolean, char,
    character varying, character,
    varchar, date, double
    precision, integer, interval,
    json,
    numeric, decimal, real,
    smallint, time (with or without time zone),
    timestamp (with or without time zone),
    xml.
   

  

  

   Each data type has an external representation determined by its input
   and output functions.  Many of the built-in types have
   obvious external formats.  However, several types are either unique
   to PostgreSQL, such as geometric
   paths, or have several possible formats, such as the date
   and time types.
   Some of the input and output functions are not invertible, i.e.,
   the result of an output function might lose accuracy when compared to
   the original input.
  

  

   
# Numeric Types

   
    data type
    numeric
   

   

    Numeric types consist of two-, four-, and eight-byte integers,
    four- and eight-byte floating-point numbers, and selectable-precision
    decimals.   lists the
    available types.
   

    
     
# Numeric Types

     
      
      
      
      
      
       
        Name
        Storage Size
        Description
        Range
       
      

      
       
        smallint
        2 bytes
        small-range integer
        -32768 to +32767
       
       
        integer
        4 bytes
        typical choice for integer
        -2147483648 to +2147483647
       
       
        bigint
        8 bytes
        large-range integer
        -9223372036854775808 to +9223372036854775807
       

       
        decimal
        variable
        user-specified precision, exact
        up to 131072 digits before the decimal point; up to 16383 digits after the decimal point
       
       
        numeric
        variable
        user-specified precision, exact
        up to 131072 digits before the decimal point; up to 16383 digits after the decimal point
       

       
        real
        4 bytes
        variable-precision, inexact
        6 decimal digits precision
       
       
        double precision
        8 bytes
        variable-precision, inexact
        15 decimal digits precision
       

       
        smallserial
        2 bytes
        small autoincrementing integer
        1 to 32767
       

       
        serial
        4 bytes
        autoincrementing integer
        1 to 2147483647
       

       
        bigserial
        8 bytes
        large autoincrementing integer
        1 to 9223372036854775807
       
      
     
    

   

    The syntax of constants for the numeric types is described in
    .  The numeric types have a
    full set of corresponding arithmetic operators and
    functions. Refer to  for more
    information.  The following sections describe the types in detail.
   

   

    
# Integer Types

    
     integer
    

    
     smallint
    

    
     bigint
    

    
     int4
     integer
    

    
     int2
     smallint
    

    
     int8
     bigint
    

    

     The types smallint, integer, and
     bigint store whole numbers, that is, numbers without
     fractional components, of various ranges.  Attempts to store
     values outside of the allowed range will result in an error.
    

    

     The type integer is the common choice, as it offers
     the best balance between range, storage size, and performance.
     The smallint type is generally only used if disk
     space is at a premium.  The bigint type is designed to be
     used when the range of the integer type is insufficient.
    

    

     SQL only specifies the integer types
     integer (or int),
     smallint, and bigint.  The
     type names int2, int4, and
     int8 are extensions, which are also used by some
     other SQL database systems.
    

   

   

    
# Arbitrary Precision Numbers

    
     numeric (data type)
    

   
    arbitrary precision numbers
   

    
     decimal
     numeric
    

    

     The type numeric can store numbers with a
     very large number of digits. It is especially recommended for
     storing monetary amounts and other quantities where exactness is
     required.  Calculations with numeric values yield exact
     results where possible, e.g.,  addition, subtraction, multiplication.
     However, calculations on numeric values are very slow
     compared to the integer types, or to the floating-point types
     described in the next section.
    

    

     We use the following terms below:  The
     precision of a numeric
     is the total count of significant digits in the whole number,
     that is, the number of digits to both sides of the decimal point.
     The scale of a numeric is the
     count of decimal digits in the fractional part, to the right of the
     decimal point.  So the number 23.5141 has a precision of 6 and a
     scale of 4.  Integers can be considered to have a scale of zero.
    

    

     Both the maximum precision and the maximum scale of a
     numeric column can be
     configured.  To declare a column of type numeric use
     the syntax:

```
NUMERIC(precision, scale)
```

     The precision must be positive, while the scale may be positive or
     negative (see below).  Alternatively:

```
NUMERIC(precision)
```

     selects a scale of 0.  Specifying:

```
NUMERIC
```

     without any precision or scale creates an unconstrained
     numeric column in which numeric values of any length can be
     stored, up to the implementation limits.  A column of this kind will
     not coerce input values to any particular scale, whereas
     numeric columns with a declared scale will coerce
     input values to that scale.  (The SQL standard
     requires a default scale of 0, i.e., coercion to integer
     precision.  We find this a bit useless.  If you're concerned
     about portability, always specify the precision and scale
     explicitly.)
    

    
     

      The maximum precision that can be explicitly specified in
      a numeric type declaration is 1000.  An
      unconstrained numeric column is subject to the limits
      described in .
     

    

    

     If the scale of a value to be stored is greater than the declared
     scale of the column, the system will round the value to the specified
     number of fractional digits.  Then, if the number of digits to the
     left of the decimal point exceeds the declared precision minus the
     declared scale, an error is raised.
     For example, a column declared as

```
NUMERIC(3, 1)
```

     will round values to 1 decimal place and can store values between
     -99.9 and 99.9, inclusive.
    

    

     Beginning in PostgreSQL 15, it is allowed
     to declare a numeric column with a negative scale.  Then
     values will be rounded to the left of the decimal point.  The
     precision still represents the maximum number of non-rounded digits.
     Thus, a column declared as

```
NUMERIC(2, -3)
```

     will round values to the nearest thousand and can store values
     between -99000 and 99000, inclusive.
     It is also allowed to declare a scale larger than the declared
     precision.  Such a column can only hold fractional values, and it
     requires the number of zero digits just to the right of the decimal
     point to be at least the declared scale minus the declared precision.
     For example, a column declared as

```
NUMERIC(3, 5)
```

     will round values to 5 decimal places and can store values between
     -0.00999 and 0.00999, inclusive.
    

    
     

      PostgreSQL permits the scale in a
      numeric type declaration to be any value in the range
      -1000 to 1000.  However, the SQL standard requires
      the scale to be in the range 0 to precision.
      Using scales outside that range may not be portable to other database
      systems.
     

    

    

     Numeric values are physically stored without any extra leading or
     trailing zeroes.  Thus, the declared precision and scale of a column
     are maximums, not fixed allocations.  (In this sense the numeric
     type is more akin to varchar(n)
     than to char(n).)  The actual storage
     requirement is two bytes for each group of four decimal digits,
     plus three to eight bytes overhead.
    

    
     infinity
     numeric (data type)
    

    
     NaN
     not a number
   

    
     not a number
     numeric (data type)
    

    

     In addition to ordinary numeric values, the numeric type
     has several special values:

Infinity
-Infinity
NaN

     These are adapted from the IEEE 754 standard, and represent
     infinity, negative infinity, and
     not-a-number, respectively. When writing these values
     as constants in an SQL command, you must put quotes around them,
     for example UPDATE table SET x = '-Infinity'.
     On input, these strings are recognized in a case-insensitive manner.
     The infinity values can alternatively be spelled inf
     and -inf.
    

    

     The infinity values behave as per mathematical expectations.  For
     example, Infinity plus any finite value equals
     Infinity, as does Infinity
     plus Infinity; but Infinity
     minus Infinity yields NaN (not a
     number), because it has no well-defined interpretation.  Note that an
     infinity can only be stored in an unconstrained numeric
     column, because it notionally exceeds any finite precision limit.
    

    

     The NaN (not a number) value is used to represent
     undefined calculational results.  In general, any operation with
     a NaN input yields another NaN.
     The only exception is when the operation's other inputs are such that
     the same output would be obtained if the NaN were to
     be replaced by any finite or infinite numeric value; then, that output
     value is used for NaN too.  (An example of this
     principle is that NaN raised to the zero power
     yields one.)
    

    
     

      In most implementations of the not-a-number concept,
      NaN is not considered equal to any other numeric
      value (including NaN).  In order to allow
      numeric values to be sorted and used in tree-based
      indexes, PostgreSQL treats NaN
      values as equal, and greater than all non-NaN
      values.
     

    

    

     The types decimal and numeric are
     equivalent.  Both types are part of the SQL
     standard.
    

    

     When rounding values, the numeric type rounds ties away
     from zero, while (on most machines) the real
     and double precision types round ties to the nearest even
     number.  For example:

```
SELECT x,
  round(x::numeric) AS num_round,
  round(x::double precision) AS dbl_round
FROM generate_series(-3.5, 3.5, 1) AS x;
  x   | num_round | dbl_round
------+-----------+-----------
 -3.5 |        -4 |        -4
 -2.5 |        -3 |        -2
 -1.5 |        -2 |        -2
 -0.5 |        -1 |        -0
  0.5 |         1 |         0
  1.5 |         2 |         2
  2.5 |         3 |         2
  3.5 |         4 |         4
(8 rows)
```

    

   

   

    
# Floating-Point Types

    
     real
    

    
     double precision
    

    
     float4
     real
    

    
     float8
     double precision
    

    
     floating point
    

    

     The data types real and double precision are
     inexact, variable-precision numeric types. On all currently supported
     platforms, these types are implementations of IEEE
     Standard 754 for Binary Floating-Point Arithmetic (single and double
     precision, respectively), to the extent that the underlying processor,
     operating system, and compiler support it.
    

    

     Inexact means that some values cannot be converted exactly to the
     internal format and are stored as approximations, so that storing
     and retrieving a value might show slight discrepancies.
     Managing these errors and how they propagate through calculations
     is the subject of an entire branch of mathematics and computer
     science and will not be discussed here, except for the
     following points:
     

      

       

        If you require exact storage and calculations (such as for
        monetary amounts), use the numeric type instead.
       

      

      

       

        If you want to do complicated calculations with these types
        for anything important, especially if you rely on certain
        behavior in boundary cases (infinity, underflow), you should
        evaluate the implementation carefully.
       

      

      

       

        Comparing two floating-point values for equality might not
        always work as expected.
       

      

     

    

    

     On all currently supported platforms, the real type has a
     range of around 1E-37 to 1E+37 with a precision of at least 6 decimal
     digits. The double precision type has a range of around
     1E-307 to 1E+308 with a precision of at least 15 digits. Values that are
     too large or too small will cause an error. Rounding might take place if
     the precision of an input number is too high. Numbers too close to zero
     that are not representable as distinct from zero will cause an underflow
     error.
    

    

     By default, floating point values are output in text form in their
     shortest precise decimal representation; the decimal value produced is
     closer to the true stored binary value than to any other value
     representable in the same binary precision. (However, the output value is
     currently never exactly midway between two
     representable values, in order to avoid a widespread bug where input
     routines do not properly respect the round-to-nearest-even rule.) This value will
     use at most 17 significant decimal digits for float8
     values, and at most 9 digits for float4 values.
    

    
     

      This shortest-precise output format is much faster to generate than the
      historical rounded format.
     

    

    

     For compatibility with output generated by older versions
     of PostgreSQL, and to allow the output
     precision to be reduced, the 
     parameter can be used to select rounded decimal output instead. Setting a
     value of 0 restores the previous default of rounding the value to 6
     (for float4) or 15 (for float8)
     significant decimal digits. Setting a negative value reduces the number
     of digits further; for example -2 would round output to 4 or 13 digits
     respectively.
    

    

     Any value of  greater than 0
     selects the shortest-precise format.
    

    
     

      Applications that wanted precise values have historically had to set
       to 3 to obtain them. For
      maximum compatibility between versions, they should continue to do so.
     

    

    
     infinity
     floating point
    

    
     not a number
     floating point
    

    

     In addition to ordinary numeric values, the floating-point types
     have several special values:

Infinity
-Infinity
NaN

     These represent the IEEE 754 special values
     infinity, negative infinity, and
     not-a-number, respectively. When writing these values
     as constants in an SQL command, you must put quotes around them,
     for example UPDATE table SET x = '-Infinity'.  On input,
     these strings are recognized in a case-insensitive manner.
     The infinity values can alternatively be spelled inf
     and -inf.
    

    
     

      IEEE 754 specifies that NaN should not compare equal
      to any other floating-point value (including NaN).
      In order to allow floating-point values to be sorted and used
      in tree-based indexes, PostgreSQL treats
      NaN values as equal, and greater than all
      non-NaN values.
     

    

    

     PostgreSQL also supports the SQL-standard
     notations float and
     float(p) for specifying
     inexact numeric types.  Here, p specifies
     the minimum acceptable precision in binary digits.
     PostgreSQL accepts
     float(1) to float(24) as selecting the
     real type, while
     float(25) to float(53) select
     double precision.  Values of p
     outside the allowed range draw an error.
     float with no precision specified is taken to mean
     double precision.
    

   

   

    
# Serial Types

    
     smallserial
    

    
     serial
    

    
     bigserial
    

    
     serial2
    

    
     serial4
    

    
     serial8
    

    
     auto-increment
     serial
    

    
     sequence
     and serial type
    

    
     

      This section describes a PostgreSQL-specific way to create an
      autoincrementing column.  Another way is to use the SQL-standard
      identity column feature, described at .
     

    

    

     The data types smallserial, serial and
     bigserial are not true types, but merely
     a notational convenience for creating unique identifier columns
     (similar to the AUTO_INCREMENT property
     supported by some other databases). In the current
     implementation, specifying:

```
CREATE TABLE tablename (
    colname SERIAL
);
```

     is equivalent to specifying:

```
CREATE SEQUENCE tablename_colname_seq AS integer;
CREATE TABLE tablename (
    colname integer NOT NULL DEFAULT nextval('tablename_colname_seq')
);
ALTER SEQUENCE tablename_colname_seq OWNED BY tablename.colname;
```

     Thus, we have created an integer column and arranged for its default
     values to be assigned from a sequence generator.  A NOT NULL
     constraint is applied to ensure that a null value cannot be
     inserted.  (In most cases you would also want to attach a
     UNIQUE or PRIMARY KEY constraint to prevent
     duplicate values from being inserted by accident, but this is
     not automatic.)  Lastly, the sequence is marked as owned by
     the column, so that it will be dropped if the column or table is dropped.
    

    
      

        Because smallserial, serial and
        bigserial are implemented using sequences, there may
        be "holes" or gaps in the sequence of values which appears in the
        column, even if no rows are ever deleted.  A value allocated
        from the sequence is still "used up" even if a row containing that
        value is never successfully inserted into the table column.  This
        may happen, for example, if the inserting transaction rolls back.
        See nextval() in 
        for details.
      

    

    

     To insert the next value of the sequence into the serial
     column, specify that the serial
     column should be assigned its default value. This can be done
     either by excluding the column from the list of columns in
     the INSERT statement, or through the use of
     the DEFAULT key word.
    

    

     The type names serial and serial4 are
     equivalent: both create integer columns.  The type
     names bigserial and serial8 work
     the same way, except that they create a bigint
     column.  bigserial should be used if you anticipate
     the use of more than 231 identifiers over the
     lifetime of the table. The type names smallserial and
     serial2 also work the same way, except that they
     create a smallint column.
    

    

     The sequence created for a serial column is
     automatically dropped when the owning column is dropped.
     You can drop the sequence without dropping the column, but this
     will force removal of the column default expression.
    

   

  

  

   
# Monetary Types

   

    The money type stores a currency amount with a fixed
    fractional precision; see .  The fractional precision is
    determined by the database's  setting.
    The range shown in the table assumes there are two fractional digits.
    Input is accepted in a variety of formats, including integer and
    floating-point literals, as well as typical
    currency formatting, such as '$1,000.00'.
    Output is generally in the latter form but depends on the locale.
   

    
     
# Monetary Types

     
      
      
      
      
      
       
        Name
        Storage Size
        Description
        Range
       
      
      
       
        money
        8 bytes
        currency amount
        -92233720368547758.08 to +92233720368547758.07
       
      
     
    

   

    Since the output of this data type is locale-sensitive, it might not
    work to load money data into a database that has a different
    setting of lc_monetary.  To avoid problems, before
    restoring a dump into a new database make sure lc_monetary has
    the same or equivalent value as in the database that was dumped.
   

   

    Values of the numeric, int, and
    bigint data types can be cast to money.
    Conversion from the real and double precision
    data types can be done by casting to numeric first, for
    example:

```
SELECT '12.34'::float8::numeric::money;
```

    However, this is not recommended.  Floating point numbers should not be
    used to handle money due to the potential for rounding errors.
   

   

    A money value can be cast to numeric without
    loss of precision. Conversion to other types could potentially lose
    precision, and must also be done in two stages:

```
SELECT '52093.89'::money::numeric::float8;
```

   

   

    Division of a money value by an integer value is performed
    with truncation of the fractional part towards zero.  To get a rounded
    result, divide by a floating-point value, or cast the money
    value to numeric before dividing and back to money
    afterwards.  (The latter is preferable to avoid risking precision loss.)
    When a money value is divided by another money
    value, the result is double precision (i.e., a pure number,
    not money); the currency units cancel each other out in the division.
   

  

  

   
# Character Types

   
    character string
    data types
   

   
    string
    character string
   

   
    character
   

   
    character varying
   

   
    text
   

   
    char
   

   
    varchar
   

   
    bpchar
   

    
     
# Character Types

     
      
       
        Name
        Description
       
      
      
       
        character varying(n), varchar(n)
        variable-length with limit
       
       
        character(n), char(n), bpchar(n)
        fixed-length, blank-padded
       
       
        bpchar
        variable unlimited length, blank-trimmed
       
       
        text
        variable unlimited length
       
     
     
    

   

     shows the
    general-purpose character types available in
    PostgreSQL.
   

   

    SQL defines two primary character types:
    character varying(n) and
    character(n), where n
    is a positive integer.  Both of these types can store strings up to
    n characters (not bytes) in length.  An attempt to store a
    longer string into a column of these types will result in an
    error, unless the excess characters are all spaces, in which case
    the string will be truncated to the maximum length. (This somewhat
    bizarre exception is required by the SQL
    standard.)
    However, if one explicitly casts a value to character
    varying(n) or
    character(n), then an over-length
    value will be truncated to n characters without
    raising an error. (This too is required by the
    SQL standard.)
    If the string to be stored is shorter than the declared
    length, values of type character will be space-padded;
    values of type character varying will simply store the
    shorter
    string.
   

   

    In addition, PostgreSQL provides the
    text type, which stores strings of any length.
    Although the text type is not in the
    SQL standard, several other SQL database
    management systems have it as well.
    text is PostgreSQL's native
    string data type, in that most built-in functions operating on strings
    are declared to take or return text not character
    varying.  For many purposes, character varying
    acts as though it were a domain
    over text.
   

   

    The type name varchar is an alias for character
    varying, while bpchar (with length specifier) and
    char are aliases for character.  The
    varchar and char aliases are defined in the
    SQL standard;  bpchar is a
    PostgreSQL extension.
   

   

    If specified, the length n must be greater
    than zero and cannot exceed 10,485,760.  If character
    varying (or varchar) is used without
    length specifier, the type accepts strings of any length. If
    bpchar lacks a length specifier, it also accepts strings
    of any length, but trailing spaces are semantically insignificant.
    If character (or char) lacks a specifier,
    it is equivalent to character(1).
   

   

    Values of type character are physically padded
    with spaces to the specified width n, and are
    stored and displayed that way.  However, trailing spaces are treated as
    semantically insignificant and disregarded when comparing two values
    of type character.  In collations where whitespace
    is significant, this behavior can produce unexpected results;
    for example SELECT 'a '::CHAR(2) COLLATE "C" <
    E'a\n'::CHAR(2) returns true, even though C
    locale would consider a space to be greater than a newline.
    Trailing spaces are removed when converting a character value
    to one of the other string types.  Note that trailing spaces
    are semantically significant in
    character varying and text values, and
    when using pattern matching, that is LIKE and
    regular expressions.
   

   

    The characters that can be stored in any of these data types are
    determined by the database character set, which is selected when
    the database is created.  Regardless of the specific character set,
    the character with code zero (sometimes called NUL) cannot be stored.
    For more information refer to .
   

   

    The storage requirement for a short string (up to 126 bytes) is 1 byte
    plus the actual string, which includes the space padding in the case of
    character.  Longer strings have 4 bytes of overhead instead
    of 1.  Long strings are compressed by the system automatically, so
    the physical requirement on disk might be less. Very long values are also
    stored in background tables so that they do not interfere with rapid
    access to shorter column values. In any case, the longest
    possible character string that can be stored is about 1 GB. (The
    maximum value that will be allowed for n in the data
    type declaration is less than that. It wouldn't be useful to
    change this because with multibyte character encodings the number of
    characters and bytes can be quite different. If you desire to
    store long strings with no specific upper limit, use
    text or character varying without a length
    specifier, rather than making up an arbitrary length limit.)
   

   
    

     There is no performance difference among these three types,
     apart from increased storage space when using the blank-padded
     type, and a few extra CPU cycles to check the length when storing into
     a length-constrained column.  While
     character(n) has performance
     advantages in some other database systems, there is no such advantage in
     PostgreSQL; in fact
     character(n) is usually the slowest of
     the three because of its additional storage costs.  In most situations
     text or character varying should be used
     instead.
    

   

   

    Refer to  for information about
    the syntax of string literals, and to 
    for information about available operators and functions.
   

   
    
# Using the Character Types

```
CREATE TABLE test1 (a character(4));
INSERT INTO test1 VALUES ('ok');
SELECT a, char_length(a) FROM test1; -- 

  a   | char_length
------+-------------
 ok   |           2

CREATE TABLE test2 (b varchar(5));
INSERT INTO test2 VALUES ('ok');
INSERT INTO test2 VALUES ('good      ');
INSERT INTO test2 VALUES ('too long');
ERROR:  value too long for type character varying(5)
INSERT INTO test2 VALUES ('too long'::varchar(5)); -- explicit truncation
SELECT b, char_length(b) FROM test2;

   b   | char_length
-------+-------------
 ok    |           2
 good  |           5
 too l |           5

```

    
     
      

       The char_length function is discussed in
       .
      

     
    
   

   

    There are two other fixed-length character types in
    PostgreSQL, shown in .
    These are not intended for general-purpose use, only for use
    in the internal system catalogs.
    The name type is used to store identifiers. Its
    length is currently defined as 64 bytes (63 usable characters plus
    terminator) but should be referenced using the constant
    NAMEDATALEN in C source code.
    The length is set at compile time (and
    is therefore adjustable for special uses); the default maximum
    length might change in a future release. The type "char"
    (note the quotes) is different from char(1) in that it
    only uses one byte of storage, and therefore can store only a single
    ASCII character. It is used in the system
    catalogs as a simplistic enumeration type.
   

    
     
# Special Character Types

     
      
       
        Name
        Storage Size
        Description
       
      
      
       
        "char"
        1 byte
        single-byte internal type
       
       
        name
        64 bytes
        internal type for object names
       
      
     
    

  

 

  
# Binary Data Types

  
   binary data
  

  
   bytea
  

   

    The bytea data type allows storage of binary strings;
    see .
   

   
    
# Binary Data Types

    
     
     
     
     
      
       Name
       Storage Size
       Description
      
     
     
      
       bytea
       1 or 4 bytes plus the actual binary string
       variable-length binary string
      
     
    
   

   

    A binary string is a sequence of octets (or bytes).  Binary
    strings are distinguished from character strings in two
    ways.  First, binary strings specifically allow storing
    octets of value zero and other non-printable
    octets (usually, octets outside the decimal range 32 to 126).
    Character strings disallow zero octets, and also disallow any
    other octet values and sequences of octet values that are invalid
    according to the database's selected character set encoding.
    Second, operations on binary strings process the actual bytes,
    whereas the processing of character strings depends on locale settings.
    In short, binary strings are appropriate for storing data that the
    programmer thinks of as raw bytes, whereas character
    strings are appropriate for storing text.
   

   

    The bytea type supports two
    formats for input and output: hex format
    and PostgreSQL's historical
    escape format.  Both
    of these are always accepted on input.  The output format depends
    on the configuration parameter ;
    the default is hex.  (Note that the hex format was introduced in
    PostgreSQL 9.0; earlier versions and some
    tools don't understand it.)
   

   

    The SQL standard defines a different binary
    string type, called BLOB or BINARY LARGE
    OBJECT.  The input format is different from
    bytea, but the provided functions and operators are
    mostly the same.
   

  

   
# bytea Hex Format

   

    The hex format encodes binary data as 2 hexadecimal digits
    per byte, most significant nibble first.  The entire string is
    preceded by the sequence \x (to distinguish it
    from the escape format).  In some contexts, the initial backslash may
    need to be escaped by doubling it
    (see ).
    For input, the hexadecimal digits can
    be either upper or lower case, and whitespace is permitted between
    digit pairs (but not within a digit pair nor in the starting
    \x sequence).
    The hex format is compatible with a wide
    range of external applications and protocols, and it tends to be
    faster to convert than the escape format, so its use is preferred.
   

   

    Example:

```
SET bytea_output = 'hex';

SELECT '\xDEADBEEF'::bytea;
   bytea
------------
 \xdeadbeef
```

   

  

  

   
# bytea Escape Format

   

    The escape format is the traditional
    PostgreSQL format for the bytea
    type.  It
    takes the approach of representing a binary string as a sequence
    of ASCII characters, while converting those bytes that cannot be
    represented as an ASCII character into special escape sequences.
    If, from the point of view of the application, representing bytes
    as characters makes sense, then this representation can be
    convenient.  But in practice it is usually confusing because it
    fuzzes up the distinction between binary strings and character
    strings, and also the particular escape mechanism that was chosen is
    somewhat unwieldy.  Therefore, this format should probably be avoided
    for most new applications.
   

   

    When entering bytea values in escape format,
    octets of certain
    values must be escaped, while all octet
    values can be escaped.  In
    general, to escape an octet, convert it into its three-digit
    octal value and precede it by a backslash.
    Backslash itself (octet decimal value 92) can alternatively be represented by
    double backslashes.
    
    shows the characters that must be escaped, and gives the alternative
    escape sequences where applicable.
   

   
    
# bytea Literal Escaped Octets

    
     
     
     
     
     
     
      
       Decimal Octet Value
       Description
       Escaped Input Representation
       Example
       Hex Representation
      
     

     
      
       0
       zero octet
       '\000'
       '\000'::bytea
       \x00
      

      
       39
       single quote
       '''' or '\047'
       ''''::bytea
       \x27
      

      
       92
       backslash
       '\\' or '\134'
       '\\'::bytea
       \x5c
      

      
       0 to 31 and 127 to 255
       non-printable octets
       '\xxx' (octal value)
       '\001'::bytea
       \x01
      

     
    
   

   

    The requirement to escape non-printable octets
    varies depending on locale settings. In some instances you can get away
    with leaving them unescaped.
   

   

    The reason that single quotes must be doubled, as shown
    in , is that this
    is true for any string literal in an SQL command.  The generic
    string-literal parser consumes the outermost single quotes
    and reduces any pair of single quotes to one data character.
    What the bytea input function sees is just one
    single quote, which it treats as a plain data character.
    However, the bytea input function treats
    backslashes as special, and the other behaviors shown in
     are implemented by
    that function.
   

   

    In some contexts, backslashes must be doubled compared to what is
    shown above, because the generic string-literal parser will also
    reduce pairs of backslashes to one data character;
    see .
   

   

    Bytea octets are output in hex
    format by default.  If you change 
    to escape,
    non-printable octets are converted to their
    equivalent three-digit octal value and preceded by one backslash.
    Most printable octets are output by their standard
    representation in the client character set, e.g.:

```
SET bytea_output = 'escape';

SELECT 'abc \153\154\155 \052\251\124'::bytea;
     bytea
----------------
 abc klm *\251T
```

    The octet with decimal value 92 (backslash) is doubled in the output.
    Details are in .
   

   
    
# bytea Output Escaped Octets

    
     
     
     
     
     
     
      
       Decimal Octet Value
       Description
       Escaped Output Representation
       Example
       Output Result
      
     

     

      
       92
       backslash
       \\
       '\134'::bytea
       \\
      

      
       0 to 31 and 127 to 255
       non-printable octets
       \xxx (octal value)
       '\001'::bytea
       \001
      

      
       32 to 126
       printable octets
       client character set representation
       '\176'::bytea
       ~
      

     
    
   

   

    Depending on the front end to PostgreSQL you use,
    you might have additional work to do in terms of escaping and
    unescaping bytea strings. For example, you might also
    have to escape line feeds and carriage returns if your interface
    automatically translates these.
   

  

 

  

   
# Date/Time Types

   
    date
   
   
    time
   
   
    time without time zone
   
   
    time with time zone
   
   
    timestamp
   
   
    timestamptz
   
   
    timestamp with time zone
   
   
    timestamp without time zone
   
   
    interval
   
   
    time span
   

   

    PostgreSQL supports the full set of
    SQL date and time types, shown in .  The operations available
    on these data types are described in
    .
    Dates are counted according to the Gregorian calendar, even in
    years before that calendar was introduced (see  for more information).
   

    
     
# Date/Time Types

     
      
       
        Name
        Storage Size
        Description
        Low Value
        High Value
        Resolution
       
      
      
       
        timestamp [ (p) ] [ without time zone ]
        8 bytes
        both date and time (no time zone conversion)
        4713 BC
        294276 AD
        1 microsecond
       
       
        timestamp [ (p) ] with time zone
        8 bytes
        both date and time, with time zone conversion
        4713 BC
        294276 AD
        1 microsecond
       
       
        date
        4 bytes
        date (no time of day)
        4713 BC
        5874897 AD
        1 day
       
       
        time [ (p) ] [ without time zone ]
        8 bytes
        time of day (no date)
        00:00:00
        24:00:00
        1 microsecond
       
       
        time [ (p) ] with time zone
        12 bytes
        time of day (no date), with time zone
        
        00:00:00+1559
        24:00:00-1559
        1 microsecond
       
       
        interval [ fields ] [ (p) ]
        16 bytes
        time interval
        -178000000 years
        178000000 years
        1 microsecond
       
      
     
    

   
    

     The SQL standard requires that writing just timestamp
     be equivalent to timestamp without time
     zone, and PostgreSQL honors that
     behavior.  timestamptz is accepted as an
     abbreviation for timestamp with time zone; this is a
     PostgreSQL extension.
    

   

   

    time, timestamp, and
    interval accept an optional precision value
    p which specifies the number of
    fractional digits retained in the seconds field. By default, there
    is no explicit bound on precision.  The allowed range of
    p is from 0 to 6.
   

   

    The interval type has an additional option, which is
    to restrict the set of stored fields by writing one of these phrases:

YEAR
MONTH
DAY
HOUR
MINUTE
SECOND
YEAR TO MONTH
DAY TO HOUR
DAY TO MINUTE
DAY TO SECOND
HOUR TO MINUTE
HOUR TO SECOND
MINUTE TO SECOND

    Note that if both fields and
    p are specified, the
    fields must include SECOND,
    since the precision applies only to the seconds.
   

   

    The type time with time zone is defined by the SQL
    standard, but the definition exhibits properties which lead to
    questionable usefulness. In most cases, a combination of
    date, time, timestamp without time
    zone, and timestamp with time zone should
    provide a complete range of date/time functionality required by
    any application.
   

   

    
# Date/Time Input

    

     Date and time input is accepted in almost any reasonable format, including
     ISO 8601, SQL-compatible,
     traditional POSTGRES, and others.
     For some formats, ordering of day, month, and year in date input is
     ambiguous and there is support for specifying the expected
     ordering of these fields.  Set the  parameter
     to MDY to select month-day-year interpretation,
     DMY to select day-month-year interpretation, or
     YMD to select year-month-day interpretation.
    

    

     PostgreSQL is more flexible in
     handling date/time input than the
     SQL standard requires.
     See 
     for the exact parsing rules of date/time input and for the
     recognized text fields including months, days of the week, and
     time zones.
    

    

     Remember that any date or time literal input needs to be enclosed
     in single quotes, like text strings.  Refer to
      for more
     information.
     SQL requires the following syntax

type [ (p) ] 'value'

     where p is an optional precision
     specification giving the number of
     fractional digits in the seconds field. Precision can be
     specified for time, timestamp, and
     interval types, and can range from 0 to 6.
     If no precision is specified in a constant specification,
     it defaults to the precision of the literal value (but not
     more than 6 digits).
    

    

    
# Dates

    
     date
    

    

      shows some possible
     inputs for the date type.
    

     
      
# Date Input

      
       
       
       
        
         Example
         Description
        
       
       
        
         1999-01-08
         ISO 8601; January 8 in any mode
         (recommended format)
        
        
         January 8, 1999
         unambiguous in any datestyle input mode
        
        
         1/8/1999
         January 8 in MDY mode;
          August 1 in DMY mode
        
        
         1/18/1999
         January 18 in MDY mode;
          rejected in other modes
        
        
         01/02/03
         January 2, 2003 in MDY mode;
          February 1, 2003 in DMY mode;
          February 3, 2001 in YMD mode
         
        
        
         1999-Jan-08
         January 8 in any mode
        
        
         Jan-08-1999
         January 8 in any mode
        
        
         08-Jan-1999
         January 8 in any mode
        
        
         99-Jan-08
         January 8 in YMD mode, else error
        
        
         08-Jan-99
         January 8, except error in YMD mode
        
        
         Jan-08-99
         January 8, except error in YMD mode
        
        
         19990108
         ISO 8601; January 8, 1999 in any mode
        
        
         990108
         ISO 8601; January 8, 1999 in any mode
        
        
         1999.008
         year and day of year
        
        
         J2451187
         Julian date
        
        
         January 8, 99 BC
         year 99 BC
        
       
      
     
    

    

     
# Times

     
      time
     
     
      time without time zone
     
     
      time with time zone
     

     

      The time-of-day types are time [
      (p) ] without time zone and
      time [ (p) ] with time
      zone.  time alone is equivalent to
      time without time zone.
     

     

      Valid input for these types consists of a time of day followed
      by an optional time zone. (See 
      and .)  If a time zone is
      specified in the input for time without time zone,
      it is silently ignored. You can also specify a date but it will
      be ignored, except when you use a time zone name that involves a
      daylight-savings rule, such as
      America/New_York. In this case specifying the date
      is required in order to determine whether standard or daylight-savings
      time applies.  The appropriate time zone offset is recorded in the
      time with time zone value and is output as stored;
      it is not adjusted to the active time zone.
     

      
       
# Time Input

       
        
         
          Example
          Description
         
        
        
         
          04:05:06.789
          ISO 8601
         
         
          04:05:06
          ISO 8601
         
         
          04:05
          ISO 8601
         
         
          040506
          ISO 8601
         
         
          04:05 AM
          same as 04:05; AM does not affect value
         
         
          04:05 PM
          same as 16:05; input hour must be <= 12
         
         
          04:05:06.789-8
          ISO 8601, with time zone as UTC offset
         
         
          04:05:06-08:00
          ISO 8601, with time zone as UTC offset
         
         
          04:05-08:00
          ISO 8601, with time zone as UTC offset
         
         
          040506-08
          ISO 8601, with time zone as UTC offset
         
         
          040506+0730
          ISO 8601, with fractional-hour time zone as UTC offset
         
         
          040506+07:30:00
          UTC offset specified to seconds (not allowed in ISO 8601)
         
         
          04:05:06 PST
          time zone specified by abbreviation
         
         
          2003-04-12 04:05:06 America/New_York
          time zone specified by full name
         
        
       
      

      
       
# Time Zone Input

       
        
         
          Example
          Description
         
        
        
         
          PST
          Abbreviation (for Pacific Standard Time)
         
         
          America/New_York
          Full time zone name
         
         
          PST8PDT
          POSIX-style time zone specification
         
         
          -8:00:00
          UTC offset for PST
         
         
          -8:00
          UTC offset for PST (ISO 8601 extended format)
         
         
          -800
          UTC offset for PST (ISO 8601 basic format)
         
         
          -8
          UTC offset for PST (ISO 8601 basic format)
         
         
          zulu
          Military abbreviation for UTC
         
         
          z
          Short form of zulu (also in ISO 8601)
         
        
       
      

     

     Refer to  for more information on how
     to specify time zones.
    

    

    

    
# Time Stamps

    
     timestamp
    

    
     timestamp with time zone
    

    
     timestamp without time zone
    

     

      Valid input for the time stamp types consists of the concatenation
      of a date and a time, followed by an optional time zone,
      followed by an optional AD or BC.
      (Alternatively, AD/BC can appear
      before the time zone, but this is not the preferred ordering.)
      Thus:

```
1999-01-08 04:05:06
```

      and:

```
1999-01-08 04:05:06 -8:00
```

      are valid values, which follow the ISO 8601
      standard.  In addition, the common format:

```
January 8 04:05:06 1999 PST
```

      is supported.
     

     

      The SQL standard differentiates
      timestamp without time zone
      and timestamp with time zone literals by the presence of a
      + or - symbol and time zone offset after
      the time.  Hence, according to the standard,

```
TIMESTAMP '2004-10-19 10:23:54'
```

      is a timestamp without time zone, while

```
TIMESTAMP '2004-10-19 10:23:54+02'
```

      is a timestamp with time zone.
      PostgreSQL never examines the content of a
      literal string before determining its type, and therefore will treat
      both of the above as timestamp without time zone.  To
      ensure that a literal is treated as timestamp with time
      zone, give it the correct explicit type:

```
TIMESTAMP WITH TIME ZONE '2004-10-19 10:23:54+02'
```

     

     

      In a value that has been determined to be timestamp without time
      zone, PostgreSQL will silently ignore
      any time zone indication.
      That is, the resulting value is derived from the date/time
      fields in the input string, and is not adjusted for time zone.
     

     

      For timestamp with time zone values, an input string
      that includes an explicit time zone will be converted to UTC
      (Universal Coordinated
      Time) using the appropriate offset
      for that time zone.  If no time zone is stated in the input string,
      then it is assumed to be in the time zone indicated by the system's
       parameter, and is converted to UTC using the
      offset for the timezone zone.
      In either case, the value is stored internally as UTC.  The
      originally stated or assumed time zone is not retained and
      cannot be retrieved later.
     

     

      When a timestamp with time
      zone value is output, it is always converted from UTC to the
      current timezone zone, and displayed as local time in that
      zone.  To see the time in another time zone, either change
      timezone or use the AT TIME ZONE construct
      (see ).
     

     

      Conversions between timestamp without time zone and
      timestamp with time zone normally assume that the
      timestamp without time zone value should be taken or given
      as timezone local time.  A different time zone can
      be specified for the conversion using AT TIME ZONE.
     

    

    

     
# Special Values

     
      time
      constants
     

     
      date
      constants
     

     

      PostgreSQL supports several
      special date/time input values for convenience, as shown in .  The values
      infinity and -infinity
      are specially represented inside the system and will be displayed
      unchanged; but the others are simply notational shorthands
      that will be converted to ordinary date/time values when read.
      (In particular, now and related strings are converted
      to a specific time value as soon as they are read.)
      All of these values need to be enclosed in single quotes when used
      as constants in SQL commands.
     

      
       
# Special Date/Time Inputs

       
        
         
          Input String
          Valid Types
          Description
         
        
        
         
          epoch
          date, timestamp
          1970-01-01 00:00:00+00 (Unix system time zero)
         
         
          infinity
          date, timestamp, interval
          later than all other time stamps
         
         
          -infinity
          date, timestamp, interval
          earlier than all other time stamps
         
         
          now
          date, time, timestamp
          current transaction's start time
         
         
          today
          date, timestamp
          midnight (00:00) today
         
         
          tomorrow
          date, timestamp
          midnight (00:00) tomorrow
         
         
          yesterday
          date, timestamp
          midnight (00:00) yesterday
         
         
          allballs
          time
          00:00:00.00 UTC
         
        
       
      

     

      The following SQL-compatible functions can also
      be used to obtain the current time value for the corresponding data
      type:
      CURRENT_DATE, CURRENT_TIME,
      CURRENT_TIMESTAMP, LOCALTIME,
      LOCALTIMESTAMP.  (See .)  Note that these are
      SQL functions and are not recognized in data input strings.
     

     
      

       While the input strings now,
       today, tomorrow,
       and yesterday are fine to use in interactive SQL
       commands, they can have surprising behavior when the command is
       saved to be executed later, for example in prepared statements,
       views, and function definitions.  The string can be converted to a
       specific time value that continues to be used long after it becomes
       stale.  Use one of the SQL functions instead in such contexts.
       For example, CURRENT_DATE + 1 is safer than
       'tomorrow'::date.
      

     

    

   

   

    
# Date/Time Output

    
     date
     output format
     formatting
    

    
     time
     output format
     formatting
    

    

     The output format of the date/time types can be set to one of the four
     styles ISO 8601,
     SQL (Ingres), traditional POSTGRES
     (Unix date format), or
     German.  The default
     is the ISO format.  (The
     SQL standard requires the use of the ISO 8601
     format.  The name of the SQL output format is a
     historical accident.)   shows examples of each
     output style.  The output of the date and
     time types is generally only the date or time part
     in accordance with the given examples.  However, the
     POSTGRES style outputs date-only values in
     ISO format.
    

     
      
# Date/Time Output Styles

      
       
       
       
       
        
         Style Specification
         Description
         Example
        
       
       
        
         ISO
         ISO 8601, SQL standard
         1997-12-17 07:37:16-08
        
        
         SQL
         traditional style
         12/17/1997 07:37:16.00 PST
        
        
         Postgres
         original style
         Wed Dec 17 07:37:16 1997 PST
        
        
         German
         regional style
         17.12.1997 07:37:16.00 PST
        
       
      
     

    
     

      ISO 8601 specifies the use of uppercase letter T to separate
      the date and time.  PostgreSQL accepts that format on
      input, but on output it uses a space rather than T, as shown
      above.  This is for readability and for consistency with
      RFC 3339 as
      well as some other database systems.
     

    

    

     In the SQL and POSTGRES styles, day appears before
     month if DMY field ordering has been specified, otherwise month appears
     before day.
     (See 
     for how this setting also affects interpretation of input values.)
      shows examples.
    

     
      
# Date Order Conventions

      
       
       
       
       
        
         datestyle Setting
         Input Ordering
         Example Output
        
       
       
        
         SQL, DMY
         day/month/year
         17/12/1997 15:37:16.00 CET
        
        
         SQL, MDY
         month/day/year
         12/17/1997 07:37:16.00 PST
        
        
         Postgres, DMY
         day/month/year
         Wed 17 Dec 07:37:16 1997 PST
        
       
      
     

    

     In the ISO style, the time zone is always shown as
     a signed numeric offset from UTC, with positive sign used for zones
     east of Greenwich.  The offset will be shown
     as hh (hours only) if it is an integral
     number of hours, else
     as hh:mm if it
     is an integral number of minutes, else as
     hh:mm:ss.
     (The third case is not possible with any modern time zone standard,
     but it can appear when working with timestamps that predate the
     adoption of standardized time zones.)
     In the other date styles, the time zone is shown as an alphabetic
     abbreviation if one is in common use in the current zone.  Otherwise
     it appears as a signed numeric offset in ISO 8601 basic format
     (hh or hhmm).
     The alphabetic abbreviations shown in these styles are taken from the
     IANA time zone database entry currently selected by the
      run-time parameter; they are not
     affected by the  setting.
    

    

     The date/time style can be selected by the user using the
     SET datestyle command, the  parameter in the
     postgresql.conf configuration file, or the
     PGDATESTYLE environment variable on the server or
     client.
    

    

     The formatting function to_char
     (see ) is also available as
     a more flexible way to format date/time output.
    

   

   

    
# Time Zones

    
     time zone
    

   

    Time zones, and time-zone conventions, are influenced by
    political decisions, not just earth geometry. Time zones around the
    world became somewhat standardized during the 1900s,
    but continue to be prone to arbitrary changes, particularly with
    respect to daylight-savings rules.
    PostgreSQL uses the widely-used
    IANA (Olson) time zone database for information about
    historical time zone rules.  For times in the future, the assumption
    is that the latest known rules for a given time zone will
    continue to be observed indefinitely far into the future.
   

    

     PostgreSQL endeavors to be compatible with
     the SQL standard definitions for typical usage.
     However, the SQL standard has an odd mix of date and
     time types and capabilities. Two obvious problems are:

     

      

       

        Although the date type
        cannot have an associated time zone, the
        time type can.
        Time zones in the real world have little meaning unless
        associated with a date as well as a time,
        since the offset can vary through the year with daylight-saving
        time boundaries.
       

      

      

       

        The default time zone is specified as a constant numeric offset
        from UTC. It is therefore impossible to adapt to
        daylight-saving time when doing date/time arithmetic across
        DST boundaries.
       

      

     

    

    

     To address these difficulties, we recommend using date/time types
     that contain both date and time when using time zones. We
     do not recommend using the type time with
     time zone (though it is supported by
     PostgreSQL for legacy applications and
     for compliance with the SQL standard).
     PostgreSQL assumes
     your local time zone for any type containing only date or time.
    

    

     All timezone-aware dates and times are stored internally in
     UTC.  They are converted to local time
     in the zone specified by the  configuration
     parameter before being displayed to the client.
    

    

     PostgreSQL allows you to specify time zones in
     three different forms:
     

      

       

        A full time zone name, for example America/New_York.
        The recognized time zone names are listed in the
        pg_timezone_names view (see ).
        PostgreSQL uses the widely-used IANA
        time zone data for this purpose, so the same time zone
        names are also recognized by other software.
       

      

      

       

        A time zone abbreviation, for example PST.  Such a
        specification merely defines a particular offset from UTC, in
        contrast to full time zone names which can imply a set of daylight
        savings transition rules as well.  The recognized abbreviations
        are listed in the pg_timezone_abbrevs view (see ).  You cannot set the
        configuration parameters  or
         to a time
        zone abbreviation, but you can use abbreviations in
        date/time input values and with the AT TIME ZONE
        operator.
       

      

      

       

        In addition to the timezone names and abbreviations,
        PostgreSQL will accept POSIX-style time zone
        specifications, as described in
        .  This option is not
        normally preferable to using a named time zone, but it may be
        necessary if no suitable IANA time zone entry is available.
       

      

     

     In short, this is the difference between abbreviations
     and full names: abbreviations represent a specific offset from UTC,
     whereas many of the full names imply a local daylight-savings time
     rule, and so have two possible UTC offsets.  As an example,
     2014-06-04 12:00 America/New_York represents noon local
     time in New York, which for this particular date was Eastern Daylight
     Time (UTC-4).  So 2014-06-04 12:00 EDT specifies that
     same time instant.  But 2014-06-04 12:00 EST specifies
     noon Eastern Standard Time (UTC-5), regardless of whether daylight
     savings was nominally in effect on that date.
    

    
     

      The sign in POSIX-style time zone specifications has the opposite meaning
      of the sign in ISO-8601 datetime values.  For example, the POSIX time zone
      for 2014-06-04 12:00+04 would be UTC-4.
     

    

    

     To complicate matters, some jurisdictions have used the same timezone
     abbreviation to mean different UTC offsets at different times; for
     example, in Moscow MSK has meant UTC+3 in some years and
     UTC+4 in others.  PostgreSQL interprets such
     abbreviations according to whatever they meant (or had most recently
     meant) on the specified date; but, as with the EST example
     above, this is not necessarily the same as local civil time on that date.
    

    

     In all cases, timezone names and abbreviations are recognized
     case-insensitively.  (This is a change from PostgreSQL
     versions prior to 8.2, which were case-sensitive in some contexts but
     not others.)
    

    

     Neither timezone names nor abbreviations are hard-wired into the server;
     they are obtained from configuration files stored under
     .../share/timezone/ and .../share/timezonesets/
     of the installation directory
     (see ).
    

    

     The  configuration parameter can
     be set in the file postgresql.conf, or in any of the
     other standard ways described in .
     There are also some special ways to set it:

     

      

       

        The SQL command SET TIME ZONE
        sets the time zone for the session.  This is an alternative spelling
        of SET TIMEZONE TO with a more SQL-spec-compatible syntax.
       

      

      

       

        The PGTZ environment variable is used by
        libpq clients
        to send a SET TIME ZONE
        command to the server upon connection.
       

      

     

    

   

   

    
# Interval Input

    
     interval
    

     

      interval values can be written using the following
      verbose syntax:

@ quantity unit quantity unit... direction

     where quantity is a number (possibly signed);
     unit is microsecond,
     millisecond, second,
     minute, hour, day,
     week, month, year,
     decade, century, millennium,
     or abbreviations or plurals of these units;
     direction can be ago or
     empty.  The at sign (@) is optional noise.  The amounts
     of the different units are implicitly added with appropriate
     sign accounting.  ago negates all the fields.
     This syntax is also used for interval output, if
      is set to
     postgres_verbose.
    

    

     Quantities of days, hours, minutes, and seconds can be specified without
     explicit unit markings.  For example, '1 12:59:10' is read
     the same as '1 day 12 hours 59 min 10 sec'.  Also,
     a combination of years and months can be specified with a dash;
     for example '200-10' is read the same as '200 years
     10 months'.  (These shorter forms are in fact the only ones allowed
     by the SQL standard, and are used for output when
     IntervalStyle is set to sql_standard.)
    

    

     Interval values can also be written as ISO 8601 time intervals, using
     either the format with designators of the standard's section
     4.4.3.2 or the alternative format of section 4.4.3.3.  The
     format with designators looks like this:

P quantity unit  quantity unit ...  T  quantity unit ...

      The string must start with a P, and may include a
      T that introduces the time-of-day units.  The
      available unit abbreviations are given in .  Units may be
      omitted, and may be specified in any order, but units smaller than
      a day must appear after T.  In particular, the meaning of
      M depends on whether it is before or after
      T.
     

     
      
# ISO 8601 Interval Unit Abbreviations

     
       
        
         Abbreviation
         Meaning
        
       
       
        
         Y
         Years
        
        
         M
         Months (in the date part)
        
        
         W
         Weeks
        
        
         D
         Days
        
        
         H
         Hours
        
        
         M
         Minutes (in the time part)
        
        
         S
         Seconds
        
       
      
     

     

      In the alternative format:

P  years-months-days   T hours:minutes:seconds 

      the string must begin with P, and a
      T separates the date and time parts of the interval.
      The values are given as numbers similar to ISO 8601 dates.
    

    

     When writing an interval constant with a fields
     specification, or when assigning a string to an interval column that was
     defined with a fields specification, the interpretation of
     unmarked quantities depends on the fields.  For
     example INTERVAL '1' YEAR is read as 1 year, whereas
     INTERVAL '1' means 1 second.  Also, field values
     to the right of the least significant field allowed by the
     fields specification are silently discarded.  For
     example, writing INTERVAL '1 day 2:03:04' HOUR TO MINUTE
     results in dropping the seconds field, but not the day field.
    

    

     According to the SQL standard all fields of an interval
     value must have the same sign, so a leading negative sign applies to all
     fields; for example the negative sign in the interval literal
     '-1 2:03:04' applies to both the days and hour/minute/second
     parts.  PostgreSQL allows the fields to have different
     signs, and traditionally treats each field in the textual representation
     as independently signed, so that the hour/minute/second part is
     considered positive in this example.  If IntervalStyle is
     set to sql_standard then a leading sign is considered
     to apply to all fields (but only if no additional signs appear).
     Otherwise the traditional PostgreSQL interpretation is
     used.  To avoid ambiguity, it's recommended to attach an explicit sign
     to each field if any field is negative.
    

    

     Internally, interval values are stored as three integral
     fields: months, days, and microseconds.  These fields are kept
     separate because the number of days in a month varies, while a day
     can have 23 or 25 hours if a daylight savings time transition is
     involved.  An interval input string that uses other units is
     normalized into this format, and then reconstructed in a standardized
     way for output, for example:

```
SELECT '2 years 15 months 100 weeks 99 hours 123456789 milliseconds'::interval;
               interval
---------------------------------------
 3 years 3 mons 700 days 133:17:36.789
```

     Here weeks, which are understood as 7 days, have been
     kept separate, while the smaller and larger time units were
     combined and normalized.
    

    

     Input field values can have fractional parts, for example '1.5
     weeks' or '01:02:03.45'.  However,
     because interval internally stores only integral fields,
     fractional values must be converted into smaller
     units.  Fractional parts of units greater than months are rounded to
     be an integer number of months, e.g. '1.5 years'
     becomes '1 year 6 mons'.  Fractional parts of
     weeks and days are computed to be an integer number of days and
     microseconds, assuming 30 days per month and 24 hours per day, e.g.,
     '1.75 months' becomes 1 mon 22 days
     12:00:00.  Only seconds will ever be shown as fractional
     on output.
    

    

      shows some examples
     of valid interval input.
    

     
      
# Interval Input

      
       
        
         Example
         Description
        
       
       
        
         1-2
         SQL standard format: 1 year 2 months
        
        
         3 4:05:06
         SQL standard format: 3 days 4 hours 5 minutes 6 seconds
        
        
         1 year 2 months 3 days 4 hours 5 minutes 6 seconds
         Traditional Postgres format: 1 year 2 months 3 days 4 hours 5 minutes 6 seconds
        
        
         P1Y2M3DT4H5M6S
         ISO 8601 format with designators: same meaning as above
        
        
         P0001-02-03T04:05:06
         ISO 8601 alternative format: same meaning as above
        
       
      
     

   

   

    
# Interval Output

    
     interval
     output format
     formatting
    

    

     As previously explained, PostgreSQL
     stores interval values as months, days, and
     microseconds.  For output, the months field is converted to years and
     months by dividing by 12.  The days field is shown as-is.  The
     microseconds field is converted to hours, minutes, seconds, and
     fractional seconds.  Thus months, minutes, and seconds will never be
     shown as exceeding the ranges 0&ndash;11, 0&ndash;59, and 0&ndash;59
     respectively, while the displayed years, days, and hours fields can
     be quite large.  (The justify_days
     and justify_hours
     functions can be used if it is desirable to transpose large days or
     hours values into the next higher field.)
    

    

     The output format of the interval type can be set to one of the
     four styles sql_standard, postgres,
     postgres_verbose, or iso_8601,
     using the command SET intervalstyle.
     The default is the postgres format.
      shows examples of each
     output style.
    

    

     The sql_standard style produces output that conforms to
     the SQL standard's specification for interval literal strings, if
     the interval value meets the standard's restrictions (either year-month
     only or day-time only, with no mixing of positive
     and negative components).  Otherwise the output looks like a standard
     year-month literal string followed by a day-time literal string,
     with explicit signs added to disambiguate mixed-sign intervals.
    

    

     The output of the postgres style matches the output of
     PostgreSQL releases prior to 8.4 when the
      parameter was set to ISO.
    

    

     The output of the postgres_verbose style matches the output of
     PostgreSQL releases prior to 8.4 when the
     DateStyle parameter was set to non-ISO output.
    

    

     The output of the iso_8601 style matches the format
     with designators described in section 4.4.3.2 of the
     ISO 8601 standard.
    

     
       
# Interval Output Style Examples

       
        
         
          Style Specification
          Year-Month Interval
          Day-Time Interval
          Mixed Interval
         
        
        
         
          sql_standard
          1-2
          3 4:05:06
          -1-2 +3 -4:05:06
         
         
          postgres
          1 year 2 mons
          3 days 04:05:06
          -1 year -2 mons +3 days -04:05:06
         
         
          postgres_verbose
          @ 1 year 2 mons
          @ 3 days 4 hours 5 mins 6 secs
          @ 1 year 2 mons -3 days 4 hours 5 mins 6 secs ago
         
         
          iso_8601
          P1Y2M
          P3DT4H5M6S
          P-1Y-2M3D&zwsp;T-4H-5M-6S
         
        
       
    

   

  

  

   
# Boolean Type

   
    Boolean
    data type
   

   
    true
   

   
    false
   

   

    PostgreSQL provides the
    standard SQL type boolean;
    see .
    The boolean type can have several states:
    true, false, and a third state,
    unknown, which is represented by the
    SQL null value.
   

   
    
# Boolean Data Type

    
     
      
       Name
       Storage Size
       Description
      
     
     
      
       boolean
       1 byte
       state of true or false
      
     
    
   

   

    Boolean constants can be represented in SQL queries by the SQL
    key words TRUE, FALSE,
    and NULL.
   

   

    The datatype input function for type boolean accepts these
    string representations for the true state:
    
     true
     yes
     on
     1
    
    and these representations for the false state:
    
     false
     no
     off
     0
    
    Unique prefixes of these strings are also accepted, for
    example t or n.
    Leading or trailing whitespace is ignored, and case does not matter.
   

   

    The datatype output function for type boolean always emits
    either t or f, as shown in
    .
   

   
    
# Using the boolean Type

```
CREATE TABLE test1 (a boolean, b text);
INSERT INTO test1 VALUES (TRUE, 'sic est');
INSERT INTO test1 VALUES (FALSE, 'non est');
SELECT * FROM test1;
 a |    b
---+---------
 t | sic est
 f | non est

SELECT * FROM test1 WHERE a;
 a |    b
---+---------
 t | sic est
```

   

   

    The key words TRUE and FALSE are
    the preferred (SQL-compliant) method for writing
    Boolean constants in SQL queries.  But you can also use the string
    representations by following the generic string-literal constant syntax
    described in , for
    example 'yes'::boolean.
   

   

    Note that the parser automatically understands
    that TRUE and FALSE are of
    type boolean, but this is not so
    for NULL because that can have any type.
    So in some contexts you might have to cast NULL
    to boolean explicitly, for
    example NULL::boolean.  Conversely, the cast can be
    omitted from a string-literal Boolean value in contexts where the parser
    can deduce that the literal must be of type boolean.
   

  

  

   
# Enumerated Types

   
    data type
    enumerated (enum)
   

   
    enumerated types
   

   

    Enumerated (enum) types are data types that
    comprise a static, ordered set of values.
    They are equivalent to the enum
    types supported in a number of programming languages. An example of an enum
    type might be the days of the week, or a set of status values for
    a piece of data.
   

   

    
# Declaration of Enumerated Types

    

     Enum types are created using the  command,
     for example:

```
CREATE TYPE mood AS ENUM ('sad', 'ok', 'happy');
```

     Once created, the enum type can be used in table and function
     definitions much like any other type:

```
CREATE TYPE mood AS ENUM ('sad', 'ok', 'happy');
CREATE TABLE person (
    name text,
    current_mood mood
);
INSERT INTO person VALUES ('Moe', 'happy');
SELECT * FROM person WHERE current_mood = 'happy';
 name | current_mood
------+--------------
 Moe  | happy
(1 row)
```

    

    

    

     
# Ordering

     

      The ordering of the values in an enum type is the
      order in which the values were listed when the type was created.
      All standard comparison operators and related
      aggregate functions are supported for enums.  For example:

```
INSERT INTO person VALUES ('Larry', 'sad');
INSERT INTO person VALUES ('Curly', 'ok');
SELECT * FROM person WHERE current_mood > 'sad';
 name  | current_mood
-------+--------------
 Moe   | happy
 Curly | ok
(2 rows)

SELECT * FROM person WHERE current_mood > 'sad' ORDER BY current_mood;
 name  | current_mood
-------+--------------
 Curly | ok
 Moe   | happy
(2 rows)

SELECT name
FROM person
WHERE current_mood = (SELECT MIN(current_mood) FROM person);
 name
-------
 Larry
(1 row)
```

     

   

   

    
# Type Safety

    

     Each enumerated data type is separate and cannot
     be compared with other enumerated types.  See this example:

```
CREATE TYPE happiness AS ENUM ('happy', 'very happy', 'ecstatic');
CREATE TABLE holidays (
    num_weeks integer,
    happiness happiness
);
INSERT INTO holidays(num_weeks,happiness) VALUES (4, 'happy');
INSERT INTO holidays(num_weeks,happiness) VALUES (6, 'very happy');
INSERT INTO holidays(num_weeks,happiness) VALUES (8, 'ecstatic');
INSERT INTO holidays(num_weeks,happiness) VALUES (2, 'sad');
ERROR:  invalid input value for enum happiness: "sad"
SELECT person.name, holidays.num_weeks FROM person, holidays
  WHERE person.current_mood = holidays.happiness;
ERROR:  operator does not exist: mood = happiness
```

    

    

     If you really need to do something like that, you can either
     write a custom operator or add explicit casts to your query:

```
SELECT person.name, holidays.num_weeks FROM person, holidays
  WHERE person.current_mood::text = holidays.happiness::text;
 name | num_weeks
------+-----------
 Moe  |         4
(1 row)
```

    

   

   

    
# Implementation Details

    

     Enum labels are case sensitive, so
     'happy' is not the same as 'HAPPY'.
     White space in the labels is significant too.
    

    

     Although enum types are primarily intended for static sets of values,
     there is support for adding new values to an existing enum type, and for
     renaming values (see ).  Existing values
     cannot be removed from an enum type, nor can the sort ordering of such
     values be changed, short of dropping and re-creating the enum type.
    

    

     An enum value occupies four bytes on disk.  The length of an enum
     value's textual label is limited by the NAMEDATALEN
     setting compiled into PostgreSQL; in standard
     builds this means at most 63 bytes.
    

    

     The translations from internal enum values to textual labels are
     kept in the system catalog
     pg_enum.
     Querying this catalog directly can be useful.
    

   

  

  

   
# Geometric Types

   

    Geometric data types represent two-dimensional spatial
    objects.  shows the geometric
    types available in PostgreSQL.
   

    
     
# Geometric Types

     
      
      
      
      
      
       
        Name
        Storage Size
        Description
        Representation
       
      
      
       
        point
        16 bytes
        Point on a plane
        (x,y)
       
       
        line
        24 bytes
        Infinite line
        {A,B,C}
       
       
        lseg
        32 bytes
        Finite line segment
        [(x1,y1),(x2,y2)]
       
       
        box
        32 bytes
        Rectangular box
        (x1,y1),(x2,y2)
       
       
        path
        16+16n bytes
        Closed path (similar to polygon)
        ((x1,y1),...)
       
       
        path
        16+16n bytes
        Open path
        [(x1,y1),...]
       
       
        polygon
        40+16n bytes
        Polygon (similar to closed path)
        ((x1,y1),...)
       
       
        circle
        24 bytes
        Circle
        <(x,y),r> (center point and radius)
       
      
     
    

   

    In all these types, the individual coordinates are stored as
    double precision (float8) numbers.
   

   

    A rich set of functions and operators is available to perform various geometric
    operations such as scaling, translation, rotation, and determining
    intersections.  They are explained in .
   

   

    
# Points

    
     point
    

    

     Points are the fundamental two-dimensional building block for geometric
     types.  Values of type point are specified using either of
     the following syntaxes:

( x , y )
  x , y

     where x and y are the respective
     coordinates, as floating-point numbers.
    

    

     Points are output using the first syntax.
    

   

   

    
# Lines

    
     line
    

    

     Lines are represented by the linear
     equation Ax + By + C = 0,
     where A and B are not both zero.  Values
     of type line are input and output in the following form:

{ A, B, C }

     Alternatively, any of the following forms can be used for input:

[ ( x1 , y1 ) , ( x2 , y2 ) ]
( ( x1 , y1 ) , ( x2 , y2 ) )
  ( x1 , y1 ) , ( x2 , y2 )
    x1 , y1   ,   x2 , y2

     where
     (x1,y1)
     and
     (x2,y2)
     are two different points on the line.
    

   

   

    
# Line Segments

    
     lseg
    

    
     line segment
    

    

     Line segments are represented by pairs of points that are the endpoints
     of the segment.  Values of type lseg are specified using any
     of the following syntaxes:

[ ( x1 , y1 ) , ( x2 , y2 ) ]
( ( x1 , y1 ) , ( x2 , y2 ) )
  ( x1 , y1 ) , ( x2 , y2 )
    x1 , y1   ,   x2 , y2

     where
     (x1,y1)
     and
     (x2,y2)
     are the end points of the line segment.
    

    

     Line segments are output using the first syntax.
    

   

   

    
# Boxes

    
     box (data type)
    

    
     rectangle
    

    

     Boxes are represented by pairs of points that are opposite
     corners of the box.
     Values of type box are specified using any of the following
     syntaxes:

( ( x1 , y1 ) , ( x2 , y2 ) )
  ( x1 , y1 ) , ( x2 , y2 )
    x1 , y1   ,   x2 , y2

     where
     (x1,y1)
     and
     (x2,y2)
     are any two opposite corners of the box.
    

    

     Boxes are output using the second syntax.
    

    

     Any two opposite corners can be supplied on input, but the values
     will be reordered as needed to store the
     upper right and lower left corners, in that order.
    

   

   

    
# Paths

    
     path (data type)
    

    

     Paths are represented by lists of connected points. Paths can be
     open, where
     the first and last points in the list are considered not connected, or
     closed,
     where the first and last points are considered connected.
    

    

     Values of type path are specified using any of the following
     syntaxes:

[ ( x1 , y1 ) , ... , ( xn , yn ) ]
( ( x1 , y1 ) , ... , ( xn , yn ) )
  ( x1 , y1 ) , ... , ( xn , yn )
  ( x1 , y1   , ... ,   xn , yn )
    x1 , y1   , ... ,   xn , yn

     where the points are the end points of the line segments
     comprising the path.  Square brackets ([]) indicate
     an open path, while parentheses (()) indicate a
     closed path.  When the outermost parentheses are omitted, as
     in the third through fifth syntaxes, a closed path is assumed.
    

    

     Paths are output using the first or second syntax, as appropriate.
    

   

   

    
# Polygons

    
     polygon
    

    

     Polygons are represented by lists of points (the vertices of the
     polygon). Polygons are very similar to closed paths; the essential
     semantic difference is that a polygon is considered to include the
     area within it, while a path is not.
    

    

     An important implementation difference between polygons and
     paths is that the stored representation of a polygon includes its
     smallest bounding box.  This speeds up certain search operations,
     although computing the bounding box adds overhead while constructing
     new polygons.
    

    

     Values of type polygon are specified using any of the
     following syntaxes:

( ( x1 , y1 ) , ... , ( xn , yn ) )
  ( x1 , y1 ) , ... , ( xn , yn )
  ( x1 , y1   , ... ,   xn , yn )
    x1 , y1   , ... ,   xn , yn

     where the points are the end points of the line segments
     comprising the boundary of the polygon.
    

    

     Polygons are output using the first syntax.
    

   

   

    
# Circles

    
     circle
    

    

     Circles are represented by a center point and radius.
     Values of type circle are specified using any of the
     following syntaxes:

< ( x , y ) , r >
( ( x , y ) , r )
  ( x , y ) , r
    x , y   , r

     where
     (x,y)
     is the center point and r is the radius of the
     circle.
    

    

     Circles are output using the first syntax.
    

   

  

  

   
# Network Address Types

   
    network
    data types
   

   

    PostgreSQL offers data types to store IPv4, IPv6, and MAC
    addresses, as shown in .  It
    is better to use these types instead of plain text types to store
    network addresses, because
    these types offer input error checking and specialized
    operators and functions (see ).
   

    
     
# Network Address Types

     
      
      
      
      
       
        Name
        Storage Size
        Description
       
      
      

       
        cidr
        7 or 19 bytes
        IPv4 and IPv6 networks
       

       
        inet
        7 or 19 bytes
        IPv4 and IPv6 hosts and networks
       

       
        macaddr
        6 bytes
        MAC addresses
       

       
        macaddr8
        8 bytes
        MAC addresses (EUI-64 format)
       

      
     
    

   

    When sorting inet or cidr data types,
    IPv4 addresses will always sort before IPv6 addresses, including
    IPv4 addresses encapsulated or mapped to IPv6 addresses, such as
    ::10.2.3.4 or ::ffff:10.4.3.2.
   

   

    
# inet

    
     inet (data type)
    

    

     The inet type holds an IPv4 or IPv6 host address, and
     optionally its subnet, all in one field.
     The subnet is represented by the number of network address bits
     present in the host address (the
     netmask).  If the netmask is 32 and the address is IPv4,
     then the value does not indicate a subnet, only a single host.
     In IPv6, the address length is 128 bits, so 128 bits specify a
     unique host address.  Note that if you
     want to accept only networks, you should use the
     cidr type rather than inet.
    

    

      The input format for this type is
      address/y
      where
      address
      is an IPv4 or IPv6 address and
      y
      is the number of bits in the netmask.  If the
      /y
      portion is omitted, the
      netmask is taken to be 32 for IPv4 or 128 for IPv6,
      so the value represents
      just a single host.  On display, the
      /y
      portion is suppressed if the netmask specifies a single host.
    

   

   

    
# cidr

    
     cidr
    

    

     The cidr type holds an IPv4 or IPv6 network specification.
     Input and output formats follow Classless Internet Domain Routing
     conventions.
     The format for specifying networks is address/y where address is the network's lowest
     address represented as an
     IPv4 or IPv6 address, and y is the number of bits in the netmask.  If
     y is omitted, it is calculated
     using assumptions from the older classful network numbering system, except
     it will be at least large enough to include all of the octets
     written in the input.  It is an error to specify a network address
     that has bits set to the right of the specified netmask.
    

    

      shows some examples.
    

     
      
# cidr Type Input Examples

      
       
        
         cidr Input
         cidr Output
         abbrev(cidr)
        
       
       
        
         192.168.100.128/25
         192.168.100.128/25
         192.168.100.128/25
        
        
         192.168/24
         192.168.0.0/24
         192.168.0/24
        
        
         192.168/25
         192.168.0.0/25
         192.168.0.0/25
        
        
         192.168.1
         192.168.1.0/24
         192.168.1/24
        
        
         192.168
         192.168.0.0/24
         192.168.0/24
        
        
         128.1
         128.1.0.0/16
         128.1/16
        
        
         128
         128.0.0.0/16
         128.0/16
        
        
         128.1.2
         128.1.2.0/24
         128.1.2/24
        
        
         10.1.2
         10.1.2.0/24
         10.1.2/24
        
        
         10.1
         10.1.0.0/16
         10.1/16
        
        
         10
         10.0.0.0/8
         10/8
        
        
         10.1.2.3/32
         10.1.2.3/32
         10.1.2.3/32
        
        
         2001:4f8:3:ba::/64
         2001:4f8:3:ba::/64
         2001:4f8:3:ba/64
        
        
         2001:4f8:3:ba:&zwsp;2e0:81ff:fe22:d1f1/128
         2001:4f8:3:ba:&zwsp;2e0:81ff:fe22:d1f1/128
         2001:4f8:3:ba:&zwsp;2e0:81ff:fe22:d1f1/128
        
        
         ::ffff:1.2.3.0/120
         ::ffff:1.2.3.0/120
         ::ffff:1.2.3/120
        
        
         ::ffff:1.2.3.0/128
         ::ffff:1.2.3.0/128
         ::ffff:1.2.3.0/128
        
       
      
     
   

   

    
# inet vs. cidr

    

    The essential difference between inet and cidr
    data types is that inet accepts values with nonzero bits to
    the right of the netmask, whereas cidr does not.  For
    example, 192.168.0.1/24 is valid for inet
    but not for cidr.
    

      
        

        If you do not like the output format for inet or
        cidr values, try the functions host,
        text, and abbrev.
        

      
   

   

    
# macaddr

    
     macaddr (data type)
    

    
     MAC address
     macaddr
    

    

     The macaddr type stores MAC addresses, known for example
     from Ethernet card hardware addresses (although MAC addresses are
     used for other purposes as well).  Input is accepted in the
     following formats:

     
      '08:00:2b:01:02:03'
      '08-00-2b-01-02-03'
      '08002b:010203'
      '08002b-010203'
      '0800.2b01.0203'
      '0800-2b01-0203'
      '08002b010203'
     

     These examples all specify the same address.  Upper and
     lower case is accepted for the digits
     a through f.  Output is always in the
     first of the forms shown.
    

    

     IEEE Standard 802-2001 specifies the second form shown (with hyphens)
     as the canonical form for MAC addresses, and specifies the first
     form (with colons) as used with bit-reversed, MSB-first notation, so that
     08-00-2b-01-02-03 = 10:00:D4:80:40:C0.  This convention is widely
     ignored nowadays, and it is relevant only for obsolete network
     protocols (such as Token Ring).  PostgreSQL makes no provisions
     for bit reversal; all accepted formats use the canonical LSB
     order.
    

    

     The remaining five input formats are not part of any standard.
    

   

   

    
# macaddr8

    
     macaddr8 (data type)
    

    
     MAC address (EUI-64 format)
     macaddr
    

    

     The macaddr8 type stores MAC addresses in EUI-64
     format, known for example from Ethernet card hardware addresses
     (although MAC addresses are used for other purposes as well).
     This type can accept both 6 and 8 byte length MAC addresses
     and stores them in 8 byte length format.  MAC addresses given
     in 6 byte format will be stored in 8 byte length format with the
     4th and 5th bytes set to FF and FE, respectively.

     Note that IPv6 uses a modified EUI-64 format where the 7th bit
     should be set to one after the conversion from EUI-48.  The
     function macaddr8_set7bit is provided to make this
     change.

     Generally speaking, any input which is comprised of pairs of hex
     digits (on byte boundaries), optionally separated consistently by
     one of ':', '-' or '.', is
     accepted.  The number of hex digits must be either 16 (8 bytes) or
     12 (6 bytes).  Leading and trailing whitespace is ignored.

     The following are examples of input formats that are accepted:

     
      '08:00:2b:01:02:03:04:05'
      '08-00-2b-01-02-03-04-05'
      '08002b:0102030405'
      '08002b-0102030405'
      '0800.2b01.0203.0405'
      '0800-2b01-0203-0405'
      '08002b01:02030405'
      '08002b0102030405'
     

     These examples all specify the same address.  Upper and
     lower case is accepted for the digits
     a through f.  Output is always in the
     first of the forms shown.
    

    

     The last six input formats shown above are not part of any standard.
    

    

     To convert a traditional 48 bit MAC address in EUI-48 format to
     modified EUI-64 format to be included as the host portion of an
     IPv6 address, use macaddr8_set7bit as shown:

```
SELECT macaddr8_set7bit('08:00:2b:01:02:03');

    macaddr8_set7bit
-------------------------
 0a:00:2b:ff:fe:01:02:03
(1 row)

```

    

   

  

  

   
# Bit String Types

   
    bit string
    data type
   

   

    Bit strings are strings of 1's and 0's.  They can be used to store
    or visualize bit masks.  There are two SQL bit types:
    bit(n) and bit
    varying(n), where
    n is a positive integer.
   

   

    bit type data must match the length
    n exactly; it is an error to attempt to
    store shorter or longer bit strings.  bit varying data is
    of variable length up to the maximum length
    n; longer strings will be rejected.
    Writing bit without a length is equivalent to
    bit(1), while bit varying without a length
    specification means unlimited length.
   

   
    

     If one explicitly casts a bit-string value to
     bit(n), it will be truncated or
     zero-padded on the right to be exactly n bits,
     without raising an error.  Similarly,
     if one explicitly casts a bit-string value to
     bit varying(n), it will be truncated
     on the right if it is more than n bits.
    

   

   

    Refer to  for information about the syntax
    of bit string constants.  Bit-logical operators and string
    manipulation functions are available; see .
   

   
    
# Using the Bit String Types

```
CREATE TABLE test (a BIT(3), b BIT VARYING(5));
INSERT INTO test VALUES (B'101', B'00');
INSERT INTO test VALUES (B'10', B'101');

ERROR:  bit string length 2 does not match type bit(3)

INSERT INTO test VALUES (B'10'::bit(3), B'101');
SELECT * FROM test;

  a  |  b
-----+-----
 101 | 00
 100 | 101

```

   

   

    A bit string value requires 1 byte for each group of 8 bits, plus
    5 or 8 bytes overhead depending on the length of the string
    (but long values may be compressed or moved out-of-line, as explained
    in  for character strings).
   

  

  

   
# Text Search Types

   
    full text search
    data types
   

   
    text search
    data types
   

   

    PostgreSQL provides two data types that
    are designed to support full text search, which is the activity of
    searching through a collection of natural-language documents
    to locate those that best match a query.
    The tsvector type represents a document in a form optimized
    for text search; the tsquery type similarly represents
    a text query.
     provides a detailed explanation of this
    facility, and  summarizes the
    related functions and operators.
   

   

    
# tsvector

    
     tsvector (data type)
    

    

     A tsvector value is a sorted list of distinct
     lexemes, which are words that have been
     normalized to merge different variants of the same word
     (see  for details).  Sorting and
     duplicate-elimination are done automatically during input, as shown in
     this example:

```
SELECT 'a fat cat sat on a mat and ate a fat rat'::tsvector;
                      tsvector
----------------------------------------------------
 'a' 'and' 'ate' 'cat' 'fat' 'mat' 'on' 'rat' 'sat'
```

     To represent
     lexemes containing whitespace or punctuation, surround them with quotes:

```
SELECT $$the lexeme '    ' contains spaces$$::tsvector;
                 tsvector
-------------------------------------------
 '    ' 'contains' 'lexeme' 'spaces' 'the'
```

     (We use dollar-quoted string literals in this example and the next one
     to avoid the confusion of having to double quote marks within the
     literals.)  Embedded quotes and backslashes must be doubled:

```
SELECT $$the lexeme 'Joe''s' contains a quote$$::tsvector;
                    tsvector
------------------------------------------------
 'Joe''s' 'a' 'contains' 'lexeme' 'quote' 'the'
```

     Optionally, integer positions
     can be attached to lexemes:

```
SELECT 'a:1 fat:2 cat:3 sat:4 on:5 a:6 mat:7 and:8 ate:9 a:10 fat:11 rat:12'::tsvector;
                                  tsvector
-------------------------------------------------------------------&zwsp;------------
 'a':1,6,10 'and':8 'ate':9 'cat':3 'fat':2,11 'mat':7 'on':5 'rat':12 'sat':4
```

     A position normally indicates the source word's location in the
     document.  Positional information can be used for
     proximity ranking.  Position values can
     range from 1 to 16383; larger numbers are silently set to 16383.
     Duplicate positions for the same lexeme are discarded.
    

    

     Lexemes that have positions can further be labeled with a
     weight, which can be A,
     B, C, or D.
     D is the default and hence is not shown on output:

```
SELECT 'a:1A fat:2B,4C cat:5D'::tsvector;
          tsvector
----------------------------
 'a':1A 'cat':5 'fat':2B,4C
```

     Weights are typically used to reflect document structure, for example
     by marking title words differently from body words.  Text search
     ranking functions can assign different priorities to the different
     weight markers.
    

    

     It is important to understand that the
     tsvector type itself does not perform any word
     normalization; it assumes the words it is given are normalized
     appropriately for the application.  For example,

```
SELECT 'The Fat Rats'::tsvector;
      tsvector
--------------------
 'Fat' 'Rats' 'The'
```

     For most English-text-searching applications the above words would
     be considered non-normalized, but tsvector doesn't care.
     Raw document text should usually be passed through
     to_tsvector to normalize the words appropriately
     for searching:

```
SELECT to_tsvector('english', 'The Fat Rats');
   to_tsvector
-----------------
 'fat':2 'rat':3
```

     Again, see  for more detail.
    

   

   

    
# tsquery

    
     tsquery (data type)
    

    

     A tsquery value stores lexemes that are to be
     searched for, and can combine them using the Boolean operators
     & (AND), | (OR), and
     ! (NOT), as well as the phrase search operator
     <-> (FOLLOWED BY).  There is also a variant
     <N> of the FOLLOWED BY
     operator, where N is an integer constant that
     specifies the distance between the two lexemes being searched
     for.  <-> is equivalent to <1>.
    

    

     Parentheses can be used to enforce grouping of these operators.
     In the absence of parentheses, ! (NOT) binds most tightly,
     <-> (FOLLOWED BY) next most tightly, then
     & (AND), with | (OR) binding
     the least tightly.
    

    

     Here are some examples:

```
SELECT 'fat & rat'::tsquery;
    tsquery
---------------
 'fat' & 'rat'

SELECT 'fat & (rat | cat)'::tsquery;
          tsquery
---------------------------
 'fat' & ( 'rat' | 'cat' )

SELECT 'fat & rat & ! cat'::tsquery;
        tsquery
------------------------
 'fat' & 'rat' & !'cat'
```

    

    

     Optionally, lexemes in a tsquery can be labeled with
     one or more weight letters, which restricts them to match only
     tsvector lexemes with one of those weights:

```
SELECT 'fat:ab & cat'::tsquery;
    tsquery
------------------
 'fat':AB & 'cat'
```

    

    

     Also, lexemes in a tsquery can be labeled with *
     to specify prefix matching:

```
SELECT 'super:*'::tsquery;
  tsquery
-----------
 'super':*
```

     This query will match any word in a tsvector that begins
     with super.
    

    

     Quoting rules for lexemes are the same as described previously for
     lexemes in tsvector; and, as with tsvector,
     any required normalization of words must be done before converting
     to the tsquery type.  The to_tsquery
     function is convenient for performing such normalization:

```
SELECT to_tsquery('Fat:ab & Cats');
    to_tsquery
------------------
 'fat':AB & 'cat'
```

     Note that to_tsquery will process prefixes in the same way
     as other words, which means this comparison returns true:

```
SELECT to_tsvector( 'postgraduate' ) @@ to_tsquery( 'postgres:*' );
 ?column?
----------
 t
```

     because postgres gets stemmed to postgr:

```
SELECT to_tsvector( 'postgraduate' ), to_tsquery( 'postgres:*' );
  to_tsvector  | to_tsquery
---------------+------------
 'postgradu':1 | 'postgr':*
```

     which will match the stemmed form of postgraduate.
    

   

  

  

   
# UUID Type

   
    UUID
   

   

    The data type uuid stores Universally Unique Identifiers
    (UUID) as defined by RFC 9562,
    ISO/IEC 9834-8:2005, and related standards.
    (Some systems refer to this data type as a globally unique identifier, or
    GUID,GUID instead.)  This
    identifier is a 128-bit quantity that is generated by an algorithm chosen
    to make it very unlikely that the same identifier will be generated by
    anyone else in the known universe using the same algorithm.  Therefore,
    for distributed systems, these identifiers provide a better uniqueness
    guarantee than sequence generators, which
    are only unique within a single database.
   

   

    RFC 9562 defines 8 different UUID versions.  Each version has specific requirements
    for generating new UUID values, and each version provides distinct benefits and drawbacks.
    PostgreSQL provides native support for generating UUIDs
    using the UUIDv4 and UUIDv7 algorithms.  Alternatively, UUID values can be generated
    outside of the database using any algorithm.  The data type uuid can be used
    to store any UUID, regardless of the origin and the UUID version.
    UUIDs are compared lexicographically on their 128-bit value.  For UUIDv7 values,
    which embed a Unix timestamp in the most significant bits, this ordering corresponds
    to chronological order, making them suitable for use with aggregate functions such as
    min() and max().
   

   

    A UUID is written as a sequence of lower-case hexadecimal digits,
    in several groups separated by hyphens, specifically a group of 8
    digits followed by three groups of 4 digits followed by a group of
    12 digits, for a total of 32 digits representing the 128 bits.  An
    example of a UUID in this standard form is:

```
a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11
```

    PostgreSQL also accepts the following
    alternative forms for input:
    use of upper-case digits, the standard format surrounded by
    braces, omitting some or all hyphens, adding a hyphen after any
    group of four digits.  Examples are:

```
A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11
{a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11}
a0eebc999c0b4ef8bb6d6bb9bd380a11
a0ee-bc99-9c0b-4ef8-bb6d-6bb9-bd38-0a11
{a0eebc99-9c0b4ef8-bb6d6bb9-bd380a11}
```

    Output is always in the standard form.
   

   

    It is possible to cast uuid values to and from type
    bytea. This is useful for using functions such as
    encode() and decode()
    with UUID values. For example:

```
encode('1ea3d64c-bc40-4cc3-84bb-6b11ee31e5c2'::uuid::bytea, 'base64')
decode('HqPWTLxATMOEu2sR7jHlwg==', 'base64')::uuid
```

   

   

    See  for how to generate a UUID in
    PostgreSQL.
   

  

  

   
# XML Type

   
    XML
   

   

    The xml data type can be used to store XML data.  Its
    advantage over storing XML data in a text field is that it
    checks the input values for well-formedness, and there are support
    functions to perform type-safe operations on it; see .  Use of this data type requires the
    installation to have been built with configure
    --with-libxml.
   

   

    The xml type can store well-formed
    documents, as defined by the XML standard, as well
    as content fragments, which are defined by reference
    to the more permissive
    document node
    of the XQuery and XPath data model.
    Roughly, this means that content fragments can have
    more than one top-level element or character node.  The expression
    xmlvalue IS DOCUMENT
    can be used to evaluate whether a particular xml
    value is a full document or only a content fragment.
   

   

    Limits and compatibility notes for the xml data type
    can be found in .
   

   

    
# Creating XML Values

   

    To produce a value of type xml from character data,
    use the function
    xmlparse:xmlparse

XMLPARSE ( { DOCUMENT | CONTENT } value)

    Examples:

```

# Manual

...
')
XMLPARSE (CONTENT 'abcbarfoo')
]]>
```

    While this is the only way to convert character strings into XML
    values according to the SQL standard, the PostgreSQL-specific
    syntaxes:

```
bar'
'bar'::xml
]]>
```

    can also be used.
   

   

    The xml type does not validate input values
    against a document type declaration
    (DTD),DTD
    even when the input value specifies a DTD.
    There is also currently no built-in support for validating against
    other XML schema languages such as XML Schema.
   

   

    The inverse operation, producing a character string value from
    xml, uses the function
    xmlserialize:xmlserialize

XMLSERIALIZE ( { DOCUMENT | CONTENT } value AS type [ [ NO ] INDENT ] )

    type can be
    character, character varying, or
    text (or an alias for one of those).  Again, according
    to the SQL standard, this is the only way to convert between type
    xml and character types, but PostgreSQL also allows
    you to simply cast the value.
   

   

    The INDENT option causes the result to be
    pretty-printed, while NO INDENT (which is the
    default) just emits the original input string.  Casting to a character
    type likewise produces the original string.
   

   

    When a character string value is cast to or from type
    xml without going through XMLPARSE or
    XMLSERIALIZE, respectively, the choice of
    DOCUMENT versus CONTENT is
    determined by the XML option
    XML option
    session configuration parameter, which can be set using the
    standard command:

SET XML OPTION { DOCUMENT | CONTENT };

    or the more PostgreSQL-like syntax

SET xmloption TO { DOCUMENT | CONTENT };

    The default is CONTENT, so all forms of XML
    data are allowed.
   

   

   

    
# Encoding Handling

   

    Care must be taken when dealing with multiple character encodings
    on the client, server, and in the XML data passed through them.
    When using the text mode to pass queries to the server and query
    results to the client (which is the normal mode), PostgreSQL
    converts all character data passed between the client and the
    server and vice versa to the character encoding of the respective
    end; see .  This includes string
    representations of XML values, such as in the above examples.
    This would ordinarily mean that encoding declarations contained in
    XML data can become invalid as the character data is converted
    to other encodings while traveling between client and server,
    because the embedded encoding declaration is not changed.  To cope
    with this behavior, encoding declarations contained in
    character strings presented for input to the xml type
    are ignored, and content is assumed
    to be in the current server encoding.  Consequently, for correct
    processing, character strings of XML data must be sent
    from the client in the current client encoding.  It is the
    responsibility of the client to either convert documents to the
    current client encoding before sending them to the server, or to
    adjust the client encoding appropriately.  On output, values of
    type xml will not have an encoding declaration, and
    clients should assume all data is in the current client
    encoding.
   

   

    When using binary mode to pass query parameters to the server
    and query results back to the client, no encoding conversion
    is performed, so the situation is different.  In this case, an
    encoding declaration in the XML data will be observed, and if it
    is absent, the data will be assumed to be in UTF-8 (as required by
    the XML standard; note that PostgreSQL does not support UTF-16).
    On output, data will have an encoding declaration
    specifying the client encoding, unless the client encoding is
    UTF-8, in which case it will be omitted.
   

   

    Needless to say, processing XML data with PostgreSQL will be less
    error-prone and more efficient if the XML data encoding, client encoding,
    and server encoding are the same.  Since XML data is internally
    processed in UTF-8, computations will be most efficient if the
    server encoding is also UTF-8.
   

   
    

     Some XML-related functions may not work at all on non-ASCII data
     when the server encoding is not UTF-8.  This is known to be an
     issue for xmltable() and xpath() in particular.
    

   
   

   

   
# Accessing XML Values

   

    The xml data type is unusual in that it does not
    provide any comparison operators.  This is because there is no
    well-defined and universally useful comparison algorithm for XML
    data.  One consequence of this is that you cannot retrieve rows by
    comparing an xml column against a search value.  XML
    values should therefore typically be accompanied by a separate key
    field such as an ID.  An alternative solution for comparing XML
    values is to convert them to character strings first, but note
    that character string comparison has little to do with a useful
    XML comparison method.
   

   

    Since there are no comparison operators for the xml
    data type, it is not possible to create an index directly on a
    column of this type.  If speedy searches in XML data are desired,
    possible workarounds include casting the expression to a
    character string type and indexing that, or indexing an XPath
    expression.  Of course, the actual query would have to be adjusted
    to search by the indexed expression.
   

   

    The text-search functionality in PostgreSQL can also be used to speed
    up full-document searches of XML data.  The necessary
    preprocessing support is, however, not yet available in the PostgreSQL
    distribution.
   

   

  

  &json;

  &array;

  &rowtypes;

  &rangetypes;

  

   
# Domain Types

   
    domain
   

   
    data type
    domain
   

   

    A domain is a user-defined data type that is
    based on another underlying type.  Optionally,
    it can have constraints that restrict its valid values to a subset of
    what the underlying type would allow.  Otherwise it behaves like the
    underlying type &mdash; for example, any operator or function that
    can be applied to the underlying type will work on the domain type.
    The underlying type can be any built-in or user-defined base type,
    enum type, array type, composite type, range type, or another domain.
   

   

    For example, we could create a domain over integers that accepts only
    positive integers:

```
CREATE DOMAIN posint AS integer CHECK (VALUE > 0);
CREATE TABLE mytable (id posint);
INSERT INTO mytable VALUES(1);   -- works
INSERT INTO mytable VALUES(-1);  -- fails
```

   

   

    When an operator or function of the underlying type is applied to a
    domain value, the domain is automatically down-cast to the underlying
    type.  Thus, for example, the result of mytable.id - 1
    is considered to be of type integer not posint.
    We could write (mytable.id - 1)::posint to cast the
    result back to posint, causing the domain's constraints
    to be rechecked.  In this case, that would result in an error if the
    expression had been applied to an id value of
    1.  Assigning a value of the underlying type to a field or variable of
    the domain type is allowed without writing an explicit cast, but the
    domain's constraints will be checked.
   

   

    For additional information see .
   

  

  

   
# Object Identifier Types

   
    object identifier
    data type
   

   
    oid
   

   
    oid8
   

   
    regclass
   

   
    regcollation
   

   
    regconfig
   

   
    regdatabase
   

   
    regdictionary
   

   
    regnamespace
   

   
    regoper
   

   
    regoperator
   

   
    regproc
   

   
    regprocedure
   

   
    regrole
   

   
    regtype
   

   
    xid8
   

   
    cid
   

   
    tid
   

   
    xid
   

   

    Object identifiers (OIDs) are used internally by
    PostgreSQL as primary keys for various
    system tables.
    Type oid represents an object identifier.  There are also
    several alias types for oid, each
    named regsomething.
     shows an
    overview.
   

   

    The oid type is currently implemented as an unsigned
    four-byte integer.  Therefore, it is not large enough to provide
    database-wide uniqueness in large databases, or even in large
    individual tables.
   

   

    In some contexts, a 64-bit variant oid8 can be used.
    It is implemented as an unsigned eight-byte integer. Unlike its
    oid counterpart, it can ensure uniqueness in large
    individual tables.
   

   

    The oid and oid8 types themselves have
    few operations beyond comparison.
    They can be cast to integer, however, and then manipulated using the
    standard integer operators.  (Beware of possible
    signed-versus-unsigned confusion if you do this.)
   

   

    The OID alias types have no operations of their own except
    for specialized input and output routines.  These routines are able
    to accept and display symbolic names for system objects, rather than
    the raw numeric value that type oid would use.  The alias
    types allow simplified lookup of OID values for objects.  For example,
    to examine the pg_attribute rows related to a table
    mytable, one could write:

```
SELECT * FROM pg_attribute WHERE attrelid = 'mytable'::regclass;
```

    rather than:

```
SELECT * FROM pg_attribute
  WHERE attrelid = (SELECT oid FROM pg_class WHERE relname = 'mytable');
```

    While that doesn't look all that bad by itself, it's still oversimplified.
    A far more complicated sub-select would be needed to
    select the right OID if there are multiple tables named
    mytable in different schemas.
    The regclass input converter handles the table lookup according
    to the schema path setting, and so it does the right thing
    automatically.  Similarly, casting a table's OID to
    regclass is handy for symbolic display of a numeric OID.
   

    
     
# Object Identifier Types

     
      
       
        Name
        References
        Description
        Value Example
       
      

      

       
        oid
        any
        numeric object identifier
        564182
       

       
        regclass
        pg_class
        relation name
        pg_type
       

       
        regcollation
        pg_collation
        collation name
        "POSIX"
       

       
        regconfig
        pg_ts_config
        text search configuration
        english
       

       
        regdatabase
        pg_database
        database name
        template1
       

       
        regdictionary
        pg_ts_dict
        text search dictionary
        simple
       

       
        regnamespace
        pg_namespace
        namespace name
        pg_catalog
       

       
        regoper
        pg_operator
        operator name
        +
       

       
        regoperator
        pg_operator
        operator with argument types
        *(integer,&zwsp;integer)
         or -(NONE,&zwsp;integer)
       

       
        regproc
        pg_proc
        function name
        sum
       

       
        regprocedure
        pg_proc
        function with argument types
        sum(int4)
       

       
        regrole
        pg_authid
        role name
        smithee
       

       
        regtype
        pg_type
        data type name
        integer
       
      
     
    

   

    All of the OID alias types for objects that are grouped by namespace
    accept schema-qualified names, and will
    display schema-qualified names on output if the object would not
    be found in the current search path without being qualified.
    For example, myschema.mytable is acceptable input
    for regclass (if there is such a table).  That value
    might be output as myschema.mytable, or
    just mytable, depending on the current search path.
    The regproc and regoper alias types will only
    accept input names that are unique (not overloaded), so they are
    of limited use; for most uses regprocedure or
    regoperator are more appropriate.  For regoperator,
    unary operators are identified by writing NONE for the unused
    operand.
   

   

    The input functions for these types allow whitespace between tokens,
    and will fold upper-case letters to lower case, except within double
    quotes; this is done to make the syntax rules similar to the way
    object names are written in SQL.  Conversely, the output functions
    will use double quotes if needed to make the output be a valid SQL
    identifier.  For example, the OID of a function
    named Foo (with upper case F)
    taking two integer arguments could be entered as
    ' "Foo" ( int, integer ) '::regprocedure.  The
    output would look like "Foo"(integer,integer).
    Both the function name and the argument type names could be
    schema-qualified, too.
   

   

    Many built-in PostgreSQL functions accept
    the OID of a table, or another kind of database object, and for
    convenience are declared as taking regclass (or the
    appropriate OID alias type).  This means you do not have to look up
    the object's OID by hand, but can just enter its name as a string
    literal.  For example, the nextval(regclass) function
    takes a sequence relation's OID, so you could call it like this:

```
nextval('foo')              operates on sequence foo
nextval('FOO')              same as above
nextval('"Foo"')            operates on sequence Foo
nextval('myschema.foo')     operates on myschema.foo
nextval('"myschema".foo')   same as above
nextval('foo')              searches search path for foo
```

   

   
    

     When you write the argument of such a function as an unadorned
     literal string, it becomes a constant of type regclass
     (or the appropriate type).
     Since this is really just an OID, it will track the originally
     identified object despite later renaming, schema reassignment,
     etc.  This early binding behavior is usually desirable for
     object references in column defaults and views.  But sometimes you might
     want late binding where the object reference is resolved
     at run time.  To get late-binding behavior, force the constant to be
     stored as a text constant instead of regclass:

```
nextval('foo'::text)      foo is looked up at runtime
```

     The to_regclass() function and its siblings
     can also be used to perform run-time lookups.  See
     .
    

   

   

    Another practical example of use of regclass
    is to look up the OID of a table listed in
    the information_schema views, which don't supply
    such OIDs directly.  One might for example wish to call
    the pg_relation_size() function, which requires
    the table OID.  Taking the above rules into account, the correct way
    to do that is

```
SELECT table_schema, table_name,
       pg_relation_size((quote_ident(table_schema) || '.' ||
                         quote_ident(table_name))::regclass)
FROM information_schema.tables
WHERE ...
```

    The quote_ident() function will take care of
    double-quoting the identifiers where needed.  The seemingly easier

```
SELECT pg_relation_size(table_name)
FROM information_schema.tables
WHERE ...
```

    is not recommended, because it will fail for
    tables that are outside your search path or have names that require
    quoting.
   

   

    An additional property of most of the OID alias types is the creation of
    dependencies.  If a
    constant of one of these types appears in a stored expression
    (such as a column default expression or view), it creates a dependency
    on the referenced object.  For example, if a column has a default
    expression nextval('my_seq'::regclass),
    PostgreSQL
    understands that the default expression depends on the sequence
    my_seq, so the system will not let the sequence
    be dropped without first removing the default expression.  The
    alternative of nextval('my_seq'::text) does not
    create a dependency.
    (regdatabase and regrole are exceptions to this
    property.  Constants of these types are not allowed in stored expressions.)
   

   

    Another identifier type used by the system is xid, or transaction
    (abbreviated xact) identifier.  This is the data type of the system columns
    xmin and xmax.  Transaction identifiers are 32-bit quantities.
    In some contexts, a 64-bit variant xid8 is used.  Unlike
    xid values, xid8 values increase strictly
    monotonically and cannot be reused in the lifetime of a database
    cluster.  See  for more details.
   

   

    A third identifier type used by the system is cid, or
    command identifier.  This is the data type of the system columns
    cmin and cmax. Command identifiers are also 32-bit quantities.
   

   

    A final identifier type used by the system is tid, or tuple
    identifier (row identifier).  This is the data type of the system column
    ctid.  A tuple ID is a pair
    (block number, tuple index within block) that identifies the
    physical location of the row within its table.
   

   

    (The system columns are further explained in .)
   

  

  

   
# pg_lsn Type

   
    pg_lsn
   

   

    The pg_lsn data type can be used to store LSN (Log Sequence
    Number) data which is a pointer to a location in the WAL. This type is a
    representation of XLogRecPtr and an internal system type of
    PostgreSQL.
   

   

    Internally, an LSN is a 64-bit integer, representing a byte position in
    the write-ahead log stream.  It is printed as two hexadecimal numbers of
    up to 8 digits each, separated by a slash; for example,
    16/B374D848.  The pg_lsn type supports the
    standard comparison operators, like = and
    >.  Two LSNs can be subtracted using the
    - operator; the result is the number of bytes separating
    those write-ahead log locations.  Also the number of bytes can be
    added into and subtracted from LSN using the
    +(pg_lsn,numeric) and
    -(pg_lsn,numeric) operators, respectively. Note that
    the calculated LSN should be in the range of pg_lsn type,
    i.e., between 0/00000000 and
    FFFFFFFF/FFFFFFFF.
   

  

  

   
# Pseudo-Types

   
    record
   

   
    any
   

   
    anyelement
   

   
    anyarray
   

   
    anynonarray
   

   
    anyenum
   

   
    anyrange
   

   
    anymultirange
   

   
    anycompatible
   

   
    anycompatiblearray
   

   
    anycompatiblenonarray
   

   
    anycompatiblerange
   

   
    anycompatiblemultirange
   

   
    void
   

   
    trigger
   

   
    event_trigger
   

   
    pg_ddl_command
   

   
    language_handler
   

   
    fdw_handler
   

   
    table_am_handler
   

   
    index_am_handler
   

   
    tsm_handler
   

   
    cstring
   

   
    internal
   

   
    unknown
   

   

    The PostgreSQL type system contains a
    number of special-purpose entries that are collectively called
    pseudo-types.  A pseudo-type cannot be used as a
    column data type, but it can be used to declare a function's
    argument or result type.  Each of the available pseudo-types is
    useful in situations where a function's behavior does not
    correspond to simply taking or returning a value of a specific
    SQL data type.   lists the existing
    pseudo-types.
   

    
     
# Pseudo-Types

     
      
      
      
       
        Name
        Description
       
      

      
       
        any
        Indicates that a function accepts any input data type.
       

       
        anyelement
        Indicates that a function accepts any data type
        (see ).
       

       
        anyarray
        Indicates that a function accepts any array data type
        (see ).
       

       
        anynonarray
        Indicates that a function accepts any non-array data type
        (see ).
       

       
        anyenum
        Indicates that a function accepts any enum data type
        (see  and
        ).
       

       
        anyrange
        Indicates that a function accepts any range data type
        (see  and
        ).
       

       
        anymultirange
        Indicates that a function accepts any multirange data type
        (see  and
        ).
       

       
        anycompatible
        Indicates that a function accepts any data type,
        with automatic promotion of multiple arguments to a common data type
        (see ).
       

       
        anycompatiblearray
        Indicates that a function accepts any array data type,
        with automatic promotion of multiple arguments to a common data type
        (see ).
       

       
        anycompatiblenonarray
        Indicates that a function accepts any non-array data type,
        with automatic promotion of multiple arguments to a common data type
        (see ).
       

       
        anycompatiblerange
        Indicates that a function accepts any range data type,
        with automatic promotion of multiple arguments to a common data type
        (see  and
        ).
       

       
        anycompatiblemultirange
        Indicates that a function accepts any multirange data type,
        with automatic promotion of multiple arguments to a common data type
        (see  and
        ).
       

       
        cstring
        Indicates that a function accepts or returns a null-terminated C string.
       

       
        internal
        Indicates that a function accepts or returns a server-internal
        data type.
       

       
        language_handler
        A procedural language call handler is declared to return language_handler.
       

       
        fdw_handler
        A foreign-data wrapper handler is declared to return fdw_handler.
       

       
        table_am_handler
        A table access method handler is declared to return table_am_handler.
       

       
        index_am_handler
        An index access method handler is declared to return index_am_handler.
       

       
        tsm_handler
        A tablesample method handler is declared to return tsm_handler.
       

       
        record
        Identifies a function taking or returning an unspecified row type.
       

       
        trigger
        A trigger function is declared to return trigger.
       

       
        event_trigger
        An event trigger function is declared to return event_trigger.
       

       
        pg_ddl_command
        Identifies a representation of DDL commands that is available to event triggers.
       

       
        void
        Indicates that a function returns no value.
       

       
        unknown
        Identifies a not-yet-resolved type, e.g., of an undecorated
         string literal.
       
      
     
    

   

    Functions coded in C (whether built-in or dynamically loaded) can be
    declared to accept or return any of these pseudo-types.  It is up to
    the function author to ensure that the function will behave safely
    when a pseudo-type is used as an argument type.
   

   

    Functions coded in procedural languages can use pseudo-types only as
    allowed by their implementation languages.  At present most procedural
    languages forbid use of a pseudo-type as an argument type, and allow
    only void and record as a result type (plus
    trigger or event_trigger when the function is used
    as a trigger or event trigger).  Some also support polymorphic functions
    using the polymorphic pseudo-types, which are shown above and discussed
    in detail in .
   

   

    The internal pseudo-type is used to declare functions
    that are meant only to be called internally by the database
    system, and not by direct invocation in an SQL
    query.  If a function has at least one internal-type
    argument then it cannot be called from SQL.  To
    preserve the type safety of this restriction it is important to
    follow this coding rule: do not create any function that is
    declared to return internal unless it has at least one
    internal argument.
