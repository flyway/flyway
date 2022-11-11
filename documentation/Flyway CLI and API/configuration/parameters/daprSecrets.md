---
layout: documentation
menu: configuration
pill: daprSecrets
subtitle: flyway.daprSecrets
---

# Dapr Secrets
{% include teams.html %}

## Description
A comma-separated list of paths to key-value secrets in a
[Dapr](https://docs.dapr.io/developing-applications/building-blocks/secrets/secrets-overview/) Secret Store that contain
Flyway configurations.

If multiple secrets specify the same configuration parameter, then the last secret takes precedence.

## Usage

### Commandline
```powershell
./flyway -plugins.dapr.secrets="secret1,secret2" info
```

### Configuration File
```properties
flyway.plugins.dapr.secrets=secret1,secret2
```

### Environment Variable
```properties
FLYWAY_PLUGINS_DAPR_SECRETS=secret1,secret2
```

### API
```java
DaprConfigurationExtension daprConfigurationExtension = configuration.getPluginRegister().getPlugin(DaprConfigurationExtension.class)
daprConfigurationExtension.setDaprSecrets("secret1", "secret2");
```

### Gradle
```groovy
flyway {
    pluginConfiguration [
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
