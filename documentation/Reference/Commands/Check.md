---
subtitle: Check
---

## Description

The `check` command encapsulates the following operations:

| Parameter                                          | Edition            | Description                                                                                                  |
|----------------------------------------------------|--------------------|--------------------------------------------------------------------------------------------------------------|
| [`check -drift`](<Commands/Check/Check Drift>)     | Enterprise         | Check for unexpected modifications to a database.                                                            |
| [`check -changes`](<Commands/Check/Check Changes>) | Enterprise         | Generate a report of pending changes to a database.                                                          |
| [`check -dryrun`](<Commands/Check/Check DryRun>)   | Teams & Enterprise | Perform a dry run, generating a deployment script representing what will be run when deployment is executed. |
| [`check -code`](<Commands/Check/Check Code>)       | All                | Run code analysis on your migrations.                                                                        |

`check` produces a [report file](<Configuration\Flyway Namespace\Flyway Report Filename Setting>) that contains the results of the operation.

 You might want this to:
 * Have a record of what the check results were for audit or reporting purposes
 * Understand and be able to communicate status and plans in a more easily shareable manner

The reports from the separate operations will all be added to the same report and consecutive runs of Flyway using the same report file will concatenate the results by timestamp. 

## Usage examples

### Generating change & drift reports

```bash
flyway check -changes -drift -buildEnvironment="build" -environment="production"
```


