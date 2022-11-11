---
layout: documentation
menu: configuration
pill: sqlMigrationSeparator
subtitle: flyway.sqlMigrationSeparator
redirect_from: /documentation/configuration/sqlMigrationSeparator/
---

# SQL Migration Separator

## Description
The file name separator for Sql migrations.

Versioned SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to V1.1__My_description.sql

## Default
__

## Usage

### Commandline
```powershell
./flyway -sqlMigrationSeparator="-" info
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