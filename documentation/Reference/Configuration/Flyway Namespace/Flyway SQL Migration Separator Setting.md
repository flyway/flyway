---
subtitle: flyway.sqlMigrationSeparator
redirect_from: Configuration/sqlMigrationSeparator/
---

## Description

The file name separator for Sql migrations.

Versioned SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to V1.1__My_description.sql

## Type

String

## Default

`"__"`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -sqlMigrationSeparator="-" info
```

### TOML Configuration File

```toml
[flyway]
sqlMigrationSeparator = "-"
```

### Configuration File

```properties
flyway.sqlMigrationSeparator=-
```

### Environment Variable

```properties
FLYWAY_SQL_MIGRATION_SEPARATOR=-
```

### API

```java
Flyway.configure()
    .sqlMigrationSeparator("-")
    .load()
```

### Gradle

```groovy
flyway {
    sqlMigrationSeparator = '-'
}
```

### Maven

```xml
<configuration>
    <sqlMigrationSeparator>-</sqlMigrationSeparator>
</configuration>
```
