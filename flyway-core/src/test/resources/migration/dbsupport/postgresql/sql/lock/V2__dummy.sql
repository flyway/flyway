/*
  We need this dummy migration for the test because it ensures that table SCHEMA_VERSION has been created and committed
  when the lock test is done in next migration file
*/
SELECT 1;
