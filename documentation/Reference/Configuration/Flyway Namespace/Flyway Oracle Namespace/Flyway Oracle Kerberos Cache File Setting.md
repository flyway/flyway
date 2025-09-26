---
subtitle: flyway.oracle.kerberosCacheFile
redirect_from: Configuration/oracleKerberosCacheFile/
---

{% include teams.html %}

## Description

The location of the `krb5cc_<UID>` credential cache file for use in Kerberos authentication. This is optional,
and only has any significance when `kerberosConfigFile` is also specified. It may assist performance.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

Note that Kerberos authentication is not currently supported for operations involving Redgate Comparison technology:

* commands such as `diff`, `check`, `prepare`, `snapshot`
* schema model and migration generation operations in Flyway Desktop

### Command-line

```powershell
./flyway -oracle.kerberosCacheFile="/temp/krb5cc_123" info
```

### TOML Configuration File

```toml
[flyway.oracle]
kerberosCacheFile = "/temp/krb5cc_123"
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
OracleConfigurationExtension oracleConfigurationExtension = configuration.getConfigurationExtension(OracleConfigurationExtension.class);
oracleConfigurationExtension.setKerberosCacheFile("/temp/krb5cc_123");
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
