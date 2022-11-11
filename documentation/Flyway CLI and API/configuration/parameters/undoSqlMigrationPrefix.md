---
layout: documentation
menu: configuration
pill: undoSqlMigrationPrefix
subtitle: flyway.undoSqlMigrationPrefix
redirect_from: /documentation/configuration/undoSqlMigrationPrefix/
---

# Undo SQL Migration Prefix
{% include teams.html %}

## Description
The file name prefix for undo SQL migrations.

Undo SQL migrations are responsible for undoing the effects of the versioned migration with the same version.

They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to U1.1__My_description.sql

## Default
U

## Usage

### Commandline
```powershell
./flyway -undoSqlMigrationPrefix="B" info
```

### Configuration File
```properties
flyway.undoSqlMigrationPrefix=B
```

### Environment Variable
```properties
FLYWAY_UNDO_SQL_MIGRATION_PREFIX=B
```

### API
```java
Flyway.configure()
    .undoSqlMigrationPrefix("B")
    .load()
```

### Gradle
```groovy
flyway {
    undoSqlMigrationPrefix = 'B'
}
```

### Maven
```xml
<configuration>
    <undoSqlMigrationPrefix>B</undoSqlMigrationPrefix>
</configuration>
```