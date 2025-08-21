---
subtitle: flyway.check.rulesConfig
---

{% include commandlineonly.html %}

## Description

You can configure the file path for the SQLFluff configuration file to customize SQLFluff behavior during code analysis. This setting allows you to specify the exact file path containing the SQLFluff configuration file (typically named `sqlfluff.cfg`).

See [Code Analysis](https://documentation.red-gate.com/flyway/flyway-concepts/code-analysis) for more information.

## Type

String

## Default

By default, Flyway will look for `sqlfluff.cfg` in the `conf/` folder of the Flyway installation. This parameter interacts with the `workingDirectory` setting.

## Usage

### Command-line

```powershell
./flyway check -code -check.rulesConfig=/path/to/sqlfluff.cfg
```

### TOML Configuration File

```toml
[flyway.check]
rulesConfig = "/path/to/sqlfluff.cfg"
```