---
subtitle: redgateCompare.oracle.options.behavior.includeOnlineIndexCreation
---

## Description

Include the 'ONLINE' clause on index creation scripts.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway generate -redgateCompare.oracle.options.behavior.includeOnlineIndexCreation=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
includeOnlineIndexCreation = true
```
