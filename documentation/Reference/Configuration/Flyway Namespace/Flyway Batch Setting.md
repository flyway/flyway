---
pill: batch
subtitle: flyway.batch
redirect_from: Configuration/batch/
---

## Description

Whether to batch SQL statements when executing them. Batching can save up to 99 percent of network roundtrips by sending up to 100 statements at once over the network to the database, instead of sending each statement individually.

This is particularly useful for very large SQL migrations composed of multiple MB or even GB of reference data, as this can dramatically reduce the network overhead.

This is supported for INSERT, UPDATE, DELETE, MERGE, and UPSERT statements. All other statements are automatically executed without batching.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -batch="true" info
```

### TOML Configuration File

```toml
[flyway]
batch = true
```

### Configuration File

```properties
flyway.batch=true
```

### Environment Variable

```properties
FLYWAY_BATCH=true
```

### API

```java
Flyway.configure()
    .batch(true)
    .load()
```

### Gradle

```groovy
flyway {
    batch = true
}
```

### Maven

```xml
<configuration>
    <batch>true</batch>
</configuration>
```

## Use Cases

### Improving performance by reducing communication overhead

Suppose you have 1000 statements in a migration, and your communication overhead is 0.1 second. Without batching the migrate process would have a 1000 x 0.1 = 100 second overhead in communication (sending the statements) alone. This is a lot of time. With
`batch` enabled, 100 statements can be sent at a time, meaning the communication overhead is only incurred 1000/100 = 10 times, resulting in a 10 x 0.1 = 1 second migrate overhead due to communication.
