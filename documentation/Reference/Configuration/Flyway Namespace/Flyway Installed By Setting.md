---
subtitle: flyway.installedBy
redirect_from: Configuration/installedBy/
---

## Description

The username that will be recorded in the schema history table as having applied the migration.

## Type

String

## Default

<i>Current database user</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -installedBy="ci-pipeline" clean
```

### TOML Configuration File

```toml
[flyway]
installedBy = "ci-pipeline"
```

### Configuration File

```properties
flyway.installedBy=ci-pipeline
```

### Environment Variable

```properties
FLYWAY_INSTALLED_BY=ci-pipeline
```

### API

```java
Flyway.configure()
    .installedBy("ci-pipeline")
    .load()
```

### Gradle

```groovy
flyway {
    installedBy = 'ci-pipeline'
}
```

### Maven

```xml
<configuration>
    <installedBy>ci-pipeline</installedBy>
</configuration>
```
