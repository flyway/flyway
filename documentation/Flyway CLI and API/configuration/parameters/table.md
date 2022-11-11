---
layout: documentation
menu: configuration
pill: table
subtitle: flyway.table
redirect_from: /documentation/configuration/table/
---

# Table

## Description
The name of Flyway's [schema history table](/documentation/concepts/migrations#schema-history-table).

By default (single-schema mode) the schema history table is placed in the default schema for the connection provided by the datasource.

When the [defaultSchema](/documentation/configuration/parameters/defaultSchema) or [schemas](/documentation/configuration/parameters/schemas) property is set (multi-schema mode), the schema history table is placed in the specified default schema.

## Default
flyway_schema_history

## Usage

### Commandline
```powershell
./flyway -table="my_schema_history_table" info
```

### Configuration File
```properties
flyway.table=my_schema_history_table
```

### Environment Variable
```properties
FLYWAY_TABLE=my_schema_history_table
```

### API
```java
Flyway.configure()
    .table("my_schema_history_table")
    .load()
```

### Gradle
```groovy
flyway {
    table = 'my_schema_history_table'
}
```

### Maven
```xml
<configuration>
    <table>my_schema_history_table</table>
</configuration>
```