@REM
@REM Copyright 2010-2017 Boxfuse GmbH
@REM
@REM INTERNAL RELEASE. ALL RIGHTS RESERVED.
@REM
@REM Must
@REM be
@REM exactly
@REM 13 lines
@REM to match
@REM community
@REM edition
@REM license
@REM length.
@REM

@Echo off

setlocal

echo Dropping DBs...

echo MySQL...
mysql -uroot -pflyway < flyway-core/src/test/resources/migration/database/mysql/dropDatabase.sql

echo MariaDB...
mysql -uroot -pflyway -P3333 < flyway-core/src/test/resources/migration/database/mysql/dropDatabase.sql

echo EnterpriseDB...
set PGPASSWORD=flyway
edb-psql -Uenterprisedb < flyway-core/src/test/resources/migration/database/edb/dropDatabase.sql

echo PostgreSQL...
set PGPASSWORD=flyway
psql -Upostgres < flyway-core/src/test/resources/migration/database/postgresql/dropDatabase.sql

echo CockroachDB...
cockroach sql --insecure < ./flyway-core/src/test/resources/migration/database/cockroachdb/dropDatabase.sql

echo Done.
