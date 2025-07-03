---
subtitle: flyway.jdbcProperties
redirect_from: Configuration/jdbcProperties/
---

## Description

JDBC properties to pass to the JDBC driver when establishing a connection.

## Usage

To supply a property `property1` with the value `value1`, you can set `environments.{environment name}.jdbcProperties.key1=value1`. Flyway will then set the `key1` property on the jdbc driver to `value1` when it establishes a connection.

These can often be set via parameters included directly in the [`url`](<Configuration/Environments Namespace/Environment URL Setting>) but it depends on the database driver.

We're going to configure the property `accessToken` for the examples below however this could be any property described in the JDBC driver documentation for the specific database you are working with.
- `accessToken` is a SQL Server parameter that can't be set in the connection URL, it can only be set via the JDBC Properties parameter

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

To configure a named environment via command line when using a TOML configuration, prefix `jdbcProperties` with `environments.{environment name}`

```powershell
./flyway -environments.sample.jdbcProperties.accessToken=my-access-token info
```

**Legacy configuration**

More information about types of configuration files can be found [here](https://documentation.red-gate.com/flyway/flyway-concepts/flyway-projects).
```powershell
./flyway -jdbcProperties.accessToken=my-access-token info
```

### TOML Configuration File

```toml
[environments.sample]
jdbcProperties.accessToken = "my-access-token"
```

### Legacy Configuration File

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

Some database JDBC drivers support authentication with access tokens, but this token may not be supported in the URL (see [SQL Server - Microsoft Entra](<Database Driver Reference/SQL Server Database>)). 

You may also not want to leak information such as tokens in the URL. In these cases, an additional properties object can be passed to the JDBC driver which can be configured with `jdbcProperties`.

Note that `jdbcProperties.accessToken` will be set as part of the [Microsoft Entra Interactive resolver](<Configuration/Environments Namespace/Environment Resolvers Namespace/Microsoft Entra Interactive Resolver>).