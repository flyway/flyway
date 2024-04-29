---
subtitle: Rebuild the Schema history table
---

## How to rebuild a Schema history table

You may want to do this if your Schema history table has been accidentally dropped or if you have rebaselined your migrations and the previous deployment information is no longer applicable.

Steps
1) Run the [Baseline command](commands/baseline) to provision the table
2) Populate the table without re-executing your migrations by running a Migrate command with the [Skip Executing Migrations](Configuration/parameters/flyway/skip-executing-migrations) switch enabled.