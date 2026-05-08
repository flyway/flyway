---
subtitle: flyway.akv.vaultUrl
---

{% include enterprise.html %}

## Description

The URL of your [Azure Key Vault](https://azure.microsoft.com/en-us/products/key-vault).

Example: `https://my-vault.vault.azure.net`

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -akv.vaultUrl="https://my-vault.vault.azure.net" info
```

### TOML Configuration File

```toml
[flyway.akv]
vaultUrl = "https://my-vault.vault.azure.net"
```

### Configuration File

```properties
flyway.akv.vaultUrl=https://my-vault.vault.azure.net
```

### Environment Variable

```properties
FLYWAY_AKV_VAULT_URL=https://my-vault.vault.azure.net
```

### API

```java
AkvConfigurationExtension akvConfigurationExtension = configuration.getConfigurationExtension(AkvConfigurationExtension.class);
AkvModel akv = akvConfigurationExtension.getAkv();
akv.setVaultUrl("https://my-vault.vault.azure.net");
```

### Gradle

```groovy
flyway {
    akv = [
        akvVaultUrl: 'https://my-vault.vault.azure.net'
    ]
}
```

### Maven

```xml
<configuration>
    <akv>
        <akvVaultUrl>https://my-vault.vault.azure.net</akvVaultUrl>
    </akv>
</configuration>
```
