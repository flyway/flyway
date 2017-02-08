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

--
-- TT FUNCTION
--
CREATE TABLE TT_FUNCTION
(
   CODE            VARCHAR(10) NOT NULL,
   LANG            VARCHAR(10) NOT NULL,
   TEXT_SHORT      VARCHAR(20),
   TEXT_LONG       VARCHAR(80),
   VALID_FROM      DATE NOT NULL,
   VALID_TO        DATE NOT NULL,
   CREATED         DATE NOT NULL,
   INVALIDATED     DATE,
   CREATED_BY      VARCHAR(32) NOT NULL,
   INVALIDATED_BY  VARCHAR(32) NOT NULL,
   PRIMARY KEY (CODE, LANG)
);

CREATE OR REPLACE VIEW V_TT_FUNCTION
(
   CODE,
   LANG,
   TEXT_SHORT,
   TEXT_LONG,
   VALID_FROM,
   VALID_TO,
   CREATED,
   INVALIDATED,
   CREATED_BY,
   INVALIDATED_BY
)
AS SELECT
   CODE,
   LANG,
   TEXT_SHORT,
   TEXT_LONG,
   VALID_FROM,
   VALID_TO,
   CREATED,
   INVALIDATED,
   CREATED_BY,
   INVALIDATED_BY
FROM
   TT_FUNCTION
;

--
-- TT SOME_KV_PACKAGE
--
CREATE TABLE TT_SOME_KV_PACKAGE
(
   CODE            VARCHAR(10) NOT NULL,
   LANG            VARCHAR(10) NOT NULL,
   TEXT_SHORT      VARCHAR(20),
   TEXT_LONG       VARCHAR(80),
   VALID_FROM      DATE NOT NULL,
   VALID_TO        DATE NOT NULL,
   CREATED         DATE NOT NULL,
   INVALIDATED     DATE,
   CREATED_BY      VARCHAR(32) NOT NULL,
   INVALIDATED_BY  VARCHAR(32) NOT NULL,
   PRIMARY KEY (CODE, LANG)
);

CREATE OR REPLACE VIEW V_TT_SOME_KV_PACKAGE
(
   CODE,
   LANG,
   TEXT_SHORT,
   TEXT_LONG,
   VALID_FROM,
   VALID_TO,
   CREATED,
   INVALIDATED,
   CREATED_BY,
   INVALIDATED_BY
)
AS SELECT
   CODE,
   LANG,
   TEXT_SHORT,
   TEXT_LONG,
   VALID_FROM,
   VALID_TO,
   CREATED,
   INVALIDATED,
   CREATED_BY,
   INVALIDATED_BY
FROM
   TT_SOME_KV_PACKAGE
;

CREATE TABLE PURCHASEPROCEDURE (
id NUMBER(18),
name VARCHAR2(100));

CREATE TABLE Action (
  Id NUMBER(11, 0) NOT NULL,
  Application VARCHAR2(64),
  Module VARCHAR2(64),
  Function VARCHAR2(64),
  Action VARCHAR2(64)
);