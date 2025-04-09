---
subtitle: flyway.check.changesSource
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

The deployment source to generate a change report from

## Type

String

### Valid values

- `"migrations"`
- `"schemaModel"`

## Default

`"migrations"`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```powershell
./flyway check -changes -url="jdbc://url" -check.changesSource="migrations"
```

### TOML Configuration File

```toml
[flyway.check]
changesSource = "migrations"
```

### Configuration File

```properties
flyway.check.changesSource=migrations
```
