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

DROP INDEX ${schema}.${table}_current_version_index;

ALTER TABLE ${schema}.${table} RENAME TO "${table}_bak";
ALTER TABLE ${schema}."${table}_bak" RENAME TO "${table}";

ALTER TABLE "${schema}"."${table}" DROP CONSTRAINT ${table}_script_unique;
ALTER TABLE "${schema}"."${table}" DROP CONSTRAINT ${table}_primary_key;
ALTER TABLE "${schema}"."${table}" DROP COLUMN current_version;

ALTER TABLE "${schema}"."${table}" ALTER COLUMN "version" SET DATA TYPE VARCHAR(50);

ALTER TABLE "${schema}"."${table}" ALTER COLUMN "description" SET DATA TYPE VARCHAR(200);
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "description" SET NOT NULL;

ALTER TABLE "${schema}"."${table}" ALTER COLUMN "type" SET DATA TYPE VARCHAR(20);
UPDATE "${schema}"."${table}" SET "type" = 'SPRING_JDBC' WHERE "type" = 'JAVA';

ALTER TABLE "${schema}"."${table}" ALTER COLUMN "script" SET DATA TYPE VARCHAR(1000);
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "installed_on" SET NOT NULL;

ALTER TABLE "${schema}"."${table}" ALTER COLUMN "execution_time" SET NOT NULL;

ALTER TABLE "${schema}"."${table}" ADD "version_rank" INT;
ALTER TABLE "${schema}"."${table}" ADD "installed_rank" INT;

ALTER TABLE "${schema}"."${table}" ADD "success" BOOLEAN;
UPDATE "${schema}"."${table}" SET "success" = TRUE WHERE state = 'SUCCESS';
UPDATE "${schema}"."${table}" SET "success" = FALSE WHERE state = 'FAILED';
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "success" SET NOT NULL;
ALTER TABLE "${schema}"."${table}" DROP COLUMN state;

