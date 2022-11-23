---
subtitle: Clean
---
# Clean

Drops all objects in the configured schemas.

![Clean](assets/command-clean.png)

Clean is a great help in development and test. It will effectively give you a fresh start, by wiping your configured schemas completely clean. All objects (tables, views, procedures, ...) will be dropped.

Needless to say: **do not use against your production DB!**

## Limitations

- [SQL Server - no users will be dropped](Supported Databases/SQL Server#limitations)

## Usage
See [configuration](Configuration/parameters/#clean) for clean specific configuration parameters.
{% include commandUsage.html command="Clean" %}
