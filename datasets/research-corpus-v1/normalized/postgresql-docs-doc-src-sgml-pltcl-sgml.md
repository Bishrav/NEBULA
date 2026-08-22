# PL/Tcl &mdash; Tcl Procedural Language

  
   PL/Tcl
  

  
   Tcl
  

  

   PL/Tcl is a loadable procedural language for the
   PostgreSQL database system
   that enables the 
   Tcl language to be used to write
   PostgreSQL functions and procedures.
  

  

  

   
# Overview

   

    PL/Tcl offers most of the capabilities a function writer has in
    the C language, with a few restrictions, and with the addition of
    the powerful string processing libraries that are available for
    Tcl.
   

   

    One compelling good restriction is that
    everything is executed from within the safety of the context of a
    Tcl interpreter.  In addition to the limited command set of safe
    Tcl, only a few commands are available to access the database via
    SPI and to raise messages via elog().  PL/Tcl
    provides no way to access internals of the database server or to
    gain OS-level access under the permissions of the
    PostgreSQL server process, as a C
    function can do.  Thus, unprivileged database users can be trusted
    to use this language; it does not give them unlimited authority.
   

   

    The other notable implementation restriction is that Tcl functions
    cannot be used to create input/output functions for new data
    types.
   

   

    Sometimes it is desirable to write Tcl functions that are not restricted
    to safe Tcl.  For example, one might want a Tcl function that sends
    email.  To handle these cases, there is a variant of PL/Tcl called PL/TclU
    (for untrusted Tcl).  This is exactly the same language except that a full
    Tcl interpreter is used.  If PL/TclU is used, it must be
    installed as an untrusted procedural language so that only
    database superusers can create functions in it.  The writer of a PL/TclU
    function must take care that the function cannot be used to do anything
    unwanted, since it will be able to do anything that could be done by
    a user logged in as the database administrator.
   

   

    The shared object code for the PL/Tcl and
    PL/TclU call handlers is automatically built and
    installed in the PostgreSQL library
    directory if Tcl support is specified in the configuration step of
    the installation procedure.  To install PL/Tcl
    and/or PL/TclU in a particular database, use the
    CREATE EXTENSION command, for example
    CREATE EXTENSION pltcl or
    CREATE EXTENSION pltclu.
   

  

  

   

    
# PL/Tcl Functions and Arguments

    

     To create a function in the PL/Tcl language, use
     the standard  syntax:

```
CREATE FUNCTION funcname (argument-types) RETURNS return-type AS $$
    # PL/Tcl function body
$$ LANGUAGE pltcl;
```

     PL/TclU is the same, except that the language has to be specified as
     pltclu.
    

    

     The body of the function is simply a piece of Tcl script.
     When the function is called, the argument values are passed to the
     Tcl script as variables named 1
     ... n.  The result is
     returned from the Tcl code in the usual way, with
     a return statement.  In a procedure, the return value
     from the Tcl code is ignored.
    

    

     For example, a function
     returning the greater of two integer values could be defined as:

```
CREATE FUNCTION tcl_max(integer, integer) RETURNS integer AS $$
    if {$1 > $2} {return $1}
    return $2
$$ LANGUAGE pltcl STRICT;
```

     Note the clause STRICT, which saves us from
     having to think about null input values: if a null value is passed, the
     function will not be called at all, but will just return a null
     result automatically.
    

    

     In a nonstrict function,
     if the actual value of an argument is null, the corresponding
     $n variable will be set to an empty string.
     To detect whether a particular argument is null, use the function
     argisnull.  For example, suppose that we wanted tcl_max
     with one null and one nonnull argument to return the nonnull
     argument, rather than null:

```
CREATE FUNCTION tcl_max(integer, integer) RETURNS integer AS $$
    if {[argisnull 1]} {
        if {[argisnull 2]} { return_null }
        return $2
    }
    if {[argisnull 2]} { return $1 }
    if {$1 > $2} {return $1}
    return $2
$$ LANGUAGE pltcl;
```

    

    

     As shown above,
     to return a null value from a PL/Tcl function, execute
     return_null.  This can be done whether the
     function is strict or not.
    

    

     Composite-type arguments are passed to the function as Tcl
     arrays.  The element names of the array are the attribute names
     of the composite type. If an attribute in the passed row has the
     null value, it will not appear in the array. Here is an example:

