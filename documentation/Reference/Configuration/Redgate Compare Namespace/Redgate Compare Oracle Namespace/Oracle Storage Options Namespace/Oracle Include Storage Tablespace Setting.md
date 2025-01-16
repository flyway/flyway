---
subtitle: redgateCompare.oracle.options.storage.includeStorageTablespace
---

## Description

Includes tablespace of tables and indexes.

Note: Including this options will also compare and script lobs and varrays.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.storage]
includeStorageTablespace = true
```
