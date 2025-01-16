---
pill: postgresqlTransactionalLock
subtitle: flyway.postgresql.transactional.lock
---

# PostgreSQL Transactional Lock

## Description

Whether transactional advisory locks should be used with PostgreSQL.

If false, session-level locks will be used instead.

This should be set to `false` for statements such as `CREATE INDEX CONCURRENTLY`.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -postgresql.transactional.lock=false info
```

### TOML Configuration File

```toml
[flyway.postgresql]
transactional.lock = false
```

### Configuration File

```properties
flyway.postgresql.transactional.lock=false
```

### Environment Variable

```properties
FLYWAY_POSTGRESQL_TRANSACTIONAL_LOCK=false
```

### API

```java
PostgreSQLConfigurationExtension configurationExtension = configuration.getPluginRegister().getPlugin(PostgreSQLConfigurationExtension.class)
configurationExtension.setTransactionalLock(false);
```

### Gradle

```groovy
flyway {
    pluginConfiguration = [
      postgresqlTransactionalLock: 'false'
    ]
}
```

### Maven

```xml
<configuration>
    <pluginConfiguration>
        <postgresqlTransactionalLock>false</postgresqlTransactionalLock>
    </pluginConfiguration>
</configuration>
```
