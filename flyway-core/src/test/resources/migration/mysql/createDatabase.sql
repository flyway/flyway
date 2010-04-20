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

CREATE DATABASE migration_test_db DEFAULT CHARACTER SET 'utf8' DEFAULT COLLATE 'utf8_bin';
CREATE USER 'test_ddl_user'@'localhost' IDENTIFIED BY 'ddlPassword';
CREATE USER 'test_dml_user'@'localhost' IDENTIFIED BY 'dmlPassword';
GRANT all ON migration_test_db.* TO 'test_ddl_user'@'localhost' IDENTIFIED BY 'ddlPassword';
GRANT select,insert,update,delete ON migration_test_db.* TO 'test_dml_user'@'localhost' IDENTIFIED BY 'dmlPassword';
FLUSH PRIVILEGES;