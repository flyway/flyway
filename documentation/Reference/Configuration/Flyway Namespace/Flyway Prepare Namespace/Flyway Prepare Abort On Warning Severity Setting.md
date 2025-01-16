---
subtitle: flyway.prepare.force
---

## Description

Will cause an error to be returned if any warnings of configured severity are raised when generating the deployment script.

## Type

String

### Valid values

- `None`
- `High`
- `Medium`
- `Low`

## Default

`None`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway prepare -abortOnWarningSeverity=true
```

### TOML Configuration File

```toml
[flyway.prepare]
abortOnWarningSeverity = "High"
```
