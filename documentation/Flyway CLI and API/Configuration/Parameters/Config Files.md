---
pill: configFiles
subtitle: flyway.configFiles
redirect_from: Configuration/Configuration Filess/
---

# Config Files

## Description
The [Flyway configuration](Configuration/Configuration Files) files to load.

These files will be relative to the [Working Directory](Configuration/Parameters/Working Directory).

## Usage

### Commandline
```powershell
./flyway -configFiles="my.conf" info
```

### Configuration File
Not available

### Environment Variable
```properties
FLYWAY_CONFIG_FILES=my.conf
```

### API
Not available

### Gradle
```groovy
flyway {
    configFiles = ['my.conf']
}
```

### Maven
```xml
<configuration>
    <configFiles>
        <configFile>my.conf</configFile>
    </configFiles>
</configuration>
```
