---
subtitle: Check Changes
---

## Description

{% include teams.html %}

The `check -dryrun` command generates a dry run script.
This is equivalent to a dry run script generated via `migrate` or `prepare`. The flag exists for the convenience of generating all pre-deployment reports in a single command invocation.

## Usage examples

```bash
flyway check -dryrun
```

## Parameters

### Optional

| Parameter                                                                        | Namespace | Description                                                                                                      |
|----------------------------------------------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------|
| [`dryRunOutput`](<Configuration/Flyway Namespace/Flyway Dry Run Output Setting>) | (root)    | The output file path.                                                                                            |
| [`workingDirectory`](<Command-line Parameters/Working Directory Parameter>)       | (root)    | The directory to consider the current working directory. All relative paths will be considered relative to this. |
| [{environment parameters}](<Configuration/Environments Namespace>)               | (root)    | Environment configuration for the source and/or target environments.                                             |

Universal commandline parameters are listed [here](<Command-line Parameters>).

Other relevant configuration settings are listed [here](<Configuration/Flyway Namespace>). The settings from these sections can be set as parameters on the `check -dryrun` command:
* General settings
* Migration location and naming settings
* Migration reading settings
* Migration execution settings
* Flyway schema history settings
* Placeholders
* Settings in database-specific namespaces
* Settings in secrets management namespaces

## JSON output format

```json
{
  "htmlReport": "report.html",
  "jsonReport": "report.json",
  "individualResults": [
    {
      message: null,
      stackTrace: null,
      sql: "SELECT 1;",
      timestamp: "2022-07-22T08-08-33Z",
      database: "testdb",
      operation: "dryrun",
      flywayVersion: undefined,
      warnings: undefined,
    }
  ]
 }
```

## Error codes

This command can produce the following error codes:
- [Generic error codes](<Exit codes and error codes/General error codes>)