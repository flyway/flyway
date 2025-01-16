---
pill: check.buildPassword
subtitle: flyway.check.buildPassword
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

Password for the build database.

## Type

String

## Default

Whatever is set as your 'flyway.password' (see [password](<Configuration/Environments Namespace/Environment password Setting>) )

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -changes -url="jdbc://url1" -check.buildPassword="mypassword"
```

### TOML Configuration File

```toml
[flyway.check]
buildPassword = "mypassword"
```

### Configuration File

```properties
flyway.check.buildPassword="mypassword"
```
