--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE FUNCTION CHECK_AGE( @AGE INT ) RETURNS bigint
AS
BEGIN
    DECLARE
        @valid bit
        IF (@AGE >100)
            SELECT
                @valid= 0 ELSE
            SELECT
                @valid= 1 RETURN @valid
        END