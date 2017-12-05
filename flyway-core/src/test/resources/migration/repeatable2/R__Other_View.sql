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

-- checksum buster
CREATE OR REPLACE VIEW other_view AS SELECT * FROM test_user WHERE name LIKE 'Mr.%';
