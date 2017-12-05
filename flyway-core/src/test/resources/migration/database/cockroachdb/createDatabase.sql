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

CREATE USER flyway;
CREATE DATABASE flyway_db;
CREATE DATABASE flyway_1;
CREATE DATABASE flyway_2;
CREATE DATABASE flyway_3;
GRANT ALL ON DATABASE flyway_db TO flyway;
GRANT ALL ON DATABASE flyway_1 TO flyway;
GRANT ALL ON DATABASE flyway_2 TO flyway;
GRANT ALL ON DATABASE flyway_3 TO flyway;