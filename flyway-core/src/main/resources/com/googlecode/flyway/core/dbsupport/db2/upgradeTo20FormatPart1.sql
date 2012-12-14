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

RENAME TABLE ${schema}.${table} TO "${table}_bak";
RENAME TABLE ${schema}."${table}_bak" TO "${table}";

ALTER TABLE "${schema}"."${table}" DROP UNIQUE "${table}_script_unique";
ALTER TABLE "${schema}"."${table}" DROP CHECK ${table}_currversion;
ALTER TABLE "${schema}"."${table}" DROP PRIMARY KEY;
ALTER TABLE "${schema}"."${table}" DROP COLUMN current_version;

ALTER TABLE "${schema}"."${table}" RENAME COLUMN version TO "version";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "version" SET DATA TYPE VARCHAR(50);

ALTER TABLE "${schema}"."${table}" RENAME COLUMN description TO "description";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "description" SET DATA TYPE VARCHAR(200);
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "description" SET NOT NULL;

ALTER TABLE "${schema}"."${table}" RENAME COLUMN type TO "type";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "type" SET DATA TYPE VARCHAR(20);
CALL SYSPROC.ADMIN_CMD ('REORG TABLE "${schema}"."${table}"');
UPDATE "${schema}"."${table}" SET "type" = 'SPRING_JDBC' WHERE "type" = 'JAVA';

ALTER TABLE "${schema}"."${table}" RENAME COLUMN script TO "script";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "script" SET DATA TYPE VARCHAR(1000);

ALTER TABLE "${schema}"."${table}" ADD "checksum_s" INT;
CALL SYSPROC.ADMIN_CMD ('REORG TABLE "${schema}"."${table}"');
UPDATE "${schema}"."${table}" SET "checksum_s" = INTEGER(checksum) WHERE checksum IS NOT NULL;
ALTER TABLE "${schema}"."${table}" DROP COLUMN checksum;
ALTER TABLE "${schema}"."${table}" RENAME COLUMN "checksum_s" TO "checksum";

ALTER TABLE "${schema}"."${table}" RENAME COLUMN installed_by TO "installed_by";
ALTER TABLE "${schema}"."${table}" RENAME COLUMN installed_on TO "installed_on";

ALTER TABLE "${schema}"."${table}" RENAME COLUMN execution_time TO "execution_time";
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "execution_time" SET NOT NULL;

ALTER TABLE "${schema}"."${table}" ADD "version_rank" INT;
ALTER TABLE "${schema}"."${table}" ADD "installed_rank" INT;

ALTER TABLE "${schema}"."${table}" ADD "success" SMALLINT;
CALL SYSPROC.ADMIN_CMD ('REORG TABLE "${schema}"."${table}"');
UPDATE "${schema}"."${table}" SET "success" = 1 WHERE state = 'SUCCESS';
UPDATE "${schema}"."${table}" SET "success" = 0 WHERE state = 'FAILED';
ALTER TABLE "${schema}"."${table}" ALTER COLUMN "success" SET NOT NULL;
ALTER TABLE "${schema}"."${table}" ADD CONSTRAINT "${table}_s" CHECK ("success" in(0,1));
ALTER TABLE "${schema}"."${table}" DROP COLUMN state;
CALL SYSPROC.ADMIN_CMD ('REORG TABLE "${schema}"."${table}"');

