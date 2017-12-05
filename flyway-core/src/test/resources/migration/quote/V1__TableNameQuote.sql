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

CREATE TABLE "user" (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

CREATE TABLE "group" (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

CREATE TABLE "table" (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

CREATE TABLE "dol$lar" (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);