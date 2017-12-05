--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

DROP ROLE IF EXISTS testrole;
CREATE ROLE testrole LOGIN PASSWORD 'testrole';
SET ROLE testrole;