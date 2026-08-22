# Event Triggers

  
   event trigger
  

  

   To supplement the trigger mechanism discussed in ,
   PostgreSQL also provides event triggers.  Unlike regular
   triggers, which are attached to a single table and capture only DML events,
   event triggers are global to a particular database and are capable of
   capturing DDL events.
  

  

   Like regular triggers, event triggers can be written in any procedural
   language that includes event trigger support, or in C, but not in plain
   SQL.
  

  

   
# Overview of Event Trigger Behavior

   

     An event trigger fires whenever the event with which it is associated
     occurs in the database in which it is defined. Currently, the
     supported events are
     login,
     ddl_command_start,
     ddl_command_end,
     table_rewrite
     and sql_drop.
     Support for additional events may be added in future releases.
   

   

    
# login

   

     The login event occurs when an authenticated user logs
     into the system. Any bug in a trigger procedure for this event may
     prevent successful login to the system. Such bugs may be worked around by
     setting  to false
     either in a connection string or configuration file. Alternatively, you can
     restart the system in single-user mode (as event triggers are
     disabled in this mode). See the  reference
     page for details about using single-user mode.
     The login event will also fire on standby servers.
     To prevent servers from becoming inaccessible, such triggers must avoid
     writing anything to the database when running on a standby.
     Also, it's recommended to avoid long-running queries in
     login event triggers.  Note that, for instance,
     canceling a connection in psql will not cancel
     the in-progress login trigger.
   

   

     For an example on how to use the login event trigger,
     see .
   

   

   

    
# ddl_command_start

   

     The ddl_command_start event occurs just before the
     execution of a DDL command.  DDL commands in this context are:
     

      

CREATE

      

ALTER

      

DROP

      

COMMENT

      

GRANT

      

IMPORT FOREIGN SCHEMA

      

REINDEX

      

REFRESH MATERIALIZED VIEW

      

REVOKE

      

SECURITY LABEL

     

     ddl_command_start also occurs just before the
     execution of a SELECT INTO command, since this is
     equivalent to CREATE TABLE AS.
   

   

     As an exception, this event does not occur for DDL commands targeting
     shared objects:
     

      

databases

      

roles (role definitions and role memberships)

      

tablespaces

      

parameter privileges

      

ALTER SYSTEM

     

     This event also does not occur for commands targeting event triggers
     themselves.
   

   

     No check whether the affected object exists or doesn't exist is performed
     before the event trigger fires.
   

   

   

    
# ddl_command_end

   

    The ddl_command_end event occurs just after the execution of
    the same set of commands as ddl_command_start.  To
    obtain more details on the DDL
    operations that took place, use the set-returning function
    pg_event_trigger_ddl_commands() from the
    ddl_command_end event trigger code (see
    ).  Note that the trigger fires
    after the actions have taken place (but before the transaction commits),
    and thus the system catalogs can be read as already changed.
   

   

   

    
# sql_drop

   

    The sql_drop event occurs just before the
    ddl_command_end event trigger for any operation that drops
    database objects.  Note that besides the obvious DROP
    commands, some ALTER commands can also trigger an
    sql_drop event.
   

   

    To list the objects that have been dropped, use the
    set-returning function pg_event_trigger_dropped_objects() from the
    sql_drop event trigger code (see
    ). Note that
    the trigger is executed after the objects have been deleted from the
    system catalogs, so it's not possible to look them up anymore.
   

   

   

    
# table_rewrite

   

    The table_rewrite event occurs just before a table is
    rewritten by some actions of the commands ALTER TABLE and
    ALTER TYPE.  While other
    control statements are available to rewrite a table,
    like CLUSTER and VACUUM,
    the table_rewrite event is not triggered by them.
    To find the OID of the table that was rewritten, use the function
    pg_event_trigger_table_rewrite_oid(), to discover the
    reason(s) for the rewrite, use the function
    pg_event_trigger_table_rewrite_reason() (see ).
   

   

   

    
# Event Triggers in Aborted Transactions

   

     Event triggers (like other functions) cannot be executed in an aborted
     transaction.  Thus, if a DDL command fails with an error, any associated
     ddl_command_end triggers will not be executed.  Conversely,
     if a ddl_command_start trigger fails with an error, no
     further event triggers will fire, and no attempt will be made to execute
     the command itself.  Similarly, if a ddl_command_end trigger
     fails with an error, the effects of the DDL statement will be rolled
     back, just as they would be in any other case where the containing
     transaction aborts.
   

   

   

    
