---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

{% include deprecation.html %}

**Note:** Redgate Clone has been removed from Flyway Desktop and is deprecated in Flyway Engine. This feature will be removed in a future version.

## Description

The lifetime of the data container.

## Type

String

### Format

This takes the form of a number optionally followed by a time unit, `s`, `m`, `h`, or `d`.

If no time unit is specified, seconds are assumed.

`0` can be used to set the lifetime to be unlimited.

## Default

<i>none</i>

## Usage

### Command-line

```bash
./flyway info -environments.development.resolvers.clone.dataContainerLifetime='1h'
```

### TOML Configuration File

```toml
[environments.development.resolvers.clone]
dataContainerLifetime = "1h"
```
