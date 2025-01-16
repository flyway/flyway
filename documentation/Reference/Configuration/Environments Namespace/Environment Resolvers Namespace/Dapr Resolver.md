---
subtitle: environments.*.resolvers.dapr
---

{% include enterprise.html %}

Per-environment Dapr secret management configuration.
Values can be inlined in the environment configuration using `${dapr.key}`.

## Settings

| Setting                                                                                                                 | Required | Type   | Description                            |
|-------------------------------------------------------------------------------------------------------------------------|----------|--------|----------------------------------------|
| [`url`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Dapr Resolver/Dapr Resolver URL Setting>) | Yes      | String | The REST API URL of your Dapr sidecar. |


## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured.

### Command-line

```bash
./flyway info \
-environment='development' \
-environments.development.url='jdbc:postgresql:${dapr.dbhost}/${dapr.dbname}' \
-environments.development.user='${dapr.username}' \
-environments.development.password='${dapr.password}' \
-environments.development.resolvers.dapr.url="http://localhost:3500/v1.0/secrets/my-secrets-store"
```

### TOML Configuration File

```toml
[environments.development.resolvers.dapr]
url = "http://localhost:3500/v1.0/secrets/my-secrets-store"

[environments.development]
url = "jdbc:postgresql:${dapr.dbhost}/${dapr.dbname}"
user = "${dapr.username}"
password = "${dapr.password}"
```