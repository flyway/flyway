---
subtitle: flyway.generate.target
---

## Description

Sets the direction of script generation by stating the target to use. This must match one of the targets provided to either `diff.source` or `diff.target` from `flyway diff` if specified. This may be useful in scenarios where some changes from a `diff` want to be captured in a migration, and others reverted without recomputing another `diff`.

## Type

String

## Default

Uses the value of `diff.target` if it is specified, otherwise the target of `diff` that was used to generate the artifact.

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -target="schemaModel"
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a config file, but it is possible:

```toml
[flyway.generate]
target = "schemaModel"
```
