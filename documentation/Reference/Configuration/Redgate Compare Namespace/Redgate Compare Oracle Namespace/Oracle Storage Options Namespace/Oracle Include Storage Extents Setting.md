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

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.storage]
includeStorageExtents = true
```
