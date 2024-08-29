---
pill: oracleSqlPlusWarn
subtitle: flyway.oracleSqlPlusWarn
redirect_from: Configuration/oracleSqlPlusWarn/
---

# Oracle SQL*Plus Warn
{% include teams.html %}

## Description
Whether Flyway should issue a warning instead of an error whenever it encounters an [Oracle SQL*Plus statement](Supported Databases/oracle database#sqlplus-commands) it doesn't yet support.

## Default
false

## Usage

### Commandline
```powershell
./flyway -oracle.sqlplusWarn="true" info
```

### TOML Configuration File
```toml
[flyway]
oracle.sqlplusWarn = true
```

### Configuration File
```properties
flyway.oracle.sqlplusWarn=true
```

### Environment Variable
```properties
FLYWAY_ORACLE_SQLPLUS_WARN=true
```

### API
```java
OracleConfigurationExtension oracleConfigurationExtension = configuration.getPluginRegister().getPlugin(OracleConfigurationExtension.class);
oracleConfigurationExtension.setSqlPlusWarn(true);
```

### Gradle
```groovy
flyway {
    oracleSqlplusWarn = true
}
```

### Maven
```xml
<configuration>
    <oracleSqlplusWarn>true</oracleSqlplusWarn>
</configuration>
```
