---
pill: createSchemas
subtitle: flyway.createSchemas
redirect_from: Configuration/createSchemas/
---

## Description

Whether Flyway should attempt to create the schemas specified in the schemas property. [See this page for more details](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/flyway-schema-history-table)

If Flyway automatically creates a schema, then the schemas themselves will be dropped when cleaning.

## Type

String

## Default

`true`

## Usage

### Flyway Desktop

This is not configurable from Flyway Desktop.
This must be set to true for migration generation to work in Flyway Desktop.

### Command-line

```powershell
./flyway -createSchemas="false" info
```

### TOML Configuration File

```toml
[flyway]
createSchemas = false
```

### Configuration File

```properties
flyway.createSchemas=false
```

### Environment Variable

```properties
FLYWAY_CREATE_SCHEMAS=false
```

### API

```java
Flyway.configure()
    .createSchemas(false)
    .load()
```

### Gradle

```groovy
flyway {
    createSchemas = false
}
```

### Maven

```xml
<configuration>
    <createSchemas>false</createSchemas>
</configuration>
```
