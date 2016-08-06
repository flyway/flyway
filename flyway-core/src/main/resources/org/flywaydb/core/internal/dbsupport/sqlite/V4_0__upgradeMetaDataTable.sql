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

UPDATE "${schema}"."${table}" SET "type"='BASELINE' WHERE "type"='INIT';
ALTER TABLE "${schema}"."${table}" RENAME TO "${table}_3";
CREATE TABLE "${schema}"."${table}" (
  "installed_rank" INT NOT NULL PRIMARY KEY,
  "version" VARCHAR(50),
  "description" VARCHAR(200) NOT NULL,
  "type" VARCHAR(20) NOT NULL,
  "script" VARCHAR(1000) NOT NULL,
  "checksum" INT,
  "installed_by" VARCHAR(100) NOT NULL,
  "installed_on" TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f','now')),
  "execution_time" INT NOT NULL,
  "success" BOOLEAN NOT NULL
);
INSERT INTO "${schema}"."${table}" SELECT "installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success" FROM "${schema}"."${table}_3";
DROP TABLE "${schema}"."${table}_3";