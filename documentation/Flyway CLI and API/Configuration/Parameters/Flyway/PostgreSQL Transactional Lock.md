---
pill: postgresqlTransactionalLock
subtitle: flyway.postgresql.transactional.lock
---

# PostgreSQL Transactional Lock

## Description
Whether or not transactional advisory locks should be used with PostgreSQL.

If false, session-level locks will be used instead.

This should be set to `false` for statements such as `CREATE INDEX CONCURRENTLY`.

## Default
true

## Usage

### Commandline
```powershell
./flyway -postgresql.transactional.lock=false info
```

### TOML Configuration File
```toml
[flyway]
[flyway.postgresql.transactional]
lock = false
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
