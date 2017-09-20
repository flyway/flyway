#!/bin/bash
/opt/mssql/bin/sqlservr &
echo "Waiting for DB to boot..."
for i in {1..20}; do /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P flywayPWD000 -i /createDatabase.sql && break || (echo "DB not up yet ..." && sleep 15); done

while true; do sleep 1000; done