```
CREATE TABLE employee (
    name text,
    salary integer,
    age integer
);

CREATE FUNCTION overpaid(employee) RETURNS boolean AS $$
    if {200000.0 < $1(salary)} {
        return "t"
    }
    if {$1(age) < 30 && 100000.0 < $1(salary)} {
        return "t"
    }
    return "f"
$$ LANGUAGE pltcl;
```

    

    

     PL/Tcl functions can return composite-type results, too.  To do this,
     the Tcl code must return a list of column name/value pairs matching
     the expected result type.  Any column names omitted from the list
     are returned as nulls, and an error is raised if there are unexpected
     column names.  Here is an example:

```
CREATE FUNCTION square_cube(IN int, OUT squared int, OUT cubed int) AS $$
    return [list squared [expr {$1 * $1}] cubed [expr {$1 * $1 * $1}]]
$$ LANGUAGE pltcl;
```

    

    

     Output arguments of procedures are returned in the same way, for example:

```
CREATE PROCEDURE tcl_triple(INOUT a integer, INOUT b integer) AS $$
    return [list a [expr {$1 * 3}] b [expr {$2 * 3}]]
$$ LANGUAGE pltcl;

CALL tcl_triple(5, 10);
```

    

    
     

      The result list can be made from an array representation of the
      desired tuple with the array get Tcl command.  For example:

```
CREATE FUNCTION raise_pay(employee, delta int) RETURNS employee AS $$
    set 1(salary) [expr {$1(salary) + $2}]
    return [array get 1]
$$ LANGUAGE pltcl;
```

     

    

    

     PL/Tcl functions can return sets.  To do this, the Tcl code should
     call return_next once per row to be returned,
     passing either the appropriate value when returning a scalar type,
     or a list of column name/value pairs when returning a composite type.
     Here is an example returning a scalar type:

```
CREATE FUNCTION sequence(int, int) RETURNS SETOF int AS $$
    for {set i $1} {$i < $2} {incr i} {
        return_next $i
    }
$$ LANGUAGE pltcl;
```

     and here is one returning a composite type:

```
CREATE FUNCTION table_of_squares(int, int) RETURNS TABLE (x int, x2 int) AS $$
    for {set i $1} {$i < $2} {incr i} {
        return_next [list x $i x2 [expr {$i * $i}]]
    }
$$ LANGUAGE pltcl;
```

    

   

   

    
# Data Values in PL/Tcl

    

     The argument values supplied to a PL/Tcl function's code are simply
     the input arguments converted to text form (just as if they had been
     displayed by a SELECT statement).  Conversely, the
     return and return_next commands will accept
     any string that is acceptable input format for the function's declared
     result type, or for the specified column of a composite result type.
    

   

   

    
# Global Data in PL/Tcl

    
     global data
     in PL/Tcl
    

    

     Sometimes it
     is useful to have some global data that is held between two
     calls to a function or is shared between different functions.
     This is easily done in PL/Tcl, but there are some restrictions that
     must be understood.
    

    

     For security reasons, PL/Tcl executes functions called by any one SQL
     role in a separate Tcl interpreter for that role.  This prevents
     accidental or malicious interference by one user with the behavior of
     another user's PL/Tcl functions.  Each such interpreter will have its own
     values for any global Tcl variables.  Thus, two PL/Tcl
     functions will share the same global variables if and only if they are
     executed by the same SQL role.  In an application wherein a single
     session executes code under multiple SQL roles (via SECURITY
     DEFINER functions, use of SET ROLE, etc.) you may need to
     take explicit steps to ensure that PL/Tcl functions can share data.  To
     do that, make sure that functions that should communicate are owned by
     the same user, and mark them SECURITY DEFINER.  You must of
     course take care that such functions can't be used to do anything
     unintended.
    

    

     All PL/TclU functions used in a session execute in the same Tcl
     interpreter, which of course is distinct from the interpreter(s)
     used for PL/Tcl functions.  So global data is automatically shared
     between PL/TclU functions.  This is not considered a security risk
     because all PL/TclU functions execute at the same trust level,
     namely that of a database superuser.
    

    

     To help protect PL/Tcl functions from unintentionally interfering
     with each other, a global
     array is made available to each function via the upvar
     command. The global name of this variable is the function's internal
     name, and the local name is GD.  It is recommended that
     GD be used
     for persistent private data of a function.  Use regular Tcl global
     variables only for values that you specifically intend to be shared among
     multiple functions.  (Note that the GD arrays are only
     global within a particular interpreter, so they do not bypass the
     security restrictions mentioned above.)
    

    

     An example of using GD appears in the
     spi_execp example below.
    

   

   

    
