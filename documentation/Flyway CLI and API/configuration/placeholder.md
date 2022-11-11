---
layout: documentation
menu: placeholders
subtitle: Placeholders
redirect_from: /documentation/placeholders/
---

# Placeholders

Flyway comes with support for placeholder replacement in:

- [SQL migrations](/documentation/configuration/placeholder#sql-migration-placeholders)
- [Script migrations](/documentation/configuration/placeholder#script-migration-placeholders) {% include teams.html %}

# SQL Migration Placeholders
In addition to regular SQL syntax, Flyway also supports placeholder replacement with configurable prefixes and suffixes.
By default it looks for Ant-style placeholders like `${myplaceholder}`. This can be very useful to abstract differences
between environments.

Changing the value of placeholders will cause repeatable migrations to be re-applied on next migrate.

Placeholders are also provided as additional properties to the database connection, so placeholders reserved by your
database (e.g. `serverName` for SQL Server) will be used by the connection.

Placeholders are supported in versioned migrations, repeatable migrations, and SQL callbacks.

## How to configure
Placeholders can be configured through a number of different ways.
- Via environment variables. `FLYWAY_PLACEHOLDERS_MYPLACEHOLDER=value`
- Via configuration parameters. `flyway.placeholders.myplaceholder=value`
- Via the api. `.placeholders(Map.of("myplaceholder", "value"))`

Placeholders are case insensitive, so a placeholder like `${myplaceholder}` can be specified with any of the above techniques.

See [parameters](/documentation/configuration/parameters/#placeholders) for placeholder specific configuration parameters.

## Default placeholders
Flyway also provides default placeholders, whose values are automatically populated:

- `${flyway:defaultSchema}` = The default schema for Flyway
- `${flyway:user}` = The user Flyway will use to connect to the database
- `${flyway:database}` = The name of the database from the connection url
- `${flyway:timestamp}` = The time that Flyway parsed the script, formatted as 'yyyy-MM-dd HH:mm:ss'
- `${flyway:filename}` = The filename of the current script
- `${flyway:workingDirectory}` = The user working directory as defined by the ['user.dir']((https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html)) System Property
- `${flyway:table}` = The name of the Flyway schema history table

### Example
Here is a small example of the supported syntax:

```sql
/* Single line comment */
CREATE TABLE test_user (
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(name)
);

/*
Multi-line
comment
*/

-- Default placeholders
GRANT SELECT ON SCHEMA ${flyway:defaultSchema} TO ${flyway:user};

-- User defined placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
```

# Script Migration Placeholders
{% include teams.html %}

Much like SQL placeholders, Flyway supports placeholder replacement in
[script migrations](/documentation/concepts/migrations#script-migrations). Placeholders can be read
through environment variables in your chosen scripting language and by default are prefixed by `FP__`
and suffixed by `__`. When accessing a placeholder that contains a colon (`:`), you must replace the colon with an underscore (`_`).

### Example
Here are some examples of the supported syntax:

Powershell:
```powershell
echo $env:FP__flyway_filename__
```

Bash:
```bash
echo $FP__flyway_filename__
```

<p class="next-steps">
  <a class="btn btn-primary" href="/documentation/configuration/scriptconfigfiles">Script Config Files <i class="fa fa-arrow-right"></i></a>
</p>
