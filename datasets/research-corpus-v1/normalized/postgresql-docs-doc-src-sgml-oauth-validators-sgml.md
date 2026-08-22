# OAuth Validator Modules

 
  OAuth Validators
 
 

  PostgreSQL provides infrastructure for creating
  custom modules to perform server-side validation of OAuth bearer tokens.
  Because OAuth implementations vary so wildly, and bearer token validation is
  heavily dependent on the issuing party, the server cannot check the token
  itself; validator modules provide the integration layer between the server
  and the OAuth provider in use.
 

 

  OAuth validator modules must at least consist of an initialization function
  (see ) and the required callback for
  performing validation (see ).
 

 
  

   Since a misbehaving validator might let unauthorized users into the database,
   correct implementation is crucial for server safety. See
    for design considerations.
  

 

 

  
# Safely Designing a Validator Module

  
   

    Read and understand the entirety of this section before implementing a
    validator module. A malfunctioning validator is potentially worse than no
    authentication at all, both because of the false sense of security it
    provides, and because it may contribute to attacks against other pieces of
    an OAuth ecosystem.
   

  

  

   
# Validator Responsibilities

   

    Although different modules may take very different approaches to token
    validation, implementations generally need to perform three separate
    actions:
   

   

    

     
Validate the Token

     

      

       The validator must first ensure that the presented token is in fact a
       valid Bearer token for use in client authentication. The correct way to
       do this depends on the provider, but it generally involves either
       cryptographic operations to prove that the token was created by a trusted
       party (offline validation), or the presentation of the token to that
       trusted party so that it can perform validation for you (online
       validation).
      

      

       Online validation, usually implemented via
       OAuth Token
       Introspection, requires fewer steps of a validator module and
       allows central revocation of a token in the event that it is stolen
       or misissued. However, it does require the module to make at least one
       network call per authentication attempt (all of which must complete
       within the configured ).
       Additionally, your provider may not provide introspection endpoints for
       use by external resource servers.
      

      

       Offline validation is much more involved, typically requiring a validator
       to maintain a list of trusted signing keys for a provider and then
       check the token's cryptographic signature along with its contents.
       Implementations must follow the provider's instructions to the letter,
       including any verification of issuer ("where is this token from?"),
       audience ("who is this token for?"), and validity period ("when can this
       token be used?"). Since there is no communication between the module and
       the provider, tokens cannot be centrally revoked using this method;
       offline validator implementations may wish to place restrictions on the
       maximum length of a token's validity period.
      

      

       If the token cannot be validated, the module should immediately fail.
       Further authentication/authorization is pointless if the bearer token
       wasn't issued by a trusted party.
      

     

    

    

     
Authorize the Client

     

      

       Next the validator must ensure that the end user has given the client
       permission to access the server on their behalf. This generally involves
       checking the scopes that have been assigned to the token, to make sure
       that they cover database access for the current HBA parameters.
      

      

       The purpose of this step is to prevent an OAuth client from obtaining a
       token under false pretenses. If the validator requires all tokens to
       carry scopes that cover database access, the provider should then loudly
       prompt the user to grant that access during the flow. This gives them the
       opportunity to reject the request if the client isn't supposed to be
       using their credentials to connect to databases.
      

      

       While it is possible to establish client authorization without explicit
       scopes by using out-of-band knowledge of the deployed architecture, doing
       so removes the user from the loop, which prevents them from catching
       deployment mistakes and allows any such mistakes to be exploited
       silently. Access to the database must be tightly restricted to only
       trusted clients
       
        

         That is, "trusted" in the sense that the OAuth client and the
         PostgreSQL server are controlled by the same
         entity. Notably, the Device Authorization client flow supported by
         libpq does not usually meet this bar, since it's designed for use by
         public/untrusted clients.
        

       
       if users are not prompted for additional scopes.
      

      

       Even if authorization fails, a module may choose to continue to pull
       authentication information from the token for use in auditing and
       debugging.
      

     

    

    

     
Authenticate the End User

     

      

       Finally, the validator should determine a user identifier for the token,
       either by asking the provider for this information or by extracting it
       from the token itself, and return that identifier to the server (which
       will then make a final authorization decision using the HBA
       configuration). This identifier will be available within the session via
       system_user
       and recorded in the server logs if 
       is enabled.
      

      

       Different providers may record a variety of different authentication
       information for an end user, typically referred to as
       claims. Providers usually document which of these
       claims are trustworthy enough to use for authorization decisions and
       which are not. (For instance, it would probably not be wise to use an
       end user's full name as the identifier for authentication, since many
       providers allow users to change their display names arbitrarily.)
       Ultimately, the choice of which claim (or combination of claims) to use
       comes down to the provider implementation and application requirements.
      

      

       Note that anonymous/pseudonymous login is possible as well, by enabling
       usermap delegation; see
       .
      

     

    

   

  

  

   
# General Coding Guidelines

   

    Developers should keep the following in mind when implementing token
    validation:
   

   

    

     
Token Confidentiality

     

      

       Modules should not write tokens, or pieces of tokens, into the server
       log. This is true even if the module considers the token invalid; an
       attacker who confuses a client into communicating with the wrong provider
       should not be able to retrieve that (otherwise valid) token from the
       disk.
      

      

       Implementations that send tokens over the network (for example, to
       perform online token validation with a provider) must authenticate the
       peer and ensure that strong transport security is in use.
      

     

    

    

     
Logging

     

      

       To simply log the reason for a validation failure, modules may set the
       freeform error_detail field during the
       validate callback.
       ( has guidelines for writing good
       DETAIL messages.) error_detail
       is printed only to the server log, as part of the final authentication
       failure message, and it is not shared with the client.
      

      

       Modules may also use the same logging
       facilities as standard extensions; however, the rules for emitting
       log entries to the client are subtly different during the authentication
       phase of the connection. Generally speaking, modules should log
       problems at the COMMERROR level and return
       normally, instead of using ERROR/FATAL
       to unwind the stack, to avoid leaking information to unauthenticated
       clients.
      

     

    

    

     
Interruptibility

     

      

       Modules must remain interruptible by signals so that the server can
       correctly handle authentication timeouts and shutdown signals from
       pg_ctl. For example, blocking calls on sockets
       should generally be replaced with code that handles both socket events
       and interrupts without races (see WaitLatchOrSocket(),
       WaitEventSetWait(), et al), and long-running loops
       should periodically call CHECK_FOR_INTERRUPTS().
       Failure to follow this guidance may result in unresponsive backend
       sessions.
      

     

    

    

     
Testing

     

      

       The breadth of testing an OAuth system is well beyond the scope of this
       documentation, but at minimum, negative testing should be considered
       mandatory. It's trivial to design a module that lets authorized users in;
       the whole point of the system is to keep unauthorized users out.
      

     

    

    

     
Documentation

     

      

       Validator implementations should document the contents and format of the
       authenticated ID that is reported to the server for each end user, since
       DBAs may need to use this information to construct pg_ident maps. (For
       instance, is it an email address? an organizational ID number? a UUID?)
       They should also document whether or not it is safe to use the module in
       delegate_ident_mapping=1 mode, and what additional
       configuration is required in order to do so.
      

      

       If an implementation provides custom
       HBA options, the names and syntax of those options should be
       documented as well.
      

     

    

   

  

  

   
# Authorizing Users (Usermap Delegation)

   

    The standard deliverable of a validation module is the user identifier,
    which the server will then compare to any configured
    pg_ident.conf
    mappings and determine whether the end user is authorized to connect.
    However, OAuth is itself an authorization framework, and tokens may carry
    information about user privileges. For example, a token may be associated
    with the organizational groups that a user belongs to, or list the roles
    that a user may assume, and duplicating that knowledge into local usermaps
    for every server may not be desirable.
   

   

    To bypass username mapping entirely, and have the validator module assume
    the additional responsibility of authorizing user connections, the HBA may
    be configured with .
    The module may then use token scopes or an equivalent method to decide
    whether the user is allowed to connect under their desired role. The user
    identifier will still be recorded by the server, but it plays no part in
    determining whether to continue the connection.
   

   

    Using this scheme, authentication itself is optional. As long as the module
    reports that the connection is authorized, login will continue even if there
    is no recorded user identifier at all. This makes it possible to implement
    anonymous or pseudonymous access to the database, where the third-party
    provider performs all necessary authentication but does not provide any
    user-identifying information to the server. (Some providers may create an
    anonymized ID number that can be recorded instead, for later auditing.)
   

   

    Usermap delegation provides the most architectural flexibility, but it turns
    the validator module into a single point of failure for connection
    authorization. Use with caution.
   

  

 

 

  
# Initialization Functions

  
   _PG_oauth_validator_module_init
  
  

   OAuth validator modules are dynamically loaded from the shared
   libraries listed in .
   Modules are loaded on demand when requested from a login in progress.
   The normal library search path is used to locate the library. To
   provide the validator callbacks and to indicate that the library is an OAuth
   validator module a function named
   _PG_oauth_validator_module_init must be provided. The
   return value of the function must be a pointer to a struct of type
   OAuthValidatorCallbacks, which contains a magic
   number and pointers to the module's token validation functions. The returned
   pointer must be of server lifetime, which is typically achieved by defining
   it as a static const variable in global scope.

```
typedef struct OAuthValidatorCallbacks
{
    uint32        magic;            /* must be set to PG_OAUTH_VALIDATOR_MAGIC */

    ValidatorStartupCB startup_cb;
    ValidatorShutdownCB shutdown_cb;
    ValidatorValidateCB validate_cb;
} OAuthValidatorCallbacks;

typedef const OAuthValidatorCallbacks *(*OAuthValidatorModuleInit) (void);
```

   Only the validate_cb callback is required, the others
   are optional.
  

 

 

  
# OAuth Validator Callbacks

  

   OAuth validator modules implement their functionality by defining a set of
   callbacks. The server will call them as required to process the
   authentication request from the user.
  

  

   
# Startup Callback

   

    The startup_cb callback is executed directly after
    loading the module. This callback can be used to set up local state,
    define custom HBA options, and
    perform additional initialization if required. If the validator module
    has state it can use state->private_data to
    store it.

```
typedef void (*ValidatorStartupCB) (ValidatorModuleState *state);
```

   

  

  

   
# Validate Callback

   

    The validate_cb callback is executed during the OAuth
    exchange when a user attempts to authenticate using OAuth.  Any state set in
    previous calls will be available in state->private_data.

```
typedef bool (*ValidatorValidateCB) (const ValidatorModuleState *state,
                                     const char *token, const char *role,
                                     ValidatorModuleResult *result);
```

    token will contain the bearer token to validate.
    PostgreSQL has ensured that the token is well-formed syntactically, but no
    other validation has been performed.  role will
    contain the role the user has requested to log in as.  The callback must
    set output parameters in the result struct, which is
    defined as below:

```
typedef struct ValidatorModuleResult
{
    bool        authorized;
    char       *authn_id;
    char       *error_detail;
} ValidatorModuleResult;
```

    The connection will only proceed if the module sets
    result->authorized to true.  To
    authenticate the user, the authenticated user name (as determined using the
    token) shall be palloc'd and returned in the result->authn_id
    field.  Alternatively, result->authn_id may be set to
    NULL if the token is valid but the associated user identity cannot be
    determined.  If the validator returns true and
    sets result->authn_id then the identity appears
    in the server log when  includes
    authentication.  This happens before authorization and
    will log authentication even if the connection is later rejected due to
    authorization.
   

   

    A validator may return false to signal an internal error,
    in which case the connection fails.  Otherwise the validator should return
    true to indicate that it has processed the token and made
    an authorization decision.
   

   

    In either failure case (validation error or internal error) the module may
    store a user-readable reason for the failure in result->error_detail.
    This will be printed to the server logs (not sent to the client) as a
    DETAIL entry for the authentication failure. The memory
    pointed to by error_detail may be either palloc'd
    or of static duration. error_detail is ignored
    on success.
   

   

    The behavior after validate_cb returns depends on the
    specific HBA setup.  Normally, the result->authn_id user
    name must exactly match the role that the user is logging in as.  (This
    behavior may be modified with a usermap.)  But when authenticating against
    an HBA rule with delegate_ident_mapping turned on,
    PostgreSQL will not perform any checks on the value of
    result->authn_id at all; in this case it is up to the
    validator to ensure that the token carries enough privileges for the user to
    log in under the indicated role.
   

  

  

   
# Shutdown Callback

   

    The shutdown_cb callback is executed when the server
    backend has finished validating tokens for the connection. If the validator
    module has any allocated state, this callback should free it to avoid
    resource leaks.

```
typedef void (*ValidatorShutdownCB) (ValidatorModuleState *state);
```

   

  

 

 

  
# Custom HBA Options

  

   Like other preloaded libraries, validator modules may define
   custom GUC parameters for user
   configuration in postgresql.conf. However, it may be
   desirable to configure behavior at a more granular level (say, for a
   particular issuer or a group of users) instead of globally.
  

  

   Beginning in PostgreSQL 19, validator
   implementations may define custom options for use inside
   pg_hba.conf. These options are then
   made available to the user
   as validator.option. The API
   for registering and retrieving custom options is described below.
  

  

   
# Options API

    

     Modules register custom HBA option names during the startup_cb
     callback, using RegisterOAuthHBAOptions():

```
/*
 * Register a list of custom option names for use in pg_hba.conf. For each name
 * "foo" registered here, that option will be provided as "validator.foo" in
 * the HBA.
 *
 * Valid option names consist of alphanumeric ASCII, underscore (_), and hyphen
 * (-). Invalid option names will be ignored with a WARNING logged at
 * connection time.
 *
 * This function may only be called during the startup_cb callback. Multiple
 * calls are permitted, which will append to the existing list of registered
 * options; options cannot be unregistered.
 *
 * Parameters:
 *
 * - state: the state pointer passed to the startup_cb callback
 * - num:   the number of options in the opts array
 * - opts:  an array of null-terminated option names to register
 *
 * The list of option names is copied internally, and the opts array is not
 * required to remain valid after the call.
 */
void RegisterOAuthHBAOptions(ValidatorModuleState *state, int num,
                             const char *opts[]);
```

    

    

     Each option's value, if set, may be later retrieved using
     GetOAuthHBAOption():

```
/*
 * Retrieve the string value of an HBA option which was registered via
 * RegisterOAuthHBAOptions(). Usable only during validate_cb or shutdown_cb.
 *
 * If the user has set the corresponding option in pg_hba.conf, this function
 * returns that value as a null-terminated string, which must not be modified
 * or freed. NULL is returned instead if the user has not set this option, if
 * the option name was not registered, or if this function is incorrectly called
 * during the startup_cb.
 *
 * Parameters:
 *
 * - state:   the state pointer passed to the validate_cb/shutdown_cb callback
 * - optname: the name of the option to retrieve
 */
const char *GetOAuthHBAOption(const ValidatorModuleState *state,
                              const char *optname);
```

    

    

     See  for sample usage.
    

  

  

   
# Limitations

   

    

     

      

       Option names are limited to ASCII alphanumeric characters,
       underscores (_), and hyphens (-).
      

     

     

      

       Option values are always freeform strings (in contrast to custom GUCs,
       which support numerics, booleans, and enums).
      

     

     

      

       Option names and values cannot be checked by the server during a reload of
       the configuration. Any unregistered options in pg_hba.conf
       will instead result in connection failures. It is the responsibility of
       each module to document and verify the syntax of option values as needed.
       
        

         If a module finds an invalid option value during validate_cb,
         it's recommended to signal
         an internal error by setting result->error_detail
         to a description of the problem and returning false.
        

       
      

     

    

   

  

  

   
# Example Usage

   

    For a hypothetical module, the options foo and
    bar could be registered as follows:

```
static void
validator_startup(ValidatorModuleState *state)
{
    static const char *opts[] = {
        "foo",      /* description of access privileges */
        "bar",      /* magic URL for additional administrator powers */
    };

    RegisterOAuthHBAOptions(state, lengthof(opts), opts);

    /* ...other setup... */
}
```

   

   

    The following sample entries in pg_hba.conf can then
    make use of these options:

```
# TYPE   DATABASE   USER   ADDRESS    METHOD
hostssl  postgres   admin  0.0.0.0/0  oauth issuer=https://admin.example.com \
                                            scope="pg-admin openid email" \
                                            map=oauth-email \
                                            validator.foo="admin access" \
                                            validator.bar=https://magic.example.com

hostssl  postgres   all    0.0.0.0/0  oauth issuer=https://www.example.com \
                                            scope="pg-user openid email" \
                                            map=oauth-email \
                                            validator.foo="user access"
```

   

   

    The module can retrieve the option settings from the HBA during validation:

```
static bool
validate_token(const ValidatorModuleState *state,
               const char *token, const char *role,
               ValidatorModuleResult *res)
{
    const char *foo = GetOAuthHBAOption(state, "foo"); /* "admin access" or "user access" */
    const char *bar = GetOAuthHBAOption(state, "bar"); /* "https://magic.example.com" or NULL */

    if (bar && !is_valid_url(bar))
    {
        res->error_detail = psprintf("validator.bar (\"%s\") is not a valid URL.", bar);
        return false;
    }

    /* proceed to validate token */
}
```

   

   

    When multiple validators are in use, their registered option lists remain
    independent:

```
in postgresql.conf:
oauth_validator_libraries = 'example_org, my_validator'

in pg_hba.conf:
# TYPE   DATABASE   USER   ADDRESS    METHOD
hostssl  postgres   admin  0.0.0.0/0  oauth issuer=https://admin.example.com \
                                            scope="pg-admin openid email" \
                                            map=oauth-email \
                                            validator=my_validator \
                                            validator.foo="admin access" \
                                            validator.bar=https://magic.example.com

hostssl  postgres   all    0.0.0.0/0  oauth issuer=https://www.example.org \
                                            scope="pg-user openid profile" \
                                            validator=example_org \
                                            delegate_ident_mapping=1 \
                                            validator.magic=on \
                                            validator.more_magic=off
```
