---
subtitle: flyway.prepare.force
---

## Description

If the deployment script already exists, overwrite it.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway prepare -force=true
```

### TOML Configuration File

```toml
[flyway.prepare]
force = true
```
