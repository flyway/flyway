---
pill: licenseKey
subtitle: flyway.licenseKey
redirect_from: Configuration/licenseKey/
---

# License Key
{% include redgate.html %}

This is how legacy Flyway licensing is managed to enable features in Flyway.

Flyway V10 brings online authentication - see [Flyway Auth](Commands/Auth)
## Description

Your Flyway license key (`FL01...`) when using Flyway Teams. This should be 516 alpha numeric characters, beginning with `FL`.

Not yet a Flyway Teams Edition customer? Request your [Flyway trial license](https://www.red-gate.com/products/flyway/teams/trial/) to try out Flyway Teams Edition features free for 28 days.

### Command line & Docker

There is a single Flyway command line download for all editions. Therefore, you only need to provide your license key in your configuration, and Flyway Teams will start automatically. This also works for the Flyway Docker image.

### Maven Plugin
See [Maven Goal](Usage/Maven Goal)

### Gradle Plugin
See [Gradle Task](Usage/Gradle Task)

### Java API
See [API (Java)](Usage/API Java)

## Configuration Reference

### Commandline
```powershell
./flyway -licenseKey="FL01..." info
```

### TOML Configuration File
```toml
[flyway]
licenseKey = "FL01..."
```

### Configuration File
```properties
flyway.licenseKey=FL01...
```

### Environment Variable
```properties
FLYWAY_LICENSE_KEY=FL01...
```

### API
```java
Flyway flyway = Flyway.configure().load();
flyway.getConfigurationExtension(LicensingConfigurationExtension.class)
        .setLicenseKey("FL01...");  
```

### Gradle
```groovy
flyway {
    pluginConfiguration = [
            licenseKey: 'FL01...'
    ]
}
```

### Maven
```xml
<configuration>
    <pluginConfiguration>
        <licenseKey>FL01...</licenseKey>
    </pluginConfiguration>
</configuration>
```