# Database Access from PL/Tcl

    

     In this section, we follow the usual Tcl convention of using question
     marks, rather than brackets, to indicate an optional element in a
     syntax synopsis.  The following commands are available to access
     the database from the body of a PL/Tcl function:

    

     

      
spi_exec -count n -array name command loop-body

      

       

        Executes an SQL command given as a string.  An error in the command
        causes an error to be raised.  Otherwise, the return value of spi_exec
        is the number of rows processed (selected, inserted, updated, or
        deleted) by the command, or zero if the command is a utility
        statement.  In addition, if the command is a SELECT statement, the
        values of the selected columns are placed in Tcl variables as
        described below.
       

       

        The optional -count value tells
        spi_exec to stop
        once n rows have been retrieved,
        much as if the query included a LIMIT clause.
        If n is zero, the query is run to
        completion, the same as when -count is omitted.
       

       

        If the command is a SELECT statement, the values of the
        result columns are placed into Tcl variables named after the columns.
        If the -array option is given, the column values are
        instead stored into elements of the named associative array, with the
        column names used as array indexes.  In addition, the current row
        number within the result (counting from zero) is stored into the array
        element named .tupno, unless that name is
        in use as a column name in the result.
       

       

        If the command is a SELECT statement and no loop-body
        script is given, then only the first row of results are stored into
        Tcl variables or array elements; remaining rows, if any, are ignored.
        No storing occurs if the query returns no rows.  (This case can be
        detected by checking the result of spi_exec.)
        For example:

```
spi_exec "SELECT count(*) AS cnt FROM pg_proc"
```

        will set the Tcl variable $cnt to the number of rows in
        the pg_proc system catalog.
       

       

        If the optional loop-body argument is given, it is
        a piece of Tcl script that is executed once for each row in the
        query result.  (loop-body is ignored if the given
        command is not a SELECT.)
        The values of the current row's columns
        are stored into Tcl variables or array elements before each iteration.
        For example:

```
spi_exec -array C "SELECT * FROM pg_class" {
    elog DEBUG "have table $C(relname)"
}
```

        will print a log message for every row of pg_class.  This
        feature works similarly to other Tcl looping constructs; in
        particular continue and break work in the
        usual way inside the loop body.
       

       

        If a column of a query result is null, the target
        variable for it is unset rather than being set.
       

      

     

     

      
spi_prepare query typelist

      

       

        Prepares and saves a query plan for later execution.  The
        saved plan will be retained for the life of the current
        session.preparing a query
        in PL/Tcl
       

       

        The query can use parameters, that is, placeholders for
        values to be supplied whenever the plan is actually executed.
        In the query string, refer to parameters
        by the symbols $1 ... $n.
        If the query uses parameters, the names of the parameter types
        must be given as a Tcl list.  (Write an empty list for
        typelist if no parameters are used.)
       

       

        The return value from spi_prepare is a query ID
        to be used in subsequent calls to spi_execp. See
        spi_execp for an example.
       

      

     

     

      
spi_execp -count n -array name -nulls string queryid value-list loop-body

      

       

        Executes a query previously prepared with spi_prepare.
        queryid is the ID returned by
        spi_prepare.  If the query references parameters,
        a value-list must be supplied.  This
        is a Tcl list of actual values for the parameters.  The list must be
        the same length as the parameter type list previously given to
        spi_prepare.  Omit value-list
        if the query has no parameters.
       

       

        The optional value for -nulls is a string of spaces and
        'n' characters telling spi_execp
        which of the parameters are null values. If given, it must have exactly the
        same length as the value-list.  If it
        is not given, all the parameter values are nonnull.
       

       

        Except for the way in which the query and its parameters are specified,
        spi_execp works just like spi_exec.
        The -count, -array, and
        loop-body options are the same,
        and so is the result value.
       

       

        Here's an example of a PL/Tcl function using a prepared plan:

