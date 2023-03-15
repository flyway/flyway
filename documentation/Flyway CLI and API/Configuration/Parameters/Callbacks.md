---
pill: callbacks
subtitle: flyway.callbacks
redirect_from: Configuration/callbacks/
---

# Callbacks

## Description
Comma-separated list of fully qualified class names of [Callback](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/callback/Callback.html) implementations to use to hook into the Flyway lifecycle, or packages to scan for these classes. Ensure the class or package is available on the classpath (see [Adding to the classpath](/Adding to the classpath)).

Note: SQL callbacks matching the correct name pattern are loaded from locations (see [Callbacks](Concepts/Callback concept)). This configuration parameter is only used for loading java callbacks. To disable loading sql callbacks, see [skipDefaultCallbacks](Configuration/Parameters/Skip Default Callbacks).

## Default
db/callback

## Usage

### Commandline
```powershell
./flyway -callbacks="my.callback.FlywayCallback,my.package.to.scan" info
```

### Configuration File
```properties
flyway.callbacks=my.callback.FlywayCallback,my.package.to.scan
```

### Environment Variable
```properties
FLYWAY_CALLBACKS=my.callback.FlywayCallback,my.package.to.scan
```

### API
```java
Flyway.configure()
    .callbacks("my.callback.FlywayCallback", "my.package.to.scan")
    .load()
```

### Gradle
```groovy
flyway {
    callbacks = ['my.callback.FlywayCallback', 'my.package.to.scan']
}
```

### Maven
```xml
<configuration>
    <callbacks>my.callback.FlywayCallback,my.package.to.scan</callbacks>
</configuration>
```
