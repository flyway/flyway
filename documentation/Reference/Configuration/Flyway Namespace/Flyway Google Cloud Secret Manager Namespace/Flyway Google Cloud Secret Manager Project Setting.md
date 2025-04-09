---
subtitle: flyway.gcsm.project
---

{% include enterprise.html %}

## Description

The GCSM Project that you are storing secrets in

Example: `quixotic-ferret-345678`

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This cannot be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -gcsm.project="quixotic-ferret-345678" info
```

### TOML Configuration File

```toml
[flyway.gcsm]
project = "quixotic-ferret-345678"
```

### Configuration File

```properties
flyway.gcsm.project=quixotic-ferret-345678
```

### Environment Variable

```properties
FLYWAY_GCSM_PROJECT=quixotic-ferret-345678
```

### API

```java
GcsmConfigurationExtension gcsmConfigurationExtension = configuration.getPluginRegister().getPlugin(GcsmConfigurationExtension.class)
gcsmConfigurationExtension.setGcsmProject("quixotic-ferret-345678");
```

### Gradle

```groovy
flyway {
    pluginConfiguration = [
        gcsmProject: 'quixotic-ferret-345678'
    ]
}
```

### Maven

```xml
<configuration>
    <pluginConfiguration>
        <gcsmProject>quixotic-ferret-345678</gcsmProject>
    </pluginConfiguration>
</configuration>
```
