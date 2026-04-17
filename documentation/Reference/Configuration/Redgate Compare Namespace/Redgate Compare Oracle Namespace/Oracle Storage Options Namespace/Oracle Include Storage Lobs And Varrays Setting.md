---
subtitle: redgateCompare.oracle.options.storage.includeStorageLobsAndVarrays
---

## Description

Include lob and varray clauses.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.storage.includeStorageLobsAndVarrays=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.storage]
includeStorageLobsAndVarrays = true
```
