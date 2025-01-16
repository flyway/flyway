---
subtitle: flyway.generate.artifactFilename
---

## Description

The location of the diff artifact.
If this is a relative path, it will be resolved relative to your [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

Defaults to the value of [
`diff.artifactFilename`](<Configuration/Flyway Namespace/Flyway Diff Namespace/Flyway Diff Artifact Filename Setting>), falling back to
`"%temp%/flyway.artifact.diff"`.

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -artifactFilename=artifact.diff
```

### TOML Configuration File

```toml
[flyway.generate]
artifactFilename = "artifact.diff"
```
