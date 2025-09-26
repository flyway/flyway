---
subtitle: flyway.db2z.databaseName
---

## Description

(Required) The name of the DB2 z/OS database.
This is used as a namespace for storage objects like tablespaces, similar to what a schema is for tables and indexes.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -db2z.databaseName="example_database" info
```

### TOML Configuration File

```toml
[flyway.db2z]
databaseName="example_database"
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
DB2ZConfigurationExtension dB2ZConfigurationExtension = configuration.getConfigurationExtension(DB2ZConfigurationExtension.class);
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
