---
subtitle: flyway.check.rulesConfig
---

{% include commandlineonly.html %}

## Description

You can configure the file path for the SQLFluff configuration file to customize SQLFluff behavior during code analysis. This setting allows you to specify the exact file path containing the SQLFluff configuration file.

This parameter interacts with the [`workingDirectory`](<Command-line Parameters/Working Directory Parameter>) setting.

See [Code Analysis](https://documentation.red-gate.com/flyway/flyway-concepts/code-analysis) for more information.

## Type

String

## Default

Flyway’s Redgate Bundle version of SQLFluff will look for `sqlfluff.cfg` in the `conf/` folder of the Flyway installation.

If you're using your own installed version of SQLFluff, Flyway will not attempt to load any default configuration file.

## Note

By default, SQLFluff will respect local configuration files (e.g. `.sqlfluff`) if they are present.

However, setting this parameter will cause Flyway to prevent SQLFluff from loading any additional configuration files. 

If you want SQLFluff to apply its own [configuration searching and nesting behavior](https://docs.sqlfluff.com/en/stable/configuration/setting_configuration.html) then do not set this parameter.

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