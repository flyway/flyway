--
-- Copyright (C) 2010-2012 the original author or authors.
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

DROP INDEX ${schema}.${table}_cv_idx;

ALTER TABLE ${schema}.${table} RENAME TO "${table}";

ALTER TABLE "${schema}"."${table}" DROP UNIQUE (script);
ALTER TABLE "${schema}"."${table}" DROP PRIMARY KEY;
ALTER TABLE "${schema}"."${table}" DROP COLUMN current_version;

ALTER TABLE "${schema}"."${table}" RENAME COLUMN version TO "version";
ALTER TABLE "${schema}"."${table}" MODIFY ("version" VARCHAR2(50));

ALTER TABLE "${schema}"."${table}" RENAME COLUMN description TO "description";
ALTER TABLE "${schema}"."${table}" MODIFY ("description" VARCHAR2(200) NOT NULL);

ALTER TABLE "${schema}"."${table}" RENAME COLUMN type TO "type";
ALTER TABLE "${schema}"."${table}" MODIFY ("type" VARCHAR2(20));
UPDATE "${schema}"."${table}" SET "type" = 'SPRING_JDBC' WHERE "type" = 'JAVA';

ALTER TABLE "${schema}"."${table}" RENAME COLUMN script TO "script";
ALTER TABLE "${schema}"."${table}" MODIFY ("script" VARCHAR(1000));

ALTER TABLE "${schema}"."${table}" RENAME COLUMN checksum TO "checksum";
ALTER TABLE "${schema}"."${table}" RENAME COLUMN installed_by TO "installed_by";

ALTER TABLE "${schema}"."${table}" RENAME COLUMN installed_on TO "installed_on";
ALTER TABLE "${schema}"."${table}" MODIFY ("installed_on" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);

ALTER TABLE "${schema}"."${table}" RENAME COLUMN execution_time TO "execution_time";
ALTER TABLE "${schema}"."${table}" MODIFY ("execution_time" INT NOT NULL);

ALTER TABLE "${schema}"."${table}" ADD "version_rank" INT;
ALTER TABLE "${schema}"."${table}" ADD "installed_rank" INT;

ALTER TABLE "${schema}"."${table}" ADD "success" NUMBER(1);
UPDATE "${schema}"."${table}" SET "success" = 1 WHERE state = 'SUCCESS';
UPDATE "${schema}"."${table}" SET "success" = 0 WHERE state = 'FAILED';
ALTER TABLE "${schema}"."${table}" MODIFY ("success" NUMBER(1) NOT NULL);
ALTER TABLE "${schema}"."${table}" DROP COLUMN state;

