---
subtitle: flyway.add.nameOnly
---

## Description

Only return the name of the migration script, without creating the empty file.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway add -nameOnly=true
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a configuration file, but it is possible:

```toml
[flyway.add]
nameOnly = false
```
