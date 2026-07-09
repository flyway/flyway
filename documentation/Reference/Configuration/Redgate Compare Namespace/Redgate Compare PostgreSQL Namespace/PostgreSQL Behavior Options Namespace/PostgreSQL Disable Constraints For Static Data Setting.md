---
subtitle: redgateCompare.postgresql.options.behavior.disableConstraintsForStaticData
---

## Description

Disables constraints for static data changes in your migration scripts such as foreign key constraint checks. 

When enabled, generated migration scripts which include static data will be wrapped with `SET session_replication_role = 'replica'` and `SET session_replication_role = 'origin';`

Note that this option: 
- Requires the deploying role to be a superuser
- Disables all triggers (Not just Foreign Key checks) for the session

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't currently be set from Flyway Desktop, although it will be honored.

### Command-line

```powershell
./flyway generate -redgateCompare.postgresql.options.behavior.disableConstraintsForStaticData=true
```

### TOML Configuration File

```toml
[redgateCompare.postgresql.options.behavior]
disableConstraintsForStaticData = true
```
