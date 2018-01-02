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

  CREATE OR REPLACE TYPE "PERSON_WITH_COMMENT" as object
( person_id number (15),
  sex       number (1),  ---'1' male, '0' female
  balance   number (10,2)

);

/