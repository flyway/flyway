@REM
@REM Copyright 2010-2017 Boxfuse GmbH
@REM
@REM INTERNAL RELEASE. ALL RIGHTS RESERVED.
@REM

@Echo off

setlocal

echo Creating DBs...

echo MySQL...
mysql -uroot -pflyway < flyway-core/src/test/resources/migration/database/mysql/createDatabase.sql

echo MariaDB...
mysql -uroot -pflyway -P3333 < flyway-core/src/test/resources/migration/database/mysql/createDatabase.sql

echo EnterpriseDB...
set PGPASSWORD=flyway
edb-psql -Uenterprisedb < flyway-core/src/test/resources/migration/database/postgresql/createDatabase.sql

echo PostgreSQL
set PGPASSWORD=flyway
psql -Upostgres < flyway-core/src/test/resources/migration/database/postgresql/createDatabase.sql

echo SolidDB...
solsql -f flyway-core/src/test/resources/migration/database/solid/createDatabase.sql "tcp localhost 1313"

echo CockroackDB
cockroach sql --insecure < ./flyway-core/src/test/resources/migration/database/cockroachdb/createDatabase.sql

echo Done.
