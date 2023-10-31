---
subtitle: TOML Configuration File
redirect_from: /documentation/tomlconfig/
---

# TOML Configuration File

## How it works in Flyway

### Loading
By default, Flyway will load TOML files from the following locations:
- {installDir}/conf/flyway.toml
- {installDir}/conf/flyway.user.toml
- {userhome}/flyway.toml
- {userhome}/flyway.user.toml
- {executionDir}/flyway.toml
- {executionDir}/flyway.user.toml

These can be changed by using the [configFiles](Configuration/Parameters/Flyway/Config Files) parameter.

### Root level Parameters
Root level parameters (e.g `locations`) inherit the namespace of the table and are read using just the parameter name.

### Feature Verbs
When using feature verbs (e.g `flyway.check`), you can include that as a table header with the parameters underneath;
```toml
[flyway.check]
deployedSnapshot = "my_snapshot"
```

Or you can include the parameters under the flyway table, with the feature verb added before the parameter.
```toml
[flyway]
check.deployedSnapshot = "my_snapshot"
```

### Root Namespaces
Flyway supports namespaces for `flyway`, `environments` and for other Redgate products.
```toml
[newNamespace]
value="my_value"
```

### Environments
Environments are created using the environment table and hold parameters that are part of that specific deployment target.

### Environment Specific Parameters
There are many parameters that are specific to an environment.

When creating an environment, you will need to set the `flyway.environment` parameter with the environment you wish to use.
```toml
[flyway]
environment="env1"
```

This is a list of all those parameters that are in `[environments]`. Other parameters are read from `[flyway]`.

```toml
[environments.env1]
url = "jdbc:h2:mem:flyway_db"
user = "myuser"
password = "mysecretpassword"
driver = "org.h2.Driver"
schemas = ["schema1", "schema2"]
connectRetries = 10
connectRetriesInterval = 60
initSql = "ALTER SESSION SET NLS_LANGUAGE='ENGLISH';"
jdbcProperties = { accessToken = "access-token" }
resolvers = ["my.resolver.MigrationResolver1", "my.resolver.MigrationResolver2"]
```

### Default Environment
The default environment is an initially loaded environment called `default` that is used if no other environment is specified. It can be configured by the user. Check the [parameters](Parameters/Environments) for their default values.

```toml
[environments.default]
user = "myuser"
password = "mysecretpassword"
```

## Property Resolvers

Configuration values can be retrieved from other sources, such as Hashicorp Vault, Google Secrets or Dapr, using property resolvers. 
More information about property resolvers can be found [here](./Property Resolvers).

## Example of conf to TOML
**flyway.conf**
```properties
flyway.url=jdbc:h2:mem:flyway_db
flyway.user=myuser
flyway.password=mysecretpassword
flyway.locations=filesystem:sql
flyway.check.deployedSnapshot=my_snapshot
```


**flyway.toml**
```toml
[environments.env1]
url = "jdbc:h2:mem:fylway_db"
user = "myuser"
password = "mysecretpassword"

[flyway]
locations = ["filesystem:sql"]

[flyway.check]
deployedSnapshot = "my_snapshot"
```


# FAQ

### Why aren't my parameters in my environment picked up by Flyway? 

You may need to specify the environment under `[flyway]` or from the commandline. For example with an environment called env1, in TOML, you would need to do the following:

```toml
[environments.env1]
...

[flyway]
environment="env1" 
```

In commandline, you would need to do the following:
```powershell
./flyway -environment=env1 migrate
```

### Why are the wrong parameters values being read?

You may have another toml config that is being read which is overwriting the TOML you expect to use.

The default file paths are:
- {installDir}/conf/flyway.toml 
- {userhome}/flyway.toml 
- {executionDir}/flyway.toml

### Why are there other sections in the config?

These belong to other Redgate products and are used by those products. They do not impact Flyway.

### Why am I getting "unknown configuration parameters" errors?

You may have a parameter under the wrong table. Check the namespace it is under and move it to the correct table. Environment specific parameters are listed [here](configuration/parameters/environments).

You may also be trying to configure a teams/enterprise config parameter in OSS edition. Check the [parameters](configuration/parameters) for the list of parameters that are not available in OSS edition.