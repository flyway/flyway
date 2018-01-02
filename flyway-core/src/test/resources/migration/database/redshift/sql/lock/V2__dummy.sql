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

/*
  We need this dummy migration for the test because it ensures that table SCHEMA_VERSION has been created and committed
  when the lock test is done in next migration file
*/
SELECT 1;
