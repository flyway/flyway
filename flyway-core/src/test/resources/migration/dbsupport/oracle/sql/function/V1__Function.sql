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

  CREATE OR REPLACE FUNCTION "EVAL" (EXPR VARCHAR2) RETURN VARCHAR2
AS
 RET VARCHAR2(4000);
 BEGIN
  EXECUTE IMMEDIATE 'BEGIN :RESULT := ' || EXPR || '; END;' USING OUT RET;
  RETURN RET;
 END;
/

create or replace procedure selectdata is
  v_number number;
  begin
    select 1 into v_number from dual;
    dbms_output.put_line('var>'||v_number);
  end;
/

CALL selectdata();

CREATE PROCEDURE remove_emp (employee_id NUMBER) AS
   tot_emps NUMBER;
   BEGIN
      DELETE FROM employees
      WHERE employees.employee_id = remove_emp.employee_id;
   tot_emps := tot_emps - 1;
   END;
/
COMMIT;