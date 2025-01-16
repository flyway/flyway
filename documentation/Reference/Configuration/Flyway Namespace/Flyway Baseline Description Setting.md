---
pill: baselineDescription
subtitle: flyway.baselineDescription
redirect_from: Configuration/baselineDescription/
---

## Description

The Description to tag an existing schema with when executing [baseline](Commands/baseline).

## Type

String

## Default

`"<< Flyway Baseline >>"`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -baselineDescription="Baseline" baseline
```

### TOML Configuration File

```toml
[flyway]
baselineDescription = "Baseline"
```

### Configuration File

```properties
flyway.baselineDescription=Baseline
```

### Environment Variable

```properties
FLYWAY_BASELINE_DESCRIPTION=Baseline
```

### API

```java
Flyway.configure()
    .baselineDescription("Baseline")
    .load()
```

### Gradle

```groovy
flyway {
    baselineDescription = 'Baseline'
}
```

### Maven

```xml
<configuration>
    <baselineDescription>Baseline</baselineDescription>
</configuration>
```
