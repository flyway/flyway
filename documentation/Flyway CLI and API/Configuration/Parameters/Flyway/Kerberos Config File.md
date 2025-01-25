---
pill: kerberosConfigFile
subtitle: flyway.kerberosConfigFile
---

# Kerberos Config File
{% include teams.html %}

## Description
The path to your Kerberos configuration file (e.g. `krb5.ini`) for use in Kerberos authentication.

_Note: Flyway only supports Kerberos for the Baseline, Clean, Info, Migrate, Repair and Undo commands. Other commands do not support Kerberos._

## Usage

### Commandline
```powershell
./flyway -kerberosConfigFile="/path/to/krb5.ini" info
```

### TOML Configuration File
```toml
[flyway]
kerberosConfigFile = "/path/to/krb5.ini"
```

### Configuration File
```properties
flyway.kerberosConfigFile=/path/to/krb5.ini
```

### Environment Variable
```properties
FLYWAY_KERBEROS_CONFIG_FILE=/path/to/krb5.ini
```

### API
```java
Flyway.configure()
    .kerberosConfigFile("/path/to/krb5.ini")
    .load()
```

### Gradle
```groovy
flyway {
    kerberosConfigFile = '/path/to/krb5.ini'
}
```

### Maven
```xml
<configuration>
    <kerberosConfigFile>/path/to/krb5.ini</kerberosConfigFile>
</configuration>
```
