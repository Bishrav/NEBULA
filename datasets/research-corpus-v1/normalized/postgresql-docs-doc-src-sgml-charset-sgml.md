# Localization

 

  This chapter describes the available localization features from the
  point of view of the administrator.
  PostgreSQL supports two localization
  facilities:

   

    

     

      Using the locale features of the operating system to provide
      locale-specific collation order, number formatting, translated
      messages, and other aspects.
      This is covered in  and
      .
     

    

    

     

      Providing a number of different character sets to support storing text
      in all kinds of languages, and providing character set translation
      between client and server.
      This is covered in .
     

    

   

  

 

  
# Locale Support

  locale

  

   Locale support refers to an application respecting
   cultural preferences regarding alphabets, sorting, number
   formatting, etc.  PostgreSQL uses the standard ISO
   C and POSIX locale facilities provided by the server operating
   system.  For additional information refer to the documentation of your
   system.
  

  

   
# Overview

   

    Locale support is automatically initialized when a database
    cluster is created using initdb.
    initdb will initialize the database cluster
    with the locale setting of its execution environment by default,
    so if your system is already set to use the locale that you want
    in your database cluster then there is nothing else you need to
    do.  If you want to use a different locale (or you are not sure
    which locale your system is set to), you can instruct
    initdb exactly which locale to use by
    specifying the --locale option. For example:

```
initdb --locale=sv_SE
```

   

   

    This example for Unix systems sets the locale to Swedish
    (sv) as spoken
    in Sweden (SE).  Other possibilities might include
    en_US (U.S. English) and fr_CA (French
    Canadian).  If more than one character set can be used for a
    locale then the specifications can take the form
    language_territory.codeset.  For example,
    fr_BE.UTF-8 represents the French language (fr) as
    spoken in Belgium (BE), with a UTF-8 character set
    encoding.
   

   

    What locales are available on your
    system under what names depends on what was provided by the operating
    system vendor and what was installed.  On most Unix systems, the command
    locale -a will provide a list of available locales.
    Windows uses more verbose locale names, such as German_Germany
    or Swedish_Sweden.1252, but the principles are the same.
   

   

    Occasionally it is useful to mix rules from several locales, e.g.,
    use English collation rules but Spanish messages.  To support that, a
    set of locale subcategories exist that control only certain
    aspects of the localization rules:

    
     
      
      
      
       
        LC_COLLATE
        String sort order (ignored unless the provider is libc)
       
       
        LC_CTYPE
        Character classification (What is a letter? Its upper-case equivalent?)
       
       
        LC_MESSAGES
        Language of messages
       
       
        LC_MONETARY
        Formatting of currency amounts
       
       
        LC_NUMERIC
        Formatting of numbers
       
       
        LC_TIME
        Formatting of dates and times
       
      
     
    

    The category names translate into names of
    initdb options to override the locale choice
    for a specific category.  For instance, to set the locale to
    French Canadian, but use U.S. rules for formatting currency, use
    initdb --locale=fr_CA --lc-monetary=en_US.
   

   

    If you want the system to behave as if it had no locale support,
    use the special locale name C, or equivalently
    POSIX.
   

   

    Some locale categories must have their values
    fixed when the database is created.  You can use different settings
    for different databases, but once a database is created, you cannot
    change them for that database anymore. LC_COLLATE
    and LC_CTYPE are these categories.  They affect
    the sort order of indexes, so they must be kept fixed, or indexes on
    text columns would become corrupt.
    (But you can alleviate this restriction using collations, as discussed
    in .)
    The default values for these
    categories are determined when initdb is run, and
    those values are used when new databases are created, unless
    specified otherwise in the CREATE DATABASE command.
   

   

    The other locale categories can be changed whenever desired
    by setting the server configuration parameters
    that have the same name as the locale categories (see  for details).  The values
    that are chosen by initdb are actually only written
    into the configuration file postgresql.conf to
    serve as defaults when the server is started.  If you remove these
    assignments from postgresql.conf then the
    server will inherit the settings from its execution environment.
   

   

    Note that the locale behavior of the server is determined by the
    environment variables seen by the server, not by the environment
    of any client.  Therefore, be careful to configure the correct locale settings
    before starting the server.  A consequence of this is that if
    client and server are set up in different locales, messages might
    appear in different languages depending on where they originated.
   

   
    

     When we speak of inheriting the locale from the execution
     environment, this means the following on most operating systems:
     For a given locale category, say the collation, the following
     environment variables are consulted in this order until one is
     found to be set: LC_ALL, LC_COLLATE
     (or the variable corresponding to the respective category),
     LANG.  If none of these environment variables are
     set then the locale defaults to C.
    

    

     Some message localization libraries also look at the environment
     variable LANGUAGE which overrides all other locale
     settings for the purpose of setting the language of messages.  If
     in doubt, please refer to the documentation of your operating
     system, in particular the documentation about
     gettext.
    

   

   

    To enable messages to be translated to the user's preferred language,
    NLS must have been selected at build time
    (configure --enable-nls).  All other locale support is
    built in automatically.
   

  

  

   
# Behavior

   

    The locale settings influence the following SQL features:

    

     

      

       Sort order in queries using ORDER BY or the standard
       comparison operators on textual data
       ORDER BYand locales
      

     

     

      

       The upper, lower, and initcap
       functions
       upperand locales
       lowerand locales
      

     

     

      

       Pattern matching operators (LIKE, SIMILAR TO,
       and POSIX-style regular expressions); locales affect both case
       insensitive matching and the classification of characters by
       character-class regular expressions
       LIKEand locales
       regular expressionsand locales
      

     

     

      

       The to_char family of functions
       to_charand locales
      

     

     

      

       The ability to use indexes with LIKE clauses
      

     

    

   

   

    The drawback of using locales other than C or
    POSIX in PostgreSQL is its performance
    impact. It slows character handling and prevents ordinary indexes
    from being used by LIKE. For this reason use locales
    only if you actually need them.
   

   

    As a workaround to allow PostgreSQL to use indexes
    with LIKE clauses under a non-C locale, several custom
    operator classes exist. These allow the creation of an index that
    performs a strict character-by-character comparison, ignoring
    locale comparison rules. Refer to 
    for more information.  Another approach is to create indexes using
    the C collation, as discussed in
    .
   

  

  

   
# Selecting Locales

   

    Locales can be selected in different scopes depending on requirements.
    The above overview showed how locales are specified using
    initdb to set the defaults for the entire cluster.  The
    following list shows where locales can be selected.  Each item provides
    the defaults for the subsequent items, and each lower item allows
    overriding the defaults on a finer granularity.
   

   
    

     

      As explained above, the environment of the operating system provides the
      defaults for the locales of a newly initialized database cluster.  In
      many cases, this is enough: if the operating system is configured for
      the desired language/territory, by default
      PostgreSQL will also behave according
      to that locale.
     

    

    

     

      As shown above, command-line options for initdb
      specify the locale settings for a newly initialized database cluster.
      Use this if the operating system does not have the locale configuration
      you want for your database system.
     

    

    

     

      A locale can be selected separately for each database.  The SQL command
      CREATE DATABASE and its command-line equivalent
      createdb have options for that.  Use this for example
      if a database cluster houses databases for multiple tenants with
      different requirements.
     

    

    

     

      Locale settings can be made for individual table columns.  This uses an
      SQL object called collation and is explained in
      .  Use this for example to sort data in
      different languages or customize the sort order of a particular table.
     

    

    

     

      Finally, locales can be selected for an individual query.  Again, this
      uses SQL collation objects.  This could be used to change the sort order
      based on run-time choices or for ad-hoc experimentation.
     

    

   
  

  

   
# Locale Providers

   

    A locale provider specifies which library defines the locale behavior for
    collations and character classifications.
   

   

    The commands and tools that select the locale settings, as described
    above, each have an option to select the locale provider. Here is an
    example to initialize a database cluster using the ICU provider:

