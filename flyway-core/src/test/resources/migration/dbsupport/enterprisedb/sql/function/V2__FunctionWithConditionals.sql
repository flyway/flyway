--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
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

  ELSIF monthly_value > 7000 and/* / */ monthly_value <= 15000 THEN /* for the well-off */
     ILevel := 'Moderate Income';

  ELSE /* for the rich */
     ILevel := 'High Income';

  END IF;

  RETURN ILevel;

END;
/