---
subtitle: flyway.tags.failOnMissing
---

- **Status:** Preview

{% include enterprise.html %}

## Description

By default, flyway will raise an error if a migration referenced by `tags.definition` does not exist. 
The `failOnMissing` setting can be set to `false` to ignore referenced migrations rather than raise an error.

## Type

Boolean

## Default

<i>true</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```powershell
./flyway "-tags.failOnMissing=false" info
```

### TOML Configuration File

```toml
[flyway.tags]
failOnMissing = false
```
