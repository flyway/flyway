---
subtitle: flyway.diff.snaphotSchemas
---

## Description

The schemas used for a snapshot comparison source/target.

## Type

String array

## Default

If this is not set, then snapshot schemas will be assumed to match the other side of the comparison.

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway diff -source="env:production" -target="snapshot:production.snp" -snapshotSchemas="schema1"
```

### TOML Configuration File

```toml
[flyway.diff]
snapshotSchemas = [ "schema1" ]
```
