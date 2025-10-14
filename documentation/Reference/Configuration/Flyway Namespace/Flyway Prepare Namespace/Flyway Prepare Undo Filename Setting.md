---
subtitle: flyway.prepare.undoFilename
---

## Description

The path to the undo script which can revert the changes that will be deployed by the deployment script.

Note that when undoing a state based deployment, the undo script will revert the schema and static data changes but
there may be a risk of data loss if some of the schema changes are destructive.

## Type

String

## Default

`deployments/DU__undo.sql`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway prepare -undoFilename="undo.sql"
```

### TOML Configuration File

```toml
[flyway.prepare]
undoFilename = "undo.sql"
```
