---
layout: commandLine
pill: cli_repair
subtitle: 'Command-line: repair'
---
# Command-line: repair

Repairs the Flyway schema history table. This will perform the following actions:
- Remove any failed migrations on databases without DDL transactions<br/>
            (User objects left behind must still be cleaned up manually)
- Realign the checksums, descriptions and types of the applied migrations with the ones of the available migrations
- Mark all missing migrations as **deleted**
    - As a result, `repair` must be given the same [`locations`](/documentation/configuration/parameters/locations) as `migrate`!

<a href="/documentation/command/repair"><img src="/assets/balsamiq/command-repair.png" alt="repair"></a>

## Usage

<pre class="console"><span>&gt;</span> flyway [options] repair</pre>

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
flyway.workingDirectory=C:/myProject
flyway.jdbcProperties.myProperty=value
```

## Sample output

<pre class="console">&gt; flyway repair

Flyway {{ site.flywayVersion }} by Redgate

Repair not necessary. No failed migration detected.</pre>

## Sample JSON output

<pre class="console">&gt; flyway repair -outputType=json

{
  "repairActions": [
    "ALIGNED APPLIED MIGRATION CHECKSUMS"
  ],
  "migrationsRemoved": [],
  "migrationsDeleted": [],
  "migrationsAligned": [
    {
      "version": "1",
      "description": "first",
      "filepath": "C:\\flyway\\sql\\V1__first.sql"
    }
  ],
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "repair"
}</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/api">API <i class="fa fa-arrow-right"></i></a>
</p>
