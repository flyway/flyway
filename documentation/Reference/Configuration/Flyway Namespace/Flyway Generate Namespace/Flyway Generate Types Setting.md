---
subtitle: flyway.generate.types
---

## Description

An array of types of script to generate.

## Type

String array

### Valid values

- `"versioned"`
- `"undo"`
- `"baseline"`

## Default

`["versioned"]`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -types="versioned"
```

### TOML Configuration File

```toml
[flyway.generate]
types = [ "versioned", "undo" ]
```
