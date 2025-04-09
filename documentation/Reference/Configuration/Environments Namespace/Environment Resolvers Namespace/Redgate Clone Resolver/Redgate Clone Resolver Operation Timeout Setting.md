---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

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

### Flyway Desktop

This can be set from the connection dialog for databases types supported by Redgate Clone.

### Command-line

```bash
./flyway info -environments.development.resolvers.clone.operationTimeout='1h' \
```

### TOML Configuration File

```toml
[environments.development.resolvers.clone]
operationTimeout = "1h"
```
