---
subtitle: Redgate Compare
---

This namespace contains the configurations relating to database comparisons and deployment script generation.

## Settings

| Setting                                                                                                    | Type         | Description                           |
|------------------------------------------------------------------------------------------------------------|--------------|---------------------------------------|
| [`filterFile`](<Configuration/Redgate Compare Namespace/Redgate Compare Filter File Setting>)              | String       | The path to your Redgate filter file. |
| [`staticDataTables`](<Configuration/Redgate Compare Namespace/Redgate Compare Static Data Tables Setting>) | Object array | Tables to track static data for.      |

## Database-specific namespaces

| Namespace                                                                                      | Description                                                                       |
|------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| [`mysql`](<Configuration/Redgate Compare Namespace/Redgate Compare MySQL Namespace>)           | Configuration relating to MySQL comparison and deployment script generation.      |
| [`oracle`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace>)         | Configuration relating to Oracle comparison and deployment script generation.     |
| [`postgresql`](<Configuration/Redgate Compare Namespace/Redgate Compare PostgreSQL Namespace>) | Configuration relating to PostgreSQL comparison and deployment script generation. |
| [`sqlserver`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace>)  | Configuration relating to SQL Server comparison and deployment script generation. |
