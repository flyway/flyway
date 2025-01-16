---
subtitle: flyway.generate.description
---

## Description

The description part of the migration name.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -description="addTable"
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a configuration file, but it is possible:

```toml
[flyway.generate]
description = "addTable"
```
