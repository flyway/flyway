CREATE DATABASE migration_test_db DEFAULT CHARACTER SET 'utf8' DEFAULT COLLATE 'utf8_bin';
CREATE USER 'test_ddl_user'@'localhost' IDENTIFIED BY 'ddlPassword';
CREATE USER 'test_dml_user'@'localhost' IDENTIFIED BY 'dmlPassword';
GRANT all ON migration_test_db.* TO 'test_ddl_user'@'localhost' IDENTIFIED BY 'ddlPassword';
GRANT select,insert,update,delete ON migration_test_db.* TO 'test_dml_user'@'localhost' IDENTIFIED BY 'dmlPassword';
FLUSH PRIVILEGES;