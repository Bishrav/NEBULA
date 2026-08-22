# XML Functions

  
   XML Functions
  

  

   The functions and function-like expressions described in this
   section operate on values of type xml.  See  for information about the xml
   type.  The function-like expressions xmlparse
   and xmlserialize for converting to and from
   type xml are documented there, not in this section.
  

  

   Use of most of these functions
   requires PostgreSQL to have been built
   with configure --with-libxml.
  

  

   
# Producing XML Content

   

    A set of functions and function-like expressions is available for
    producing XML content from SQL data.  As such, they are
    particularly suitable for formatting query results into XML
    documents for processing in client applications.
   

  

    
# xmltext

    
     xmltext
    

xmltext ( text ) xml

    

     The function xmltext returns an XML value with a single
     text node containing the input argument as its content. Predefined entities
     like ampersand (), left and right angle brackets
     (]]>), and quotation marks ()
     are escaped.
    

    

     Example:

```
');
         xmltext
-------------------------
 < foo & bar >
]]>
```

    

   

   

    
# xmlcomment

    
     xmlcomment
    

xmlcomment ( text ) xml

    

     The function xmlcomment creates an XML value
     containing an XML comment with the specified text as content.
     The text cannot contain -- or end with a
     -, otherwise the resulting construct
     would not be a valid XML comment.
     If the argument is null, the result is null.
    

    

     Example:

```

]]>
```

    

   

   

    
# xmlconcat

    
     xmlconcat
    

xmlconcat ( xml , ... ) xml

    

     The function xmlconcat concatenates a list
     of individual XML values to create a single value containing an
     XML content fragment.  Null values are omitted; the result is
     only null if there are no nonnull arguments.
    

    

     Example:

```
', 'foo');

      xmlconcat
----------------------
 foo
]]>
```

    

    

     XML declarations, if present, are combined as follows.  If all
     argument values have the same XML version declaration, that
     version is used in the result, else no version is used.  If all
     argument values have the standalone declaration value
     yes, then that value is used in the result.  If
     all argument values have a standalone declaration value and at
     least one is no, then that is used in the result.
     Else the result will have no standalone declaration.  If the
     result is determined to require a standalone declaration but no
     version declaration, a version declaration with version 1.0 will
     be used because XML requires an XML declaration to contain a
     version declaration.  Encoding declarations are ignored and
     removed in all cases.
    

    

     Example:

```
', '');

             xmlconcat
-----------------------------------
 
]]>
```

    

   

   

    
# xmlelement

   
    xmlelement
   

xmlelement ( NAME name , XMLATTRIBUTES ( attvalue  AS attname  , ... )  , content , ... ) xml

    

     The xmlelement expression produces an XML
     element with the given name, attributes, and content.
     The name
     and attname items shown in the syntax are
     simple identifiers, not values.  The attvalue
     and content items are expressions, which can
     yield any PostgreSQL data type.  The
     argument(s) within XMLATTRIBUTES generate attributes
     of the XML element; the content value(s) are
     concatenated to form its content.
    

    

     Examples:

```

SELECT xmlelement(NAME foo, xmlattributes('xyz' AS bar));

    xmlelement
------------------
 

SELECT xmlelement(NAME foo, xmlattributes(current_date AS bar), 'cont', 'ent');

             xmlelement
-------------------------------------
 content
]]>
```

    

    

     Element and attribute names that are not valid XML names are
     escaped by replacing the offending characters by the sequence
     _xHHHH_, where
     HHHH is the character's Unicode
     codepoint in hexadecimal notation.  For example:

```

]]>
```

    

    

     An explicit attribute name need not be specified if the attribute
     value is a column reference, in which case the column's name will
     be used as the attribute name by default.  In other cases, the
     attribute must be given an explicit name.  So this example is
     valid:

```
CREATE TABLE test (a xml, b xml);
SELECT xmlelement(NAME test, xmlattributes(a, b)) FROM test;
```

     But these are not:

```
SELECT xmlelement(NAME test, xmlattributes('constant'), a, b) FROM test;
SELECT xmlelement(NAME test, xmlattributes(func(a, b))) FROM test;
```

    

    

     Element content, if specified, will be formatted according to
     its data type.  If the content is itself of type xml,
     complex XML documents can be constructed.  For example:

