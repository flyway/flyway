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

BEGIN
   DBMS_UTILITY.COMPILE_SCHEMA(SCHEMA => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'));
END;
/