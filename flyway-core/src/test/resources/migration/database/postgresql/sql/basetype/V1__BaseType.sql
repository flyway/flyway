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

CREATE TYPE test_type;

CREATE FUNCTION test_type_in(cstring) RETURNS test_type AS
'record_in'
LANGUAGE internal STABLE STRICT COST 1;

CREATE FUNCTION test_type_out(test_type) RETURNS cstring AS
'record_out' LANGUAGE internal STABLE STRICT COST 1;

CREATE TYPE test_type(INPUT=test_type_in, OUTPUT=test_type_out);