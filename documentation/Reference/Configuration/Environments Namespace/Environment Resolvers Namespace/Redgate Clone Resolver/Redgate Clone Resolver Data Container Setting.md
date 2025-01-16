---
pill: gcsmProject
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

## Description

The data container to use for the database clone.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog for databases types supported by Redgate Clone.

### Command-line

```bash
./flyway info -environments.development.resolvers.clone.dataContainer='MyContainer'
```

### TOML Configuration File

```toml
[environments.development.resolvers.clone]
dataContainer = "MyContainer"
```
