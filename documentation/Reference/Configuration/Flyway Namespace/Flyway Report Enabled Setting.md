---
subtitle: flyway.reportEnabled
---

{% include commandlineonly.html %}

## Description

Whether to enable generation of a report file.

Flyway's reports solve the problem of reporting on what Flyway did - it does not produce additional information but packages up the CLI output in a more convenient manner.

 You might want this to:
 * Have a record of what happened when for audit or reporting purposes
 * Understand and be able to communicate status or failures in a more easily shareable manner than the raw CLI output logs

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