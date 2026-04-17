---
subtitle: environments.*.resolvers.gcsm.project
---

{% include enterprise.html %}

{% include deprecation.html %}

**Note:** Redgate Clone has been removed from Flyway Desktop and is deprecated in Flyway Engine. This feature will be removed in a future version.

## Description

The Redgate Clone server URL.

## Type

String

## Default

<i>none</i>

## Usage

### Command-line

```bash
./flyway info -environments.development.url='${clone.url}databaseName=my-database'
```

### TOML Configuration File

```toml
[environments.development.resolvers.clone]
url = "https://clone.red-gate.com:1234/cloning-api"
```
