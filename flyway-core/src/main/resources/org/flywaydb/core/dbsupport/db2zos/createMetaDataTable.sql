--
-- Copyright 2010-2014 Axel Fontaine and the many contributors.
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

CREATE TABLESPACE SDBVERS
      IN "${schema}"
     USING STOGROUP SENSITIV PRIQTY -1 SECQTY -1 ERASE NO FREEPAGE 0 PCTFREE 10 DEFINE YES TRACKMOD YES
       SEGSIZE 64
     BUFFERPOOL BP3
     LOCKSIZE  PAGE
     LOCKMAX SYSTEM
     CLOSE YES
     COMPRESS YES
     CCSID UNICODE
;



CREATE TABLE "${schema}"."${table}" (
    "version_rank" INT NOT NULL,
    "installed_rank" INT NOT NULL,
    "version" VARCHAR(50) NOT NULL,
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
  CCSID UNICODE
;

-- Indekser og constraints
CREATE UNIQUE INDEX "${schema}"."${table}_VPK_IDX" ON ${schema}."${table}" ("version" ASC);
ALTER TABLE "${schema}"."${table}" ADD CONSTRAINT "${table}_PK" PRIMARY KEY ("version");

CREATE INDEX "${schema}"."${table}_VR_IDX" ON "${schema}"."${table}" ("version_rank");
CREATE INDEX "${schema}"."${table}_IR_IDX" ON "${schema}"."${table}" ("installed_rank");
CREATE INDEX "${schema}"."${table}_S_IDX" ON "${schema}"."${table}" ("success");


