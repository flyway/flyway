---
subtitle: Repair
---

## Description

Repairs the schema history table.

This will perform the following actions:
- Remove any failed migrations<br/>
  (User objects left behind must still be cleaned up manually)
- Realign the checksums, descriptions and types of the applied migrations with the ones of the available migrations
- Mark all missing migrations as **deleted**
  - As a result, `repair` must be given the same [`locations`](<Configuration/Flyway Namespace/Flyway Locations Setting>) as `migrate`!

For more information, see [Flyway Schema History Table](https://documentation.red-gate.com/display/FD/Flyway+schema+history+table).

## Usage examples

### Repair via command line

```bash
flyway repair
```

### Repair via Maven

```bash
mvn flyway:repair
```

### Repair via Gradle

```bash
gradle flywayRepair
```

## Parameters

Universal commandline parameters are listed [here](<Command-line Parameters>).

All relevant configuration settings are listed [here](<Configuration/Flyway Namespace>). The settings from these sections can be set as parameters on the `repair` command:
* General settings
* Flyway schema history settings
* Validation settings
* Settings in database-specific namespaces
* Settings in secrets management namespaces

It is also possible to set [environment settings](<Configuration/Environments Namespace>) as parameters.

All parameters are optional, although a target environment must be configured or passed in.

## JSON output format

```json
{
  "repairActions": [
    "ALIGNED APPLIED MIGRATION CHECKSUMS"
  ],
  "migrationsRemoved": [],
  "migrationsDeleted": [],
  "migrationsAligned": [
    {
      "version": "1",
      "description": "first",
      "filepath": "C:\\flyway\\sql\\V1__first.sql"
    }
  ],
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "repair"
}
```
