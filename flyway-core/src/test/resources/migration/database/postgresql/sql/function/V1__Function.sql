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

CREATE FUNCTION add(integer, integer) RETURNS integer
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
    select $1 + $2;
$_$;
