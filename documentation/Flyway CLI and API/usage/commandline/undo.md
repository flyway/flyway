---
layout: commandLine
pill: cli_undo
subtitle: 'Command-line: undo'
---
# Command-line: undo
{% include teams.html %}

[Undoes](/documentation/command/undo) the most recently applied versioned migration.

<a href="/documentation/command/undo"><img src="/assets/balsamiq/command-undo.png" alt="undo"></a>

## Usage

<pre class="console"><span>&gt;</span> flyway [options] undo</pre>

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
flyway.locations=classpath:com.mycomp.migration,database/migrations,filesystem:/sql-migrations
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
flyway.skipDefaultCallResolvers=false
flyway.callbacks=com.mycomp.project.CustomCallback,com.mycomp.project.AnotherCallback
flyway.skipDefaultCallbacks=false
flyway.outputQueryResults=false
flyway.target=5.1
flyway.mixed=false
flyway.group=false
flyway.cleanOnValidationError=false
flyway.installedBy=my-user
flyway.errorOverrides=99999:17110:E,42001:42001:W
flyway.dryRunOutput=/my/sql/dryrun-outputfile.sql
flyway.lockRetryCount=10
flyway.oracle.sqlplus=true
flyway.oracle.sqlplusWarn=true
flyway.workingDirectory=C:/myProject
flyway.jdbcProperties.myProperty=value
```

## Sample output

<pre class="console">&gt; flyway undo

Flyway {{ site.flywayVersion }} by Redgate

Database: jdbc:h2:file:C:\Programs\flyway-0-SNAPSHOT\flyway.db (H2 1.3)
Current version of schema "PUBLIC": 1
Undoing migration of schema "PUBLIC" to version 1 - First
Successfully undid 1 migration to schema "PUBLIC" (execution time 00:00.024s).</pre>

## Sample JSON output

<pre class="console">&gt; flyway undo -outputType=json

{
  "initialSchemaVersion": "1",
  "targetSchemaVersion": null,
  "schemaName": "public",
  "undoneMigrations": [
    {
      "version": "1",
      "description": "undoFirst",
      "filepath": "C:\\flyway\\sql\\U1__undoFirst.sql",
      "executionTime": 0
    }
  ],
  "migrationsUndone": 1,
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "undo"
}</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/commandline/baseline">Command-line: baseline <i class="fa fa-arrow-right"></i></a>
</p>