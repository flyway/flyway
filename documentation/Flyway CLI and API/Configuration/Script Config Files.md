---
subtitle: Script Config Files
redirect_from: /documentation/scriptconfigfiles/
---
# Script Config Files

It's possible to configure SQL migrations on a per-script basis.

This is achieved by creating a script configuration file in the same folder as the migration. The script configuration
file name must match the migration file name, with the `.conf` suffix added. Script configuration files do **not**
need to be explicitly listed in the main configuration, or the command line.

For example, a migration file `sql/V2__my_script.sql` would have a script configuration file `sql/V2__my_script.sql.conf`.

Script Config Files have a subset of the options from the other ways of configuring Flyway (e.g. `flyway.conf`). See the Reference section at the bottom of this page for the complete list of options.

## Structure

Script config files have the following structure:

```properties
# Settings are simple key-value pairs
key=value
```

## Reference

### encoding

Encoding of this SQL migration. 

Caution: changing the encoding after this migration has been run will invalidate the calculated checksum and require a `flyway repair`.
#### example

```encoding=ISO_8859_1```

### executeInTransaction

Manually determine whether or not to execute this migration in a transaction. 

This is useful for databases like PostgreSQL and SQL Server where certain statements can only execute outside a transaction.
#### example
```executeInTransaction=false```

### placeholderReplacement

Whether this SQL migration should have its Flyway placeholders replaced. This behaves exactly like the global 'placeholderReplacement' parameter but applies only to this script. 

Default: true
#### example

```placeholderReplacement=true```

### shouldExecute (Flyway Teams only)
Whether this migration should be executed or ignored. 

Valid values are 'true', 'false', 'A==B', 'A!=B' (where A,B are values), and combinations of these using `&&` (AND), `||` (OR) and parentheses. 

This migration is executed if the boolean expression evaluates to true, ignored if it evaluates to false, and throws an exception if the expression is invalid. 

#### example

```shouldExecute=(${environment}==dev || ${environment}==test)``` 

will run if '${environment}' is 'dev' or 'test'

Placeholder replacement is also supported in the expression.

```shouldExecute=${should_execute}==true```
