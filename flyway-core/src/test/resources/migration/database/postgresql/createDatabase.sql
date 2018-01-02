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

CREATE ROLE flyway LOGIN PASSWORD 'flyway';
CREATE DATABASE flyway_db
  WITH OWNER = flyway ENCODING = 'UTF8' TABLESPACE = pg_default;