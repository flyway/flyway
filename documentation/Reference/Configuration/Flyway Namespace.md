---
subtitle: flyway
---

This namespace contains the configurations specific to the Flyway engine.

## General settings

| Setting                                                                                                     | Tier      | Type         | Description                                       |
|-------------------------------------------------------------------------------------------------------------|-----------|--------------|---------------------------------------------------|
| [`communityDBSupportEnabled`](<Configuration/Flyway Namespace/Flyway Community DB Support Enabled Setting>) | Community | Boolean      | Enables support for community databases.          |
| [`environment`](<Configuration/Flyway Namespace/Flyway Environment Setting>)                                | Community | String       | The target environment.                           |
| [`kerberosConfigFile`](<Configuration/Flyway Namespace/Flyway Kerberos Config File Setting>)                | Teams     | String       | The path to the your Kerberos configuration file. |
| [`loggers`](<Configuration/Flyway Namespace/Flyway Loggers Setting>)                                        | Community | String array | Loggers to use.                                   |
| [`reportEnabled`](<Configuration/Flyway Namespace/Flyway Report Enabled Setting>)                           | Community | Boolean      | Whether to enable generating a report file.       |
| [`reportFilename`](<Configuration/Flyway Namespace/Flyway Report Filename Setting>)                         | Community | String       | Filename for the report file.                     |

## Migration location and naming settings

| Setting                                                                                                           | Tier      | Type         | Description                                                               |
|-------------------------------------------------------------------------------------------------------------------|-----------|--------------|---------------------------------------------------------------------------|
| [`baselineMigrationPrefix`](<Configuration/Flyway Namespace/Flyway Baseline Migration Prefix Setting>)            | Community | String       | The file name prefix for baseline migrations.                             |
| [`failOnMissingLocations`](<Configuration/Flyway Namespace/Flyway Fail On Missing Locations Setting>)             | Community | Boolean      | Whether to fail if a location doesn't exist.                              |
| [`jarDirs`](<Configuration/Flyway Namespace/Flyway Jar Dirs Setting>)                                             | Community | String array | Directories containing JDBC drivers and Java-based migrations.            |
| [`locations`](<Configuration/Flyway Namespace/Flyway Locations Setting>)                                          | Community | String array | Locations to scan recursively for migrations.                             |
| [`repeatableSqlMigrationPrefix`](<Configuration/Flyway Namespace/Flyway Repeatable SQL Migration Prefix Setting>) | Community | String       | The file name prefix for repeatable migrations.                           |
| [`sqlMigrationPrefix`](<Configuration/Flyway Namespace/Flyway SQL Migration Prefix Setting>)                      | Community | String       | The file name prefix for versioned migrations.                            |
| [`sqlMigrationSeparator`](<Configuration/Flyway Namespace/Flyway SQL Migration Separator Setting>)                | Community | String       | The file name separator for migrations.                                   |
| [`sqlMigrationSuffixes`](<Configuration/Flyway Namespace/Flyway SQL Migration Suffixes Setting>)                  | Community | String array | The file name suffixes for migrations.                                    |
| [`migrationResolvers`](<Configuration/Flyway Namespace/Flyway Migration Resolvers Setting>)                       | Community | String array | Custom migration resolvers for resolving migrations to apply.             |
| [`skipDefaultResolvers`](<Configuration/Flyway Namespace/Flyway Skip Default Resolvers Setting>)                  | Community | Boolean      | Skip built-in migration resolvers.                                        |
| [`undoSqlMigrationPrefix`](<Configuration/Flyway Namespace/Flyway Undo SQL Migration Prefix Setting>)             | Teams     | String       | The file name prefix for undo migrations.                                 |
| [`validateMigrationNaming`](<Configuration/Flyway Namespace/Flyway Validate Migration Naming Setting>)            | Community | Boolean      | Error on migration files whose names do not match the naming conventions. |

## Migration reading settings

