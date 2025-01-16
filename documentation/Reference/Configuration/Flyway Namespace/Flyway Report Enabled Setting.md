---
pill: reportEnabled
subtitle: flyway.reportEnabled
---

{% include commandlineonly.html %}

## Description

Whether to enable generating a report file.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -reportEnabled=true migrate
```

### TOML Configuration File

```toml
[flyway]
reportEnabled = true
```

### Configuration File

```properties
flyway.reportEnabled=true
```

## Notes

The results of `check` will consistently trigger the generation of a report, unaffected by this configuration.