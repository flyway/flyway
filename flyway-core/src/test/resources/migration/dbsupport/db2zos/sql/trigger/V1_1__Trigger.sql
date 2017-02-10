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
  id INT NOT NULL,
  name VARCHAR(25) NOT NULL
)
IN "AURINT".SPERS;

  CREATE TABLE COMPANY_STATS (
  id INT NOT NULL,
  NBEMP INT
)
IN "AURINT".SPERS;

CREATE UNIQUE INDEX COMPANY_STATS_pk_idx ON COMPANY_STATS (id ASC);
ALTER TABLE COMPANY_STATS ADD CONSTRAINT COMPANY_STATS_PK PRIMARY KEY (id);

CREATE UNIQUE INDEX EMPLOYEE_pk_idx ON EMPLOYEE (id ASC);
ALTER TABLE EMPLOYEE ADD CONSTRAINT EMPLOYEE_PK PRIMARY KEY (id);

CREATE TRIGGER NEW_HIRED
     AFTER INSERT ON EMPLOYEE
     FOR EACH ROW MODE DB2SQL
     INSERT INTO COMPANY_STATS(id, NBEMP) VALUES (1, 1);