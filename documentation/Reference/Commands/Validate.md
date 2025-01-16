---
subtitle: Validate
---

## Description

Validates the applied migrations against the available ones.

Validation fails if
- differences in migration names, types or checksums are found
- versions have been applied that aren't resolved locally anymore
- versions have been resolved that haven't been applied yet

Validate works by storing a checksum (CRC32 for SQL migrations) when a migration is executed. The validate mechanism checks if the migration locally still has the same checksum as the migration already executed in the database.

See [Flyway Schema History Table](https://documentation.red-gate.com/display/FD/Flyway+schema+history+table) for more information.

## Usage examples

### Validate via command line

```bash
flyway validate
```

### Validate via Maven

```bash
mvn flyway:validate
```

Note that default phase is `pre-integration-test`.

### Validate via Gradle

```bash
gradle flywayValidate
```

## Parameters

Universal commandline parameters are listed [here](<Command-line Parameters>).

All relevant configuration settings are listed [here](<Configuration/Flyway Namespace>). The settings from these sections can be set as parameters on the `validate` command:
* General settings
* Migration location and naming settings
* Flyway schema history settings
* Validation settings
* Settings in database-specific namespaces
* Settings in secrets management namespaces

It is also possible to set [environment settings](<Configuration/Environments Namespace>) as parameters.

All parameters are optional, although a target environment must be configured or passed in.

## JSON output format

```json
{
  "errorDetails": null,
  "invalidMigrations": [],
  "validationSuccessful": true,
  "validateCount": 2,
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "validate"
}
```