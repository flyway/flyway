---
layout: documentation
menu: check
subtitle: Check
---

# Check

<div id="toc"></div>

## Overview

In Flyway, “Checks” is the collective term we use for the pre- or post-deployment analysis of some aspect of your database migration. Checks are instantiated using the top level `check` command.

Before performing a deployment to the target database (most notably, production), you might want to look over what you’re about to do and understand one or more of the following:

- Does this set of changes affect the objects I expect it to, or will I be inadvertently having an impact on something else?
- What database changes have been made recently, that coincide with the changes in database performance we are seeing? Are the two related?
- Is the production database in the same state you were expecting when I began developing my changes? Has anything about the target database changed that would mean my changes no longer have the desired effect?
- Does our approach to database change development meet our internal policies? Are our migration scripts adhering to our naming conventions, for example? Are we following the security best-practices required of us by our external regulatory requirements?

Each of these scenarios can be met with the `check` command, using the corresponding flag:

| Scenario                                                                             | Command & Flag                                   | Output                                  |
|--------------------------------------------------------------------------------------|--------------------------------------------------|-----------------------------------------|
| Will these changes have the effect I am expecting?                                   | check **-changes** {% include enterprise.html %} | ChangeReport.html,<br>ChangeReport.json |
| What database changes have been made recently?                                       | check **-changes** {% include enterprise.html %} | ChangeReport.html,<br>ChangeReport.json |
| Is the production database in the state I am expecting it to be in?                  | check **-drift**   {% include enterprise.html %} | ChangeReport.html,<br>ChangeReport.json |
| _In Preview_<br>Are our changes following internal policies?                         | check **-code**                                  | ChangeReport.html,<br>ChangeReport.json |
| What SQL will be executed in the next deployment?                                    | check **-dryrun**  {% include teams.html %}      | ChangeReport.html,<br>ChangeReport.json |

## `Check –changes`

{% include enterprise.html %}

### Overview
The `–changes` flag produces a report indicating differences between applied migration scripts on your target database and pending migrations scripts (ie. the set of instructions you want to use to change your target database).
You can use this capability pre- and post-deployment:
- In pre-deployment scenarios to check the effect of your pending changes
- In post-deployment scenarios to have captured a history of changes for retrospective auditing or reporting

In either scenario, using the `-changes` flag will help you infer which database objects will be/have been affected - and how – when you execute/have executed your migration script(s).

### Requirements and behavior

There are 4 ways to generate a change report:
- If you have access to both your target and build database you should use `url` and `check.buildUrl`
- If you can't access your target database from your build environment you should use `check.appliedMigrations` and `check.buildUrl`
- If you do not have a build database you should use `url` and `check.nextSnapshot`
- If you cannot access either your target or build database you should use `check.deployedSnapshot` and `check.nextSnapshot`

#### Example: `url` and `buildUrl`

The `check –changes` command and flag works by building a temporary database. This 'build' database is first made to reflect the state of your target schema, and then made to reflect your target schema with the pending changes applied.

The difference between the two states of this build database (target now, and target with changes applied) represents the effect your pending migrations will have (or have had) when the scripts are (or were) executed. This difference is captured as an artefact called a “Change Report”. The change report is available as both HTML (human readable) and JSON (machine readable) formats.

The process works like this:
![Check_changes.png](/assets/balsamiq/Check_changes.png)
1. Specify your target database location
    1. This is the database you want to apply your changes to, where Flyway is already being used to manage migrations (ie. A Flyway migrations table exists)
1. Specify a build database
    1. This is an existing build database (note: Flyway will [`clean`](/documentation/command/clean) this database, so if you specify a full database, you must ensure it is ok to for Flyway to erase its schema)
1. Run `flyway check –changes -check.buildUrl="jdbc://build-url" -url="jdbc://url" -check.reportFilename="changeReport.html"`

Flyway’s `check –changes` will then:
1. Clean your build database
1. Query the target database for the list of applied migrations (for simplicity, let’s say it’s at V2)
1. Apply these migrations to the build database
1. Take a [`snapshot`](/documentation/command/snapshot) of the build database (now also at V2)
1. Applying pending migrations to the build database (let’s say it’s now at V5)
1. Take a [`snapshot`](/documentation/command/snapshot) of the build database
1. Compare the V2 build database snapshot to the V5 build database snapshot
1. Generate a HTML (human readable) and JSON (machine readable) Change Report, indicating the additions, deletions, and modifications of database objects between V2 and V5

#### Example: `appliedMigrations` and `buildUrl`

The `check –changes` command and flag works by building a temporary database. This 'build' database is first made to reflect the state specified by `appliedMigrations`, and then made to reflect your `appliedMigrations` with the pending changes applied.

The difference between the two states of this build database (`appliedMigrations`, and `appliedMigrations` with changes applied) represents the effect your pending migrations will have (or have had) when the scripts are (or were) executed. This difference is captured as an artefact called a “Change Report”. The change report is available as both HTML (human readable) and JSON (machine readable) formats.

The process works like this:
![Check_appliedMigrations.png](/assets/balsamiq/Check_appliedMigrations.png)
1. Run `flyway info -infoOfState="success,pending,out_of_order" -migrationIds > appliedMigrations.txt`
    1. This will produce a comma-separated list which represents the applied migrations of your target database
1. Specify a build database
    1. This is an existing build database (note: Flyway will [`clean`](/documentation/command/clean) this database, so if you specify a full database, you must ensure it is ok to for Flyway to erase its schema)
1. Run `flyway check –changes -check.buildUrl="jdbc://build-url" -check.appliedMigrations="$(cat appliedMigrations.txt)" -check.reportFilename="changeReport.html"`