```
initdb --locale-provider=icu --icu-locale=en
```

    See the description of the respective commands and programs for
    details.  Note that you can mix locale providers at different
    granularities, for example use libc by default for the
    cluster but have one database that uses the icu
    provider, and then have collation objects using either provider within
    those databases.
   

   

    Regardless of the locale provider, the operating system is still used to
    provide some locale-aware behavior, such as messages (see ).
   

   

    The available locale providers are listed below:
   

   

    

     
builtin

     

      

       The builtin provider uses built-in operations. Only
       the C, C.UTF-8, and
       PG_UNICODE_FAST locales are supported for this
       provider.
      

      

       The C locale behavior is identical to the
       C locale in the libc provider. When using this
       locale, the behavior may depend on the database encoding.
      

      

       The C.UTF-8 locale is available only for when the
       database encoding is UTF-8, and the behavior is
       based on Unicode. The collation uses the code point values only. The
       regular expression character classes are based on the "POSIX
       Compatible" semantics, and the case mapping is the "simple" variant.
      

      

       The PG_UNICODE_FAST locale is available only when
       the database encoding is UTF-8, and the behavior is
       based on Unicode. The collation uses the code point values only. The
       regular expression character classes are based on the "Standard"
       semantics, and the case mapping is the "full" variant.
      

     

    

    

     
icu

     

      

       The icu provider uses the external
       ICUICU
       library. PostgreSQL must have been
       configured with support.
      

      

       ICU provides collation and character classification behavior that is
       independent of the operating system and database encoding, which is
       preferable if you expect to transition to other platforms without any
       change in results. LC_COLLATE and
       LC_CTYPE can be set independently of the ICU
       locale.
      

      
       

        For the ICU provider, results may depend on the version of the ICU
        library used, as it is updated to reflect changes in natural language
        over time.
       

      
     

    

    

     
libc

     

      

       The libc provider uses the operating system's C
       library. The collation and character classification behavior is
       controlled by the settings LC_COLLATE and
       LC_CTYPE, so they cannot be set independently.
      

      
       

        The same locale name may have different behavior on different
        platforms when using the libc provider.
       

      
     

    

   

  

  

   
# ICU Locales

   

    
# ICU Locale Names

    

     The ICU format for the locale name is a Language Tag.

```
CREATE COLLATION mycollation1 (provider = icu, locale = 'ja-JP');
CREATE COLLATION mycollation2 (provider = icu, locale = 'fr');
```

    

   

   

    
# Locale Canonicalization and Validation

    

     When defining a new ICU collation object or database with ICU as the
     provider, the given locale name is transformed ("canonicalized") into a
     language tag if not already in that form. For instance,

```
CREATE COLLATION mycollation3 (provider = icu, locale = 'en-US-u-kn-true');
NOTICE:  using standard form "en-US-u-kn" for locale "en-US-u-kn-true"
CREATE COLLATION mycollation4 (provider = icu, locale = 'de_DE.utf8');
NOTICE:  using standard form "de-DE" for locale "de_DE.utf8"
```

     If you see this notice, ensure that the provider and
     locale are the expected result. For consistent results
     when using the ICU provider, specify the canonical language tag instead of relying on the
     transformation.
    

    

     A locale with no language name, or the special language name
     root, is transformed to have the language
     und ("undefined").
    

    

     ICU can transform most libc locale names, as well as some other formats,
     into language tags for easier transition to ICU. If a libc locale name is
     used in ICU, it may not have precisely the same behavior as in libc.
    

    

     If there is a problem interpreting the locale name, or if the locale name
     represents a language or region that ICU does not recognize, you will see
     the following warning:

```
CREATE COLLATION nonsense (provider = icu, locale = 'nonsense');
WARNING:  ICU locale "nonsense" has unknown language "nonsense"
HINT:  To disable ICU locale validation, set parameter icu_validation_level to DISABLED.
CREATE COLLATION
```

      controls how the message is
     reported. Unless set to ERROR, the collation will
     still be created, but the behavior may not be what the user intended.
    

   

   

    
# Language Tag

    

     A language tag, defined in BCP 47, is a standardized identifier used to
     identify languages, regions, and other information about a locale.
    

    

     Basic language tags are simply
     language-region;
     or even just language. The
     language is a language code
     (e.g. fr for French), and
     region is a region code
     (e.g. CA for Canada). Examples:
     ja-JP, de, or
     fr-CA.
    

    

     Collation settings may be included in the language tag to customize
     collation behavior. ICU allows extensive customization, such as
     sensitivity (or insensitivity) to accents, case, and punctuation;
     treatment of digits within text; and many other options to satisfy a
     variety of uses.
    

    

     To include this additional collation information in a language tag,
     append -u, which indicates there are additional
     collation settings, followed by one or more
     -key-value
     pairs. The key is the key for a collation setting and
     value is a valid value for that setting. For
     boolean settings, the -key
     may be specified without a corresponding
     -value, which implies a
     value of true.
    

    

     For example, the language tag en-US-u-kn-ks-level2
     means the locale with the English language in the US region, with
     collation settings kn set to true
     and ks set to level2. Those
     settings mean the collation will be case-insensitive and treat a sequence
     of digits as a single number:

```
CREATE COLLATION mycollation5 (provider = icu, deterministic = false, locale = 'en-US-u-kn-ks-level2');
SELECT 'aB' = 'Ab' COLLATE mycollation5 AS result;
 result
--------
 t
(1 row)

SELECT 'N-45' < 'N-123' COLLATE mycollation5 AS result;
 result
--------
 t
(1 row)
```

    

    

     See  for details and additional
     examples of using language tags with custom collation information for the
     locale.
    

   

  

  

   
# Problems

   

    If locale support doesn't work according to the explanation above,
    check that the locale support in your operating system is
    correctly configured.  To check what locales are installed on your
    system, you can use the command locale -a if
    your operating system provides it.
   

   

    Check that PostgreSQL is actually using the locale
    that you think it is.  The LC_COLLATE and LC_CTYPE
    settings are determined when a database is created, and cannot be
    changed except by creating a new database.  Other locale
    settings including LC_MESSAGES and LC_MONETARY
    are initially determined by the environment the server is started
    in, but can be changed on-the-fly.  You can check the active locale
    settings using the SHOW command.
   

   

    The directory src/test/locale in the source
    distribution contains a test suite for
    PostgreSQL's locale support.
   

   

    Client applications that handle server-side errors by parsing the
    text of the error message will obviously have problems when the
    server's messages are in a different language.  Authors of such
    applications are advised to make use of the error code scheme
    instead.
   

   

    Maintaining catalogs of message translations requires the on-going
    efforts of many volunteers that want to see
    PostgreSQL speak their preferred language well.
    If messages in your language are currently not available or not fully
    translated, your assistance would be appreciated.  If you want to
    help, refer to  or write to the developers'
    mailing list.
   

  

 

 

  
# Collation Support

  collation

  

   The collation feature allows specifying the sort order and character
   classification behavior of data per-column, or even per-operation.
   This alleviates the restriction that the
   LC_COLLATE and LC_CTYPE settings
   of a database cannot be changed after its creation.
  

  

   
