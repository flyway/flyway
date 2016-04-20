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

CREATE TABLE `user` (
  name VARCHAR(25)
);

-- group is a reserved keyword in hive
CREATE TABLE `group` (
  name VARCHAR(25)
);

CREATE TABLE `table` (
  name VARCHAR(25)
);

-- special characters in table names are not supported in Hive
--CREATE TABLE `dol$lar` (
--  name VARCHAR(25)
--);