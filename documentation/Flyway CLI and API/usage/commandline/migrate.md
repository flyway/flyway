---
layout: commandLine
pill: cli_migrate
subtitle: 'Command-line: migrate'
---
# Command-line: migrate

Migrates the schema to the latest version. Flyway will create the schema history table automatically if it doesn't
    exist.

<a href="/documentation/command/migrate"><img src="/assets/balsamiq/command-migrate.png" alt="migrate"></a>

## Usage

<pre class="console"><span>&gt;</span> flyway [options] migrate</pre>

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
flyway.defaultSchema=schema1
flyway.schemas=schema1,schema2,schema3
flyway.createSchemas=true
flyway.table=schema_history
flyway.tablespace=my_tablespace
flyway.locations=classpath:com.mycomp.migration,database/migrations,filesystem:/sql-migrations,s3:migrationsBucket,gcs:migrationsBucket
flyway.sqlMigrationPrefix=Migration-
flyway.undoSqlMigrationPrefix=downgrade
flyway.repeatableSqlMigrationPrefix=RRR
flyway.sqlMigrationSeparator=__
flyway.sqlMigrationSuffixes=.sql,.pkg,.pkb
flyway.stream=true
flyway.batch=true
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
flyway.target=5.1
flyway.outOfOrder=false
flyway.outputQueryResults=false
flyway.validateOnMigrate=true
flyway.cleanOnValidationError=false
flyway.mixed=false
flyway.group=false
flyway.cleanDisabled=false
flyway.baselineOnMigrate=false
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

<pre class="console">&gt; flyway migrate

Flyway {{ site.flywayVersion }} by Redgate

Database: jdbc:h2:file:flyway.db (H2 1.3)
Successfully validated 5 migrations (execution time 00:00.010s)
Creating Schema History table: "PUBLIC"."flyway_schema_history"
Current version of schema "PUBLIC": << Empty Schema >>
Migrating schema "PUBLIC" to version 1 - First
Migrating schema "PUBLIC" to version 1.1 - View
Successfully applied 2 migrations to schema "PUBLIC" (execution time 00:00.030s).</pre>

## Sample JSON output

<pre class="console">&gt; flyway migrate -outputType=json

{
  "initialSchemaVersion": null,
  "targetSchemaVersion": "1",
  "schemaName": "public",
  "migrations": [
    {
      "category": "Versioned",
      "version": "1",
      "description": "first",
      "type": "SQL",
      "filepath": "C:\\flyway\\sql\\V1__first.sql",
      "executionTime": 0
    },
    {
      "category": "Repeatable",
      "version": "",
      "description": "repeatable",
      "type": "SQL",
      "filepath": "C:\\flyway\\sql\\R__repeatable.sql",
      "executionTime": 0
    }
  ],
  "migrationsExecuted": 2,
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "migrate"
}</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/commandline/clean">Command-line: clean <i class="fa fa-arrow-right"></i></a>
</p>