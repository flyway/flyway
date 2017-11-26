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

CREATE TABLE some_table (
  id int
);

CREATE TABLE some_other_table (
  id int
);

CREATE TABLE some_other_table2 (
  id int
);

CREATE OR REPLACE RULE my_rule AS
ON DELETE TO some_table
DO ALSO
(
DELETE FROM some_other_table WHERE some_other_table.id=123;
DELETE FROM some_other_table2 WHERE some_other_table2.id=123;
);

CREATE TABLE some_other_table4 (
  id int
);
