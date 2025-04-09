---
subtitle: configuration
redirect_from: Configuration/
---

Here is a complete list of Flyway configurations, organized according to their hierarchy in the TOML configuration file.

Root level settings:

| Setting                                                            | Tier      | Type   | Description                                                      |
|--------------------------------------------------------------------|-----------|--------|------------------------------------------------------------------|
| [`id`](<Configuration/Id Setting>)                      | Community | String | A unique identifier for the project. This should not be altered. |
| [`name`](<Configuration/Name Setting>)                  | Community | String | The display name of the project.                                 |
| [`databaseType`](<Configuration/Database Type Setting>) | Community | String | The type of database being managed by this project.              |

Root level namespaces:

| Namespace                                                     | Description                                                                                     |
|---------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| [`environments`](<Configuration/Environments Namespace>)      | All the database environments used in the development or deployment of this project.            |
| [`flyway`](<Configuration/Flyway Namespace>)                  | The configurations relating to the flyway engine. These are parameters for flyway CLI commands. |
| [`flywayDesktop`](<Configuration/Flyway Desktop Namespace>)   | The configurations specific to Flyway Desktop.                                                  |
| [`redgateCompare`](<Configuration/Redgate Compare Namespace>) | The configurations relating to database comparisons and deployment script generation.           |

Each individual settings page will list how it can be configured.
Settings may be configurable via:
- TOML configuration
- CONF configuration
- Command Line
- Environment variable
- Maven
- Gradle
- API
- Flyway Desktop
