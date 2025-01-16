---
pill: daprUrl
subtitle: flyway.daprUrl
---

{% include enterprise.html %}

## Description

The REST API URL of your [Dapr](https://docs.dapr.io/developing-applications/building-blocks/secrets/secrets-overview/) sidecar,

Example: `http://localhost:3500/v1.0/secrets/my-secrets-store`

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```bash
./flyway info -environments.development.resolvers.dapr.url="http://localhost:3500/v1.0/secrets/my-secrets-store"
```

### TOML Configuration File

```toml
[environments.development.resolvers.dapr]
url = "http://localhost:3500/v1.0/secrets/my-secrets-store"
```
