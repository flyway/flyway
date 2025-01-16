---
pill: skipExecutingMigrations
subtitle: flyway.skipExecutingMigrations
redirect_from: Configuration/skipExecutingMigrations/
---

## Description

Whether Flyway should skip migration execution. The remainder of the operation will run as normal - including updating the schema history table, callbacks, etc.

`skipExecutingMigrations` essentially allows you to mimic a migration being executed, because the schema history table is still updated as normal.

`skipExecutingMigrations` can be used to bring an out-of-process change into Flyway's change control process. For instance, a script run against the database outside of Flyway (like a hotfix) can be turned into a migration. The hotfix migration can be deployed with Flyway with
`skipExecutingMigrations=true`. The schema history table will be updated with the new migration, but the script itself won't be executed again.

`skipExecutingMigrations` can be used with [`cherryPick`](<Configuration/Flyway Namespace/Flyway Cherry Pick Setting>) to skip specific migrations.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
. Namespace/Flyway Cherry Pick) to skip specific migrations.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
. flyway -skipExecutingMigrations="true" migrate
```

### TOML Configuration File

```toml
[flyway]
skipExecutingMigrations = true
```

### Configuration File

```properties
flyway.skipExecutingMigrations=true
```

### Environment Variable

```properties
FLYWAY_SKIP_EXECUTING_MIGRATIONS=true
```

### API

```java
Flyway.configure()
    .skipExecutingMigrations(true)
    .load()
```

### Gradle

```groovy
flyway {
    skipExecutingMigrations = true
}
```

### Maven

```xml
<configuration>
    <skipExecutingMigrations>true</skipExecutingMigrations>
</configuration>
```

## Related Reading

See the following article for additional information on `skipExecutingMigrations` along with examples and use cases.

- [Blog Post: Skip Executing Migrations Examples](https://documentation.red-gate.com/fd/october-2020-skip-executing-migrations-examples-259621238.html)

