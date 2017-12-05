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

CREATE TABLE ${schema1}.test_user1 (
  name VARCHAR(25) NOT NULL,  -- this is a valid comment
  PRIMARY KEY(name)
);

CREATE TABLE ${schema2}.test_user2 (
  name VARCHAR(25) NOT NULL,  -- this is a valid comment
  PRIMARY KEY(name)
);

CREATE TABLE ${schema3}.test_user3 (
  name VARCHAR(25) NOT NULL,  -- this is a valid comment
  PRIMARY KEY(name)
);
