# Client Authentication

 
  client authentication
 

 

  When a client application connects to the database server, it
  specifies which PostgreSQL database user name it
  wants to connect as, much the same way one logs into a Unix computer
  as a particular user. Within the SQL environment the active database
  user name determines access privileges to database objects &mdash; see
   for more information. Therefore, it is
  essential to restrict which database users can connect.
 

 
  

   As explained in ,
   PostgreSQL actually does privilege
   management in terms of roles.  In this chapter, we
   consistently use database user to mean role with the
   LOGIN privilege.
  

 

 

  Authentication is the process by which the
  database server establishes the identity of the client, and by
  extension determines whether the client application (or the user
  who runs the client application) is permitted to connect with the
  database user name that was requested.
 

 

  PostgreSQL offers a number of different
  client authentication methods. The method used to authenticate a
  particular client connection can be selected on the basis of
  (client) host address, database, and user.
 

 

  PostgreSQL database user names are logically
  separate from user names of the operating system in which the server
  runs. If all the users of a particular server also have accounts on
  the server's machine, it makes sense to assign database user names
  that match their operating system user names. However, a server that
  accepts remote connections might have many database users who have no local
  operating system
  account, and in such cases there need be no connection between
  database user names and OS user names.
 

 

  
# The pg_hba.conf File

  
   pg_hba.conf
  

  

   Client authentication is controlled by a configuration file,
   which traditionally is named
   pg_hba.conf and is stored in the database
   cluster's data directory.
   (HBA stands for host-based authentication.) A default
   pg_hba.conf file is installed when the data
   directory is initialized by .  It is
   possible to place the authentication configuration file elsewhere,
   however; see the  configuration parameter.
  

  

   The pg_hba.conf file is read on start-up and when
   the main server process receives a
   SIGHUPSIGHUP
   signal. If you edit the file on an
   active system, you will need to signal the postmaster
   (using pg_ctl reload, calling the SQL function
   pg_reload_conf(), or using kill
   -HUP) to make it re-read the file.
  

  
   

    The preceding statement is not true on Microsoft Windows: there, any
    changes in the pg_hba.conf file are immediately
    applied by subsequent new connections.
   

  

  

   The system view
   pg_hba_file_rules
   can be helpful for pre-testing changes to the pg_hba.conf
   file, or for diagnosing problems if loading of the file did not have the
   desired effects.  Rows in the view with
   non-null error fields indicate problems in the
   corresponding lines of the file.
  

  

   The general format of the pg_hba.conf file is
   a set of records, one per line. Blank lines are ignored, as is any
   text after the # comment character.
   A record can be continued onto the next line by ending the line with
   a backslash. (Backslashes are not special except at the end of a line.)
   A record is made
   up of a number of fields which are separated by spaces and/or tabs.
   Fields can contain white space if the field value is double-quoted.
   Quoting one of the keywords in a database, user, or address field (e.g.,
   all or replication) makes the word lose its special
   meaning, and just match a database, user, or host with that name.
   Backslash line continuation applies even within quoted text or comments.
  

  

   Each authentication record specifies a connection type, a client IP address
   range (if relevant for the connection type), a database name, a user name,
   and the authentication method to be used for connections matching
   these parameters. The first record with a matching connection type,
   client address, requested database, and user name is used to perform
   authentication. There is no fall-through or
   backup: if one record is chosen and the authentication
   fails, subsequent records are not considered. If no record matches,
   access is denied.
  

  

   Each record can be an include directive or an authentication record.
   Include directives specify files that can be included, that contain
   additional records. The records will be inserted in place of the
   include directives. Include directives only contain two fields:
   include, include_if_exists or
   include_dir directive and the file or directory to be
   included. The file or directory can be a relative or absolute path, and can
   be double-quoted.  For the include_dir form, all files
   not starting with a . and ending with
   .conf will be included. Multiple files within an include
   directory are processed in file name order (according to C locale rules,
   i.e., numbers before letters, and uppercase letters before lowercase ones).
  

  

   A record can have several formats:

local               database  user  auth-method auth-options
host                database  user  address     auth-method  auth-options
hostssl             database  user  address     auth-method  auth-options
hostnossl           database  user  address     auth-method  auth-options
hostgssenc          database  user  address     auth-method  auth-options
hostnogssenc        database  user  address     auth-method  auth-options
host                database  user  IP-address  IP-mask      auth-method  auth-options
hostssl             database  user  IP-address  IP-mask      auth-method  auth-options
hostnossl           database  user  IP-address  IP-mask      auth-method  auth-options
hostgssenc          database  user  IP-address  IP-mask      auth-method  auth-options
hostnogssenc        database  user  IP-address  IP-mask      auth-method  auth-options
include             file
include_if_exists   file
include_dir         directory

   The meaning of the fields is as follows:

   

    

     
local

     

      

       This record matches connection attempts using Unix-domain
       sockets.  Without a record of this type, Unix-domain socket
       connections are disallowed.
      

     

    

    

     
host

     

      

       This record matches connection attempts made using TCP/IP.
       host records match
       SSL or non-SSL connection
       attempts as well as GSSAPI encrypted or
       non-GSSAPI encrypted connection attempts.
      

     
      

       Remote TCP/IP connections will not be possible unless
       the server is started with an appropriate value for the
        configuration parameter,
       since the default behavior is to listen for TCP/IP connections
       only on the local loopback address localhost.
      

     
     

    

    

     
hostssl

     

      

       This record matches connection attempts made using TCP/IP,
       but only when the connection is made with SSL
       encryption.
      

      

       To make use of this option the server must be built with
       SSL support. Furthermore,
       SSL must be enabled
       by setting the  configuration parameter (see
        for more information).
       Otherwise, the hostssl record is ignored except for
       logging a warning that it cannot match any connections.
      

     

    

    

     
hostnossl

     

      

       This record type has the opposite behavior of hostssl;
       it only matches connection attempts made over
       TCP/IP that do not use SSL.
      

     

    

    

     
hostgssenc

     

      

       This record matches connection attempts made using TCP/IP,
       but only when the connection is made with GSSAPI
       encryption.
      

      

       To make use of this option the server must be built with
       GSSAPI support.  Otherwise,
       the hostgssenc record is ignored except for logging
       a warning that it cannot match any connections.
      

     

    

    

     
hostnogssenc

     

      

       This record type has the opposite behavior of hostgssenc;
       it only matches connection attempts made over
       TCP/IP that do not use GSSAPI encryption.
      

     

    

    

     
