---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

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

### Flyway Desktop

This can be set from the connection dialog for databases types supported by Redgate Clone.

### Command-line

```bash
./flyway info -environments.development.resolvers.clone.dataContainerLifetime='1h'
```

### TOML Configuration File

```toml
[environments.development.resolvers.clone]
dataContainerLifetime = "1h"
```
