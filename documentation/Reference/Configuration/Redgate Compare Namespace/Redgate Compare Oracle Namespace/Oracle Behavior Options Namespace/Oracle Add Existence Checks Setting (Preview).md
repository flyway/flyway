---
subtitle: redgateCompare.oracle.options.behavior.objectExistenceChecks
---
- **Status:** {% include preview.html %}

## Description

Adds existence checks to scripts to improve their ability to be re-run multiple times without encountering 'already exists' errors.

When this option is active the script will include an existence check before attempting to create or drop an object.

The following objects are supported:
- Functions
- Indexes
- Jobs
- Materialized views
- Materialized view logs
- Packages
- Procedures
- Queues
- Queue tables
- Schedules
- Sequences
- Synonyms
- Tables
- Table columns
- Triggers
- Types
- Views

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
