---
pill: table
subtitle: flyway.table
redirect_from: Configuration/table/
---

# Table

## Description
The name of Flyway's [schema history table](Concepts/migrations#schema-history-table).

By default (single-schema mode) the schema history table is placed in the default schema for the connection provided by the datasource.

When the [defaultSchema](Configuration/Parameters/Flyway/Default Schema) or [schemas](Configuration/parameters/environments/schemas) property is set (multi-schema mode), the schema history table is placed in the specified default schema.

## Default
flyway_schema_history

## Usage

### Commandline
```powershell
./flyway -table="my_schema_history_table" info
```

### TOML Configuration File
```toml
[flyway]
table = "my_schema_history_table"
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
