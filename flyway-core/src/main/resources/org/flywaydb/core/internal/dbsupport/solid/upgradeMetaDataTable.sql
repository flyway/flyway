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

-- It seems that Solid can't alter the PRIMARY key
-- The temporary table schema_version_old has to be manually deleted afterwards
DROP TRIGGER ${schema}.${table}_create;
commit work;
DROP INDEX ${schema}.${table}_S_IDX;
commit work;
ALTER TABLE ${schema}.${table} SET TABLE NAME ${table}_OLD;
commit work;
CREATE TABLE ${schema}.${table}
(
   INSTALLED_RANK integer PRIMARY KEY NOT NULL,
   VERSION varchar(50) NOT NULL,
   DESCRIPTION varchar(200) NOT NULL,
   TYPE varchar(20) NOT NULL,
   SCRIPT varchar(1000) NOT NULL,
   CHECKSUM integer,
   INSTALLED_BY varchar(100) NOT NULL,
   INSTALLED_ON timestamp,
   EXECUTION_TIME integer NOT NULL,
   SUCCESS integer NOT NULL
);
commit work;
INSERT INTO ${table} (INSTALLED_RANK, VERSION,DESCRIPTION,TYPE,SCRIPT,CHECKSUM,INSTALLED_BY,INSTALLED_ON,EXECUTION_TIME,SUCCESS) select INSTALLED_RANK, VERSION,DESCRIPTION,TYPE,SCRIPT,CHECKSUM,INSTALLED_BY,INSTALLED_ON,EXECUTION_TIME,SUCCESS from SCHEMA_VERSION_OLD;
commit work;
"CREATE TRIGGER ${schema}.${table}_CREATE ON ${schema}.${table}
    BEFORE INSERT REFERENCING NEW INSTALLED_ON AS NEW_INSTALLED_ON
    BEGIN
    SET NEW_INSTALLED_ON = NOW();
    END";
commit work;
CREATE INDEX SCHEMA_VERSION_S_IDX ON ${schema}.${table}(SUCCESS);

-- orginal Flyway:
-- DROP INDEX "${schema}"."${table}_vr_idx";
-- DROP INDEX "${schema}"."${table}_ir_idx";
-- ALTER TABLE "${schema}"."${table}" DROP COLUMN "version_rank";
-- ALTER TABLE "${schema}"."${table}" DROP CONSTRAINT "${table}_pk";
-- ALTER TABLE "${schema}"."${table}" ALTER COLUMN "version" SET NULL;
-- ALTER TABLE "${schema}"."${table}" ADD CONSTRAINT "${table}_pk" PRIMARY KEY ("installed_rank");
-- UPDATE "${schema}"."${table}" SET "type"='BASELINE' WHERE "type"='INIT';
