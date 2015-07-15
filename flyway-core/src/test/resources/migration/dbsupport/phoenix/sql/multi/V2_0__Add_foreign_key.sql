--
-- Copyright 2010-2015 Axel Fontaine
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

-- We're not actually adding a foreign key, but keeping the name in line
-- means the unit test will run
CREATE TABLE ${schema1}.couple1 (
  id INTEGER NOT NULL,
  name1 VARCHAR(25) NOT NULL,
  name2 VARCHAR(25) NOT NULL
  CONSTRAINT pk PRIMARY KEY (id, name1, name2)
);
UPSERT INTO ${schema1}.couple1 (id, name1, name2) VALUES (1, 'Mr. T', 'Mr. Semicolon;');