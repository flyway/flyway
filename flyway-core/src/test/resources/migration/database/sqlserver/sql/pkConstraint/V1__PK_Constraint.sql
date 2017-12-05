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

CREATE TABLE test_1 (
  id VARCHAR(255) NOT NULL,
  value VARCHAR(255)
);

CREATE TABLE test_2 (
  id VARCHAR(255) NOT NULL,
  dt DATETIME,
  value VARCHAR(255)
);

ALTER TABLE test_2 ADD CONSTRAINT pk_test_2 PRIMARY KEY (id,dt);

CREATE TABLE test_3 (
  id VARCHAR(255) NOT NULL,
  value VARCHAR(255)
);
