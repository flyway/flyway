---
layout: documentation
menu: configuration
pill: baselineOnMigrate
subtitle: flyway.baselineOnMigrate
redirect_from: /documentation/configuration/baselineOnMigrate/
---

# Baseline On Migrate

## Description
Whether to automatically call [baseline](/documentation/command/baseline) when [migrate](/documentation/command/migrate) is executed against a non-empty schema with no metadata table. This schema will then be baselined with the `baselineVersion` before executing the migrations. Only migrations above `baselineVersion` will then be applied.

This is useful for initial Flyway production deployments on projects with an existing DB.

Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong database in case of a configuration mistake!

## Default
false

## Usage

### Commandline
```powershell
./flyway -baselineOnMigrate="true" migrate
```

### Configuration File
```properties
flyway.baselineOnMigrate=true
```

### Environment Variable
```properties
FLYWAY_BASELINE_ON_MIGRATE=true
```

### API
```java
Flyway.configure()
    .baselineOnMigrate(true)
    .load()
```

### Gradle
```groovy
flyway {
    baselineOnMigrate = true
}
```

### Maven
```xml
<configuration>
    <baselineOnMigrate>true</baselineOnMigrate>
</configuration>
```
