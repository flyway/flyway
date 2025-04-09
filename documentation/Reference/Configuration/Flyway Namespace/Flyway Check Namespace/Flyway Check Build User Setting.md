---
subtitle: flyway.check.buildUser
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

Username for the build database

## Type

String

## Default

Whatever is set as your 'flyway.user'

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -changes -url="jdbc://url1" -check.buildUser="sa"
```

### TOML Configuration File

```toml
[flyway.check]
buildUser = "sa"
```

### Configuration File

```properties
flyway.check.buildUser="sa"
```
