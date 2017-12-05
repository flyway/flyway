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

/*
   First ' comment
 */
CREATE TABLE user1 (
  name VARCHAR(25) NOT NULL,
  -- second '
  PRIMARY KEY(name)
);

CREATE TABLE group1 (
/*
  third '
 */
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

-- 'fourth'
CREATE TABLE table1 (
-- ' fifth
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

CREATE TABLE table2 (
/*'
  sixth
 '*/
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);
