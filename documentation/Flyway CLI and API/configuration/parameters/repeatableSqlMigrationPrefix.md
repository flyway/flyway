---
layout: documentation
menu: configuration
pill: repeatableSqlMigrationPrefix
subtitle: flyway.repeatableSqlMigrationPrefix
redirect_from: /documentation/configuration/repeatableSqlMigrationPrefix/
---

# Repeatable SQL Migration Prefix

## Description
The file name prefix for repeatable SQL migrations.

Repeatable SQL migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix, which using the defaults translates to R__My_description.sql

## Default
R

## Usage

### Commandline
```powershell
./flyway -repeatableSqlMigrationPrefix="A" info
```

### Configuration File
```properties
flyway.repeatableSqlMigrationPrefix=A
```

### Environment Variable
```properties
FLYWAY_REPEATABLE_SQL_MIGRATION_PREFIX=A
```

### API
```java
Flyway.configure()
    .repeatableSqlMigrationPrefix("A")
    .load()
```

### Gradle
```groovy
flyway {
    repeatableSqlMigrationPrefix = 'A'
}
```

### Maven
```xml
<configuration>
    <repeatableSqlMigrationPrefix>A</repeatableSqlMigrationPrefix>
</configuration>
```