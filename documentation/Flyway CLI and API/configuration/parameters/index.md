---
layout: documentation
menu: configuration
pill: configuration
subtitle: configuration
redirect_from: /documentation/configuration/
---

# Configuration

Flyway has many different parameters that can be set to configure its behavior. These parameters can be set through a variety of different means, depending on how you are using Flyway.

## Usage

### Command Line
If using the command line, config parameters can be set via command line arguments (e.g. `./flyway -url=jdbc:h2:mem:flyway info`), [configuration files](/documentation/configuration/configfile), or environment variables (e.g. `FLYWAY_URL=jdbc:h2:mem:flyway`).

### Api
If using the api, config parameters can be set via calling methods on the configuration object returned by `Flyway.configure()` (e.g. `Flyway.configure().url("jdbc:h2:mem:flyway").load()`), [configuration files](/documentation/configuration/configfile), or environment variables if the `.envVars()` method is called on the configuration object.

### Maven
If using maven, config parameters can be set on the configuration xml block in the maven config, [configuration files](/documentation/configuration/configfile), or environment variables (e.g. `FLYWAY_URL=jdbc:h2:mem:flyway`).

### Gradle
If using maven, config parameters can be set in the plugin configuration block, [configuration files](/documentation/configuration/configfile), or environment variables (e.g. `FLYWAY_URL=jdbc:h2:mem:flyway`).

## Parameters

### Connection
- [url](/documentation/configuration/parameters/url)
- [user](/documentation/configuration/parameters/user)
- [password](/documentation/configuration/parameters/password)
- [driver](/documentation/configuration/parameters/driver)
- [connectRetries](/documentation/configuration/parameters/connectRetries)
- [connectRetriesInterval](/documentation/configuration/parameters/connectRetriesInterval)
- [initSql](/documentation/configuration/parameters/initSql)
- [jdbcProperties](/documentation/configuration/parameters/jdbcProperties) {% include teams.html %}

### General

- [batch](/documentation/configuration/parameters/batch) {% include teams.html %}
- [callbacks](/documentation/configuration/parameters/callbacks)
- [cherryPick](/documentation/configuration/parameters/cherryPick) {% include teams.html %}
- [configFileEncoding](/documentation/configuration/parameters/configFileEncoding)
- [configFiles](/documentation/configuration/parameters/configFiles)
- [detectEncoding](/documentation/configuration/parameters/detectEncoding) {% include teams.html %}
- [dryRunOutput](/documentation/configuration/parameters/dryRunOutput) {% include teams.html %}
- [encoding](/documentation/configuration/parameters/encoding)
- [errorOverrides](/documentation/configuration/parameters/errorOverrides) {% include teams.html %}
- [group](/documentation/configuration/parameters/group)
- [installedBy](/documentation/configuration/parameters/installedBy)
- [jarDirs](/documentation/configuration/parameters/jarDirs)
- [kerberosConfigFile](/documentation/configuration/parameters/kerberosConfigFile) {% include teams.html %}
- [licenseKey](/documentation/configuration/parameters/licenseKey) {% include teams.html %}
- [locations](/documentation/configuration/parameters/locations)
- [failOnMissingLocations](/documentation/configuration/parameters/failOnMissingLocations)
- [lockRetryCount](/documentation/configuration/parameters/lockRetryCount)
- [loggers](/documentation/configuration/parameters/loggers)
- [mixed](/documentation/configuration/parameters/mixed)
- [outOfOrder](/documentation/configuration/parameters/outOfOrder)
- [outputQueryResults](/documentation/configuration/parameters/outputQueryResults) {% include teams.html %}
- [skipDefaultCallbacks](/documentation/configuration/parameters/skipDefaultCallbacks)
- [skipDefaultResolvers](/documentation/configuration/parameters/skipDefaultResolvers)
- [skipExecutingMigrations](/documentation/configuration/parameters/skipExecutingMigrations) {% include teams.html %}
- [stream](/documentation/configuration/parameters/stream) {% include teams.html %}
- [table](/documentation/configuration/parameters/table)
- [tablespace](/documentation/configuration/parameters/tablespace)
- [target](/documentation/configuration/parameters/target)
- [validateMigrationNaming](/documentation/configuration/parameters/validateMigrationNaming)
- [validateOnMigrate](/documentation/configuration/parameters/validateOnMigrate)
- [workingDirectory](/documentation/configuration/parameters/workingDirectory)

