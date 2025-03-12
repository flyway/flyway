---
subtitle: Baseline
---

## Description

Baselines an existing database, excluding all migrations up to and including baselineVersion.

See [Baselines](https://documentation.red-gate.com/display/FD/Baselines) for more information.

## Usage examples

### Baseline via command line

```bash
flyway baseline
```

### Baseline via Maven

```bash
mvn flyway:baseline
```

### Baseline via Gradle

```bash
gradle flywayBaseline
```

## Parameters

Universal commandline parameters are listed [here](<Command-line Parameters>).

All relevant configuration settings are listed [here](<Configuration/Flyway Namespace>). The settings from these sections can be set as parameters on the `baseline` command:
* General settings
* Flyway schema history settings
* Baseline settings
* Settings in database-specific namespaces
* Settings in secrets management namespaces

It is also possible to set [environment settings](<Configuration/Environments Namespace>) as parameters.

All parameters are optional, although a target environment must be configured or passed in.

## JSON output format

```json
{
  "successfullyBaselined": true,
  "baselineVersion": "1",
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "baseline"
}
```

## Error codes

This command can produce the following error codes:
- [Generic error codes](<Exit codes and error codes/General error codes>)
