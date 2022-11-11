---
layout: documentation
menu: configuration
pill: licenseKey
subtitle: flyway.licenseKey
redirect_from: /documentation/configuration/licenseKey/
---

# License Key
{% include teams.html %}

## Description
Your Flyway license key (`FL01...`) when using Flyway Teams. This should be 516 alpha numeric characters, beginning with `FL`.

Not yet a Flyway Teams Edition customer? Request your <a href="" data-toggle="modal" data-target="#flyway-trial-license-modal">Flyway trial license key</a> to try out Flyway Teams Edition features free for 28 days.

## Accessing Flyway Teams Edition artifacts

Flyway has different artifacts for Community and Teams edition. Therefore, in order to use Flyway Teams edition, you need to make sure you're using the correct artifact.

Once you're using the right artifact, you need to provide the license key in your configuration.

### Command line & Docker

The Flyway command line contains both Community and Teams edition artifacts. Therefore, you only need to provide your license key in your configuration, and Flyway Teams will start automatically. This also works for the Flyway Docker image.

### Maven Plugin

Update your Maven Plugin dependency to use `org.flywaydb.enterprise.flyway-maven-plugin`.

[See the Flyway Teams Maven plugin entry in Maven Central](https://mvnrepository.com/artifact/org.flywaydb.enterprise/flyway-maven-plugin).

### Gradle Plugin

Update your Gradle Plugin dependency to use `org.flywaydb.enterprise.flyway`.

[See the Flyway Teams Gradle Plugins entry](https://plugins.gradle.org/plugin/org.flywaydb.enterprise.flyway).

### Java API

Update your Maven dependency to use `org.flywaydb.enterprise.flyway-core`.

[See the Flyway Teams core artifact entry in Maven Central](https://mvnrepository.com/artifact/org.flywaydb.enterprise/flyway-core).

## Configuration Reference

### Commandline
```powershell
./flyway -licenseKey="FL01..." info
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
Flyway.configure()
    .licenseKey("FL01...")
    .load()
```

### Gradle
```groovy
flyway {
    licenseKey = 'FL01...'
}
```

### Maven
```xml
<configuration>
    <licenseKey>FL01...</licenseKey>
</configuration>
```