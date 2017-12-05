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

-- just to have one statement before the actual test statement
CREATE VIEW all_misters AS SELECT * FROM test_user WHERE name LIKE 'Mr.%';

-- a statement with a comment containing the Strings CREATE PROCEDURE and BEGIN
 CREATE TABLE
 -- could as well be achieved by CREATE PROCEDURE get_misters
 -- BEGIN
 test_user (name VARCHAR(100));

-- just to have a trailing statement for testing
DROP VIEW all_misters;