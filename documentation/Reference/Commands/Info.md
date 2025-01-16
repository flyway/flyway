---
subtitle: Info
---

## Description

Prints the details and status information about all the migrations.

See [Flyway Schema History Table](https://documentation.red-gate.com/display/FD/Flyway+schema+history+table) for more information.

## Usage examples

### Info via command line

```bash
flyway info -infoSinceDate="01/12/2020 13:00"
```

### Info via Maven

```bash
mvn flyway:info
```

### Info via Gradle

```bash
gradle flywayInfo
```

## Parameters

Universal commandline parameters are listed [here](<Command-line Parameters>).

All relevant configuration settings are listed [here](<Configuration/Flyway Namespace>). The settings from these sections can be set as parameters on the `info` command:
* General settings
* Migration location and naming settings
* Flyway schema history settings
* Settings in database-specific namespaces
* Settings in secrets management namespaces

It is also possible to set [environment settings](<Configuration/Environments Namespace>) as parameters.

The following parameters are also available:

| Parameter          | Description                                                                                                                                                                                                                                                                        |
|--------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `infoSinceDate`    | Limits info to show only migrations applied after this date, and any unapplied migrations. Must be in the format `dd/MM/yyyy HH:mm` (e.g. `01/12/2020 13:00`)                                                                                                                      |
| `infoUntilDate`    | Limits info to show only migrations applied before this date. Must be in the format `dd/MM/yyyy HH:mm` (e.g. `01/12/2020 13:00`)                                                                                                                                                   |
| `infoSinceVersion` | Limits info to show only migrations greater than or equal to this version, and any repeatable migrations. (e.g `1.1`)                                                                                                                                                              |
| `infoUntilVersion` | Limits info to show only migrations less than or equal to this version, and any repeatable migrations. (e.g. `1.1`)                                                                                                                                                                |
| `infoOfState`      | Limits info to show only migrations of the provided states. This is a case insensitive, comma-separated list. The valid states can be found at [Migration States](https://documentation.red-gate.com/display/FD/Flyway+schema+history+table).                                      |
| `migrationIds`     | Suppresses all other output and displays a comma-separated list of migration versions for versioned migrations and descriptions for repeatable migrations. This is equivalent to the expected input to [`cherryPick`](<Configuration/Flyway Namespace/Flyway Cherry Pick Setting>) |

All parameters are optional, although a target environment must be configured or passed in.

## JSON output format

```json
{
  "schemaVersion": null,
  "schemaName": "public",
  "migrations": [
    {
      "category": "Versioned",
      "version": "1",
      "description": "first",
      "type": "SQL",
      "installedOnUTC": "",
      "state": "Pending",
      "undoable": "No",
      "filepath": "C:\\flyway\\sql\\V1__first.sql",
      "installedBy": "",
      "executionTime": 0
    },
    {
      "category": "Repeatable",
      "version": "",
      "description": "repeatable",
      "type": "SQL",
      "installedOnUTC": "",
      "state": "Pending",
      "undoable": "",
      "filepath": "C:\\flyway\\sql\\R__repeatable.sql",
      "installedBy": "",
      "executionTime": 0
    }
  ],
  "allSchemasEmpty": false,
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "info"
}
```