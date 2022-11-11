---
layout: documentation
menu: configuration
pill: sqlMigrationPrefix
subtitle: flyway.sqlMigrationPrefix
redirect_from: /documentation/configuration/sqlMigrationPrefix/
---

# SQL Migration Prefix

## Description
The file name prefix for versioned SQL migrations.

Versioned SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to V1.1__My_description.sql

## Default
V

## Usage

### Commandline
```powershell
./flyway -sqlMigrationPrefix="M" info
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