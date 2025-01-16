---
subtitle: redgateCompare.oracle
---

This namespace contains the Oracle-specific configurations relating to database comparisons and deployment script generation.

## Settings

| Setting                                                                                                                          | Type   | Description                                                                |
|----------------------------------------------------------------------------------------------------------------------------------|--------|----------------------------------------------------------------------------|
| [`ignoreRulesFile`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Ignore Rules File Setting>) | String | The path to your Oracle ignore rules file.                                 |
| [`filterFile`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Filter File Setting>)            | String | The path to your Oracle filter file.                                       |
| [`edition`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Edition Setting>)                   | String | Compare objects within a specific edition in addition to the base edition. |

## Database-specific namespaces

| Namespace                                                                                                                                        | Description                                                                                               |
|--------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| [`data.options.comparison`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Data Comparison Options Namespace>) | Configuration relating to Oracle static data comparison.                                                  |
| [`options.behavior`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Behavior Options Namespace>)               | Configuration relating to Oracle database comparison and script generation behavior.                      |
| [`options.ignores`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Ignore Options Namespace>)                  | Configuration relating to the database features/syntax to track when running Oracle database comparisons. |
| [`options.storage`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Storage Options Namespace>)                 | Configuration relating to Oracle storage.                                                                 |
