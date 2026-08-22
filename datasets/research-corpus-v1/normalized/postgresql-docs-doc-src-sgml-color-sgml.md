# Color Support

 
  color
 

 

  Most programs in the PostgreSQL package can produce colorized console
  output.  This appendix describes how that is configured.
 

 

  
# When Color is Used

  

   To use colorized output, set the environment variable
   PG_COLORPG_COLOR
   as follows:

   
    

     

      If the value is always, then color is used.
     

    

    

     

      If the value is auto and the standard error stream
      is associated with a terminal device, then color is used.
     

    

    

     

      Otherwise, color is not used.
     

    

   
  

 

 

  
# Configuring the Colors

  

   The actual colors to be used are configured using the environment variable
   PG_COLORSPG_COLORS
   (note plural).  The value is a colon-separated list of
   key=value
   pairs.  The keys specify what the color is to be used for.  The values are
   SGR (Select Graphic Rendition) specifications, which are interpreted by the
   terminal.
  

  

   The following keys are currently in use:
   

    

     
error

     

      
used to highlight the text error in error messages

     

    

    

     
warning

     

      
used to highlight the text warning in warning
      messages

     

    

    

     
note

     

      
used to highlight the text detail and
      hint in such messages

     

    

    

     
locus

     

      
used to highlight location information (e.g., program name and
      file name) in messages

     

    

   

  

  

   The default value is
   error=01;31:warning=01;35:note=01;36:locus=01
   (01;31 = bold red, 01;35 = bold
   magenta, 01;36 = bold cyan, 01 = bold
   default color).
  

  
   

    This color specification format is also used by other software packages
    such as GCC, GNU
    coreutils, and GNU grep.
