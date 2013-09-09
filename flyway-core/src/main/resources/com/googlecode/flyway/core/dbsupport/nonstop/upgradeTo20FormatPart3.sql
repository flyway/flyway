--
-- Copyright 2010-2013 Axel Fontaine and the many contributors.
--
-- Licensed under the Apache License, Version 2.0 (the License);
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an AS IS BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

ALTER TABLE ${schema}.${table} ALTER COLUMN installed_rank SET NOT NULL;
--CALL SYSPROC.ADMIN_CMD ('REORG TABLE ${schema}.${table}');
ALTER TABLE ${schema}.${table} ALTER COLUMN version_rank SET NOT NULL;
--CALL SYSPROC.ADMIN_CMD ('REORG TABLE ${schema}.${table}');
ALTER TABLE ${schema}.${table} ADD PRIMARY KEY (version_rank);
--CALL SYSPROC.ADMIN_CMD ('REORG TABLE ${schema}.${table}');
