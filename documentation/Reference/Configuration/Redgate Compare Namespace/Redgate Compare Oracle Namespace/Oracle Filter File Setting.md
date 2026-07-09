.---
subtitle: redgateCompare.oracle.filterFile
---

## Description

The path to the Oracle filter file, containing custom filtering rules for excluding objects from database comparisons.
Objects will be filtered out only after the comparison has taken place so there will be no performance benefit. See [Ignore Rules](<Filter Formats/Oracle Ignore Rules Format>) for pre-filtering. 

This will be resolved relative to the [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

`Filter.scpf`

## Usage

### Flyway Desktop

This can't currently be configured from Flyway Desktop, although it will be honored.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.filterFile=Custom.scpf
```

### TOML Configuration File

```toml
[redgateCompare.oracle]
filterFile = "Custom.scpf"
```
