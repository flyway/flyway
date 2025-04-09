---
subtitle: flyway.table
redirect_from: Configuration/table/
---

## Description

The name of Flyway's [schema history table](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/flyway-schema-history-table).

By default (single-schema mode) the schema history table is placed in the default schema for the connection provided by the datasource.

When the [`defaultSchema`](<Configuration/Flyway Namespace/Flyway Default Schema Setting>) or [`schemas`](<Configuration/Environments Namespace/Environment schemas Setting>) property is set (multi-schema mode), the schema history table is placed in the specified default schema.

## Type

String

## Default

`"flyway_schema_history"`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

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
