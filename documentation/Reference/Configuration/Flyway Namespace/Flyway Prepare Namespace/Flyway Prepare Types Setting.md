---
subtitle: flyway.prepare.types
---

## Description

An array of types of script to generate.

## Type

String array

### Valid values

- `"deploy"`
- `"undo"`

## Default

`["deploy"]`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway prepare -types="deploy"
```

### TOML Configuration File

```toml
[flyway.prepare]
types = [ "deploy", "undo" ]
```