# Concepts

   

    Conceptually, every expression of a collatable data type has a
    collation.  (The built-in collatable data types are
    text, varchar, and char.
    User-defined base types can also be marked collatable, and of course
    a domain over a
    collatable data type is collatable.)  If the
    expression is a column reference, the collation of the expression is the
    defined collation of the column.  If the expression is a constant, the
    collation is the default collation of the data type of the
    constant.  The collation of a more complex expression is derived
    from the collations of its inputs, as described below.
   

   

    The collation of an expression can be the default
    collation, which means the locale settings defined for the
    database.  It is also possible for an expression's collation to be
    indeterminate.  In such cases, ordering operations and other
    operations that need to know the collation will fail.
   

   

    When the database system has to perform an ordering or a character
    classification, it uses the collation of the input expression.  This
    happens, for example, with ORDER BY clauses
    and function or operator calls such as <.
    The collation to apply for an ORDER BY clause
    is simply the collation of the sort key.  The collation to apply for a
    function or operator call is derived from the arguments, as described
    below.  In addition to comparison operators, collations are taken into
    account by functions that convert between lower and upper case
    letters, such as lower, upper, and
    initcap; by pattern matching operators; and by
    to_char and related functions.
   

   

    For a function or operator call, the collation that is derived by
    examining the argument collations is used at run time for performing
    the specified operation.  If the result of the function or operator
    call is of a collatable data type, the collation is also used at parse
    time as the defined collation of the function or operator expression,
    in case there is a surrounding expression that requires knowledge of
    its collation.
   

   

    The collation derivation of an expression can be
    implicit or explicit.  This distinction affects how collations are
    combined when multiple different collations appear in an
    expression.  An explicit collation derivation occurs when a
    COLLATE clause is used; all other collation
    derivations are implicit.  When multiple collations need to be
    combined, for example in a function call, the following rules are
    used:

    
     

      

       If any input expression has an explicit collation derivation, then
       all explicitly derived collations among the input expressions must be
       the same, otherwise an error is raised.  If any explicitly
       derived collation is present, that is the result of the
       collation combination.
      

     

     

      

       Otherwise, all input expressions must have the same implicit
       collation derivation or the default collation.  If any non-default
       collation is present, that is the result of the collation combination.
       Otherwise, the result is the default collation.
      

     

     

      

       If there are conflicting non-default implicit collations among the
       input expressions, then the combination is deemed to have indeterminate
       collation.  This is not an error condition unless the particular
       function being invoked requires knowledge of the collation it should
       apply.  If it does, an error will be raised at run-time.
      

     

    

    For example, consider this table definition:

```
CREATE TABLE test1 (
    a text COLLATE "de_DE",
    b text COLLATE "es_ES",
    ...
);
```

    Then in

```
SELECT a < 'foo' FROM test1;
```

    the < comparison is performed according to
    de_DE rules, because the expression combines an
    implicitly derived collation with the default collation.  But in

```
SELECT a < ('foo' COLLATE "fr_FR") FROM test1;
```

    the comparison is performed using fr_FR rules,
    because the explicit collation derivation overrides the implicit one.
    Furthermore, given

```
SELECT a < b FROM test1;
```

    the parser cannot determine which collation to apply, since the
    a and b columns have conflicting
    implicit collations.  Since the < operator
    does need to know which collation to use, this will result in an
    error.  The error can be resolved by attaching an explicit collation
    specifier to either input expression, thus:

```
SELECT a < b COLLATE "de_DE" FROM test1;
```

    or equivalently

```
SELECT a COLLATE "de_DE" < b FROM test1;
```

    On the other hand, the structurally similar case

```
SELECT a || b FROM test1;
```

    does not result in an error, because the || operator
    does not care about collations: its result is the same regardless
    of the collation.
   

   

    The collation assigned to a function or operator's combined input
    expressions is also considered to apply to the function or operator's
    result, if the function or operator delivers a result of a collatable
    data type.  So, in

```
SELECT * FROM test1 ORDER BY a || 'foo';
```

    the ordering will be done according to de_DE rules.
    But this query:

```
SELECT * FROM test1 ORDER BY a || b;
```

    results in an error, because even though the || operator
    doesn't need to know a collation, the ORDER BY clause does.
    As before, the conflict can be resolved with an explicit collation
    specifier:

```
SELECT * FROM test1 ORDER BY a || b COLLATE "fr_FR";
```

   

  

  

   
# Managing Collations

   

    A collation is an SQL schema object that maps an SQL name to locales
    provided by libraries installed in the operating system.  A collation
    definition has a provider that specifies which
    library supplies the locale data.  One standard provider name
    is libc, which uses the locales provided by the
    operating system C library.  These are the locales used by most tools
    provided by the operating system.  Another provider
    is icu, which uses the external
    ICUICU library.  ICU locales can only be
    used if support for ICU was configured when PostgreSQL was built.
   

   

    A collation object provided by libc maps to a
    combination of LC_COLLATE and LC_CTYPE
    settings, as accepted by the setlocale() system library call.  (As
    the name would suggest, the main purpose of a collation is to set
    LC_COLLATE, which controls the sort order.  But
    it is rarely necessary in practice to have an
    LC_CTYPE setting that is different from
    LC_COLLATE, so it is more convenient to collect
    these under one concept than to create another infrastructure for
    setting LC_CTYPE per expression.)  Also,
    a libc collation
    is tied to a character set encoding (see ).
    The same collation name may exist for different encodings.
   

   

    A collation object provided by icu maps to a named
    collator provided by the ICU library.  ICU does not support
    separate collate and ctype settings, so
    they are always the same.  Also, ICU collations are independent of the
    encoding, so there is always only one ICU collation of a given name in
    a database.
   

   

    
# Standard Collations

   

    On all platforms, the following collations are supported:

    

     

      
unicode

      

       

        This SQL standard collation sorts using the Unicode Collation
        Algorithm with the Default Unicode Collation Element Table.  It is
        available in all encodings.  ICU support is required to use this
        collation, and behavior may change if PostgreSQL is built with a
        different version of ICU.  (This collation has the same behavior as
        the ICU root locale; see .)
       

      

     

     

      
ucs_basic

      

       

        This SQL standard collation sorts using the Unicode code point values
        rather than natural language order, and only the ASCII letters
        A through
        Z are treated as letters.  The
        behavior is efficient and stable across all versions.  Only available
        for encoding UTF8.  (This collation has the same
        behavior as the libc locale specification C in
        UTF8 encoding.)
       

      

     

     

      
pg_unicode_fast

      

       

        This collation sorts by Unicode code point values rather than natural
        language order.  For the functions lower,
        initcap, and upper it uses
        Unicode full case mapping. For pattern matching (including regular
        expressions), it uses the Standard variant of Unicode Compatibility
        Properties.  Behavior is efficient and stable within a
        Postgres major version.  It is only
        available for encoding UTF8.
       

      

     

     

      
pg_c_utf8

      

       

        This collation sorts by Unicode code point values rather than natural
        language order.  For the functions lower,
        initcap, and upper, it uses
        Unicode simple case mapping.  For pattern matching (including regular
        expressions), it uses the POSIX Compatible variant of Unicode Compatibility
        Properties.  Behavior is efficient and stable within a
        PostgreSQL major version.  This collation is
        only available for encoding UTF8.
       

      

     

     

      
C (equivalent to POSIX)

      

       

        The C and POSIX collations are
        based on traditional C behavior.  They sort by byte
        values rather than natural language order, and only the ASCII letters
        A through
        Z are treated as letters.  The
        behavior is efficient and stable across all versions for a given
        database encoding, but behavior may vary between different database
        encodings.
       

      

     

     

      
default

      

       

        The default collation selects the locale specified
        at database creation time.
       

      

     

    

   

   

    Additional collations may be available depending on operating system
    support.  The efficiency and stability of these additional collations
    depend on the collation provider, the provider version, and the locale.
   

  

  

   
# Predefined Collations

   

    If the operating system provides support for using multiple locales
    within a single program (newlocale and related functions),
    or if support for ICU is configured,
    then when a database cluster is initialized, initdb
    populates the system catalog pg_collation with
    collations based on all the locales it finds in the operating
    system at the time.
   

   

    To inspect the currently available locales, use the query SELECT
    * FROM pg_collation, or the command \dOS+
    in psql.
   

  
   
# libc Collations

   

    For example, the operating system might
    provide a locale named de_DE.utf8.
    initdb would then create a collation named
    de_DE.utf8 for encoding UTF8
    that has both LC_COLLATE and
    LC_CTYPE set to de_DE.utf8.
    It will also create a collation with the .utf8
    tag stripped off the name.  So you could also use the collation
    under the name de_DE, which is less cumbersome
    to write and makes the name less encoding-dependent.  Note that,
    nevertheless, the initial set of collation names is
    platform-dependent.
   

   

    The default set of collations provided by libc map
    directly to the locales installed in the operating system, which can be
    listed using the command locale -a.  In case
    a libc collation is needed that has different values
    for LC_COLLATE and LC_CTYPE, or if new
    locales are installed in the operating system after the database system
    was initialized, then a new collation may be created using
    the  command.
    New operating system locales can also be imported en masse using
    the pg_import_system_collations() function.
   

   

    Within any particular database, only collations that use that
    database's encoding are of interest.  Other entries in
    pg_collation are ignored.  Thus, a stripped collation
    name such as de_DE can be considered unique
    within a given database even though it would not be unique globally.
    Use of the stripped collation names is recommended, since it will
    make one fewer thing you need to change if you decide to change to
    another database encoding.  Note however that the default,
    C, and POSIX collations can be used regardless of
    the database encoding.
   

   

    PostgreSQL considers distinct collation
    objects to be incompatible even when they have identical properties.
    Thus for example,

