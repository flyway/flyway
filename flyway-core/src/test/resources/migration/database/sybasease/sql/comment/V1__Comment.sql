--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

/*
   First ' comment
 */
CREATE TABLE user1 (
  name VARCHAR(25) NOT NULL,
  -- second '
  PRIMARY KEY(name)
)

go

CREATE TABLE group1 (
/*
  third '
 */
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
)

go

-- 'fourth'
CREATE TABLE table1 (
-- ' fifth
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
)

go