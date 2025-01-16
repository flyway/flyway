---
subtitle: databaseType
---

## Description

The primary database flavour being managed in the project.

This is currently only used by Flyway Desktop.

## Type

String

## Default

Not applicable.

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This will need to be supplied when creating a project in Flyway Desktop.

If this is missing when opening a project in Flyway Desktop, Flyway Desktop will prompt for it.

This setting is not configurable through Flyway Desktop.

### Command-line

This can't be overridden on the command line, but it can be set using [`flyway init`](<Commands/Init>)

```bash
./flyway init -databaseType="oracle"
```

### TOML Configuration File

```toml
databaseType = "oracle"
```
