---
subtitle: flyway.check.scope
---

{% include commandlineonly.html %}

## Description

If this parameter is specified, Flyway will use it to determine the scope of migration files to include in code analysis.

__Note:__ Only the following values are valid for this parameter (case-insensitive):
- DEFAULT
- ALL
- PENDING

## Type

String

## Default

If not specified, Flyway determines the scope based on the presence of a connection URL. If a connection URL is provided, only PENDING migrations are included; otherwise, ALL migrations are analyzed.

## Usage

### Command-line

```powershell
./flyway check -code -check.scope=ALL
```

### TOML Configuration File

```toml
[flyway.check]
scope = "ALL"
```
