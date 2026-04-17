---
subtitle: redgateCompare.oracle.options.behavior.objectExistenceChecks
---
- **Status:** {% include preview.html %}

## Description

Adds existence checks to scripts to improve their ability to be re-run multiple times without encountering “already exists” errors.

When this option is active the script will include an existence check before attempting to create an object.

Creation statements for the following objects are supported:
- Indexes

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway generate -redgateCompare.oracle.options.behavior.objectExistenceChecks=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
objectExistenceChecks = true
```
