---
pill: sqlServerKerberosLoginFile
subtitle: flyway.sqlServer.kerberosLoginFile
---

# SQL Server Kerberos Login File
{% include teams.html %}

## Description
The path to the SQL Server login module configuration file (e.g. `SQLJDBCDriver.conf`) for use in Kerberos authentication.

## Usage

### Commandline
```powershell
./flyway -sqlserver.kerberos.login.file="/path/to/SQLJDBCDriver.conf" info
```

### TOML Configuration File
```toml
[flyway]
sqlserver.kerberos.login.file = "/path/to/SQLJDBCDriver.conf"
```

### Configuration File
```properties
flyway.sqlserver.kerberos.login.file=/path/to/SQLJDBCDriver.conf
```

### Environment Variable
```properties
FLYWAY_SQL_SERVER_KERBEROS_LOGIN_FILE=/path/to/SQLJDBCDriver.conf
```

### API
```java
SQLServerConfigurationExtension sqlServerConfigurationExtension = configuration.getPluginRegister().getPlugin(SQLServerConfigurationExtension.class);
sqlServerConfigurationExtension.setKerberosLoginFile("/path/to/SQLJDBCDriver.conf");
```

### Gradle
```groovy
flyway {
    pluginConfiguration = [
        sqlserverKerberosLoginFile: '/path/to/SQLJDBCDriver.conf'
    ]
}
```

### Maven
```xml
<configuration>
    <pluginConfiguration>
        <sqlserverKerberosLoginFile>/path/to/SQLJDBCDriver.conf</sqlserverKerberosLoginFile>
    </pluginConfiguration>
</configuration>
```