### Schema
- [createSchemas](/documentation/configuration/parameters/createSchemas)
- [defaultSchema](/documentation/configuration/parameters/defaultSchema)
- [schemas](/documentation/configuration/parameters/schemas)

### Baseline
- [baselineDescription](/documentation/configuration/parameters/baselineDescription)
- [baselineOnMigrate](/documentation/configuration/parameters/baselineOnMigrate)
- [baselineVersion](/documentation/configuration/parameters/baselineVersion)

### Clean
- [cleanDisabled](/documentation/configuration/parameters/cleanDisabled)
- [cleanOnValidationError](/documentation/configuration/parameters/cleanOnValidationError)

### Validate
- [ignoreMigrationPatterns](/documentation/configuration/parameters/ignoreMigrationPatterns)

### Migrations
- [repeatableSqlMigrationPrefix](/documentation/configuration/parameters/repeatableSqlMigrationPrefix)
- [resolvers](/documentation/configuration/parameters/resolvers)
- [sqlMigrationPrefix](/documentation/configuration/parameters/sqlMigrationPrefix)
- [sqlMigrationSeparator](/documentation/configuration/parameters/sqlMigrationSeparator)
- [sqlMigrationSuffixes](/documentation/configuration/parameters/sqlMigrationSuffixes)
- [undoSqlMigrationPrefix](/documentation/configuration/parameters/undoSqlMigrationPrefix) {% include teams.html %}
- [baselineMigrationPrefix](/documentation/configuration/parameters/baselineMigrationPrefix) {% include teams.html %}

### Placeholders
- [placeholderPrefix](/documentation/configuration/parameters/placeholderPrefix)
- [scriptPlaceholderPrefix](/documentation/configuration/parameters/scriptPlaceholderPrefix) {% include teams.html %}
- [placeholderReplacement](/documentation/configuration/parameters/placeholderReplacement)
- [placeholders](/documentation/configuration/parameters/placeholders)
- [placeholderSeparator](/documentation/configuration/parameters/placeholderSeparator)
- [placeholderSuffix](/documentation/configuration/parameters/placeholderSuffix)
- [scriptPlaceholderSuffix](/documentation/configuration/parameters/scriptPlaceholderSuffix) {% include teams.html %}

### Command Line
- [color](/documentation/configuration/parameters/cliColor)
- [edition](/documentation/configuration/parameters/edition)

### Oracle
- [oracleSqlPlus](/documentation/configuration/parameters/oracleSqlPlus) {% include teams.html %}
- [oracleSqlPlusWarn](/documentation/configuration/parameters/oracleSqlPlusWarn) {% include teams.html %}
- [oracleKerberosCacheFile](/documentation/configuration/parameters/oracleKerberosCacheFile) {% include teams.html %}
- [oracleWalletLocation](/documentation/configuration/parameters/oracleWalletLocation) {% include teams.html %}

### PostgreSQL
- [postgresqlTransactionalLock](/documentation/configuration/parameters/postgresqlTransactionalLock)

### SQL Server
- [sqlServerKerberosLoginFile](/documentation/configuration/parameters/sqlServerKerberosLoginFile) {% include teams.html %}

### Secrets Management - Dapr Secret Store
- [daprUrl](/documentation/configuration/parameters/daprUrl) {% include teams.html %}
- [daprSecrets](/documentation/configuration/parameters/daprSecrets) {% include teams.html %}

### Secrets Management - Google Cloud Secret Manager
- [gcsmProject](/documentation/configuration/parameters/gcsmProject) {% include teams.html %}
- [gcsmSecrets](/documentation/configuration/parameters/gcsmSecrets) {% include teams.html %}

### Secrets Management - Vault
- [vaultUrl](/documentation/configuration/parameters/vaultUrl) {% include teams.html %}
- [vaultToken](/documentation/configuration/parameters/vaultToken) {% include teams.html %}
- [vaultSecrets](/documentation/configuration/parameters/vaultSecrets) {% include teams.html %}
