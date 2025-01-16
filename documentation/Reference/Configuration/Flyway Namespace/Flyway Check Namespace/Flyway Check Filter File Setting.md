---
pill: check.filterFile
subtitle: flyway.check.filterFile
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

A filter configuration can be passed through to the underlying comparison engine that will be used in change and drift reports.

See [Filters & Ignore Rules](https://documentation.red-gate.com/flyway/database-development-using-flyway/database-development-using-flyway-desktop/configuring-comparisons-and-script-generations/filters-ignore-rules) for how to generate these in Flyway Desktop.

## Type

String

## Default

Flyway will identify and use filter files with specific names - no additional configuration is required through the
`filterFile` parameter if you adhere to these names.

_note:_ If you want Flyway to pick up a default file then it needs to be named exactly as specified, case is important. Otherwise the filter file will be ignored.

### SQL Server & Oracle

`Filter.scpf`

### Other databases

`filter.rgf`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway check -changes -url="jdbc://url1" -check.filterFile="filter.rgf"
```

### Configuration File

```properties
flyway.check.filterFile="Filters.scpf"
```

### TOML Configuration File

```toml
[flyway.check]
filterFile="Filters.scpf"
```

_note:_ `[redgateCompare]` will be the main way to use filter files in the future.
`flyway.check.filterFile` is supported for backwards compatibility, and will be deprecated in a future release.

```toml
[redgateCompare]
filterFile="Filters.scpf"
```


