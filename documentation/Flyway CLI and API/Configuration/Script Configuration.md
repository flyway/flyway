---
subtitle: Script Configuration 
---
# Script Configuration

It is possible to configure SQL migrations on a per-script basis.

This is achieved by creating a script configuration file in the same folder as the migration. The script configuration
file name must match the migration file name, with the `.conf` suffix added. 

Script configuration files do **not** need to be explicitly listed in the main configuration or the command line.

For example, a migration file `V2__my_script.sql` would have a script configuration file `V2__my_script.sql.conf`.

Script Configuration files have a subset of the options from the other ways of configuring Flyway.
These parameters are effective at the level of a specific migration rather than globally. 

## Structure

Script config files have the following structure:

```properties
# Settings are simple key-value pairs
key=value
```

# Available Parameters

## Should Execute 
{% include teams.html %}

Whether this migration should be executed or ignored. 

See [`shouldExecute`](<Configuration/Script Configuration/Should Execute>) parameter for further details.

### Usage

```shouldExecute=(${environment}==test)```

## Placeholder Replacement

Whether this SQL migration should have its Flyway placeholders replaced. This behaves exactly like the global [`placeholderReplacement`](<Configuration/Parameters/Flyway/Placeholder Replacement>) parameter but applies only to this script. 

See the global [`placeholders`](<Configuration/Parameters/Flyway/Placeholders>) parameter for valid values.

### Usage

```placeholderReplacement=true```

## Execute In Transaction

Manually determine whether to execute this migration in a transaction.

See the global [`executeInTransaction`](<Configuration/Parameters/Flyway/Execute in transaction>) parameter for valid values.

This is useful for databases like PostgreSQL and SQL Server where certain statements can only execute outside a transaction.
### Usage

```executeInTransaction=false```

## Encoding

Encoding of this SQL migration.

See the global [`encoding`](Configuration/Parameters/Flyway/Encoding) parameter for valid values.

Caution: changing the encoding after this migration has been run will invalidate the calculated checksum and require a [Repair](/Commands/Repair) to be run.

### Usage

```encoding=ISO_8859_1```
