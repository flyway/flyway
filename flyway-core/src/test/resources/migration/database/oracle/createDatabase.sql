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

-- create test users
alter session set "_ORACLE_SCRIPT"=true;
CREATE USER FLYWAY IDENTIFIED BY "flyway";
CREATE USER FLYWAY_AUX IDENTIFIED BY "flyway";
CREATE USER "flyway_proxy" IDENTIFIED BY "flyway";
GRANT ALL PRIVILEGES TO FLYWAY;
GRANT ALL PRIVILEGES TO FLYWAY_AUX;
ALTER USER FLYWAY GRANT CONNECT THROUGH "flyway_proxy";

-- grants for reading some DBA_ dictionary views
GRANT SELECT ON DBA_REGISTRY TO FLYWAY;
GRANT SELECT ON DBA_CONTEXT TO FLYWAY;
GRANT SELECT ON DBA_XML_SCHEMAS TO FLYWAY;
GRANT SELECT ON DBA_OBJECTS TO FLYWAY;
GRANT SELECT ON DBA_DB_LINKS TO FLYWAY;

-- grant for administering queue tables
GRANT AQ_ADMINISTRATOR_ROLE TO FLYWAY;

-- grant for administering XML schemas
GRANT XDBADMIN TO FLYWAY;

-- grant for administering advanced rewrite
GRANT EXECUTE ON SYS.DBMS_ADVANCED_REWRITE TO FLYWAY;

-- grant for administering file groups
GRANT EXECUTE ON DBMS_FILE_GROUP TO FLYWAY;

-- create flashback archive if possible
DECLARE
  l_flg NUMBER;
BEGIN
  SELECT COUNT(*) INTO l_flg FROM V$OPTION WHERE PARAMETER = 'Flashback Data Archive' AND VALUE = 'TRUE';
  IF l_flg > 0 THEN
    EXECUTE IMMEDIATE 'CREATE FLASHBACK ARCHIVE FLYWAY_FBA TABLESPACE USERS QUOTA 10 M RETENTION 1 DAY';
  END IF;
END;
/