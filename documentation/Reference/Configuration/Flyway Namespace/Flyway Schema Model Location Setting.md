---
subtitle: flyway.schemaModelLocation
redirect_from: Configuration/schemaModelLocation/
---

## Description

The location of the schema model folder.
If not set then this defaults to the folder "schema-model" in the current working directory.

It may be necessary to specify this option if using `schemaModel` as a comparison source or target with the
`diff` command. It may also be necessary to specify this option if using the
`model` command to apply changes to the schema
model.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```bash
./flyway -schemaModelLocation="C:\Users\FlywayUser\Project\schema-model"
```

### TOML Configuration File

```toml
[flyway]
schemaModelLocation = 'C:\Users\FlywayUser\Project\schema-model'
```
