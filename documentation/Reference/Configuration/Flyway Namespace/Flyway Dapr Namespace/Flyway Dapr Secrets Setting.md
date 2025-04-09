---
subtitle: flyway.daprSecrets
---

{% include enterprise.html %}

## Description

An array of paths to key-value secrets in a
[Dapr](https://docs.dapr.io/developing-applications/building-blocks/secrets/secrets-overview/) Secret Store that contain
Flyway configurations.

If multiple secrets specify the same configuration parameter, then the last secret takes precedence.

## Type

String array

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -dapr.secrets="secret1,secret2" info
```

### TOML Configuration File

```toml
[flyway.dapr]
secrets = ["secret1", "secret2"]
```

### Configuration File

```properties
flyway.dapr.secrets=secret1,secret2
```

### Environment Variable

```properties
FLYWAY_DAPR_SECRETS=secret1,secret2
```

### API

```java
DaprConfigurationExtension daprConfigurationExtension = configuration.getPluginRegister().getPlugin(DaprConfigurationExtension.class)
daprConfigurationExtension.setDaprSecrets("secret1", "secret2");
```

### Gradle

```groovy
flyway {
    pluginConfiguration = [
        daprSecrets: ['secret1', 'secret2']
    ]
}
```

### Maven

```xml
<configuration>
    <pluginConfiguration>
        <daprSecrets>secret1,secret2</daprSecrets>
    </pluginConfiguration>
</configuration>
```
