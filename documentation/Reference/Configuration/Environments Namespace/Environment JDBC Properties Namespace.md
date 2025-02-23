---
pill: jdbcProperties
subtitle: flyway.jdbcProperties
redirect_from: Configuration/jdbcProperties/
---

## Description

JDBC properties to pass to the JDBC driver when establishing a connection.

## Usage

### Flyway Desktop

These can't be explicitly set via the connection dialog, although `jdbcProperties.accessToken` will be set as part of the [Azure Active Directory Interactive resolver](<Configuration/Environments Namespace/Environment Resolvers Namespace/Azure Active Directory Interactive Resolver>).

### Command-line

To configure via command line if you're using legacy configuration, i.e. your project includes a `.conf` file.
More information about types of configuration files can be found [here](https://documentation.red-gate.com/flyway/flyway-concepts/flyway-projects).

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

Some database JDBC drivers support authentication with access tokens, but this token may not be supported in the URL (see [SQL Server Azure Active Directory](<Database Driver Reference/SQL Server Database>)). You may also not want to leak information such as tokens in the URL. In these cases, an additional properties object can be passed to the JDBC driver which can be configured with
`jdbcProperties` allowing you to achieve, for example, authentication that wasn't previously possible.
