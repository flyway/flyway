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

INSERT INTO ${schema1}.test_user1 (name) VALUES ('Mr. T');
INSERT INTO ${schema1}.test_user1 (name) VALUES ('Mr. Semicolon;');

INSERT INTO ${schema2}.test_user2 (name) VALUES ('Mr. T');
INSERT INTO ${schema2}.test_user2 (name) VALUES ('Mr. Semicolon;');

INSERT INTO ${schema3}.test_user3 (name) VALUES ('Mr. T');
INSERT INTO ${schema3}.test_user3 (name) VALUES ('Mr. Semicolon;');