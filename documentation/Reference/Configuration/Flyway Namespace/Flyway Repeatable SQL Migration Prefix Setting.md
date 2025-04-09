---
subtitle: flyway.repeatableSqlMigrationPrefix
redirect_from: Configuration/repeatableSqlMigrationPrefix/
---

## Description

The file name prefix for repeatable SQL migrations.

Repeatable SQL migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix, which using the defaults translates to R__My_description.sql

## Type

String

## Default

`"R"`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -repeatableSqlMigrationPrefix="A" info
```

### TOML Configuration File

```toml
[flyway]
repeatableSqlMigrationPrefix = "A"
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
