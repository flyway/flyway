--
-- Copyright 2010-2016 Boxfuse GmbH
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

CREATE TABLE ${schema1}.couple1 (
  id INT NOT NULL,
  name1 VARCHAR(25) NOT NULL,
  name2 VARCHAR(25) NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT couple_user1_fk FOREIGN KEY (name1) REFERENCES ${schema1}.test_user1(name),
  CONSTRAINT couple_user2_fk FOREIGN KEY (name2) REFERENCES ${schema1}.test_user1(name)
);
INSERT INTO ${schema1}.couple1 (id, name1, name2) VALUES (1, 'Mr. T', 'Mr. Semicolon;');

CREATE TABLE ${schema2}.couple2 (
  id INT NOT NULL,
  name1 VARCHAR(25) NOT NULL,
  name2 VARCHAR(25) NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT couple_user1_fk FOREIGN KEY (name1) REFERENCES ${schema2}.test_user2(name),
  CONSTRAINT couple_user2_fk FOREIGN KEY (name2) REFERENCES ${schema2}.test_user2(name)
);
INSERT INTO ${schema2}.couple2 (id, name1, name2) VALUES (1, 'Mr. T', 'Mr. Semicolon;');

CREATE TABLE ${schema3}.couple3 (
  id INT NOT NULL,
  name1 VARCHAR(25) NOT NULL,
  name2 VARCHAR(25) NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT couple_user1_fk FOREIGN KEY (name1) REFERENCES ${schema3}.test_user3(name),
  CONSTRAINT couple_user2_fk FOREIGN KEY (name2) REFERENCES ${schema3}.test_user3(name)
);
INSERT INTO ${schema3}.couple3 (id, name1, name2) VALUES (1, 'Mr. T', 'Mr. Semicolon;');