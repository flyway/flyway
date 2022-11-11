---
layout: documentation
menu: configuration
pill: oracleKerberosCacheFile
subtitle: flyway.oracle.kerberosCacheFile
redirect_from: /documentation/configuration/oracleKerberosCacheFile/
---

# Oracle Kerberos Cache File
{% include teams.html %}

## Description
The location of the `krb5cc_<UID>` credential cache file for use in Kerberos authentication. This is optional,
and only has any significance when `kerberosConfigFile` is also specified. It may assist performance.

## Usage

### Commandline
```powershell
./flyway -oracle.kerberosCacheFile="/temp/krb5cc_123" info
```

### Configuration File
```properties
flyway.oracle.kerberosCacheFile=/temp/krb5cc_123
```

### Environment Variable
```properties
FLYWAY_ORACLE_KERBEROS_CACHE_FILE=/temp/krb5cc_123
```

### API
```java
Flyway.configure()
    .oracleKerberosCacheFile("/temp/krb5cc_123")
    .load()
```

### Gradle
```groovy
flyway {
    oracleKerberosCacheFile = '/temp/krb5cc_123'
}
```

### Maven
```xml
<configuration>
    <oracleKerberosCacheFile>/temp/krb5cc_123</oracleKerberosCacheFile>
</configuration>
```
