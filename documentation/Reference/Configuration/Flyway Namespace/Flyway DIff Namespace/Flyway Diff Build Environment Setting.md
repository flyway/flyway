---
subtitle: flyway.diff.buildEnvironment
---

## Description

If diff source or diff target is migrations, this specifies the environment to use as the build environment.
Must match the id of an environment specified in [environments](<Configuration/Environments Namespace>).

## Type

String

## Default

<i>none - this is a required parameter when source or target of `diff` command is `migrations`</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway diff -source="schemaModel" -target="migrations" -buildEnvironment="shadow"
```

### TOML Configuration File

```toml
[flyway.diff]
buildEnvironment = "shadow"
```
