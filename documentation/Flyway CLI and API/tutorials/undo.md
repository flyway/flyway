---
layout: documentation
menu: undo
subtitle: 'Tutorial: Undo Migrations'
redirect_from:
- /getStarted/undo/
- /documentation/getstarted/undo/
- /documentation/getstarted/advanced/undo/
---
# Tutorial: Undo Migrations
{% include teams.html %}

This tutorial assumes you have successfully completed the [**First Steps: Command-line**](/documentation/getstarted/firststeps/commandline)
tutorial **If you have not done so, please do so first.** This tutorial picks up where that one left off.

This brief tutorial will teach **how to use undo migrations**. It will take you through the
steps on how to create and use them.

## Introduction

**Undo migrations** are the opposite of regular versioned migrations. An undo migration is responsible for undoing the effects
of the versioned migration with the same version. Undo migrations are optional and not required to run regular versioned migrations.

## Reviewing the status

After having completed the [First Steps: Command-line](/documentation/getstarted/firststeps/commandline), you can now execute

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong></pre>

This should give you the following status:

<pre class="console">Database: jdbc:h2:file:./foobardb (H2 1.4)

+-----------+---------+---------------------+------+---------------------+---------+----------+
| Category  | Version | Description         | Type | Installed On        | State   | Undoable |
+-----------+---------+---------------------+------+---------------------+---------+----------+
| Versioned | 1       | Create person table | SQL  | 2017-12-17 19:57:28 | Success | No       |
| Versioned | 2       | Add people          | SQL  | 2017-12-17 20:01:13 | Success | No       |
+-----------+---------+---------------------+------+---------------------+---------+----------+</pre>

## Creating the undo migrations

Now let's create undo migrations for these two applied versioned migrations. With Flyway's default naming convention,
the filenames will be identical to the regular migrations, except for the `V` prefix which is now replaced with a `U`.

So go ahead and create `U2__Add_people.sql` in the `/sql` directory:

```sql
DELETE FROM PERSON;
```

And add a `U1__Create_person_table.sql` as well:

```sql
DROP TABLE PERSON;
```

This is now the status

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong>

Database: Database: jdbc:h2:file:./foobardb (H2 1.4)

+-----------+---------+---------------------+------+---------------------+---------+----------+
| Category  | Version | Description         | Type | Installed On        | State   | Undoable |
+-----------+---------+---------------------+------+---------------------+---------+----------+
| Versioned | 1       | Create person table | SQL  | 2017-12-17 19:57:28 | Success | Yes      |
| Versioned | 2       | Add people          | SQL  | 2017-12-17 20:01:13 | Success | Yes      |
+-----------+---------+---------------------+------+---------------------+---------+----------+</pre>

Note that both migrations have now been marked as being *undoable*.

## Undoing the last migration

By default, **undo** undoes the last applied versioned migration.

So go ahead and invoke

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>undo</strong></pre>

This will give you the following result:

<pre class="console">Database: Database: jdbc:h2:file:./foobardb (H2 1.4)
Current version of schema "PUBLIC": 2
Undoing migration of schema "PUBLIC" to version 2 - Add people
Successfully undid 1 migration to schema "PUBLIC" (execution time 00:00.030s)</pre>

And you can check that this is indeed the new status:

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong>

Database: Database: jdbc:h2:file:./foobardb (H2 1.4)

+-----------+---------+---------------------+----------+---------------------+---------+----------+
| Category  | Version | Description         | Type     | Installed On        | State   | Undoable |
+-----------+---------+---------------------+----------+---------------------+---------+----------+
| Versioned | 1       | Create person table | SQL      | 2017-12-17 19:57:28 | Success | Yes      |
| Versioned | 2       | Add people          | SQL      | 2017-12-17 20:01:13 | Undone  |          |
| Undo      | 2       | Add people          | UNDO_SQL | 2017-12-17 22:45:56 | Success |          |
| Versioned | 2       | Add people          | SQL      |                     | Pending | Yes      |
+-----------+---------+---------------------+----------+---------------------+---------+----------+</pre>

Our audit trail now clearly shows that version 2 was first applied, then undone and is now pending again.

We can now safely reapply it with

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>migrate</strong>

Database: Database: jdbc:h2:file:./foobardb (H2 1.4)
Successfully validated 5 migrations (execution time 00:00.020s)
Current version of schema "PUBLIC": 1
Migrating schema "PUBLIC" to version 2 - Add people
Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.017s)</pre>

And the status is now

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong>

Database: Database: jdbc:h2:file:./foobardb (H2 1.4)

+-----------+---------+---------------------+----------+---------------------+---------+----------+
| Category  | Version | Description         | Type     | Installed On        | State   | Undoable |
+-----------+---------+---------------------+----------+---------------------+---------+----------+
| Versioned | 1       | Create person table | SQL      | 2017-12-17 19:57:28 | Success | Yes      |
| Versioned | 2       | Add people          | SQL      | 2017-12-17 20:01:13 | Undone  |          |
| Undo      | 2       | Add people          | UNDO_SQL | 2017-12-17 22:45:56 | Success |          |
| Versioned | 2       | Add people          | SQL      | 2017-12-17 22:50:49 | Success | Yes      |
+-----------+---------+---------------------+----------+---------------------+---------+----------+</pre>

## Summary

In this brief tutorial we saw how to
- create undo migrations
- undo and redo existing migrations

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/concepts/migrations#undo-migrations">Read the undo migration documentation <i class="fa fa-arrow-right"></i></a>
</p>
