---
pill: skipExecutingMigrations
subtitle: flyway.skipExecutingMigrations
redirect_from: Configuration/skipExecutingMigrations/
---

# Skip Executing Migrations
{% include teams.html %}

## Description
Whether Flyway should skip migration execution. The remainder of the operation will run as normal - including updating the schema history table, callbacks, and so on.

`skipExecutingMigrations` essentially allows you to mimic a migration being executed, because the schema history table is still updated as normal.

`skipExecutingMigrations` can be used to bring an out-of-process change into Flyway's change control process. For instance, a script run against the database outside of Flyway (like a hotfix) can be turned into a migration. The hotfix migration can be deployed with Flyway with `skipExecutingMigrations=true`. The schema history table will be updated with the new migration, but the script itself won't be executed again.

`skipExecutingMigrations` can be used with with [cherryPick](Configuration/Parameters/Cherry Pick) to skip specific migrations.

## Default
false

## Usage

### Commandline
```powershell
./flyway -skipExecutingMigrations="true" migrate
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

See the following articles for additional information on `skipExecutingMigrations` along with examples and use cases.

- [Blog Post: Skip Executing Migrations Examples](https://flywaydb.org/blog/skipExecutingMigrations)
    - [Hotfixes](https://flywaydb.org/blog/skipExecutingMigrations#hotfixes)
    - [Migration Generation](https://flywaydb.org/blog/skipExecutingMigrations#migration-generation)
    - [Deferred Migration Execution](https://flywaydb.org/blog/skipExecutingMigrations#deferred-migration-execution)
    - [Intermediate Baseline](https://flywaydb.org/blog/skipExecutingMigrations#intermediate-baseline)
