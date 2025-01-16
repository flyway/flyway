---
pill: check.majorTolerance
subtitle: flyway.check.majorTolerance
---

{% include teams.html %}

{% include commandlineonly.html %}

## Description

You can configure your pipeline to fail when specified static code analysis rules beyond a given tolerance level are violated.

`majorTolerance` sets the number of major rules violations to be tolerated before throwing an error

If the total number of [`majorRules`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Major Rules Setting>) violations exceeds the `majorTolerance`, Flyway will fail.

## Type

Integer

## Default

<i>There is no maximum tolerance (i.e. violations will not cause a failure)</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -code -check.majorTolerance=3
```

### TOML Configuration File

```toml
[flyway.check]
majorTolerance = 3
```

### Configuration File

```properties
flyway.check.majorTolerance=3
```
