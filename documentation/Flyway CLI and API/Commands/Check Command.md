---
subtitle: Check
---

# Check

`check` produces reports to increase confidence in your migrations.

You can read more about how `check`works here: [Check Concept](<Concepts/Check Concept>).

More specific detail about each aspect can be found here:
- [Check Changes Concept](<Concepts/Check Changes concept>)
- [Check Drift Concept](<Concepts/Check Drift concept>)
- [Dry Run Concept](<Concepts/Dry Runs>)
- [Check Code Concept](<Concepts/Check Code concept>)

#### Flags:
- _One or more flags must be present_

| Parameter                                                                 | Edition            | Description                                                                    |
|---------------------------------------------------------------------------|--------------------|--------------------------------------------------------------------------------|
| [`check -changes`](<Configuration/Parameters/Flyway/Check/Check Changes>) | Enterprise         | Include pending changes that will be applied to the database                   |
| [`check -drift`](<Configuration/Parameters/Flyway/Check/Check Drift>)     | Enterprise         | Include changes applied out of process to the database                         |
| `check -dryrun`                                                           | Teams & Enterprise | Performs a dry run, showing what changes would be applied in a real deployment |
| [`check -code`](<Configuration/Parameters/Flyway/Check/Check Code>)       | All                | Performs code analysis on your migrations                                      |


#### Database Support

##### `-changes` and `-drift`

Change and drift reports work on the following databases:

- SQL Server
- PostgreSQL
- Oracle
- MySQL
- SQLite

##### `-code` and `-dryrun`

Code analysis and Dry run reports work on any database supported by Flyway.

##### Check for Oracle

When using Check with an Oracle database there are additional requirements.

If no schemas are specified in the configuration `flyway.schemas`, then the database connection username will be used as the default schema, otherwise `flyway.schemas` will be used.
These schema names are case-sensitive.

##### Check for SQL Server

If you see errors about being unable to connect to your database and you are specifying `localhost` as the host, then this may be due to not having pipes configured. Using `127.0.0.1` instead should work.
