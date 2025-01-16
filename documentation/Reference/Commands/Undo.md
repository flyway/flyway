---
subtitle: Undo
---

## Description

{% include teams.html %}

Undoes the most recently applied versioned migration.

If `target` is specified, Flyway will attempt to undo versioned migrations in the reverse of their applied order, until it hits
one with a version below the target, or one without a corresponding undo migration. If `group` is active, Flyway will attempt to undo all these migrations within a
single transaction.

If there is no versioned migration to undo, calling undo has no effect.

There is no undo functionality for repeatable migrations. In that case the repeatable migration should be modified to
include the older state that one desires and then reapplied using [migrate](Commands/migrate).

See [Migrations](https://documentation.red-gate.com/display/FD/Migrations) for more information.

## Usage examples

### Undo via command line

```bash
flyway undo
```

### Undo via Maven

```bash
mvn flyway:undo
```

The new database version number is exposed in the `flyway.current` Maven property.

### Undo via Gradle

```bash
gradle flywayUndo
```

## Parameters

Universal commandline parameters are listed [here](<Command-line Parameters>).

All relevant configuration settings are listed [here](<Configuration/Flyway Namespace>). The settings from these sections can be set as parameters on the `undo` command:
* General settings
* Migration location and naming settings
* Migration reading settings
* Migration execution settings
* Flyway schema history settings
* Placeholders
* Settings in database-specific namespaces
* Settings in secrets management namespaces
  
It is also possible to set [environment settings](<Configuration/Environments Namespace>) as parameters.

All parameters are optional, although a target environment must be configured or passed in.

## JSON output format

```json
{
  "initialSchemaVersion": "1",
  "targetSchemaVersion": null,
  "schemaName": "public",
  "undoneMigrations": [
    {
      "version": "1",
      "description": "undoFirst",
      "filepath": "C:\\flyway\\sql\\U1__undoFirst.sql",
      "executionTime": 0
    }
  ],
  "migrationsUndone": 1,
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "undo"
}
```
