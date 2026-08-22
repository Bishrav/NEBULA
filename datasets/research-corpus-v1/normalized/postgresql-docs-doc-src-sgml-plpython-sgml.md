# PL/Python &mdash; Python Procedural Language

 PL/Python
 Python

 

  The PL/Python procedural language allows
  PostgreSQL functions and procedures to be written in the
  Python language.
 

 

  To install PL/Python in a particular database, use
  CREATE EXTENSION plpython3u.
 

  
   

    If a language is installed into template1, all subsequently
    created databases will have the language installed automatically.
   

  

 

  PL/Python is only available as an untrusted language, meaning
  it does not offer any way of restricting what users can do in it and
  is therefore named plpython3u.  A trusted
  variant plpython might become available in the future
  if a secure execution mechanism is developed in Python.  The
  writer of a function in untrusted PL/Python must take care that the
  function cannot be used to do anything unwanted, since it will be
  able to do anything that could be done by a user logged in as the
  database administrator.  Only superusers can create functions in
  untrusted languages such as plpython3u.
 

 
  

   Users of source packages must specially enable the build of
   PL/Python during the installation process.  (Refer to the
   installation instructions for more information.)  Users of binary
   packages might find PL/Python in a separate subpackage.
  

 

 

  
# PL/Python Functions

  

   Functions in PL/Python are declared via the
   standard  syntax:

```
CREATE FUNCTION funcname (argument-list)
  RETURNS return-type
AS $$
  # PL/Python function body
$$ LANGUAGE plpython3u;
```

  

  

   The body of a function is simply a Python script. When the function
   is called, its arguments are passed as elements of the list
   args; named arguments are also passed as
   ordinary variables to the Python script.  Use of named arguments is
   usually more readable.  The result is returned from the Python code
   in the usual way, with return or
   yield (in case of a result-set statement).  If
   you do not provide a return value, Python returns the default
   None. PL/Python translates
   Python's None into the SQL null value.  In a procedure,
   the result from the Python code must be None (typically
   achieved by ending the procedure without a return
   statement or by using a return statement without
   argument); otherwise, an error will be raised.
  

  

   For example, a function to return the greater of two integers can be
   defined as:

```
CREATE FUNCTION pymax (a integer, b integer)
  RETURNS integer
AS $$
  if a > b:
    return a
  return b
$$ LANGUAGE plpython3u;
```

   The Python code that is given as the body of the function definition
   is transformed into a Python function. For example, the above results in:

```
def __plpython_procedure_pymax_23456():
  if a > b:
    return a
  return b
```

   assuming that 23456 is the OID assigned to the function by
   PostgreSQL.
  

  

   The arguments are set as global variables.  Because of the scoping
   rules of Python, this has the subtle consequence that an argument
   variable cannot be reassigned inside the function to the value of
   an expression that involves the variable name itself, unless the
   variable is redeclared as global in the block.  For example, the
   following won't work:

```
CREATE FUNCTION pystrip(x text)
  RETURNS text
AS $$
  x = x.strip()  # error
  return x
$$ LANGUAGE plpython3u;
```

   because assigning to x
   makes x a local variable for the entire block,
   and so the x on the right-hand side of the
   assignment refers to a not-yet-assigned local
   variable x, not the PL/Python function
   parameter.  Using the global statement, this can
   be made to work:

```
CREATE FUNCTION pystrip(x text)
  RETURNS text
AS $$
  global x
  x = x.strip()  # ok now
  return x
$$ LANGUAGE plpython3u;
```

   But it is advisable not to rely on this implementation detail of
   PL/Python.  It is better to treat the function parameters as
   read-only.
  

 

 

  
# Data Values

  

   Generally speaking, the aim of PL/Python is to provide
   a natural mapping between the PostgreSQL and the
   Python worlds.  This informs the data mapping rules described
   below.
  

  

   
