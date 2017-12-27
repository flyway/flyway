--
-- Copyright 2010-2017 Boxfuse GmbH
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

CREATE TABLE `${table}` (
    `installed_rank` INT64 NOT NULL,
    `version` STRING(50),
    `description` STRING(200) NOT NULL,
    `type` STRING(20) NOT NULL,
    `script` STRING(1000) NOT NULL,
    `checksum` INT64,
    `installed_by` STRING(100) NOT NULL,
    `installed_on` TIMESTAMP,
    `execution_time` INT64 NOT NULL,
    `success` BOOL NOT NULL
)
PRIMARY KEY (`installed_rank`)
;

CREATE INDEX `${table}_s_idx` ON `${table}` (`success`);