```
CREATE FUNCTION t1_count(integer, integer) RETURNS integer AS $$
    if {![ info exists GD(plan) ]} {
        # prepare the saved plan on the first call
        set GD(plan) [ spi_prepare \
                "SELECT count(*) AS cnt FROM t1 WHERE num >= \$1 AND num <= \$2" \
                [ list int4 int4 ] ]
    }
    spi_execp -count 1 $GD(plan) [ list $1 $2 ]
    return $cnt
$$ LANGUAGE pltcl;
```

        We need backslashes inside the query string given to
        spi_prepare to ensure that the
        $n markers will be passed
        through to spi_prepare as-is, and not replaced by Tcl
        variable substitution.

       

      

     

     

      
subtransaction command

      

       

        The Tcl script contained in command is
        executed within an SQL subtransaction.  If the script returns an
        error, that entire subtransaction is rolled back before returning the
        error out to the surrounding Tcl code.
        See  for more details and an
        example.
       

      

     

     

      
quote string

      

       

        Doubles all occurrences of single quote and backslash characters
        in the given string.  This can be used to safely quote strings
        that are to be inserted into SQL commands given
        to spi_exec or
        spi_prepare.
        For example, think about an SQL command string like:

```
"SELECT '$val' AS ret"
```

        where the Tcl variable val actually contains
        doesn't. This would result
        in the final command string:

```
SELECT 'doesn't' AS ret
```

        which would cause a parse error during
        spi_exec or
        spi_prepare.
        To work properly, the submitted command should contain:

```
SELECT 'doesn''t' AS ret
```

        which can be formed in PL/Tcl using:

```
"SELECT '[ quote $val ]' AS ret"
```

        One advantage of spi_execp is that you don't
        have to quote parameter values like this, since the parameters are never
        parsed as part of an SQL command string.
       

      

     

     

      

       elog level msg
       
        elog
        in PL/Tcl
       
      

      

       

        Emits a log or error message. Possible levels are
        DEBUG, LOG, INFO,
        NOTICE, WARNING, ERROR, and
        FATAL. ERROR
        raises an error condition; if this is not trapped by the surrounding
        Tcl code, the error propagates out to the calling query, causing
        the current transaction or subtransaction to be aborted.  This
        is effectively the same as the Tcl error command.
        FATAL aborts the transaction and causes the current
        session to shut down.  (There is probably no good reason to use
        this error level in PL/Tcl functions, but it's provided for
        completeness.)  The other levels only generate messages of different
        priority levels.
        Whether messages of a particular priority are reported to the client,
        written to the server log, or both is controlled by the
         and
         configuration
        variables. See 
        and 
        for more information.
       

      

     

    

    

   

   

    
# Trigger Functions in PL/Tcl

    
     trigger
     in PL/Tcl
    

    

     Trigger functions can be written in PL/Tcl.
     PostgreSQL requires that a function that is to be called
     as a trigger must be declared as a function with no arguments
     and a return type of trigger.
    

    

     The information from the trigger manager is passed to the function body
     in the following variables:

     

      

       
$TG_name

       

        

         The name of the trigger from the CREATE TRIGGER statement.
        

       

      

      

       
$TG_relid

       

        

         The object ID of the table that caused the trigger function
         to be invoked.
        

       

      

      

       
$TG_table_name

       

        

         The name of the table that caused the trigger function
         to be invoked.
        

       

      

      

       
$TG_table_schema

       

        

         The schema of the table that caused the trigger function
         to be invoked.
        

       

      

      

       
$TG_relatts

       

        

         A Tcl list of the table column names, prefixed with an empty list
         element. So looking up a column name in the list with Tcl's
         lsearch command returns the element's number starting
         with 1 for the first column, the same way the columns are customarily
         numbered in PostgreSQL.  (Empty list
         elements also appear in the positions of columns that have been
         dropped, so that the attribute numbering is correct for columns
         to their right.)
        

       

      

      

       
$TG_when

       

        

         The string BEFORE, AFTER, or
         INSTEAD OF, depending on the type of trigger event.
        

       

      

      

       
