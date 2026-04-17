---
subtitle: snapshot
---

## Description

{% include enterprise.html %}

Generates a snapshot from a database environment, build environment, schema snapshot folder or empty source.
A snapshot captures the schema of a specified source into a file for subsequent use with
the [Check Changes command](<Commands/Check/Check Changes>), [Check Drift command](<Commands/Check/Check Drift>), [Snapshot command](<Commands/Snapshot>)
or [Snapshot provisioner](Configuration/Environments Namespace/Environment Provisioner Setting/Snapshot Provisioner).

Be sure to set [environment schemas](<Configuration/Environments Namespace/Environment Schemas Setting>) when generating
a snapshot for Oracle databases.

Snapshots may be stored in the snapshot history table by specifying an appropriate location using the
[`filename`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot Source Setting>) parameter. In
this case, Flyway will create the snapshot history table automatically if it doesn't exist.

See [Snapshots](https://documentation.red-gate.com/display/FD/Snapshots) for more information.

## Usage examples

### Generating a snapshot from a database URL

```bash
flyway snapshot -url=jdbc:sqlserver://localhost:1433;encrypt=false;databaseName=Inventory -user=sa -password=... -filename=C:\snapshot.json
```

### Generating a snapshot from a database environment

if a database environment named `dev` is configured in the `flyway.toml` file, then the environment name can be provided
as a CLI argument instead of passing the connection details as CLI arguments.

```bash
flyway snapshot -source=dev -filename=C:\snapshot.json
```

### Generating a snapshot from a schema snapshot folder

```bash
flyway snapshot -source=schemaSnapshot -filename=C:\snapshot.json
```

### Generating a snapshot using the build database

```bash
flyway snapshot -source=migrations -buildEnvironment=shadow -buildVersion=2 -filename=C:\snapshot.json
```

The `snapshot.build*` arguments make it possible to create a snapshot for any migration version.

## Parameters

### Required

| Parameter                                                                                                 | Namespace | Description                                                                                   |
|-----------------------------------------------------------------------------------------------------------|-----------|-----------------------------------------------------------------------------------------------|
| [`filename`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot Filename Setting>) | snapshot  | Destination location for the snapshot. Can be a file path, or a snapshot history table entry. |

### Optional

| Parameter                                                                                                                  | Namespace | Description                                                                                                      |
|----------------------------------------------------------------------------------------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------|
| [`source`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot Source Setting>)                      | snapshot  | The source from which a snapshot should be generated.                                                            |
| [`historyTable`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot History Table Setting>)         | snapshot  | The name of the Flyway snapshot history database table, in which to store snapshots.                             |
| [`historyLimit`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot History Limit Setting>)         | snapshot  | The maximum number of snapshots to store in the database table.                                                  |
| [`buildEnvironment`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot Build Environment Setting>) | snapshot  | If source is migrations, this specifies the environment to use as the build environment.                         |
| [`buildVersion`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot Build Version Setting>)         | snapshot  | If source is migrations, this specifies migration version to migrate the build environment to.                   |
| [`buildCherryPick`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot Build Cherry Pick Setting>)  | snapshot  | If source is migrations, this specifies list of migrations to migrate the build environment with.                |
| [`rebuild`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot Rebuild Setting>)                    | snapshot  | If source is migrations, forces a reprovision (rebuild) of the build environment.                                |
| [`schemaModelLocation`](<Configuration/Flyway Namespace/Flyway Schema Model Location Setting>)                             | (root)    | The path to the schema model.                                                                                    |
| [`schemaModelSchemas`](<Configuration/Flyway Namespace/Flyway Schema Model Schemas Setting>)                               | (root)    | The schemas in the schema model.                                                                                 |
| [`workingDirectory`](<Command-line Parameters/Working Directory Parameter>)                                                | (root)    | The directory to consider the current working directory. All relative paths will be considered relative to this. |
| [{environment parameters}](<Configuration/Environments Namespace>)                                                         | (root)    | Environment configuration for the source environment.                                                            |

Universal commandline parameters are listed [here](<Command-line Parameters>).

Settings from the following sections of the [Flyway namespace](<Configuration/Flyway Namespace>) can be specified in
relation to database connections:

* General settings
* Settings in database-specific namespaces
* Settings in secrets management namespaces

When the source is `migrations`, settings from the following sections of
the [Flyway namespace](<Configuration/Flyway Namespace>) can also be set:

* Migration location and naming settings
* Migration reading settings
* Migration execution settings
* Flyway schema history settings
* Placeholders

## JSON output format

```json
{
  "filename": "C:\snapshot.json"
}
```

## Error codes

This command can produce the following error codes:

- [Generic error codes](<Exit codes and error codes/General error codes>)
- [Comparison error codes](<Exit codes and error codes/Comparison error codes>)

### Database Binary size limits

When storing snapshots to
the [Flyway Snapshot History Table](<Configuration/Flyway Namespace/flyway-snapshot-namespace/flyway-snapshot-history-table-setting>),
there is a size constraint.
The snapshots will be compressed before insertion into the table, so the constraint only applies after compression.

| Database   | Datatype       | Size |
|------------|----------------|------|
| SQL Server | VARBINARY(MAX) | 2Gb  |
| Oracle     | BLOB           | 4Gb  |
| MySQL      | LONGBLOB       | 2Gb  |
| PostgreSQL | BYTEA          | 1Gb  |