---
subtitle: Flyway Vault Token
---

{% include enterprise.html %}

## Description

The [Vault](https://www.vaultproject.io/) token required to access your secrets.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -vault.token="s.abcdefghijklmnopqrstuvwx" info
```

### TOML Configuration File

```toml
[flyway.vault]
token = "s.abcdefghijklmnopqrstuvwx"
```

### Configuration File

```properties
flyway.vault.token=s.abcdefghijklmnopqrstuvwx
```

### Environment Variable

```properties
FLYWAY_VAULT_TOKEN=s.abcdefghijklmnopqrstuvwx
```

### API

```java
VaultConfigurationExtension vaultConfigurationExtension = configuration.getConfigurationExtension(VaultConfigurationExtension.class);
vaultConfigurationExtension.setVaultToken("s.abcdefghijklmnopqrstuvwx");
```

### Gradle

```groovy
flyway {
    pluginConfiguration = [
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
