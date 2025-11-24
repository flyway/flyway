---
subtitle: flyway.diff.buildVersion
---

## Description

If diff source or diff target is migrations, this specifies migration version to migrate the build environment to.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway diff -source="schemaModel" -target="migrations" -buildVersion="001"
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a config file, but it is possible:

```toml
[flyway.diff]
buildVersion = [ "001" ]
```
