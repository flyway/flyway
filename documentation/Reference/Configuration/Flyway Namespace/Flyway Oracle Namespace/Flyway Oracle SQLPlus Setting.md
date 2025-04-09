---
subtitle: flyway.oracleSqlPlus
redirect_from: Configuration/oracleSqlPlus/
---

{% include teams.html %}

## Description

Enable Flyway's support for [Oracle SQL*Plus commands](<Database Driver Reference/oracle database#sqlplus-commands>).

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -oracle.sqlplus="true" info
```

### TOML Configuration File

```toml
[flyway.oracle]
sqlplus = true
```

### Configuration File

```properties
flyway.oracle.sqlplus=true
```

### Environment Variable

```properties
FLYWAY_ORACLE_SQLPLUS=true
```

### API

```java
OracleConfigurationExtension oracleConfigurationExtension = configuration.getPluginRegister().getPlugin(OracleConfigurationExtension.class);
oracleConfigurationExtension.setSqlPlus(true);
```

### Gradle

```groovy
flyway {
    oracleSqlplus = true
}
```

### Maven

```xml
<configuration>
    <oracleSqlplus>true</oracleSqlplus>
</configuration>
```

## Use Cases

### Configuring consistent sessions for your migrations

See our list of [supported SQL\*Plus commands](<Database Driver Reference/oracle database#sqlplus-commands>) and how you can utilize them with [site and user profiles](<Database Driver Reference/oracle database#site-profiles-gloginsql--user-profiles-loginsql>) once SQL\*Plus is enable to achieved this.
