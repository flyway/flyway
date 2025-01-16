---
subtitle: redgateCompare.oracle.options.behavior.detectRenamedColumns
---

## Description

Attempts to identify renamed columns by matching the strings, position in the table, and datatype.

A renamed column will be identified if any of the following apply (by order of priority):

1. The names are the same (case-insensitive)
2. The target column contains the whole of the source string (eg "Company" in "CompanyName")
3. The columns are the same ordinal and the same type, and the names are similar
4. The columns have the same type, and the names are similar

Note: detecting renames isn't completely reliable. Some renames won't be identified, and there may be some false positives.

## Type

Boolean

## Default

`true`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
detectRenamedColumns = true
```
