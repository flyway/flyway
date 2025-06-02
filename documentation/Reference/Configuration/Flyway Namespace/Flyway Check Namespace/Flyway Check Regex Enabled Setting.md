---
subtitle: flyway.check.regexEnabled
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

You can configure this feature flag to enable or disable the Regex Engine for code analysis

## Type

Boolean

## Default

`true`

## Usage

### Command-line

```powershell
./flyway check -code -check.regexEnabled=false
```

### TOML Configuration File

```toml
[flyway.check]
regexEnabled = false
```

### Configuration File

```properties
flyway.check.regexEnabled=false
```