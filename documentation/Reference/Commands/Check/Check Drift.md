---
subtitle: Check Changes
---

## Description

The `check -drift` command checks a target environment for drift, in order to ensure that it is still in the expected state for running deployments.

Deployment can be aborted if drift is detected, and a report will be generated.

The report is generated using [snapshots](https://documentation.red-gate.com/display/FD/Snapshots) .

See [Drift analysis](https://documentation.red-gate.com/display/FD/Drift+analysis) for more information.

## Usage examples

### Generating report based upon a target environment

```bash
flyway check -drift -buildEnvironment="build" -environment="production"
```

This approach captures a snapshot of the updated build database, and uses this as the basis for the comparison against the target database which underlies the report.
This approach will detect all drift from what is in the migrations, regardless of whether it has occurred since the last deployment or was pre-existing.

### Generating report based upon applied migrations

```bash
flyway info -infoOfState="success,out_of_order" -migrationIds > appliedMigrations.txt
flyway check -drift -buildEnvironment="build" -appliedMigrations="$(cat appliedMigrations.txt) -environment="production"
```

This approach captures a snapshot of the updated build database, and uses this as the basis for the comparison against the target database which underlies the report.
This assumes that the migrations deployed to the target environment represent the full state of that database at the point of last deployment.

### Generating report based upon a snapshot

```bash
flyway check -drift -deployedSnapshot="C:\snapshot1.json" -environment="Production"
```
The comparison which underlies the report is generated from the specified snapshot and a target database. 

## Parameters

### Conditionally required

#### When generating report using a build environment

| Parameter                                                                                                            | Namespace | Description           |
|----------------------------------------------------------------------------------------------------------------------|-----------|-----------------------|
| [`buildEnvironment`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Build Environment Setting>) | check     | Build environment id. |

#### When generating report using a build environment defined inline

| Parameter                                                                                            | Namespace | Description                 |
|------------------------------------------------------------------------------------------------------|-----------|-----------------------------|
| [`buildUrl`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Build URL Setting>) | check     | URL for the build database. |

#### When generating report using a target snapshot

| Parameter                                                                                                            | Namespace | Description                                                                                  |
|----------------------------------------------------------------------------------------------------------------------|-----------|----------------------------------------------------------------------------------------------|
| [`deployedSnapshot`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Deployed Snapshot Setting>) | check     | A snapshot containing all applied migrations and thus matching what should be in the target. |

### Optional

| Parameter                                                                                                              | Namespace | Description                                                                                                      |
|------------------------------------------------------------------------------------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------|
| [`failOnDrift`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Fail On Drift Setting>)            | check     | Return an error if drift is detected.                                                                            |
| [`appliedMigrations`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Applied Migrations Setting>) | check     | Migration ids (migration versions or repeatable descriptions) to apply to create snapshots.                      |
| [`buildUser`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Build User Setting>)                 | check     | Username for the build database.                                                                                 |
| [`buildPassword`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Build Password Setting>)         | check     | Password for the build database.                                                                                 |
| [`filterFile`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Filter File Setting>)               | check     | The path to a filter file, containing custom filtering rules for excluding objects from the comparisons.         |
| [`environment`](<Configuration/Flyway Namespace/Flyway Environment Setting>)                                           | (root)    | The target environment id.                                                                                       |
| [`reportFilename`](<Configuration/Flyway Namespace/Flyway Report Filename Setting>)                                    | (root)    | The output path of the generated report.                                                                         |
| [`workingDirectory`](<Command-line Parameters/Working Directory Parameter>)                                             | (root)    | The directory to consider the current working directory. All relative paths will be considered relative to this. |
| [{environment parameters}](<Configuration/Environments Namespace>)                                                     | (root)    | Environment configuration for the source and/or target environments.                                             |

Universal commandline parameters are listed [here](<Command-line Parameters>).

Settings from the following sections of the [Flyway namespace](<Configuration/Flyway Namespace>) can be specified in relation to database connections:
* General settings
* Settings in database-specific namespaces
* Settings in secrets management namespaces

When a build environment is used, settings from the following sections of the [Flyway namespace](<Configuration/Flyway Namespace>) can also be set:
* Migration location and naming settings
* Migration reading settings
* Migration execution settings
* Flyway schema history settings
* Placeholders

## JSON output format

```json
{
  "htmlReport": "report.html",
  "jsonReport": "report.json",
  "individualResults": [
    {
      message: null,
      stackTrace: null,
      onlyInSource: [],
      onlyInTarget: [],
      differences: [
        {
          name: "Table_1",
          schema: "dbo",
          objectType: "Table",
          definitionBefore: "CREATE TABLE Table_1 (id text)",
          definitionAfter: "CREATE TABLE Table_1 (different_id text)",
        },
      ],
      timestamp: "2022-07-22T08-08-33Z",
      database: "testdb",
      operation: "drift",
      flywayVersion: undefined,
      warnings: undefined,
    }
  ]
 }
```

## Error codes

This command can produce the following error codes:
- [Generic error codes](<Exit codes and error codes/General error codes>)
- [Check error codes](<Exit codes and error codes/Check error codes>)