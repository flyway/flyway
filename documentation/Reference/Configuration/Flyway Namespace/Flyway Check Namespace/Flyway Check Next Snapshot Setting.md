---
subtitle: flyway.check.nextSnapshot
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

A snapshot containing all migrations including those that are pending (generated via [`snapshot`](Commands/snapshot))

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -changes -url="jdbc://url" -check.nextSnapshot="my_snapshot"
```

### TOML Configuration File

```toml
[flyway.check]
nextSnapshot = "my_snapshot"
```

### Configuration File

```properties
flyway.check.nextSnapshot=my_snapshot
```