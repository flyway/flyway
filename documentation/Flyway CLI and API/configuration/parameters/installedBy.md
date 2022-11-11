---
layout: documentation
menu: configuration
pill: installedBy
subtitle: flyway.installedBy
redirect_from: /documentation/configuration/installedBy/
---

# Installed By

## Description
The username that will be recorded in the schema history table as having applied the migration.

## Default
<i>Current database user</i>

## Usage

### Commandline
```powershell
./flyway -installedBy="ci-pipeline" clean
```

### Configuration File
```properties
flyway.installedBy=ci-pipeline
```

### Environment Variable
```properties
FLYWAY_INSTALLED_BY=ci-pipeline
```

### API
```java
Flyway.configure()
    .installedBy("ci-pipeline")
    .load()
```

### Gradle
```groovy
flyway {
    installedBy = 'ci-pipeline'
}
```

### Maven
```xml
<configuration>
    <installedBy>ci-pipeline</installedBy>
</configuration>
```