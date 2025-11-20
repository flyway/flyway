---
subtitle: redgateCompare.oracle.options.behavior.ignoreLobStorage
---

## Description

Ignores lob storage details when comparing and deploying schemas.

Note: This option supersedes the following include options with respect to lob storage aspects:
* [`redgateCompare.oracle.options.storage.includeStorageLobsAndVarrays`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Storage Options Namespace/Oracle Include Storage Lobs And Varrays Setting>)
* [`redgateCompare.oracle.options.storage.includeStoragePartitioning`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Storage Options Namespace/Oracle Include Storage Partitioning Setting>)
* [`redgateCompare.oracle.options.storage.includeStorageTablespace`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Storage Options Namespace/Oracle Include Storage Tablespace Setting>)

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreLobStorage=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreLobStorage = true
```
