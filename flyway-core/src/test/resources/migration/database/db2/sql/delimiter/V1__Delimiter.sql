--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE FUNCTION TEST_FUNC(PARAM1 INTEGER)
  RETURNS INTEGER
LANGUAGE SQL
  RETURN
  1;

--#SET TERMINATOR @
CREATE FUNCTION TEST_FUNC(PARAM1 INTEGER, PARAM2 INTEGER)
  RETURNS INTEGER
LANGUAGE SQL
  RETURN
  1@

CREATE OR REPLACE PROCEDURE TESTPROC()
LANGUAGE SQL
  SPECIFIC SP_TESTPROC
  MAIN:BEGIN
  --DELETE FROM TEST;
END@

COMMENT ON PROCEDURE TESTPROC IS 'TESTPROC'@

CREATE OR REPLACE PROCEDURE SCR101010_FIX_PROCEDURE()
LANGUAGE SQL
  SPECIFIC SP_SCR101010_FIX_PROCEDURE
  BEGIN
    --DROP TABLE TEST;
    --CREATE TABLE TEST(COL1 INT);
    --INSERT INTO TEST(COL1) VALUES(10);
  END@

CALL SCR101010_FIX_PROCEDURE@

DROP PROCEDURE SCR101010_FIX_PROCEDURE@

--#SET TERMINATOR ;
CREATE FUNCTION TEST_FUNC(PARAM1 INTEGER, PARAM2 INTEGER, PARAM3 INTEGER)
  RETURNS INTEGER
LANGUAGE SQL
  RETURN
  1;