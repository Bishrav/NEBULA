# libpq Fast-Path Interface Removed

   
    fast path
   

   
    PQfn
   

   

    In PostgreSQL 19 and below,
    libpq supported a fast-path interface to send
    simple function calls to the server via the PQfn
    function.  This interface was unsafe and obsolete, and thus was removed in
    PostgreSQL 20.  The PQfn
    symbol still exists so that applications continue to link, but it now
    always fails.  One can achieve similar performance and greater
    functionality by setting up a prepared statement to define the function
    call.  Then, executing the statement with binary transmission of parameters
    and results substitutes for a fast-path function call.
