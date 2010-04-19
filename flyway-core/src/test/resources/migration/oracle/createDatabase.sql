CREATE USER test_ddl_user IDENTIFIED BY ddlPassword;
GRANT all privileges TO test_ddl_user;
GRANT create session TO test_ddl_user;