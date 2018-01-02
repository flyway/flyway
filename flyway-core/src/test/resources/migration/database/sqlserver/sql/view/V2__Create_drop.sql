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

CREATE TABLE [dbo].TEST (
  name NVARCHAR
)

GO

CREATE VIEW dbo.TESTVIEW AS (SELECT T1.* from dbo.TEST as T1)
GO

DROP VIEW dbo.TESTVIEW
GO