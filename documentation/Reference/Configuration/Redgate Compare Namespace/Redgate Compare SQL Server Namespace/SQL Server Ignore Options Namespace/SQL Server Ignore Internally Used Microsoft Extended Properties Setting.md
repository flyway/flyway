---
subtitle: redgateCompare.sqlserver.options.ignores.ignoreInternallyUsedMicrosoftExtendedProperties
---

## Description

Makes scripting out of objects ignore the extended properties used internally by the SSMS designer (e.g. MS_DiagramPaneCount, MS_DiagramPane1, MS_DiagramPane2, MS_DiagramPane3, etc).

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.ignores.ignoreInternallyUsedMicrosoftExtendedProperties=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreInternallyUsedMicrosoftExtendedProperties = true
```
