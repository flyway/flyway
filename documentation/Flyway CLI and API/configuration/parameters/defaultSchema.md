---
layout: documentation
menu: configuration
pill: defaultSchema
subtitle: flyway.defaultSchema
redirect_from: /documentation/configuration/defaultSchema/
---

# Default Schema

## Description
The default schema managed by Flyway. This schema will be the one containing the [schema history table](/documentation/concepts/migrations#schema-history-table).
If not specified in [schemas](/documentation/configuration/parameters/schemas), Flyway will automatically attempt to create and clean this schema first.

This schema will also be the default for the database connection (provided the database supports this concept).

## Default
If [schemas](/documentation/configuration/parameters/schemas) is specified, the first schema in that list. Otherwise, the database's default schema.

## Usage

### Commandline
```powershell
./flyway -defaultSchema="schema2" info
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
