--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE VIEW ${schema}.all_misters AS SELECT * FROM ${schema}.test_user WHERE name LIKE 'Mr.%';
