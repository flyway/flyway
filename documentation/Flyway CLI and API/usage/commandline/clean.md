---
layout: commandLine
pill: cli_clean
subtitle: 'Command-line: clean'
---
# Command-line: clean

Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.

The schemas are cleaned in the order specified by the [`schemas`](/documentation/configuration/parameters/schemas) and [`defaultSchema`](/documentation/configuration/parameters/defaultSchema) property.

<a href="/documentation/command/clean"><img src="/assets/balsamiq/command-clean.png" alt="clean"></a>

## Usage

<pre class="console"><span>&gt;</span> flyway [options] clean</pre>

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
flyway.callbacks=com.mycomp.project.CustomCallback,com.mycomp.project.AnotherCallback
flyway.skipDefaultCallbacks=false
flyway.cleanDisabled=false
flyway.workingDirectory=C:/myProject
flyway.jdbcProperties.myProperty=value
```

## Sample output
<pre class="console">&gt; flyway clean

Flyway {{ site.flywayVersion }} by Redgate

Cleaned database schema 'PUBLIC' (execution time 00:00.014s)</pre>

## Sample JSON output

<pre class="console">&gt; flyway clean -outputType=json

{
  "schemasCleaned": [
    "public"
  ],
  "schemasDropped": [],
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "clean"
}</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/commandline/info">Command-line: info <i class="fa fa-arrow-right"></i></a>
</p>