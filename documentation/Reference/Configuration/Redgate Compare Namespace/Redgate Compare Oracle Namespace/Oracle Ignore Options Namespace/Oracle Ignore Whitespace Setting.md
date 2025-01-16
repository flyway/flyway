---
subtitle: redgateCompare.oracle.options.behavior.ignoreWhitespace
---

## Description

Ignores whitespace (e.g. newlines, tabs, spaces) in PL/SQL blocks when comparing databases.

Note: whitespace in object names isn't ignored. White space PL/SQL blocks won't be ignored when the databases are deployed.

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
[redgateCompare.oracle.options.ignores]
ignoreWhitespace = true
```