```
SELECT a COLLATE "C" < b COLLATE "POSIX" FROM test1;
```

    will draw an error even though the C and POSIX
    collations have identical behaviors.  Mixing stripped and non-stripped
    collation names is therefore not recommended.
   

  

  
   
# ICU Collations

   

    With ICU, it is not sensible to enumerate all possible locale names.  ICU
    uses a particular naming system for locales, but there are many more ways
    to name a locale than there are actually distinct locales.
    initdb uses the ICU APIs to extract a set of distinct
    locales to populate the initial set of collations.  Collations provided by
    ICU are created in the SQL environment with names in BCP 47 language tag
    format, with a private use
    extension -x-icu appended, to distinguish them from
    libc locales.
   

   

    Here are some example collations that might be created:

    

     

      
de-x-icu

      

       
German collation, default variant

      

     

     

      
de-AT-x-icu

      

       
German collation for Austria, default variant

       

        (There are also, say, de-DE-x-icu
        or de-CH-x-icu, but as of this writing, they are
        equivalent to de-x-icu.)
       

      

     

     

      
und-x-icu (for undefined)

      

       

        ICU root collation.  Use this to get a reasonable
        language-agnostic sort order.
       

      

     

    

   

   

    Some (less frequently used) encodings are not supported by ICU.  When the
    database encoding is one of these, ICU collation entries
    in pg_collation are ignored.  Attempting to use one
    will draw an error along the lines of collation "de-x-icu" for
    encoding "WIN874" does not exist.
   

  
  

  

   
# Creating New Collation Objects

   

    If the standard and predefined collations are not sufficient, users can
    create their own collation objects using the SQL
    command .
   

   

    The standard and predefined collations are in the
    schema pg_catalog, like all predefined objects.
    User-defined collations should be created in user schemas.  This also
    ensures that they are saved by pg_dump.
   

   
    
# libc Collations

    

     New libc collations can be created like this:

```
CREATE COLLATION german (provider = libc, locale = 'de_DE');
```

     The exact values that are acceptable for the locale
     clause in this command depend on the operating system.  On Unix-like
     systems, the command locale -a will show a list.
    

    

     Since the predefined libc collations already include all collations
     defined in the operating system when the database instance is
     initialized, it is not often necessary to manually create new ones.
     Reasons might be if a different naming system is desired (in which case
     see also ) or if the operating system has
     been upgraded to provide new locale definitions (in which case see
     also pg_import_system_collations()).
    

   

   
    
# ICU Collations

    

     ICU collations can be created like:

```
CREATE COLLATION german (provider = icu, locale = 'de-DE');
```

     ICU locales are specified as a BCP 47 Language Tag, but can also accept most
     libc-style locale names. If possible, libc-style locale names are
     transformed into language tags.
    

    

     New ICU collations can customize collation behavior extensively by
     including collation attributes in the language tag. See  for details and examples.
    

   
   
   
# Copying Collations

   

    The command  can also be used to
    create a new collation from an existing collation, which can be useful to
    be able to use operating-system-independent collation names in
    applications, create compatibility names, or use an ICU-provided collation
    under a more readable name.  For example:

```
CREATE COLLATION german FROM "de_DE";
CREATE COLLATION french FROM "fr-x-icu";
```

   

   
   

   

    
# Nondeterministic Collations

    

     A collation is either deterministic or
     nondeterministic.  A deterministic collation uses
     deterministic comparisons, which means that it considers strings to be
     equal only if they consist of the same byte sequence.  Nondeterministic
     comparison may determine strings to be equal even if they consist of
     different bytes.  Typical situations include case-insensitive comparison,
     accent-insensitive comparison, as well as comparison of strings in
     different Unicode normal forms.  It is up to the collation provider to
     actually implement such insensitive comparisons; the deterministic flag
     only determines whether ties are to be broken using bytewise comparison.
     See also Unicode Technical
     Standard 10 for more information on the terminology.
    

    

     To create a nondeterministic collation, specify the property
     deterministic = false to CREATE
     COLLATION, for example:

```
CREATE COLLATION ndcoll (provider = icu, locale = 'und', deterministic = false);
```

     This example would use the standard Unicode collation in a
     nondeterministic way.  In particular, this would allow strings in
     different normal forms to be compared correctly.  More interesting
     examples make use of the ICU customization facilities explained above.
     For example:

```
CREATE COLLATION case_insensitive (provider = icu, locale = 'und-u-ks-level2', deterministic = false);
CREATE COLLATION ignore_accents (provider = icu, locale = 'und-u-ks-level1-kc-true', deterministic = false);
```

    

    

     All standard and predefined collations are deterministic, all
     user-defined collations are deterministic by default.  While
     nondeterministic collations give a more correct behavior,
     especially when considering the full power of Unicode and its many
     special cases, they also have some drawbacks.  Foremost, their use leads
     to a performance penalty.  Note, in particular, that B-tree cannot use
     deduplication with indexes that use a nondeterministic collation.  Also,
     certain operations are not possible with nondeterministic collations,
     such as some pattern matching operations.  Therefore, they should be used
     only in cases where they are specifically wanted.
    

    
     

      To deal with text in different Unicode normalization forms, it is also
      an option to use the functions/expressions
      normalize and is normalized to
      preprocess or check the strings, instead of using nondeterministic
      collations.  There are different trade-offs for each approach.
     

    
   

  

  

   
# ICU Custom Collations

   

    ICU allows extensive control over collation behavior by defining new
    collations with collation settings as a part of the language tag. These
    settings can modify the collation order to suit a variety of needs. For
    instance:

```
-- ignore differences in accents and case
CREATE COLLATION ignore_accent_case (provider = icu, deterministic = false, locale = 'und-u-ks-level1');
SELECT 'Å' = 'A' COLLATE ignore_accent_case; -- true
SELECT 'z' = 'Z' COLLATE ignore_accent_case; -- true

-- upper case letters sort before lower case.
CREATE COLLATION upper_first (provider = icu, locale = 'und-u-kf-upper');
SELECT 'B' < 'b' COLLATE upper_first; -- true

-- treat digits numerically and ignore punctuation
CREATE COLLATION num_ignore_punct (provider = icu, deterministic = false, locale = 'und-u-ka-shifted-kn');
SELECT 'id-45' < 'id-123' COLLATE num_ignore_punct; -- true
SELECT 'w;x*y-z' = 'wxyz' COLLATE num_ignore_punct; -- true
```

    Many of the available options are described in , or see  for more details.
   

   

    
# ICU Comparison Levels

    

     Comparison of two strings (collation) in ICU is determined by a
     multi-level process, where textual features are grouped into
     "levels". Treatment of each level is controlled by the collation settings. Higher
     levels correspond to finer textual features.
    

    

      shows which textual feature
     differences are considered significant when determining equality at the
     given level. The Unicode character U+2063 is an
     invisible separator, and as seen in the table, is ignored for at all
     levels of comparison less than identic.
    

     
      
# ICU Collation Levels

      
       
       
       
       
       
       
       
       

       
        
         Level
         Description
         'f' = 'f'
         'ab' = U&'a\2063b'
         'x-y' = 'x_y'
         'g' = 'G'
         'n' = 'ñ'
         'y' = 'z'
        
       

       
        
         level1
         Base Character
         true
         true
         true
         true
         true
         false
        
        
         level2
         Accents
         true
         true
         true
         true
         false
         false
        
        
         level3
         Case/Variants
         true
         true
         true
         false
         false
         false
        
        
         level4
         Punctuation
