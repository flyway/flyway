---
subtitle: flyway.schemas
redirect_from: Configuration/schemas/
---

## Description

Case-sensitive list of schemas managed by Flyway.

Flyway will attempt to create these schemas if they do not already exist, and will clean them in the order of this list.
If Flyway automatically created them, then the schemas themselves will be dropped when cleaning.

If [`defaultSchema`](<Configuration/Flyway Namespace/Flyway Default Schema Setting>)is not specified, the first schema in this list also acts as the default schema, which is where the schema history table will be placed.

For [diff](<Commands/Diff>) or [prepare](<Commands/Prepare>) operations these will be the schemas used in the comparison (except in the case of SQL Server where this parameter will be ignored).

## Type

String array

## Default

`[]`

## Usage

### Flyway Desktop

This can be set from the connection dialog for all databases other than SQL Server, where schemas should not be specified.

### Command-line

```powershell
./flyway -schemas="schema1,schema2" info
```

To configure a named environment via command line when using a TOML configuration, prefix `schemas` with
`environments.{environment name}.` for example:

```powershell
./flyway "-environments.sample.schemas=schema1,schema2" info
```

### TOML Configuration File

```toml
[environments.default]
schemas = ["schema1", "schema2"]
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
