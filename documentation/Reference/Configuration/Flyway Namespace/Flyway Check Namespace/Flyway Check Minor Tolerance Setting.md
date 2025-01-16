---
pill: check.minorTolerance
subtitle: flyway.check.minorTolerance
---

{% include teams.html %}

{% include commandlineonly.html %}

## Description

You can configure your pipeline to fail when specified static code analysis rules beyond a given tolerance level are violated.

`minorTolerance` sets the number of minor rules violations to be tolerated before throwing an error

If the total number of [`minorRules`](<Command-line Parameters/Check Namespace/Flyway Check Minor Rules Setting) violations exceeds the `minorTolerance`, Flyway will fail.

## Type

Integer

## Default

There is no maximum tolerance (i.e. violations will not cause a failure)

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced Parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -code -check.minorTolerance=7
```

### TOML Configuration File

```toml
[flyway.check]
minorTolerance = 7
```

### Configuration File

```properties
flyway.check.minorTolerance=7
```
