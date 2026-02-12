---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

{% include deprecation.html %}

**Note:** Redgate Clone has been removed from Flyway Desktop and is deprecated in Flyway Engine. This feature will be removed in a future version.

## Description

The data container to use for the database clone.

## Type

String

## Default

<i>none</i>

## Usage

### Command-line

```bash
./flyway info -environments.development.resolvers.clone.dataContainer='MyContainer'
```

### TOML Configuration File

```toml
[environments.development.resolvers.clone]
dataContainer = "MyContainer"
```
