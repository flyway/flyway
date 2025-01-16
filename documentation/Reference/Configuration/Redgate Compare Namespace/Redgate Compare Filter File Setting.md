---
subtitle: redgateCompare.filterFile
---

## Description

The path to the Redgate Compare filter file, containing custom filtering rules for excluding objects from database comparisons.

This will be resolved relative to the [working directory](<Command-line Parameters/Working Directory Parameter>).

Note that this format of filter file is not currently supported for SQL Server or Oracle databases, which have dedicated filter file formats.

## Type

String

## Default

`filter.rgf`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can't currently be configured from Flyway Desktop, although it will be honoured.

### TOML Configuration File

```toml
[redgateCompare]
filterFile = "custom.rgf"
```
