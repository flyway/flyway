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

| Parameter                                                                                                             | Namespace | Description                                                                                                      |
|-----------------------------------------------------------------------------------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------|
| [`rulesLocation`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Location Setting>)        | check     | Where Flyway looks for rules.                                                                                    |
| [`rulesConfig`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Config Setting>)            | check     | Where to locate the SQLFluff configuration file.                                                                 |
| [`rulesDialect`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Dialect Setting>)          | check     | Specifies the SQL dialect for analysis.                                                                          |
| [`scope`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Scope Setting>)                         | check     | Specifies the scope of migration files to include in code analysis                                               |
| [`regexEnabled`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Regex Enabled Setting>)          | check     | Enable or disable the Regex Engine for code analysis.                                                            |
| [`sqlfluffEnabled`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check SQLFluff Enabled Setting>)    | check     | Enable or disable the `SQLFluff` Engine for code analysis.                                                       |
| [`code.failOnError`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Fail On Error Setting>) | check     | Whether to fail based on the violation severity level.                                                           |
| [`reportFilename`](<Configuration/Flyway Namespace/Flyway Report Filename Setting>)                                   | (root)    | The output path of the generated report.                                                                         |
| [`workingDirectory`](<Command-line Parameters/Working Directory Parameter>)                                           | (root)    | The directory to consider the current working directory. All relative paths will be considered relative to this. |
| [{environment parameters}](<Configuration/Environments Namespace>)                                                    | (root)    | Environment configuration for the source and/or target environments.                                             |

Universal commandline parameters are listed [here](<Command-line Parameters>).

_Note_:
- _Static code analysis will not interrupt the execution of subsequent Flyway verb operations if they are chained. This means that even if `failOnError` is enabled, Flyway will continue processing the remaining verb operations.
  For this reason, it is recommended to run subsequent Flyway verbs separately._

## JSON output format

```json
{
  "individualResults" : [ {
    "timestamp" : "2026-02-03T15:35:14.170951075",
    "operation" : "code",
    "exception" : null,
    "licenseFailed" : false,
    "results" : [ {
      "filepath" : "/projects/flyway/sql/V1__no_where.sql",
      "violations" : [ {
        "line_no" : 1,
        "line_pos" : 1,
        "line_no_end" : 1,
        "line_pos_end" : 18,
        "description" : "Ensure delete statements have a condition attached.",
        "code" : "RG06",
        "warning" : false,
        "help" : "https://help.red-gate.com/help/flyway-cli12/help_0.aspx?topic=rules/RG06"
      } ]
    } ]
  } ]
}
```

## Error codes

This command can produce the following error codes:
- [Generic error codes](<Exit codes and error codes/General error codes>)