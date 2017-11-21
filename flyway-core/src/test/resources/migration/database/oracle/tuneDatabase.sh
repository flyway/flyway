#!/usr/bin/env bash
# increase the number of connections
echo "alter system set processes=250 scope=spfile;" | sqlplus -s SYSTEM/oracle
echo "alter system reset sessions scope=spfile sid='*';" | sqlplus -s SYSTEM/oracle
service oracle-xe restart
echo "alter system disable restricted session;" | sqlplus -s SYSTEM/oracle
echo "show parameter sessions;" | sqlplus -s SYSTEM/oracle
echo "show parameter processes;" | sqlplus -s SYSTEM/oracle