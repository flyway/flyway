--
-- Copyright 2010-2014 Axel Fontaine
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

CREATE TABLE ${schema}.SCHEM_VERS (
    version_rank FOR COLUMN VERS_RANK INT NOT NULL,
    installed_rank FOR COLUMN INST_RANK INT NOT NULL,
    version VARCHAR(50) NOT NULL,
    description FOR COLUMN DESCR VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by FOR COLUMN INST_BY VARCHAR(100) NOT NULL,
    installed_on FOR COLUMN INST_ON TIMESTAMP DEFAULT CURRENT TIMESTAMP NOT NULL,
    execution_time FOR COLUMN EXEC_TIME INT NOT NULL,
    success SMALLINT NOT NULL,
    CONSTRAINT ${schema}.SCHEM_VERS_s CHECK (success in(0,1))
);
ALTER TABLE ${schema}.SCHEM_VERS ADD CONSTRAINT ${schema}.SCHEM_VERS_pk PRIMARY KEY (version);

CREATE INDEX ${schema}.SCHEM_VERS_vr_idx ON ${schema}.SCHEM_VERS (version_rank);
CREATE INDEX ${schema}.SCHEM_VERS_ir_idx ON ${schema}.SCHEM_VERS (installed_rank);
CREATE INDEX ${schema}.SCHEM_VERS_s_idx ON ${schema}.SCHEM_VERS (success);

RENAME TABLE ${schema}.SCHEM_VERS TO ${table}
 FOR SYSTEM NAME SCHEM_VERS;