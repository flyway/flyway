---
subtitle: redgateCompare.oracle.options.storage.includeStorageExtents
---

## Description

Include storage extents `INITIAL`, `NEXT`, `MINEXTENTS` and `MAXEXTENTS`.

Note: difference in `INITIAL` extents are not marked as a difference if the object already exists in both databases.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.storage.includeStorageExtents=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.storage]
includeStorageExtents = true
```
