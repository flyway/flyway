@REM
@REM Copyright 2010-2017 Boxfuse GmbH
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM         http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@Echo off

setlocal

echo Dropping DBs...

echo Oracle...
sqlplus SYSTEM/flyway@XE < flyway-core/src/test/resources/migration/dbsupport/oracle/dropDatabase.sql

echo MySQL...
mysql -uroot -pflyway < flyway-core/src/test/resources/migration/dbsupport/mysql/dropDatabase.sql

echo MariaDB...
mysql -uroot -pflyway -P3333 < flyway-core/src/test/resources/migration/dbsupport/mysql/dropDatabase.sql

echo EnterpriseDB...
set PGPASSWORD=flyway
edb-psql -Uenterprisedb < flyway-core/src/test/resources/migration/dbsupport/edb/dropDatabase.sql

echo PostgreSQL...
set PGPASSWORD=flyway
psql -Upostgres < flyway-core/src/test/resources/migration/dbsupport/postgresql/dropDatabase.sql

echo SQL Server...
sqlcmd -U sa -P flyway -S localhost\SQLExpress -i flyway-core\src\test\resources\migration\dbsupport\sqlserver\dropDatabase.sql

echo DB2...
db2cmd -c "db2 -tvf flyway-core/src/test/resources/migration/dbsupport/db2/dropDatabase.sql"

echo Done.
