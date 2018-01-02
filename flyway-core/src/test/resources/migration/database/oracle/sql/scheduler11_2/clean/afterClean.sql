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

declare
  l_owner varchar2(128) := SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA');
begin
  for r in (select * from all_scheduler_credentials where owner = l_owner) loop
    dbms_scheduler.drop_credential(
      credential_name => '"' || l_owner || '"."' || r.credential_name || '"'
    );
  end loop;
end;
/

