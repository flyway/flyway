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


CREATE OR REPLACE Function IncomeLevel
     ( monthly_value IN number(6) )
     RETURN varchar2
IS
     ILevel varchar2(20);

BEGIN

  IF monthly_value <= 4000 THEN  /* for the poor */
     ILevel := 'Low Income';

  ELSIF monthly_value > 4000 and/*and weekly_value*/monthly_value <= 7000 THEN /* for the middle class */
     ILevel := 'Avg Income';

  ELSIF monthly_value > 7000 and/* /* */monthly_value <= 15000 THEN /* for the well-off */
     ILevel := 'Moderate Income';

  ELSE /* for the rich */
     ILevel := 'High Income';

  END IF;

  RETURN ILevel;

END;
/