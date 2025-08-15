---
subtitle: Check Changes
---

## Description

The `check -code` command runs static code analysis over your SQL migrations from `filesystem:` [Locations](<Configuration/Flyway Namespace/Flyway Locations Setting>).

See [Code Analysis](https://documentation.red-gate.com/flyway/flyway-concepts/code-analysis) for more information.

## Usage examples

```bash
flyway check -code
```

## Parameters

### Optional

| Parameter                                                                                                        | Namespace | Description                                                                                                      |
|------------------------------------------------------------------------------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------|
| [`rulesLocation`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Location Setting>)   | check     | Where Flyway looks for rules.                                                                                    |
| [`scope`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Scope Setting>)                      | check     | Specifies the scope of migration files to include in code analysis                                        |
| [`majorRules`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Major Rules Setting>)         | check     | List of rules considered to be major.                                                                            |
| [`majorTolerance`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Major Tolerance Setting>) | check     | The number of major rules violations to be tolerated before throwing an error.                                   |
| [`minorRules`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Minor Rules Setting>)         | check     | List of rules considered to be minor.                                                                            |
| [`minorTolerance`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Minor Tolerance Setting>) | check     | The number of minor rules violations to be tolerated before throwing an error.                                   |
| [`regexEnabled`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Regex Enabled Setting>) | check     | Enable or disable the Regex Engine for code analysis.                                                            |  
| [`sqlfluffEnabled`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check SQLFluff Enabled Setting>) | check     | Enable or disable the `SQLFluff` Engine for code analysis.                                                       |  
| [`reportFilename`](<Configuration/Flyway Namespace/Flyway Report Filename Setting>)                              | (root)    | The output path of the generated report.                                                                         |
| [`workingDirectory`](<Command-line Parameters/Working Directory Parameter>)                                       | (root)    | The directory to consider the current working directory. All relative paths will be considered relative to this. |
| [{environment parameters}](<Configuration/Environments Namespace>)                                               | (root)    | Environment configuration for the source and/or target environments.                                             |

Universal commandline parameters are listed [here](<Command-line Parameters>).

_Note_:
- _Static code analysis will not interrupt the execution of subsequent Flyway verb operations if they are chained. This means that even if a `majorTolerance` or `minorTolerance` error occurs, Flyway will continue processing the remaining verb operations.
  For this reason, it is recommended to run subsequent Flyway verbs separately._

## JSON output format

```json
{
  "htmlReport": "report.html",
  "jsonReport": "report.json",
  "individualResults": [
    {
      message: null,
      stackTrace: null,
      results: [
        {
          filepath: "C:\\flywayProject\migrations\V001__AddTable.sql",
          violations: [
            "line_no:": 5,
            "line_pos": 10,
            "description": "violation description",
            "code": "violation code"
          ]
        },
      ],
      timestamp: "2022-07-22T08-08-33Z",
      database: "testdb",
      operation: "code",
      flywayVersion: undefined,
      warnings: undefined,
    }
  ]
 }
```

## Error codes

This command can produce the following error codes:
- [Generic error codes](<Exit codes and error codes/General error codes>)