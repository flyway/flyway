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

### Flyway Desktop

This can't currently be configured from Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.ignoreRules=Custom.scpf
```

### TOML Configuration File

```toml
[redgateCompare.oracle]
ignoreRules = "Custom.scpf"
```
