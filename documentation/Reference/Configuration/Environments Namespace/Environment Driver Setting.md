---
subtitle: flyway.environments.*.driver
redirect_from: Configuration/driver/
---

## Description

The fully qualified class name of the jdbc driver to use to connect to the database.

This must match the driver for the database type in the [url](<Configuration/Environments Namespace/Environment url Setting>) you are using.

If you use a driver class that is not shipped with Flyway, you must ensure that it is available on the classpath (see [Adding to the classpath](<Usage/Adding to the classpath>)).

## Type

String

## Default

<i>Auto-detected based on the url</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog for certain database types using Flyway Desktop.

### Command-line

```powershell
./flyway -driver=com.microsoft.sqlserver.jdbc.SQLServerDriver info
```

To configure a named environment via command line when using a TOML configuration, prefix `driver` with
`environments.{environment name}.` for example:

```powershell
./flyway -environments.sample.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver info
```

### TOML Configuration File

```toml
[environments.default]
driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
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
