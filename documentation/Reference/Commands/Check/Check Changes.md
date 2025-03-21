---
subtitle: Check Changes
---

## Description

The `check -changes` command produces a report indicating differences between what is applied on your target database and pending changes (i.e. the set of instructions you want to use to change your target database).

The report is generated entirely using [snapshots](https://documentation.red-gate.com/display/FD/Snapshots) of the configured [build environment](https://documentation.red-gate.com/display/FD/Shadow+and+build+environments) which means that any drift to the production database will be ignored in this report.

Note:

- There is no requirement for the build environment to be in your production system
- Please note that the build environment **may be cleaned** before the operation starts
- Your Flyway instance assumes that it is the only party changing the build environment so it shouldn't be used concurrently by different developers
- If you get an `ERROR: Invalid argument: -check`, this is because some systems (for example, Powershell) do not like the period in the argument. You can wrap the arguments in a single or double quotes to work around this (e.g. `flyway check -changes "-check.buildURL"`) or use [namespace short-circuiting](https://documentation.red-gate.com/display/FD/Configuration+namespaces).

See [Migrations - Migration validation](https://documentation.red-gate.com/display/FD/Migrations) for more information.

## Usage examples

### Generating report based upon environment

```bash
flyway check -changes -buildEnvironment="build" -environment="production"
```

This approach captures snapshots of the build database before and after pending migrations are executed, and uses these as the basis for the comparison which underlies the report.

### Generating report based upon applied migrations

```bash
flyway info -infoOfState="success,out_of_order" -migrationIds > appliedMigrations.txt
flyway check -changes -buildEnvironment="build" -appliedMigrations="$(cat appliedMigrations.txt)
```

This approach captures snapshots of the build database before and after pending migrations are executed, and uses these as the basis for the comparison which underlies the report.

### Generating report based upon snapshots

```bash
flyway check -changes -deployedSnapshot="C:\snapshot1.json" -nextSnapshot="C:\snapshot2.json"
```

The comparison which underlies the report is generated from the specified snapshots.

### Generating report from a Schema model based upon environment

```bash
flyway check -changes -check.changesSource="schemaModel" -environment="production"
```

The comparison which underlies the report is generated from the specified Schema model and environment.

### Generating report from a Schema model based upon deployed snapshot

```bash
flyway check -changes -check.changesSource="schemaModel" -deployedSnapshot="C:\snapshot.json"
```

The comparison which underlies the report is generated from the specified Schema model and deployed snapshot.

## Parameters

### Conditionally required

#### When generating report based upon configured build environment

| Parameter                                                                                                            | Namespace | Description           |
|----------------------------------------------------------------------------------------------------------------------|-----------|-----------------------|
| [`buildEnvironment`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Build Environment Setting>) | check     | Build environment id. |

#### When generating report based upon build environment defined inline

| Parameter                                                                                            | Namespace | Description                 |
|------------------------------------------------------------------------------------------------------|-----------|-----------------------------|
| [`buildUrl`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Build URL Setting>) | check     | URL for the build database. |

#### When generating report based upon snapshots

| Parameter                                                                                                            | Namespace | Description                                                                                  |
|----------------------------------------------------------------------------------------------------------------------|-----------|----------------------------------------------------------------------------------------------|
| [`deployedSnapshot`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Deployed Snapshot Setting>) | check     | A snapshot containing all applied migrations and thus matching what should be in the target. |
| [`nextSnapshot`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Next Snapshot Setting>)         | check     | A snapshot containing all migrations including those that are pending.                       |

#### When generating report from a Schema model

| Parameter                                                                                      | Namespace | Description                              |
|------------------------------------------------------------------------------------------------|-----------|------------------------------------------|
| [`schemaModelLocation`](<Configuration/Flyway Namespace/Flyway Schema Model Location Setting>) | (root)    | The location of the schema model folder. |

### Optional

| Parameter                                                                                                              | Namespace | Description                                                                                                      |
|------------------------------------------------------------------------------------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------|
| [`appliedMigrations`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Applied Migrations Setting>) | check     | Migration ids (migration versions or repeatable descriptions) to apply to create snapshots.                      |
| [`buildUser`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Build User Setting>)                 | check     | Username for the build database.                                                                                 |
| [`buildPassword`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Build Password Setting>)         | check     | Password for the build database.                                                                                 |
| [`filterFile`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Filter File Setting>)               | check     | The path to a filter file, containing custom filtering rules for excluding objects from the comparisons.         |
| [`changesSource`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Changes Source Setting>)         | check     | The deployment source to generate a change report from.                                                          |
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

When a Schema model is used, settings from the following sections of the [Flyway namespace](<Configuration/Flyway Namespace>) can also be set:
* Schema model settings

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
      operation: "changes",
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
