---
subtitle: flyway.check.code.failOnError
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

You can configure this parameter to control whether the code review should fail the operation based on the violation severity level. This parameter works in conjunction with the `severity` field in code review rules configuration to determine which violations should be treated as failures.

When enabled, only code review violations with severity level `error` will cause the check command to fail. Violations with severity level `warning` will be reported but will not cause the operation to fail. 

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