#!/bin/bash
set -e
echo -e "$DB2INST1_PASSWORD\n$DB2INST1_PASSWORD" | passwd db2inst1
su - db2inst1 -c "db2start"
su - db2inst1 -c "db2 -tvf /createDatabase.sql"
nohup /usr/sbin/sshd -D 2>&1 > /dev/null &
while true; do sleep 1000; done