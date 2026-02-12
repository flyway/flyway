---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

{% include deprecation.html %}

**Note:** Redgate Clone has been removed from Flyway Desktop and is deprecated in Flyway Engine. This feature will be removed in a future version.

## Description

The amount of time to wait for Redgate Clone operations to complete.

## Type

String

### Format

This takes the form of a number optionally followed by a time unit, `s`, `m`, `h`, or `d`.

If no time unit is specified, seconds are assumed.

## Default

`5m`

## Usage

### Command-line

```bash
./flyway info -environments.development.resolvers.clone.operationTimeout='1h' \
```

### TOML Configuration File

```toml
[environments.development.resolvers.clone]
operationTimeout = "1h"
```
