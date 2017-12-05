--
-- Copyright 2010-2017 Boxfuse GmbH
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

CREATE FUNCTION add(integer, integer) RETURNS integer
    LANGUAGE sql/*test*/ IMMUTABLE
    AS $$select $1 + $2;$$;
    
CREATE FUNCTION """add2"""(integer, integer) RETURNS integer
    LANGUAGE sql/*test*/ IMMUTABLE
    AS $$select $1 + $2;$$;

CREATE FUNCTION inc(i integer) RETURNS VARCHAR(25)
    LANGUAGE sql IMMUTABLE
    AS $$SELECT 'ABC123'$$;