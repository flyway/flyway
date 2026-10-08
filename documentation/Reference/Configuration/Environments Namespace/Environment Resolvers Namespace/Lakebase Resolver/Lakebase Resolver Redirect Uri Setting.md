---
subtitle: environments.*.resolvers.lakebase.redirectUri
---

## Description

The redirect URI the browser returns to once the Databricks sign-in completes.

Flyway signs in through the published `databricks-cli` OAuth application, and Databricks rejects any redirect that
application has not registered.

## Type

String

## Default

`http://localhost:8020`

## Usage

### Command-line

```bash
./flyway info -environments.development.resolvers.lakebase.redirectUri='http://localhost:8020'
```

### TOML Configuration File

```toml
[environments.development.resolvers.lakebase]
redirectUri = "http://localhost:8020"
```
