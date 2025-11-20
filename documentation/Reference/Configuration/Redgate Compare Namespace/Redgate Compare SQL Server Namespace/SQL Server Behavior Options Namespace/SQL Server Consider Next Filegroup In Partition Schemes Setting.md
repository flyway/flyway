---
subtitle: redgateCompare.sqlserver.options.behavior.considerNextFilegroupInPartitionSchemes
---

## Description

When this option is selected, if a partition scheme contains a next filegroup, the next filegroup is considered in the comparison. The next filegroup does not affect the way in which data is stored.

To ignore next filegroups, clear the check box. Note that we may still change partition scheme filegroups in deployments where the partition scheme needs to change, e.g. because its partition function is changing.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.considerNextFilegroupInPartitionSchemes=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
considerNextFilegroupInPartitionSchemes = false
```
