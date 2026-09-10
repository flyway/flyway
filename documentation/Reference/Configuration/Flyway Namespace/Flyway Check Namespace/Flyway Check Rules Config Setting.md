---
subtitle: flyway.check.rulesConfig
---

{% include commandlineonly.html %}

## Description

You can configure the file path for the SQLFluff configuration file to customize SQLFluff behavior during code review. This setting allows you to specify the exact file path containing the SQLFluff configuration file.

This parameter interacts with the [`workingDirectory`](<Command-line Parameters/Working Directory Parameter>) setting.

See [Code Review](https://documentation.red-gate.com/flyway/flyway-concepts/code-analysis) for more information.

## Type

String

## Default

Flyway’s Redgate Bundle version of SQLFluff will look for `sqlfluff.cfg` in the `conf/` folder of the Flyway installation.

If you're using your own installed version of SQLFluff, Flyway will not attempt to load any default configuration file.

## Note

Setting this parameter isn't the only way Flyway ends up ignoring local configuration files such as `.sqlfluff`. Flyway's Redgate Bundle version of SQLFluff does this whenever it finds `sqlfluff.cfg` in the `conf/` folder and the bundled engine is present, whether or not you set this parameter.

See [Which configuration is used](<Code Review Rules/Configuring SQLFluff Rules#which-configuration-is-used>) for the full precedence between this setting, the default `conf/sqlfluff.cfg`, and SQLFluff's own configuration searching and nesting behavior.

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