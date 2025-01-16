---
subtitle: flywayDesktop.generate.undoScripts
---

## Description

When enabled, an undo script will be generated for every versioned migration which is generated in Flyway Desktop.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This setting can be configured from the project settings menu in Flyway Desktop.

### TOML Configuration File

```toml
[flywayDesktop.generate]
undoScripts = true
```
