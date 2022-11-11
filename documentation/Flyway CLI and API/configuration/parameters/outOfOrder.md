---
layout: documentation
menu: configuration
pill: outOfOrder
subtitle: flyway.outOfOrder
redirect_from: /documentation/configuration/outOfOrder/
---

# Out Of Order

## Description
Allows migrations to be run "out of order".

If you already have versions `1.0` and `3.0` applied, and now a version `2.0` is found, it will be applied too instead of being ignored.

## Default
false

## Usage

### Commandline
```powershell
./flyway -outOfOrder="true" info
```

### Configuration File
```properties
flyway.outOfOrder=true
```

### Environment Variable
```properties
FLYWAY_OUT_OF_ORDER=true
```

### API
```java
Flyway.configure()
    .outOfOrder(true)
    .load()
```

### Gradle
```groovy
flyway {
    outOfOrder = true
}
```

### Maven
```xml
<configuration>
    <outOfOrder>true</outOfOrder>
</configuration>
```