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