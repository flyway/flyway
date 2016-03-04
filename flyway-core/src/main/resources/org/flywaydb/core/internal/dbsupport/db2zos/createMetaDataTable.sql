--
-- Copyright 2010-2016 Boxfuse GmbH
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

set current sqlid = '${schema}';

CREATE TABLESPACE SDBVERS
      IN "${schema}"
     USING STOGROUP SYSDEFLT PRIQTY -1 SECQTY -1 ERASE NO FREEPAGE 3 PCTFREE 10 DEFINE YES TRACKMOD YES
      SEGSIZE 4
      BUFFERPOOL BP0
      LOCKSIZE PAGE
      LOCKMAX SYSTEM
      CLOSE YES
      COMPRESS YES
      CCSID EBCDIC
  ;



CREATE TABLE "${schema}"."${table}" (
    "installed_rank" INT NOT NULL,
    "version" VARCHAR(50),
    "description" VARCHAR(200) NOT NULL,
    "type" VARCHAR(20) NOT NULL,
    "script" VARCHAR(1000) NOT NULL,
    "checksum" INT,
    "installed_by" VARCHAR(100) NOT NULL,
    "installed_on" TIMESTAMP NOT NULL WITH DEFAULT,
    "execution_time" INT NOT NULL,
    "success" SMALLINT NOT NULL,
    CONSTRAINT "${table}_s" CHECK ("success" in(0,1))
)
IN "${schema}".SDBVERS
  CCSID EBCDIC
;

ALTER TABLE "${schema}"."${table}" ADD CONSTRAINT "${table}_PK" PRIMARY KEY ("installed_rank");

CREATE INDEX "${schema}"."${table}_S_IDX" ON "${schema}"."${table}" ("success");


