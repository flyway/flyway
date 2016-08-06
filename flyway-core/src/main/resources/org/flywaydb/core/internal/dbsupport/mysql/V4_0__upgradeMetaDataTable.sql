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

DROP INDEX `${table}_vr_idx` ON `${schema}`.`${table}`;
DROP INDEX `${table}_ir_idx` ON `${schema}`.`${table}`;
ALTER TABLE `${schema}`.`${table}` DROP COLUMN `version_rank`;
-- Do this in a single step in case `innodb_force_primary_key` is enabled
ALTER TABLE `${schema}`.`${table}` DROP PRIMARY KEY, ADD CONSTRAINT `${table}_pk` PRIMARY KEY (`installed_rank`);
ALTER TABLE `${schema}`.`${table}` MODIFY `version` VARCHAR(50);
UPDATE `${schema}`.`${table}` SET `type`='BASELINE' WHERE `type`='INIT';