database

     

      

       Specifies which database name(s) this record matches.  The value
       all specifies that it matches all databases.
       The value sameuser specifies that the record
       matches if the requested database has the same name as the
       requested user.  The value samerole specifies that
       the requested user must be a member of the role with the same
       name as the requested database.  (samegroup is an
       obsolete but still accepted spelling of samerole.)
       Superusers are not considered to be members of a role for the
       purposes of samerole unless they are explicitly
       members of the role, directly or indirectly, and not just by
       virtue of being a superuser.
       The value replication specifies that the record
       matches if a physical replication connection is requested, however, it
       doesn't match with logical replication connections. Note that physical
       replication connections do not specify any particular database whereas
       logical replication connections do specify it.
       Otherwise, this is the name of a specific
       PostgreSQL database or a regular expression.
       Multiple database names and/or regular expressions can be supplied by
       separating them with commas.
      

      

       If the database name starts with a slash (/), the
       remainder of the name is treated as a regular expression.
       (See  for details of
       PostgreSQL's regular expression syntax.)
      

      

       A separate file containing database names and/or regular expressions
       can be specified by preceding the file name with @.
      

     

    

    

     
user

     

      

       Specifies which database user name(s) this record
       matches. The value all specifies that it
       matches all users.  Otherwise, this is either the name of a specific
       database user, a regular expression when starting with a slash
       (/), or a group name preceded by +.
       (Recall that there is no real distinction between users and groups
       in PostgreSQL; a + mark really means
       match any of the roles that are directly or indirectly members
       of this role, while a name without a + mark matches
       only that specific role.) For this purpose, a superuser is only
       considered to be a member of a role if they are explicitly a member
       of the role, directly or indirectly, and not just by virtue of
       being a superuser.
       Multiple user names and/or regular expressions can be supplied by
       separating them with commas.
      

      

       If the user name starts with a slash (/), the
       remainder of the name is treated as a regular expression.
       (See  for details of
       PostgreSQL's regular expression syntax.)
      

      

       A separate file containing user names and/or regular expressions can
       be specified by preceding the file name with @.
      

     

    

    

     
address

     

      

       Specifies the client machine address(es) that this record
       matches.  This field can contain either a host name, an IP
       address range, or one of the special key words mentioned below.
      

      

       An IP address range is specified using standard numeric notation
       for the range's starting address, then a slash (/)
       and a CIDR mask length.  The mask
       length indicates the number of high-order bits of the client
       IP address that must match.  Bits to the right of this should
       be zero in the given IP address.
       There must not be any white space between the IP address, the
       /, and the CIDR mask length.
      

      

       Typical examples of an IPv4 address range specified this way are
       172.20.143.89/32 for a single host, or
       172.20.143.0/24 for a small network, or
       10.6.0.0/16 for a larger one.
       An IPv6 address range might look like ::1/128
       for a single host (in this case the IPv6 loopback address) or
       fe80::7a31:c1ff:0000:0000/96 for a small
       network.
       0.0.0.0/0 represents all
       IPv4 addresses, and ::0/0 represents
       all IPv6 addresses.
       To specify a single host, use a mask length of 32 for IPv4 or
       128 for IPv6.  In a network address, do not omit trailing zeroes.
      

      

       An entry given in IPv4 format will match only IPv4 connections,
       and an entry given in IPv6 format will match only IPv6 connections,
       even if the represented address is in the IPv4-in-IPv6 range.
      

      

       You can also write all to match any IP address,
       samehost to match any of the server's own IP
       addresses, or samenet to match any address in any
       subnet that the server is directly connected to.
      

      

       If a host name is specified (anything that is not an IP address
       range or a special key word is treated as a host name),
       that name is compared with the result of a reverse name
       resolution of the client's IP address (e.g., reverse DNS
       lookup, if DNS is used).  Host name comparisons are case
       insensitive.  If there is a match, then a forward name
       resolution (e.g., forward DNS lookup) is performed on the host
       name to check whether any of the addresses it resolves to are
       equal to the client's IP address.  If both directions match,
       then the entry is considered to match.  (The host name that is
       used in pg_hba.conf should be the one that
       address-to-name resolution of the client's IP address returns,
       otherwise the line won't be matched.  Some host name databases
       allow associating an IP address with multiple host names, but
       the operating system will only return one host name when asked
       to resolve an IP address.)
      

      

       A host name specification that starts with a dot
       (.) matches a suffix of the actual host
       name.  So .example.com would match
       foo.example.com (but not just
       example.com).
      

      

       When host names are specified
       in pg_hba.conf, you should make sure that
       name resolution is reasonably fast.  It can be of advantage to
       set up a local name resolution cache such
       as nscd.  Also, you may wish to enable the
       configuration parameter log_hostname to see
       the client's host name instead of the IP address in the log.
      

      

       These fields do not apply to local records.
      

      
       

        Users sometimes wonder why host names are handled
        in this seemingly complicated way, with two name resolutions
        including a reverse lookup of the client's IP address.  This
        complicates use of the feature in case the client's reverse DNS
        entry is not set up or yields some undesirable host name.
        It is done primarily for efficiency: this way, a connection attempt
        requires at most two resolver lookups, one reverse and one forward.
        If there is a resolver problem with some address, it becomes only
        that client's problem.  A hypothetical alternative
        implementation that only did forward lookups would have to
        resolve every host name mentioned in
        pg_hba.conf during every connection attempt.
        That could be quite slow if many names are listed.
        And if there is a resolver problem with one of the host names,
        it becomes everyone's problem.
       

       

        Also, a reverse lookup is necessary to implement the suffix
        matching feature, because the actual client host name needs to
        be known in order to match it against the pattern.
       

       

        Note that this behavior is consistent with other popular
        implementations of host name-based access control, such as the
        Apache HTTP Server and TCP Wrappers.
       

      
     

    

    

     
IP-address

     
IP-mask

     

      

       These two fields can be used as an alternative to the
       IP-address/mask-length
       notation.  Instead of
       specifying the mask length, the actual mask is specified in a
       separate column. For example, 255.0.0.0 represents an IPv4
       CIDR mask length of 8, and 255.255.255.255 represents a
       CIDR mask length of 32.
      

      

       These fields do not apply to local records.
      

     

    

    

     
auth-method

     

      

       Specifies the authentication method to use when a connection matches
       this record. The possible choices are summarized here; details
       are in .  All the options
       are lower case and treated case sensitively, so even acronyms like
       ldap must be specified as lower case.

       

        

         
trust

         

         

          Allow the connection unconditionally. This method
          allows anyone that can connect to the
          PostgreSQL database server to login as
          any PostgreSQL user they wish,
          without the need for a password or any other authentication.  See  for details.
         

        

       

       

        
reject

        

         

          Reject the connection unconditionally. This is useful for
          filtering out certain hosts from a group, for example a
          reject line could block a specific host from connecting,
          while a later line allows the remaining hosts in a specific
          network to connect.
         

        

       

       

        
scram-sha-256

        

         

          Perform SCRAM-SHA-256 authentication to verify the user's
          password. See  for details.
         

        

       

       

        
md5

        

         

          Perform SCRAM-SHA-256 or MD5 authentication to verify the
          user's password. See 
          for details.
         

         
          

           Support for MD5-encrypted passwords is deprecated and will be
           removed in a future release of
           PostgreSQL.  Refer to
            for details about migrating to
           another password type.
          

         
        

       

       

        
password

        

         

          Require the client to supply an unencrypted password for
          authentication.
          Since the password is sent in clear text over the
          network, this should not be used on untrusted networks.
          See  for details.
         

        

       

       

        
gss

        

         

          Use GSSAPI to authenticate the user. This is only
          available for TCP/IP connections. See  for details.  It can be used in conjunction
          with GSSAPI encryption.
         

        

       

       

        
sspi

        

         

          Use SSPI to authenticate the user. This is only
          available on Windows. See  for details.
         

        

       

       

        
ident

        

         

          Obtain the operating system user name of the client
          by contacting the ident server on the client
          and check if it matches the requested database user name.
          Ident authentication can only be used on TCP/IP
          connections. When specified for local connections, peer
          authentication will be used instead.
          See  for details.
         

        

       

       

        
peer

        

         

          Obtain the client's operating system user name from the operating
          system and check if it matches the requested database user name.
          This is only available for local connections.
          See  for details.
         

        

       

       

        
ldap

        

         

          Authenticate using an LDAP server. See  for details.
         

        

       

       

        
cert

        

         

          Authenticate using SSL client certificates. See
           for details.
         

        

       

       

        
pam

        

         

          Authenticate using the Pluggable Authentication Modules
          (PAM) service provided by the operating system.  See  for details.
         

        

       

       

        
bsd

        

         

          Authenticate using the BSD Authentication service provided by the
          operating system. See  for details.
         

        

       

       

        
oauth

        

         

          Authorize and optionally authenticate using a third-party OAuth 2.0
          identity provider. See  for details.
         

        

       

      

      

     

    

    

     
auth-options

     

      

       After the auth-method field, there can be field(s) of
       the form name=value that
       specify options for the authentication method. Details about which
       options are available for which authentication methods appear below.
      

      

       In addition to the method-specific options listed below, there is a
       method-independent authentication option clientcert, which
       can be specified in any hostssl record.
       This option can be set to verify-ca or
       verify-full. Both options require the client
       to present a valid (trusted) SSL certificate, while
       verify-full additionally enforces that the
       cn (Common Name) in the certificate matches
       the username or an applicable mapping.
       This behavior is similar to the cert authentication
       method (see ) but enables pairing
       the verification of client certificates with any authentication
       method that supports hostssl entries.
      

      

       On any record using client certificate authentication (i.e. one
       using the cert authentication method or one
       using the clientcert option), you can specify
       which part of the client certificate credentials to match using
       the clientname option. This option can have one
       of two values. If you specify clientname=CN, which
       is the default, the username is matched against the certificate's
       Common Name (CN). If instead you specify
       clientname=DN the username is matched against the
       entire Distinguished Name (DN) of the certificate.
       This option is probably best used in conjunction with a username map.
       The comparison is done with the DN in
       RFC 2253
       format. To see the DN of a client certificate
       in this format, do

```
openssl x509 -in myclient.crt -noout -subject -nameopt RFC2253 | sed "s/^subject=//"
```

        Care needs to be taken when using this option, especially when using
        regular expression matching against the DN.
      

     

    

    

     
include

     

      

       This line will be replaced by the contents of the given file.
      

     

    

    

     
include_if_exists

     

      

       This line will be replaced by the content of the given file if the
       file exists. Otherwise, a message is logged to indicate that the file
       has been skipped.
      

     

    

    

     
include_dir

     

      

       This line will be replaced by the contents of all the files found in
       the directory, if they don't start with a . and end
       with .conf, processed in file name order (according
       to C locale rules, i.e., numbers before letters, and uppercase letters
       before lowercase ones).
      

     

    

   

  

  

   Files included by @ constructs are read as lists of names,
   which can be separated by either whitespace or commas.  Comments are
   introduced by #, just as in
   pg_hba.conf, and nested @ constructs are
   allowed.  Unless the file name following @ is an absolute
   path, it is taken to be relative to the directory containing the
   referencing file.
  

  

   Since the pg_hba.conf records are examined
   sequentially for each connection attempt, the order of the records is
   significant. Typically, earlier records will have tight connection
   match parameters and weaker authentication methods, while later
   records will have looser match parameters and stronger authentication
   methods. For example, one might wish to use trust
   authentication for local TCP/IP connections but require a password for
   remote TCP/IP connections. In this case a record specifying
   trust authentication for connections from 127.0.0.1 would
   appear before a record specifying password authentication for a wider
   range of allowed client IP addresses.
  

  
   

    To connect to a particular database, a user must not only pass the
    pg_hba.conf checks, but must have the
    CONNECT privilege for the database.  If you wish to
    restrict which users can connect to which databases, it's usually
    easier to control this by granting/revoking CONNECT privilege
    than to put the rules in pg_hba.conf entries.
   

  

  

   Some examples of pg_hba.conf entries are shown in
   . See the next section for details on the
   different authentication methods.
  

   
    
# Example pg_hba.conf Entries

```
# Allow any user on the local system to connect to any database with
# any database user name using Unix-domain sockets (the default for local
# connections).
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
local   all             all                                     trust

# The same using local loopback TCP/IP connections.
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    all             all             127.0.0.1/32            trust

# The same as the previous line, but using a separate netmask column
#
# TYPE  DATABASE        USER            IP-ADDRESS      IP-MASK             METHOD
host    all             all             127.0.0.1       255.255.255.255     trust

# The same over IPv6.
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    all             all             ::1/128                 trust

# The same using a host name (would typically cover both IPv4 and IPv6).
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    all             all             localhost               trust

# The same using a regular expression for DATABASE, that allows connection
# to any databases with a name beginning with "db" and finishing with a
# number using two to four digits (like "db1234" or "db12").
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    "/^db\d{2,4}$"  all             localhost               trust

# Allow any user from any host with IP address 192.168.93.x to connect
# to database "postgres" as the same user name that ident reports for
# the connection (typically the operating system user name).
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    postgres        all             192.168.93.0/24         ident

# Allow any user from host 192.168.12.10 to connect to database
# "postgres" if the user's password is correctly supplied.
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    postgres        all             192.168.12.10/32        scram-sha-256

# Allow any user from hosts in the example.com domain to connect to
# any database if the user's password is correctly supplied.
#
# Require SCRAM authentication for most users, but make an exception
# for user 'mike', who uses an older client that doesn't support SCRAM
# authentication.
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    all             mike            .example.com            md5
host    all             all             .example.com            scram-sha-256

# In the absence of preceding "host" lines, these three lines will
# reject all connections from 192.168.54.1 (since that entry will be
# matched first), but allow GSSAPI-encrypted connections from anywhere else
# on the Internet.  The zero mask causes no bits of the host IP address to
# be considered, so it matches any host.  Unencrypted GSSAPI connections
# (which "fall through" to the third line since "hostgssenc" only matches
# encrypted GSSAPI connections) are allowed, but only from 192.168.12.10.
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    all             all             192.168.54.1/32         reject
hostgssenc all          all             0.0.0.0/0               gss
host    all             all             192.168.12.10/32        gss

# Allow users from 192.168.x.x hosts to connect to any database, if
# they pass the ident check.  If, for example, ident says the user is
# "bryanh" and he requests to connect as PostgreSQL user "guest1", the
# connection is allowed if there is an entry in pg_ident.conf for map
# "omicron" that says "bryanh" is allowed to connect as "guest1".
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    all             all             192.168.0.0/16          ident map=omicron

# If these are the only four lines for local connections, they will
# allow local users to connect only to their own databases (databases
# with the same name as their database user name) except for users whose
# name end with "helpdesk", administrators and members of role "support",
# who can connect to all databases.  The file $PGDATA/admins contains a
# list of names of administrators.  Passwords are required in all cases.
#
# TYPE  DATABASE        USER            ADDRESS                 METHOD
local   sameuser        all                                     scram-sha-256
local   all             /^.*helpdesk$                           scram-sha-256
local   all             @admins                                 scram-sha-256
local   all             +support                                scram-sha-256

# The last two lines above can be combined into a single line:
local   all             @admins,+support                        scram-sha-256

# The database column can also use lists and file names:
local   db1,db2,@demodbs  all                                   scram-sha-256
```

   
 

 

  
# User Name Maps

  
   User name maps
  

  

   When using an external authentication system such as Ident or GSSAPI,
   the name of the operating system user that initiated the connection
   might not be the same as the database user (role) that is to be used.
   In this case, a user name map can be applied to map the operating system
   user name to a database user.  To use user name mapping, specify
   map=map-name
   in the options field in pg_hba.conf. This option is
   supported for all authentication methods that receive external user names.
   Since different mappings might be needed for different connections,
   the name of the map to be used is specified in the
   map-name parameter in pg_hba.conf
   to indicate which map to use for each individual connection.
  

  

   User name maps are defined in the ident map file, which by default is named
   pg_ident.confpg_ident.conf
   and is stored in the
   cluster's data directory.  (It is possible to place the map file
   elsewhere, however; see the 
   configuration parameter.)
   The ident map file contains lines of the general forms:

