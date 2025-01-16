---
pill: callbacks
subtitle: flyway.callbacks
redirect_from: Configuration/callbacks/
---

## Description

Array of fully qualified class names of [Callback](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/callback/Callback.html) implementations to use to hook into the Flyway lifecycle, or packages to scan for these classes. Ensure the class or package is available on the classpath (see [Adding to the classpath](<Usage/Adding to the classpath>)).

Note: SQL callbacks matching the correct name pattern are loaded from locations (see [Callbacks](https://documentation.red-gate.com/flyway/flyway-concepts/callbacks)). This configuration parameter is only used for loading java callbacks. To disable loading sql callbacks, see [skipDefaultCallbacks](<Configuration/Flyway Namespace/Flyway Skip Default Callbacks Setting>).

## Type

String array

## Default

`["db/callback"]`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -callbacks="my.callback.FlywayCallback,my.package.to.scan" info
```

### TOML Configuration File

```toml
[flyway]
callbacks = ["my.callback.FlywayCallback", "my.package.to.scan"]
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
