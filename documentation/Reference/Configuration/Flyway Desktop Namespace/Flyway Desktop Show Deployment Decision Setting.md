---
subtitle: flywayDesktop.showDeploymentDecision
---

## Description

Determines the pages displayed in the Flyway Desktop UI in relation to deployment.

## Type

Boolean

## Default

`false` - it is set to `true` for new projects, but defaults to `false` if not set for backward compatibility

## Usage

### Flyway Desktop

This will always be set to `true` when a project is created using Flyway Desktop, though Flyway Desktop will
honor it if it is manually changed in the settings file.
This setting will be updated when choosing a deployment method on a deployment page and can be updated subsequently from
the Flyway Desktop settings modal.

### TOML Configuration File

```toml
[flywayDesktop]
showDeploymentDecision = false
```