map-name system-username database-username
include file
include_if_exists file
include_dir directory

   Comments, whitespace and line continuations are handled in the same way as in
   pg_hba.conf.  The
   map-name is an arbitrary name that will be used to
   refer to this mapping in pg_hba.conf. The other
   two fields specify an operating system user name and a matching
   database user name. The same map-name can be
   used repeatedly to specify multiple user-mappings within a single map.
  

  

   As for pg_hba.conf, the lines in this file can
   be include directives, following the same rules.
  

  

   The pg_ident.conf file is read on start-up and
   when the main server process receives a
   SIGHUPSIGHUP
   signal. If you edit the file on an
   active system, you will need to signal the postmaster
   (using pg_ctl reload, calling the SQL function
   pg_reload_conf(), or using kill
   -HUP) to make it re-read the file.
  

  

   The system view
   pg_ident_file_mappings
   can be helpful for pre-testing changes to the
   pg_ident.conf file, or for diagnosing problems if
   loading of the file did not have the desired effects.  Rows in the view with
   non-null error fields indicate problems in the
   corresponding lines of the file.
  

  

   There is no restriction regarding how many database users a given
   operating system user can correspond to, nor vice versa.  Thus, entries
   in a map should be thought of as meaning this operating system
   user is allowed to connect as this database user, rather than
   implying that they are equivalent.  The connection will be allowed if
   there is any map entry that pairs the user name obtained from the
   external authentication system with the database user name that the
   user has requested to connect as. The value all
   can be used as the database-username to specify
   that if the system-username matches, then this
   user is allowed to log in as any of the existing database users. Quoting
   all makes the keyword lose its special meaning.
  

  

   If the database-username begins with a
   + character, then the operating system user can login as
   any user belonging to that role, similarly to how user names beginning with
   + are treated in pg_hba.conf.
   Thus, a + mark means match any of the roles that
   are directly or indirectly members of this role, while a name
   without a + mark matches only that specific role. Quoting
   a username starting with a + makes the
   + lose its special meaning.
  

  

   If the system-username field starts with a slash (/),
   the remainder of the field is treated as a regular expression.
   (See  for details of
   PostgreSQL's regular expression syntax.)  The regular
   expression can include a single capture, or parenthesized subexpression.
   The portion of the system user name that matched the capture can then
   be referenced in the database-username
   field as \1 (backslash-one).  This allows the mapping of
   multiple user names in a single line, which is particularly useful for
   simple syntax substitutions.  For example, these entries

```
mymap   /^(.*)@mydomain\.com$      \1
mymap   /^(.*)@otherdomain\.com$   guest
```

   will remove the domain part for users with system user names that end with
   @mydomain.com, and allow any user whose system name ends with
   @otherdomain.com to log in as guest.
   Quoting a database-username containing
   \1 does not make
   \1 lose its special meaning.
  

  

   If the database-username field starts with
   a slash (/), the remainder of the field is treated
   as a regular expression.
   When the database-username field is a regular
   expression, it is not possible to use \1 within it to
   refer to a capture from the system-username
   field.
  

  
   

    Keep in mind that by default, a regular expression can match just part of
    a string.  It's usually wise to use ^ and $, as
    shown in the above example, to force the match to be to the entire
    system user name.
   

  

  

   A pg_ident.conf file that could be used in
   conjunction with the pg_hba.conf file in  is shown in . In this example, anyone
   logged in to a machine on the 192.168 network that does not have the
   operating system user name bryanh, ann, or
   robert would not be granted access. Unix user
   robert would only be allowed access when he tries to
   connect as PostgreSQL user bob, not
   as robert or anyone else. ann would
   only be allowed to connect as ann. User
   bryanh would be allowed to connect as either
   bryanh or as guest1.
  

  
   
# An Example pg_ident.conf File

```
# MAPNAME       SYSTEM-USERNAME         PG-USERNAME

omicron         bryanh                  bryanh
omicron         ann                     ann
# bob has user name robert on these machines
omicron         robert                  bob
# bryanh can also connect as guest1
omicron         bryanh                  guest1
```

  
 

 

  
# Authentication Methods

  

   PostgreSQL provides various methods for
   authenticating users:

   

    

     

      Trust authentication, which
      simply trusts that users are who they say they are.
     

    

    

     

      Password authentication, which
      requires that users send a password.
     

    

    

     

      GSSAPI authentication, which
      relies on a GSSAPI-compatible security library.  Typically this is
      used to access an authentication server such as a Kerberos or
      Microsoft Active Directory server.
     

    

    

     

      SSPI authentication, which
      uses a Windows-specific protocol similar to GSSAPI.
     

    

    

     

      Ident authentication, which
      relies on an Identification Protocol
      (RFC 1413)
      service on the client's machine.  (On local Unix-socket connections,
      this is treated as peer authentication.)
     

    

    

     

      Peer authentication, which
      relies on operating system facilities to identify the process at the
      other end of a local connection.  This is not supported for remote
      connections.
     

    

    

     

      LDAP authentication, which
      relies on an LDAP authentication server.
     

    

    

     

      Certificate authentication, which
      requires an SSL connection and authenticates users by checking the
      SSL certificate they send.
     

    

    

     

      PAM authentication, which
      relies on a PAM (Pluggable Authentication Modules) library.
     

    

    

     

      BSD authentication, which
      relies on the BSD Authentication framework (currently available
      only on OpenBSD).
     

    

    

     

      OAuth authorization/authentication,
      which relies on an external OAuth 2.0 identity provider.
     

    

   

  

  

   Peer authentication is usually recommendable for local connections,
   though trust authentication might be sufficient in some circumstances.
   Password authentication is the easiest choice for remote connections.
   All the other options require some kind of external security
   infrastructure (usually an authentication server or a certificate
   authority for issuing SSL certificates), or are platform-specific.
  

  

   The following sections describe each of these authentication methods
   in more detail.
  

 

  

   
