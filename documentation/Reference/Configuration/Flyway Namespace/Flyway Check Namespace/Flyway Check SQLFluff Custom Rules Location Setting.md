---
subtitle: flyway.check.sqlfluffCustomRulesLocation
---

{% include teams.html %}

## Description

To use custom SQLFluff rules you need to Flyway to the appropriate directory, this is done by setting the `sqlfluffCustomRulesPath` parameter.

## Type

String

## Default

If not specified, Flyway will not attempt to run any custom rules.

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```shell
./flyway check -code -check.sqlfluffCustomRulesPath=/code-review-rules
```

### TOML Configuration File

```toml
[flyway.check]
sqlfluffCustomRulesPath = "/code-review-rules"
```

### Configuration File

```properties
flyway.check.sqlfluffCustomRulesPath=/code-review-rules
```
