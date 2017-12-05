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