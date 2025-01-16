---
subtitle: flyway.snapshot.buildEnvironment
---

## Description

If snapshot source is `"migrations"`, this specifies the environment to use as the build environment.
Must match the id of an environment specified in [environments](<Configuration/Environments Namespace>).

## Type

String

## Default

<i>none - this is a required parameter when source of `snapshot` command is `"migrations"`</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway snapshot -source="migrations" -buildEnvironment="shadow"
```

### TOML Configuration File

```toml
[flyway.snapshot]
buildEnvironment = "shadow"
```
