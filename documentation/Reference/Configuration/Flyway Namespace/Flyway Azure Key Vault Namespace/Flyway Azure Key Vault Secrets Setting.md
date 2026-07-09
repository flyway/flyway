---
subtitle: flyway.akv.secrets
---

{% include enterprise.html %}

## Description

An array of secret names in [Azure Key Vault](https://azure.microsoft.com/en-us/products/key-vault) that contain Flyway configurations.

Each secret's value should contain Flyway configuration parameters. If multiple secrets specify the same configuration parameter, then the last secret takes precedence.

## Type

String array

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honored, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -akv.secrets="flyway-config-url,flyway-config-creds" info
```

### TOML Configuration File

```toml
[flyway.akv]
secrets = ["flyway-config-url", "flyway-config-creds"]
```

### Configuration File

```properties
flyway.akv.secrets=flyway-config-url,flyway-config-creds
```

### Environment Variable

```properties
FLYWAY_AKV_SECRETS=flyway-config-url,flyway-config-creds
```

### API

```java
AkvConfigurationExtension akvConfigurationExtension = configuration.getConfigurationExtension(AkvConfigurationExtension.class);
AkvModel akv = akvConfigurationExtension.getAkv();
akv.setSecrets(new String[]{"flyway-config-url", "flyway-config-creds"});
```

### Gradle

```groovy
flyway {
    akv = [
        akvSecrets: ['flyway-config-url', 'flyway-config-creds']
    ]
}
```

### Maven

```xml
<configuration>
    <akv>
        <akvSecrets>flyway-config-url,flyway-config-creds</akvSecrets>
    </akv>
</configuration>
```
