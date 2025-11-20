---
subtitle: flyway.check.sqlfluffEnabled
---

{% include commandlineonly.html %}

## Description

You can configure this feature flag to enable or disable the `SQLFluff` Engine for code analysis

## Type

Boolean

## Default

`true`

## Usage

### Command-line

```powershell
./flyway check -code -check.sqlfluffEnabled=false
```

### TOML Configuration File

```toml
[flyway.check]
sqlfluffEnabled = false
```

### Configuration File

```properties
flyway.check.sqlfluffEnabled=false
```

### Redgate Bundle `SQLFluff` Engine

{% include enterprise.html %}

Flyway Enterprise now includes a built-in `SQLFluff` engine, allowing you to run `SQLFluff` without requiring a separate Python or `SQLFluff` installation in your local environment.

To use your own `SQLFluff` installation, set the environment variable `FLYWAY_ENV_NATIVE_SQLFLUFF` to `false` to disable this feature.