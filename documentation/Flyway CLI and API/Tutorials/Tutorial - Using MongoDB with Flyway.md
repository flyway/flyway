---
subtitle: 'Tutorial: Using MongoDB with Flyway'
---
# Tutorial: Using MongoDB with Flyway

This tutorial assumes you have successfully completed the **{% include quickstart-cli.html %}**
tutorial. **If you have not done so, please do so first.** This tutorial picks up where that one left off.

This brief tutorial will teach you the subtle differences when using Flyway with MongoDB.

## Introduction

**MongoDB** differs from traditional relational databases in that it is a document database. It stores data in JSON-like 
format and does not support SQL. As such, MongoDB doesn't have the concept of a `schema`. Having Flyway manage your MongoDB
instance still has some fantastic benefits, such as:

- Migrating static data
- Maintaining views
- User management

Flyway supports MongoDB by allowing you to execute JavaScript files containing MongoDB commands.
All core Flyway functionality is enabled for MongoDB, such as [info](Commands/Info), [migrate](Commands/Migrate) and [undo](Commands/Undo).

## Prerequisites

Before we can get started, please make sure you have the following in place:

- A MongoDB instance running on `localhost:27017` (Using [docker](https://hub.docker.com/_/mongo/) is a great way to get started)
- A user with the `root` role on the `admin` database
- Port 27017 is publicly exposed from within the docker container 

You can verify that this is working using the MongoDB Compass tool to connect to your database.

## Connecting to MongoDB

Connecting to MongoDB is done the same as any other database. 
You can use the [url](Configuration/Parameters/Environments/URL) configuration property to specify the connection string,
either as part of an environment configuration or as standalone, also see [MongoDB support](Supported Databases/MongoDB):

In this tutorial we'll be setting things up in the TOML configuration file 
```toml
[environments.mongodb]
url = "jdbc:mongodb://localhost:27017/"
user = "your username"
password = "your password"

[flyway]
environment = "mongodb"
```
Note: _MongoDB defaults to the `test` database if you don't specify one in the url_
## Handling scripts

Unlike other databases that Flyway supports, MongoDB scripts are JavaScript files containing MongoDB commands rather than
SQL files. However, by default, Flyway will look for SQL files. 
To tell Flyway to look for JavaScript files instead, you can configure [SqlMigrationSuffix](Configuration/Parameters/Flyway/SQL Migration Suffixes) to be `.js`:

```toml
[flyway]
sqlMigrationSuffixes = [".js"]
```

This is not required as Flyway will happily load Mongo javascript from within files with a `.sql` suffix but making this
change allows you to Mongo native `.js` files and gain all the benefits this entails.

## Adding a new migration

Let's add a new migration to make sure everything is working as expected.

In your migrations directory (either use a `sql` folder within the Flyway folder or configure [locations](Configuration/Parameters/Flyway/Locations/) 
to specify your own location) create a migration called `V1__mongo-migration.js`:

```javascript
db.user.insert({name: "Ada Lovelace", age: 205});
```

Now run the following command:

```bash
./flyway migrate info
```

After the successful migration, you should see the following at the end of the output:

<pre class="console">
Schema version: 1

+-----------+---------+---------------------+------+---------------------+---------+----------+
| Category  | Version | Description         | Type | Installed On        | State   | Undoable |
+-----------+---------+---------------------+------+---------------------+---------+----------+
| Versioned | 1       | mongo-migration     | SQL  | 2017-12-22 15:26:39 | Success | No       |
+-----------+---------+---------------------+------+---------------------+---------+----------+</pre>

You should also notice a new record (`Ada Lovelace`) within a new collection (`user`) within your Mongo tooling.
As Mongo is a document database, there is no requirement to create a schema before inserting data as it will 
create collections ad-hoc.

## Summary

In this brief tutorial we saw how to
- configure Flyway to use MongoDB
- test the configuration by deploying a migration.
