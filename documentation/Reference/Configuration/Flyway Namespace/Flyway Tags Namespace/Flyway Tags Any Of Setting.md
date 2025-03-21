---
subtitle: flyway.tags.anyOf
---

- **Status:** Preview

{% include enterprise.html %}

## Description

A tag condition containing a list of tags to filter migrations by, if specified. 
A migration's tags must match at least one of the given tags in `anyOf` in order for the migration to be selected.
When this option is combined with `allOf` or `noneOf`, then a migration must meet all conditions to be selected.

Example:
```toml
[flyway.tags.definitions]
"0.1" = ["tagA"]
"0.2" = ["tagB"]
"0.3" = ["tagC", "tagB"]
```

Specifying the condition `-tags.anyOf=tagB,tagC` would select versions `0.2` and `0.3`.

## Type

String array

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```powershell
./flyway "-tags.anyOf=tagA,tagB,tagC" info
```

### TOML Configuration File

```toml
[flyway.tags]
anyOf = ["tagA", "tagB", "tagC"]
```