# Data Type Mapping

   

    When a PL/Python function is called, its arguments are converted from
    their PostgreSQL data type to a corresponding Python type:

    

     

      

       PostgreSQL boolean is converted to Python bool.
      

     

     

      

       PostgreSQL smallint, int, bigint
       and oid are converted to Python int.
      

     

     

      

       PostgreSQL real and double are converted to
       Python float.
      

     

     

      

       PostgreSQL numeric is converted to
       Python Decimal.  This type is imported from
       the cdecimal package if that is available.
       Otherwise,
       decimal.Decimal from the standard library will be
       used.  cdecimal is significantly faster
       than decimal.  In Python 3.3 and up,
       however, cdecimal has been integrated into the
       standard library under the name decimal, so there is
       no longer any difference.
      

     

     

      

       PostgreSQL bytea is converted to Python bytes.
      

     

     

      

       All other data types, including the PostgreSQL character string types,
       are converted to a Python str (in Unicode like all Python
       strings).
      

     

     

      

       For nonscalar data types, see below.
      

     

    

   

   

    When a PL/Python function returns, its return value is converted to the
    function's declared PostgreSQL return data type as follows:

    

     

      

       When the PostgreSQL return type is boolean, the
       return value will be evaluated for truth according to the
       Python rules.  That is, 0 and empty string
       are false, but notably 'f' is true.
      

     

     

      

       When the PostgreSQL return type is bytea, the return value
       will be converted to Python bytes using the respective
       Python built-ins, with the result being converted to
       bytea.
      

     

     

      

       For all other PostgreSQL return types, the return value is converted
       to a string using the Python built-in str, and the
       result is passed to the input function of the PostgreSQL data type.
       (If the Python value is a float, it is converted using
       the repr built-in instead of str, to
       avoid loss of precision.)
      

      

       Strings are automatically converted to the PostgreSQL server encoding
       when they are passed to PostgreSQL.
      

     

     

      

       For nonscalar data types, see below.
      

     

    

    Note that logical mismatches between the declared PostgreSQL
    return type and the Python data type of the actual return object
    are not flagged; the value will be converted in any case.
   

  

  

   
# Null, None

  

   If an SQL null valuenull valuein PL/Python is passed to a
   function, the argument value will appear as None in
   Python. For example, the function definition of pymax
   shown in  will return the wrong answer for null
   inputs. We could add STRICT to the function definition
   to make PostgreSQL do something more reasonable:
   if a null value is passed, the function will not be called at all,
   but will just return a null result automatically. Alternatively,
   we could check for null inputs in the function body:

```
CREATE FUNCTION pymax (a integer, b integer)
  RETURNS integer
AS $$
  if (a is None) or (b is None):
    return None
  if a > b:
    return a
  return b
$$ LANGUAGE plpython3u;
```

   As shown above, to return an SQL null value from a PL/Python
   function, return the value None. This can be done whether the
   function is strict or not.
  

  

  

   
# Arrays, Lists

  

   SQL array values are passed into PL/Python as a Python list.  To
   return an SQL array value out of a PL/Python function, return a
   Python list:

```
CREATE FUNCTION return_arr()
  RETURNS int[]
AS $$
return [1, 2, 3, 4, 5]
$$ LANGUAGE plpython3u;

SELECT return_arr();
 return_arr
-------------
 {1,2,3,4,5}
(1 row)
```

   Multidimensional arrays are passed into PL/Python as nested Python lists.
   A 2-dimensional array is a list of lists, for example. When returning
   a multi-dimensional SQL array out of a PL/Python function, the inner
   lists at each level must all be of the same size. For example:

```
CREATE FUNCTION test_type_conversion_array_int4(x int4[]) RETURNS int4[] AS $$
plpy.info(x, type(x))
return x
$$ LANGUAGE plpython3u;

SELECT * FROM test_type_conversion_array_int4(ARRAY[[1,2,3],[4,5,6]]);
INFO:  ([[1, 2, 3], [4, 5, 6]], <type 'list'>)
 test_type_conversion_array_int4
---------------------------------
 {{1,2,3},{4,5,6}}
(1 row)
```

   Other Python sequences, like tuples, are also accepted for
   backwards-compatibility with PostgreSQL versions 9.6 and below, when
   multi-dimensional arrays were not supported. However, they are always
   treated as one-dimensional arrays, because they are ambiguous with
   composite types. For the same reason, when a composite type is used in a
   multi-dimensional array, it must be represented by a tuple, rather than a
   list.
  

  

   Note that in Python, strings are sequences, which can have
   undesirable effects that might be familiar to Python programmers:

