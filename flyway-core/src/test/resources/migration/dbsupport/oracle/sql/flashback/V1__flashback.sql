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

CREATE TABLE test (
  id   NUMBER(10) NOT NULL,
  name VARCHAR(20),
  PRIMARY KEY (id)
);

CREATE TABLE table1 (
  id      NUMBER,
  name    VARCHAR2(20),
  test_id NUMBER(10) NOT NULL,
  FOREIGN KEY (test_id) REFERENCES test (id)
);

-- unfortunately you need to specify archive name, but I do not have the permissions to create it in my environment
-- so I will not even try to write it properly, with creating the archive from scratch
ALTER TABLE table1 FLASHBACK ARCHIVE fda_trac;
ALTER TABLE test FLASHBACK ARCHIVE fda_trac;
--from now on you will be unable to delete the tables with plain DROP command

BEGIN
  INSERT INTO test VALUES (1, 'aaa ' || 1);

  INSERT INTO table1 VALUES (2, 'daa' || 2, 1);

  commit;

  --load enough data so the underlying asynchronous flashback structures are indeed created
  FOR i IN 1..10
  LOOP
    FOR j IN 1..1000
    LOOP

      INSERT INTO test VALUES (10000*i+j, 'aaa ' || i);

      INSERT INTO table1 VALUES (j, 'daa' || j, 10000*i+j);

    END LOOP;
    UPDATE test
    SET name = 'cccc' || i;

    COMMIT;

  END LOOP;

END;
/

