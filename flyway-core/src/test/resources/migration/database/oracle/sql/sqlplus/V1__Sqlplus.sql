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

EXEC selectdata();