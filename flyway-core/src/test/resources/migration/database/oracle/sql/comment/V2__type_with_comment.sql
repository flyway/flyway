--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

  CREATE OR REPLACE TYPE "PERSON_WITH_COMMENT" as object
( person_id number (15),
  sex       number (1),  ---'1' male, '0' female
  balance   number (10,2)

);

/