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

CREATE DATABASE flyway_db DEFAULT CHARACTER SET 'utf8' DEFAULT COLLATE 'utf8_bin';
CREATE USER 'flyway'@'localhost' IDENTIFIED BY 'flyway';

-- For MySQL 5.1 and up
GRANT all ON *.* TO 'flyway'@'localhost';

