# flyway-play-plugin

Flyway plugin for Play 2.1. It aims to be a substitute for play-evolutions.

## Usage

### Dev

Almost the same as play-evolutions.

Place your migration scripts in config/db/migration/${dbName} .


![screenshot](/screenshot1.png)


### Test

In Test, migration is done automatically.



### Prod

In production mode, migration is done automatically if db.${dbName}.migration.auto is set to be true in application.conf.
Otherwise it failed to start when migration is needed.

