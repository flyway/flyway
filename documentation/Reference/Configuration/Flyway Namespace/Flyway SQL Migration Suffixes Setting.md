---
pill: sqlMigrationSuffixes
subtitle: flyway.sqlMigrationSuffixes
redirect_from: Configuration/sqlMigrationSuffixes/
---

## Description

Array of file name suffixes for SQL migrations.

SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to V1_1__My_description.sql

Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as editors with specific file associations.

## Type

String array

## Default

`[".sql"]`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -sqlMigrationSuffixes=".sql,.pkg,.pkb" info
```

### TOML Configuration File

```toml
[flyway]
sqlMigrationSuffixes = [".sql", ".pkg", ".pkb"]
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