# Creating Event Triggers

   

     Event triggers are created using the command .
     In order to create an event trigger, you must first create a function with
     the special return type event_trigger.  This function
     need not (and may not) return a value; the return type serves merely as
     a signal that the function is to be invoked as an event trigger.
   

   

     If more than one event trigger is defined for a particular event, they will
     fire in alphabetical order by trigger name.
   

   

     A trigger definition can also specify a WHEN
     condition so that, for example, a ddl_command_start
     trigger can be fired only for particular commands which the user wishes
     to intercept. A common use of such triggers is to restrict the range of
     DDL operations which users may perform.
   

   

  

  

   
# Writing Event Trigger Functions in C

   
    event trigger
    in C
   

   

    This section describes the low-level details of the interface to an
    event trigger function. This information is only needed when writing
    event trigger functions in C. If you are using a higher-level language
    then these details are handled for you. In most cases you should
    consider using a procedural language before writing your event triggers
    in C. The documentation of each procedural language explains how to
    write an event trigger in that language.
   

   

    Event trigger functions must use the version 1 function
    manager interface.
   

   

    When a function is called by the event trigger manager, it is not passed
    any normal arguments, but it is passed a context pointer
    pointing to a EventTriggerData structure. C functions can
    check whether they were called from the event trigger manager or not by
    executing the macro:

```
CALLED_AS_EVENT_TRIGGER(fcinfo)
```

    which expands to:

```
((fcinfo)->context != NULL && IsA((fcinfo)->context, EventTriggerData))
```

    If this returns true, then it is safe to cast
    fcinfo->context to type EventTriggerData
    * and make use of the pointed-to
    EventTriggerData structure.  The function must
    not alter the EventTriggerData
    structure or any of the data it points to.
   

   

    struct EventTriggerData is defined in
    commands/event_trigger.h:

```
typedef struct EventTriggerData
{
    NodeTag     type;
    const char *event;      /* event name */
    Node       *parsetree;  /* parse tree */
    CommandTag  tag;        /* command tag */
} EventTriggerData;
```

    where the members are defined as follows:

    

     

      
type

      

       

        Always T_EventTriggerData.
       

      

     

     

      
event

      

       

        Describes the event for which the function is called, one of
        "login", "ddl_command_start",
        "ddl_command_end", "sql_drop",
        "table_rewrite".
        See  for the meaning of these
        events.
       

      

     

     

      
parsetree

      

       

        A pointer to the parse tree of the command.  Check the PostgreSQL
        source code for details.  The parse tree structure is subject to change
        without notice.
       

      

     

     

      
tag

      

       

        The command tag associated with the event for which the event trigger
        is run, for example "CREATE FUNCTION".
       

      

     

    

   

   

    An event trigger function must return a NULL pointer
    (not an SQL null value, that is, do not
    set 
isNull
 true).
   

  

  

   
# A Complete Event Trigger Example

   

    Here is a very simple example of an event trigger function written in C.
    (Examples of triggers written in procedural languages can be found in
    the documentation of the procedural languages.)
   

   

    The function noddl raises an exception each time it is called.
    The event trigger definition associated the function with
    the ddl_command_start event.  The effect is that all DDL
    commands (with the exceptions mentioned
    in ) are prevented from running.
   

   

    This is the source code of the trigger function:

```
context;

    ereport(ERROR,
            (errcode(ERRCODE_INSUFFICIENT_PRIVILEGE),
             errmsg("command \"%s\" denied",
                    GetCommandTagName(trigdata->tag))));

    PG_RETURN_NULL();
}
]]>
```

   

   

    After you have compiled the source code (see ),
    declare the function and the triggers:

```
CREATE FUNCTION noddl() RETURNS event_trigger
    AS 'noddl' LANGUAGE C;

CREATE EVENT TRIGGER noddl ON ddl_command_start
    EXECUTE FUNCTION noddl();
```

   

   

    Now you can test the operation of the trigger:

```
=# \dy
                     List of event triggers
 Name  |       Event       | Owner | Enabled | Function | Tags
-------+-------------------+-------+---------+----------+------
 noddl | ddl_command_start | dim   | enabled | noddl    |
(1 row)

=# CREATE TABLE foo(id serial);
ERROR:  command "CREATE TABLE" denied
```

   

   

    In this situation, in order to be able to run some DDL commands when you
    need to do so, you have to either drop the event trigger or disable it.  It
    can be convenient to disable the trigger for only the duration of a
    transaction:

```
BEGIN;
ALTER EVENT TRIGGER noddl DISABLE;
CREATE TABLE foo (id serial);
ALTER EVENT TRIGGER noddl ENABLE;
COMMIT;
```

    (Recall that DDL commands on event triggers themselves are not affected by
    event triggers.)
   

  

  

   
# A Table Rewrite Event Trigger Example

   

    Thanks to the table_rewrite event, it is possible to implement
    a table rewriting policy only allowing the rewrite in maintenance windows.
   

   

    Here's an example implementing such a policy.

```
CREATE OR REPLACE FUNCTION no_rewrite()
 RETURNS event_trigger
 LANGUAGE plpgsql AS
$$
---
--- Implement local Table Rewriting policy:
---   public.foo is not allowed rewriting, ever
---   other tables are only allowed rewriting between 1am and 6am
---   unless they have more than 100 blocks
---
DECLARE
  table_oid oid := pg_event_trigger_table_rewrite_oid();
  current_hour integer := extract('hour' FROM current_time);
  pages integer;
  max_pages integer := 100;
BEGIN
  IF pg_event_trigger_table_rewrite_oid() = 'public.foo'::regclass
  THEN
        RAISE EXCEPTION 'you''re not allowed to rewrite the table %',
                        table_oid::regclass;
  END IF;

  SELECT INTO pages relpages FROM pg_class WHERE oid = table_oid;
  IF pages > max_pages
  THEN
        RAISE EXCEPTION 'rewrites only allowed for table with less than % pages',
                        max_pages;
  END IF;

  IF current_hour NOT BETWEEN 1 AND 6
  THEN
        RAISE EXCEPTION 'rewrites only allowed between 1am and 6am';
  END IF;
END;
$$;

CREATE EVENT TRIGGER no_rewrite_allowed
                  ON table_rewrite
   EXECUTE FUNCTION no_rewrite();
```

   

 

  

    
# A Database Login Event Trigger Example

    

      The event trigger on the login event can be
      useful for logging user logins, for verifying the connection and
      assigning roles according to current circumstances, or for session
      data initialization. It is very important that any event trigger using
      the login event checks whether or not the database is
      in recovery before performing any writes. Writing to a standby server
      will make it inaccessible.
    

    

      The following example demonstrates these options.

```
-- create test tables and roles
CREATE TABLE user_login_log (
  "user" text,
  "session_start" timestamp with time zone
);
CREATE ROLE day_worker;
CREATE ROLE night_worker;

-- the example trigger function
CREATE OR REPLACE FUNCTION init_session()
  RETURNS event_trigger SECURITY DEFINER
  LANGUAGE plpgsql AS
$$
DECLARE
  hour integer = EXTRACT('hour' FROM current_time at time zone 'utc');
  rec boolean;
BEGIN
-- 1. Forbid logging in between 2AM and 4AM.
IF hour BETWEEN 2 AND 4 THEN
  RAISE EXCEPTION 'Login forbidden';
END IF;

-- The checks below cannot be performed on standby servers so
-- ensure the database is not in recovery before we perform any
-- operations.
SELECT pg_is_in_recovery() INTO rec;
IF rec THEN
  RETURN;
END IF;

-- 2. Assign some roles. At daytime, grant the day_worker role, else the
-- night_worker role.
IF hour BETWEEN 8 AND 20 THEN
  EXECUTE 'REVOKE night_worker FROM ' || quote_ident(session_user);
  EXECUTE 'GRANT day_worker TO ' || quote_ident(session_user);
ELSE
  EXECUTE 'REVOKE day_worker FROM ' || quote_ident(session_user);
  EXECUTE 'GRANT night_worker TO ' || quote_ident(session_user);
END IF;

-- 3. Initialize user session data
CREATE TEMP TABLE session_storage (x float, y integer);
ALTER TABLE session_storage OWNER TO session_user;

-- 4. Log the connection time
INSERT INTO public.user_login_log VALUES (session_user, current_timestamp);

END;
$$;

-- trigger definition
CREATE EVENT TRIGGER init_session
  ON login
  EXECUTE FUNCTION init_session();
ALTER EVENT TRIGGER init_session ENABLE ALWAYS;
```