only with
         ka-shifted; see 

         true
         true
         false
         false
         false
         false
        
        
         identic
         All
         true
         false
         false
         false
         false
         false
        
       
      
     

    

     At every level, even with full normalization off, basic normalization is
     performed. For example, 'á' may be composed of the
     code points U&'\0061\0301' or the single code
     point U&'\00E1', and those sequences will be
     considered equal even at the identic level. To treat
     any difference in code point representation as distinct, use a collation
     created with deterministic set to
     true.
    

    
     
# Collation Level Examples

```
CREATE COLLATION level3 (provider = icu, deterministic = false, locale = 'und-u-ka-shifted-ks-level3');
CREATE COLLATION level4 (provider = icu, deterministic = false, locale = 'und-u-ka-shifted-ks-level4');
CREATE COLLATION identic (provider = icu, deterministic = false, locale = 'und-u-ka-shifted-ks-identic');

-- invisible separator ignored at all levels except identic
SELECT 'ab' = U&'a\2063b' COLLATE level4; -- true
SELECT 'ab' = U&'a\2063b' COLLATE identic; -- false

-- punctuation ignored at level3 but not at level 4
SELECT 'x-y' = 'x_y' COLLATE level3; -- true
SELECT 'x-y' = 'x_y' COLLATE level4; -- false
```

    
   

   

    
# Collation Settings for an ICU Locale

    

      shows the available
     collation settings, which can be used as part of a language tag to
     customize a collation.
    

     
      
# ICU Collation Settings

      
       
       
       
       

       
        
         Key
         Values
         Default
         Description
        
       

       
        
         co
         emoji, phonebk, standard, ...
         standard
         
          Collation type. See  for additional options and details.
         
        

        
         ka
         noignore, shifted
         noignore
         
          If set to shifted, causes some characters
          (e.g. punctuation or space) to be ignored in comparison. Key
          ks must be set to level3 or
          lower to take effect. Set key kv to control which
          character classes are ignored.
         
        

        
         kb
         true, false
         false
         
          Backwards comparison for the level 2 differences. For example,
          locale und-u-kb sorts 'àe'
          before 'aé'.
         
        

        
         kc
         true, false
         false
         
          

           Separates case into a "level 2.5" that falls between accents and
           other level 3 features.
          

          

           If set to true and ks is set
           to level1, will ignore accents but take case
           into account.
          

         
        

        
         kf
         
          upper, lower,
          false
         
         false
         
          If set to upper, upper case sorts before lower
          case. If set to lower, lower case sorts before
          upper case. If set to false, the sort depends on
          the rules of the locale.
         
        

        
         kn
         true, false
         false
         
          If set to true, numbers within a string are
          treated as a single numeric value rather than a sequence of
          digits. For example, 'id-45' sorts before
          'id-123'.
         
        

        
         kk
         true, false
         false
         
          

           Enable full normalization; may affect performance. Basic
           normalization is performed even when set to
           false. Locales for languages that require full
           normalization typically enable it by default.
          

          

           Full normalization is important in some cases, such as when
           multiple accents are applied to a single character. For example,
           the code point sequences U&'\0065\0323\0302'
           and U&'\0065\0302\0323' represent
           an e with circumflex and dot-below accents
           applied in different orders. With full normalization
           on, these code point sequences are treated as equal; otherwise they
           are unequal.
          

         
        

        
         kr
         
          space, punct,
          symbol, currency,
          digit, script-id
         
         
         
          

           Set to one or more of the valid values, or any BCP 47
           script-id, e.g. latn
           ("Latin") or grek ("Greek"). Multiple values are
           separated by "-".
          

          

           Redefines the ordering of classes of characters; those characters
           belonging to a class earlier in the list sort before characters
           belonging to a class later in the list. For instance, the value
           digit-currency-space (as part of a language tag
           like und-u-kr-digit-currency-space) sorts
           punctuation before digits and spaces.
          

         
        

        
         ks
         level1, level2, level3, level4, identic
         level3
         
          Sensitivity (or "strength") when determining equality, with
          level1 the least sensitive to differences and
          identic the most sensitive to differences. See
           for details.
         
        

        
         kv
         
          space, punct,
          symbol, currency
         
         punct
         
          Classes of characters ignored during comparison at level 3. Setting
          to a later value includes earlier values;
          e.g. symbol also includes
          punct and space in the
          characters to be ignored. Key ka must be set to
          shifted and key ks must be set
          to level3 or lower to take effect.
         
        
       
      
     

    

     Defaults may depend on locale. The above table is not meant to be
     complete. See  for additional
     options and details.
    

    
     

      For many collation settings, you must create the collation with
      deterministic set to false for the
      setting to have the desired effect (see ). Additionally, some settings
      only take effect when the key ka is set to
      shifted (see ).
     

    
   

   

    
# Collation Settings Examples

     

      

       
CREATE COLLATION "de-u-co-phonebk-x-icu" (provider = icu, locale = 'de-u-co-phonebk');

       

        
German collation with phone book collation type

       

      

      

       
CREATE COLLATION "und-u-co-emoji-x-icu" (provider = icu, locale = 'und-u-co-emoji');

       

        

         Root collation with Emoji collation type, per Unicode Technical Standard #51
        

       

      

      

       
CREATE COLLATION latinlast (provider = icu, locale = 'en-u-kr-grek-latn');

       

        

         Sort Greek letters before Latin ones.  (The default is Latin before Greek.)
        

       

      

      

       
CREATE COLLATION upperfirst (provider = icu, locale = 'en-u-kf-upper');

       

        

         Sort upper-case letters before lower-case letters.  (The default is
         lower-case letters first.)
        

       

      

      

       
CREATE COLLATION special (provider = icu, locale = 'en-u-kf-upper-kr-grek-latn');

       

        

         Combines both of the above options.
        

       

      

     

   

   

    
# ICU Tailoring Rules

    

     If the options provided by the collation settings shown above are not
     sufficient, the order of collation elements can be changed with tailoring
     rules, whose syntax is detailed at .
    

    

     This small example creates a collation based on the root locale with a
     tailoring rule:

```

```

     With this rule, the letter W is sorted after
     V, but is treated as a secondary difference similar to an
     accent.  Rules like this are contained in the locale definitions of some
     languages.  (Of course, if a locale definition already contains the
     desired rules, then they don't need to be specified again explicitly.)
    

    

     Here is a more complex example.  The following statement sets up a
     collation named ebcdic with rules to sort US-ASCII
     characters in the order of the EBCDIC encoding.

```
' 

SELECT c
FROM (VALUES ('a'), ('b'), ('A'), ('B'), ('1'), ('2'), ('!'), ('^')) AS x(c)
ORDER BY c COLLATE ebcdic;
 c
---
 !
 a
 b
 ^
 A
 B
 1
 2
```

    

   

   

    
# External References for ICU

    

     This section () is only a brief
     overview of ICU behavior and language tags. Refer to the following
     documents for technical details, additional options, and new behavior:
    

    

     

      

       Unicode Technical Standard #35
      

     

     

      

       BCP 47
      

     

     

      

       CLDR repository
      

     

     

      

       
      

     

     

      

       
      

     

    

   

  

 

 

  
# Character Set Support

  character set

  

   The character set support in PostgreSQL
   allows you to store text in a variety of character sets (also called
   encodings), including
   single-byte character sets such as the ISO 8859 series and
   multiple-byte character sets such as EUC (Extended Unix
   Code) and UTF-8.  All supported character sets
   can be used transparently by clients, but a few are not supported
   for use within the server (that is, as a server-side encoding).
   The default character set is selected while
   initializing your PostgreSQL database
   cluster using initdb.  It can be overridden when you
   create a database, so you can have multiple
   databases each with a different character set.
  

  

   An important restriction, however, is that each database's character set
   must be compatible with the database's LC_CTYPE (character
   classification) and LC_COLLATE (string sort order) locale
   settings. For C or
   POSIX locale, any character set is allowed, but for other
   libc-provided locales there is only one character set that will work
   correctly.
   (On Windows, however, UTF-8 encoding can be used with any locale.)
   If you have ICU support configured, ICU-provided locales can be used
   with most but not all server-side encodings.
  

   

    
