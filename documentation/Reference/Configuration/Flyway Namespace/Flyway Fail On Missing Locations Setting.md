---
pill: failOnMissingLocations
subtitle: flyway.failOnMissingLocations
---

## Description

Whether to fail if a location specified in the [
`locations`](<Configuration/Flyway Namespace/Flyway locations Setting>)option doesn't exist.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
. Namespace/Flyway locations) option doesn't exist.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
. flyway -failOnMissingLocations="true" migrate
```

### TOML Configuration File

```toml
[flyway]
failOnMissingLocations = true
```

### Configuration File

```properties
flyway.failOnMissingLocations=true
```

### Environment Variable

```properties
FLYWAY_FAIL_ON_MISSING_LOCATIONS=true
```

### API

```java
Flyway.configure()
    .failOnMissingLocations(true)
    .load()
```

### Gradle

```groovy
flyway {
    failOnMissingLocations = true
}
```

### Maven

```xml
<configuration>
    <failOnMissingLocations>true</failOnMissingLocations>
</configuration>
```
