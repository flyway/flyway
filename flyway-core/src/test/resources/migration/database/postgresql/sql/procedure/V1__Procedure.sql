--
-- Copyright 2010-2018 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--
-- Must
-- be
-- exactly
-- 13 lines
-- to match
-- community
-- edition
-- license
-- length.
--

CREATE TABLE test_data (
  value/*test*/ /*test*/VARCHAR(25) NOT NULL PRIMARY KEY
);

CREATE FUNCTION AddData() RETURNS INTEGER
AS $$
    BEGIN
     INSERT INTO test_data (value) VALUES ('Hello');
     RETURN 1;
   END;
 $$ LANGUAGE plpgsql;

SELECT *  INTO TEMP adddata_temp_table FROM AddData() ;

CREATE FUNCTION add(integer, integer) RETURNS integer
    LANGUAGE sql/*test*/ IMMUTABLE STRICT
    AS $_$select $1 + $2;$_$;
    
CREATE FUNCTION """add2"""(integer, integer) RETURNS integer
    LANGUAGE sql/*test*/ IMMUTABLE STRICT
    AS $_$select $1 + $2;$_$;

CREATE FUNCTION inc(i integer) RETURNS VARCHAR(25)
    LANGUAGE sql
    AS $$SELECT * FROM test_data$$;    