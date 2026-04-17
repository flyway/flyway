---
subtitle: flyway.check.code.noqaSeverity
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

You can configure this parameter to control the severity of using `noqa` to suppress [SQLFluff rules](<Code Review Rules/Configuring SQLFluff Rules>). Setting the severity to `DISABLED` will prevent Flyway from checking for usage of `noqa` entirely.

## Type

String

### Valid values

- `"DISABLED"`
- `"WARNING"`
- `"ERROR"`

## Default

`WARNING`

## Usage

### Command-line

```powershell
./flyway check -code "-check.code.noqaSeverity=ERROR"
```

### TOML Configuration File

```toml
[flyway.check.code]
noqaSeverity = "ERROR"
```