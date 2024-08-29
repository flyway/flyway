---
pill: configFileEncoding
subtitle: flyway.configFileEncoding
redirect_from: Configuration/Configuration FilesEncoding/
---

# Config File Encoding

## Description
The file encoding to use when loading [Flyway configuration files](Configuration/Configuration Files).

The encodings that Flyway supports are:

- `US-ASCII`
- `ISO-8859-1`
- `UTF-8`
- `UTF-16BE`
- `UTF-16LE`
- `UTF-16`

All your config files must have the same file encoding.

## Default
UTF-8

## Usage

### Commandline
```powershell
./flyway -configFileEncoding="UTF-16" info
```

### TOML Configuration File
Not available

### Configuration File
Not available

### Environment Variable
```properties
FLYWAY_CONFIG_FILE_ENCODING=UTF-16
```

### API
Not available

### Gradle
```groovy
flyway {
    configFileEncoding = 'UTF-16'
}
```

### Maven
```xml
<configuration>
    <configFileEncoding>UTF-16</configFileEncoding>
</configuration>
```
