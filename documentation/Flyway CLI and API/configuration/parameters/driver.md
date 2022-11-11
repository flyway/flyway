---
layout: documentation
menu: configuration
pill: driver
subtitle: flyway.driver
redirect_from: /documentation/configuration/driver/
---

# Driver

## Description
The fully qualified classname of the jdbc driver to use to connect to the database.

This must match the driver for the database type in the [url](/documentation/configuration/parameters/url) you are using.

If you use a driver class that is not shipped with Flyway, you must ensure that it is available on the classpath (see [Adding to the classpath](/documentation/addingToTheClasspath)).

## Default
Auto-detected based on the url

## Usage

### Commandline
```powershell
./flyway -driver=com.microsoft.sqlserver.jdbc.SQLServerDriver info
```

### Configuration File
```properties
flyway.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### Environment Variable
```properties
FLYWAY_DRIVER=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### API
```java
Flyway.configure()
    .driver("com.microsoft.sqlserver.jdbc.SQLServerDriver")
    .load()
```

### Gradle
```groovy
flyway {
    driver = 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
}
```

### Maven
```xml
<configuration>
    <driver>com.microsoft.sqlserver.jdbc.SQLServerDriver</driver>
</configuration>
```