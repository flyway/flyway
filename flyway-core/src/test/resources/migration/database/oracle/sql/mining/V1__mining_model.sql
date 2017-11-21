--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
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