# Trust Authentication

   

    When trust authentication is specified,
    PostgreSQL assumes that anyone who can
    connect to the server is authorized to access the database with
    whatever database user name they specify (even superuser names).
    Of course, restrictions made in the database and
    user columns still apply.
    This method should only be used when there is adequate
    operating-system-level protection on connections to the server.
   

   

    trust authentication is appropriate and very
    convenient for local connections on a single-user workstation.  It
    is usually not appropriate by itself on a multiuser
    machine.  However, you might be able to use trust even
    on a multiuser machine, if you restrict access to the server's
    Unix-domain socket file using file-system permissions.  To do this, set the
    unix_socket_permissions (and possibly
    unix_socket_group) configuration parameters as
    described in .  Or you
    could set the unix_socket_directories
    configuration parameter to place the socket file in a suitably
    restricted directory.
   

   

    Setting file-system permissions only helps for Unix-socket connections.
    Local TCP/IP connections are not restricted by file-system permissions.
    Therefore, if you want to use file-system permissions for local security,
    remove the host ... 127.0.0.1 ... line from
    pg_hba.conf, or change it to a
    non-trust authentication method.
   

   

    trust authentication is only suitable for TCP/IP connections
    if you trust every user on every machine that is allowed to connect
    to the server by the pg_hba.conf lines that specify
    trust.  It is seldom reasonable to use trust
    for any TCP/IP connections other than those from localhost (127.0.0.1).
   

  

  

   
# Password Authentication

   
    MD5
   
   
    SCRAM
   
   
    password
    authentication
   

   

    There are several password-based authentication methods.  These methods
    operate similarly but differ in how the users' passwords are stored on the
    server and how the password provided by a client is sent across the
    connection.
   

   

    

     
scram-sha-256

     

      

       The method scram-sha-256 performs SCRAM-SHA-256
       authentication, as described in
       RFC 7677.  It
       is a challenge-response scheme that prevents password sniffing on
       untrusted connections and supports storing passwords on the server in a
       cryptographically hashed form that is thought to be secure.
      

      

       This is the most secure of the currently provided methods, but it is
       not supported by older client libraries.
      

     

    

    

     
