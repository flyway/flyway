---
layout: documentation
menu: repeatable
subtitle: 'Tutorial: Repeatable Migrations'
redirect_from:
- /getStarted/repeatable/
- /documentation/getstarted/repeatable/
- /documentation/getstarted/advanced/repeatable/
---
# Tutorial: Repeatable Migrations

This tutorial assumes you have successfully completed the [**First Steps: Command-line**](/documentation/getstarted/firststeps/commandline)
tutorial. **If you have not done so, please do so first.** This tutorial picks up where that one left off.

This brief tutorial will teach **how to use repeatable migrations**. It will take you through the
steps on how to create and use them.

## Introduction

**Repeatable migrations** are very useful for managing database objects whose definition can then simply be maintained
in a single file in version control. Instead of being run just once, they are (re-)applied every time their checksum changes.
 
They are typically used for
- (Re-)creating views/procedures/functions/packages/…
- Bulk reference data reinserts

## Reviewing the status

After having completed the [First Steps: Command-line](/documentation/getstarted/firststeps/commandline), you can now execute

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong></pre>

This should give you the following status:

<pre class="console">Database: jdbc:h2:file:./foobardb (H2 1.4)
                     
+-----------+---------+---------------------+------+---------------------+---------+----------+
| Category  | Version | Description         | Type | Installed On        | State   | Undoable |
+-----------+---------+---------------------+------+---------------------+---------+----------+
| Versioned | 1       | Create person table | SQL  | 2017-12-21 18:05:10 | Success | No       |
| Versioned | 2       | Add people          | SQL  | 2017-12-21 18:05:10 | Success | No       |
+-----------+---------+---------------------+------+---------------------+---------+----------+</pre>

## Creating a repeatable migration

Now let's create a repeatable migration to manage a view of the person table. With Flyway's default naming convention,
the filename will be similar to the regular migrations, except for the `V` prefix which is now replaced with a `R` and
the lack of a version.

So go ahead and create `R__People_view.sql` in the `/sql` directory:

```sql
CREATE OR REPLACE VIEW people AS 
    SELECT id, name FROM person;
```

This is now the status

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong>

Database: jdbc:h2:file:./foobardb (H2 1.4)
                     
+------------+---------+---------------------+------+---------------------+---------+----------+
| Category   | Version | Description         | Type | Installed On        | State   | Undoable |
+------------+---------+---------------------+------+---------------------+---------+----------+
| Versioned  | 1       | Create person table | SQL  | 2017-12-21 18:05:10 | Success | No       |
| Versioned  | 2       | Add people          | SQL  | 2017-12-21 18:05:10 | Success | No       |
| Repeatable |         | People view         | SQL  |                     | Pending |          |
+------------+---------+---------------------+------+---------------------+---------+----------+</pre>

Note the new pending repeatable migration.

## Executing the migration

It's time to execute our new migration.

So go ahead and invoke

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>migrate</strong></pre>

This will give you the following result:

<pre class="console">Database: jdbc:h2:file:./foobardb (H2 1.4)
Successfully validated 3 migrations (execution time 00:00.032s)
Current version of schema "PUBLIC": 2
Migrating schema "PUBLIC" with repeatable migration People view
Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.023s)</pre>

And you can check that this is indeed the new status:

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong>

Database: jdbc:h2:file:./foobardb (H2 1.4)
                     
+------------+---------+---------------------+------+---------------------+---------+----------+
| Category   | Version | Description         | Type | Installed On        | State   | Undoable |
+------------+---------+---------------------+------+---------------------+---------+----------+
| Versioned  | 1       | Create person table | SQL  | 2017-12-21 18:05:10 | Success | No       |
| Versioned  | 2       | Add people          | SQL  | 2017-12-21 18:05:10 | Success | No       |
| Repeatable |         | People view         | SQL  | 2017-12-21 18:08:29 | Success |          |
+------------+---------+---------------------+------+---------------------+---------+----------+</pre>

As expected we can see that the repeatable migration was applied successfully.

## Modifying the migration

Now let's see what happens when we modify our migration file in place.

Update `R__People_view.sql` in the `/sql` directory as follows:

```sql
CREATE OR REPLACE VIEW people AS 
    SELECT id, name FROM person WHERE name like 'M%';
```

And check the status again:

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong>

Database: jdbc:h2:file:./foobardb (H2 1.4)
                     
+------------+---------+---------------------+------+---------------------+----------+----------+
| Category   | Version | Description         | Type | Installed On        | State    | Undoable |
+------------+---------+---------------------+------+---------------------+----------+----------+
| Versioned  | 1       | Create person table | SQL  | 2017-12-21 18:05:10 | Success  | No       |
| Versioned  | 2       | Add people          | SQL  | 2017-12-21 18:05:10 | Success  | No       |
| Repeatable |         | People view         | SQL  | 2017-12-21 18:08:29 | Outdated |          |
| Repeatable |         | People view         | SQL  |                     | Pending  |          |
+------------+---------+---------------------+------+---------------------+----------+----------+</pre>

Our audit trail now clearly shows that the repeatable migration that was previously applied has become outdated and is
now marked as pending again, ready to be reapplied.

So let's do exactly that:

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>migrate</strong>

Database: jdbc:h2:file:./foobardb (H2 1.4)
Successfully validated 4 migrations (execution time 00:00.019s)
Current version of schema "PUBLIC": 2
Migrating schema "PUBLIC" with repeatable migration People view
Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.027s)</pre>

And the status is now

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong>

Database: jdbc:h2:file:./foobardb (H2 1.4)

+------------+---------+---------------------+------+---------------------+------------+----------+
| Category   | Version | Description         | Type | Installed On        | State      | Undoable |
+------------+---------+---------------------+------+---------------------+------------+----------+
| Versioned  | 1       | Create person table | SQL  | 2017-12-21 18:05:10 | Success    | No       |
| Versioned  | 2       | Add people          | SQL  | 2017-12-21 18:05:10 | Success    | No       |
| Repeatable |         | People view         | SQL  | 2017-12-21 18:08:29 | Superseded |          |
| Repeatable |         | People view         | SQL  | 2017-12-21 18:15:35 | Success    |          |
+------------+---------+---------------------+------+---------------------+------------+----------+</pre>

Our initial run has now been superseded by the one we just did. And so whenever the object you are managing
(the `people` view in our example) needs to change, simply update the file in place and run migrate again.

## Summary

In this brief tutorial we saw how to
- create repeatable migrations
- run and rerun repeatable migrations

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/concepts/migrations#repeatable-migrations">Read the repeatable migration documentation <i class="fa fa-arrow-right"></i></a>
</p>