---
subtitle: Vault
---
# Vault
{% include enterprise.html %}

To use the Vault property resolver, Vault details must be configured per-environment. This expects 4 parameters:
 - The `url` of the Vault API endpoint.
 - The `token` to authenticate with Vault.
 - `engineName` - the name of the secret engine. E.g. `secret`, or `database`.
 - `engineVersion` - the version of the secret engine. E.g. `v1`, or `v2`.

Values can then be inlined using `${vault.path/to/secret/key}`.

For example:
```TOML
[environments.development]
url = "jdbc:postgresql://localhost/postgres"
user = "postgres"
password = "${vault.flyway/password}"

[environments.development.resolvers.vault]
url = "http://localhost:8200/v1"
token = "abc.1234567890"
engineName = "secret"
engineVersion = "v2"
```