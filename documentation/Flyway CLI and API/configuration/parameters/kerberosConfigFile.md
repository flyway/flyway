---
layout: documentation
menu: configuration
pill: kerberosConfigFile
subtitle: flyway.kerberosConfigFile
---

# Kerberos Config File
{% include teams.html %}

## Description
The path to the your Kerberos configuration file (e.g. `krb5.ini`) for use in Kerberos authentication.

## Usage

### Commandline
```powershell
./flyway -kerberosConfigFile="/path/to/krb5.ini" info
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
