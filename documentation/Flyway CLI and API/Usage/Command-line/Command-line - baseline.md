---
pill: cli_baseline
subtitle: 'Command-line: baseline'
---
# Command-line: baseline

Baselines an existing database, excluding all migrations up to and including `baselineVersion`.

<a href="Commands/baseline"><img src="assets/command-baseline.png" alt="baseline"></a>

## Usage

<pre class="console"><span>&gt;</span> flyway [options] baseline</pre>

## Options

See [configuration](Configuration/parameters) for a full list of supported configuration parameters.

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
flyway.tablespace=my_tablespace
flyway.callbacks=com.mycomp.project.CustomCallback,com.mycomp.project.AnotherCallback
flyway.skipDefaultCallbacks=false
flyway.baselineVersion=1.0
flyway.baselineDescription=Base Migration
flyway.workingDirectory=C:/myProject
flyway.createSchemas=true
flyway.jdbcProperties.myProperty=value
```

## Sample output
<pre class="console">&gt; flyway baseline

Flyway {{ site.flywayVersion }} by Redgate

Creating schema history table: "PUBLIC"."flyway_schema_history"
Schema baselined with version: 1</pre>

## Sample JSON output

<pre class="console">&gt; flyway baseline -outputType=json

{
  "successfullyBaselined": true,
  "baselineVersion": "1",
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "baseline"
}</pre>
