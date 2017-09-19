#!/bin/bash
/opt/mssql/bin/sqlservr &
echo "Waiting for DB to boot..."
sleep 120
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P flywayPWD000 -i /createDatabase.sql

while true; do sleep 1000; done