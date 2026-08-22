# SQL Conformance

 

  This section attempts to outline to what extent
  PostgreSQL conforms to the current SQL
  standard.  The following information is not a full statement of
  conformance, but it presents the main topics in as much detail as is
  both reasonable and useful for users.
 

 

  The formal name of the SQL standard is ISO/IEC 9075 Database
  Language SQL.  A revised version of the standard is released
  from time to time; the most recent update appearing in 2023.
  The 2023 version is referred to as ISO/IEC 9075:2023, or simply as SQL:2023.
  The versions prior to that were SQL:2016, SQL:2011, SQL:2008, SQL:2006, SQL:2003,
  SQL:1999, and SQL-92.  Each version
  replaces the previous one, so claims of conformance to earlier
  versions have no official merit.
  PostgreSQL development aims for
  conformance with the latest official version of the standard where
  such conformance does not contradict traditional features or common
  sense.  Many of the features required by the SQL
  standard are supported, though sometimes with slightly differing
  syntax or function.  Further moves towards conformance can be
  expected over time.
 

 

  SQL-92 defined three feature sets for
  conformance: Entry, Intermediate, and Full.  Most database
  management systems claiming SQL standard
  conformance were conforming at only the Entry level, since the
  entire set of features in the Intermediate and Full levels was
  either too voluminous or in conflict with legacy behaviors.
 

 

  Starting with SQL:1999, the SQL standard defines
  a large set of individual features rather than the ineffectively
  broad three levels found in SQL-92.  A large
  subset of these features represents the Core
  features, which every conforming SQL implementation must supply.
  The rest of the features are purely optional.
 

 

  The standard is split into a number of parts, each also known by a shorthand
  name:

  

   

ISO/IEC 9075-1 Framework (SQL/Framework)
SQL/Framework

   

ISO/IEC 9075-2 Foundation (SQL/Foundation)
SQL/Foundation

   

ISO/IEC 9075-3 Call Level Interface (SQL/CLI)
SQL/CLI

   

ISO/IEC 9075-4 Persistent Stored Modules (SQL/PSM)
SQL/PSM

   

ISO/IEC 9075-9 Management of External Data (SQL/MED)
SQL/MED

   

ISO/IEC 9075-10 Object Language Bindings (SQL/OLB)
SQL/OLB

   

ISO/IEC 9075-11 Information and Definition Schemas (SQL/Schemata)
SQL/Schemata

   

ISO/IEC 9075-13 Routines and Types using the Java Language (SQL/JRT)
SQL/JRT

   

ISO/IEC 9075-14 XML-related specifications (SQL/XML)
SQL/XML

   

ISO/IEC 9075-15 Multi-dimensional arrays (SQL/MDA)
SQL/MDA

   

ISO/IEC 9075-16 Property Graph Queries (SQL/PGQ)
SQL/PGQ

  

  Note that some part numbers are not (or no longer) used.
 

 

  The PostgreSQL core covers parts 1, 2, 9,
  11, 14, and 16.  Part 3 is covered by the ODBC driver, and part 13 is
  covered by the PL/Java plug-in, but exact conformance is currently
  not being verified for these components.  There are currently no
  implementations of parts 4, 10, and 15
  for PostgreSQL.
 

 

  PostgreSQL supports most of the major features of SQL:2023.  Out of
  177 mandatory features required for full Core conformance,
  PostgreSQL conforms to at least 170.  In addition, there is a long
  list of supported optional features.  It might be worth noting that at
  the time of writing, no current version of any database management
  system claims full conformance to Core SQL:2023.
 

 

  In the following two sections, we provide a list of those features
  that PostgreSQL supports, followed by a
  list of the features defined in SQL:2023 which
  are not yet supported in PostgreSQL.
  Both of these lists are approximate: There might be minor details that
  are nonconforming for a feature that is listed as supported, and
  large parts of an unsupported feature might in fact be implemented.
  The main body of the documentation always contains the most accurate
  information about what does and does not work.
 

 
  

   Feature codes containing a hyphen are subfeatures.  Therefore, if a
   particular subfeature is not supported, the main feature is listed
   as unsupported even if some other subfeatures are supported.
  

 

  

   
# Supported Features

   

    
     
      
      
      
      
      
       
        Identifier
        Core?
        Description
        Comment
       
      

      &features-supported;

     
    
   

  

  

   
