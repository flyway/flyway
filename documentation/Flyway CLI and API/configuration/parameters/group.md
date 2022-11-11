---
layout: documentation
menu: configuration
pill: group
subtitle: flyway.group
redirect_from: /documentation/configuration/group/
---

# Group

## Description
Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions)

## Default
false

## Usage

### Commandline
```powershell
./flyway -group="true" info
```

### Configuration File
```properties
flyway.group=true
```

### Environment Variable
```properties
FLYWAY_GROUP=true
```

### API
```java
Flyway.configure()
    .group(true)
    .load()
```

### Gradle
```groovy
flyway {
    group = true
}
```

### Maven
```xml
<configuration>
    <group>true</group>
</configuration>
```