```

]]>
```

     Content of other types will be formatted into valid XML character
     data.  This means in particular that the characters <, >,
     and & will be converted to entities.  Binary data (data type
     bytea) will be represented in base64 or hex
     encoding, depending on the setting of the configuration parameter
     .  The particular behavior for
     individual data types is expected to evolve in order to align the
     PostgreSQL mappings with those specified in SQL:2006 and later,
     as discussed in .
    

   

   

    
# xmlforest

   
    xmlforest
   

xmlforest ( content  AS name  , ... ) xml

    

     The xmlforest expression produces an XML
     forest (sequence) of elements using the given names and content.
     As for xmlelement,
     each name must be a simple identifier, while
     the content expressions can have any data
     type.
    

    

     Examples:

```
SELECT xmlforest('abc' AS foo, 123 AS bar);

          xmlforest
------------------------------
 <foo>abc</foo><bar>123</bar>

SELECT xmlforest(table_name, column_name)
FROM information_schema.columns
WHERE table_schema = 'pg_catalog';

                                xmlforest
------------------------------------&zwsp;-----------------------------------
 <table_name>pg_authid</table_name>&zwsp;<column_name>rolname</column_name>
 <table_name>pg_authid</table_name>&zwsp;<column_name>rolsuper</column_name>
 ...
```

     As seen in the second example, the element name can be omitted if
     the content value is a column reference, in which case the column
     name is used by default.  Otherwise, a name must be specified.
    

    

     Element names that are not valid XML names are escaped as shown
     for xmlelement above.  Similarly, content
     data is escaped to make valid XML content, unless it is already
     of type xml.
    

    

     Note that XML forests are not valid XML documents if they consist
     of more than one element, so it might be useful to wrap
     xmlforest expressions in
     xmlelement.
    

   

   

    
# xmlpi

   
    xmlpi
   

xmlpi ( NAME name , content  ) xml

    

     The xmlpi expression creates an XML
     processing instruction.
     As for xmlelement,
     the name must be a simple identifier, while
     the content expression can have any data type.
     The content, if present, must not contain the
     character sequence ?>.
    

    

     Example:

```

]]>
```

    

   

   

    
# xmlroot

   
    xmlroot
   

xmlroot ( xml, VERSION {text|NO VALUE} , STANDALONE {YES|NO|NO VALUE}  ) xml

    

     The xmlroot expression alters the properties
     of the root node of an XML value.  If a version is specified,
     it replaces the value in the root node's version declaration; if a
     standalone setting is specified, it replaces the value in the
     root node's standalone declaration.
    

    

```
abc'),
               version '1.0', standalone yes);

                xmlroot
----------------------------------------
 
 abc
]]>
```

    

   

   

    
# xmlagg

    
     xmlagg
    

xmlagg ( xml ) xml

    

     The function xmlagg is, unlike the other
     functions described here, an aggregate function.  It concatenates the
     input values to the aggregate function call,
     much like xmlconcat does, except that concatenation
     occurs across rows rather than across expressions in a single row.
     See  for additional information
     about aggregate functions.
    

    

     Example:

```
abc');
INSERT INTO test VALUES (2, '');
SELECT xmlagg(x) FROM test;
        xmlagg
----------------------
 abc
]]>
```

    

    

     To determine the order of the concatenation, an ORDER BY
     clause may be added to the aggregate call as described in
     . For example:

```
abc
]]>
```

    

    

     The following non-standard approach used to be recommended
     in previous versions, and may still be useful in specific
     cases:

```
abc
]]>
```

    

   

   

   

    
# XML Predicates

    

     The expressions described in this section check properties
     of xml values.
    

   

    
# IS DOCUMENT

    
     IS DOCUMENT
    

xml IS DOCUMENT boolean

    

     The expression IS DOCUMENT returns true if the
     argument XML value is a proper XML document, false if it is not
     (that is, it is a content fragment), or null if the argument is
     null.  See  about the difference
     between documents and content fragments.
    

   

   

    
# IS NOT DOCUMENT

    
     IS NOT DOCUMENT
    

xml IS NOT DOCUMENT boolean

    

     The expression IS NOT DOCUMENT returns false if the
     argument XML value is a proper XML document, true if it is not (that is,
     it is a content fragment), or null if the argument is null.
    

   

   

    
# XMLEXISTS

    
     XMLEXISTS
    

