---
pill: validateOnMigrate
subtitle: flyway.validateOnMigrate
redirect_from: Configuration/validateOnMigrate/
---

# Validate On Migrate

## Description
Whether to automatically call [validate](Commands/validate) or not when running migrate.

## Default
true

## Usage

### Commandline
```powershell
./flyway -validateOnMigrate="false" migrate
```

### TOML Configuration File
```toml
[flyway]
validateOnMigrate = false
```

### Configuration File
```properties
flyway.validateOnMigrate=false
```

### Environment Variable
```properties
FLYWAY_VALIDATE_ON_MIGRATE=false
```

### API
```java
Flyway.configure()
    .validateOnMigrate(false)
    .load()
```

### Gradle
```groovy
flyway {
    validateOnMigrate = false
}
```

### Maven
```xml
<configuration>
    <validateOnMigrate>false</validateOnMigrate>
</configuration>
```