md5

     

      

       The method md5 uses a custom less secure challenge-response
       mechanism.  It prevents password sniffing and avoids storing passwords
       on the server in plain text but provides no protection if an attacker
       manages to steal the password hash from the server.  Also, the MD5 hash
       algorithm is nowadays no longer considered secure against determined
       attacks.
      

      

       To ease transition from the md5 method to the newer
       SCRAM method, if md5 is specified as a method
       in pg_hba.conf but the user's password on the
       server is encrypted for SCRAM (see below), then SCRAM-based
       authentication will automatically be chosen instead.
      

      
       

        Support for MD5-encrypted passwords is deprecated and will be removed
        in a future release of PostgreSQL.  Refer to
        the text below for details about migrating to another password type.
       

      
     

    

    

     
password

     

      

       The method password sends the password in clear-text and is
       therefore vulnerable to password sniffing attacks. It should
       always be avoided if possible. If the connection is protected by SSL
       encryption then password can be used safely, though.
       (Though SSL certificate authentication might be a better choice if one
       is depending on using SSL).
      

     

    

   

   

    PostgreSQL database passwords are
    separate from operating system user passwords. The password for
    each database user is stored in the pg_authid system
    catalog. Passwords can be managed with the SQL commands
     and
    ,
    e.g., CREATE ROLE foo WITH LOGIN PASSWORD 'secret',
    or the psql
    command \password.
    If no password has been set up for a user, the stored password
    is null and password authentication will always fail for that user.
   

   

    The availability of the different password-based authentication methods
    depends on how a user's password on the server is encrypted (or hashed,
    more accurately).  This is controlled by the configuration
    parameter  at the time the
    password is set.  If a password was encrypted using
    the scram-sha-256 setting, then it can be used for the
    authentication methods scram-sha-256
    and password (but password transmission will be in
    plain text in the latter case).  The authentication method
    specification md5 will automatically switch to using
    the scram-sha-256 method in this case, as explained
    above, so it will also work.  If a password was encrypted using
    the md5 setting, then it can be used only for
    the md5 and password authentication
    method specifications (again, with the password transmitted in plain text
    in the latter case).  (Previous PostgreSQL releases supported storing the
    password on the server in plain text.  This is no longer possible.)  To
    check the currently stored password hashes, see the system
    catalog pg_authid.
   

   

    To upgrade an existing installation from md5
    to scram-sha-256, after having ensured that all client
    libraries in use are new enough to support SCRAM,
    set password_encryption = 'scram-sha-256'
    in postgresql.conf, make all users set new passwords,
    and change the authentication method specifications
    in pg_hba.conf to scram-sha-256.
   

  

  

   
# GSSAPI Authentication

   
    GSSAPI
   

   

    GSSAPI is an industry-standard protocol
    for secure authentication defined in
    RFC 2743.
    PostgreSQL
    supports GSSAPI for authentication,
    communications encryption, or both.
    GSSAPI provides automatic authentication
    (single sign-on) for systems that support it. The authentication itself is
    secure.  If GSSAPI encryption
    or SSL encryption is
    used, the data sent along the database connection will be encrypted;
    otherwise, it will not.
   

   

    GSSAPI support has to be enabled when PostgreSQL is built;
    see  for more information.
   

   

    When GSSAPI uses
    Kerberos, it uses a standard service
    principal (authentication identity) name in the format
    servicename/hostname@realm.
    The principal name used by a particular installation is not encoded in
    the PostgreSQL server in any way; rather it
    is specified in the keytab file that the server
    reads to determine its identity.  If multiple principals are listed in
    the keytab file, the server will accept any one of them.
    The server's realm name is the preferred realm specified in the Kerberos
    configuration file(s) accessible to the server.
   

   

    When connecting, the client must know the principal name of the server
    it intends to connect to.  The servicename
    part of the principal is ordinarily postgres,
    but another value can be selected via libpq's
     connection parameter.
    The hostname part is the fully qualified
    host name that libpq is told to connect to.
    The realm name is the preferred realm specified in the Kerberos
    configuration file(s) accessible to the client.
   

   

    The client will also have a principal name for its own identity
    (and it must have a valid ticket for this principal).  To
    use GSSAPI for authentication, the client
    principal must be associated with
    a PostgreSQL database user name.
    The pg_ident.conf configuration file can be used
    to map principals to user names; for example,
    pgusername@realm could be mapped to just pgusername.
    Alternatively, you can use the full username@realm principal as
    the role name in PostgreSQL without any mapping.
   

   

    PostgreSQL also supports mapping
    client principals to user names by just stripping the realm from
    the principal.  This method is supported for backwards compatibility and is
    strongly discouraged as it is then impossible to distinguish different users
    with the same user name but coming from different realms.  To enable this,
    set include_realm to 0.  For simple single-realm
    installations, doing that combined with setting the
    krb_realm parameter (which checks that the principal's realm
    matches exactly what is in the krb_realm parameter)
    is still secure; but this is a
    less capable approach compared to specifying an explicit mapping in
    pg_ident.conf.
   

   

    The location of the server's keytab file is specified by the  configuration parameter.
    For security reasons, it is recommended to use a separate keytab
    just for the PostgreSQL server rather
    than allowing the server to read the system keytab file.
    Make sure that your server keytab file is readable (and preferably
    only readable, not writable) by the PostgreSQL
    server account.  (See also .)
   

   

    The keytab file is generated using the Kerberos software; see the
    Kerberos documentation for details. The following example shows
    doing this using the kadmin tool of
    MIT Kerberos:

```
kadmin% addprinc -randkey postgres/server.my.domain.org
kadmin% ktadd -k krb5.keytab postgres/server.my.domain.org
```

   

   

    The following authentication options are supported for
    the GSSAPI authentication method:
    

     

      
include_realm

      

       

        If set to 0, the realm name from the authenticated user principal is
        stripped off before being passed through the user name mapping
        (). This is discouraged and is
        primarily available for backwards compatibility, as it is not secure
        in multi-realm environments unless krb_realm is
        also used.  It is recommended to
        leave include_realm set to the default (1) and to
        provide an explicit mapping in pg_ident.conf to convert
        principal names to PostgreSQL user names.
       

      

     

     

      
map

      

       

        Allows mapping from client principals to database user names. See
         for details.  For a GSSAPI/Kerberos
        principal, such as username@EXAMPLE.COM (or, less
        commonly, username/hostbased@EXAMPLE.COM), the
        user name used for mapping is
        username@EXAMPLE.COM (or
        username/hostbased@EXAMPLE.COM, respectively),
        unless include_realm has been set to 0, in which case
        username (or username/hostbased)
        is what is seen as the system user name when mapping.
       

      

     

     

      
krb_realm

      

       

        Sets the realm to match user principal names against. If this parameter
        is set, only users of that realm will be accepted.  If it is not set,
        users of any realm can connect, subject to whatever user name mapping
        is done.
       

      

     

    

   

   

    In addition to these settings, which can be different for
    different pg_hba.conf entries, there is the
    server-wide  configuration
    parameter.  If that is set to true, client principals are matched to
    user map entries case-insensitively.  krb_realm, if
    set, is also matched case-insensitively.
   

  

  

   
