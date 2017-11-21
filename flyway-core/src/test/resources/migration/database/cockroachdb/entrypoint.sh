#!/bin/bash
/cockroach/cockroach start --insecure --background
/cockroach/cockroach sql --insecure < /createDatabase.sql

while true; do sleep 1000; done