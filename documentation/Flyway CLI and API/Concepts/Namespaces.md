---
subtitle: Namespaces
---

# Namespaces

[Flyway configuration parameters](<Configuration>) are almost universally consistent between configuration and command
line.
So for example, you can run `flyway migrate -baselineOnMigrate=true` or you can set the following in your toml config to
apply to all invocations of migrate:

```toml
[flyway]
baselineOnMigrate = true
``` 

Flyway configuration added under the `flyway` root generally represents broader configuration that applies to multiple
commands (or was added before flyway contained any other namespaces)

Database-specific configuration is namespaced by database, e.g. `-oracle.sqlplus=true`. This is primarily for the
purposes of clarity of organisation.
These configurations can still be specified in the toml config

```toml
[flyway.oracle]
sqlplus = true
``` 

Configuration which applies to a single command only is namespaced by the command name, e.g. `-diff.target=production`.
This is fully distinct from the `target` parameter which applies to the migrate command.

## Namespace escaping

It is worth noting that when executing the flyway command line using PowerShell, the namespaces will need escaping, e.g.
`flyway migrate "-oracle.sqlplus=true"`

## Namespace short-circuiting

Always including command-specific namespaces is redundant and leads to a verbose command line, e.g.
`flyway diff -diff.source=development -diff.target=production`
To this end, flyway allows short-circuiting of namespaces within the context of a command, when the command name is to
the left of the parameters.
That is to say that `flyway diff -source=development -target=production` will work correctly, with `-source=development`
evaluating to `-diff.source=development` and `-target=production` evaluating to `-diff.target=production`
However, you still need to specify the namespace outside the context of the verb, e.g.
`flyway -diff.source=development -diff.target=production diff`

It is worth noting that this also works in the context of chaining of flyway commands:
e.g. `flyway diff -source=development -target=production generate` will work, but namespaces will be required for
`flyway diff generate -diff.source=development -diff.target=production`