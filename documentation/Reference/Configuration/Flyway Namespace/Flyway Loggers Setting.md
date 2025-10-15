---
subtitle: flyway.loggers
---

## Description

Allows you to override Flyway's logging auto-detection and specify an exact logger, or comma-separated list of loggers, to use.
This can be useful when a dependency comes with a logger you do not wish to use.

### Valid Options


| Logger              | Details                                              | Ships with Flyway Command-line   |
| ------------------- | ---------------------------------------------------- | -------------------------------- |
| **auto**            | Auto detect the logger (default behavior)            | Yes                              |
| **console**         | Use stdout/stderr (_only available when using CLI_)  | Yes                              |
| **slf4j**           | Use the slf4j logger                                 | Yes                              |
| **log4j2**          | Use the log4j2 logger                                | No                               |
| **apache-commons**  | Use the Apache Commons logger                        | No                               |

Alternatively you can provide the fully qualified class name for any other logger to use that.

## Type

String array

### Default

`["auto"]`

### Notes

If you are using Flyway within Java and have multiple instances of the Flyway object with different configurations, you must ensure they all have the same loggers configured.
Other scenarios are unsupported.

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop.

### Command-line

```powershell
./flyway -loggers=auto
```

### TOML Configuration File

```toml
[flyway]
loggers = ["auto"]
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
    <loggers>auto</lockRetryCount>
</configuration>
```
