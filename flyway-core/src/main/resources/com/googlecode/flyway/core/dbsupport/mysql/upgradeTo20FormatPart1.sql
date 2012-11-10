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

DROP INDEX ${table}_current_version_index ON ${schema}.${table};

RENAME TABLE ${schema}.${table} TO `${schema}`.`${table}_bak`;
RENAME TABLE `${schema}`.`${table}_bak` TO `${schema}`.`${table}`;

ALTER TABLE `${schema}`.`${table}` DROP INDEX script;
ALTER TABLE `${schema}`.`${table}` DROP PRIMARY KEY;
ALTER TABLE `${schema}`.`${table}` DROP COLUMN current_version;

ALTER TABLE `${schema}`.`${table}` CHANGE version `version` VARCHAR(50) NOT NULL;
ALTER TABLE `${schema}`.`${table}` CHANGE description `description` VARCHAR(200) NOT NULL;

ALTER TABLE `${schema}`.`${table}` CHANGE type `type` VARCHAR(20) NOT NULL;
UPDATE `${schema}`.`${table}` SET `type` = 'SPRING_JDBC' WHERE `type` = 'JAVA';

ALTER TABLE `${schema}`.`${table}` CHANGE script `script` VARCHAR(1000) NOT NULL;
ALTER TABLE `${schema}`.`${table}` CHANGE checksum `checksum` INT;
ALTER TABLE `${schema}`.`${table}` CHANGE installed_by `installed_by` VARCHAR(30) NOT NULL;
ALTER TABLE `${schema}`.`${table}` CHANGE installed_on `installed_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE `${schema}`.`${table}` CHANGE execution_time `execution_time` INT NOT NULL;

ALTER TABLE `${schema}`.`${table}` ADD `version_rank` INT;
ALTER TABLE `${schema}`.`${table}` ADD `installed_rank` INT;

ALTER TABLE `${schema}`.`${table}` ADD `success` BOOL;
UPDATE `${schema}`.`${table}` SET `success` = TRUE WHERE state = 'SUCCESS';
UPDATE `${schema}`.`${table}` SET `success` = FALSE WHERE state = 'FAILED';
ALTER TABLE `${schema}`.`${table}` MODIFY COLUMN `success` BOOL NOT NULL;
ALTER TABLE `${schema}`.`${table}` DROP COLUMN state;

