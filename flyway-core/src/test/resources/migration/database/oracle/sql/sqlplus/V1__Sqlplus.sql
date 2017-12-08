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

show rel;

REM xyz
REM abc-
def
REM abc-;

show user

create or replace procedure selectdata is
  v_number number;
  begin
    select 1 into v_number from dual;
    dbms_output.put_line('var>'||v_number);
  end;
/

PROMPT ready-
to-
execute


create or replace procedure update_test
begin
  update test set id=1;
end;
/

sho err

WHENEVER SQLERROR CONTINUE;

very_bad_sql;

WHENEVER SQLERROR EXIT FAILURE

PROMPT 3..2..1..go-;
sho err procedure abc

SET SERVEROUTPUT ON

DECLARE
  lines dbms_output.chararr;
  num_lines number;
BEGIN
  -- enable the buffer with default size 20000
  dbms_output.put_line('Hello Reader!');
  dbms_output.put_line('Hope you have enjoyed the tutorials!');
  dbms_output.put_line('Have a great time exploring pl/sql!');

  num_lines := 3;

  dbms_output.get_lines(lines, num_lines);

  FOR i IN 1..num_lines LOOP
    dbms_output.put_line(lines(i));
  END LOOP;
END;
/

SET SERVEROUT OFF

BEGIN
  dbms_output.put_line('Hello Reader!');
  dbms_output.put_line('Hope you have enjoyed the tutorials!');
  dbms_output.put_line('Have a great time exploring pl/sql!');
END;
/

EXEC selectdata();