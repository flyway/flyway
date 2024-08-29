---
pill: vaultUrl
subtitle: flyway.vaultUrl
---

# Vault URL
{% include enterprise.html %}

## Description
The REST API URL of your [Vault](https://www.vaultproject.io/) server, including the API version. Currently only supports API version v1.

Example: `http://localhost:8200/v1/`

## Usage

### Commandline
```powershell
./flyway -vault.url="http://localhost:8200/v1/" info
```

### TOML Configuration File
```toml
[flyway.vault]
url = "http://localhost:8200/v1/"
```

### Configuration File
```properties
flyway.vault.url=http://localhost:8200/v1/
```

### Environment Variable
```properties
FLYWAY_VAULT_URL=http://localhost:8200/v1/
```

### API
```java
VaultConfigurationExtension vaultConfigurationExtension = configuration.getPluginRegister().getPlugin(VaultConfigurationExtension.class)
vaultConfigurationExtension.setVaultUrl("http://localhost:8200/v1/");
```

### Gradle
```groovy
flyway {
    pluginConfiguration = [
      vaultUrl: 'http://localhost:8200/v1/'
    ]
}
```

### Maven
```xml
<configuration>
    <pluginConfiguration>
        <vaultUrl>http://localhost:8200/v1/</vaultUrl>
    </pluginConfiguration>
</configuration>
```
