---
layout: commandLine
pill: cli_validate
subtitle: 'Command-line: validate'
---
# Command-line: validate

Validate applied migrations against resolved ones (on the filesystem or classpath)
to detect accidental changes that may prevent the schema(s) from being recreated exactly.

Validation fails if
- differences in migration names, types or checksums are found
- versions have been applied that aren't resolved locally anymore
- versions have been resolved that haven't been applied yet

<a href="/documentation/command/validate"><img src="/assets/balsamiq/command-validate.png" alt="validate"></a>

## Usage

<pre class="console"><span>&gt;</span> flyway [options] validate</pre>

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
flyway.skipDefaultResolvers=false
flyway.callbacks=com.mycomp.project.CustomCallback,com.mycomp.project.AnotherCallback
flyway.skipDefaultCallbacks=false
flyway.target=5.1
flyway.outOfOrder=false
flyway.cleanOnValidationError=false
flyway.oracle.sqlplus=true
flyway.oracle.sqlplusWarn=true
flyway.workingDirectory=C:/myProject
flyway.jdbcProperties.myProperty=value
```

## Sample output

<pre class="console">&gt; flyway validate

Flyway {{ site.flywayVersion }} by Redgate

No migrations applied yet. No validation necessary.</pre>

## Sample JSON output

<pre class="console">&gt; flyway validate -outputType=json

{
  "errorDetails": null,
  "invalidMigrations": [],
  "validationSuccessful": true,
  "validateCount": 2,
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "validate"
}</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/commandline/undo">Command-line: undo <i class="fa fa-arrow-right"></i></a>
</p>
