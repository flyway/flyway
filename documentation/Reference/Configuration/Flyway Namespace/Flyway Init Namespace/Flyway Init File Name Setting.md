---
subtitle: flyway.init.projectName
---

## Description

The name of the main TOML settings file.
This will be resolved relative to the [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

`"flyway.toml"`

## Usage

### Command-line

```bash
./flyway init -projectName="My Project" -databaseType="sqlserver" -fileName="custom.toml"
```
