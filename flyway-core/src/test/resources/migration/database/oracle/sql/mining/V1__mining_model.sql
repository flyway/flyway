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

CREATE TABLE TEST_DATA_TABLE (
    CASE_ID,
    TARGET,
    FILLER,
    PRIMARY KEY (CASE_ID)
)
AS
SELECT ROWNUM, TO_CHAR(MOD(ROWNUM, 31)), TO_CHAR(ORA_HASH(ROWNUM), 'FM0XXXXXXX')
FROM DUAL CONNECT BY ROWNUM <= 100;

-- Oracle 11g bug: changing current_schema breaks CREATE_MODEL (no target column in data table)
-- Create mining model via a local procedure
CREATE OR REPLACE PROCEDURE CREATE_TEST_MODEL
AS
BEGIN
    DBMS_DATA_MINING.CREATE_MODEL(
      MODEL_NAME           => 'TEST_MINING_MODEL',
      MINING_FUNCTION      => DBMS_DATA_MINING.CLASSIFICATION,
      DATA_TABLE_NAME      => 'TEST_DATA_TABLE',
      CASE_ID_COLUMN_NAME  => 'CASE_ID',
      TARGET_COLUMN_NAME   => 'TARGET');
END;
/

BEGIN
  CREATE_TEST_MODEL();
END;
/