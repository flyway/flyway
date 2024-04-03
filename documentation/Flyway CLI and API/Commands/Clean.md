---
subtitle: Clean
---
# Clean

Drops all objects in the configured schemas.

![Clean](assets/command-clean.png)

Clean is a great help in development and test. It will effectively give you a fresh start, by wiping your configured schemas completely clean. All objects (tables, views, procedures, ...) will be dropped.

**Do not use against your production DB!** 

Flyway has some default configuration to try and save you from accidents but it cannot interactively ask you if you really intended to do this.   

## Limitations

- [SQL Server - no users will be dropped](<Supported Databases/SQL Server Database>)

## Cleaning additional objects
For complicated database structures an accurate dependency graph cannot always be constructed, so not every object is cleaned.
There are also objects that we do not drop as they aren’t always safe to, for example, users in SQL Server.
To clean additional objects, you can add an afterClean [callback](Concepts/Callback concept) defining drop statements. For example afterClean.sql:

```
DROP USER test_user
```

## Usage
See [configuration](Configuration/parameters/#clean) for clean specific configuration parameters.
{% include commandUsage.html command="Clean" %}
