---
pill: oracleSqlPlusWarn
subtitle: flyway.oracleSqlPlusWarn
redirect_from: Configuration/oracleSqlPlusWarn/
---

{% include teams.html %}

## Description

Whether Flyway should issue a warning instead of an error whenever it encounters an [Oracle SQL*Plus statement](<Database Driver Reference/oracle database#sqlplus-commands>) it doesn't yet support.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -oracle.sqlplusWarn="true" info
```

### TOML Configuration File

```toml
[flyway.oracle]
sqlplusWarn = true
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
