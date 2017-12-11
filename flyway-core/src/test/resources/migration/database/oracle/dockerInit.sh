#!/bin/sh
#
# $Header: dbaas/docker/build/dbsetup/setup/dockerInit.sh rduraisa_docker_122_image/2 2017/03/02 13:26:07 rduraisa Exp $
#
# dockerInit.sh
#
# Copyright (c) 2016, 2017, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      dockerInit.sh - docker container initiation file
#
#    DESCRIPTION
#      init script to be run everytime docker container is started
#
#    NOTES
#      run as root
#
#    MODIFIED   (MM/DD/YY)
#    rduraisa    03/02/17 - Modify scripts to build for 12102 and 12201
#    xihzhang    10/25/16 - Remove EE bundles
#    xihzhang    09/14/16 - Use ENTRYPOINT
#    xihzhang    09/06/16 - Optimize build
#    xihzhang    08/08/16 - Remove privilege mode
#    xihzhang    05/23/16 - Creation
#

# basic parameters
LOG_DIR=/home/oracle/setup/log
SETUP_DIR=/home/oracle/setup

if [ ! -d $LOG_DIR ]
then
    mkdir $LOG_DIR
    chmod 775 $LOG_DIR
    chown oracle:oinstall $LOG_DIR
fi

# logfile
INIT_LOG=$LOG_DIR/dockerInit.log
echo `date` >> $INIT_LOG

# check setup path
if [ ! -d $SETUP_DIR ]
then
    echo "ERROR : setup files are not found"
    echo "ERROR : setup files are not found" >> $INIT_LOG
    echo "" >> $INIT_LOG
    exit 1
fi

# check whether it is the first time this container is up
# if it is the first time, setup the DB
# if not, startup the existing db
if [ -f /home/oracle/setup/log/setupDB.log ]
then
    echo "Start up Oracle Database"
    echo "Start up Oracle Database" >> $INIT_LOG
    /bin/bash $SETUP_DIR/startupDB.sh 2>&1
else
    echo "Setup Oracle Database"
    echo "Setup Oracle Database" >> $INIT_LOG
    /bin/bash $SETUP_DIR/setupDB.sh 2>&1
    echo "Setup Flyway Database"
    echo "Setup Flyway Database" >> $INIT_LOG
    bash -c "source /home/oracle/.bashrc; sqlplus 'sys/Oradoc_db1 as sysdba'" < /createDatabase.sql
fi

echo "" >> $INIT_LOG

# remove passwd param
unset DB_PASSWD

# basic parameters
BASH_RC=/home/oracle/.bashrc
source ${BASH_RC}

# Define SIGTERM-handler for graceful shutdown
term_handler() {
  if [ $childPID -ne 0 ]; then
    /bin/bash $SETUP_DIR/shutDB.sh
  fi
  exit 143; # 128 + 15 -- SIGTERM
}
# setup SIGTERM Handler
trap 'kill ${!}; term_handler' SIGTERM

# keep container runing
ALERT_LOG=/u01/app/oracle/diag/rdbms/${ORACLE_SID,,}/${ORACLE_SID}/trace/alert_${ORACLE_SID}.log
tail -f $ALERT_LOG &
childPID=$!
wait $childPID

# end
