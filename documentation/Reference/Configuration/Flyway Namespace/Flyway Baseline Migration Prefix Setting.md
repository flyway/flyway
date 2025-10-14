---
subtitle: flyway.baselineMigrationPrefix
---

## Description

The file name prefix for baseline migrations.

Baseline migrations represent all migrations with
`version <= current baseline migration version` while keeping older migrations if needed for upgrading older deployments.

They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to B1.1__My_description.sql

## Type

String

## Default

`"B"`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -baselineMigrationPrefix="IB" info
```

### TOML Configuration File

```toml
[flyway]
baselineMigrationPrefix = "IB"
```

### Configuration File

```properties
flyway.baselineMigrationPrefix=IB
```

### Environment Variable

```properties
FLYWAY_BASELINE_MIGRATION_PREFIX=IB
```

### API

```java
BaselineMigrationConfigurationExtension baselineMigrationConfigurationExtension = configuration.getConfigurationExtension(BaselineMigrationConfigurationExtension.class);
baselineMigrationConfigurationExtension.setBaselineMigrationPrefix("IB");
```

### Gradle

```groovy
flyway {
    pluginConfiguration = [
      baselineMigrationPrefix: 'IB'
    ]
}
```

### Maven

```xml
<configuration>
    <pluginConfiguration>
        <baselineMigrationPrefix>IB</baselineMigrationPrefix>
    </pluginConfiguration>
</configuration>
```
