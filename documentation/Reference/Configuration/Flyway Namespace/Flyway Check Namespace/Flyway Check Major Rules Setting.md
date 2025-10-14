---
subtitle: flyway.check.majorRules
---

{% include teams.html %}

{% include commandlineonly.html %}

## Description
***This is deprecated - please look at using the `check.code.failOnError` mechanism described in [Code Analysis Rules](<Code Analysis Rules>)***

You can configure your pipeline to fail when specified static code analysis rules beyond a given tolerance level are violated.

`majorRules` should contain an array of [rules](Code Analysis Rules) which are considered to be major.

If the total number of `majorRules` violations exceeds the [`majorTolerance`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Major Tolerance Setting>), Flyway will fail.

## Type

String array

## Default

No rules are regarded as major violations

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -code -check.majorRules=L001
```

### TOML Configuration File

```toml
[flyway.check]
majorRules = ["L001"]
```

### Configuration File

```properties
flyway.check.majorRules=L001
```