$TG_level

       

        

         The string ROW or STATEMENT depending on the
         type of trigger event.
        

       

      

      

       
$TG_op

       

        

         The string INSERT, UPDATE,
         DELETE, or TRUNCATE depending on the type of
         trigger event.
        

       

      

      

       
$NEW

       

        

         An associative array containing the values of the new table
         row for INSERT or UPDATE actions, or
         empty for DELETE.  The array is indexed by column
         name.  Columns that are null will not appear in the array.
         This is not set for statement-level triggers.
        

       

      

      

       
$OLD

       

        

         An associative array containing the values of the old table
         row for UPDATE or DELETE actions, or
         empty for INSERT.  The array is indexed by column
         name.  Columns that are null will not appear in the array.
         This is not set for statement-level triggers.
        

       

      

      

       
$args

       

        

         A Tcl list of the arguments to the function as given in the
         CREATE TRIGGER statement. These arguments are also accessible as
         $1 ... $n in the function body.
        

       

      

     

    

    

     The return value from a trigger function can be one of the strings
     OK or SKIP, or a list of column name/value pairs.
     If the return value is OK,
     the operation (INSERT/UPDATE/DELETE)
     that fired the trigger will proceed
     normally. SKIP tells the trigger manager to silently suppress
     the operation for this row. If a list is returned, it tells PL/Tcl to
     return a modified row to the trigger manager; the contents of the
     modified row are specified by the column names and values in the list.
     Any columns not mentioned in the list are set to null.
     Returning a modified row is only meaningful
     for row-level BEFORE INSERT or UPDATE
     triggers, for which the modified row will be inserted instead of the one
     given in $NEW; or for row-level INSTEAD OF
     INSERT or UPDATE triggers where the returned row
     is used as the source data for INSERT RETURNING or
     UPDATE RETURNING clauses.
     In row-level BEFORE DELETE or INSTEAD
     OF DELETE triggers, returning a modified row has the same
     effect as returning OK, that is the operation proceeds.
     The trigger return value is ignored for all other types of triggers.
    

    
     

      The result list can be made from an array representation of the
      modified tuple with the array get Tcl command.
     

    

    

     Here's a little example trigger function that forces an integer value
     in a table to keep track of the number of updates that are performed on the
     row. For new rows inserted, the value is initialized to 0 and then
     incremented on every update operation.

```
CREATE FUNCTION trigfunc_modcount() RETURNS trigger AS $$
    switch $TG_op {
        INSERT {
            set NEW($1) 0
        }
        UPDATE {
            set NEW($1) $OLD($1)
            incr NEW($1)
        }
        default {
            return OK
        }
    }
    return [array get NEW]
$$ LANGUAGE pltcl;

CREATE TABLE mytab (num integer, description text, modcnt integer);

CREATE TRIGGER trig_mytab_modcount BEFORE INSERT OR UPDATE ON mytab
    FOR EACH ROW EXECUTE FUNCTION trigfunc_modcount('modcnt');
```

     Notice that the trigger function itself does not know the column
     name; that's supplied from the trigger arguments.  This lets the
     trigger function be reused with different tables.
    

   

   

    
# Event Trigger Functions in PL/Tcl

    
     event trigger
     in PL/Tcl
    

    

     Event trigger functions can be written in PL/Tcl.
     PostgreSQL requires that a function that is
     to be called as an event trigger must be declared as a function with no
     arguments and a return type of event_trigger.
    

    

     The information from the trigger manager is passed to the function body
     in the following variables:

     

      

       
$TG_event

       

        

         The name of the event the trigger is fired for.
        

       

      

      

       
$TG_tag

       

        

         The command tag for which the trigger is fired.
        

       

      

     

    

    

     The return value of the trigger function is ignored.
    

    

     Here's a little example event trigger function that simply raises
     a NOTICE message each time a supported command is
     executed:

