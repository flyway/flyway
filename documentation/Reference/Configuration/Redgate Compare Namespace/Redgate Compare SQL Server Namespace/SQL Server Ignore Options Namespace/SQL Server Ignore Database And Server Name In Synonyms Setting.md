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

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreDatabaseAndServerNameInSynonyms=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreDatabaseAndServerNameInSynonyms = false
```
