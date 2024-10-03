---
pill: db2zDatabaseName
subtitle: flyway.db2z.databaseName
---

# DB2 z/OS Database Name

## Description
(Required) The name of the DB2 z/OS database. 
This is used as a namespace for storage objects like tablespaces, similar to what a schema is for tables and indexes.

## Usage

### Commandline
```powershell
./flyway -db2z.databaseName="example_database" info
```

### TOML Configuration File
```toml
[flyway]
db2z.databaseName="example_database"
```

### Configuration File
```properties
flyway.db2z.databaseName="example_database"
```

### Environment Variable
```properties
FLYWAY_DB2Z_DATABASE_NAME=example_database
```

### API
```java
DB2ZConfigurationExtension dB2ZConfigurationExtension = configuration.getPluginRegister().getPlugin(DB2ZConfigurationExtension.class);
dB2ZConfigurationExtension.setDatabaseName("example_database");
```

### Gradle
```groovy
flyway {
    pluginConfiguration = [
        db2zDatabaseName: 'example_database'
    ]
}
```

### Maven
```xml
<configuration>
    <pluginConfiguration>
        <db2zDatabaseName>example_database</db2zDatabaseName>
    </pluginConfiguration>
</configuration>
```
