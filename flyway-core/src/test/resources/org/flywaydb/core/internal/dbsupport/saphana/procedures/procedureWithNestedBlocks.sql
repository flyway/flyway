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

-- a procedure with nested begin-end; blocks
-- also taken from
-- SAP HANA SQLScript Reference - SAP Library at:
-- http://help-legacy.sap.com/saphelp_hanaplatform/helpdata/en/c6/558a64245942ebb52f6a6ddb3e4278/content.htm?frameset=/en/d4/3d91578c3b42b3bacfd89aacf0d62f/frameset.htm&current_toc=/en/ed/4f384562ce4861b48e22a8be3171e5/plain.htm&node_id=60
CREATE PROCEDURE nested_block(OUT val INT) LANGUAGE SQLSCRIPT
READS SQL DATA AS
BEGIN
	DECLARE a INT = 1;
	BEGIN
		DECLARE a INT = 2;
		BEGIN
			DECLARE a INT;
			a = 3;
		END;
		val = a;
	END;
END;

-- just to have a trailing statement for testing
DROP VIEW all_misters;