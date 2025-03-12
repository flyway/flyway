---
subtitle: Clean
---

## Description

Drops all objects in the configured schemas. If Flyway automatically created them, then the schemas themselves will be dropped when cleaning.

**Do not use against your production DB!** 

Flyway has some default configuration to try and save you from accidents, but it can't interactively ask you if you really intended to do this.   

## Limitations

- [SQL Server - no users will be dropped](<Database Driver Reference/SQL Server Database>)

## Usage examples

### Clean via command line

```bash
flyway clean
```

### Clean via Maven

```bash
mvn flyway:clean
```

Note that default phase is `pre-integration-test`.

### Clean via Gradle

```bash
gradle flywayClean
```

## Parameters

Universal commandline parameters are listed [here](<Command-line Parameters>).

All relevant configuration settings are listed [here](<Configuration/Flyway Namespace>). The settings from these sections can be set as parameters on the `clean` command:
* General settings
* Flyway schema history settings
* Clean settings
* Settings in database-specific namespaces
* Settings in secrets management namespaces

It is also possible to set [environment settings](<Configuration/Environments Namespace>) as parameters.

All parameters are optional, although a target environment must be configured or passed in.

## JSON output format

```json
{
  "schemasCleaned": [
    "public"
  ],
  "schemasDropped": [],
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "clean"
}
```

## Error codes

This command can produce the following error codes:
- [Generic error codes](<Exit codes and error codes/General error codes>)