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

CREATE TABLE test1(a1 INT);
CREATE TABLE test2(a2 INT);
CREATE TABLE test3(a3 SERIAL NOT NULL PRIMARY KEY);
CREATE TABLE test4(
  a4 SERIAL NOT NULL PRIMARY KEY,
  b4 INT DEFAULT 0
);

CREATE SEQUENCE test_sequence START 101;
SELECT setval('test_sequence', 400);

CREATE FUNCTION testtrigger() RETURNS trigger
AS $$
  BEGIN
    INSERT INTO test2 (a2) VALUES(NEW.a1);
    DELETE FROM test3 WHERE a3 = NEW.a1;
    UPDATE test4 SET b4 = b4 + 1 WHERE a4 = NEW.a1;
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER testref BEFORE INSERT ON test1
  FOR EACH ROW EXECUTE PROCEDURE testtrigger();



INSERT INTO test3 (a3) VALUES
  (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT),
  (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT);

INSERT INTO test4 (a4) VALUES
  (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT), (DEFAULT);
