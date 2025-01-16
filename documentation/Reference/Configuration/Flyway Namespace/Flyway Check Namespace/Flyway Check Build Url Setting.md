---
pill: check.buildUrl
subtitle: flyway.check.buildUrl
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

URL for a build database.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -changes -url="jdbc://url1" -check.buildUrl="jdbc://url2"
```

### TOML Configuration File

```toml
[flyway.check]
buildUrl = "jdbc://url2"
```

### Configuration File

```properties
flyway.check.buildUrl="jdbc://url2"
```