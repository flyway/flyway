begin
  dbms_scheduler.create_job(
    job_name    => 'test_job',
    job_type    => 'PLSQL_BLOCK',
    job_action  => 'select 1 from dual');
end;
/

