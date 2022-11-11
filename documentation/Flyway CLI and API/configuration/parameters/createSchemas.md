---
layout: documentation
menu: configuration
pill: createSchemas
subtitle: flyway.createSchemas
redirect_from: /documentation/configuration/createSchemas/
---

# Create Schemas

## Description
Whether Flyway should attempt to create the schemas specified in the schemas property. [See this page for more details](/documentation/concepts/migrations#the-createschemas-option-and-the-schema-history-table)

## Default
true

## Usage

### Commandline
```powershell
./flyway -createSchemas="false" info
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
