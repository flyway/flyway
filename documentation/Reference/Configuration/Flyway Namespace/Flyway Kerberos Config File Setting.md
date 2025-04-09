---
subtitle: flyway.kerberosConfigFile
---

{% include teams.html %}

## Description

The path to the your Kerberos configuration file (e.g. `krb5.ini`) for use in Kerberos authentication.
If this is a relative path, it will be resolved relative to your [working directory](<Command-line Parameters/Working Directory Parameter>).

Note that Kerberos authentication is not currently supported for operations involving Redgate Comparison technology:

* commands such as `diff`, `check`, `prepare`, `snapshot`
* schema model and migration generation operations in Flyway Desktop

_Note: This parameter does [not apply to Native Connectors](https://documentation.red-gate.com/display/FD/Flyway+Native+Connectors+-+MongoDB)._

## Type

String

## Default

<i>none</i>

## Usage

### Command-line

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