Flyway’s `check –changes` will then:
1. Clean your build database
1. Apply the migrations specified in `appliedMigrations` to the build database (for simplicity, let’s say it’s at V2)
1. Take a [`snapshot`](/documentation/command/snapshot) of the build database (now also at V2)
1. Applying pending migrations to the build database (let’s say it’s now at V5)
1. Take a [`snapshot`](/documentation/command/snapshot) of the build database
1. Compare the V2 build database snapshot to the V5 build database snapshot
1. Generate a HTML (human readable) and JSON (machine readable) Change Report, indicating the additions, deletions, and modifications of database objects between V2 and V5

## `Check –drift`

{% include enterprise.html %}

### Overview
The `–drift` flag produces a report indicating differences between structure of your target database and structure created by the migrations applied by Flyway.

### Requirements and behavior

There are 2 ways to generate a drift report:
- If you have access to both your target and build database you should use `url` and `check.buildUrl`
- If you do not have a build database you should use `url` and `check.deployedSnapshot`

#### Example: `url` and `buildUrl`

The `check –drift` command and flag works by building a temporary database. This 'build' database is made to reflect the state of your target schema based on the migrations applied by Flyway.

The difference between the two states of this build database and your target database represents the drift between the expected structure according to Flyway and the actual structure. This difference is captured as an artefact called a “Drift Report”. The drift report is available as both HTML (human readable) and JSON (machine readable) formats.

The process works like this:
![Check_drift.png](/assets/balsamiq/Check_drift.png)
1. Specify your target database location
    1. This is the database you want to apply your changes to, where Flyway is already being used to manage migrations (ie. A Flyway migrations table exists)
1. Specify a build database
    1. This is an existing build database (note: Flyway will “clean” this database, so if you specify a full database, you must ensure it is ok to for Flyway to erase its schema)
1. Run `flyway check –drift -check.buildUrl="jdbc://build-url" -url="jdbc://url" -check.reportFilename="driftReport.html"`

Flyway’s `check –drift` will then:
1. Take a [`snapshot`](/documentation/command/snapshot) of the target database
2. Clean your build database
3. Query the target database for the list of applied migrations (for simplicity, let’s say it’s at V2)
4. Apply these migrations to the build database
5. Take a [`snapshot`](/documentation/command/snapshot) of the build database (now also at V2)
6. Compare the V2 target database snapshot to the V2 build database snapshot
7. Generate a HTML (human readable) and JSON (machine readable) Drift Report, indicating the additions, deletions, and modifications of database objects between target and build

## Good things to know
- There is no requirement for the build database to be in your production system
- Please note that the build database **may be cleaned** before the operation starts
- The underlying comparison technology is dependent on [.NET 6](https://dotnet.microsoft.com/en-us/download/dotnet/6.0) which is why this is required
- If you get an ERROR: Invalid argument: -check, this is because some systems do not like the period in the argument.  You can wrap the arguments in a single or double quotes.  Eg, -'check.buildURL'

## `Check -code`

### Overview

The `-code` flag produces a report showing the results of running static code analysis over your SQL migrations.
This report is an integration with [SQLFluff](https://www.sqlfluff.com/) which analyses your SQL according to a set of rules to ensure standards are met.

### Requirements and behavior

SQLFluff needs to be installed on the machine producing the report. We currently support version 1.2.1. You can install it by running:

```
pip3 install sqlfluff==1.2.1
```

As this is an integration, it can be used in Flyway Community too albeit with more manual steps.

#### Example: Flyway Community and SQLFluff

You can invoke SQLFluff by running:

```
sqlfluff lint --dialect <dialect> [migrations]
```

The dialect should be the flavour of SQL you are using, such as `ansi` or `tsql`.

`[migrations]` could be either a space-separated list of location(s) containing your migrations, or individual migration(s).

This will produce a report in your terminal.

#### Example: Flyway Teams

In Flyway Teams, you can run:

```
flyway check -code -check.reportFilename=report.html -url=jdbc:postgresql://...
```

This will run SQLFluff under the hood, and produce a HTML and JSON report that you can use to check the standards of your migrations.

Flyway makes use of any configured `locations` to determine what migrations to analyse.
If you have a `URL` configured, Flyway will only run analysis on pending migrations.
If no `URL` is configured, then _all_ migrations (pending and applied) will be analysed.

### Configuring SQLFluff

#### Dialects

If you provide a URL to `check -code` Flyway will use it to automatically determine which SQL dialect to use when analysing your SQL.

If no URL is provided, then you need to configure the dialect in a `.sqlfluff` configuration file.
This file needs to be located in the same location as the migration(s) being analysed.
You can find more information on SQLFluff configuration [here](https://docs.sqlfluff.com/en/stable/configuration.html).

You can also use this to configure more than just the dialect, such as which rules should be enabled or disabled.

#### Failing on Rule Violations 

{% include teams.html %}

You can configure your pipeline to fail when specified SQL Fluff rules are violated beyond a given tolerance level.
This can be done by configuring `check.majorRules`,`check.minorRules`,`check.majorTolerance` and `check.minorTolerance`.

`majorRules` should contain a comma-separated list of [SQL Fluff rule codes](https://docs.sqlfluff.com/en/stable/rules.html) which are considered to be `major`.
If the total number of `majorRules` violations exceeds the `majorTolerance`, Flyway will fail. 

The same applies to `minorRules` and `minorTolerance`.

For example:

```
./flyway check -code '-check.majorTolerance=3' '-check.majorRules=L034,L042'
```

This will fail if rules `L034` and `L042` are violated 4 or more times in total across all scanned migration scripts.