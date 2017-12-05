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

ALTER TABLE
    USERS ADD CONSTRAINT NO_OLDER_THAN_100_CONSTRAINT CHECK (dbo.CHECK_AGE(AGE)=1)