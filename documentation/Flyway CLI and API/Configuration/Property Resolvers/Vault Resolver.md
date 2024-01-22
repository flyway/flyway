---
subtitle: Vault
---
# Vault
{% include enterprise.html %}

To resolve values from Hashicorp Vault, configure [Vault token](Configuration/Parameters/Flyway/Vault Token) and [Vault URL](Configuration/Parameters/Flyway/Vault URL).
Values can then be inlined using `${vault.key}`.
For example:
```TOML
[flyway.vault]
url = "http://localhost:8200/v1/"

[environments.default]
url = "jdbc:postgresql:${vault.dbhost}/${vault.dbname}"
user = "${vault.username}"
password = "${vault.password}"
```