```
CREATE FUNCTION return_str_arr()
  RETURNS varchar[]
AS $$
return "hello"
$$ LANGUAGE plpython3u;

SELECT return_str_arr();
 return_str_arr
----------------
 {h,e,l,l,o}
(1 row)
```

  

  

  

   
# Composite Types

  

   Composite-type arguments are passed to the function as Python mappings. The
   element names of the mapping are the attribute names of the composite type.
   If an attribute in the passed row has the null value, it has the value
   None in the mapping. Here is an example:

```
CREATE TABLE employee (
  name text,
  salary integer,
  age integer
);

CREATE FUNCTION overpaid (e employee)
  RETURNS boolean
AS $$
  if e["salary"] > 200000:
    return True
  if (e["age"] < 30) and (e["salary"] > 100000):
    return True
  return False
$$ LANGUAGE plpython3u;
```

  

  

   There are multiple ways to return row or composite types from a Python
   function. The following examples assume we have:

```
CREATE TYPE named_value AS (
  name   text,
  value  integer
);
```

   A composite result can be returned as a:

   

    

     
Sequence type (a tuple or list, but not a set because
     it is not indexable)

     

      

       Returned sequence objects must have the same number of items as the
       composite result type has fields. The item with index 0 is assigned to
       the first field of the composite type, 1 to the second and so on. For
       example:

```
CREATE FUNCTION make_pair (name text, value integer)
  RETURNS named_value
AS $$
  return ( name, value )
  # or alternatively, as list: return [ name, value ]
$$ LANGUAGE plpython3u;
```

       To return an SQL null for any column, insert None at
       the corresponding position.
      

      

       When an array of composite types is returned, it cannot be returned as a list,
       because it is ambiguous whether the Python list represents a composite type,
       or another array dimension.
      

     

    

    

     
Mapping (dictionary)

     

      

       The value for each result type column is retrieved from the mapping
       with the column name as key. Example:

```
CREATE FUNCTION make_pair (name text, value integer)
  RETURNS named_value
AS $$
  return { "name": name, "value": value }
$$ LANGUAGE plpython3u;
```

       Any extra dictionary key/value pairs are ignored. Missing keys are
       treated as errors.
       To return an SQL null value for any column, insert
       None with the corresponding column name as the key.
      

     

    

    

     
Object (any object providing method __getattr__)

     

      

       This works the same as a mapping.
       Example:

```
CREATE FUNCTION make_pair (name text, value integer)
  RETURNS named_value
AS $$
  class named_value:
    def __init__ (self, n, v):
      self.name = n
      self.value = v
  return named_value(name, value)

  # or simply
  class nv: pass
  nv.name = name
  nv.value = value
  return nv
$$ LANGUAGE plpython3u;
```

      

     

    

   

  

   

    Functions with OUT parameters are also supported.  For example:

```
CREATE FUNCTION multiout_simple(OUT i integer, OUT j integer) AS $$
return (1, 2)
$$ LANGUAGE plpython3u;

SELECT * FROM multiout_simple();
```

   

   

    Output parameters of procedures are passed back the same way.  For example:

```
CREATE PROCEDURE python_triple(INOUT a integer, INOUT b integer) AS $$
return (a * 3, b * 3)
$$ LANGUAGE plpython3u;

CALL python_triple(5, 10);
```

   

  

  

   
# Set-Returning Functions

  

   A PL/Python function can also return sets of
   scalar or composite types. There are several ways to achieve this because
   the returned object is internally turned into an iterator. The following
   examples assume we have composite type:

```
CREATE TYPE greeting AS (
  how text,
  who text
);
```

   A set result can be returned from a:

   

    

     
Sequence type (tuple, list, set)

     

      

```
CREATE FUNCTION greet (how text)
  RETURNS SETOF greeting
AS $$
  # return tuple containing lists as composite types
  # all other combinations work also
  return ( [ how, "World" ], [ how, "PostgreSQL" ], [ how, "PL/Python" ] )
$$ LANGUAGE plpython3u;
```

      

     

    

    

     
Iterator (any object providing __iter__ and
      __next__ methods)

     

      

