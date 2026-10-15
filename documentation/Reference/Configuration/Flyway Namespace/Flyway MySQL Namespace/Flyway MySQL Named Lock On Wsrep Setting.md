---
subtitle: flyway.mysql.namedLockOnWsrep
---

## Description

Whether named locks (`GET_LOCK`) should be used if `wsrep_on=ON` (e.g. on a Galera cluster).

If false, a row-level lock (`SELECT ... FOR UPDATE`) on the schema history table is used instead.

Named locks are node-local, so they only protect concurrent migrations that reach the same node.
Set this to `true` if your migrations run against a single-writer cluster where all writes go to the same node.
Leave this as `false` if your migrations run against a multi-writer cluster, and you rely on the row-level lock.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honored.

### Command-line

```powershell
./flyway -mysql.namedLockOnWsrep=true info
```

### TOML Configuration File

```toml
[flyway.mysql]
namedLockOnWsrep = true
```

### Configuration File

```properties
flyway.mysql.namedLockOnWsrep=true
```

### Environment Variable

```properties
FLYWAY_MYSQL_NAMED_LOCK_ON_WSREP=true
```

### API

```java
MySQLConfigurationExtension configurationExtension = configuration.getConfigurationExtension(MySQLConfigurationExtension.class);
configurationExtension.setNamedLockOnWsrep(true);
```

### Gradle

```groovy
flyway {
    pluginConfiguration = [
      mysqlNamedLockOnWsrep: 'true'
    ]
}
```

### Maven

```xml
<configuration>
    <pluginConfiguration>
        <mysqlNamedLockOnWsrep>true</mysqlNamedLockOnWsrep>
    </pluginConfiguration>
</configuration>
```
