---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreDatabaseAndServerNameInSynonyms
---

## Description

Ignores the database and server name in synonyms when comparing databases.

## Type

Boolean

## Default

`true`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreDatabaseAndServerNameInSynonyms = true
```
