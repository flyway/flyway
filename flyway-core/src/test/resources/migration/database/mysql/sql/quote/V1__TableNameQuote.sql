--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE TABLE `user` (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

CREATE TABLE `group` (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

CREATE TABLE `table` (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);