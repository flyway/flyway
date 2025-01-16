---
subtitle: flywayDesktop.schemaModel
---

> [!Warning]
> This property has been deprecated. It is replaced
> by [schemaModelLocation](<Configuration/Flyway Namespace/Flyway Schema Model Location Setting>)

## Description

The location of your shadow schema model. <!-- TODO link -->
If this is a relative path, it will be resolved relative to your [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

`"schema-model"`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This will always be set to the default value when a project is created using Flyway Desktop, though Flyway Desktop will
honour it if it is manually changed in the settings file.
When Flyway Desktop updates the schema model, it will create it if it does not exist.

### TOML Configuration File

```toml
[flywayDesktop]
schemaModel = "schema-model"
```
