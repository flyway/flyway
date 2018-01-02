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

-- Create overall database
IF NOT EXISTS(SELECT * FROM sys.databases WHERE name = '${DBNAME}')
   BEGIN
    PRINT 'Creating ${DBNAME} Database'
    CREATE DATABASE ${DBNAME}
   END
ELSE
    PRINT '${DBNAME} Database Exists'
GO