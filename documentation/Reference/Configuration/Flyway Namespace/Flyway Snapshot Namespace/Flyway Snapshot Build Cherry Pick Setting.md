---
subtitle: flyway.snapshot.buildCherryPick
---

## Description

If snapshot source is `"migrations"`, this specifies list of migrations to migrate the build environment with.

## Type

String array

## Default

`[]`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway snapshot -source="migrations" -buildCherryPick="001"
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a config file, but it is possible:

```toml
[flyway.snapshot]
buildCherryPick = [ "001" ]
```
