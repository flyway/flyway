--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE VIEW ${schema}.all_misters AS SELECT * FROM ${schema}.test_user WHERE name LIKE 'Mr.%';

-- The pg_sleep is just to simulate creating an index concurrently
-- on a large table which can trigger a deadlock in combination with
-- the use of advisory locks. The sleep is not strictly required for
-- the deadlock to occur.
SELECT pg_sleep(1);

CREATE INDEX CONCURRENTLY idx_test_user_name ON ${schema}.test_user(name);
