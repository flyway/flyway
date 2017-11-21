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

CREATE PROCEDURE SP_EQIP_HOURS_AGGRGT_DAY_VIS (
    IN "@MACH_CO_ID"	VARCHAR(15000),
    IN "@START_TIME"	TIMESTAMP,
    IN "@END_TIME"	TIMESTAMP )
  SPECIFIC "SP_EQIP_HOURS_AGGRGT_DAY_VIS_2"
  DYNAMIC RESULT SETS 1
  LANGUAGE SQL
  NOT DETERMINISTIC
  NO EXTERNAL ACTION
  READS SQL DATA
  CALLED ON NULL INPUT
  INHERIT SPECIAL REGISTERS
  OLD SAVEPOINT LEVEL
MAIN:BEGIN
DECLARE V_SECONDS INTEGER DEFAULT 0;
SET V_SECONDS = (SELECT MOD(5,1) * 60 * 60
			FROM SYSIBM.SYSDUMMY1);

RETURN; END;
