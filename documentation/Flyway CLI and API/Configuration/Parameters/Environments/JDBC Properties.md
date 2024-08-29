---
pill: jdbcProperties
subtitle: flyway.jdbcProperties
redirect_from: Configuration/jdbcProperties/
---

# JDBC Properties

## Description
JDBC properties to pass to the JDBC driver when establishing a connection.

## Usage

### Commandline
To configure via command line if you're using legacy configuration, i.e. your project includes a `.conf` file. 
More information about types of configuration files can be found [here](Configuration/Configuration Files). 
```powershell
./flyway -jdbcProperties.accessToken=my-access-token info
```

To configure a named environment via command line when using a TOML configuration, prefix `jdbcProperties` with `environments.{environment name}.` for example:
```powershell
./flyway -environments.sample.jdbcProperties.accessToken=my-access-token info
```

### TOML Configuration File
```toml
[environments.default.jdbcProperties]
accessToken = "my-access-token"
```

### Configuration File
```properties
flyway.jdbcProperties.accessToken=my-access-token
```

### Environment Variable
```properties
FLYWAY_JDBC_PROPERTIES_accessToken=access-token
```

### API
```java
Map<String, String> properties = new HashMap<>();
properties.put("accessToken", "access-token");

Flyway.configure()
    .jdbcProperties(properties)
    .load()
```

### Gradle
```groovy
flyway {
    jdbcProperties = ['accessToken' : 'access-token']
}
```

### Maven
```xml
<configuration>
    <jdbcProperties>
        <accessToken>access-token</accessToken>
    </jdbcProperties>
</configuration>
```

## Use Cases

### Passing access tokens

Some database JDBC drivers support authentication with access tokens, but this token may not be supported in the URL (see [SQL Server Azure Active Directory](<Supported Databases/SQL Server Database>)). You may also not want to leak information such as tokens in the URL. In these cases, an additional properties object can be passed to the JDBC driver which can be configured with `jdbcProperties` allowing you to achieve, for example, authentication that wasn't previously possible.
