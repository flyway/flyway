---
pill: sqlMigrationPrefix
subtitle: flyway.sqlMigrationPrefix
redirect_from: Configuration/sqlMigrationPrefix/
---

## Description

The file name prefix for versioned SQL migrations.

Versioned SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to V1.1__My_description.sql

## Type

String

## Default

`"V"`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -sqlMigrationPrefix="M" info
```

### TOML Configuration File

```toml
[flyway]
sqlMigrationPrefix = "M"
```

### Configuration File

```properties
flyway.sqlMigrationPrefix=M
```

### Environment Variable

```properties
FLYWAY_SQL_MIGRATION_PREFIX=M
```

### API

```java
Flyway.configure()
    .sqlMigrationPrefix("M")
    .load()
```

### Gradle

```groovy
flyway {
    sqlMigrationPrefix = 'M'
}
```

### Maven

```xml
<configuration>
    <sqlMigrationPrefix>M</sqlMigrationPrefix>
</configuration>
```
