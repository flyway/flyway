--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE TABLE usertable (
  id INT NOT NULL,
  name VARCHAR(25) NOT NULL,  -- this is a valid comment
  PRIMARY KEY(name)
);

CREATE TRIGGER update_usertable UPDATE OF name ON usertable
  BEGIN
    UPDATE usertable SET id = 666 WHERE name = old.name;
  END;