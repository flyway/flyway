--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

  CREATE OR REPLACE TYPE "PERSON_WITHOUT_COMMENT" as object
( person_id number (15),
  sex       number (1),
  balance   number (10,2)

);

/