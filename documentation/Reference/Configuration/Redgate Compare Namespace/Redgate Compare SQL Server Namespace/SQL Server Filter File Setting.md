---
subtitle: redgateCompare.sqlserver.filterFile
---

## Description

The path to the SQL Server filter file, containing custom filtering rules for excluding objects from database comparisons.

This will be resolved relative to the [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

`Filter.scpf`

## Usage

### Flyway Desktop

This can't currently be configured from Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.filterFile=Custom.scpf
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver]
filterFile = "Custom.scpf"
```
