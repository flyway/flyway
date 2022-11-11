---
layout: documentation
menu: configuration
pill: gcsmProject
subtitle: flyway.gcsm.project
---

# Google Cloud Secret Manager Project
{% include teams.html %}

## Description
The GCSM Project that you are storing secrets in

Example: `quixotic-ferret-345678`

## Usage

### Commandline
```powershell
./flyway -plugins.gcsm.project="quixotic-ferret-345678" info
```

### Configuration File
```properties
flyway.plugins.gcsm.project=quixotic-ferret-345678
```

### Environment Variable
```properties
FLYWAY_PLUGINS_GCSM_PROJECT=quixotic-ferret-345678
```

### API
```java
GcsmConfigurationExtension gcsmConfigurationExtension = configuration.getPluginRegister().getPlugin(GcsmConfigurationExtension.class)
gcsmConfigurationExtension.setGcsmProject("quixotic-ferret-345678");
```

### Gradle
```groovy
flyway {
    pluginConfiguration [
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
