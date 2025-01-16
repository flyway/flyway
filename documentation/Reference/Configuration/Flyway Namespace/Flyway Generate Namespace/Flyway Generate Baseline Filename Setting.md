---
subtitle: flyway.generate.baselineFilename
---

## Description

The filename (or full path) to use for the generated baseline migration.
Mutually exclusive with `generate.description` & `generate.version`.
If this is a relative path, it will be resolved relative to your [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -baselineFilename=B001__baseline.sql
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a config file, but it is possible:

```toml
[flyway.generate]
baselineFilename = "B001__baseline.sql"
```