# Supported Character Sets

    

      shows the character sets available
     for use in PostgreSQL.
    

     
      
# PostgreSQL Character Sets

      
       
       
       
       
       
       
       
       
        
         Name
         Description
         Language
         Server?
         ICU?
         
         Bytes/&zwsp;Char
         Aliases
        
       
       
        
         BIG5
         Big Five
         Traditional Chinese
         No
         No
         1&ndash;2
         WIN950, Windows950
        
        
         EUC_CN
         Extended UNIX Code-CN
         Simplified Chinese
         Yes
         Yes
         1&ndash;3
         
        
        
         EUC_JP
         Extended UNIX Code-JP
         Japanese
         Yes
         Yes
         1&ndash;3
         
        
        
         EUC_JIS_2004
         Extended UNIX Code-JP, JIS X 0213
         Japanese
         Yes
         No
         1&ndash;3
         
        
        
         EUC_KR
         Extended UNIX Code-KR
         Korean
         Yes
         Yes
         1&ndash;3
         
        
        
         EUC_TW
         Extended UNIX Code-TW
         Traditional Chinese, Taiwanese
         Yes
         Yes
         1&ndash;4
         
        
        
         GB18030
         National Standard, version 2022
         Chinese
         No
         No
         1&ndash;4
         
        
        
         GBK
         Extended National Standard
         Simplified Chinese
         No
         No
         1&ndash;2
         WIN936, Windows936
        
        
         ISO_8859_5
         ISO 8859-5, ECMA 113
         Latin/Cyrillic
         Yes
         Yes
         1
         
        
        
         ISO_8859_6
         ISO 8859-6, ECMA 114
         Latin/Arabic
         Yes
         Yes
         1
         
        
        
         ISO_8859_7
         ISO 8859-7, ECMA 118
         Latin/Greek
         Yes
         Yes
         1
         
        
        
         ISO_8859_8
         ISO 8859-8, ECMA 121
         Latin/Hebrew
         Yes
         Yes
         1
         
        
        
         JOHAB
         JOHAB
         Korean (Hangul)
         No
         No
         1&ndash;3
         
        
        
         KOI8R
         KOI8-R
         Cyrillic (Russian)
         Yes
         Yes
         1
         KOI8
        
        
         KOI8U
         KOI8-U
         Cyrillic (Ukrainian)
         Yes
         Yes
         1
         
        
        
         LATIN1
         ISO 8859-1, ECMA 94
         Western European
         Yes
         Yes
         1
         ISO88591
        
        
         LATIN2
         ISO 8859-2, ECMA 94
         Central European
         Yes
         Yes
         1
         ISO88592
        
        
         LATIN3
         ISO 8859-3, ECMA 94
         South European
         Yes
         Yes
         1
         ISO88593
        
        
         LATIN4
         ISO 8859-4, ECMA 94
         North European
         Yes
         Yes
         1
         ISO88594
        
        
         LATIN5
         ISO 8859-9, ECMA 128
         Turkish
         Yes
         Yes
         1
         ISO88599
        
        
         LATIN6
         ISO 8859-10, ECMA 144
         Nordic
         Yes
         Yes
         1
         ISO885910
        
        
         LATIN7
         ISO 8859-13
         Baltic
         Yes
         Yes
         1
         ISO885913
        
        
         LATIN8
         ISO 8859-14
         Celtic
         Yes
         Yes
         1
         ISO885914
        
        
         LATIN9
         ISO 8859-15
         LATIN1 with Euro and accents
         Yes
         Yes
         1
         ISO885915
        
        
         LATIN10
         ISO 8859-16, ASRO SR 14111
         Romanian
         Yes
         No
         1
         ISO885916
        
        
         SJIS
         Shift JIS
         Japanese
         No
         No
         1&ndash;2
         Mskanji, ShiftJIS, WIN932, Windows932
        
        
         SHIFT_JIS_2004
         Shift JIS, JIS X 0213
         Japanese
         No
         No
         1&ndash;2
         
        
        
         SQL_ASCII
         unspecified (see text)
         any
         Yes
         No
         1
         
        
        
         UHC
         Unified Hangul Code
         Korean
         No
         No
         1&ndash;2
         WIN949, Windows949
        
        
         UTF8
         Unicode, 8-bit
         all
         Yes
         Yes
         1&ndash;4
         Unicode
        
        
         WIN866
         Windows CP866
         Cyrillic
         Yes
         Yes
         1
         ALT
        
        
         WIN874
         Windows CP874
         Thai
         Yes
         No
         1
         
        
        
         WIN1250
         Windows CP1250
         Central European
         Yes
         Yes
         1
         
        
        
         WIN1251
         Windows CP1251
         Cyrillic
         Yes
         Yes
         1
         WIN
        
        
         WIN1252
         Windows CP1252
         Western European
         Yes
         Yes
         1
         
        
        
         WIN1253
         Windows CP1253
         Greek
         Yes
         Yes
         1
         
        
        
         WIN1254
         Windows CP1254
         Turkish
         Yes
         Yes
         1
         
        
        
         WIN1255
         Windows CP1255
         Hebrew
         Yes
         Yes
         1
         
        
        
         WIN1256
         Windows CP1256
         Arabic
         Yes
         Yes
         1
         
        
        
         WIN1257
         Windows CP1257
         Baltic
         Yes
         Yes
         1
         
        
        
         WIN1258
         Windows CP1258
         Vietnamese
         Yes
         Yes
         1
         ABC, TCVN, TCVN5712, VSCII
        
       
      
     

     

      Not all client APIs support all the listed character sets. For example, the
      PostgreSQL
      JDBC driver does not support LATIN6,
      LATIN8, and LATIN10.
     

     

      The SQL_ASCII setting behaves considerably differently
      from the other settings.  When the server character set is
      SQL_ASCII, the server interprets byte values 0&ndash;127
      according to the ASCII standard, while byte values 128&ndash;255 are taken
      as uninterpreted characters.  No encoding conversion will be done when
      the setting is SQL_ASCII.  Thus, this setting is not so
      much a declaration that a specific encoding is in use, as a declaration
      of ignorance about the encoding.  In most cases, if you are
      working with any non-ASCII data, it is unwise to use the
      SQL_ASCII setting because
      PostgreSQL will be unable to help you by
      converting or validating non-ASCII characters.
     

    

   

    
# Setting the Character Set

    

     initdb defines the default character set (encoding)
     for a PostgreSQL cluster. For example,

```
initdb -E EUC_JP
```

     sets the default character set to
     EUC_JP (Extended Unix Code for Japanese).  You
     can use --encoding instead of
     -E if you prefer longer option strings.
     If no -E or --encoding option is
     given, initdb attempts to determine the appropriate
     encoding to use based on the specified or default locale.
    

    

     You can specify a non-default encoding at database creation time,
     provided that the encoding is compatible with the selected locale:

```
createdb -E EUC_KR -T template0 --lc-collate=ko_KR.euckr --lc-ctype=ko_KR.euckr korean
```

     This will create a database named korean that
     uses the character set EUC_KR, and locale ko_KR.
     Another way to accomplish this is to use this SQL command:

```
CREATE DATABASE korean WITH ENCODING 'EUC_KR' LC_COLLATE='ko_KR.euckr' LC_CTYPE='ko_KR.euckr' TEMPLATE=template0;
```

     Notice that the above commands specify copying the template0
     database.  When copying any other database, the encoding and locale
     settings cannot be changed from those of the source database, because
     that might result in corrupt data.  For more information see
     .
    

    

     The encoding for a database is stored in the system catalog
     pg_database.  You can see it by using the
     psql -l option or the
     \l command.