```
CREATE FUNCTION greet (how text)
  RETURNS SETOF greeting
AS $$
  class producer:
    def __init__ (self, how, who):
      self.how = how
      self.who = who
      self.ndx = -1

    def __iter__ (self):
      return self

    def __next__(self):
      self.ndx += 1
      if self.ndx == len(self.who):
        raise StopIteration
      return ( self.how, self.who[self.ndx] )

  return producer(how, [ "World", "PostgreSQL", "PL/Python" ])
$$ LANGUAGE plpython3u;
```

      

     

    

    

     
Generator (yield)

     

      

```
CREATE FUNCTION greet (how text)
  RETURNS SETOF greeting
AS $$
  for who in [ "World", "PostgreSQL", "PL/Python" ]:
    yield ( how, who )
$$ LANGUAGE plpython3u;
```

      

     

    

   

  

   

    Set-returning functions with OUT parameters
    (using RETURNS SETOF record) are also
    supported.  For example:

```
CREATE FUNCTION multiout_simple_setof(n integer, OUT integer, OUT integer) RETURNS SETOF record AS $$
return [(1, 2)] * n
$$ LANGUAGE plpython3u;

SELECT * FROM multiout_simple_setof(3);
```

   

  

 

 

  
# Sharing Data

  

   The global dictionary SD is available to store
   private data between repeated calls to the same function.
   The global dictionary GD is public data,
   that is available to all Python functions within a session;  use with
   care.global data
   in PL/Python
  

  

   Each function gets its own execution environment in the
   Python interpreter, so that global data and function arguments from
   myfunc are not available to
   myfunc2.  The exception is the data in the
   GD dictionary, as mentioned above.
  

 

 

  
# Anonymous Code Blocks

  

   PL/Python also supports anonymous code blocks called with the
    statement:

```
DO $$
    # PL/Python code
$$ LANGUAGE plpython3u;
```

   An anonymous code block receives no arguments, and whatever value it
   might return is discarded.  Otherwise it behaves just like a function.
  

 

 

  
# Trigger Functions

  
   trigger
   in PL/Python
  

  

   PL/Python can be used to define trigger
   functions.
   PostgreSQL requires that a function that is to
   be called as a trigger must be declared as a function with no arguments and
   a return type of trigger.
  

  

   When a function is used as a trigger, the dictionary
   TD contains trigger-related values:
   

    

     
TD["event"]

     

      

       contains the event as a string:
       INSERT, UPDATE,
       DELETE, or TRUNCATE.
      

     

    

    

     
TD["when"]

     

      

       contains one of BEFORE, AFTER, or
       INSTEAD OF.
      

     

    

    

     
TD["level"]

     

      

       contains ROW or STATEMENT.
      

     

    

    

     
TD["new"]

     
TD["old"]

     

      

       For a row-level trigger, one or both of these fields contain
       the respective trigger rows, depending on the trigger event.
      

     

    

    

     
TD["name"]

     

      

       contains the trigger name.
      

     

    

    

     
TD["table_name"]

     

      

       contains the name of the table on which the trigger occurred.
      

     

    

    

     
TD["table_schema"]

     

      

       contains the schema of the table on which the trigger occurred.
      

     

    

    

     
TD["relid"]

     

      

       contains the OID of the table on which the trigger occurred.
      

     

    

    

     
TD["args"]

     

      

       If the CREATE TRIGGER command
       included arguments, they are available in TD["args"][0] to
       TD["args"][n-1].
      

     

    

   

  

  

   If TD["when"] is BEFORE or
   INSTEAD OF and
   TD["level"] is ROW, you can
   return None or "OK" from the
   Python function to indicate the row is unmodified,
   "SKIP" to abort the event, or if TD["event"]
   is INSERT or UPDATE you can return
   "MODIFY" to indicate you've modified the new row.
   Otherwise the return value is ignored.
  

 

 

  
# Event Trigger Functions

  
   event trigger
   in PL/Python
  

  

   PL/Python can be used to define event triggers
   (see also ).
   PostgreSQL requires that a function that is to
   be called as an event trigger must be declared as a function with no
   arguments and a return type of event_trigger.
  

  

   When a function is used as an event trigger, the dictionary
   TD contains trigger-related values:

   

    

     
TD["event"]

     

      

       The event the trigger was fired for, as a string, for example
       ddl_command_start.
      

     

    

    

     
