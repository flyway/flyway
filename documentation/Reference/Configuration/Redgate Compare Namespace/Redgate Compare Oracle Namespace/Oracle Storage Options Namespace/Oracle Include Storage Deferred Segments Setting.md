---
subtitle: redgateCompare.oracle.options.storage.includeStorageDeferredSegments
---

## Description

Include `DEFERRED SEGMENT CREATION` clause.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.storage.includeStorageDeferredSegments=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.storage]
includeStorageDeferredSegments = true
```
