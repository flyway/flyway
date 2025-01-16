---
subtitle: flyway.init.from
---

## Description

The path of a project to import from.
This will be resolved relative to the [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

Defaults to the [working directory](<Command-line Parameters/Working Directory Parameter>) if [`fromType`](<Configuration/Flyway Namespace/Flyway Init Namespace/Flyway Init From Type Setting>) is specified.

## Usage

### Command-line

```bash
flyway init -projectName=MyProject -databaseType=Sqlite -from=flyway.conf
```
