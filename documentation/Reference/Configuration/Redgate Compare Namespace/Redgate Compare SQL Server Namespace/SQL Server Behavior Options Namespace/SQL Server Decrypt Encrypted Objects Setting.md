---
subtitle: redgateCompare.sqlserver.options.behavior.decryptEncryptedObjects
---

## Description

When this option is selected, encrypted database objects will be decrypted. This can have a significant performance impact on large databases.

When this option is set, all encrypted objects are decrypted within snapshots or scripts folders.
This option requires sysadmin permissions and does not work with Azure SQL databases.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.options.behavior.decryptEncryptedObjects=false
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.options.behavior]
decryptEncryptedObjects = false
```
