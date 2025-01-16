---
pill: tablespace
subtitle: flyway.tablespace
redirect_from: Configuration/tablespace/
---

## Description

The tablespace in which to create the schema history table that will be used by Flyway.

This setting is only relevant for databases that do support the notion of tablespaces. Its value is simply ignored for all others.

## Type

String

## Default

<i>Flyway uses the default tablespace for the database connection</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

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
