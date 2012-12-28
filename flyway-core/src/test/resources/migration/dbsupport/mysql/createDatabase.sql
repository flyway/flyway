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

CREATE DATABASE flyway_db DEFAULT CHARACTER SET 'utf8' DEFAULT COLLATE 'utf8_bin';
CREATE DATABASE flyway_cloudsql_db DEFAULT CHARACTER SET 'utf8' DEFAULT COLLATE 'utf8_bin';
CREATE USER 'flyway' IDENTIFIED BY 'flyway';

-- For MySQL 5.1 and up
GRANT all ON *.* TO 'flyway' IDENTIFIED BY 'flyway';

-- For MySQL 5.0 which still needs explicit grants
GRANT all ON flyway_db.* TO 'flyway' IDENTIFIED BY 'flyway';
GRANT all ON flyway_cloudsql_db.* TO 'flyway' IDENTIFIED BY 'flyway';
GRANT all ON current_schema_test.* TO 'flyway' IDENTIFIED BY 'flyway';
GRANT all ON concurrent_test.* TO 'flyway' IDENTIFIED BY 'flyway';

FLUSH PRIVILEGES;