---
subtitle: flyway.check.code.failOnError
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

You can configure this parameter to control whether the code analysis should fail the operation based on the violation severity level. This parameter works in conjunction with the `severity` field in code analysis rules configuration to determine which violations should be treated as failures.

When enabled, only code analysis violations with severity level `error` will cause the check command to fail. Violations with severity level `warning` will be reported but will not cause the operation to fail. 

**Note**: Enabling this mode is incompatible with setting the deprecated code analysis `majorTolerance` and `minorTolerance` parameters.

See [Code Analysis](https://documentation.red-gate.com/flyway/flyway-concepts/code-analysis) for more information.

## Type

Boolean

## Default

`false`

## Usage

### Command-line

```powershell
./flyway check -code "-check.code.failOnError=true"
```

### TOML Configuration File

```toml
[flyway.check.code]
failOnError = true
```