---
subtitle: redgateCompare.sqlserver
---

This namespace contains the SQL-Server-specific configurations relating to database comparisons and deployment script generation.

## Settings

| Setting                                                                                                                       | Type   | Description                              |
|-------------------------------------------------------------------------------------------------------------------------------|--------|------------------------------------------|
| [`filterFile`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Filter File Setting>) | String | The path to your SQL Server filter file. |

## Database-specific namespaces

| Namespace                                                                                                                                                | Tier       | Description                                                                                                   |
|----------------------------------------------------------------------------------------------------------------------------------------------------------|------------|---------------------------------------------------------------------------------------------------------------|
| [`data.options.comparison`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Comparison Options Namespace>) | Teams      | Configuration relating to SQL Server static data comparison.                                                  |
| [`data.options.deployment`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Deployment Options Namespace>) | Teams      | Configuration relating to SQL Server static data deployment.                                                  |
| [`data.options.mapping`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Mapping Options Namespace>)       | Teams      | Configuration relating to SQL Server static data mapping.                                                     | 
| [`options.behavior`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Behavior Options Namespace>)               | Enterprise | Configuration relating to SQL Server database comparison and script generation behavior.                      | 
| [`options.ignores`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Ignore Options Namespace>)                  | Enterprise | Configuration relating to the database features/syntax to track when running SQL Server database comparisons. | 

## Hard-coded options

The following options are available in [Redgate SQL Compare](https://documentation.red-gate.com/sc) (Flyway uses the same engine under the hood) but are hard-coded in Flyway and cannot be altered:

| Option name                     | Hard-coded value |
|---------------------------------|------------------|
| AddDatabaseUseStatement         | false            |
| DisableAndReenableDdlTriggers   | false            |
| DisableAutoColumnMapping        | false            |
| DoNotOutputCommentHeader        | true             |
| DropAndCreateInsteadOfAlter     | false            |
| IgnoreCertificatesAndCryptoKeys | false            |
| IgnoreChecks                    | false            |
| IgnoreConstraintNames           | false            |
| IgnoreIdentityProperties        | false            |
| IgnoreKeys                      | false            |
| IgnoreTriggerOrder              | false            |
| NoDeploymentLogging             | true             |
| NoErrorHandling                 | true             |
| NoTransactions                  | true             |
| ThrowOnFileParseFailed          | false            |