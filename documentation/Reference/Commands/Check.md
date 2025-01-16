---
subtitle: Check
---

## Description

The `check` command amalgamates the following commands:

| Parameter                                          | Edition            | Description                                                                                                  |
|----------------------------------------------------|--------------------|--------------------------------------------------------------------------------------------------------------|
| [`check -drift`](<Commands/Check/Check Drift>)     | Enterprise         | Check for unexpected modifications to a database.                                                            |
| [`check -changes`](<Commands/Check/Check Changes>) | Enterprise         | Generate a report of pending changes to a database.                                                          |
| [`check -dryrun`](<Commands/Check/Check DryRun>)   | Teams & Enterprise | Perform a dry run, generating a deployment script representing what will be run when deployment is executed. |
| [`check -code`](<Commands/Check//Check Code>)      | All                | Run code analysis on your migrations.                                                                        |

The reports from the separate operations can all be added to the same report file. 

## Usage examples

### Generating all reports

```bash
flyway check -changes -drift -buildEnvironment="build" -environment="production"
```