```
$ psql -l
                                         List of databases
   Name    |  Owner   | Encoding  |  Collation  |    Ctype    |          Access Privileges
-----------+----------+-----------+-------------+-------------+-------------------------------------
 clocaledb | hlinnaka | SQL_ASCII | C           | C           |
 englishdb | hlinnaka | UTF8      | en_GB.UTF8  | en_GB.UTF8  |
 japanese  | hlinnaka | UTF8      | ja_JP.UTF8  | ja_JP.UTF8  |
 korean    | hlinnaka | EUC_KR    | ko_KR.euckr | ko_KR.euckr |
 postgres  | hlinnaka | UTF8      | fi_FI.UTF8  | fi_FI.UTF8  |
 template0 | hlinnaka | UTF8      | fi_FI.UTF8  | fi_FI.UTF8  | {=c/hlinnaka,hlinnaka=CTc/hlinnaka}
 template1 | hlinnaka | UTF8      | fi_FI.UTF8  | fi_FI.UTF8  | {=c/hlinnaka,hlinnaka=CTc/hlinnaka}
(7 rows)
```

    

    
     

      On most modern operating systems, PostgreSQL
      can determine which character set is implied by the LC_CTYPE
      setting, and it will enforce that only the matching database encoding is
      used.  On older systems it is your responsibility to ensure that you use
      the encoding expected by the locale you have selected.  A mistake in
      this area is likely to lead to strange behavior of locale-dependent
      operations such as sorting.
     

     

      PostgreSQL will allow superusers to create
      databases with SQL_ASCII encoding even when
      LC_CTYPE is not C or POSIX.  As noted
      above, SQL_ASCII does not enforce that the data stored in
      the database has any particular encoding, and so this choice poses risks
      of locale-dependent misbehavior.  Using this combination of settings is
      deprecated and may someday be forbidden altogether.
     

    
   

   

    
# Automatic Character Set Conversion Between Server and Client

    

     PostgreSQL supports automatic character
     set conversion between server and client for many combinations of
     character sets (
     shows which ones).
    

    

     To enable automatic character set conversion, you have to
     tell PostgreSQL the character set
     (encoding) you would like to use in the client. There are several
     ways to accomplish this:

     

      

       

        Using the \encoding command in
        psql.
        \encoding allows you to change client
        encoding on the fly. For
        example, to change the encoding to SJIS, type:

```
\encoding SJIS
```

       

      

      

       

        libpq () has functions to control the client encoding.
       

      

      

       

        Using SET client_encoding TO.

        Setting the client encoding can be done with this SQL command:

```
SET CLIENT_ENCODING TO 'value';
```

        Also you can use the standard SQL syntax SET NAMES
        for this purpose:

```
SET NAMES 'value';
```

        To query the current client encoding:

```
SHOW client_encoding;
```

        To return to the default encoding:

