---
pill: failOnMissingLocations
subtitle: flyway.failOnMissingLocations
---

# Fail On Missing Locations

## Description
Whether to fail if a location specified in the [`locations`](Configuration/parameters/flyway/locations) option doesn't exist.

## Default
false

## Usage

### Commandline
```powershell
./flyway -failOnMissingLocations="true" migrate
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
