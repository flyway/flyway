---
subtitle: flyway.check.minorRules
---

{% include teams.html %}

{% include commandlineonly.html %}

## Description
***This is deprecated - please look at using the `check.code.failOnError` mechanism described in [Code Analysis Rules](<Code Analysis Rules>)***

You can configure your pipeline to fail when specified static code analysis rules beyond a given tolerance level are violated.

`minorRules` should contain an array of [rules](Code Analysis Rules) which are considered to be minor.

If the total number of `minorRules` violations exceeds the [`minorTolerance`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Minor Tolerance Setting>), Flyway will fail.

## Type

String array

## Default

No rules are regarded as minor violations

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -code -check.minorRules=L002
```

### TOML Configuration File

```toml
[flyway.check]
minorRules = ["L002"]
```

### Configuration File

```properties
flyway.check.minorRules=L002
```