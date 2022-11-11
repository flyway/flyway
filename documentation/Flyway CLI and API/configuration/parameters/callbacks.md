---
layout: documentation
menu: configuration
pill: callbacks
subtitle: flyway.callbacks
redirect_from: /documentation/configuration/callbacks/
---

# Callbacks

## Description
Comma-separated list of fully qualified class names of [Callback](/documentation/usage/api/javadoc/org/flywaydb/core/api/callback/Callback) implementations to use to hook into the Flyway lifecycle, or packages to scan for these classes. Ensure the class or package is available on the classpath (see [Adding to the classpath](/documentation/addingToTheClasspath)).

Note: SQL callbacks matching the correct name pattern are loaded from locations (see [Callbacks](/documentation/concepts/callbacks)). This configuration parameter is only used for loading java callbacks. To disable loading sql callbacks, see [skipDefaultCallbacks](/documentation/configuration/parameters/skipDefaultCallbacks).

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