--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--


-- checksum buster
CREATE OR REPLACE VIEW all_view AS SELECT * FROM test_user WHERE name LIKE 'Mr.%';
