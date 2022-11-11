---
layout: commandLine
pill: cli_info
subtitle: 'Command-line: info'
---
# Command-line: info

Prints the details and status information about all the migrations.

<a href="/documentation/command/info"><img src="/assets/balsamiq/command-info.png" alt="info"></a>

## Usage

<pre class="console"><span>&gt;</span> flyway [options] info</pre>

## Options

See [configuration](/documentation/configuration/parameters) for a full list of supported configuration parameters.

## Sample configuration

```properties
flyway.driver=org.hsqldb.jdbcDriver
flyway.url=jdbc:hsqldb:file:/db/flyway_sample
flyway.user=SA
flyway.password=mySecretPwd
flyway.connectRetries=10
flyway.initSql=SET ROLE 'myuser'
flyway.schemas=schema1,schema2,schema3
flyway.table=schema_history
flyway.locations=classpath:com.mycomp.migration,database/migrations,filesystem:/sql-migrations,s3:migrationsBucket,gcs:migrationsBucket
flyway.sqlMigrationPrefix=Migration-
flyway.undoSqlMigrationPrefix=downgrade
flyway.repeatableSqlMigrationPrefix=RRR
flyway.sqlMigrationSeparator=__
flyway.sqlMigrationSuffixes=.sql,.pkg,.pkb
flyway.encoding=ISO-8859-1
flyway.placeholderReplacement=true
flyway.placeholders.aplaceholder=value
flyway.placeholders.otherplaceholder=value123
flyway.placeholderPrefix=#[
flyway.placeholderSuffix=]
flyway.resolvers=com.mycomp.project.CustomResolver,com.mycomp.project.AnotherResolver
flyway.skipDefaultResolvers=false
flyway.callbacks=com.mycomp.project.CustomCallback,com.mycomp.project.AnotherCallback
flyway.skipDefaultCallbacks=false
flyway.target=5.1
flyway.outOfOrder=false
flyway.workingDirectory=C:/myProject
flyway.jdbcProperties.myProperty=value
```

## Sample output

<pre class="console">&gt; flyway info

Flyway {{ site.flywayVersion }} by Redgate

Database: jdbc:h2:file:flyway.db (H2 1.3)

+------------+---------+----------------+------+---------------------+---------+----------+
| Category   | Version | Description    | Type | Installed on        | State   | Undoable |
+------------+---------+----------------+------+---------------------+---------+----------+
| Versioned  | 1       | First          | SQL  |                     | Pending | Yes      |
| Versioned  | 1.1     | View           | SQL  |                     | Pending | Yes      |
| Versioned  | 1.2     | Populate table | SQL  |                     | Pending | No       |
+------------+---------+----------------+------+---------------------+---------+----------+</pre>

## Sample JSON output

<pre class="console">&gt; flyway info -outputType=json

{
  "schemaVersion": null,
  "schemaName": "public",
  "migrations": [
    {
      "category": "Versioned",
      "version": "1",
      "description": "first",
      "type": "SQL",
      "installedOnUTC": "",
      "state": "Pending",
      "undoable": "No",
      "filepath": "C:\\flyway\\sql\\V1__first.sql",
      "installedBy": "",
      "executionTime": 0
    },
    {
      "category": "Repeatable",
      "version": "",
      "description": "repeatable",
      "type": "SQL",
      "installedOnUTC": "",
      "state": "Pending",
      "undoable": "",
      "filepath": "C:\\flyway\\sql\\R__repeatable.sql",
      "installedBy": "",
      "executionTime": 0
    }
  ],
  "allSchemasEmpty": false,
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "info"
}</pre>

## Filtering output
{% include teams.html %}

The output from `info` can be filtered to only the parts of the history that you care about using the following parameters:
- `infoSinceDate`: Limits info to show only migrations applied after this date, and any unapplied migrations. Must be in the format `dd/MM/yyyy HH:mm` (e.g. `01/12/2020 13:00`)
- `infoUntilDate`: Limits info to show only migrations applied before this date. Must be in the format `dd/MM/yyyy HH:mm` (e.g. `01/12/2020 13:00`)
- `infoSinceVersion`: Limits info to show only migrations greater than or equal to this version, and any repeatable migrations. (e.g `1.1`)
- `infoUntilVersion`: Limits info to show only migrations less than or equal to this version, and any repeatable migrations. (e.g. `1.1`)
- `infoOfState`: Limits info to show only migrations of the provided states. This is a case insensitive, comma-separated list. The valid states can be found at [Migration States](/documentation/concepts/migrations#migration-states).
- `migrationIds`: Suppresses all other output and displays a comma-separated list of migration versions for versioned migrations and descriptions for repeatable migrations. This is equivalent to the expected input to [`cherryPick`](/documentation/configuration/parameters/cherryPick)

Example:
<pre class="console">&gt; flyway info -infoSinceDate="01/12/2020 13:00"
</pre>


<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/commandline/validate">Command-line: validate <i class="fa fa-arrow-right"></i></a>
</p>
