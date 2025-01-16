---
pill: gcsmProject
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

## Description

The GCSM Project that you are storing secrets in

Example: `quixotic-ferret-345678`

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```bash
./flyway info -environments.development.resolvers.gcsm.project='quixotic-ferret-345678'
```

### TOML Configuration File

```toml
[environments.development.resolvers.gcsm]
project = "quixotic-ferret-345678"
```
