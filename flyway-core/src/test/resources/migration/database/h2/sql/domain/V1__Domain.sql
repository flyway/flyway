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

CREATE DOMAIN EMAIL AS VARCHAR(255) CHECK (POSITION('@', VALUE) > 1);

CREATE TABLE test_user (
  name VARCHAR(100) NOT NULL,
  address EMAIL NOT NULL,
  PRIMARY KEY(name)
);

INSERT INTO test_user (name, address) VALUES ('Axel', 'axel@spam.la');