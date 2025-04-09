---
subtitle: flyway.defaultSchema
redirect_from: Configuration/defaultSchema/
---

## Description

The default schema managed by Flyway. This schema will be the one containing the [schema history table](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/flyway-schema-history-table).
If not specified in [schemas](<Configuration/Environments Namespace/Environment schemas Setting>), Flyway will automatically attempt to create and clean this schema first. If Flyway automatically created it, then it will be dropped when cleaning.

This schema will also be the default for the database connection (provided the database supports this concept).

## Type

String

## Default

If [schemas](<Configuration/Environments Namespace/Environment schemas Setting>) are specified for the specified environment, the first schema in that list. Otherwise, the database's default schema.

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -defaultSchema="schema2" info
```

### TOML Configuration File

```toml
[flyway]
defaultSchema = "schema2"
```

### Configuration File

```properties
flyway.defaultSchema=schema2
```

### Environment Variable

```properties
FLYWAY_DEFAULT_SCHEMA=schema2
```

### API

```java
Flyway.configure()
    .defaultSchema("schema2")
    .load()
```

### Gradle

```groovy
flyway {
    defaultSchema = 'schema2'
}
```

### Maven

```xml
<configuration>
    <defaultSchema>schema2</defaultSchema>
</configuration>
```
