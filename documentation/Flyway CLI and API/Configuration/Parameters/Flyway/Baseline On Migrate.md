---
pill: baselineOnMigrate
subtitle: flyway.baselineOnMigrate
redirect_from: Configuration/baselineOnMigrate/
---

# Baseline On Migrate

## Description
Whether to automatically call [baseline](Commands/baseline) when [migrate](Commands/migrate) is executed against a non-empty schema with no [schema history table](configuration/parameters/flyway/table). This schema will then be baselined with the `baselineVersion` before executing the migrations. Only migrations above `baselineVersion` will then be applied.

This is useful for initial Flyway production deployments on projects with an existing DB.

Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong database in case of a configuration mistake!

## Default
false

## Usage

### Commandline
```powershell
./flyway -baselineOnMigrate="true" migrate
```

### TOML Configuration File
```toml
[flyway]
baselineOnMigrate = true
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
