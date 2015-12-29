--
-- Copyright 2010-2015 Boxfuse GmbH
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


CREATE TABLESPACE SPERS
      IN TESTADM
     USING STOGROUP SENSITIV PRIQTY -1 SECQTY -1 ERASE NO FREEPAGE 0 PCTFREE 10 DEFINE YES TRACKMOD YES
       SEGSIZE 64
     BUFFERPOOL BP3
     LOCKSIZE  PAGE
     LOCKMAX SYSTEM
     CLOSE YES
     COMPRESS YES
     CCSID UNICODE
;

CREATE TABLE EMPLOYEE (
  id INT NOT NULL,
  name VARCHAR(25) NOT NULL
)
IN TESTADM.SPERS
  CCSID UNICODE;

  CREATE TABLE COMPANY_STATS (
  id INT NOT NULL,
  NBEMP INT
)
IN TESTADM.SPERS
  CCSID UNICODE;

CREATE UNIQUE INDEX COMPANY_STATS_pk_idx ON COMPANY_STATS (id ASC);
ALTER TABLE COMPANY_STATS ADD CONSTRAINT COMPANY_STATS_PK PRIMARY KEY (id);

CREATE UNIQUE INDEX EMPLOYEE_pk_idx ON EMPLOYEE (id ASC);
ALTER TABLE EMPLOYEE ADD CONSTRAINT EMPLOYEE_PK PRIMARY KEY (id);

CREATE TRIGGER NEW_HIRED
     AFTER INSERT ON EMPLOYEE
     FOR EACH ROW MODE DB2SQL
     INSERT INTO COMPANY_STATS(id, NBEMP) VALUES (1, 1);



