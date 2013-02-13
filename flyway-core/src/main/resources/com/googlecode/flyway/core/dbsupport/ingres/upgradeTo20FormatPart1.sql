--
-- Copyright (C) 2010-2013 the original author or authors.
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

DROP INDEX ${table}_current_version_index;

ALTER TABLE ${table} RENAME TO "${table}_bak";
ALTER TABLE "${table}_bak" RENAME TO "${table}";

ALTER TABLE "${table}" DROP CONSTRAINT "${table}_script_unique";
ALTER TABLE "${table}" DROP CONSTRAINT "${table}_primary_key";
ALTER TABLE "${table}" DROP COLUMN current_version;

ALTER TABLE "${table}" ALTER COLUMN "version" SET DATA TYPE VARCHAR(50);

ALTER TABLE "${table}" ALTER COLUMN "description" SET DATA TYPE VARCHAR(200);
ALTER TABLE "${table}" ALTER COLUMN "description" SET NOT NULL;

ALTER TABLE "${table}" ALTER COLUMN "type" SET DATA TYPE VARCHAR(20);
UPDATE "${table}" SET "type" = 'SPRING_JDBC' WHERE "type" = 'JAVA';

ALTER TABLE "${table}" ALTER COLUMN "script" SET DATA TYPE VARCHAR(1000);
ALTER TABLE "${table}" ALTER COLUMN "installed_on" SET NOT NULL;

ALTER TABLE "${table}" ALTER COLUMN "execution_time" SET NOT NULL;

ALTER TABLE "${table}" ADD "version_rank" INT;
ALTER TABLE "${table}" ADD "installed_rank" INT;

ALTER TABLE "${table}" ADD "success" BOOLEAN;
UPDATE "${table}" SET "success" = TRUE WHERE state = 'SUCCESS';
UPDATE "${table}" SET "success" = FALSE WHERE state = 'FAILED';
ALTER TABLE "${table}" ALTER COLUMN "success" SET NOT NULL;
ALTER TABLE "${table}" DROP COLUMN state;

