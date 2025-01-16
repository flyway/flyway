---
subtitle: flyway.diff.rebuild
---

## Description

If diff source or diff target is migrations, forces a reprovision (rebuild) of the build environment.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway diff -source="schemaModel" -target="migrations" -rebuild=true
```

### TOML Configuration File

```toml
[flyway.diff]
rebuild = true
```
