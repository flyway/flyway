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

CREATE TABLE test_1 (
  id VARCHAR(255) NOT NULL,
  value VARCHAR(255)
);

CREATE TABLE test_2 (
  id VARCHAR(255) NOT NULL,
  dt DATETIME,
  value VARCHAR(255)
);

ALTER TABLE test_2 ADD CONSTRAINT pk_test_2 PRIMARY KEY (id,dt);

CREATE TABLE test_3 (
  id VARCHAR(255) NOT NULL,
  value VARCHAR(255)
);
