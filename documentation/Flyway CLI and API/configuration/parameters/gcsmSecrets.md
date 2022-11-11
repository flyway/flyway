---
layout: documentation
menu: configuration
pill: gcsmSecrets
subtitle: flyway.gcsm.secrets
---

# Google Cloud Secret Manager Secrets
{% include teams.html %}

## Description
A comma-separated list of paths to key-value secrets in a [Google Secret Manager](/documentation/configuration/secretsManagement#google-cloud-secret-manager) account that contain Flyway configurations.

If multiple secrets specify the same configuration parameter, then the last secret takes precedence.

## Usage

### Commandline
```powershell
./flyway -plugins.gcsm.secrets="secret1,secret2" info
```

### Configuration File
```properties
flyway.plugins.gcsm.secrets=secret1,secret2
```

### Environment Variable
```properties
FLYWAY_PLUGINS_GCSM_SECRETS=secret1,secret2
```

### API
```java
GcsmConfigurationExtension gcsmConfigurationExtension = configuration.getPluginRegister().getPlugin(GcsmConfigurationExtension.class)
gcsmConfigurationExtension.setGcsmSecrets("secret1", "secret2");
```

### Gradle
```groovy
flyway {
    pluginConfiguration [
        gcsmSecrets: ['secret1', 'secret2']
    ]
}
```

### Maven
```xml
<configuration>
    <pluginConfiguration>
        <gcsmSecrets>secret1,secret2</gcsmSecrets>
    </pluginConfiguration>
</configuration>
```
