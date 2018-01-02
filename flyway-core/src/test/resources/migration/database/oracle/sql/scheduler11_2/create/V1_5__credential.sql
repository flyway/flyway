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
  l_prefix VARCHAR2(131) := '"' || SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') || '".';
begin
  dbms_scheduler.create_credential(
    credential_name => l_prefix || 'TEST_CREDENTIAL',
    username        => 'test_user',
    password        => 't3$t_p@$$w0rd');
end;
/
