---
subtitle: id
---

## Description

A unique identifier for the project.
This will be generated automatically when running [`flyway init`](<Commands/Init>) or creating a Flyway project through
Flyway Desktop.
This should not be altered.

## Type

String

## Default

Not applicable.

## Usage

This setting can't be configured other than in a TOML configuration file

### Flyway Desktop

This will be generated automatically when creating a Flyway project through Flyway Desktop.

If this is missing when opening a project in Flyway Desktop, Flyway Desktop will prompt to fill in missing information
and then automatically regenerate it.

This setting is not configurable through Flyway Desktop.

### TOML Configuration File

```toml
id = "abc"
```

