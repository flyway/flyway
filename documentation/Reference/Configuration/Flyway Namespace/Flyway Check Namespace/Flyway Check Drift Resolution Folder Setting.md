---
subtitle: flyway.check.driftResolutionFolder
---

{% include enterprise.html %}

## Description

The folder in which [drift resolution scripts](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Generate Drift Resolution Scripts Setting>)
are generated when drift is detected.

Relative paths are resolved against your [configured working directory](<Command-line Parameters/Working Directory Parameter>)
if set, and your current working directory otherwise. Absolute paths are used as-is. Any parent folders that do not yet
exist will be created.

If the folder already exists, its contents will be deleted and regenerated each time drift resolution scripts are
produced.

For more information on resolving drift,
see [Checking production environments for drift](https://documentation.red-gate.com/flyway/deploying-database-changes-using-flyway/checking-production-environments-for-drift).

## Type

String

## Default

`drift-resolution`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honored, and it can be configured as an
advanced parameter in operations on the Migrations page.

### Command-line

```powershell
"./flyway check -drift -driftResolutionFolder=drift-resolution"
```

### TOML Configuration File

```toml
[flyway.check]
driftResolutionFolder = "drift-resolution"
```