XMLEXISTS ( text PASSING BY {REF|VALUE} xml BY {REF|VALUE} ) boolean

    

     The function xmlexists evaluates an XPath 1.0
     expression (the first argument), with the passed XML value as its context
     item.  The function returns false if the result of that evaluation
     yields an empty node-set, true if it yields any other value.  The
     function returns null if any argument is null.  A nonnull value
     passed as the context item must be an XML document, not a content
     fragment or any non-XML value.
    

    

     Example:
     
```
TorontoOttawa');

 xmlexists
------------
 t
(1 row)
]]>
```

    

    

     The BY REF and BY VALUE clauses
     are accepted in PostgreSQL, but are ignored,
     as discussed in .
    

    

     In the SQL standard, the xmlexists function
     evaluates an expression in the XML Query language,
     but PostgreSQL allows only an XPath 1.0
     expression, as discussed in
     .
    

   

   

    
# xml_is_well_formed

    
     xml_is_well_formed
    

    
     xml_is_well_formed_document
    

    
     xml_is_well_formed_content
    

xml_is_well_formed ( text ) boolean
xml_is_well_formed_document ( text ) boolean
xml_is_well_formed_content ( text ) boolean

    

     These functions check whether a text string represents
     well-formed XML, returning a Boolean result.
     xml_is_well_formed_document checks for a well-formed
     document, while xml_is_well_formed_content checks
     for well-formed content.  xml_is_well_formed does
     the former if the  configuration
     parameter is set to DOCUMENT, or the latter if it is set to
     CONTENT.  This means that
     xml_is_well_formed is useful for seeing whether
     a simple cast to type xml will succeed, whereas the other two
     functions are useful for seeing whether the corresponding variants of
     XMLPARSE will succeed.
    

    

     Examples:

```
');
 xml_is_well_formed
--------------------
 f
(1 row)

SELECT xml_is_well_formed('');
 xml_is_well_formed
--------------------
 t
(1 row)

SET xmloption TO CONTENT;
SELECT xml_is_well_formed('abc');
 xml_is_well_formed
--------------------
 t
(1 row)

SELECT xml_is_well_formed_document('bar');
 xml_is_well_formed_document
-----------------------------
 t
(1 row)

SELECT xml_is_well_formed_document('bar');
 xml_is_well_formed_document
-----------------------------
 f
(1 row)
]]>
```

     The last example shows that the checks include whether
     namespaces are correctly matched.
    

   

  

  

   
# Processing XML

   

    To process values of data type xml, PostgreSQL offers
    the functions xpath and
    xpath_exists, which evaluate XPath 1.0
    expressions, and the XMLTABLE
    table function.
   

   

    
# xpath

    
     XPath
    

xpath ( 
xpath
 text, 
xml
 xml , 
nsarray
 text[]  ) xml[]

    

     The function xpath evaluates the XPath 1.0
     expression 
xpath
 (given as text)
     against the XML value
     
xml
.  It returns an array of XML values
     corresponding to the node-set produced by the XPath expression.
     If the XPath expression returns a scalar value rather than a node-set,
     a single-element array is returned.
    

    

     The second argument must be a well formed XML document. In particular,
     it must have a single root node element.
    

    

     The optional third argument of the function is an array of namespace
     mappings.  This array should be a two-dimensional text array with
     the length of the second axis being equal to 2 (i.e., it should be an
     array of arrays, each of which consists of exactly 2 elements).
     The first element of each array entry is the namespace name (alias), the
     second the namespace URI. It is not required that aliases provided in
     this array be the same as those being used in the XML document itself (in
     other words, both in the XML document and in the xpath
     function context, aliases are local).
    

    

     Example:

```
test',
             ARRAY[ARRAY['my', 'http://example.com']]);

 xpath
--------
 {test}
(1 row)
]]>
```

    

    

     To deal with default (anonymous) namespaces, do something like this:

```
test',
             ARRAY[ARRAY['mydefns', 'http://example.com']]);

 xpath
--------
 {test}
(1 row)
]]>
```

    

   

   

    
# xpath_exists

    
     xpath_exists
    

xpath_exists ( 
xpath
 text, 
xml
 xml , 
nsarray
 text[]  ) boolean

    

     The function xpath_exists is a specialized form
     of the xpath function.  Instead of returning the
     individual XML values that satisfy the XPath 1.0 expression, this function
     returns a Boolean indicating whether the query was satisfied or not
     (specifically, whether it produced any value other than an empty node-set).
     This function is equivalent to the XMLEXISTS predicate,
     except that it also offers support for a namespace mapping argument.
    

    

     Example:

