---
subtitle: 'Tutorial: Using Pingu to connect to MongoDB'
---
# Tutorial: Using Native Connectors to connect to MongoDB

This tutorial shows you how to connect to MongoDB using Flyway with Native Connectors enabled.
Native Connectors is a new engine designed to handle migrations without relying on the JDBC framework.

## Prerequisites

Before we can get started, please make sure you have the following in place:

- Flyway v11.0.x or later
- A MongoDB instance running on `localhost:27017` (Using [docker](https://hub.docker.com/_/mongo/) is a great way to get started)
- A user with the `root` role on the `admin` database
- Port 27017 is publicly exposed from within the docker container
- If you are using javascript migrations then you'll need [`mongosh`](https://www.mongodb.com/docs/mongodb-shell/install/) to be installed

You can verify that this is working using the MongoDB Compass tool to connect to your database.

## Enabling Native Connectors

If you are using the OSS edition of Flyway, then Native Connectors is already enabled. If you are using a Redgate edition,
then you will need to set an environment variable to enable Native Connectors:

### Environment variable

```properties
FLYWAY_NATIVE_CONNECTORS=true
```

Note: _Setting this environment variable to `false` will disable Native Connectors in Flyway._

## Connecting to MongoDB

Connecting to MongoDB is done the same as without Native Connectors enabled. 

In this tutorial we'll be setting things up in the TOML configuration file:
```toml
[environments.mongodb]
url = "mongodb://localhost:27017/"
user = "your username"
password = "your password"

[flyway]
environment = "mongodb"
```
Note: _MongoDB defaults to the `test` database if you don't specify one in the url._

Note: _Native Connectors supports the url prefix `jdbc:mongodb` for backwards compatibility but `mongodb:` is the planned protocol descriptor._

You should now be able to run `flyway info -environment=mongodb` to verify that Flyway can connect to your MongoDB instance.

## Migration files

Native Connectors enables two different types of migration file: JSON and JavaScript. The JSON files are executed using a
native MongoDB API, while the JavaScript files are executed using the MongoDB shell on your computer.

Note: _You can check if your MongoDB shell is installed by running `mongosh --version` in your terminal. If it isn't, the [install guide is here](https://www.mongodb.com/docs/mongodb-shell/install/)._

Here is an insert statement in each format:

### JSON
```json
{
  "insert": "user",
  "documents": [ {"name":  "Ada Lovelace", "age":  205} ]
}
```

### Javascript
```javascript
db.user.insert({name: "Ada Lovelace", age: 205});
```

You will need to decide which format is best for you in your environment.
In order to configure Flyway to look for MongoDB migration files, you will need to set the following configuration:

### JSON
```toml
[flyway]
sqlMigrationSuffixes = [".json"]
```

### Javascript
```toml
[flyway]
sqlMigrationSuffixes = [".js"]
```

Once you have your migration file and have configured Flyway for the appropriate file type, you can proceed

In this tutorial we are creating a versioned migration called `V1__my_mongodb_migration.js` or `V1__my_mongodb_migration.json` in the `sql\` folder of your flyway installation.

## Migrating

Now run the following command:

```bash
./flyway migrate info -environment=mongodb
```

You should see output similar to the following:
```
Flyway OSS Edition 11.1.0 by Redgate

See release notes here: https://rd.gt/416ObMi
-----------------------------------------------------------------------------
You are using a preview feature 'ExperimentalMigrate'.
Please report any issues you encounter to DatabaseDevOps@red-gate.com
-----------------------------------------------------------------------------
Database: <<details removed>> (MongoDB)
Schema history table "test"."flyway_schema_history" does not exist yet
Successfully validated 1 migration (execution time 00:00.934s)
Creating Schema History table "test"."flyway_schema_history" ...
Current version of schema "test": << Empty Schema >>
Migrating schema "test" to version "1 - my mongodb migration" [non-transactional]
Successfully applied 1 migration to schema "test", now at version v1 (execution time 00:01.231s)
-----------------------------------------------------------------------------
You are using a preview feature 'ExperimentalInfo'.
Please report any issues you encounter to DatabaseDevOps@red-gate.com
-----------------------------------------------------------------------------
Schema version: 1

+-----------+---------+------------------------------+--------+---------------------+---------+----------+
| Category  | Version | Description                  | Type   | Installed On        | State   | Undoable |
+-----------+---------+------------------------------+--------+---------------------+---------+----------+
|           |         | << Flyway Schema Creation >> | SCHEMA | 2024-12-12 10:33:15 | Success |          |
| Versioned | 1       | my mongodb migration         | SQL    | 2024-12-12 10:33:16 | Success | No       |
+-----------+---------+------------------------------+--------+---------------------+---------+----------+
```


You should also notice a new record (Ada Lovelace) within a new collection (user) within your Mongo tooling. As Mongo is a document database, there is no requirement to create a schema before inserting data as it will create collections ad-hoc.