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

-- Create table
CREATE TABLE "${schema}"."${table}" (
    "version" VARCHAR(50),
    "installed_rank" INTEGER NOT NULL PRIMARY KEY,
    "description" VARCHAR(200),
    "type" VARCHAR(20),
    "script" VARCHAR(1000),
    "checksum" INTEGER,
    "installed_by" VARCHAR(100),
    "installed_on" TIMESTAMP,
    "execution_time" INTEGER,
    "success" BOOLEAN
);

-- Create indices
--CREATE INDEX "${table}_s_idx" ON "${schema}"."${table}" ("success");