#!/bin/bash

# Before calling this script you must configure MonetDB (vide: https://www.monetdb.org/Documentation/UserGuide/Tutorial) and create moentdb test database:
#
# monetdb create flywaydb
# monetdb release flywaydb
#
# default admin user: monetdb
# his password: monetdb

echo "MonetDB"

sudo monetdb create flywaydb
sudo monetdb release flywaydb

mclient -u monetdb -d flywaydb flyway-core/src/test/resources/migration/dbsupport/monetdb/createDatabase.sql