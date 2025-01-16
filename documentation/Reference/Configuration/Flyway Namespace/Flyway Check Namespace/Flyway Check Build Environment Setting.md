---
pill: check.buildUser
subtitle: flyway.check.buildUser
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

[Environment](<Configuration/Flyway Namespace/Flyway Environment Setting>) for the build database

## Type

String

## Default

`"default_build"`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -changes -environment=env1 -check.buildEnvironment=build1
```

### TOML Configuration File

```toml
[flyway.check]
buildEnvironment = "build1"
```

### Configuration File

```properties
flyway.check.buildEnvironment="build1"
```