# SSPI Authentication

   
    SSPI
   

   

    SSPI is a Windows
    technology for secure authentication with single sign-on.
    PostgreSQL will use SSPI in
    negotiate mode, which will use
    Kerberos when possible and automatically
    fall back to NTLM in other cases.
    SSPI and GSSAPI
    interoperate as clients and servers, e.g., an
    SSPI client can authenticate to an
    GSSAPI server.  It is recommended to use
    SSPI on Windows clients and servers and
    GSSAPI on non-Windows platforms.
   

   

    When using Kerberos authentication,
    SSPI works the same way
    GSSAPI does; see 
    for details.
   

   

    The following configuration options are supported for SSPI:
    

     

      
include_realm

      

       

        If set to 0, the realm name from the authenticated user principal is
        stripped off before being passed through the user name mapping
        (). This is discouraged and is
        primarily available for backwards compatibility, as it is not secure
        in multi-realm environments unless krb_realm is
        also used.  It is recommended to
        leave include_realm set to the default (1) and to
        provide an explicit mapping in pg_ident.conf to convert
        principal names to PostgreSQL user names.
       

      

     

     

      
compat_realm

      

       

        If set to 1, the domain's SAM-compatible name (also known as the
        NetBIOS name) is used for the include_realm
        option. This is the default. If set to 0, the true realm name from
        the Kerberos user principal name is used.
       

       

        Do not disable this option unless your server runs under a domain
        account (this includes virtual service accounts on a domain member
        system) and all clients authenticating through SSPI are also using
        domain accounts, or authentication will fail.
       

      

     

     

      
upn_username

      

       

        If this option is enabled along with compat_realm,
        the user name from the Kerberos UPN is used for authentication. If
        it is disabled (the default), the SAM-compatible user name is used.
        By default, these two names are identical for new user accounts.
       

       

        Note that libpq uses the SAM-compatible name if no
        explicit user name is specified. If you use
        libpq or a driver based on it, you should
        leave this option disabled or explicitly specify user name in the
        connection string.
       

      

     

     

      
map

      

       

        Allows for mapping between system and database user names. See
         for details.  For an SSPI/Kerberos
        principal, such as username@EXAMPLE.COM (or, less
        commonly, username/hostbased@EXAMPLE.COM), the
        user name used for mapping is
        username@EXAMPLE.COM (or
        username/hostbased@EXAMPLE.COM, respectively),
        unless include_realm has been set to 0, in which case
        username (or username/hostbased)
        is what is seen as the system user name when mapping.
       

      

     

     

      
krb_realm

      

       

        Sets the realm to match user principal names against. If this parameter
        is set, only users of that realm will be accepted.  If it is not set,
        users of any realm can connect, subject to whatever user name mapping
        is done.
       

      

     

    

   

  

  

   
# Ident Authentication

   
    ident
   

   

    The ident authentication method works by obtaining the client's
    operating system user name from an ident server and using it as
    the allowed database user name (with an optional user name mapping).
    This is only supported on TCP/IP connections.
   

   
    

     When ident is specified for a local (non-TCP/IP) connection,
     peer authentication (see ) will be
     used instead.
    

   

   

    The following configuration options are supported for ident:
    

     

      
map

      

       

        Allows for mapping between system and database user names. See
         for details.
       

      

     

    

   

   

    The Identification Protocol is described in
    RFC 1413.
    Virtually every Unix-like
    operating system ships with an ident server that listens on TCP
    port 113 by default. The basic functionality of an ident server
    is to answer questions like What user initiated the
    connection that goes out of your port X
    and connects to my port Y?.
    Since PostgreSQL knows both X and
    Y when a physical connection is established, it
    can interrogate the ident server on the host of the connecting
    client and can theoretically determine the operating system user
    for any given connection.
   

   

    The drawback of this procedure is that it depends on the integrity
    of the client: if the client machine is untrusted or compromised,
    an attacker could run just about any program on port 113 and
    return any user name they choose. This authentication method is
    therefore only appropriate for closed networks where each client
    machine is under tight control and where the database and system
    administrators operate in close contact. In other words, you must
    trust the machine running the ident server.
    Heed the warning:
    

     RFC 1413
     

      The Identification Protocol is not intended as an authorization
      or access control protocol.
     

    

   

   

    Some ident servers have a nonstandard option that causes the returned
    user name to be encrypted, using a key that only the originating
    machine's administrator knows.  This option must not be
    used when using the ident server with PostgreSQL,
    since PostgreSQL does not have any way to decrypt the
    returned string to determine the actual user name.
   

  

  

   
# Peer Authentication

   
    peer
   

   

    The peer authentication method works by obtaining the client's
    operating system user name from the kernel and using it as the
    allowed database user name (with optional user name mapping). This
    method is only supported on local connections.
   

   

    The following configuration options are supported for peer:
    

     

      
map

      

       

        Allows for mapping between system and database user names. See
         for details.
       

      

     

    

   

   

    Peer authentication is only available on operating systems providing
    the getpeereid() function, the SO_PEERCRED
    socket parameter, or similar mechanisms.  Currently that includes
    Linux,
    most flavors of BSD including
    macOS,
    and Solaris.
   

  

  

   
# LDAP Authentication

   
    LDAP
   

   

    This authentication method operates similarly to
    password except that it uses LDAP
    as the password verification method. LDAP is used only to validate
    the user name/password pairs. Therefore the user must already
    exist in the database before LDAP can be used for
    authentication.
   

   

    LDAP authentication can operate in two modes. In the first mode,
    which we will call the simple bind mode,
    the server will bind to the distinguished name constructed as
    prefix username suffix.
    Typically, the prefix parameter is used to specify
    cn=, or DOMAIN\ in an Active
    Directory environment.  suffix is used to specify the
    remaining part of the DN in a non-Active Directory environment.
   

   

    In the second mode, which we will call the search+bind mode,
    the server first binds to the LDAP directory with
    a fixed user name and password, specified with ldapbinddn
    and ldapbindpasswd, and performs a search for the user trying
    to log in to the database. If no user and password is configured, an
    anonymous bind will be attempted to the directory. The search will be
    performed over the subtree at ldapbasedn, and will try to
    do an exact match of the attribute specified in
    ldapsearchattribute.
    Once the user has been found in
    this search, the server re-binds to the directory as
    this user, using the password specified by the client, to verify that the
    login is correct. This mode is the same as that used by LDAP authentication
    schemes in other software, such as Apache mod_authnz_ldap and pam_ldap.
    This method allows for significantly more flexibility
    in where the user objects are located in the directory, but will cause
    two additional requests to the LDAP server to be made.
   

   

    The following configuration options are used in both modes:
    

     

      
ldapserver

      

       

        Names or IP addresses of LDAP servers to connect to. Multiple
        servers may be specified, separated by spaces.
       

      

     

     

      
ldapport

      

       

        Port number on LDAP server to connect to. If no port is specified,
        the LDAP library's default port setting will be used.
       

      

     

     

      
ldapscheme

      

       

        Set to ldaps to use LDAPS.  This is a non-standard
        way of using LDAP over SSL, supported by some LDAP server
        implementations.  See also the ldaptls option for
        an alternative.
       

      

     

     

      
ldaptls

      

       

        Set to 1 to make the connection between PostgreSQL and the LDAP server
        use TLS encryption.  This uses the StartTLS
        operation per RFC 4513.
        See also the ldapscheme option for an alternative.
       

      

     

    

   

   

    Note that using ldapscheme or
    ldaptls only encrypts the traffic between the
    PostgreSQL server and the LDAP server.  The connection between the
    PostgreSQL server and the PostgreSQL client will still be unencrypted
    unless SSL is used there as well.
   

   

    The following options are used in simple bind mode only:
    

     

      
ldapprefix

      

       

        String to prepend to the user name when forming the DN to bind as,
        when doing simple bind authentication.
       

      

     

     

      
ldapsuffix

      

       

        String to append to the user name when forming the DN to bind as,
        when doing simple bind authentication.
       

      

     

    

   

   

    The following options are used in search+bind mode only:
    

     

      
