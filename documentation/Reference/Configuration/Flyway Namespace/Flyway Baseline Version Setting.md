---
pill: baselineVersion
subtitle: flyway.baselineVersion
redirect_from: Configuration/baselineVersion/
---

## Description

The version to tag an existing schema with when executing [baseline](Commands/baseline).

## Type

String

## Default

`"1"`

## Usage

### Flyway Desktop

This is automatically set when generating a baseline script through Flyway Desktop.
Otherwise, it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -baselineVersion="0.0" baseline
```

### TOML Configuration File

```toml
[flyway]
baselineVersion = "0.0"
```

### Configuration File

```properties
flyway.baselineVersion=0.0
```

### Environment Variable

```properties
FLYWAY_BASELINE_VERSION=0.0
```

### API

```java
Flyway.configure()
    .baselineVersion("0.0")
    .load()
```

### Gradle

```groovy
flyway {
    baselineVersion = '0.0'
}
```

### Maven

```xml
<configuration>
    <baselineVersion>0.0</baselineVersion>
</configuration>
```
