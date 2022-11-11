---
layout: documentation
menu: configuration
pill: sqlMigrationSuffixes
subtitle: flyway.sqlMigrationSuffixes
redirect_from: /documentation/configuration/sqlMigrationSuffixes/
---

# SQL Migration Suffixes

## Description
Comma-separated list of file name suffixes for SQL migrations.

SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to V1_1__My_description.sql

Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as editors with specific file associations.

## Default
.sql

## Usage

### Commandline
```powershell
./flyway -sqlMigrationSuffixes=".sql,.pkg,.pkb" info
```

### Configuration File
```properties
flyway.sqlMigrationSuffixes=.sql,.pkg,.pkb
```

### Environment Variable
```properties
FLYWAY_SQL_MIGRATION_SUFFIXES=.sql,.pkg,.pkb
```

### API
```java
Flyway.configure()
    .sqlMigrationSuffixes(".sql,.pkg,.pkb")
    .load()
```

### Gradle
```groovy
flyway {
    sqlMigrationSuffixes = '.sql,.pkg,.pkb'
}
```

### Maven
```xml
<configuration>
    <sqlMigrationSuffixes>.sql,.pkg,.pkb</sqlMigrationSuffixes>
</configuration>
```