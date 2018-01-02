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

CREATE DOMAIN dom as VARCHAR(64);
CREATE TABLE t (x dom);
INSERT INTO T VALUES ('foo');

CREATE SEQUENCE foo_seq;
CREATE DOMAIN foo_seq_dom AS BIGINT NOT NULL DEFAULT nextval('foo_seq');