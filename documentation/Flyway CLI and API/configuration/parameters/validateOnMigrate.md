---
layout: documentation
menu: configuration
pill: validateOnMigrate
subtitle: flyway.validateOnMigrate
redirect_from: /documentation/configuration/validateOnMigrate/
---

# Validate On Migrate

## Description
Whether to automatically call [validate](/documentation/command/validate) or not when running migrate.

## Default
true

## Usage

### Commandline
```powershell
./flyway -validateOnMigrate="false" migrate
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