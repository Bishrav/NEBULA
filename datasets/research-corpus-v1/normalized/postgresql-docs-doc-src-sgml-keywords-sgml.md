# SQL Key Words

 
  key word
  list of
 

 

   lists all tokens that are key words
  in the SQL standard and in PostgreSQL
  &version;.  Background information can be found in .
  (For space reasons, only the latest two versions of the SQL standard, and
  SQL-92 for historical comparison, are included.  The differences between
  those and the other intermediate standard versions are small.)
 

 

  SQL distinguishes between reserved and
  non-reserved key words.  According to the standard,
  reserved key words
  are the only real key words; they are never allowed as identifiers.
  Non-reserved key words only have a special meaning in particular
  contexts and can be used as identifiers in other contexts.  Most
  non-reserved key words are actually the names of built-in tables
  and functions specified by SQL.  The concept of non-reserved key
  words essentially only exists to declare that some predefined meaning
  is attached to a word in some contexts.
 

 

  In the PostgreSQL parser, life is a bit
  more complicated. There are several different classes of tokens
  ranging from those that can never be used as an identifier to those
  that have absolutely no special status in the parser, but are considered
  ordinary identifiers.  (The latter is usually the case for
  functions specified by SQL.)  Even reserved key words are not
  completely reserved in PostgreSQL, but
  can be used as column labels (for example, SELECT 55 AS
  CHECK, even though CHECK is a reserved key
  word).
 

 

  In  in the column for
  PostgreSQL we classify as
  non-reserved those key words that are explicitly
  known to the parser but are allowed as column or table names.
  Some key words that are otherwise
  non-reserved cannot be used as function or data type names and are
  marked accordingly.  (Most of these words represent built-in
  functions or data types with special syntax.  The function or type
  is still available but it cannot be redefined by the user.)  Labeled
  reserved are those tokens that are not allowed as
  column or table names.  Some reserved key words are
  allowable as names for functions or data types; this is also shown in the
  table.  If not so marked, a reserved key word is only allowed as a
  column label.
  A blank entry in this column means that the word is treated as an
  ordinary identifier by PostgreSQL.
 

 

  Furthermore, while most key words can be used as bare
  column labels without writing AS before them (as
  described in ), there are a few
  that require a leading AS to avoid ambiguity.  These
  are marked in the table as requires AS.
 

 

  As a general rule, if you get spurious parser errors for commands
  that use any of the listed key words as an identifier, you should
  try quoting the identifier to see if the problem goes away.
 

 

  It is important to understand before studying  that the fact that a key word is not
  reserved in PostgreSQL does not mean that
  the feature related to the word is not implemented.  Conversely, the
  presence of a key word does not indicate the existence of a feature.
 

 &keywords-table;
