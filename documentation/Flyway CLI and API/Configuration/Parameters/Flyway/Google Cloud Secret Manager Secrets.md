---
pill: gcsmSecrets
subtitle: flyway.gcsm.secrets
---

# Google Cloud Secret Manager Secrets
{% include enterprise.html %}

## Description
A comma-separated list of paths to key-value secrets in a [Google Secret Manager](Configuration/Secrets Management#google-cloud-secret-manager) account that contain Flyway configurations.

If multiple secrets specify the same configuration parameter, then the last secret takes precedence.

## Usage

### Commandline
```powershell
./flyway -gcsm.secrets="secret1,secret2" info
```

### TOML Configuration File
```toml
[flyway.gcsm]
secrets = ["secret1", "secret2"]
```

### Configuration File
```properties
flyway.gcsm.secrets=secret1,secret2
```

### Environment Variable
```properties
FLYWAY_GCSM_SECRETS=secret1,secret2
```

### API
```java
GcsmConfigurationExtension gcsmConfigurationExtension = configuration.getPluginRegister().getPlugin(GcsmConfigurationExtension.class)
gcsmConfigurationExtension.setGcsmSecrets("secret1", "secret2");
```

### Gradle
```groovy
flyway {
    pluginConfiguration = [
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
