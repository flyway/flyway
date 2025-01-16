---
pill: gcsmProject
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

## Description

The Redgate Clone server URL.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog for databases types supported by Redgate Clone.

### Command-line

```bash
./flyway info -environments.development.url='${clone.url}databaseName=my-database'
```

### TOML Configuration File

```toml
[environments.development.resolvers.clone]
url = "https://clone.red-gate.com:1234/cloning-api"
```
