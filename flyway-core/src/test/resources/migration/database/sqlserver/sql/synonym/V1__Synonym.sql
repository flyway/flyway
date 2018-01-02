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

CREATE TABLE [dbo].[MY_TABLE] (
ID INT PRIMARY KEY NOT NULL,
DESCRIPTION VARCHAR(20)
)

CREATE SYNONYM [dbo].[MY_SYNONYM] FOR [dbo].[MY_TABLE]