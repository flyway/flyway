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
  dbms_scheduler.create_job(
    job_name      => l_prefix || 'TEST_JOB',
    program_name  => l_prefix || 'TEST_PROGRAM',
    schedule_name => l_prefix || 'TEST_SCHEDULE');
end;
/

