---
subtitle: redgateCompare.sqlserver.data.options.comparison
---

{% include enterprise.html %}

This namespace contains the configurations relating to SQL Server static data deployment.

## Settings

| Setting                                                                                                                                                                                                                                       | Type    | Description                                                                                  |
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|----------------------------------------------------------------------------------------------|
| [`disableDdlTriggers`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Deployment Options Namespace/SQL Server Data disable DDL Triggers Setting>)                                              | Boolean | Disables then re-enables DDL triggers in the deployment script.                              |
| [`disableDmlTriggers`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Deployment Options Namespace/SQL Server Data disable DML Triggers Setting>)                                              | Boolean | Disables then re-enables DML triggers on tables and views in the deployment script.          |
| [`disableForeignKeys`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Deployment Options Namespace/SQL Server Data Disable Foreign Keys Setting>)                                              | Boolean | Disables then re-enables foreign keys in the deployment script.                              |
| [`dontIncludeCommentsInScript`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Deployment Options Namespace/SQL Server Data Dont Include Comments In Script Setting>)                          | Boolean | Do not include the comments in the deployment script.                                        |
| [`dropPrimaryKeysIndexesAndUniqueConstraints`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Deployment Options Namespace/SQL Server Data Drop Primary Keys Indexes And Constraints Setting>) | Boolean | Drops then recreates primary keys, indexes, and unique constraints in the deployment script. |
| [`reseedIdentityColumns`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Deployment Options Namespace/SQL Server Data Reseed Identity Columns Setting>)                                        | Boolean | Reseeds identity values in the target database after deployment.                             |
| [`skipIntegrityChecksForForeignKeys`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Deployment Options Namespace/SQL Server Data Skip Integrity Checks For Foreign Keys Setting>)             | Boolean | Uses `WITH NOCHECK` to skip integrity checks for foreign key constraints.                    |
| [`transportClrDataTypesAsBinary`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Deployment Options Namespace/SQL Server Data Transport CLR Data Types As Binary Setting>)                     | Boolean | Uses the binary representation of CLR types in the deployment script.                        |