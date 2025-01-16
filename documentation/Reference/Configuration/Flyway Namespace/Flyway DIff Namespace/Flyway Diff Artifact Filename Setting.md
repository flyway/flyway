---
subtitle: flyway.diff.artifactFilename
---

## Description

The output location of the diff artifact.
If this is a relative path, it will be resolved relative to your [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

Boolean

## Default

`"%temp%/flyway.artifact.diff"`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway diff -source="schemaModel" -target="migrations" -artifactFilename=artifact.diff
```

### TOML Configuration File

```toml
[flyway.diff]
artifactFilename = "artifact.diff"
```
