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

create table EMPLOYEE ( "ID" integer not null, "NAME" varchar(100) );
alter table EMPLOYEE add primary KEY ("ID");
ALTER TABLE EMPLOYEE ADD COLUMN SYS_START TIMESTAMP(12) NOT NULL GENERATED AS ROW BEGIN IMPLICITLY HIDDEN;
ALTER TABLE EMPLOYEE ADD COLUMN SYS_END TIMESTAMP(12) NOT NULL GENERATED AS ROW END IMPLICITLY HIDDEN;
ALTER TABLE EMPLOYEE ADD COLUMN TRANS_ID TIMESTAMP(12) GENERATED AS TRANSACTION START ID IMPLICITLY HIDDEN;
ALTER TABLE EMPLOYEE ADD PERIOD SYSTEM_TIME (sys_start, sys_end);

CREATE TABLE EMPLOYEE_HIST LIKE EMPLOYEE;

ALTER TABLE EMPLOYEE ADD VERSIONING USE HISTORY TABLE EMPLOYEE_HIST;
