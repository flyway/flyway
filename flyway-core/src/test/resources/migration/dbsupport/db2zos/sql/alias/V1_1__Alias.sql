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

SET CURRENT SQLID = 'AURINT';

CREATE TABLE EMPLOYEE (
    "ID" integer not null,
     "NAME" varchar(100)
)
 IN "AURINT".SPERS;

-- Indexes and constraints
CREATE UNIQUE INDEX EMPLOYEE_pk_idx ON EMPLOYEE (id ASC);
ALTER TABLE EMPLOYEE ADD CONSTRAINT EMPLOYEE_PK PRIMARY KEY (id);

INSERT INTO EMPLOYEE VALUES(1, 'Mark');
INSERT INTO EMPLOYEE VALUES(2, 'Tommy');

CREATE ALIAS "POOR_SLAVE" FOR EMPLOYEE;