| Setting                                                                                                | Tier      | Type    | Description                                                                       |
|--------------------------------------------------------------------------------------------------------|-----------|---------|-----------------------------------------------------------------------------------|
| [`detectEncoding`](<Configuration/Flyway Namespace/Flyway Detect Encoding Setting>)                    | Community | Boolean | Whether Flyway should attempt to auto-detect the file encoding of each migration. |
| [`encoding`](<Configuration/Flyway Namespace/Flyway Encoding Setting>)                                 | Community | String  | The encoding of migration scripts.                                                |
| [`placeholderPrefix`](<Configuration/Flyway Namespace/Flyway Placeholder Prefix Setting>)              | Community | String  | The prefix of every placeholder.                                                  |
| [`placeholderReplacement`](<Configuration/Flyway Namespace/Flyway Placeholder Replacement Setting>)    | Community | Boolean | Whether placeholders should be replaced.                                          |
| [`placeholderSeparator`](<Configuration/Flyway Namespace/Flyway Placeholder Separator Setting>)        | Community | String  | The separator of default placeholders.                                            |
| [`placeholderSuffix`](<Configuration/Flyway Namespace/Flyway Placeholder Suffix Setting>)              | Community | String  | The suffix of every placeholder.                                                  |
| [`scriptPlaceholderPrefix`](<Configuration/Flyway Namespace/Flyway Script Placeholder Prefix Setting>) | Community | String  | The prefix of every script migration placeholder.                                 |
| [`scriptPlaceholderSuffix`](<Configuration/Flyway Namespace/Flyway Script Placeholder Suffix Setting>) | Community | String  | The suffix of every script migration placeholder.                                 |

## Migration execution settings

| Setting                                                                                                | Tier      | Type         | Description                                                                                   |
|--------------------------------------------------------------------------------------------------------|-----------|--------------|-----------------------------------------------------------------------------------------------|
| [`batch`](<Configuration/Flyway Namespace/Flyway Batch Setting>)                                       | Community | Boolean      | Whether to batch SQL statements when executing them.                                          |
| [`callbackLocations`](<Configuration/Flyway Namespace/Flyway Callback Locations Setting>)              | Community | String array | Locations to scan recursively for callbacks to use to hook into the Flyway lifecycle.         |
| [`callbacks`](<Configuration/Flyway Namespace/Flyway Callbacks Setting>)                               | Community | String array | Callbacks to use to hook into the Flyway lifecycle.                                           |
| [`cherryPick`](<Configuration/Flyway Namespace/Flyway Cherry Pick Setting>)                            | Teams     | String array | A list of migrations that Flyway should consider when migrating.                              |
| [`createSchemas`](<Configuration/Flyway Namespace/Flyway Create Schemas Setting>)                      | Community | Boolean      | Create the configured schemas if they do not exist.                                           |
| [`dryRunOutput`](<Configuration/Flyway Namespace/Flyway Dry Run Output Setting>)                       | Teams     | String       | File path to output a dry run script to.                                                      |
| [`errorOverrides`](<Configuration/Flyway Namespace/Flyway Error Overrides Setting>)                    | Teams     | String array | Rules for the built-in error handler.                                                         |
| [`executeInTransaction`](<Configuration/Flyway Namespace/Flyway Execute In Transaction Setting>)       | Community | Boolean      | Whether to execute scripts within a transaction.                                              |
| [`group`](<Configuration/Flyway Namespace/Flyway Group Setting>)                                       | Community | Boolean      | Whether to group all pending migrations together in the same transaction when applying them.  |
| [`installedBy`](<Configuration/Flyway Namespace/Flyway Installed By Setting>)                          | Community | String       | The username that will be recorded in the schema history table as having applied a migration. |
| [`lockRetryCount`](<Configuration/Flyway Namespace/Flyway Lock Retry Count Setting>)                   | Community | Integer      | Number of times to try and take a lock at 1s intervals when migrating.                        |
| [`mixed`](<Configuration/Flyway Namespace/Flyway Mixed Setting>)                                       | Community | Boolean      | Allow mixing transactional and non-transactional statements within the same migration.        |
| [`outOfOrder`](<Configuration/Flyway Namespace/Flyway Out Of Order Setting>)                           | Community | Boolean      | Allow migrations to be run out of order.                                                      |
| [`outputQueryResults`](<Configuration/Flyway Namespace/Flyway Output Query Results Setting>)           | Community | Boolean      | Output a table with the results of queries when executing migrations.                         |
| [`skipDefaultCallbacks`](<Configuration/Flyway Namespace/Flyway Skip Default Callbacks Setting>)       | Community | Boolean      | Skip built-in callbacks.                                                                      |
| [`skipExecutingMigrations`](<Configuration/Flyway Namespace/Flyway Skip Executing Migrations Setting>) | Community | Boolean      | Skip migration execution.                                                                     |
| [`stream`](<Configuration/Flyway Namespace/Flyway Stream Setting>)                                     | Community | Boolean      | Stream migrations when executing them.                                                        |
| [`target`](<Configuration/Flyway Namespace/Flyway Target Setting>)                                     | Community | String       | The target version up to which to consider migrations.                                        |
| [`validateOnMigrate`](<Configuration/Flyway Namespace/Flyway Validate On Migrate Setting>)             | Community | Boolean      | Run `validate` command when running `migrate` command.                                        |

