---
subtitle: Script Configuration 
---

It is possible to configure SQL migrations on a per-script basis.

This is achieved by creating a script configuration file in the same folder as the migration. The script configuration
file name must match the migration file name, with the `.conf` suffix added.

Script configuration files do **not** need to be explicitly listed in the main configuration or the command line.

For example, a migration file `V2__my_script.sql` would have a script configuration file `V2__my_script.sql.conf`.

Script configuration files also apply to Callbacks. For example, the `afterMigrate.sql` callback can have an associated configuration file named `afterMigrate.sql.conf`.

Script Configuration files have a subset of the options from the other ways of configuring Flyway.
These parameters are effective at the level of a specific migration rather than globally.

## Structure

Script config files have the following structure:

```properties
# Settings are simple key-value pairs
key=value
```

## Available Settings

### Dedicated script configuration settings

| Setting                                                  | Tier  | Description                                           |
|----------------------------------------------------------|-------|-------------------------------------------------------|
| [`shouldExecute`](<Script Configuration/Should Execute>) | Teams | Whether this migration should be executed or ignored. |

### Overrides of global configuration settings

| Setting                                                                                             | Tier      | Description                                                              |
|-----------------------------------------------------------------------------------------------------|-----------|--------------------------------------------------------------------------|
| [`placeholderReplacement`](<Configuration/Flyway Namespace/Flyway Placeholder Replacement Setting>) | Community | Whether this SQL migration should have its Flyway placeholders replaced. |
| [`executeInTransaction`](<Configuration/Flyway Namespace/Flyway Execute In Transaction Setting>)    | Community | Whether to execute this migration in a transaction.                      |
| [`encoding`](<Configuration/Flyway Namespace/Flyway Encoding Setting>)                              | Community | Encoding of this SQL migration.                                          |
