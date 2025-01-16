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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.ignores]
ignoreInternallyUsedMicrosoftExtendedProperties = true
```
