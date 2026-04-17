---
subtitle: redgateCompare.sqlserver.data.options.comparison.compressTemporaryFiles
---

## Description

Compresses the temporary files that are generated while performing the comparison. This reduces the possibility of running out of temporary disk space when comparing very large databases.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.comparison.compressTemporaryFiles=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.comparison]
compressTemporaryFiles = true
```
