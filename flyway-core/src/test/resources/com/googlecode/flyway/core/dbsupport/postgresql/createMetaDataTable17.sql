--
-- Copyright 2010-2013 Axel Fontaine and the many contributors.
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

CREATE TABLE ${schema}.${table} (
    "version" VARCHAR(20) NOT NULL,
    description VARCHAR(100),
    "type" VARCHAR(10) NOT NULL,
    script VARCHAR(200) NOT NULL,
    checksum INTEGER,
    installed_by VARCHAR(30) NOT NULL,
    installed_on TIMESTAMP DEFAULT now(),
    execution_time INTEGER,
    state VARCHAR(15) NOT NULL,
    current_version BOOLEAN NOT NULL,
    CONSTRAINT ${table}_primary_key PRIMARY KEY (version),
    CONSTRAINT ${table}_script_unique UNIQUE (script)
) WITH (
  OIDS=FALSE
);
CREATE INDEX ${table}_current_version_index ON ${schema}.${table} (current_version);