```
test',
                     ARRAY[ARRAY['my', 'http://example.com']]);

 xpath_exists
--------------
 t
(1 row)
]]>
```

    

   

   

    
# xmltable

    
     xmltable
    

    
     table function
     XMLTABLE
    

XMLTABLE (
     XMLNAMESPACES ( namespace_uri AS namespace_name , ... ), 
    row_expression PASSING BY {REF|VALUE} document_expression BY {REF|VALUE}
    COLUMNS name { type PATH column_expression DEFAULT default_expression NOT NULL | NULL
                  | FOR ORDINALITY }
            , ...
) setof record

    

     The xmltable expression produces a table based
     on an XML value, an XPath filter to extract rows, and a
     set of column definitions.
     Although it syntactically resembles a function, it can only appear
     as a table in a query's FROM clause.
    

    

     The optional XMLNAMESPACES clause gives a
     comma-separated list of namespace definitions, where
     each namespace_uri is a text
     expression and each namespace_name is a simple
     identifier.  It specifies the XML namespaces used in the document and
     their aliases. A default namespace specification is not currently
     supported.
    

    

     The required row_expression argument is an
     XPath 1.0 expression (given as text) that is evaluated,
     passing the XML value document_expression as
     its context item, to obtain a set of XML nodes. These nodes are what
     xmltable transforms into output rows. No rows
     will be produced if the document_expression
     is null, nor if the row_expression produces
     an empty node-set or any value other than a node-set.
    

    

     document_expression provides the context
     item for the row_expression. It must be a
     well-formed XML document; fragments/forests are not accepted.
     The BY REF and BY VALUE clauses
     are accepted but ignored, as discussed in
     .
    

    

     In the SQL standard, the xmltable function
     evaluates expressions in the XML Query language,
     but PostgreSQL allows only XPath 1.0
     expressions, as discussed in
     .
    

    

     The required COLUMNS clause specifies the
     column(s) that will be produced in the output table.
     See the syntax summary above for the format.
     A name is required for each column, as is a data type
     (unless FOR ORDINALITY is specified, in which case
     type integer is implicit).  The path, default and
     nullability clauses are optional.
    

    

     A column marked FOR ORDINALITY will be populated
     with row numbers, starting with 1, in the order of nodes retrieved from
     the row_expression's result node-set.
     At most one column may be marked FOR ORDINALITY.
    

    
     

      XPath 1.0 does not specify an order for nodes in a node-set, so code
      that relies on a particular order of the results will be
      implementation-dependent.  Details can be found in
      .
     

    

    

     The column_expression for a column is an
     XPath 1.0 expression that is evaluated for each row, with the current
     node from the row_expression result as its
     context item, to find the value of the column.  If
     no column_expression is given, then the
     column name is used as an implicit path.
    

    

     If a column's XPath expression returns a non-XML value (which is limited
     to string, boolean, or double in XPath 1.0) and the column has a
     PostgreSQL type other than xml, the column will be set
     as if by assigning the value's string representation to the PostgreSQL
     type.  (If the value is a boolean, its string representation is taken
     to be 1 or 0 if the output
     column's type category is numeric, otherwise true or
     false.)
    

    

     If a column's XPath expression returns a non-empty set of XML nodes
     and the column's PostgreSQL type is xml, the column will
     be assigned the expression result exactly, if it is of document or
     content form.
     
      

       A result containing more than one element node at the top level, or
       non-whitespace text outside of an element, is an example of content form.
       An XPath result can be of neither form, for example if it returns an
       attribute node selected from the element that contains it. Such a result
       will be put into content form with each such disallowed node replaced by
       its string value, as defined for the XPath 1.0
       string function.
      

     
    

    

     A non-XML result assigned to an xml output column produces
     content, a single text node with the string value of the result.
     An XML result assigned to a column of any other type may not have more than
     one node, or an error is raised. If there is exactly one node, the column
     will be set as if by assigning the node's string
     value (as defined for the XPath 1.0 string function)
     to the PostgreSQL type.
    

    

     The string value of an XML element is the concatenation, in document order,
     of all text nodes contained in that element and its descendants. The string
     value of an element with no descendant text nodes is an
     empty string (not NULL).
     Any xsi:nil attributes are ignored.
     Note that the whitespace-only text() node between two non-text
     elements is preserved, and that leading whitespace on a text()
     node is not flattened.
     The XPath 1.0 string function may be consulted for the
     rules defining the string value of other XML node types and non-XML values.
    

    

     The conversion rules presented here are not exactly those of the SQL
     standard, as discussed in .
    

    

     If the path expression returns an empty node-set
     (typically, when it does not match)
     for a given row, the column will be set to NULL, unless
     a default_expression is specified; then the
     value resulting from evaluating that expression is used.
    

    

     A default_expression, rather than being
     evaluated immediately when xmltable is called,
     is evaluated each time a default is needed for the column.
     If the expression qualifies as stable or immutable, the repeat
     evaluation may be skipped.
     This means that you can usefully use volatile functions like
     nextval in
     default_expression.
    

    

     Columns may be marked NOT NULL. If the
     column_expression for a NOT
     NULL column does not match anything and there is
     no DEFAULT or
     the default_expression also evaluates to null,
     an error is reported.
    

    

     Examples:
  
