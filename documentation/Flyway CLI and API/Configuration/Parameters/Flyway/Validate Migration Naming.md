---
pill: validateMigrationNaming
subtitle: flyway.validateMigrationNaming
redirect_from: Configuration/validateMigrationNaming/
---

# Validate Migration Naming

## Description
Whether to ignore migration files whose names do not match the naming conventions.

If `false`, files with invalid names are ignored and Flyway continues normally. If `true`, Flyway fails fast and lists the offending files.

## Default
false

## Usage

### Commandline
```powershell
./flyway -validateMigrationNaming="true" info
```

### TOML Configuration File
```toml
[flyway]
validateMigrationNaming = true
```

### Configuration File
```properties
flyway.validateMigrationNaming=true
```

### Environment Variable
```properties
FLYWAY_VALIDATE_MIGRATION_NAMING=true
```

### API
```java
Flyway.configure()
    .validateMigrationNaming(true)
    .load()
```

### Gradle
```groovy
flyway {
    validateMigrationNaming = true
}
```

### Maven
```xml
<configuration>
    <validateMigrationNaming>true</validateMigrationNaming>
</configuration>
```
