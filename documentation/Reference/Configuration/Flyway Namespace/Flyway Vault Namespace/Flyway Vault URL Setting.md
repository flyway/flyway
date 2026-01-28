---
subtitle: Flyway Vault URL
---

{% include enterprise.html %}

## Description

The REST API URL of your [Vault](https://www.vaultproject.io/) server, including the API version. Currently only supports API version v1.

Example: `http://localhost:8200/v1/`

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

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
VaultConfigurationExtension vaultConfigurationExtension = configuration.getConfigurationExtension(VaultConfigurationExtension.class);
vaultConfigurationExtension.setVaultUrl("http://localhost:8200/v1/");
```

### Gradle

```groovy
flyway {
    vault = [
      vaultUrl: 'http://localhost:8200/v1/'
    ]
}
```

### Maven

```xml
<configuration>
    <vault>
        <vaultUrl>http://localhost:8200/v1/</vaultUrl>
    </vault>
</configuration>
```
