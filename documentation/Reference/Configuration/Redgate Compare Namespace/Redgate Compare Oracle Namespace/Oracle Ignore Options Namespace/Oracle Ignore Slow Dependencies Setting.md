---
subtitle: redgateCompare.oracle.options.behavior.ignoreSlowDependencies
---

## Description

Ignores certain dependencies that affect comparison performance when comparing databases. The following dependencies are ignored:

- type objects in your schema that are referenced by an object in another user's schema
- REF constraints in your schema that reference, or are referenced by, objects in another user's schema

Note: if this option isn't selected, all dependencies will be read, and comparisons may be slow.

## Type

Boolean

## Default

`true`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### TOML Configuration File

```toml
[redgateCompare.oracle.options.ignores]
ignoreSlowDependencies = true
```
