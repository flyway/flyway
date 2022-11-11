---
layout: documentation
menu: tut_baselinemigrations
subtitle: 'Tutorial: Baseline Migrations'
redirect_from: /documentation/tutorials/stateScripts
---
# Tutorial: Baseline Migrations
{% include teams.html %}

This brief tutorial will teach you **how to use baseline migrations**.

## Introduction

Over the lifetime of a project, many database objects may be created and destroyed across many migrations which leaves behind a lengthy history of migrations that need to be applied in order to bring a new environment up to speed.

Instead, you might wish to add a single, cumulative migration that represents the state of your database after all of those migrations have been applied without disrupting existing environments.

[Baseline migrations](/documentation/concepts/statescripts) let you achieve just that. These are a new type of migration, similar to [versioned migrations](/documentation/concepts/migrations#versioned-migrations) except with `B` as their prefix.

In existing deployments they have no effect as your database is already where it needs to be. In new environments, the baseline migration with the latest version is applied first in order to bring your database up to speed before applying later migrations. Any migrations with a version older than the latest baseline migration's version are not applied and are treated as being [ignored](/documentation/concepts/migrations#migration-states). <br/>
Note that repeatable migrations are executed as normal.

## Example

Let’s say we have the following 3 migrations:

```
V1__create_two_tables.sql
V2__drop_one_table.sql
V3__alter_column.sql
```

If we execute these migrations in order we will have gone through the process of creating two tables, dropping one of them and finally altering a column in the remaining table.

Instead of going through this process for every one of our environments, we might decide that it is easier to create the final table as it would be at the end of applying these 3 migrations in order to simplify the SQL we execute as well as our migration history.

To achieve this, we need only create the following migration:

```
B3__create_table.sql
```

This should contain the SQL that represents our environment after the original 3 migrations are applied. After adding this migration to our existing environment, we will notice no difference as shown in the below output after running `flyway info`:

```
+-----------+---------+-------------------+------+---------------------+---------+----------+
| Category  | Version | Description       | Type | Installed On        | State   | Undoable |
+-----------+---------+-------------------+------+---------------------+---------+----------+
| Versioned | 1       | create two tables | SQL  |         ...         | Success | No       |
| Versioned | 2       | drop one table    | SQL  |         ...         | Success | No       |
| Versioned | 3       | alter column      | SQL  |         ...         | Success | No       |
+-----------+---------+-------------------+------+---------------------+---------+----------+
```

However, when we come to apply our migrations in a new environment, `flyway info` will show the following output:

```
+-----------+---------+--------------+------------------+--------------+---------+----------+
| Category  | Version | Description  | Type             | Installed On | State   | Undoable |
+-----------+---------+--------------+------------------+--------------+---------+----------+
| Versioned | 3       | create table |   SQL_BASELINE   |              | Pending | No       |
+-----------+---------+--------------+------------------+--------------+---------+----------+
```

Migrations with a version less than or equal to the latest baseline migration's version are ignored. Running `flyway migrate` will cause just the `B3` script to be applied, and the history table will show this as a result:

```
+-----------+---------+--------------+------------------+---------------------+----------+----------+
| Category  | Version | Description  | Type             | Installed On        | State    | Undoable |
+-----------+---------+--------------+------------------+---------------------+----------+----------+
| Versioned | 3       | create table |   SQL_BASELINE   |         ...         | Baseline | No       |
+-----------+---------+--------------+------------------+---------------------+----------+----------+
```

## Summary

In this brief tutorial we saw how to:

- Use baseline migrations to signal a new baseline in new environments
