---
subtitle: flyway.gcsm.secrets
---

{% include enterprise.html %}

## Description

An array of paths to key-value secrets in a [Google Secret Manager](Configuration/flyway-namespace/flyway-google-cloud-secret-manager-namespace) account that contain Flyway configurations.

If multiple secrets specify the same configuration parameter, then the last secret takes precedence.

## Type

String array

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

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
GcsmConfigurationExtension gcsmConfigurationExtension = configuration.getConfigurationExtension(GcsmConfigurationExtension.class);
gcsmConfigurationExtension.setGcsmSecrets("secret1", "secret2");
```

### Gradle

```groovy
flyway {
    gcsm = [
        gcsmSecrets: ['secret1', 'secret2']
    ]
}
```

### Maven

```xml
<configuration>
    <gcsm>
        <gcsmSecrets>secret1,secret2</gcsmSecrets>
    </gcsm>
</configuration>
```
