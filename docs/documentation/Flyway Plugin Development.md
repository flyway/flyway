---
layout: default
title: Plugin Development
permalink: /documentation/plugins
---
# Code: Flyway Plugin Development
Between Flyway 9.0.0 and Flyway 10.0.0, the architecture of Flyway has changed significantly to a modularized system. 
This has made it easier to develop plugins for Flyway to expand its capabilities and allow for a more streamlined approach for contributions. 
This tutorial will walk you through the steps of creating a plugin for Flyway 10.0.0.

## Creating a Flyway plugin
To create a Flyway plugin, you will need to create a new Maven module depending on the `flyway-core` module and any other modules related to your database type.
For example, if you are creating a database support plugin for a PostgreSQL variation, you will need to depend on the `flyway-core` and `flyway-database-postgresql` modules.

Plugins are loaded in Flyway via the Flyway Plugin Register which uses the Java `ServiceLoader` to load plugins at runtime. 
To do this you will need a class which implements `org.flywaydb.core.extensibility.Plugin` or a child interface and to create a file in `src/main/resources/META-INF/services` called `org.flywaydb.core.extensibility.Plugin` which contains the fully qualified name of your plugin class.

Example `org.flywaydb.core.extensibility.Plugin`:
```
org.flywaydb.database.cockroachdb.CockroachDBDatabaseType
org.flywaydb.database.postgresql.PostgreSQLDatabaseType
org.flywaydb.database.postgresql.PostgreSQLConfigurationExtension
```


## Plugin types
Flyway is now expandable via the Plugin interface within `flyway-core`. This interface allows flyway to load plugins at runtime to use them to extend its functionality and can be extended to provide helper interfaces for specific functionality.
For the purposes of this tutorial, we will be explaining the following plugin types:
* `PluginMetadata`
* `DatabaseType`
* `ConfigurationExtension`

### PluginMetadata
The `PluginMetadata` interface (`org.flywaydb.core.extensibility.PluginMetadata`) is used to provide Flyway with metadata about a plugin.

### DatabaseType
Open source database plugins have been moved to a separate repository, see: [Contributing Database Compatibility to Flyway](</documentation/communitydb>)

### ConfigurationExtension
The `ConfigurationExtension` interface (`org.flywaydb.core.extensibility.ConfigurationExtension`) is used to provide Flyway with the ability to support new configuration options. This is usually used to provide new configuration options for plugins.
`ConfigurationExtensions` require the following methods to be implemented:
* `String getNamespace()` - This is the namespace for the configuration option under the `flyway` configuration domain. This is used to group configuration options together. For example the configuration `flyway.postgresql.transactional.lock` has the namespace `postgresql`
* `String getConfigurationParameterFromEnvironmentVariable(String environmentVariable)` - This is used to get the configuration parameter from an environment variable. This is used to allow users to set configuration options via environment variables. For example, the configuration `flyway.postgresql.transactional.lock` can be set via the environment variable `FLYWAY_POSTGRESQL_TRANSACTIONAL_LOCK`. This is only necessary for legacy Flyway configuration. If you are using new Flyway configuration, the Environment Variable format will by default be screaming snake case of your configuration option's path. 
