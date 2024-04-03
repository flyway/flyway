---
subtitle: placeholder page
---

### Connection
- [url](configuration/parameters/environments/url)
- [user](configuration/parameters/environments/user)
- [password](configuration/parameters/environments/password)
- [driver](configuration/parameters/environments/driver)
- [connectRetries](configuration/parameters/environments/Connect Retries)
- [connectRetriesInterval](configuration/parameters/environments/Connect Retries Interval)
- [initSql](configuration/parameters/environments/Init SQL)
- [jdbcProperties](configuration/parameters/environments/JDBC Properties)

### General

- [batch](configuration/parameters/flyway/batch) {% include teams.html %}
- [callbacks](configuration/parameters/flyway/callbacks)
- [cherryPick](configuration/parameters/flyway/Cherry Pick) {% include teams.html %}
- [configFileEncoding](configuration/parameters/flyway/Config File Encoding)
- [configFiles](configuration/parameters/flyway/Config Files)
- [detectEncoding](configuration/parameters/flyway/Detect Encoding) {% include teams.html %}
- [dryRunOutput](configuration/parameters/flyway/Dry Run Output) {% include teams.html %}
- [encoding](configuration/parameters/flyway/encoding)
- [environment](configuration/parameters/flyway/environment)
- [errorOverrides](configuration/parameters/flyway/errorOverrides) {% include teams.html %}
- [executeInTransaction](configuration/parameters/flyway/Execute In Transaction)
- [group](configuration/parameters/flyway/group)
- [installedBy](configuration/parameters/flyway/Installed By)
- [jarDirs](<configuration/parameters/flyway/Jar Dirs>)
- [kerberosConfigFile](configuration/parameters/flyway/Kerberos Config File) {% include teams.html %}
- [licenseKey](configuration/parameters/flyway/License Key) {% include teams.html %}
- [locations](configuration/parameters/flyway/locations)
- [failOnMissingLocations](configuration/parameters/flyway/Fail On Missing Locations)
- [lockRetryCount](configuration/parameters/flyway/Lock Retry Count)
- [loggers](configuration/parameters/flyway/loggers)
- [mixed](configuration/parameters/flyway/mixed)
- [outOfOrder](configuration/parameters/flyway/Out Of Order)
- [outputQueryResults](configuration/parameters/flyway/Output Query Results) {% include teams.html %}
- [reportFilename](configuration/parameters/flyway/Report Filename)
- [skipDefaultCallbacks](configuration/parameters/flyway/Skip Default Callbacks)
- [skipDefaultResolvers](configuration/parameters/flyway/Skip Default Resolvers)
- [skipExecutingMigrations](configuration/parameters/flyway/Skip Executing Migrations) {% include teams.html %}
- [stream](configuration/parameters/flyway/stream) {% include teams.html %}
- [table](configuration/parameters/flyway/table)
- [tablespace](configuration/parameters/flyway/tablespace)
- [target](configuration/parameters/flyway/target)
- [validateMigrationNaming](configuration/parameters/flyway/Validate Migration Naming)
- [validateOnMigrate](configuration/parameters/flyway/Validate On Migrate)
- [workingDirectory](configuration/parameters/flyway/Working Directory)

### Schema
- [createSchemas](configuration/parameters/flyway/Create Schemas)
- [defaultSchema](configuration/parameters/flyway/Default Schema)
- [schemas](configuration/parameters/environments/schemas)

### Baseline
- [baselineDescription](configuration/parameters/flyway/Baseline Description)
- [baselineOnMigrate](configuration/parameters/flyway/Baseline On Migrate)
- [baselineVersion](configuration/parameters/flyway/Baseline Version)

### Clean
- [cleanDisabled](configuration/parameters/flyway/Clean Disabled)
- [cleanOnValidationError](configuration/parameters/flyway/Clean On Validation Error)

### Validate
- [ignoreMigrationPatterns](configuration/parameters/flyway/Ignore Migration Patterns)