## Flyway schema history settings

| Setting                                                                           | Tier      | Type   | Description                                                      |
|-----------------------------------------------------------------------------------|-----------|--------|------------------------------------------------------------------|
| [`defaultSchema`](<Configuration/Flyway Namespace/Flyway Default Schema Setting>) | Community | String | The schema in which to create Flyway's schema history table.     |
| [`table`](<Configuration/Flyway Namespace/Flyway Table Setting>)                  | Community | String | The name of Flyway's schema history table.                       |
| [`tablespace`](<Configuration/Flyway Namespace/Flyway Tablespace Setting>)        | Community | String | The tablespace in which to create Flyway's schema history table. |

## Schema model settings

| Setting                                                                                        | Tier       | Type         | Description                                                  |
|------------------------------------------------------------------------------------------------|------------|--------------|--------------------------------------------------------------|
| [`schemaModelLocation`](<Configuration/Flyway Namespace/Flyway Schema Model Location Setting>) | Community* | String       | The location of the schema model folder.                     |
| [`schemaModelSchemas`](<Configuration/Flyway Namespace/Flyway Schema Model Schemas Setting>)   | Community* | String array | schemas that should be supported by the schema model folder. |

## Authentication settings

| Setting                                                          | Tier      | Type   | Description                                                   |
|------------------------------------------------------------------|-----------|--------|---------------------------------------------------------------|
| [`email`](<Configuration/Flyway Namespace/Flyway Email Setting>) | Community | String | Email to be used in conjunction with a personal access token. |
| [`token`](<Configuration/Flyway Namespace/Flyway Token Setting>) | Community | String | Personal access token used for licensing Flyway.              |

## Flyway Pipelines integration settings

| Setting                                                                                         | Tier      | Type    | Description                                                           |
|-------------------------------------------------------------------------------------------------|-----------|---------|-----------------------------------------------------------------------|
| [`checkDriftOnMigrate`](<Configuration/Flyway Namespace/Flyway Check Drift On Migrate Setting>) | Community | Boolean | Enables automatic drift checks on migrate.                            |
| [`pipelineId`](<Configuration/Flyway Namespace/Flyway Pipeline Id Setting>)                     | Community | String  | An id for identifying your pipeline.                                  |
| [`publishResult`](<Configuration/Flyway Namespace/Flyway Publish Result Setting>)               | Community | Boolean | Whether to publish the result of your Flyway run to Flyway Pipelines. |

## Baseline settings

| Setting                                                                                       | Tier      | Type    | Description                                                               |
|-----------------------------------------------------------------------------------------------|-----------|---------|---------------------------------------------------------------------------|
| [`baselineDescription`](<Configuration/Flyway Namespace/Flyway Baseline Description Setting>) | Community | String  | The Description to tag an existing schema with when executing `baseline`. |
| [`baselineOnMigrate`](<Configuration/Flyway Namespace/Flyway Baseline On Migrate Setting>)    | Community | Boolean | Run `baseline` command when running `migrate` command.                    |
| [`baselineVersion`](<Configuration/Flyway Namespace/Flyway Baseline Version Setting>)         | Community | String  | The version to tag an existing schema with when executing `baseline`.     |

## Clean settings

| Setting                                                                           | Tier      | Type    | Description               |
|-----------------------------------------------------------------------------------|-----------|---------|---------------------------|
| [`cleanDisabled`](<Configuration/Flyway Namespace/Flyway Clean Disabled Setting>) | Community | Boolean | Whether to disable clean. |

## Validation settings

