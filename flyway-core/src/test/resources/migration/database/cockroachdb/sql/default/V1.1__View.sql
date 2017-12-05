--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE VIEW all_misters AS SELECT id, name FROM test_user WHERE name LIKE 'Mr.%';
