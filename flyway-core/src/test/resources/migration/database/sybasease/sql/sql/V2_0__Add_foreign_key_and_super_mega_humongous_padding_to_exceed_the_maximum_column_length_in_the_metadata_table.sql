--
-- Copyright 2010-2017 Boxfuse GmbH
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

CREATE TABLE couple (
  id INT NOT NULL,
  name1 VARCHAR(25) NOT NULL,
  name2 VARCHAR(25) NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT couple_user1_fk FOREIGN KEY (name1) REFERENCES test_user(name),
  CONSTRAINT couple_user2_fk FOREIGN KEY (name2) REFERENCES test_user(name)
)
go

INSERT INTO couple (id, name1, name2) VALUES (1, 'Mr. Iße T', 'Mr. Semicolon;')
go