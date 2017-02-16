-- just to have one statement before the actual test statement
CREATE VIEW all_misters AS SELECT * FROM test_user WHERE name LIKE 'Mr.%';

-- a procedure with nested begin-end; blocks
-- also taken from
-- SAP HANA SQLScript Reference - SAP Library at:
-- http://help-legacy.sap.com/saphelp_hanaplatform/helpdata/en/f0/a6dceac8b94cca98dd2741ac6541b8/content.htm?frameset=/en/c6/558a64245942ebb52f6a6ddb3e4278/frameset.htm
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