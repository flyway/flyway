---
subtitle: redgateCompare.sqlserver.options.behavior.addNoPopulationToFulltextIndexes
---

## Description

Adds the `NO POPULATION` clause to all new fulltext indexes, so that you can control when the first population occurs, rather than letting them populate at deployment time.

Note that this will automatically turn change tracking off for those fulltext indexes, as `NO POPULATION` is incompatible with other change tracking values.

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
[redgateCompare.sqlserver.options.behavior]
addNoPopulationToFulltextIndexes = true
```
