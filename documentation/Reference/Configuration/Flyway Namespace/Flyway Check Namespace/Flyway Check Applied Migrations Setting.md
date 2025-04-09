---
subtitle: flyway.check.appliedMigrations
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

An array of migration ids (migration versions or repeatable descriptions) to apply to create snapshots (generated via [`info`](Commands/info))

## Type

String array

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -changes -check.appliedMigrations="1,2,3"
```

### TOML Configuration File

```toml
[flyway.check]
appliedMigrations = ["1", "2", "3"]
```

### Configuration File

```properties
flyway.check.appliedMigrations=1,2,3
```