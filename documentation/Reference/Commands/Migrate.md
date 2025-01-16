---
subtitle: Migrate
---

## Description

Migrates the schema to the latest version. Flyway will create the schema history table automatically if it doesn't exist.

See [Migrations](https://documentation.red-gate.com/display/FD/Migrations) for more information.

## Usage examples

### Migrate via command line

```bash
flyway migrate
```

### Migrate via Maven

```bash
mvn flyway:migrate
```

Note that default phase is `pre-integration-test`.
The new database version number is exposed in the `flyway.current` Maven property.

### Migrate via Gradle

```bash
gradle flywayMigrate
```

## Parameters

Universal commandline parameters are listed [here](<Command-line Parameters>).

All relevant configuration settings are listed [here](<Configuration/Flyway Namespace>). The settings from these sections can be set as parameters on the `migrate` command:
* General settings
* Migration location and naming settings
* Migration reading settings
* Migration execution settings
* Flyway schema history settings
* Flyway Pipelines integration settings
* Baseline settings
* Placeholders
* Settings in database-specific namespaces
* Settings in secrets management namespaces

It is also possible to set [environment settings](<Configuration/Environments Namespace>) as parameters.

All parameters are optional, although a target environment must be configured or passed in.

## JSON output format

```json
{
  "initialSchemaVersion": null,
  "targetSchemaVersion": "1",
  "schemaName": "public",
  "migrations": [
    {
      "category": "Versioned",
      "version": "1",
      "description": "first",
      "type": "SQL",
      "filepath": "C:\\flyway\\sql\\V1__first.sql",
      "executionTime": 0
    },
    {
      "category": "Repeatable",
      "version": "",
      "description": "repeatable",
      "type": "SQL",
      "filepath": "C:\\flyway\\sql\\R__repeatable.sql",
      "executionTime": 0
    }
  ],
  "migrationsExecuted": 2,
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "migrate"
}
```
