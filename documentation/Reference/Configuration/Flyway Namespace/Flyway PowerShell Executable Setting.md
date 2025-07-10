---
subtitle: flyway.powershellExecutable
---

## Description

The PowerShell executable used for running PowerShell script migrations (`.ps1` files).

## Type

String

## Default

`"powershell"` on Windows, `"pwsh"` on other platforms

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```bash
./flyway -powershellExecutable="pwsh" migrate
```

### TOML Configuration File

```toml
[flyway]
powershellExecutable = "pwsh"
```

### Environment Variable

```bash
export FLYWAY_POWERSHELL_EXECUTABLE=pwsh
```

### API

```java
Flyway.configure()
    .powershellExecutable("pwsh")
    .load()
```

### Gradle

```groovy
flyway {
    powershellExecutable = 'pwsh'
}
```

### Maven

```xml
<configuration>
    <powershellExecutable>pwsh</powershellExecutable>
</configuration>
```

## Notes

- **PowerShell Core**: Use `pwsh` to use PowerShell 7+ - works consistently across Windows, Linux, and macOS
- **Windows PowerShell 5.1**: Use `powershell` for legacy Windows PowerShell
- **CI/CD Environments**: Use `pwsh` for consistent PowerShell version across different environments