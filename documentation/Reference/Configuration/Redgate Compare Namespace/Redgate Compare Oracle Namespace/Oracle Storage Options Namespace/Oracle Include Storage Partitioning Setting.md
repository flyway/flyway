---
subtitle: redgateCompare.oracle.options.storage.includeStoragePartitioning
---

## Description

Includes partitioning of tables and indexes.

Note: including this options will also compare and script lobs and varrays.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.storage.includeStoragePartitioning=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.storage]
includeStoragePartitioning = true
```
