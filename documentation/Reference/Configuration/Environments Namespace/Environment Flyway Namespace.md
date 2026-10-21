---
subtitle: flyway.environments.*.flyway
---

There are configuration settings that can be defined for all Flyway invocations. However, there are instances where you may want more fine-grained control to specify this for a specific [environment](<Configuration/Environments Namespace>).

For example, changing [`cleanDisabled`](<Configuration/Flyway Namespace/Flyway Clean Disabled Setting>) may be appropriate for your test environment but not for your production environment.

For more information, including how to configure overrides and which settings can be overridden, refer to [Environment Overrides](<Configuration/Environment Overrides>).
