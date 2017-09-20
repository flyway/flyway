--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- create test users
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