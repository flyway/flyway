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

-- just to have one statement before the actual test statement
CREATE VIEW all_misters AS SELECT * FROM test_user WHERE name LIKE 'Mr.%';

-- a statement with a comment containing the Strings CREATE PROCEDURE and BEGIN
 CREATE TABLE
 -- could as well be achieved by CREATE PROCEDURE get_misters
 -- BEGIN
 test_user (name VARCHAR(100));

-- just to have a trailing statement for testing
DROP VIEW all_misters;