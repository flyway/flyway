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

/*
   First ' comment
 */

SET CURRENT SQLID = 'AURINT';

CREATE TABLE PERSON1 (
  name VARCHAR(25) NOT NULL,
  -- second '
  PRIMARY KEY(name)
) IN "AURINT".SPERS;

CREATE TABLE GROUP1 (
/*
  third '
 */
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
) IN "AURINT".SPERS;

-- 'fourth'
CREATE TABLE TABLE1 (
-- ' fifth
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
) IN "AURINT".SPERS;

CREATE TABLE TABLE2 (
/*'
  sixth
 '*/
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
) IN "AURINT".SPERS;
