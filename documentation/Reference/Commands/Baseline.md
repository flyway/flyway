---
subtitle: Baseline
---

## Description

Baselines an existing database, excluding all migrations up to and including baselineVersion.

This command and the [`baselineVersion`](<Configuration/Flyway Namespace/Flyway Baseline Version Setting>) setting should be considered together:

- `baseline` records a baseline marker in the [schema history table](<Configuration/Flyway Namespace/Flyway Table Setting>), tagging the database with `baselineVersion`.
- Flyway treats every migration with a version up to and including `baselineVersion` as already applied. Only migrations with a higher version will be applied by [migrate](<Commands/Migrate>).

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
