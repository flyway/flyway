---
subtitle: flyway.daprUrl
---

{% include enterprise.html %}

## Description

The REST API URL of your [Dapr](https://docs.dapr.io/developing-applications/building-blocks/secrets/secrets-overview/) sidecar,

Example: `http://localhost:3500/v1.0/secrets/my-secrets-store`

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -dapr.url="http://localhost:3500/v1.0/secrets/my-secrets-store" info
```

### TOML Configuration File

```toml
[flyway.dapr]
url = "http://localhost:3500/v1.0/secrets/my-secrets-store"
```

### Configuration File

```properties
flyway.dapr.url=http://localhost:3500/v1.0/secrets/my-secrets-store
```

### Environment Variable

```properties
FLYWAY_DAPR_URL=http://localhost:3500/v1.0/secrets/my-secrets-store
```

### API

```java
DaprConfigurationExtension daprConfigurationExtension = configuration.getPluginRegister().getPlugin(DaprConfigurationExtension.class)
daprConfigurationExtension.setDaprUrl("http://localhost:3500/v1.0/secrets/my-secrets-store");
```

### Gradle

```groovy
flyway {
    pluginConfiguration = [
        daprUrl: 'http://localhost:3500/v1.0/secrets/my-secrets-store'
    ]
}
```

### Maven

```xml
<configuration>
    <pluginConfiguration>
        <daprUrl>http://localhost:3500/v1.0/secrets/my-secrets-store</daprUrl>
    </pluginConfiguration>
</configuration>
```