# Unsupported Features

   

    The following features defined in SQL:2023 are not
    implemented in this release of
    PostgreSQL. In a few cases, equivalent
    functionality is available.

    
     
      
      
      
      
      
       
        Identifier
        Core?
        Description
        Comment
       
      

      &features-unsupported;

     
    
   

  

  

   
# XML Limits and Conformance to SQL/XML

   
    SQL/XML
    limits and conformance
   

   

    Significant revisions to the XML-related specifications in ISO/IEC 9075-14
    (SQL/XML) were introduced with SQL:2006.
    PostgreSQL's implementation of the XML data
    type and related functions largely follows the earlier 2003 edition,
    with some borrowing from later editions.  In particular:
    

     

      

       Where the current standard provides a family of XML data types
       to hold document or content in
       untyped or XML Schema-typed variants, and a type
       XML(SEQUENCE) to hold arbitrary pieces of XML content,
       PostgreSQL provides the single
       xml type, which can hold document or
       content.  There is no equivalent of the
       standard's sequence type.
      

     

     

      

       PostgreSQL provides two functions
       introduced in SQL:2006, but in variants that use the XPath 1.0
       language, rather than XML Query as specified for them in the
       standard.
      

     

     

      

       PostgreSQL does not support the
       RETURNING CONTENT or RETURNING SEQUENCE
       clauses, functions which are defined to have these in the specification
       are implicitly returning content.
      

     

    

   

   

    This section presents some of the resulting differences you may encounter.
   

   

    
# Queries Are Restricted to XPath 1.0

    

     The PostgreSQL-specific functions
     xpath() and xpath_exists()
     query XML documents using the XPath language.
     PostgreSQL also provides XPath-only variants
     of the standard functions XMLEXISTS and
     XMLTABLE, which officially use
     the XQuery language. For all of these functions,
     PostgreSQL relies on the
     libxml2 library, which provides only XPath 1.0.
    

    

     There is a strong connection between the XQuery language and XPath
     versions 2.0 and later: any expression that is syntactically valid and
     executes successfully in both produces the same result (with a minor
     exception for expressions containing numeric character references or
     predefined entity references, which XQuery replaces with the
     corresponding character while XPath leaves them alone).  But there is
     no such connection between these languages and XPath 1.0; it was an
     earlier language and differs in many respects.
    

    

     There are two categories of limitation to keep in mind: the restriction
     from XQuery to XPath for the functions specified in the SQL standard, and
     the restriction of XPath to version 1.0 for both the standard and the
     PostgreSQL-specific functions.
    

    

     
# Restriction of XQuery to XPath

     

      Features of XQuery beyond those of XPath include:

      

       

        

         XQuery expressions can construct and return new XML nodes, in
         addition to all possible XPath values.  XPath can create and return
         values of the atomic types (numbers, strings, and so on) but can
         only return XML nodes that were already present in documents
         supplied as input to the expression.
        

       

       

        

         XQuery has control constructs for iteration, sorting, and grouping.
        

       

       

        

         XQuery allows declaration and use of local functions.
        

       

      

     

     

      Recent XPath versions begin to offer capabilities overlapping with
      these (such as functional-style for-each and
      sort, anonymous functions, and
      parse-xml to create a node from a string),
      but such features were not available before XPath 3.0.
     

    

    

     
