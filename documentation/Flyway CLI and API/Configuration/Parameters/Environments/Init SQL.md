---
pill: initSql
subtitle: flyway.initSql
redirect_from: Configuration/initSql/
---

# Init SQL

## Description
SQL statements to be run immediately after a database connection has been established.

This is mainly used for setting some state on the connection that needs to be shared across all scripts, or should not be included into any one script.

This could effectively be considered an environment specific afterConnect [callback](Concepts/Callback concept).

## Usage

### Commandline
```powershell
./flyway -initSql="ALTER SESSION SET NLS_LANGUAGE='ENGLISH';" info
```

To configure a named environment via command line when using a TOML configuration, prefix `initSql` with `environments.{environment name}.` for example:
```powershell
./flyway "-environments.sample.initSql=ALTER SESSION SET NLS_LANGUAGE='ENGLISH';" info
```

### TOML Configuration File
```toml
[environments.default]
initSql = "ALTER SESSION SET NLS_LANGUAGE='ENGLISH';"
```

### Configuration File
```properties
flyway.initSql=ALTER SESSION SET NLS_LANGUAGE='ENGLISH';
```

### Environment Variable
```properties
FLYWAY_INIT_SQL=ALTER SESSION SET NLS_LANGUAGE='ENGLISH';
```

### API
```java
Flyway.configure()
    .initSql("ALTER SESSION SET NLS_LANGUAGE='ENGLISH';")
    .load()
```

### Gradle
```groovy
flyway {
    initSql = "ALTER SESSION SET NLS_LANGUAGE='ENGLISH';"
}
```

### Maven
```xml
<configuration>
    <initSql>ALTER SESSION SET NLS_LANGUAGE='ENGLISH';</initSql>
</configuration>
```
