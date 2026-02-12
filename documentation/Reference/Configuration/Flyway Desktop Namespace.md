---
subtitle: Flyway Desktop
---

This namespace contains the configurations specific to Flyway Desktop.

## Settings

| Setting                                                                                                              | Tier        | Type   | Description                                                                        |
|----------------------------------------------------------------------------------------------------------------------|-------------|--------|------------------------------------------------------------------------------------|
| [`showDeploymentDecision`](<Configuration/Flyway Desktop Namespace/Flyway Desktop Show Deployment Decision Setting>) | Community*  | String | Whether or not to display the deployment method choice UI on the deployment pages. |
| [`development`](<Configuration/Flyway Desktop Namespace/Flyway Desktop Development Setting>)                         | Community*  | String | The name of your development environment.                                          |
| [`shadow`](<Configuration/Flyway Desktop Namespace/Flyway Desktop Shadow Setting>)                                   | Community** | String | The name of your shadow environment.                                               |

## Namespaces

| Namespace                                                                                | Description                                                        |
|------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
| [`generate`](<Configuration/Flyway Desktop Namespace/Flyway Desktop Generate Namespace>) | Settings relating to migration script generation in FlywayDesktop. |

## Deprecated settings

| Setting                                                                                       | Tier       | Type   | Description                               |
|-----------------------------------------------------------------------------------------------|------------|--------|-------------------------------------------|
| [`schemaModel`](<Configuration/Flyway Desktop Namespace/Flyway Desktop Schema Model Setting>) | Community* | String | The location of your schema model folder. |

\* There is no license restriction on this setting strictly speaking, but it is used to configure functionality which is only
available at Teams and above.

\** There is no license restriction on this setting strictly speaking, but it is used to configure functionality which is only
available at Enterprise.