```
CREATE OR REPLACE FUNCTION tclsnitch() RETURNS event_trigger AS $$
  elog NOTICE "tclsnitch: $TG_event $TG_tag"
$$ LANGUAGE pltcl;

CREATE EVENT TRIGGER tcl_a_snitch ON ddl_command_start EXECUTE FUNCTION tclsnitch();
```

    

   

   

    
# Error Handling in PL/Tcl

    
     exceptions
     in PL/Tcl
    

    

     Tcl code within or called from a PL/Tcl function can raise an error,
     either by executing some invalid operation or by generating an error
     using the Tcl error command or
     PL/Tcl's elog command.  Such errors can be caught
     within Tcl using the Tcl catch command.  If an
     error is not caught but is allowed to propagate out to the top level of
     execution of the PL/Tcl function, it is reported as an SQL error in the
     function's calling query.
    

    

     Conversely, SQL errors that occur within PL/Tcl's
     spi_exec, spi_prepare,
     and spi_execp commands are reported as Tcl errors,
     so they are catchable by Tcl's catch command.
     (Each of these PL/Tcl commands runs its SQL operation in a
     subtransaction, which is rolled back on error, so that any
     partially-completed operation is automatically cleaned up.)
     Again, if an error propagates out to the top level without being caught,
     it turns back into an SQL error.
    

    

     Tcl provides an errorCode variable that can represent
     additional information about an error in a form that is easy for Tcl
     programs to interpret.  The contents are in Tcl list format, and the
     first word identifies the subsystem or library reporting the error;
     beyond that the contents are left to the individual subsystem or
     library.  For database errors reported by PL/Tcl commands, the first
     word is POSTGRES, the second word is the PostgreSQL
     version number, and additional words are field name/value pairs
     providing detailed information about the error.
     Fields SQLSTATE, condition,
     and message are always supplied
     (the first two represent the error code and condition name as shown
     in ).
     Fields that may be present include
     detail, hint, context,
     schema, table, column,
     datatype, constraint,
     statement, cursor_position,
     filename, lineno, and
     funcname.
    

    

     A convenient way to work with PL/Tcl's errorCode
     information is to load it into an array, so that the field names become
     array subscripts.  Code for doing that might look like

```
if {[catch { spi_exec $sql_command }]} {
    if {[lindex $::errorCode 0] == "POSTGRES"} {
        array set errorArray $::errorCode
        if {$errorArray(condition) == "undefined_table"} {
            # deal with missing table
        } else {
            # deal with some other type of SQL error
        }
    }
}
```

     (The double colons explicitly specify that errorCode
     is a global variable.)
    

   

   

    
# Explicit Subtransactions in PL/Tcl

    
     subtransactions
     in PL/Tcl
    

    

     Recovering from errors caused by database access as described in
      can lead to an undesirable
     situation where some operations succeed before one of them fails,
     and after recovering from that error the data is left in an
     inconsistent state.  PL/Tcl offers a solution to this problem in
     the form of explicit subtransactions.
    

    

     Consider a function that implements a transfer between two accounts:

```
CREATE FUNCTION transfer_funds() RETURNS void AS $$
    if [catch {
        spi_exec "UPDATE accounts SET balance = balance - 100 WHERE account_name = 'joe'"
        spi_exec "UPDATE accounts SET balance = balance + 100 WHERE account_name = 'mary'"
    } errormsg] {
        set result [format "error transferring funds: %s" $errormsg]
    } else {
        set result "funds transferred successfully"
    }
    spi_exec "INSERT INTO operations (result) VALUES ('[quote $result]')"
$$ LANGUAGE pltcl;
```

     If the second UPDATE statement results in an
     exception being raised, this function will log the failure, but
     the result of the first UPDATE will
     nevertheless be committed.  In other words, the funds will be
     withdrawn from Joe's account, but will not be transferred to
     Mary's account.  This happens because each spi_exec
     is a separate subtransaction, and only one of those subtransactions
     got rolled back.
    

    

     To handle such cases, you can wrap multiple database operations in an
     explicit subtransaction, which will succeed or roll back as a whole.
     PL/Tcl provides a subtransaction command to manage
     this.  We can rewrite our function as:

