---
pill: baselineOnMigrate
subtitle: flyway.baselineOnMigrate
redirect_from: Configuration/baselineOnMigrate/
---

## Description

Whether to automatically call [baseline](Commands/baseline) when [migrate](Commands/migrate) is executed against a non-empty schema with no [schema history table](<Configuration/Flyway Namespace/Flyway Table Setting>).

This schema will then be baselined with the
[`baselineVersion`](<Configuration/Flyway Namespace/Flyway Baseline Version Setting>) before executing the migrations. 
Only migrations above [`baselineVersion`](<Configuration/Flyway Namespace/Flyway Baseline Version Setting>) will then be applied.

_Note_ - Flyway will check the [schemas](<Configuration/Environments Namespace/Environment schemas Setting>) you have specified to decide whether your database is empty. If the database appears empty and a relevant baseline script exists then Flyway will attempt to execute it as part of the migration.

### Why should I use this ?

It is a convenience instead of running a separate [baseline](Commands/baseline) step for initial Flyway production deployments on projects with an existing DB.

Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong database in case of a configuration mistake!

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
flyway -baselineOnMigrate="true" migrate
```

### TOML Configuration File

```toml
[flyway]
baselineOnMigrate = true
```

### Configuration File

```properties
flyway.baselineOnMigrate=true
```

### Environment Variable

```properties
FLYWAY_BASELINE_ON_MIGRATE=true
```

### API

```java
Flyway.configure()
    .baselineOnMigrate(true)
    .load()
```

### Gradle

```groovy
flyway {
    baselineOnMigrate = true
}
```

### Maven

```xml
<configuration>
    <baselineOnMigrate>true</baselineOnMigrate>
</configuration>
```