TD["tag"]

     

      

       The command tag for which the trigger was fired, as a string, for
       example DROP TABLE.
      

     

    

   

  

  

    shows an example of an
   event trigger function in PL/Python.
  

  
   
# A PL/Python Event Trigger Function

   

    This example trigger simply raises a NOTICE message
    each time a supported command is executed.
   

```
CREATE OR REPLACE FUNCTION pysnitch() RETURNS event_trigger
LANGUAGE plpython3u
AS $$
  plpy.notice("TD[event] => " + TD["event"] + " ; TD[tag] => " + TD["tag"]);
$$;

CREATE EVENT TRIGGER pysnitch ON ddl_command_start EXECUTE FUNCTION pysnitch();
```

  
 

 

  
# Database Access

  

   The PL/Python language module automatically imports a Python module
   called plpy.  The functions and constants in
   this module are available to you in the Python code as
   plpy.foo.
  

  

    
# Database Access Functions

  

   The plpy module provides several functions to execute
   database commands:
  

  

   

    
plpy.execute(query [, limit])

    

     

      Calling plpy.execute with a query string and an
      optional row limit argument causes that query to be run and the result to
      be returned in a result object.
     

     

      If limit is specified and is greater than
      zero, then plpy.execute retrieves at
      most limit rows, much as if the query
      included a LIMIT
      clause.  Omitting limit or specifying it as
      zero results in no row limit.
     

     

      The result object emulates a list or dictionary object.  The result
      object can be accessed by row number and column name.  For example:

```
rv = plpy.execute("SELECT * FROM my_table", 5)
```

      returns up to 5 rows from my_table.  If
      my_table has a column
      my_column, it would be accessed as:

```
foo = rv[i]["my_column"]
```

      The number of rows returned can be obtained using the built-in
      len function.
     

     

      The result object has these additional methods:
      

       

        
nrows()

        

         

          Returns the number of rows processed by the command.  Note that this
          is not necessarily the same as the number of rows returned.  For
          example, an UPDATE command will set this value but
          won't return any rows (unless RETURNING is used).
         

        

       

       

        
status()

        

         

          The SPI_execute() return value.
         

        

       

       

        
colnames()

        
coltypes()

        
coltypmods()

        

         

          Return a list of column names, list of column type OIDs, and list of
          type-specific type modifiers for the columns, respectively.
         

         

          These methods raise an exception when called on a result object from
          a command that did not produce a result set, e.g.,
          UPDATE without RETURNING, or
          DROP TABLE.  But it is OK to use these methods on
          a result set containing zero rows.
         

        

       

       

        
__str__()

        

         

          The standard __str__ method is defined so that it
          is possible for example to debug query execution results
          using plpy.debug(rv).
         

        

       

      

     

     

      The result object can be modified.
     

     

      Note that calling plpy.execute will cause the entire
      result set to be read into memory.  Only use that function when you are
      sure that the result set will be relatively small.  If you don't want to
      risk excessive memory usage when fetching large results,
      use plpy.cursor rather
      than plpy.execute.
     

    

   

   

    
plpy.prepare(query [, argtypes])

    
plpy.execute(plan [, arguments [, limit]])

    

     

      preparing a queryin PL/Python
      plpy.prepare prepares the execution plan for a
      query.  It is called with a query string and a list of parameter types,
      if you have parameter references in the query.  For example:

```
plan = plpy.prepare("SELECT last_name FROM my_users WHERE first_name = $1", ["text"])
```

      text is the type of the variable you will be passing
      for $1.  The second argument is optional if you don't
      want to pass any parameters to the query.
     

     

      After preparing a statement, you use a variant of the
      function plpy.execute to run it:

```
rv = plpy.execute(plan, ["name"], 5)
```

      Pass the plan as the first argument (instead of the query string), and a
      list of values to substitute into the query as the second argument.  The
      second argument is optional if the query does not expect any parameters.
      The third argument is the optional row limit as before.
     

     

      Alternatively, you can call the execute method on
      the plan object:

```
rv = plan.execute(["name"], 5)
```

     

     

      Query parameters and result row fields are converted between PostgreSQL
      and Python data types as described in .
     

     

      When you prepare a plan using the PL/Python module it is automatically
      saved.  Read the SPI documentation () for a
      description of what this means.  In order to make effective use of this
      across function calls one needs to use one of the persistent storage
      dictionaries SD or GD (see
      ). For example:

