---
subtitle: redgateCompare.oracle.options.behavior.preserveIdentifierCase
---
- **Status:** {% include preview.html %}

## Description

Keeps schema names in deployment scripts in the capitalization Oracle stores them in, instead of converting them to lowercase.

By default, a generated deployment script can refer to the same schema in two different cases. An existence check queries the data dictionary using the stored form (`OWNER = 'ACME'`), while the statement it guards is scripted in lowercase (`EXECUTE IMMEDIATE 'CREATE TABLE acme.table1 ...'`). If you replace the schema name with a placeholder so the script can be deployed to several environments, no single placeholder value can be correct in both places.

With this option active, both refer to the schema the same way, so one placeholder value substitutes correctly throughout the script.

Only the schema name is affected. Object and column names are scripted as before.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway generate -redgateCompare.oracle.options.behavior.preserveIdentifierCase=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.behavior]
preserveIdentifierCase = true
```
