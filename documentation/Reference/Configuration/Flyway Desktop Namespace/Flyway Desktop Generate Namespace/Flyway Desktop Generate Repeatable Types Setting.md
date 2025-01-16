---
subtitle: flywayDesktop.generate.undoScripts
---

> [!Warning]
> This property has been deprecated. It is replaced
> by [repeatableObjectTypes](<Configuration/Flyway Namespace/Flyway Generate Namespace/Flyway Generate Repeatable Object Types Setting>)

> [!Important]
> This is not currently recommended for databases other than Oracle.
> The generated repeatable migrations will get executed in alphabetical order on `migrate`, which may not account for
> any dependencies between them.

## Description

List of object types for which to generate repeatable migrations.
A sensible list of object types for Oracle might
be ["Function", "Package", "PackageBody", "Procedure", "Trigger", "Type", "MaterializedView", "View"]

## Type

String array

## Default

`[]`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This setting can't currently be configured within Flyway Desktop.

### TOML Configuration File

```toml
[flywayDesktop.generate]
repeatableTypes = true
```
