---
layout: documentation
menu: configuration
pill: vaultToken
subtitle: flyway.vaultToken
---

# Vault Token
{% include teams.html %}

## Description
The [Vault](https://www.vaultproject.io/) token required to access your secrets.

## Usage

### Commandline
```powershell
./flyway -plugins.vault.token="s.abcdefghijklmnopqrstuvwx" info
```

### Configuration File
```properties
flyway.plugins.vault.token=s.abcdefghijklmnopqrstuvwx
```

### Environment Variable
```properties
FLYWAY_PLUGINS_VAULT_TOKEN=s.abcdefghijklmnopqrstuvwx
```

### API
```java
VaultConfigurationExtension vaultConfigurationExtension = configuration.getPluginRegister().getPlugin(VaultConfigurationExtension.class)
vaultConfigurationExtension.setVaultToken("s.abcdefghijklmnopqrstuvwx");
```

### Gradle
```groovy
flyway {
    pluginConfiguration [
      vaultToken: 's.abcdefghijklmnopqrstuvwx'
    ]
}
```

### Maven
```xml
<configuration>
    <pluginConfiguration>
        <vaultToken>s.abcdefghijklmnopqrstuvwx</vaultToken>
    </pluginConfiguration>
</configuration>
```
