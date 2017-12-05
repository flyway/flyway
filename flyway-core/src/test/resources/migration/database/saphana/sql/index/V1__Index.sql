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

CREATE TABLE t (a INT, b NVARCHAR(10), c NVARCHAR(20));
 CREATE INDEX idx ON t(b);

CREATE COLUMN TABLE A (A VARCHAR(10) PRIMARY KEY, B VARCHAR(10));
CREATE FULLTEXT INDEX i ON A(A) FUZZY SEARCH INDEX OFF SYNC;