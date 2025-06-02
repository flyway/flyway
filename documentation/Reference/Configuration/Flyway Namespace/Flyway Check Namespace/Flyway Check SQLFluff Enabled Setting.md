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

### Native `SQLFluff` Engine

{% include enterprise.html %}

Flyway Enterprise now includes a built-in `SQLFluff` engine, allowing you to run `SQLFluff` without requiring a separate Python or `SQLFluff` installation in your local environment.

You can enable this feature by setting the environment variable `FLYWAY_ENV_NATIVE_SQLFLUFF` to `true`. By default, this variable is set to `false`. Therefore, if you prefer to use your own local `SQLFluff` installation instead of the built-in version, no action is required.

Note: The overall `sqlfluffEnabled` flag must be enabled for the built-in `SQLFluff` engine to work, even if `FLYWAY_ENV_NATIVE_SQLFLUFF` is set to `true`.