```
CREATE FUNCTION usesavedplan() RETURNS trigger AS $$
    if "plan" in SD:
        plan = SD["plan"]
    else:
        plan = plpy.prepare("SELECT 1")
        SD["plan"] = plan
    # rest of function
$$ LANGUAGE plpython3u;
```

     

    

   

   

    
plpy.cursor(query)

    
plpy.cursor(plan [, arguments])

    

     

      The plpy.cursor function accepts the same arguments
      as plpy.execute (except for the row limit) and returns
      a cursor object, which allows you to process large result sets in smaller
      chunks.  As with plpy.execute, either a query string
      or a plan object along with a list of arguments can be used, or
      the cursor function can be called as a method of
      the plan object.
     

     

      The cursor object provides a fetch method that accepts
      an integer parameter and returns a result object.  Each time you
      call fetch, the returned object will contain the next
      batch of rows, never larger than the parameter value.  Once all rows are
      exhausted, fetch starts returning an empty result
      object.  Cursor objects also provide an
      iterator
      interface, yielding one row at a time until all rows are
      exhausted.  Data fetched that way is not returned as result objects, but
      rather as dictionaries, each dictionary corresponding to a single result
      row.
     

     

      An example of two ways of processing data from a large table is:

```
CREATE FUNCTION count_odd_iterator() RETURNS integer AS $$
odd = 0
for row in plpy.cursor("SELECT num FROM largetable"):
    if row['num'] % 2:
         odd += 1
return odd
$$ LANGUAGE plpython3u;

CREATE FUNCTION count_odd_fetch(batch_size integer) RETURNS integer AS $$
odd = 0
cursor = plpy.cursor("SELECT num FROM largetable")
while True:
    rows = cursor.fetch(batch_size)
    if not rows:
        break
    for row in rows:
        if row['num'] % 2:
            odd += 1
return odd
$$ LANGUAGE plpython3u;

CREATE FUNCTION count_odd_prepared() RETURNS integer AS $$
odd = 0
plan = plpy.prepare("SELECT num FROM largetable WHERE num % $1 <> 0", ["integer"])
rows = list(plpy.cursor(plan, [2]))  # or: = list(plan.cursor([2]))

return len(rows)
$$ LANGUAGE plpython3u;
```

     

     

      Cursors are automatically disposed of.  But if you want to explicitly
      release all resources held by a cursor, use the close
      method.  Once closed, a cursor cannot be fetched from anymore.
     

     
      

        Do not confuse objects created by plpy.cursor with
        DB-API cursors as defined by
        the Python
        Database API specification.  They don't have anything in common
        except for the name.
      

     
    

   

  

  

  

   
# Trapping Errors

   

    Functions accessing the database might encounter errors, which
    will cause them to abort and raise an exception.  Both
    plpy.execute and
    plpy.prepare can raise an instance of a subclass of
    plpy.SPIError, which by default will terminate
    the function.  This error can be handled just like any other
    Python exception, by using the try/except
    construct.  For example:

```
CREATE FUNCTION try_adding_joe() RETURNS text AS $$
    try:
        plpy.execute("INSERT INTO users(username) VALUES ('joe')")
    except plpy.SPIError:
        return "something went wrong"
    else:
        return "Joe added"
$$ LANGUAGE plpython3u;
```

   

   

    The actual class of the exception being raised corresponds to the
    specific condition that caused the error.  Refer
    to  for a list of possible
    conditions.  The module
    plpy.spiexceptions defines an exception class
    for each PostgreSQL condition, deriving
    their names from the condition name.  For
    instance, division_by_zero
    becomes DivisionByZero, unique_violation
    becomes UniqueViolation, fdw_error
    becomes FdwError, and so on.  Each of these
    exception classes inherits from SPIError.  This
    separation makes it easier to handle specific errors, for
    instance:

