---
pill: gcsmProject
subtitle: flyway.gcsm.project
---

# Google Cloud Secret Manager Project
{% include enterprise.html %}

## Description
The GCSM Project that you are storing secrets in

Example: `quixotic-ferret-345678`

## Usage

### Commandline
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
