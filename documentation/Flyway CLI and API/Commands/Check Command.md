---
subtitle: Check
---

# Check

`check` produces reports to increase confidence in your migrations.

For `-changes` and `-drift`, Flyway migrates against a build database and compares this against the target database in order to generate a report.
**This build database will be cleaned before it is used, so you must ensure it does not contain anything of importance.**

You can read more about the `check` concept [here](Concepts/Check Concept).

#### Requirements
- `sqlfluff` is required for Code Analysis (`-code`). You can install it by running `pip3 install sqlfluff==1.2.1` or use the `redgate/flyway` Docker image that has `sqlfluff` pre-installed.

#### Flags:
- _One or more flags must be present_

| Parameter                     | Edition            |  Description
| ----------------------------- | -------------------| -----------------------------------------------------
|    -changes                   | Enterprise         | Include pending changes that will be applied to the database
|    -drift                     | Enterprise         | Include changes applied out of process to the database
|    -dryrun                    | Teams & Enterprise | Performs a [dry run](Concepts/Dry Runs), showing what changes would be applied in a real deployment
|    -code                      | All                | Performs code analysis on your migrations

#### Configuration parameters:
 _Format: -key=value_

| Parameter               | Operation      | Description
|-------------------------| -------------- | -------------------------------------------------
| check.buildEnvironment  | changes, drift | Environment for the build database. Defaults to 'default_build', see [Check Concept](Concepts/Check Concept)
| check.buildUrl          | changes, drift | **[DEPRECATED]** URL for a build database. Will be replaced by check.buildEnvironment in Flyway 10.0
| check.buildUser         | changes, drift | **[DEPRECATED]** Username for the build database. Defaults to 'flyway.user'. Will be replaced by check.buildEnvironment in Flyway 10.0
| check.buildPassword     | changes, drift | **[DEPRECATED]** Password for the build database. Defaults to 'flyway.password'. Will be replaced by check.buildEnvironment in Flyway 10.0
| check.nextSnapshot      | changes, drift | A snapshot containing all migrations including those that are pending (generated via [`snapshot`](Commands/snapshot))
| check.deployedSnapshot  | changes, drift | A snapshot containing all applied migrations and thus matching what should be in the target (generated via [`snapshot`](Commands/snapshot))
| check.appliedMigrations | changes, drift | A comma-separated list of migration ids (migration versions or repeatable descriptions) to apply to create snapshots (generated via [`info`](Commands/info))
| check.failOnDrift       | drift          | Will Flyway terminate with a non-zero return code if drift detected, see [`check.failOnDrift`](Configuration/Parameters/Flyway/Check/Fail On Drift)
| check.majorRules        | code           | A comma-separated list of rule codes that are considered to be 'major' issues, see [Check Concept](Concepts/Check Concept)
| check.minorRules        | code           | A comma-separated list of rule codes that are considered to be 'minor' issues, see [Check Concept](Concepts/Check Concept)
| check.majorTolerance    | code           | The number of 'major' issues to be tolerated before throwing an error, see [Check Concept](Concepts/Check Concept)
| check.minorTolerance    | code           | The number of 'minor' issues to be tolerated before throwing an error, see [Check Concept](Concepts/Check Concept)

##### Example conf configuration file

```properties
flyway.url=jdbc:example:database
flyway.user=username
flyway.password=password
flyway.check.buildEnvironment=build
```

##### Example TOML configuration file

```properties
[ flyway ]
locations = [ "filesystem:sql" ]
cleanDisabled = false

[ flyway.check ]
buildEnvironment = "build"

[ environments.default ]
url = "jdbc:example:database"

[ environments.build ]
url = "jdbc:example:database2"
```

#### Database Support

##### `-changes` and `-drift`

Change and drift reports work on the following databases:

- SQL Server
- PostgreSQL
- Oracle
- SQLite

##### `-code` and `-dryrun`

Code analysis and Dry run reports work on any database supported by Flyway.

##### Check for Oracle

When using Check with an Oracle database there are additional requirements.

If no schemas are specified in the configuration `flyway.schemas`, then the database connection username will be used as the default schema, otherwise `flyway.schemas` will be used.
These schema names are case-sensitive.

##### Check for SQL Server

If you see errors about being unable to connect to your database and you are specifying `localhost` as the host, then this may be due to not having pipes configured. Using `127.0.0.1` instead should work.
