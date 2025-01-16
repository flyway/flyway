---
subtitle: flyway.add.location
---

## Description

The location to generate the migration to.
If this is a relative path, it will be resolved relative to your [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

<i>The first filesystem location specified by [`flyway.locations`](<Configuration/Flyway Namespace/Flyway Locations Setting>)< Namespace/Flyway Locations Setting>)<i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway add -location="migrations"
```

### TOML Configuration File

It is unlikely to be desirable to specify this in a configuration file, but it is possible:

```toml
[flyway.add]
location = "migrations"
```