```

  
    AU
    Australia
  
  
    JP
    Japan
    Shinzo Abe
    145935
  
  
    SG
    Singapore
    697
  

$$ AS data;

SELECT xmltable.*
  FROM xmldata,
       XMLTABLE('//ROWS/ROW'
                PASSING data
                COLUMNS id int PATH '@id',
                        ordinality FOR ORDINALITY,
                        "COUNTRY_NAME" text,
                        country_id text PATH 'COUNTRY_ID',
                        size_sq_km float PATH 'SIZE[@unit = "sq_km"]',
                        size_other text PATH
                             'concat(SIZE[@unit!="sq_km"], " ", SIZE[@unit!="sq_km"]/@unit)',
                        premier_name text PATH 'PREMIER_NAME' DEFAULT 'not specified');

 id | ordinality | COUNTRY_NAME | country_id | size_sq_km |  size_other  | premier_name
----+------------+--------------+------------+------------+--------------+---------------
  1 |          1 | Australia    | AU         |            |              | not specified
  5 |          2 | Japan        | JP         |            | 145935 sq_mi | Shinzo Abe
  6 |          3 | Singapore    | SG         |        697 |              | not specified
]]>
```

     The following example shows concatenation of multiple text() nodes,
     usage of the column name as XPath filter, and the treatment of whitespace,
     XML comments and processing instructions:

  
```

     Hello2a2   bbbxxxCC  
  
$$ AS data;

SELECT xmltable.*
  FROM xmlelements, XMLTABLE('/root' PASSING data COLUMNS element text);
         element
-------------------------
   Hello2a2   bbbxxxCC
]]>
```

    

    

     The following example illustrates how
     the XMLNAMESPACES clause can be used to specify
     a list of namespaces
     used in the XML document as well as in the XPath expressions:

  
```

 
 
 
'::xml)
)
SELECT xmltable.*
  FROM XMLTABLE(XMLNAMESPACES('http://example.com/myns' AS x,
                              'http://example.com/b' AS "B"),
             '/x:example/x:item'
                PASSING (SELECT data FROM xmldata)
                COLUMNS foo int PATH '@foo',
                  bar int PATH '@B:bar');
 foo | bar
-----+-----
   1 |   2
   3 |   4
   4 |   5
(3 rows)
]]>
```

    

   

  

  

   
# Mapping Tables to XML

   
    XML export
   

   

    The following functions map the contents of relational tables to
    XML values.  They can be thought of as XML export functionality:

table_to_xml ( 
table
 regclass, 
nulls
 boolean,
               
tableforest
 boolean, 
targetns
 text ) xml
query_to_xml ( 
query
 text, 
nulls
 boolean,
               
tableforest
 boolean, 
targetns
 text ) xml
cursor_to_xml ( 
cursor
 refcursor, 
count
 integer, 
nulls
 boolean,
                
tableforest
 boolean, 
targetns
 text ) xml

   

   

    table_to_xml maps the content of the named
    table, passed as parameter 
table
.  The
    regclass type accepts strings identifying tables using the
    usual notation, including optional schema qualification and
    double quotes (see  for details).
    query_to_xml executes the
    query whose text is passed as parameter
    
query
 and maps the result set.
    cursor_to_xml fetches the indicated number of
    rows from the cursor specified by the parameter
    
cursor
.  This variant is recommended if
    large tables have to be mapped, because the result value is built
    up in memory by each function.
   

   

    If 
tableforest
 is false, then the resulting
    XML document looks like this:

```

  
    data
    data
  

  
    ...
  

  ...

]]>
```

    If 
