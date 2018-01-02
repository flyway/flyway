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

  CREATE OR REPLACE FUNCTION "EVAL" (EXPR VARCHAR2) RETURN VARCHAR2
AS
 RET VARCHAR2(4000);
 BEGIN
  EXECUTE IMMEDIATE 'BEGIN :RESULT := ' || EXPR || '; END;' USING OUT RET;
  RETURN RET;
 END;
/

;

create or replace procedure selectdata is
  v_number number;
  begin
    select 1 into v_number from dual;
    dbms_output.put_line('var>'||v_number);
  end;
/

CALL selectdata();

/

CREATE PROCEDURE remove_emp (employee_id NUMBER) AS
   tot_emps NUMBER;
   BEGIN
      DELETE FROM employees
      WHERE employees.employee_id = remove_emp.employee_id;
   tot_emps := tot_emps - 1;
   END;
/
COMMIT;

EXEC selectdata();