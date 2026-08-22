# refint Extension Removed

   
    refint
   

   

    PostgreSQL 19 and below shipped an extension
    named refint (part of the spi contrib
    module) that provided the trigger functions
    check_primary_key and
    check_foreign_key as an early way to enforce
    referential integrity.  This functionality was long superseded by the
    built-in foreign key mechanism (see ),
    and the extension was removed in PostgreSQL 20.
