---
subtitle: redgateCompare.sqlserver.options.behavior.writeAssembliesAsDlls
---

## Description

CLR Assembly objects will be written in their true DLL form rather than a sql script containing a binary blob in hex.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.writeAssembliesAsDlls=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
writeAssembliesAsDlls = true
```
