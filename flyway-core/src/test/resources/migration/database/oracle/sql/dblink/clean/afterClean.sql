--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

begin
  for r in (select * from user_db_links) loop
    execute immediate 'DROP DATABASE LINK ' || r.db_link;
  end loop;
end;
/

