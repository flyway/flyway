---
subtitle: flywayDesktop.generate
---

This namespace contains the configurations specific to migration script generation in FlywayDesktop.

## Settings

| Setting                                                                                                                                  | Tier       | Type    | Description                                                                    |
|------------------------------------------------------------------------------------------------------------------------------------------|------------|---------|--------------------------------------------------------------------------------|
| [`undoScripts`](<Configuration/Flyway Desktop Namespace/Flyway Desktop Generate Namespace/Flyway Desktop Generate Undo Scripts Setting>) | Community* | Boolean | Whether to automatically generate undo scripts alongside versioned migrations. |

## Deprecated settings

| Setting                                                                                                                                          | Tier       | Type         | Description                                                       |
|--------------------------------------------------------------------------------------------------------------------------------------------------|------------|--------------|-------------------------------------------------------------------|
| [`repeatableTypes`](<Configuration/Flyway Desktop Namespace/Flyway Desktop Generate Namespace/Flyway Desktop Generate Repeatable Types Setting>) | Community* | String array | List of object types for which to generate repeatable migrations. |

\* There is no license restriction on this setting strictly speaking, but it is used to configure functionality which is only
available at Enterprise.