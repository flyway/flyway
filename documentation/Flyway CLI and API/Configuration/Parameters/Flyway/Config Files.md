---
pill: configFiles
subtitle: flyway.configFiles
redirect_from: Configuration/Configuration Filess/
---

# Config Files

## Description
The [Flyway configuration](Configuration/Configuration Files) files to load.

These files will be relative to the [Working Directory](Configuration/Parameters/Flyway/Working Directory).

_Note:_ It is possible to have Flyway read from the standard input using the special option `-configFiles=-`, see 
[Configuration from Standard Input](Configuration/Configuration from Standard Input). 
It is simpler to use environment variables and/or variable substitution if you need more dynamic configuration.

## Usage

### Commandline
```powershell
./flyway -configFiles="my.toml" info
```
To pass in multiple files, separate their names with commas:

```powershell
./flyway -configFiles=path/to/myAlternativeConfig.toml,other.toml migrate
```

This will also work with `.conf` config files.

### TOML Configuration File
Not available

### Configuration File
Not available

### Environment Variable
```properties
FLYWAY_CONFIG_FILES=my.toml
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
