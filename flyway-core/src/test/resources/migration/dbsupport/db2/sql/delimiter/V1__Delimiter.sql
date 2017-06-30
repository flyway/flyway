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
