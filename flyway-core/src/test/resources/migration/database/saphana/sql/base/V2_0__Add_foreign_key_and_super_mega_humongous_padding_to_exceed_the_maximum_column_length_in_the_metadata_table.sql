--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE TABLE couple (
  id INT NOT NULL,
  name1 VARCHAR(25) NOT NULL REFERENCES test_user(name),
  name2 VARCHAR(25) NOT NULL REFERENCES test_user(name),
  PRIMARY KEY(id)
);

INSERT INTO couple (id, name1, name2) VALUES (1, N'Mr. Iße T', 'Mr. Semicolon;');