ldapbasedn

      

       

        Root DN to begin the search for the user in, when doing search+bind
        authentication.
       

      

     

     

      
ldapbinddn

      

       

        DN of user to bind to the directory with to perform the search when
        doing search+bind authentication.
       

      

     

     

      
ldapbindpasswd

      

       

        Password for user to bind to the directory with to perform the search
        when doing search+bind authentication.
       

      

      

      

       
ldapsearchattribute

       

        

         Attribute to match against the user name in the search when doing
         search+bind authentication.  If no attribute is specified, the
         uid attribute will be used.
        

       

      

      

       
ldapsearchfilter

       

        

         The search filter to use when doing search+bind authentication.
         Occurrences of $username will be replaced with the
         user name.  This allows for more flexible search filters than
         ldapsearchattribute.
        

       

      

     

    

    

     The following option may be used as an alternative way to write some of the
     above LDAP options in a more compact and standard form:
     

      

       
ldapurl

       

        

         An RFC 4516
         LDAP URL.  The format is

ldap[s]://host[:port]/basedn[?[attribute][?[scope][?[filter]]]]

         scope must be one
         of base, one, sub,
         typically the last.  (The default is base, which
         is normally not useful in this application.)  attribute can
         nominate a single attribute, in which case it is used as a value for
         ldapsearchattribute.  If
         attribute is empty then
         filter can be used as a value for
         ldapsearchfilter.
        

        

         The URL scheme ldaps chooses the LDAPS method for
         making LDAP connections over SSL, equivalent to using
         ldapscheme=ldaps.  To use encrypted LDAP
         connections using the StartTLS operation, use the
         normal URL scheme ldap and specify the
         ldaptls option in addition to
         ldapurl.
        

        

         For non-anonymous binds, ldapbinddn
         and ldapbindpasswd must be specified as separate
         options.
        

        

         LDAP URLs are currently only supported with
         OpenLDAP, not on Windows.
        

       

      

    

   

   

    It is an error to mix configuration options for simple bind with options
    for search+bind.  To use ldapurl in simple bind mode, the
    URL must not contain a basedn or query elements.
   

   

    When using search+bind mode, the search can be performed using a single
    attribute specified with ldapsearchattribute, or using
    a custom search filter specified with
    ldapsearchfilter.
    Specifying ldapsearchattribute=foo is equivalent to
    specifying ldapsearchfilter="(foo=$username)".  If neither
    option is specified the default is
    ldapsearchattribute=uid.
   

   

     If PostgreSQL was compiled with
     OpenLDAP as the LDAP client library, the
     ldapserver setting may be omitted.  In that case, a
     list of host names and ports is looked up via
     RFC 2782 DNS SRV records.
     The name _ldap._tcp.DOMAIN is looked up, where
     DOMAIN is extracted from ldapbasedn.
   

   

    Here is an example for a simple-bind LDAP configuration:

```
host ... ldap ldapserver=ldap.example.net ldapprefix="cn=" ldapsuffix=", dc=example, dc=net"
```

    When a connection to the database server as database
    user someuser is requested, PostgreSQL will attempt to
    bind to the LDAP server using the DN cn=someuser, dc=example,
    dc=net and the password provided by the client.  If that connection
    succeeds, the database access is granted.
   

   

    Here is a different simple-bind configuration, which uses the LDAPS scheme
    and a custom port number, written as a URL:

```
host ... ldap ldapurl="ldaps://ldap.example.net:49151" ldapprefix="cn=" ldapsuffix=", dc=example, dc=net"
```

    This is slightly more compact than specifying ldapserver,
    ldapscheme, and ldapport separately.
   

   

    Here is an example for a search+bind configuration:

```
host ... ldap ldapserver=ldap.example.net ldapbasedn="dc=example, dc=net" ldapsearchattribute=uid
```

    When a connection to the database server as database
    user someuser is requested, PostgreSQL will attempt to
    bind anonymously (since ldapbinddn was not specified) to
    the LDAP server, perform a search for (uid=someuser)
    under the specified base DN.  If an entry is found, it will then attempt to
    bind using that found information and the password supplied by the client.
    If that second bind succeeds, the database access is granted.
   

   

    Here is the same search+bind configuration written as a URL:

```
host ... ldap ldapurl="ldap://ldap.example.net/dc=example,dc=net?uid?sub"
```

    Some other software that supports authentication against LDAP uses the
    same URL format, so it will be easier to share the configuration.
   

   

    Here is an example for a search+bind configuration that uses
    ldapsearchfilter instead of
    ldapsearchattribute to allow authentication by
    user ID or email address:

```
host ... ldap ldapserver=ldap.example.net ldapbasedn="dc=example, dc=net" ldapsearchfilter="(|(uid=$username)(mail=$username))"
```

   

   

    Here is an example for a search+bind configuration that uses DNS SRV
    discovery to find the host name(s) and port(s) for the LDAP service for the
    domain name example.net:

```
host ... ldap ldapbasedn="dc=example,dc=net"
```

   

   
    

     Since LDAP often uses commas and spaces to separate the different
     parts of a DN, it is often necessary to use double-quoted parameter
     values when configuring LDAP options, as shown in the examples.
    

   

  

  

   
# Certificate Authentication

   
    Certificate
   

   

    This authentication method uses SSL client certificates to perform
    authentication. It is therefore only available for SSL connections;
    see  for SSL configuration instructions.
    When using this authentication method, the server will require that
    the client provide a valid, trusted certificate.  No password prompt
    will be sent to the client.  The cn (Common Name)
    attribute of the certificate
    will be compared to the requested database user name, and if they match
    the login will be allowed.  User name mapping can be used to allow
    cn to be different from the database user name.
   

   

    The following configuration options are supported for SSL certificate
    authentication:
    

     

      
map

      

       

        Allows for mapping between system and database user names. See
         for details.
       

      

     

    

   

   

    It is redundant to use the clientcert option with
    cert authentication because cert
    authentication is effectively trust authentication
    with clientcert=verify-full.
   

  

  

   
# PAM Authentication

   
    PAM
   

   

    This authentication method operates similarly to
    password except that it uses PAM (Pluggable
    Authentication Modules) as the authentication mechanism. The
    default PAM service name is postgresql.
    PAM is used only to validate user name/password pairs and optionally the
    connected remote host name or IP address. Therefore the user must already
    exist in the database before PAM can be used for authentication.  For more
    information about PAM, please read the
    
    Linux-PAM Page.
   

   

    The following configuration options are supported for PAM:
    

     

      
pamservice

      

       

        PAM service name.
       

      

     

     

      
pam_use_hostname

      

       

        Determines whether the remote IP address or the host name is provided
        to PAM modules through the PAM_RHOST item.  By
        default, the IP address is used.  Set this option to 1 to use the
        resolved host name instead.  Host name resolution can lead to login
        delays.  (Most PAM configurations don't use this information, so it is
        only necessary to consider this setting if a PAM configuration was
        specifically created to make use of it.)
       

      

     

    

   

   
    

     If PAM is set up to read /etc/shadow, authentication
     will fail because the PostgreSQL server is started by a non-root
     user.  However, this is not an issue when PAM is configured to use
     LDAP or other authentication methods.
    

   
  

  

   
# BSD Authentication

   
    BSD Authentication
   

   

    This authentication method operates similarly to
    password except that it uses BSD Authentication
    to verify the password. BSD Authentication is used only
    to validate user name/password pairs. Therefore the user's role must
    already exist in the database before BSD Authentication can be used
    for authentication. The BSD Authentication framework is currently
    only available on OpenBSD.
   

   

    BSD Authentication in PostgreSQL uses
    the auth-postgresql login type and authenticates with
    the postgresql login class if that's defined
    in login.conf. By default that login class does not
    exist, and PostgreSQL will use the default login class.
   

   
    

     To use BSD Authentication, the PostgreSQL user account (that is, the
     operating system user running the server) must first be added to
     the auth group.  The auth group
     exists by default on OpenBSD systems.
    

   
  

  

   
