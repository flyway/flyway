---
subtitle: flyway.tags.noneOf
---

- **Status:** Preview

{% include enterprise.html %}

## Description

A tag condition containing a list of tags to filter migrations by, if specified. 
A migration's tags must contain none of the given tags in `noneOf` in order for the migration to be selected.
When this option is combined with `anyOf` or `allOf`, then a migration must meet all conditions to be selected.

Example:
```toml
[flyway.tags.definitions]
"0.1" = ["tagA"]
"0.2" = ["tagB"]
"0.3" = ["tagC", "tagB"]
```

Specifying the condition `-tags.noneOf=tagB,tagC` would select only version `0.1`.

## Type

String array

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```powershell
./flyway "-tags.noneOf=tagA,tagB,tagC" info
```

### TOML Configuration File

```toml
[flyway.tags]
noneOf = ["tagA", "tagB", "tagC"]
```

