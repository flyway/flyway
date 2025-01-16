---
subtitle: flyway.snapshot.rebuild
---

## Description

If snapshot source is `"migrations"`, forces a reprovision (rebuild) of the build environment.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway snapshot -source="migrations" -rebuild=true
```

### TOML Configuration File

```toml
[flyway.snapshot]
rebuild = true
```
