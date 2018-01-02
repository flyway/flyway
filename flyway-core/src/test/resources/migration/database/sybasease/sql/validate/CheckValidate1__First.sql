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

-- CHANGE IN COMMENT

CREATE TABLE test_user (
  id INT NOT NULL,
  name VARCHAR(25) NOT NULL,  -- this is a valid comment
  PRIMARY KEY(name)
)
go
