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

CREATE FUNCTION dbo.fnA()
  RETURNS VARCHAR(50)
  WITH SCHEMABINDING
AS
  BEGIN
    RETURN 'function a';
  END;