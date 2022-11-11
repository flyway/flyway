---
layout: documentation
menu: configuration
pill: schemas
subtitle: flyway.schemas
redirect_from: /documentation/configuration/schemas/
---

# Schemas

## Description
Comma-separated, case-sensitive list of schemas managed by Flyway.

Flyway will attempt to create these schemas if they do not already exist, and will clean them in the order of this list.
If Flyway created them, then the schemas themselves will be dropped when cleaning.

If [defaultSchema](/documentation/configuration/parameters/defaultSchema) is not specified, the first schema in this list also acts as the default schema, which is where the schema history table will be placed.

## Usage

### Commandline
```powershell
./flyway -schemas="schema1,schema2" info
```

### Configuration File
```properties
flyway.schemas=schema1,schema2
```

### Environment Variable
```properties
FLYWAY_SCHEMAS=schema1,schema2
```

### API
```java
Flyway.configure()
    .schemas("schema1", "schema2")
    .load()
```

### Gradle
```groovy
flyway {
    schemas = ['schema1', 'schema2']
}
```

### Maven
```xml
<configuration>
    <schemas>
        <schema>schema1</schema>
        <schema>schema2</schema>
    </schemas>
</configuration>
```
