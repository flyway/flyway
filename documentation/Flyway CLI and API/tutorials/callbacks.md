---
layout: documentation
menu: tut_callbacks
subtitle: 'Tutorial: Callbacks'
redirect_from:
- /getStarted/callbacks/
- /documentation/getstarted/callbacks/
- /documentation/getstarted/advanced/callbacks/
---
# Tutorial: Callbacks

This tutorial assumes you have successfully completed the [**First Steps: Command-line**](/documentation/getstarted/firststeps/commandline)
tutorial. **If you have not done so, please do so first.** This tutorial picks up where that one left off.

This brief tutorial will teach **how to use callbacks**. It will take you through the
steps on how to create and use them.

## Introduction

**Callbacks** let you hook into Flyway's lifecycle. This is particularly useful when you execute the same housekeeping 
action over and over again.
 
They are typically used for
- Recompiling procedures
- Updating materialized views
- Storage housekeeping (`VACUUM` for PostgreSQL for example)

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

## Creating a callback

Now let's create a callback to flush all data to disk before a migration run. To do so, we'll make use of Flyway's
`beforeMigrate` callback.

So go ahead and create `beforeMigrate.sql` in the `/sql` directory:

```sql
CHECKPOINT SYNC;
```

## Triggering the callback

To trigger the execution of the callback, we'll clean and migrate the database again.

So go ahead and invoke

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway clean <strong>migrate</strong></pre>

This will give you the following result:

<pre class="console">Database: jdbc:h2:file:./foobardb (H2 1.4)
Successfully cleaned schema "PUBLIC" (execution time 00:00.003s)
Successfully validated 2 migrations (execution time 00:00.010s)
<strong>Executing SQL callback: beforeMigrate</strong>
Creating Schema History table: "PUBLIC"."flyway_schema_history"
Current version of schema "PUBLIC": << Empty Schema >>
Migrating schema "PUBLIC" to version 1 - Create person table
Migrating schema "PUBLIC" to version 2 - Add people
Successfully applied 2 migrations to schema "PUBLIC" (execution time 00:00.034s)</pre>

As expected we can see that the `beforeMigrate` callback was triggered and executed successfully before the `migrate`
operation. Each time you invoke migrate again in the future, the callback will now be executed again.

## Summary

In this brief tutorial we saw how to
- create callbacks
- triggers the execution of callbacks

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/concepts/callbacks">Read the callback documentation <i class="fa fa-arrow-right"></i></a>
</p>