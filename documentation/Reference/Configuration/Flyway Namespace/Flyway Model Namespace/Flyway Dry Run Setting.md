---
subtitle: flyway.model.dryRun
---

## Description

When set to true, the list of files that would be updated is returned, along with the dependency information and any
warnings, but no update is performed.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop.

### Command-line

```bash
./flyway model -dryRun=true
```

### TOML Configuration File

```toml
[flyway.model]
dryRun = true
```
