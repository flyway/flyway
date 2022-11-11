---
layout: documentation
menu: configuration
pill: cleanOnValidationError
subtitle: flyway.cleanOnValidationError
redirect_from: /documentation/configuration/cleanOnValidationError/
---

# Clean On Validation Error

## Description
Whether to automatically call clean or not when a validation error occurs.

This is exclusively intended as a convenience for development. Even though we strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will bring you back to the state checked into SCM.

<strong>Warning ! Do not enable in production !</strong>

## Default
false

## Usage

### Commandline
```powershell
./flyway -cleanOnValidationError="true" validate
```

### Configuration File
```properties
flyway.cleanOnValidationError=true
```

### Environment Variable
```properties
FLYWAY_CLEAN_ON_VALIDATION_ERROR=true
```

### API
```java
Flyway.configure()
    .cleanOnValidationError(true)
    .load()
```

### Gradle
```groovy
flyway {
    cleanOnValidationError = true
}
```

### Maven
```xml
<configuration>
    <cleanOnValidationError>true</cleanOnValidationError>
</configuration>
```