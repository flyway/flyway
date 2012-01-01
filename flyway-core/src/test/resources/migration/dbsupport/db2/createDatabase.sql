--
-- Copyright (C) 2010-2012 the original author or authors.
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

CREATE DATABASE flyway;

CREATE SCHEMA flyway_1 AUTHORIZATION db2admin;
CREATE SCHEMA flyway_2 AUTHORIZATION db2admin;
CREATE SCHEMA flyway_3 AUTHORIZATION db2admin;

grant ALTERIN,CREATEIN,DROPIN ON SCHEMA Flyway_1 To user db2admin;
grant ALTERIN,CREATEIN,DROPIN ON SCHEMA Flyway_2 To user db2admin;
grant ALTERIN,CREATEIN,DROPIN ON SCHEMA Flyway_3 To user db2admin;