tableforest
 is true, the result is an
    XML content fragment that looks like this:

```

  data
  data

  ...

...
]]>
```

    If no table name is available, that is, when mapping a query or a
    cursor, the string table is used in the first
    format, row in the second format.
   

   

    The choice between these formats is up to the user.  The first
    format is a proper XML document, which will be important in many
    applications.  The second format tends to be more useful in the
    cursor_to_xml function if the result values are to be
    reassembled into one document later on.  The functions for
    producing XML content discussed above, in particular
    xmlelement, can be used to alter the results
    to taste.
   

   

    The data values are mapped in the same way as described for the
    function xmlelement above.
   

   

    The parameter 
nulls
 determines whether null
    values should be included in the output.  If true, null values in
    columns are represented as:

```

]]>
```

    where xsi is the XML namespace prefix for XML
    Schema Instance.  An appropriate namespace declaration will be
    added to the result value.  If false, columns containing null
    values are simply omitted from the output.
   

   

    The parameter 
targetns
 specifies the
    desired XML namespace of the result.  If no particular namespace
    is wanted, an empty string should be passed.
   

   

    The following functions return XML Schema documents describing the
    mappings performed by the corresponding functions above:

table_to_xmlschema ( 
table
 regclass, 
nulls
 boolean,
                     
tableforest
 boolean, 
targetns
 text ) xml
query_to_xmlschema ( 
query
 text, 
nulls
 boolean,
                     
tableforest
 boolean, 
targetns
 text ) xml
cursor_to_xmlschema ( 
cursor
 refcursor, 
nulls
 boolean,
                      
tableforest
 boolean, 
targetns
 text ) xml

    It is essential that the same parameters are passed in order to
    obtain matching XML data mappings and XML Schema documents.
   

   

    The following functions produce XML data mappings and the
    corresponding XML Schema in one document (or forest), linked
    together.  They can be useful where self-contained and
    self-describing results are wanted:

table_to_xml_and_xmlschema ( 
table
 regclass, 
nulls
 boolean,
                             
tableforest
 boolean, 
targetns
 text ) xml
query_to_xml_and_xmlschema ( 
query
 text, 
nulls
 boolean,
                             
tableforest
 boolean, 
targetns
 text ) xml

   

   

    In addition, the following functions are available to produce
    analogous mappings of entire schemas or the entire current
    database:

schema_to_xml ( 
schema
 name, 
nulls
 boolean,
                
tableforest
 boolean, 
targetns
 text ) xml
schema_to_xmlschema ( 
schema
 name, 
nulls
 boolean,
                      
tableforest
 boolean, 
targetns
 text ) xml
schema_to_xml_and_xmlschema ( 
schema
 name, 
nulls
 boolean,
                              
tableforest
 boolean, 
targetns
 text ) xml

database_to_xml ( 
nulls
 boolean,
                  
tableforest
 boolean, 
targetns
 text ) xml
database_to_xmlschema ( 
nulls
 boolean,
                        
tableforest
 boolean, 
targetns
 text ) xml
database_to_xml_and_xmlschema ( 
nulls
 boolean,
                                
tableforest
 boolean, 
targetns
 text ) xml

    These functions ignore tables that are not readable by the current user.
    The database-wide functions additionally ignore schemas that the current
    user does not have USAGE (lookup) privilege for.
   

   

    Note that these potentially produce a lot of data, which needs to
    be built up in memory.  When requesting content mappings of large
    schemas or databases, it might be worthwhile to consider mapping the
    tables separately instead, possibly even through a cursor.
   

   

    The result of a schema content mapping looks like this:

```

table1-mapping

table2-mapping

...

]]>
```

    where the format of a table mapping depends on the
    
tableforest
 parameter as explained above.
   

   

    The result of a database content mapping looks like this:

```

  ...

  ...

...

]]>
```

    where the schema mapping is as above.
   

   

    As an example of using the output produced by these functions,
     shows an XSLT stylesheet that
    converts the output of
    table_to_xml_and_xmlschema to an HTML
    document containing a tabular rendition of the table data.  In a
    similar manner, the results from these functions can be
    converted into other XML-based formats.
   

   
    
# XSLT Stylesheet for Converting SQL/XML Output to HTML

```

  

  
    
    
    

    
      
        
# 

      
      
        
          
            
              
            
          

          
            
              
                
              
            
          
        
      
    
  

]]>
```
