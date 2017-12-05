--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

INSERT INTO test_user (name) VALUES ( $$Mr. T$$);
INSERT INTO test_user (name) VALUES ( $$Mr. ' Quote$$);
INSERT INTO test_user (name) VALUES ( $$'Mr. Semicolon+Linebreak;
another line'$$);