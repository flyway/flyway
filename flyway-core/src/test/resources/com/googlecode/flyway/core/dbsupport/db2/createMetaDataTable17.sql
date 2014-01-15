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

CREATE TABLE ${schema}.${table} (
    version VARCHAR(20) NOT NULL,
    description VARCHAR(100),
    type VARCHAR(10) NOT NULL,
    script VARCHAR(200) NOT NULL UNIQUE,
    CONSTRAINT "${table}_script_unique" UNIQUE(script),
    checksum BIGINT,
    installed_by VARCHAR(30) NOT NULL,
    installed_on TIMESTAMP DEFAULT CURRENT TIMESTAMP NOT NULL,
    execution_time INT,
    state VARCHAR(15) NOT NULL,
    current_version SMALLINT NOT NULL,
    CONSTRAINT ${table}_currversion CHECK (current_version in(0,1)),
    PRIMARY KEY (version)
);
CREATE INDEX ${schema}.${table}_cv_idx ON ${schema}.${table} (current_version);