```
CREATE FUNCTION transfer_funds2() RETURNS void AS $$
    if [catch {
        subtransaction {
            spi_exec "UPDATE accounts SET balance = balance - 100 WHERE account_name = 'joe'"
            spi_exec "UPDATE accounts SET balance = balance + 100 WHERE account_name = 'mary'"
        }
    } errormsg] {
        set result [format "error transferring funds: %s" $errormsg]
    } else {
        set result "funds transferred successfully"
    }
    spi_exec "INSERT INTO operations (result) VALUES ('[quote $result]')"
$$ LANGUAGE pltcl;
```

     Note that use of catch is still required for this
     purpose.  Otherwise the error would propagate to the top level of the
     function, preventing the desired insertion into
     the operations table.
     The subtransaction command does not trap errors, it
     only assures that all database operations executed inside its scope will
     be rolled back together when an error is reported.
    

    

     A rollback of an explicit subtransaction occurs on any error reported
     by the contained Tcl code, not only errors originating from database
     access.  Thus a regular Tcl exception raised inside
     a subtransaction command will also cause the
     subtransaction to be rolled back.  However, non-error exits out of the
     contained Tcl code (for instance, due to return) do
     not cause a rollback.
    

   

   

    
# Transaction Management

    

     In a procedure called from the top level or an anonymous code block
     (DO command) called from the top level it is possible
     to control transactions.  To commit the current transaction, call the
     commit command.  To roll back the current transaction,
     call the rollback command.  (Note that it is not
     possible to run the SQL commands COMMIT or
     ROLLBACK via spi_exec or similar.
     It has to be done using these functions.)  After a transaction is ended,
     a new transaction is automatically started, so there is no separate
     command for that.
    

    

     Here is an example:

```
CREATE PROCEDURE transaction_test1()
LANGUAGE pltcl
AS $$
for {set i 0} {$i < 10} {incr i} {
    spi_exec "INSERT INTO test1 (a) VALUES ($i)"
    if {$i % 2 == 0} {
        commit
    } else {
        rollback
    }
}
$$;

CALL transaction_test1();
```

    

    

     Transactions cannot be ended when an explicit subtransaction is active.
    

   

   

    
# PL/Tcl Configuration

    

     This section lists configuration parameters that
     affect PL/Tcl.
    

    

     

      

       pltcl.start_proc (string)
       
        pltcl.start_proc configuration parameter
       
      

      

       

        This parameter, if set to a nonempty string, specifies the name
        (possibly schema-qualified) of a parameterless PL/Tcl function that
        is to be executed whenever a new Tcl interpreter is created for
        PL/Tcl.  Such a function can perform per-session initialization, such
        as loading additional Tcl code.  A new Tcl interpreter is created
        when a PL/Tcl function is first executed in a database session, or
        when an additional interpreter has to be created because a PL/Tcl
        function is called by a new SQL role.
       

       

        The referenced function must be written in the pltcl
        language, and must not be marked SECURITY DEFINER.
        (These restrictions ensure that it runs in the interpreter it's
        supposed to initialize.)  The current user must have permission to
        call it, too.
       

       

        If the function fails with an error it will abort the function call
        that caused the new interpreter to be created and propagate out to
        the calling query, causing the current transaction or subtransaction
        to be aborted.  Any actions already done within Tcl won't be undone;
        however, that interpreter won't be used again.  If the language is
        used again the initialization will be attempted again within a fresh
        Tcl interpreter.
       

       

        Only superusers can change this setting.  Although this setting
        can be changed within a session, such changes will not affect Tcl
        interpreters that have already been created.
       

      

     

     

      

       pltclu.start_proc (string)
       
        pltclu.start_proc configuration parameter
       
      

      

       

        This parameter is exactly like pltcl.start_proc,
        except that it applies to PL/TclU.  The referenced function must
        be written in the pltclu language.
       

      

     

    

   

   

    
# Tcl Procedure Names

    

     In PostgreSQL, the same function name can be used for
     different function definitions if the functions are placed in different
     schemas, or if the number of arguments or their types
     differ. Tcl, however, requires all procedure names to be distinct.
     PL/Tcl deals with this by including the argument type names in the
     internal Tcl procedure name, and then appending the function's object
     ID (OID) to the internal Tcl procedure name if necessary to make it
     different from the names of all previously-loaded functions in the
     same Tcl interpreter.  Thus,
     PostgreSQL functions with the same name
     and different argument types will be different Tcl procedures, too.  This
     is not normally a concern for a PL/Tcl programmer, but it might be visible
     when debugging.
    

    

     For this reason among others, a PL/Tcl function cannot call another one
     directly (that is, within Tcl).  If you need to do that, you must go
     through SQL, using spi_exec or a related command.