```
RESET client_encoding;
```

       

      

      

       

        Using PGCLIENTENCODING. If the environment variable
        PGCLIENTENCODING is defined in the client's
        environment, that client encoding is automatically selected
        when a connection to the server is made.  (This can
        subsequently be overridden using any of the other methods
        mentioned above.)
       

      

      

      

       Using the configuration variable . If the
       client_encoding variable is set, that client
       encoding is automatically selected when a connection to the
       server is made.  (This can subsequently be overridden using any
       of the other methods mentioned above.)
       

      

     

    

    

     If the conversion of a particular character is not possible
     &mdash; suppose you chose EUC_JP for the
     server and LATIN1 for the client, and some
     Japanese characters are returned that do not have a representation in
     LATIN1 &mdash; an error is reported.
    

    

     If the client character set is defined as SQL_ASCII,
     encoding conversion is disabled, regardless of the server's character
     set.  (However, if the server's character set is
     not SQL_ASCII, the server will still check that
     incoming data is valid for that encoding; so the net effect is as
     though the client character set were the same as the server's.)
     Just as for the server, use of SQL_ASCII is unwise
     unless you are working with all-ASCII data.
    

   

   

    
# Available Character Set Conversions

    

     PostgreSQL allows conversion between any
     two character sets for which a conversion function is listed in the
     pg_conversion
     system catalog.  PostgreSQL comes with
     some predefined conversions, as summarized in
      and shown in more
     detail in .  You can
     create a new conversion using the SQL command
     .  (To be used for automatic
     client/server conversions, a conversion must be marked
     as default for its character set pair.)
    

    
     
# Built-in Client/Server Character Set Conversions

     
      
      
      
       
        Server Character Set
        Available Client Character Sets
       
      
      
       
        BIG5
        not supported as a server encoding
        
       
       
        EUC_CN
        EUC_CN,
        UTF8
        
       
       
        EUC_JP
        EUC_JP,
        SJIS,
        UTF8
        
       
       
        EUC_JIS_2004
        EUC_JIS_2004,
        SHIFT_JIS_2004,
        UTF8
        
       
       
        EUC_KR
        EUC_KR,
        UTF8
        
       
       
        EUC_TW
        EUC_TW,
        BIG5,
        UTF8
        
       
       
        GB18030
        not supported as a server encoding
        
       
       
        GBK
        not supported as a server encoding
        
       
       
        ISO_8859_5
        ISO_8859_5,
        KOI8R,
        UTF8,
        WIN866,
        WIN1251
        
       
       
        ISO_8859_6
        ISO_8859_6,
        UTF8
        
       
       
        ISO_8859_7
        ISO_8859_7,
        UTF8
        
       
       
        ISO_8859_8
        ISO_8859_8,
        UTF8
        
       
       
        JOHAB
        not supported as a server encoding
        
       
       
        KOI8R
        KOI8R,
        ISO_8859_5,
        UTF8,
        WIN866,
        WIN1251
        
       
       
        KOI8U
        KOI8U,
        UTF8
        
       
       
        LATIN1
        LATIN1,
        UTF8
        
       
       
        LATIN2
        LATIN2,
        UTF8,
        WIN1250
        
       
       
        LATIN3
        LATIN3,
        UTF8
        
       
       
        LATIN4
        LATIN4,
        UTF8
        
       
       
        LATIN5
        LATIN5,
        UTF8
        
       
       
        LATIN6
        LATIN6,
        UTF8
        
       
       
        LATIN7
        LATIN7,
        UTF8
        
       
       
        LATIN8
        LATIN8,
        UTF8
        
       
       
        LATIN9
        LATIN9,
        UTF8
        
       
       
        LATIN10
        LATIN10,
        UTF8
        
       
       
        SJIS
        not supported as a server encoding
        
       
       
        SHIFT_JIS_2004
        not supported as a server encoding
        
       
       
        SQL_ASCII
        any (no conversion will be performed)
        
       
       
        UHC
        not supported as a server encoding
        
       
       
        UTF8
        all supported encodings
        
       
       
        WIN866
        WIN866,
         ISO_8859_5,
         KOI8R,
         UTF8,
         WIN1251
        
       
       
        WIN874
        WIN874,
        UTF8
        
       
       
        WIN1250
        WIN1250,
         LATIN2,
         UTF8
        
       
       
        WIN1251
        WIN1251,
         ISO_8859_5,
         KOI8R,
         UTF8,
         WIN866
        
       
       
        WIN1252
        WIN1252,
         UTF8
        
       
       
        WIN1253
        WIN1253,
         UTF8
        
       
       
        WIN1254
        WIN1254,
         UTF8
        
       
       
        WIN1255
        WIN1255,
         UTF8
        
       
       
        WIN1256
        WIN1256,
        UTF8
        
       
       
        WIN1257
        WIN1257,
         UTF8
        
       
       
        WIN1258
        WIN1258,
        UTF8
        
       
      
     
    

    
     
# All Built-in Character Set Conversions

     
      
      
      
      
       
        Conversion Name
         
          

           The conversion names follow a standard naming scheme: The
           official name of the source encoding with all
           non-alphanumeric characters replaced by underscores, followed
           by _to_, followed by the similarly processed
           destination encoding name.  Therefore, these names sometimes
           deviate from the customary encoding names shown in
           .
          

         
        
        Source Encoding
        Destination Encoding
       
      

      
       
        big5_to_euc_tw
        BIG5
        EUC_TW
       
       
        big5_to_utf8
        BIG5
        UTF8
       
       
        euc_cn_to_utf8
        EUC_CN
        UTF8
       
       
        euc_jp_to_sjis
        EUC_JP
        SJIS
       
       
        euc_jp_to_utf8
        EUC_JP
        UTF8
       
       
        euc_kr_to_utf8
        EUC_KR
        UTF8
       
       
        euc_tw_to_big5
        EUC_TW
        BIG5
       
       
        euc_tw_to_utf8
        EUC_TW
        UTF8
       
       
        gb18030_to_utf8
        GB18030
        UTF8
       
       
        gbk_to_utf8
        GBK
        UTF8
       
       
        iso_8859_10_to_utf8
        LATIN6
        UTF8
       
       
        iso_8859_13_to_utf8
        LATIN7
        UTF8
       
       
        iso_8859_14_to_utf8
        LATIN8
        UTF8
       
       
        iso_8859_15_to_utf8
        LATIN9
        UTF8
       
       
        iso_8859_16_to_utf8
        LATIN10
        UTF8
       
       
        iso_8859_1_to_utf8
        LATIN1
        UTF8
       
       
        iso_8859_2_to_utf8
        LATIN2
        UTF8
       
       
        iso_8859_2_to_windows_1250
        LATIN2
        WIN1250
       
       
        iso_8859_3_to_utf8
        LATIN3
        UTF8
       
       
        iso_8859_4_to_utf8
        LATIN4
        UTF8
       
       
        iso_8859_5_to_koi8_r
        ISO_8859_5
        KOI8R
       
       
        iso_8859_5_to_utf8
        ISO_8859_5
        UTF8
       
       
        iso_8859_5_to_windows_1251
        ISO_8859_5
        WIN1251
       
       
        iso_8859_5_to_windows_866
        ISO_8859_5
        WIN866
       
       
        iso_8859_6_to_utf8
        ISO_8859_6
        UTF8
       
       
        iso_8859_7_to_utf8
        ISO_8859_7
        UTF8
       
       
        iso_8859_8_to_utf8
        ISO_8859_8
        UTF8
       
       
        iso_8859_9_to_utf8
        LATIN5
        UTF8
       
       
        johab_to_utf8
        JOHAB
        UTF8
       
       
        koi8_r_to_iso_8859_5
        KOI8R
        ISO_8859_5
       
       
        koi8_r_to_utf8
        KOI8R
        UTF8
       
       
        koi8_r_to_windows_1251
        KOI8R
        WIN1251
       
       
        koi8_r_to_windows_866
        KOI8R
        WIN866
       
       
        koi8_u_to_utf8
        KOI8U
        UTF8
       
       
        sjis_to_euc_jp
        SJIS
        EUC_JP
       
       
        sjis_to_utf8
        SJIS
        UTF8
       
       
        windows_1258_to_utf8
        WIN1258
        UTF8
       
       
        uhc_to_utf8
        UHC
        UTF8
       
       
        utf8_to_big5
        UTF8
        BIG5
       
       
        utf8_to_euc_cn
        UTF8
        EUC_CN
       
       
        utf8_to_euc_jp
        UTF8
        EUC_JP
       
       
        utf8_to_euc_kr
        UTF8
        EUC_KR
       
       
        utf8_to_euc_tw
        UTF8
        EUC_TW
       
       
        utf8_to_gb18030
        UTF8
        GB18030
       
       
        utf8_to_gbk
        UTF8
        GBK
       
       
        utf8_to_iso_8859_1
        UTF8
        LATIN1
       
       
        utf8_to_iso_8859_10
        UTF8
        LATIN6
       
       
        utf8_to_iso_8859_13
        UTF8
        LATIN7
       
       
        utf8_to_iso_8859_14
        UTF8
        LATIN8
       
       
        utf8_to_iso_8859_15
        UTF8
        LATIN9
       
       
        utf8_to_iso_8859_16
        UTF8
        LATIN10
       
       
        utf8_to_iso_8859_2
        UTF8
        LATIN2
       
       
        utf8_to_iso_8859_3
        UTF8
        LATIN3
       
       
        utf8_to_iso_8859_4
        UTF8
        LATIN4
       
       
        utf8_to_iso_8859_5
        UTF8
        ISO_8859_5
       
       
        utf8_to_iso_8859_6
        UTF8
        ISO_8859_6
       
       
        utf8_to_iso_8859_7
        UTF8
        ISO_8859_7
       
       
        utf8_to_iso_8859_8
        UTF8
        ISO_8859_8
       
       
        utf8_to_iso_8859_9
        UTF8
        LATIN5
       
       
        utf8_to_johab
        UTF8
        JOHAB
       
       
        utf8_to_koi8_r
        UTF8
        KOI8R
       
       
        utf8_to_koi8_u
        UTF8
        KOI8U
       
       
        utf8_to_sjis
        UTF8
        SJIS
       
       
        utf8_to_windows_1258
        UTF8
        WIN1258
       
       
        utf8_to_uhc
        UTF8
        UHC
       
       
        utf8_to_windows_1250
        UTF8
        WIN1250
       
       
        utf8_to_windows_1251
        UTF8
        WIN1251
       
       
        utf8_to_windows_1252
        UTF8
        WIN1252
       
       
        utf8_to_windows_1253
        UTF8
        WIN1253
       
       
        utf8_to_windows_1254
        UTF8
        WIN1254
       
       
        utf8_to_windows_1255
        UTF8
        WIN1255
       
       
        utf8_to_windows_1256
        UTF8
        WIN1256
       
       
        utf8_to_windows_1257
        UTF8
        WIN1257
       
       
        utf8_to_windows_866
        UTF8
        WIN866
       
       
        utf8_to_windows_874
        UTF8
        WIN874
       
       
        windows_1250_to_iso_8859_2
        WIN1250
        LATIN2
       
       
        windows_1250_to_utf8
        WIN1250
        UTF8
       
       
        windows_1251_to_iso_8859_5
        WIN1251
        ISO_8859_5
       
       
        windows_1251_to_koi8_r
        WIN1251
        KOI8R
       
       
        windows_1251_to_utf8
        WIN1251
        UTF8
       
       
        windows_1251_to_windows_866
        WIN1251
        WIN866
       
       
        windows_1252_to_utf8
        WIN1252
        UTF8
       
       
        windows_1256_to_utf8
        WIN1256
        UTF8
       
       
        windows_866_to_iso_8859_5
        WIN866
        ISO_8859_5
       
       
        windows_866_to_koi8_r
        WIN866
        KOI8R
       
       
        windows_866_to_utf8
        WIN866
        UTF8
       
       
        windows_866_to_windows_1251
        WIN866
        WIN
       
       
        windows_874_to_utf8
        WIN874
        UTF8
       
       
        euc_jis_2004_to_utf8
        EUC_JIS_2004
        UTF8
       
       
        utf8_to_euc_jis_2004
        UTF8
        EUC_JIS_2004
       
       
        shift_jis_2004_to_utf8
        SHIFT_JIS_2004
        UTF8
       
       
        utf8_to_shift_jis_2004
        UTF8
        SHIFT_JIS_2004
       
       
        euc_jis_2004_to_shift_jis_2004
        EUC_JIS_2004
        SHIFT_JIS_2004
       
       
        shift_jis_2004_to_euc_jis_2004
        SHIFT_JIS_2004
        EUC_JIS_2004
       
      
     
    
   

   

    
# Further Reading

    

     These are good sources to start learning about various kinds of encoding
     systems.

     

      

       
CJKV Information Processing: Chinese, Japanese, Korean & Vietnamese Computing

       

        

         Contains detailed explanations of EUC_JP,
         EUC_CN, EUC_KR,
         EUC_TW.
        

       

      

      

       

       

        

         The web site of the Unicode Consortium.
        

       

      

      

       
RFC 3629

       

        

         UTF-8 (8-bit UCS/Unicode Transformation
         Format) is defined here.
