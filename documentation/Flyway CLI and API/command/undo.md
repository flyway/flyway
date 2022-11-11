---
layout: documentation
menu: undo
subtitle: Undo
---
# Undo

<a class="btn btn-primary" href="https://flywaydb.org/try-flyway-teams-edition">Get Undo in Flyway Teams</a>

Undoes the most recently applied versioned migration.

![Undo](/assets/balsamiq/command-undo.png)

## Usage
{% include commandUsage.html command="undo" %}
<br/>

API users will need to include `flyway-proprietary` as a dependency in order to use `undo`. For example:

```
<dependency>
    <groupId>org.flywaydb.enterprise</groupId>
    <artifactId>flyway-proprietary</artifactId>
    <version>{{ site.flywayVersion }}</version>
</dependency>
```

## Description

If `target` is specified, Flyway will attempt to undo versioned migrations in the reverse of their applied order, until it hits
one with a version below the target, or one without a corresponding undo migration. If `group` is active, Flyway will attempt to undo all these migrations within a
single transaction. 

If there is no versioned migration to undo, calling undo has no effect.

There is no undo functionality for repeatable migrations. In that case the repeatable migration should be modified to
include the older state that one desires and then reapplied using [migrate](/documentation/command/migrate).

## Important notes

Please note, you should take care if you have destructive changes (drop, delete, truncate etc) in your deployment. 
Undo migrations assume the whole migration succeeded and should now be undone. 

This means that failed versioned migrations on databases without DDL transactions may require a different approach. 
This is because a migration can fail at any point. If you have 10 statements, it is possible for the 1st, the 5th, 
the 7th or the 10th to fail, whereas undo migrations will undo an entire versioned migration and so will not help 
under such conditions. 

In such circumstances, an alternative approach could be to **maintain backwards compatibility between the DB and all 
versions of the code currently deployed in production**. This way the old version of the application is still compatible 
with the DB, so you can simply roll back the application code, investigate, and take corrective measures.

This should be complemented with a **proper, well tested, backup and restore strategy**. It is independent of the database 
structure, and once it is tested and proven to work, no migration script can break it. For optimal performance, and 
if your infrastructure supports this, we recommend using the snapshot technology of your underlying storage solution. 
Especially for larger data volumes, this can be several orders of magnitude faster than traditional backups and restores.

<a class="btn btn-primary" href="https://flywaydb.org/try-flyway-teams-edition">Get Undo in Flyway Teams</a>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/command/baseline">Baseline <i class="fa fa-arrow-right"></i></a>
</p>