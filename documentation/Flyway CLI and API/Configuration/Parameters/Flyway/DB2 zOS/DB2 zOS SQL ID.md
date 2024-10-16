---
pill: db2zSQLID
subtitle: flyway.db2z.sqlId
---

# DB2 z/OS SQL ID

## Description
(Required) The SQLID to be used in execution. 
This is the user under which it will create objects like tables etc. As creator, this user will get all rights on the object. 
If not explicitly set, the DB2z community support plugin will use the schema name as SQLID.

## Usage

### Commandline
```powershell
./flyway -db2z.sqlId="example_group" info
```

### TOML Configuration File
```toml
[flyway]
db2z.sqlId="example_group"
```

### Configuration File
```properties
flyway.db2z.sqlId="example_group"
```

### Environment Variable
```properties
FLYWAY_DB2Z_SQL_ID=example_group
```

### API
```java
DB2ZConfigurationExtension dB2ZConfigurationExtension = configuration.getPluginRegister().getPlugin(DB2ZConfigurationExtension.class);
dB2ZConfigurationExtension.setSqlId("example_group");
```

### Gradle
```groovy
flyway {
    pluginConfiguration = [
        db2zSqlId: 'example_group'
    ]
}
```

### Maven
```xml
<configuration>
    <pluginConfiguration>
        <db2zSqlId>example_group</db2zSqlId>
    </pluginConfiguration>
</configuration>
```
