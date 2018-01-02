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
  dbms_scheduler.create_chain(
    chain_name => l_prefix || 'TEST_CHAIN');
  dbms_scheduler.define_chain_step(
    chain_name   => l_prefix || 'TEST_CHAIN',
    step_name    => 'TEST_STEP_1',
    program_name => l_prefix || 'TEST_PROGRAM');
end;
/

