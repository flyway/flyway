---
subtitle: Flyway Vault Secrets
---

{% include enterprise.html %}

## Description

An array of paths to key-value secrets in [Vault](https://www.vaultproject.io/) that contain Flyway configurations. This must start with the name of the engine and end with the name of the secret.

The resulting form is `{engine_name}/data/{path}/{to}/{secret_name}` for the key-value V2 engine, and
`{engine_name}/{path}/{to}/{secret_name}` for the key-value V1 engine.
If multiple secrets specify the same configuration parameter, then the last secret takes precedence.

Example: `kv/data/flyway/flywayConfig`

## Type

String array

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -vault.secrets="kv/data/flyway/flywayConfig1,kv/flyway/flywayConfig2" info
```

### TOML Configuration File

```toml
[flyway.vault]
secrets = ["kv/data/flyway/flywayConfig1", "kv/flyway/flywayConfig2"]
```

### Configuration File

```properties
flyway.vault.secrets=kv/data/flyway/flywayConfig1,kv/flyway/flywayConfig2
```

### Environment Variable

```properties
FLYWAY_VAULT_SECRETS=kv/data/flyway/flywayConfig1,kv/flyway/flywayConfig2
```

### API

```java
VaultConfigurationExtension vaultConfigurationExtension = configuration.getConfigurationExtension(VaultConfigurationExtension.class);
vaultConfigurationExtension.setVaultSecrets("kv/data/flyway/flywayConfig1", "kv/flyway/flywayConfig2");
```

### Gradle

```groovy
flyway {
    pluginConfiguration = [
      vaultSecrets: ['kv/data/flyway/flywayConfig1', 'kv/flyway/flywayConfig2']
    ]
}
```

### Maven

```xml
<configuration>
    <pluginConfiguration>
      <vaultSecrets>kv/data/flyway/flywayConfig1,kv/flyway/flywayConfig2</vaultSecrets>
    </pluginConfiguration>
</configuration>
```
