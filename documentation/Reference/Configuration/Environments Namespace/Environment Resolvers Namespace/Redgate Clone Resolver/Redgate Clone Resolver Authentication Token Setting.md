---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

{% include deprecation.html %}

**Note:** Redgate Clone has been removed from Flyway Desktop and is deprecated in Flyway Engine. This feature will be removed in a future version.

## Description

The token required for authenticating with the Redgate Clone Server.

It is recommended to store this as a secret and resolve it using an appropriate [property resolver](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers).

## Type

String

## Default

<i>none</i>

## Usage

### Command-line

```bash
./flyway info -environments.development.resolvers.clone.authenticationToken='${localSecret.RedgateCloneToken}'
```

### TOML Configuration File

```toml
[environments.development.resolvers.clone]
authenticationToken = "${localSecret.RedgateCloneToken}"
```