# Restriction of XPath to 1.0

     

      For developers familiar with XQuery and XPath 2.0 or later, XPath 1.0
      presents a number of differences to contend with:

      

       

        

         The fundamental type of an XQuery/XPath expression, the
         sequence, which can contain XML nodes, atomic values,
         or both, does not exist in XPath 1.0. A 1.0 expression can only
         produce a node-set (containing zero or more XML nodes), or a single
         atomic value.
        

       

       

        

          Unlike an XQuery/XPath sequence, which can contain any desired
          items in any desired order, an XPath 1.0 node-set has no
          guaranteed order and, like any set, does not allow multiple
          appearances of the same item.
         
          

           The libxml2 library does seem to
           always return node-sets to PostgreSQL
           with their members in the same relative order they had in the
           input document.  Its documentation does not commit to this
           behavior, and an XPath 1.0 expression cannot control it.
          

         
        

       

       

        

         While XQuery/XPath provides all of the types defined in XML Schema
         and many operators and functions over those types, XPath 1.0 has only
         node-sets and the three atomic types boolean,
         double, and string.
        

       

       

        

         XPath 1.0 has no conditional operator. An XQuery/XPath expression
         such as if ( hat ) then hat/@size else "no hat"
         has no XPath 1.0 equivalent.
        

       

       

        

         XPath 1.0 has no ordering comparison operator for strings. Both
         "cat" < "dog" and
         "cat" > "dog" are false, because each is a
         numeric comparison of two NaNs. In contrast,
         = and != do compare the strings
         as strings.
        

       

       

        

         XPath 1.0 blurs the distinction between
         value comparisons and
         general comparisons as XQuery/XPath define
         them.  Both sale/@hatsize = 7 and
         sale/@customer = "alice" are existentially
         quantified comparisons, true if there is
         any sale with the given value for the
         attribute, but sale/@taxable = false() is a
         value comparison to the
         effective boolean value of a whole node-set.
         It is true only if no sale has
         a taxable attribute at all.
        

       

       

        

         In the XQuery/XPath data model, a document
         node can have either document form (i.e., exactly one
         top-level element, with only comments and processing instructions
         outside of it) or content form (with those constraints
         relaxed). Its equivalent in XPath 1.0, the
         root node, can only be in document form.
         This is part of the reason an xml value passed as the
         context item to any PostgreSQL
         XPath-based function must be in document form.
        

       

      

     

     

      The differences highlighted here are not all of them. In XQuery and
      the 2.0 and later versions of XPath, there is an XPath 1.0 compatibility
      mode, and the W3C lists of
      function library changes
      and
      language changes
      applied in that mode offer a more complete (but still not exhaustive)
      account of the differences.  The compatibility mode cannot make the
      later languages exactly equivalent to XPath 1.0.
     

    

    

     
# Mappings between SQL and XML Data Types and Values

     

      In SQL:2006 and later, both directions of conversion between standard SQL
      data types and the XML Schema types are specified precisely. However, the
      rules are expressed using the types and semantics of XQuery/XPath, and
      have no direct application to the different data model of XPath 1.0.
     

     

      When PostgreSQL maps SQL data values to XML
      (as in xmlelement), or XML to SQL (as in the output
      columns of xmltable), except for a few cases
      treated specially, PostgreSQL simply assumes
      that the XML data type's XPath 1.0 string form will be valid as the
      text-input form of the SQL datatype, and conversely. This rule has the
      virtue of simplicity while producing, for many data types, results similar
      to the mappings specified in the standard.
     

     

      Where interoperability with other systems is a concern, for some data
      types, it may be necessary to use data type formatting functions (such
      as those in ) explicitly to
      produce the standard mappings.
     

    

   

   

    
# Incidental Limits of the Implementation

    

     This section concerns limits that are not inherent in the
     libxml2 library, but apply to the current
     implementation in PostgreSQL.
    

    

     
# Only BY VALUE Passing Mechanism Is Supported

     

      The SQL standard defines two passing mechanisms
      that apply when passing an XML argument from SQL to an XML function or
      receiving a result: BY REF, in which a particular XML
      value retains its node identity, and BY VALUE, in which
      the content of the XML is passed but node identity is not preserved. A
      mechanism can be specified before a list of parameters, as the default
      mechanism for all of them, or after any parameter, to override the
      default.
     

     

      To illustrate the difference, if
      x is an XML value, these two queries in
      an SQL:2006 environment would produce true and false, respectively:

```
SELECT XMLQUERY('$a is $b' PASSING BY REF x AS a, x AS b NULL ON EMPTY);
SELECT XMLQUERY('$a is $b' PASSING BY VALUE x AS a, x AS b NULL ON EMPTY);
```

     

     

      PostgreSQL will accept
      BY VALUE or BY REF in an
      XMLEXISTS or XMLTABLE
      construct, but it ignores them.  The xml data type holds
      a character-string serialized representation, so there is no node
      identity to preserve, and passing is always effectively BY
      VALUE.
     

    

    

     
# Cannot Pass Named Parameters to Queries

     

      The XPath-based functions support passing one parameter to serve as the
      XPath expression's context item, but do not support passing additional
      values to be available to the expression as named parameters.
     

    

    

     
# No XML(SEQUENCE) Type

     

      The PostgreSQL xml data type
      can only hold a value in DOCUMENT
      or CONTENT form.  An XQuery/XPath expression
      context item must be a single XML node or atomic value, but XPath 1.0
      further restricts it to be only an XML node, and has no node type
      allowing CONTENT.  The upshot is that a
      well-formed DOCUMENT is the only form of XML value
      that PostgreSQL can supply as an XPath
      context item.
