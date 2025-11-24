---
subtitle: flyway.diff.includeFlywayObjects
---

## Description

Should the diff include flyway objects (e.g. schema history table).

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway diff -source="env:development" -target="schemaModel" -includeFlywayObjects=true
```

### TOML Configuration File

```toml
[flyway.diff]
includeFlywayObjects = true
```
