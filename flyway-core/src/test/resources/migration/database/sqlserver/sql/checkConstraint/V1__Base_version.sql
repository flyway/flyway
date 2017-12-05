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

  CREATE TABLE USERS
  (
    ID int identity( 100000000, 1 ),
    AGE int NOT NULL,
    NAME nvarchar( 50 ) NOT NULL,
    CONSTRAINT PK_USER PRIMARY KEY( ID ),
  )

USE MSDB;