# OAuth Authorization/Authentication

   
    OAuth Authorization/Authentication
   

   

    OAuth 2.0 is an industry-standard framework, defined in
    RFC 6749,
    to enable third-party applications to obtain limited access to a protected
    resource.

    OAuth client support has to be enabled when PostgreSQL
    is built, see  for more information.
   

   

    This documentation uses the following terminology when discussing the OAuth
    ecosystem:

    

     

      
Resource Owner (or End User)

      

       

        The user or system who owns protected resources and can grant access to
        them. This documentation also uses the term end user
        when the resource owner is a person. When you use
        psql to connect to the database using OAuth,
        you are the resource owner/end user.
       

      

     

     

      
Client

      

       

        The system which accesses the protected resources using access
        tokens. Applications using libpq, such as psql,
        are the OAuth clients when connecting to a
        PostgreSQL cluster.
       

      

     

     

      
Resource Server

      

       

        The system hosting the protected resources which are
        accessed by the client. The PostgreSQL
        cluster being connected to is the resource server.
       

      

     

     

      
Provider

      

       

        The organization, product vendor, or other entity which develops and/or
        administers the OAuth authorization servers and clients for a given application.
        Different providers typically choose different implementation details
        for their OAuth systems; a client of one provider is not generally
        guaranteed to have access to the servers of another.
       

       

        This use of the term "provider" is not standard, but it seems to be in
        wide use colloquially. (It should not be confused with OpenID's similar
        term "Identity Provider". While the implementation of OAuth in
        PostgreSQL is intended to be interoperable
        and compatible with OpenID Connect/OIDC, it is not itself an OIDC client
        and does not require its use.)
       

      

     

     

      
Authorization Server

      

       

        The system which receives requests from, and issues access tokens to,
        the client after the authenticated resource owner has given approval.
        PostgreSQL does not provide an authorization
        server; it is the responsibility of the OAuth provider.
       

      

     

     

      
Issuer

      

       

        An identifier for an authorization server, printed as an
        https:// URL, which provides a trusted "namespace"
        for OAuth clients and applications. The issuer identifier allows a
        single authorization server to talk to the clients of mutually
        untrusting entities, as long as they maintain separate issuers.
       

      

     

    

    
     

      For small deployments, there may not be a meaningful distinction between
      the "provider", "authorization server", and "issuer". However, for more
      complicated setups, there may be a one-to-many (or many-to-many)
      relationship: a provider may rent out multiple issuer identifiers to
      separate tenants, then provide multiple authorization servers, possibly
      with different supported feature sets, to interact with their clients.
     

    
   

   

    PostgreSQL supports bearer tokens, defined in
    RFC 6750,
    which are a type of access token used with OAuth 2.0 where the token is an
    opaque string.  The format of the access token is implementation specific
    and is chosen by each authorization server.
   

   

    The following configuration options are supported for OAuth:
    

     

      
issuer

      

       

        An HTTPS URL which is either the exact
        issuer identifier of the
        authorization server, as defined by its discovery document, or a
        well-known URI that points directly to that discovery document. This
        parameter is required.
       

       

        When an OAuth client connects to the server, a URL for the discovery
        document will be constructed using the issuer identifier. By default,
        this URL uses the conventions of OpenID Connect Discovery: the path
        /.well-known/openid-configuration will be appended
        to the end of the issuer identifier. Alternatively, if the
        issuer contains a /.well-known/
        path segment, that URL will be provided to the client as-is.
       

       
        

         The OAuth client in libpq requires the server's issuer setting to
         exactly match the issuer identifier which is provided in the discovery
         document, which must in turn match the client's
          setting. No variations in
         case or formatting are permitted.
        

       
      

     

     

      
scope

      

       

        A space-separated list of the OAuth scopes needed for the server to
        both authorize the client and authenticate the user.  Appropriate values
        are determined by the authorization server and the OAuth validation
        module used (see  for more
        information on validators).  This parameter is required.
       

      

     

     

      
validator

      

       

        The library to use for validating bearer tokens. If given, the name must
        exactly match one of the libraries listed in
        .  This parameter is
        optional unless oauth_validator_libraries contains
        more than one library, in which case it is required.
       

      

     

     

      

       validator.option
      

      

       

        Validator modules may define
        additional configuration options for oauth
        HBA entries. These validator-specific options are accessible via the
        validator.* "namespace". For example, a module may
        register the validator.foo and
        validator.bar options and define their effects on
        authentication.
       

       

        The name, syntax, and behavior of each option
        are not determined by PostgreSQL; consult the
        documentation for the validator module in use.
       

       
        

         A limitation of the current implementation is that unrecognized
         option names will not be caught until
         connection time. A pg_ctl reload will succeed, but
         matching connections will fail:

```
LOG:  connection received: host=[local]
WARNING:  unrecognized authentication option name: "validator.bad"
DETAIL:  The installed validator module ("my_validator") did not define an option named "bad".
HINT:  All OAuth connections matching this line will fail. Correct the option and reload the server configuration.
CONTEXT:  line 2 of configuration file "data/pg_hba.conf"
```

         Use caution when making changes to validator-specific HBA options in
         production systems.
        

       
      

     

     

      
map

      

       

        Allows for mapping between OAuth identity provider and database user
        names.  See  for details.  If a
        map is not specified, the user name associated with the token (as
        determined by the OAuth validator) must exactly match the role name
        being requested.  This parameter is optional.
       

      

     

     

      

       delegate_ident_mapping
      

      

       

        An advanced option which is not intended for common use.
       

       

        When set to 1, standard user mapping with
        pg_ident.conf is skipped, and the OAuth validator
        takes full responsibility for mapping end user identities to database
        roles.  If the validator authorizes the token, the server trusts that
        the user is allowed to connect under the requested role, and the
        connection is allowed to proceed regardless of the authentication
        status of the user.
       

       

        This parameter is incompatible with map.
       

       
        

         delegate_ident_mapping provides additional
         flexibility in the design of the authentication system, but it also
         requires careful implementation of the OAuth validator, which must
         determine whether the provided token carries sufficient end-user
         privileges in addition to the standard
         checks required of all validators.  Use with caution.
        

       
      

     

    

   

  

  

   
# Authentication Problems

   

    Authentication failures and related problems generally
    manifest themselves through error messages like the following:
   

   

```
FATAL:  no pg_hba.conf entry for host "123.123.123.123", user "andym", database "testdb"
```

    This is what you are most likely to get if you succeed in contacting
    the server, but it does not want to talk to you. As the message
    suggests, the server refused the connection request because it found
    no matching entry in its pg_hba.conf
    configuration file.
   

   

```
FATAL:  password authentication failed for user "andym"
```

    Messages like this indicate that you contacted the server, and it is
    willing to talk to you, but not until you pass the authorization
    method specified in the pg_hba.conf file. Check
    the password you are providing, or check your Kerberos or ident
    software if the complaint mentions one of those authentication
    types.
   

   

```
FATAL:  user "andym" does not exist
```

    The indicated database user name was not found.
   

   

```
FATAL:  database "testdb" does not exist
```

    The database you are trying to connect to does not exist. Note that
    if you do not specify a database name, it defaults to the database
    user name.
   

   
   

    The server log might contain more information about an
    authentication failure than is reported to the client. If you are
    confused about the reason for a failure, check the server log.
