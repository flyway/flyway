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

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.ignores.ignoreWhitespace=false
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreWhitespace = true
```
