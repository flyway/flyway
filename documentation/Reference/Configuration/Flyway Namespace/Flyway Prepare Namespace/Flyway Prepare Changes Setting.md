---
subtitle: flyway.prepare.artifactFilename
---

## Description

An array of change ids for which to fetch diff text.
It is also possible to specify a single argument of
`"-"` to read changes from stdin (this is beneficial when streaming a lot of ids as it avoids path limit).

## Type

String array

## Default

If unspecified, all changes will be used.

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

Specific ids:

```bash
./flyway prepare -changes=123,456
```

Read ids from stdin:

```bash
./flyway prepare -changes=-
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a config file, but it is possible:

```toml
[flyway.prepare]
changes = ["-"]
```
