---
subtitle: flyway.init.fromType
---

## Description

The type of project to import.

## Type

String

### Valid values

- `"Conf"` - for upgrading a Flyway .conf file to TOML
- `"SqlSourceControl"` - for importing a SQL Source Control project
- `"SourceControlForOracle"` - for importing a Source Control for Oracle project

## Default

<i>There is no default. If [`from`](<Configuration/Flyway Namespace/Flyway Init Namespace/Flyway Init From Setting>) is specified, this will be derived.</i>

## Usage

### Command-line

```bash
flyway init -projectName=MyProject -databaseType=Sqlite -fromType=Conf
```
