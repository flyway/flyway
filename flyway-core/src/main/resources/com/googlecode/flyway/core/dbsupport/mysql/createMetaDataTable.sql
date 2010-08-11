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
    version VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(100),
    migration_type VARCHAR(10) NOT NULL,
    script VARCHAR(200) NOT NULL UNIQUE,
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time INT,
    state VARCHAR(15) NOT NULL,
    current_version BOOL NOT NULL,
    checksum BIGINT,
    PRIMARY KEY(version)
) ENGINE=InnoDB;
ALTER TABLE ${tableName} ADD INDEX ${tableName}_current_version_index (current_version);