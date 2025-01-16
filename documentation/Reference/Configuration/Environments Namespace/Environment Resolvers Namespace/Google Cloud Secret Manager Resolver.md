---
subtitle: environments.*.resolvers.gcsm
---

# Google Cloud Secret Manager

{% include enterprise.html %}

Per-environment Google Cloud Secret Manager secret management configuration.
Values can be inlined in the environment configuration using `${googlesecrets.key}`.

## Settings

| Setting                                                                                                                                                                       | Required | Type   | Description                                       |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|--------|---------------------------------------------------|
| [`project`](<Configuration/Environments Namespace/Environment Resolvers Namespace/Google Cloud Secret Manager Resolver/Google Cloud Secret Manager Resolver Project Setting>) | Yes      | String | The GCSM Project that you are storing secrets in. |

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured.

### Command-line

```bash
./flyway info \
-environment='development' \
-environments.development.url='jdbc:postgresql:${googlesecrets.dbhost}/${googlesecrets.dbname}' \
-environments.development.user='${googlesecrets.username}' \
-environments.development.password='${googlesecrets.password}' \
-environments.development.resolvers.gcsm.project='quixotic-ferret-345678'
```

### TOML Configuration File

```toml
[environments.development.resolvers.gcsm]
project = "quixotic-ferret-345678"

[environments.development]
url = "jdbc:postgresql:${googlesecrets.dbhost}/${googlesecrets.dbname}"
user = "${googlesecrets.username}"
password = "${googlesecrets.password}"
```