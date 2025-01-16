---
pill: resolvers
subtitle: flyway.resolvers
redirect_from: Configuration/resolvers/
---

## Description

Array of fully qualified class names of custom [MigrationResolver](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/resolver/MigrationResolver.html) implementations to be used in addition to the built-in ones for resolving Migrations to apply.

You must ensure that the resolver is available on the classpath (see [Adding to the classpath](<Usage/Adding to the classpath>)).

## Type

String Array

## Default

`[]`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

Using TOML configuration file

```powershell
./flyway -migrationResolvers="my.resolver.MigrationResolver" info
```

Using conf file

```powershell
./flyway -resolvers="my.resolver.MigrationResolver" info
```

### TOML Configuration File

```toml
[flyway]
migrationResolvers = ["my.resolver.MigrationResolver"]
```

### Configuration File

```properties
flyway.resolvers=my.resolver.MigrationResolver
```

### Environment Variable

```properties
FLYWAY_RESOLVERS=my.resolver.MigrationResolver
```

### API

```java
Flyway.configure()
    .resolvers("my.resolver.MigrationResolver")
    .load()
```

### Gradle

```groovy
flyway {
    resolvers = 'my.resolver.MigrationResolver'
}
```

### Maven

```xml
<configuration>
    <resolvers>my.resolver.MigrationResolver</resolvers>
</configuration>
```