### Migrations
- [repeatableSqlMigrationPrefix](configuration/parameters/flyway/Repeatable SQL Migration Prefix)
- [resolvers](configuration/parameters/environments/Resolver)
- [sqlMigrationPrefix](configuration/parameters/flyway/SQL Migration Prefix)
- [sqlMigrationSeparator](configuration/parameters/flyway/SQL Migration Separator)
- [sqlMigrationSuffixes](configuration/parameters/flyway/SQL Migration Suffixes)
- [undoSqlMigrationPrefix](configuration/parameters/flyway/Undo SQL Migration Prefix) {% include teams.html %}
- [baselineMigrationPrefix](configuration/parameters/flyway/Baseline Migration Prefix) {% include teams.html %}

### Placeholders
- [placeholderPrefix](configuration/parameters/flyway/Placeholder Prefix)
- [scriptPlaceholderPrefix](configuration/parameters/flyway/Script Placeholder Prefix) {% include teams.html %}
- [placeholderReplacement](configuration/parameters/flyway/Placeholder Replacement)
- [placeholders](configuration/parameters/flyway/placeholders)
- [placeholderSeparator](configuration/parameters/flyway/Placeholder Separator)
- [placeholderSuffix](configuration/parameters/flyway/Placeholder Suffix)
- [scriptPlaceholderSuffix](configuration/parameters/flyway/Script Placeholder Suffix) {% include teams.html %}

### Command Line
- [color](configuration/parameters/flyway/Color)
- [outputType](configuration/parameters/flyway/outputtype)

### Check
- [buildUrl](configuration/parameters/flyway/check/Build URL)
- [buildUser](configuration/parameters/flyway/check/Build User)
- [buildPassword](configuration/parameters/flyway/check/Build Password)
- [nextSnapshot](configuration/parameters/flyway/check/Next Snapshot)
- [deployedSnapshot](configuration/parameters/flyway/check/Deployed Snapshot)
- [appliedMigrations](configuration/parameters/flyway/check/Applied Migrations)
- [failOnDrift](configuration/parameters/flyway/check/Fail On Drift)
- [filterFile](configuration/parameters/flyway/check/Filter File)
- [minorTolerance](configuration/parameters/flyway/check/Minor Tolerance)
- [majorTolerance](configuration/parameters/flyway/check/Major Tolerance)
- [minorRules](configuration/parameters/flyway/check/Minor Rules)
- [majorRules](configuration/parameters/flyway/check/Major Rules)
- [rulesLocation](configuration/parameters/flyway/check/Rules Location)

### Oracle
- [oracleSqlPlus](configuration/parameters/flyway/oracle/Oracle SQLPlus) {% include teams.html %}
- [oracleSqlPlusWarn](configuration/parameters/flyway/oracle/Oracle SQLPlus Warn) {% include teams.html %}
- [oracleKerberosCacheFile](configuration/parameters/flyway/oracle/Oracle Kerberos Cache File) {% include teams.html %}
- [oracleWalletLocation](configuration/parameters/flyway/oracle/Oracle Wallet Location) {% include teams.html %}

### PostgreSQL
- [postgresqlTransactionalLock](configuration/parameters/flyway/PostgreSQL Transactional Lock)

### SQL Server
- [sqlServerKerberosLoginFile](configuration/parameters/flyway/SQL Server Kerberos Login File) {% include teams.html %}

### Secrets Management - Dapr Secret Store
- [daprUrl](configuration/parameters/flyway/Dapr URL) {% include enterprise.html %}
- [daprSecrets](configuration/parameters/flyway/dapr-secrets) {% include enterprise.html %}

### Secrets Management - Google Cloud Secret Manager
- [gcsmProject](configuration/parameters/flyway/Google Cloud Secret Manager Project) {% include enterprise.html %}
- [gcsmSecrets](configuration/parameters/flyway/Google Cloud Secret Manager Secrets) {% include enterprise.html %}

### Secrets Management - Vault
- [vaultUrl](configuration/parameters/flyway/Vault Url) {% include enterprise.html %}
- [vaultToken](configuration/parameters/flyway/Vault Token) {% include enterprise.html %}
- [vaultSecrets](configuration/parameters/flyway/Vault Secrets) {% include enterprise.html %}