| Setting                                                                                                | Tier      | Type         | Description                                                                             |
|--------------------------------------------------------------------------------------------------------|-----------|--------------|-----------------------------------------------------------------------------------------|
| [`ignoreMigrationPatterns`](<Configuration/Flyway Namespace/Flyway Ignore Migration Patterns Setting>) | Community | String array | Ignore migrations during `validate` and `repair` according to a given list of patterns. |

## General namespaces

| Namespace                                                                        | Description                            |
|----------------------------------------------------------------------------------|----------------------------------------|
| [`placeholders`](<Configuration/Flyway Namespace/Flyway Placeholders Namespace>) | Placeholders to replace in migrations. |

## Database-specific namespaces

| Namespace                                                                    | Description                      |
|------------------------------------------------------------------------------|----------------------------------|
| [`clickhouse`](<Configuration/Flyway Namespace/Flyway Clickhouse Namespace>) | Settings specific to Clickhouse. |
| [`db2z`](<Configuration/Flyway Namespace/Flyway DB2 zOS Namespace>)          | Settings specific to DB2 zOS.    |
| [`oracle`](<Configuration/Flyway Namespace/Flyway Oracle Namespace>)         | Settings specific to Oracle.     |
| [`postgresql`](<Configuration/Flyway Namespace/Flyway PostgreSQL Namespace>) | Settings specific to PostgreSQL. |
| [`sqlserver`](<Configuration/Flyway Namespace/Flyway SQL Server Namespace>)  | Settings specific to SQL Server. |

## Command-specific namespaces

| Namespace                                                                | Tier             | Description                                  |
|--------------------------------------------------------------------------|------------------|----------------------------------------------|
| [`add`](<Configuration/Flyway Namespace/Flyway Add Namespace>)           | Community        | Settings specific to the `add` command.      |
| [`check`](<Configuration/Flyway Namespace/Flyway Check Namespace>)       | Teams/Enterprise | Settings specific to the `check` command.    |
| [`deploy`](<Configuration/Flyway Namespace/Flyway Deploy Namespace>)     | Community        | Settings specific to the `deploy` command.   |
| [`diff`](<Configuration/Flyway Namespace/Flyway Diff Namespace>)         | Teams            | Settings specific to the `diff` command.     |
| [`diffText`](<Configuration/Flyway Namespace/Flyway DiffText Namespace>) | Teams            | Settings specific to the `diffText` command. |
| [`generate`](<Configuration/Flyway Namespace/Flyway Generate Namespace>) | Enterprise       | Settings specific to the `generate` command. |
| [`init`](<Configuration/Flyway Namespace/Flyway Init Namespace>)         | Community        | Settings specific to the `init` command.     |
| [`migrate`](<Configuration/Flyway Namespace/Flyway Migrate Namespace>)   | Teams            | Settings specific to the `migrate` command.  |
| [`model`](<Configuration/Flyway Namespace/Flyway Model Namespace>)       | Teams            | Settings specific to the `model` command.    |
| [`prepare`](<Configuration/Flyway Namespace/Flyway Prepare Namespace>)   | Enterprise       | Settings specific to the `prepare` command.  |
| [`snapshot`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace>) | Enterprise       | Settings specific to the `snapshot` command. |
| [`undo`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace>)     | Enterprise       | Settings specific to the `undo` command.     |

## Secrets management namespaces

Usage of [per-environment resolvers](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers)
is preferred.

| Namespace                                                                               | Description                                       |
|-----------------------------------------------------------------------------------------|---------------------------------------------------|
| [`dapr`](<Configuration/Flyway Namespace/Flyway Dapr Namespace>)                        | Settings specific to Dapr.                        |
| [`gcsm`](<Configuration/Flyway Namespace/Flyway Google Cloud Secret Manager Namespace>) | Settings specific to Google Cloud Secret Manager. |
| [`vault`](<Configuration/Flyway Namespace/Flyway Vault Namespace>)                      | Settings specific to Vault.                       |

## Deprecated settings

| Setting                                                                     | Tier  | Type   | Description              |
|-----------------------------------------------------------------------------|-------|--------|--------------------------|
| [`licenseKey`](<Configuration/Flyway Namespace/Flyway License Key Setting>) | Teams | String | Your Flyway license key. |

\* There is no license restriction on this setting strictly speaking, but it is used to configure functionality which is
only
available at Teams and above.