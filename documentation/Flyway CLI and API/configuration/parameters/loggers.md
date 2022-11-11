---
layout: documentation
menu: configuration
pill: loggers
subtitle: flyway.loggers
---

# Loggers

## Description
The `loggers` configuration parameter allows you to override Flyway's logging auto-detection and specify an exact logger, or comma-separated list of loggers, you wish to use.
This can be useful when a dependency comes with a logger you do not wish to use.

### Valid Options

* `auto` - Auto detect the logger (default behavior)
* `console` - Use stdout/stderr (_only available when using CLI_)
* `slf4j2` - Use the slf4j2 logger
* `log4j2` - Use the log4j2 logger
* `apache-commons` - Use the Apache Commons logger

Alternatively you can provide the fully qualified class name for any other logger to use that.

### Default
`auto`

### Notes

If you are using Flyway within Java and have multiple instances of the Flyway object with different configurations, you must ensure they all have the same loggers configured.
Other scenarios are unsupported.

## Usage

### Commandline
```powershell
./flyway -loggers=auto
```

### Configuration File
```properties
flyway.loggers=auto
```

### Environment Variable
```properties
FLYWAY_LOGGERS=auto
```

### API
```java
Flyway.configure()
    .loggers("auto")
    .load()
```

### Gradle
```groovy
flyway {
    loggers = 'auto'
}
```

### Maven
```xml
<configuration>
    <loggers>auto</loggers>
</configuration>
```
