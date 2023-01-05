---
pill: cli_clean
subtitle: 'Command-line: clean'
---
# Command-line: clean

Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.<br/>
The schemas are cleaned in the order specified by the `schemas` property.

<a href="Commands/clean"><img src="assets/command-clean.png" alt="clean"></a>

## Usage

<pre class="console"><span>&gt;</span> flyway [options] clean</pre>

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

## Limitations

- [SQL Server - no users will be dropped](Supported Databases/SQL Server#limitations)

## Cleaning additional objects
For complicated database structures an accurate dependency graph cannot always be constructed, so not every object is cleaned.
There are also objects that we do not drop as they aren’t always safe to, for example, users in SQL Server.
To clean additional objects, you can add an afterClean [callback](Concepts/Callback concept) defining drop statements. For example afterClean.sql:

```
DROP USER test_user
```
