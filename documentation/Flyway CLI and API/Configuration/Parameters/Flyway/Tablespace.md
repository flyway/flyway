---
pill: tablespace
subtitle: flyway.tablespace
redirect_from: Configuration/tablespace/
---

# Tablespace

## Description
The tablespace where to create the schema history table that will be used by Flyway.

This setting is only relevant for databases that do support the notion of tablespaces. Its value is simply ignored for all others.

## Usage

### Commandline
```powershell
./flyway -tablespace="xyz" info
```

### TOML Configuration File
```toml
[flyway]
tablespace = "xyz"
```

### Configuration File
```properties
flyway.tablespace=xyz
```

### Environment Variable
```properties
FLYWAY_TABLESPACE=xyz
```

### API
```java
Flyway.configure()
    .tablespace("xyz")
    .load()
```

### Gradle
```groovy
flyway {
    tablespace = 'xyz'
}
```

### Maven
```xml
<configuration>
    <tablespace>xyz</tablespace>
</configuration>
```
