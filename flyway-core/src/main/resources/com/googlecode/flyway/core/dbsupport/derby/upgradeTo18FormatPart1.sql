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

DROP INDEX ${schema}.${table}_current_version_index;

RENAME TABLE ${schema}.${table} TO "${table}";

ALTER TABLE "${schema}"."${table}" DROP UNIQUE ${schema}.${table}_script_unique;
ALTER TABLE "${schema}"."${table}" DROP PRIMARY KEY;
ALTER TABLE "${schema}"."${table}" DROP COLUMN current_version;

RENAME COLUMN "${schema}"."${table}".version TO "version";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "version" SET DATA TYPE VARCHAR(50);

RENAME COLUMN "${schema}"."${table}".description TO "description";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "description" SET DATA TYPE VARCHAR(200);
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "description" NOT NULL;

RENAME COLUMN "${schema}"."${table}".type TO "type";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "type" SET DATA TYPE VARCHAR(20);
UPDATE "${schema}"."${table}" SET "type" = 'SPRING_JDBC' WHERE "type" = 'JAVA';

RENAME COLUMN "${schema}"."${table}".script TO "script";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "script" SET DATA TYPE VARCHAR(1000);

RENAME COLUMN "${schema}"."${table}".checksum TO "checksum";
RENAME COLUMN "${schema}"."${table}".installed_by TO "installed_by";
RENAME COLUMN "${schema}"."${table}".installed_on TO "installed_on";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "installed_on" NOT NULL;

RENAME COLUMN "${schema}"."${table}".execution_time TO "execution_time";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "execution_time" NOT NULL;

ALTER TABLE "${schema}"."${table}" ADD "version_rank" INT;
ALTER TABLE "${schema}"."${table}" ADD "installed_rank" INT;

ALTER TABLE "${schema}"."${table}" ADD "success" BOOLEAN;
UPDATE "${schema}"."${table}" SET "success" = TRUE WHERE state = 'SUCCESS';
UPDATE "${schema}"."${table}" SET "success" = FALSE WHERE state = 'FAILED';
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "success" NOT NULL;
ALTER TABLE "${schema}"."${table}" DROP COLUMN state;