```
CREATE FUNCTION insert_fraction(numerator int, denominator int) RETURNS text AS $$
from plpy import spiexceptions
try:
    plan = plpy.prepare("INSERT INTO fractions (frac) VALUES ($1 / $2)", ["int", "int"])
    plpy.execute(plan, [numerator, denominator])
except spiexceptions.DivisionByZero:
    return "denominator cannot equal zero"
except spiexceptions.UniqueViolation:
    return "already have that fraction"
except plpy.SPIError as e:
    return "other error, SQLSTATE %s" % e.sqlstate
else:
    return "fraction inserted"
$$ LANGUAGE plpython3u;
```

    Note that because all exceptions from
    the plpy.spiexceptions module inherit
    from SPIError, an except
    clause handling it will catch any database access error.
   

   

    As an alternative way of handling different error conditions, you
    can catch the SPIError exception and determine
    the specific error condition inside the except
    block by looking at the sqlstate attribute of
    the exception object.  This attribute is a string value containing
    the SQLSTATE error code.  This approach provides
    approximately the same functionality
   

  

 

 

  
# Explicit Subtransactions

  

   Recovering from errors caused by database access as described in
    can lead to an undesirable
   situation where some operations succeed before one of them fails,
   and after recovering from that error the data is left in an
   inconsistent state.  PL/Python offers a solution to this problem in
   the form of explicit subtransactions.
  

  

   
# Subtransaction Context Managers

   

    Consider a function that implements a transfer between two
    accounts:

```
CREATE FUNCTION transfer_funds() RETURNS void AS $$
try:
    plpy.execute("UPDATE accounts SET balance = balance - 100 WHERE account_name = 'joe'")
    plpy.execute("UPDATE accounts SET balance = balance + 100 WHERE account_name = 'mary'")
except plpy.SPIError as e:
    result = "error transferring funds: %s" % e.args
else:
    result = "funds transferred correctly"
plan = plpy.prepare("INSERT INTO operations (result) VALUES ($1)", ["text"])
plpy.execute(plan, [result])
$$ LANGUAGE plpython3u;
```

    If the second UPDATE statement results in an
    exception being raised, this function will report the error, but
    the result of the first UPDATE will
    nevertheless be committed.  In other words, the funds will be
    withdrawn from Joe's account, but will not be transferred to
    Mary's account.
   

   

    To avoid such issues, you can wrap your
    plpy.execute calls in an explicit
    subtransaction.  The plpy module provides a
    helper object to manage explicit subtransactions that gets created
    with the plpy.subtransaction() function.
    Objects created by this function implement the
    
    context manager interface.  Using explicit subtransactions
    we can rewrite our function as:

```
CREATE FUNCTION transfer_funds2() RETURNS void AS $$
try:
    with plpy.subtransaction():
        plpy.execute("UPDATE accounts SET balance = balance - 100 WHERE account_name = 'joe'")
        plpy.execute("UPDATE accounts SET balance = balance + 100 WHERE account_name = 'mary'")
except plpy.SPIError as e:
    result = "error transferring funds: %s" % e.args
else:
    result = "funds transferred correctly"
plan = plpy.prepare("INSERT INTO operations (result) VALUES ($1)", ["text"])
plpy.execute(plan, [result])
$$ LANGUAGE plpython3u;
```

    Note that the use of try/except is still
    required.  Otherwise the exception would propagate to the top of
    the Python stack and would cause the whole function to abort with
    a PostgreSQL error, so that the
    operations table would not have any row
    inserted into it.  The subtransaction context manager does not
    trap errors, it only assures that all database operations executed
    inside its scope will be atomically committed or rolled back.  A
    rollback of the subtransaction block occurs on any kind of
    exception exit, not only ones caused by errors originating from
    database access.  A regular Python exception raised inside an
    explicit subtransaction block would also cause the subtransaction
    to be rolled back.
   

  

 

 

  
# Transaction Management

  

   In a procedure called from the top level or an anonymous code block
   (DO command) called from the top level it is possible to
   control transactions.  To commit the current transaction, call
   plpy.commit().  To roll back the current transaction,
   call plpy.rollback().  (Note that it is not possible to
   run the SQL commands COMMIT or
   ROLLBACK via plpy.execute or
   similar.  It has to be done using these functions.)  After a transaction is
   ended, a new transaction is automatically started, so there is no separate
   function for that.
  

  

   Here is an example:

