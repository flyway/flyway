--
-- Copyright 2010-2018 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--
-- Must
-- be
-- exactly
-- 13 lines
-- to match
-- community
-- edition
-- license
-- length.
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
