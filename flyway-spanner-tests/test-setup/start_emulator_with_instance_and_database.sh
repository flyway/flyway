#!/bin/bash

set -e
# turn on bash's job control
set -m

# Start the emulator.
printf "\nStarting Emulator..."
./gateway_main --hostname 0.0.0.0 &
sleep 5

# Preconfigured settings
ENDPOINT=http://localhost:9020
PROJECT_ID=test-project
INSTANCE_ID=test-instance
DATABASE_ID=test_database

# Create a Cloud Spanner Instance.
printf "\n\nCreating an instance..."
curl --request POST \
   -L "$ENDPOINT/v1/projects/$PROJECT_ID/instances" \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/json' \
    --data "{\"instance\":{\"config\":\"emulator-config\",\"nodeCount\":1,\"displayName\":\"Test Instance\"},\"instanceId\":\"$INSTANCE_ID\"}"

# Create a Cloud Spanner Database
printf "\n\nCreating a database..."
curl --request POST \
  -L "$ENDPOINT/v1/projects/$PROJECT_ID/instances/$INSTANCE_ID/databases" \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --data "{\"createStatement\":\"CREATE DATABASE $DATABASE_ID\",\"extraStatements\":[\"CREATE TABLE mytable (a INT64, b INT64) PRIMARY KEY(a)\"]}"

printf "\n\nCompleted Initialization.. \n"

# Docker will exit unless a foreground process is running.
# https://docs.docker.com/config/containers/multi-service_container/
fg %1