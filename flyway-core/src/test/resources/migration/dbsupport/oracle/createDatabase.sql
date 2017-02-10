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

CREATE USER "FLYWAY" IDENTIFIED BY flyway;
CREATE USER "flyway_proxy" IDENTIFIED BY flyway;
GRANT all privileges TO "FLYWAY";
GRANT all privileges TO "flyway_proxy";
GRANT create session TO "FLYWAY";
GRANT create session TO "flyway_proxy";

ALTER USER "FLYWAY" GRANT CONNECT THROUGH "flyway_proxy";

-- grants for administering queue tables
GRANT EXECUTE ON DBMS_AQADM TO flyway;