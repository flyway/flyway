--
-- Copyright (C) 2009-2010 the original author or authors.
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

CREATE TABLE ${tableName} (
    version VARCHAR(20) PRIMARY KEY,
    description VARCHAR(100),
    script VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time INT,
    state VARCHAR(15) NOT NULL,
    current_version BOOLEAN NOT NULL,
    CONSTRAINT unique_script UNIQUE (script)
);
CREATE INDEX ${tableName}_current_version_index ON ${tableName} (current_version);