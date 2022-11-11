---
layout: documentation
menu: tut_dryruns
subtitle: 'Tutorial: Dry Runs'
redirect_from: 
- /getStarted/dryruns/
- /documentation/getstarted/advanced/dryruns/
---
# Tutorial: Dry Runs
{% include teams.html %}

This tutorial assumes you have successfully completed the [**First Steps: Command-line**](/documentation/getstarted/firststeps/commandline)
tutorial. **If you have not done so, please do so first.** This tutorial picks up where that one left off.

This brief tutorial will teach **how to do Dry Runs**. It will take you through the
steps on how to use them.

## Introduction

**Dry Runs** are a great fit for situations where you may want to:
- preview the changes Flyway will make to the database
- submit the SQL statements for review to a DBA before applying them
- use Flyway to determine what needs updating, yet use a different tool to apply the actual database changes

When doing a Dry Run, Flyway sets up a read-only connection to the database. It assesses what migrations need to run and
generates a single SQL file containing all statements it would have executed in case of a regular migration
run. This SQL file can then be reviewed. If satisfactory, Flyway can then be instructed to migrate the database and
all changes will be applied. Alternatively a separate tool of your choice can also be used to apply the dry run SQL file
directly to the database without using Flyway. This SQL file also contains the necessary statements to create and update Flyway's
[schema history table](/documentation/concepts/migrations#schema-history-table), ensuring that all schema changes are tracked the usual way.  

## Reviewing the status

After having completed the [First Steps: Command-line](/documentation/getstarted/firststeps/commandline), you can now execute

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong></pre>

This should give you the following status:

<pre class="console">Database: jdbc:h2:file:./target/foobar (H2 1.4))

+-----------+---------+---------------------+------+---------------------+---------+
| Category  | Version | Description         | Type | Installed On        | State   |
+-----------+---------+---------------------+------+---------------------+---------+
| Versioned | 1       | Create person table | SQL  | 2017-12-22 15:26:39 | Success |
| Versioned | 2       | Add people          | SQL  | 2017-12-22 15:28:17 | Success |
+-----------+---------+---------------------+------+---------------------+---------+</pre>

## Adding a new migration

Let's add a new migration for which we'll do a dry run at first.

In the `./sql` directory, create a migration called `V3__Couple.sql`:

```sql
create table COUPLE (
    ID int not null,
    PERSON1 int not null references PERSON(ID), 
    PERSON2 int not null references PERSON(ID) 
);
```

## Doing a dry run

Now let's preview the database changes of this migration by doing a dry run:

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway migrate <strong>-dryRunOutput=dryrun.sql</strong></pre>

This will produce a file called `dryrun.sql` which contains all SQL statements that Flyway would have executed against
the database in case of a regular migration. See for yourself: 

```sql
---====================================
-- Flyway Dry Run (2018-01-25 17:19:17)
---====================================

SET SCHEMA "PUBLIC";

-- Executing: validate (with callbacks)
------------------------------------------------------------------------------------------
-- ...

-- Executing: migrate (with callbacks)
------------------------------------------------------------------------------------------
-- ...

-- Executing: migrate -> v3 (with callbacks)
------------------------------------------------------------------------------------------

-- Source: ./V3__Couple.sql
---------------------------
create table COUPLE (
    ID int not null,
    PERSON1 int not null references PERSON(ID), 
    PERSON2 int not null references PERSON(ID) 
);
INSERT INTO "PUBLIC"."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","execution_time","success") VALUES (2, '3', 'Couple', 'SQL', 'V3__Couple.sql', -722651034, 'SA', 0, 1);
-- ...
```

This file can now be manually inspected.
 
## Applying the changes
 
Once the inspection has completed and it is deemed to be OK, the migration can then be applied
using the usual `migrate` command:

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>migrate</strong></pre>

Alternatively, you can also apply the migration using your database's built-in support for running SQL scripts. For example, using H2:

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> java -cp drivers/* org.h2.tools.RunScript -url jdbc:h2:file:./foobardb -script <strong>dryrun.sql</strong></pre>

Either one of these approaches yields the same result as you can see using:

<pre class="console"><span>flyway-{{ site.flywayVersion }}&gt;</span> flyway <strong>info</strong></pre>

This should give you the following status:

<pre class="console">Database: jdbc:h2:file:./target/foobar (H2 1.4)
Schema version: 3

+-----------+---------+---------------------+------+---------------------+---------+----------+
| Category  | Version | Description         | Type | Installed On        | State   | Undoable |
+-----------+---------+---------------------+------+---------------------+---------+----------+
| Versioned | 1       | Create person table | SQL  | 2017-12-22 15:26:39 | Success | No       |
| Versioned | 2       | Add people          | SQL  | 2017-12-22 15:28:17 | Success | No       |
| Versioned | 3       | Couple              | SQL  | 2018-01-25 17:57:13 | Success | No       |
+-----------+---------+---------------------+------+---------------------+---------+----------+</pre>

## Summary

In this brief tutorial we saw how to
- configure and execute Flyway to do a dry run
- apply the changes after the dry run has been validated

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/concepts/dryruns">Read the Dry Runs documentation <i class="fa fa-arrow-right"></i></a>
</p>
