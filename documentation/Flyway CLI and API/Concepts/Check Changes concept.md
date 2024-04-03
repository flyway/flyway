---
subtitle: Check Changes
---
# Check Changes Concept
{% include enterprise.html %}

## Overview
The `-changes` flag produces a report indicating differences between applied migration scripts on your target database and pending migrations scripts (ie. the set of instructions you want to use to change your target database).
You can use this capability pre- and post-deployment:
- In pre-deployment scenarios to check the effect of your pending changes
- In post-deployment scenarios to have captured a history of changes for retrospective auditing or reporting

In either scenario, using the `-changes` flag will help you infer which database objects will be/have been affected - and how - when you execute/have executed your migration scripts.

## Configuring the environment
Please see the [check -changes](<Configuration/Parameters/Flyway/Check/Check Changes>) parameter reference page

## Examples
There are a variety of ways to configure Flyway to produce this report so we'll step through a couple and show you what it is doing along the way.

### Example: `environment` and `buildEnvironment`

The `check -changes` command and flag works by building a temporary database. This 'build' database is first made to reflect the state of your target schema, and then made to reflect your target schema with the pending changes applied.

The difference between the two states of this build database (target now, and target with changes applied) represents the effect your pending migrations will have when the scripts are executed. This difference is captured as an artefact called a "Change Report". The change report is available as both HTML (human readable) and JSON (machine readable) formats.

The process works like this:
![Check_changes.png](assets/Check_changes.png)
1. Specify your target database location
    1. This is the database you want to apply your changes to, where Flyway is already being used to manage migrations (ie. A Flyway migrations table exists)
1. Specify a build environment
    1. This is an existing build environment that contains a build database (note: Flyway will [`clean`](Commands/clean) this database, so if you specify a full database, you must ensure it is ok to for Flyway to erase its schema)
1. Run `flyway check -changes -check.buildEnvironment="build" -environment="production"`

Flyway's `check -changes` will then:
1. Clean your build database
1. Query the target database for the list of applied migrations (for simplicity, let's say it's at V2)
1. Apply these migrations to the build database
1. Take a [`snapshot`](Commands/snapshot) of the build database (now also at V2)
1. Applying pending migrations to the build database (let's say it's now at V5)
1. Take a [`snapshot`](Commands/snapshot) of the build database
1. Compare the V2 build database snapshot to the V5 build database snapshot
1. Generate a HTML (human readable) and JSON (machine readable) Change Report, indicating the additions, deletions, and modifications of database objects between V2 and V5

### Example: `appliedMigrations` and `buildEnvironment`

The `check -changes` command and flag works by building a temporary database. This 'build' database is first made to reflect the state specified by `appliedMigrations`, and then made to reflect your `appliedMigrations` with the pending changes applied.

The difference between the two states of this build database (`appliedMigrations`, and `appliedMigrations` with changes applied) represents the effect your pending migrations will have (or have had) when the scripts are (or were) executed. This difference is captured as an artefact called a "Change Report". The change report is available as both HTML (human readable) and JSON (machine readable) formats.

The process works like this:
![Check_appliedMigrations.png](assets/Check_appliedMigrations.png)
1. Run `flyway info -infoOfState="success,pending,out_of_order" -migrationIds > appliedMigrations.txt`
    1. This will produce a comma-separated list which represents the applied migrations of your target database
1. Specify a build environment
    1. This is an existing build environment that contains a build database (note: Flyway will [`clean`](Commands/clean) this database, so if you specify a full database, you must ensure it is ok to for Flyway to erase its schema)
1. Run `flyway check -changes -check.buildEnvironment="build" -check.appliedMigrations="$(cat appliedMigrations.txt)"`

Flyway's `check -changes` will then:
1. Clean your build database
1. Apply the migrations specified in `appliedMigrations` to the build database (for simplicity, let's say it's at V2)
1. Take a [`snapshot`](Commands/snapshot) of the build database (now also at V2)
1. Applying pending migrations to the build database (let's say it's now at V5)
1. Take a [`snapshot`](Commands/snapshot) of the build database
1. Compare the V2 build database snapshot to the V5 build database snapshot
1. Generate a HTML (human readable) and JSON (machine readable) Change Report, indicating the additions, deletions, and modifications of database objects between V2 and V5

## Good things to know
- There is no requirement for the build environment to be in your production system
- Please note that the build environment **may be cleaned** before the operation starts
- Your Flyway instance assumes that it is the only party changing the build environment so it shouldn't be used concurrently by different developers
- If you get an ERROR: Invalid argument: -check, this is because some systems (for example, Powershell) do not like the period in the argument.  You can wrap the arguments in a single or double quotes to work around this.
  - for example, `flyway check -changes "-check.buildURL"`