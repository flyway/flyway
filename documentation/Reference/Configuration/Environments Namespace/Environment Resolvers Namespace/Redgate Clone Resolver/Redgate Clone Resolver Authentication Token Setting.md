---
pill: gcsmProject
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

## Description

The token required for authenticating with the Redgate Clone Server.

It is recommended to store this as a secret and resolve it using an appropriate [property resolver](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers).

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog for databases types supported by Redgate Clone.

### Command-line

```bash
./flyway info -environments.development.resolvers.clone.authenticationToken='${localSecret.RedgateCloneToken}'
```

### TOML Configuration File

```toml
[environments.development.resolvers.clone]
authenticationToken = "${localSecret.RedgateCloneToken}"
```
