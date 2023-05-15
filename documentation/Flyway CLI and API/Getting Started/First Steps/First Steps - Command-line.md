---
subtitle: Flyway Command-line - First Steps
redirect_from: /Getting Started/First Steps/First Steps - Command-line.md/
---

# First Steps: Command-line

This brief tutorial will teach **how to get up and running with the Flyway Command-line through Docker**. It will take you through the
steps on how to configure it and how to write and execute your first few database migrations.

This tutorial should take you about **5 minutes** to complete.

## Prerequisites

Start by downloading Flyway Teams and Enterprise Docker image to your Docker environment. 

```
docker pull redgate/flyway
```

## Configuring Flyway

The easiest way to get started is simply to test the default image by running

```
docker run --rm redgate/flyway
```

This will give you Flyway Command-line's usage instructions.

Next, configure Flyway by creating a file named `flyway.conf`, like this:

```properties
flyway.url=jdbc:sqlite:/flyway/db/test_db.sqlite3
flyway.user=sa
```

## Creating the first migration

Now create your first migration sql called `V1__Create_person_table.sql`:

```sql
create table PERSON (
    ID int not null,
    NAME varchar(100) not null
);
```

## Migrating the database

It's now time to execute Flyway to migrate your database.

- Replace {absolute path to folder to store SQLite db file} with the path to where you would like to store your SQLite db file.

- Replace {absolute path to folder containing sql migrations} with the path to your sql file directory.

- Replace {absolute path to folder containing conf file} with the path to your conf file directory.

```
docker run --rm -v "{absolute path to folder to store SQLite db file}:/flyway/db" -v "{absolute path to folder containing sql migrations}:/flyway/sql" -v "{absolute path to folder containing conf file}:/flyway/conf" redgate/flyway migrate
```

If all went well, you should see the following output:

<pre class="console">Database: jdbc:sqlite:/flyway/db/test_db.sqlite3 (SQLite 3.34)
Schema history table "main"."flyway_schema_history" does not exist yet
Successfully validated 1 migration (execution time 00:00.009s)
Creating Schema History table "main"."flyway_schema_history" ...
Current version of schema "main": << Empty Schema >>
Migrating schema "main" to version "1 - Create person table"
Successfully applied 1 migration to schema "main", now at version v1 (execution time 00:00.034s)</pre>

## Adding a second migration

If you now add a second migration to your sql directory called `V2__Add_people.sql`:

```sql
insert into PERSON (ID, NAME) values (1, 'Axel');
insert into PERSON (ID, NAME) values (2, 'Mr. Foo');
insert into PERSON (ID, NAME) values (3, 'Ms. Bar');
```

and execute it by issuing:

```
docker run --rm -v "{absolute path to folder to store SQLite db file}:/flyway/db" -v "{absolute path to folder containing sql migrations}:/flyway/sql" -v "{absolute path to folder containing conf file}:/flyway/conf" redgate/flyway migrate
```

You now get:

<pre class="console">Database: jdbc:sqlite:/flyway/db/test_db.sqlite3 (SQLite 3.34)
Successfully validated 2 migrations (execution time 00:00.015s)
Current version of schema "main": 1
Migrating schema "main" to version "2 - Add people"
Successfully applied 1 migration to schema "main", now at version v2 (execution time 00:00.029s)</pre>

## Verification

You can use `info` command to check what Flyway did.  

```
docker run --rm -v "{absolute path to folder to store SQLite db file}:/flyway/db" -v "{absolute path to folder containing sql migrations}:/flyway/sql" -v "{absolute path to folder containing conf file}:/flyway/conf" redgate/flyway info
```

This will give output as below: 

<pre class="console">

Database: jdbc:sqlite:/flyway/db/test_db.sqlite3 (SQLite 3.34)
Schema version: 2

+-----------+---------+---------------------+------+---------------------+---------+----------+
| Category  | Version | Description         | Type | Installed On        | State   | Undoable |
+-----------+---------+---------------------+------+---------------------+---------+----------+
| Versioned | 1       | Create person table | SQL  | 2023-04-26 13:01:02 | Success | No       |
| Versioned | 2       | Add people          | SQL  | 2023-04-26 13:05:04 | Success | No       |
+-----------+---------+---------------------+------+---------------------+---------+----------+

</pre>

If you run the `migrate` command again, Flyway will give below output: 

<pre class="console">Database: jdbc:sqlite:/flyway/db/test_db.sqlite3 (SQLite 3.34)
Successfully validated 2 migrations (execution time 00:00.016s)
Current version of schema "main": 2
Schema "main" is up to date. No migration necessary.</pre>

## Summary

In this brief tutorial we saw how to:
- set up the Flyway Command-line environment through Docker
- configure it so it can talk to your local database
- write our first couple of migrations

These migrations were then successfully found and executed.
