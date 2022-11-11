---
layout: documentation
menu: clean
subtitle: Clean
---

# Clean

Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.

The schemas are cleaned in the order specified by the [`schemas`](/documentation/configuration/parameters/schemas) and [`defaultSchema`](/documentation/configuration/parameters/defaultSchema) property.

![Clean](/assets/balsamiq/command-clean.png)

Clean is a great help in development and test. It will effectively give you a fresh start, by wiping your configured schemas completely clean. All objects (tables, views, procedures, ...) will be dropped.

Needless to say: **do not use against your production DB!**

## Limitations

- [SQL Server - no users will be dropped](/documentation/database/sqlserver#limitations)

### Cleaning additional objects

For complicated database structures an accurate dependency graph cannot always be constructed, so not every object is cleaned.
We also have objects we do not drop as they aren't always safe to, for example `users` in SQL Server.
To clean additional objects, you can add an [`afterClean`](/documentation/concepts/callbacks#afterClean) callback defining drop statements, for example `afterClean.sql`:

```sql
DROP USER test_user
```

## Usage
See [configuration](/documentation/configuration/parameters/#clean) for clean specific configuration parameters.
{% include commandUsage.html command="clean" %}

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/command/info">Info <i class="fa fa-arrow-right"></i></a>
</p>
