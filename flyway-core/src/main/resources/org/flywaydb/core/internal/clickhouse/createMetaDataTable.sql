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

CREATE TABLE `${schema}`.`${table}` (
    installed_rank Int32,
    version Nullable(String),
    description String,
    type String,
    script String,
    checksum Nullable(Int32),
    installed_by Nullable(String),
    installed_on DateTime,
    execution_time Int32,
    success UInt8
) ENGINE = TinyLog;
