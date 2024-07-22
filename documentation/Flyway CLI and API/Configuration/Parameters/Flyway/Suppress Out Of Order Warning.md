---
pill: outOfOrder
subtitle: flyway.outOfOrder
redirect_from: Configuration/suppressOutOfOrderWarning/
---

# Out Of Order

## Description
Disables the warning message that is shown during an execution with "outOfOrder" mode.

This parameter has no effect if `outOfOrder` is not set to `true`.

## Default
false

## Usage

### Commandline
```powershell
./flyway -outOfOrder="true" -suppressOutOfOrderWarning="true" info
```

### TOML Configuration File
```toml
[flyway]
outOfOrder = true
suppressOutOfOrderWarning = true
```

### Configuration File
```properties
flyway.outOfOrder=true
flyway.suppressOutOfOrderWarning=true
```

### Environment Variable
```properties
FLYWAY_OUT_OF_ORDER=true
FLYWAY_SUPPRESS_OUT_OF_ORDER_WARNING=true
```

### API
```java
Flyway.configure()
    .outOfOrder(true)
    .suppressOutOfOrderWarning(true)
    .load()
```

### Gradle
```groovy
flyway {
    outOfOrder = true
    suppressOutOfOrderWarning = true
}
```

### Maven
```xml
<configuration>
    <outOfOrder>true</outOfOrder>
    <suppressOutOfOrderWarning>true</suppressOutOfOrderWarning>
</configuration>
```
