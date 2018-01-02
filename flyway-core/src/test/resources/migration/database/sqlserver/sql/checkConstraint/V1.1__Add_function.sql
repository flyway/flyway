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