ANALYZE;

/*
  ANALYZE is just an example. Depending on the LOCK MODE other SQL commands trying to access
  SCHEMA_VERSION can also create the problem. Other example:
  SELECT * FROM SCHEMA_VERSION;
  hangs when lock mode is ACCESS EXCLUSIVE (the most restrictive one)
*/
