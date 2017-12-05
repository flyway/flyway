--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

declare
  l_prefix VARCHAR2(131) := '"' || SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') || '".';
begin
  DBMS_SCHEDULER.CREATE_FILE_WATCHER (
    file_watcher_name => l_prefix || 'test_file_watcher',
    directory_path    => '?/rdbms/log',
    file_name         => '*.log',
    credential_name   => l_prefix || 'TEST_CREDENTIAL');
end;
/