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

CREATE PACKAGE TEST_CTX_PKG AS
  DUMMY NUMBER;
END;
/

CREATE CONTEXT TEST_CTX USING TEST_CTX_PKG;
