---
subtitle: redgateCompare.oracle.options.storage.includeStorageCompression
---

## Description

Include compression clause.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.storage.includeStorageCompression=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.storage]
includeStorageCompression = true
```
