---
subtitle: Dapr
---
# Dapr
{% include enterprise.html %}

Once Dapr has been configured by setting [Dapr URL](Configuration/Parameters/Flyway/Dapr Url), values can be loaded from Dapr using the following syntax: `${dapr.key}`.
For example:
```TOML
[flyway.dapr]
url = "http://localhost:3500/v1.0/secrets/my-secrets-store"

[environments.default]
url = "jdbc:postgresql:${dapr.dbhost}/${dapr.dbname}"
user = "${dapr.username}"
password = "${dapr.password}"
```