```
CREATE PROCEDURE transaction_test1()
LANGUAGE plpython3u
AS $$
for i in range(0, 10):
    plpy.execute("INSERT INTO test1 (a) VALUES (%d)" % i)
    if i % 2 == 0:
        plpy.commit()
    else:
        plpy.rollback()
$$;

CALL transaction_test1();
```

  

  

   Transactions cannot be ended when an explicit subtransaction is active.
  

 

 

  
# Utility Functions

  

   The plpy module also provides the functions
   
    plpy.debug(msg, **kwargs)
    plpy.log(msg, **kwargs)
    plpy.info(msg, **kwargs)
    plpy.notice(msg, **kwargs)
    plpy.warning(msg, **kwargs)
    plpy.error(msg, **kwargs)
    plpy.fatal(msg, **kwargs)
   
   elogin PL/Python
   plpy.error and plpy.fatal
   actually raise a Python exception which, if uncaught, propagates out to
   the calling query, causing the current transaction or subtransaction to
   be aborted.  raise plpy.Error(msg) and
   raise plpy.Fatal(msg) are
   equivalent to calling plpy.error(msg) and
   plpy.fatal(msg), respectively but
   the raise form does not allow passing keyword arguments.
   The other functions only generate messages of different priority levels.
   Whether messages of a particular priority are reported to the client,
   written to the server log, or both is controlled by the
    and
    configuration
   variables. See  for more information.
  

  

   The msg argument is given as a positional argument.  For
   backward compatibility, more than one positional argument can be given. In
   that case, the string representation of the tuple of positional arguments
   becomes the message reported to the client.
  

  

   The following keyword-only arguments are accepted:
   
    detail
    hint
    sqlstate
    schema_name
    table_name
    column_name
    datatype_name
    constraint_name
   
   The string representation of the objects passed as keyword-only arguments
   is used to enrich the messages reported to the client. For example:

```
CREATE FUNCTION raise_custom_exception() RETURNS void AS $$
plpy.error("custom exception message",
           detail="some info about exception",
           hint="hint for users")
$$ LANGUAGE plpython3u;

=# SELECT raise_custom_exception();
ERROR:  plpy.Error: custom exception message
DETAIL:  some info about exception
HINT:  hint for users
CONTEXT:  Traceback (most recent call last):
  PL/Python function "raise_custom_exception", line 4, in <module>
    hint="hint for users")
PL/Python function "raise_custom_exception"
```

  

  

   Another set of utility functions are
   plpy.quote_literal(string),
   plpy.quote_nullable(string), and
   plpy.quote_ident(string).  They
   are equivalent to the built-in quoting functions described in .  They are useful when constructing
   ad-hoc queries.  A PL/Python equivalent of dynamic SQL from  would be:

```
plpy.execute("UPDATE tbl SET %s = %s WHERE key = %s" % (
    plpy.quote_ident(colname),
    plpy.quote_nullable(newvalue),
    plpy.quote_literal(keyvalue)))
```

  

 

 

  
# Python 2 vs. Python 3

  

   PL/Python supports only Python 3. Past versions of
   PostgreSQL supported Python 2, using the
   plpythonu and plpython2u language
   names.
  

 

 

  
# Environment Variables

  

   Some of the environment variables that are accepted by the Python
   interpreter can also be used to affect PL/Python behavior.  They
   would need to be set in the environment of the main PostgreSQL
   server process, for example in a start script.  The available
   environment variables depend on the version of Python; see the
   Python documentation for details.  At the time of this writing, the
   following environment variables have an affect on PL/Python,
   assuming an adequate Python version:
   

    

     
PYTHONHOME

    

    

     
PYTHONPATH

    

    

     
PYTHONY2K

    

    

     
PYTHONOPTIMIZE

    

    

     
PYTHONDEBUG

    

    

     
PYTHONVERBOSE

    

    

     
PYTHONCASEOK

    

    

     
PYTHONDONTWRITEBYTECODE

    

    

     
PYTHONIOENCODING

    

    

     
PYTHONUSERBASE

    

    

     
PYTHONHASHSEED

    

   

   (It appears to be a Python implementation detail beyond the control
   of PL/Python that some of the environment variables listed on
   the python man page are only effective in a
   command-line interpreter and not an embedded Python interpreter.)
