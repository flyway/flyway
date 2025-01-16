---
subtitle: redgateCompare.oracle.ignoreRules
---

## Description

The path to the Oracle pre-filter file, containing custom filtering rules for excluding objects from database comparisons.
Objects will be excluded before database comparison takes place, yielding a potential performance improvement.

This will be resolved relative to the [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

`IgnoreRules.scpf`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can't currently be configured from Flyway Desktop, although it will be honoured.

### TOML Configuration File

```toml
[redgateCompare.oracle]
ignoreRules = "Custom.scpf"
```
