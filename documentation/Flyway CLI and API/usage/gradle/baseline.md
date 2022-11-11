---
layout: gradle
pill: gradle_baseline
subtitle: 'gradle flywayBaseline'
---
# Gradle Task: flywayBaseline

Baselines an existing database, excluding all migrations up to and including baselineVersion.

<a href="/documentation/command/baseline"><img src="/assets/balsamiq/command-baseline.png" alt="baseline"></a>

## Usage

<pre class="console">&gt; gradle flywayBaseline</pre>

## Configuration

See [configuration](/documentation/configuration/parameters) for a full list of supported configuration parameters.

## Sample configuration

```groovy
flyway {
    driver = 'org.hsqldb.jdbcDriver'
    url = 'jdbc:hsqldb:file:/db/flyway_sample;shutdown=true'
    user = 'SA'
    password = 'mySecretPwd'
    connectRetries = 10
    initSql = 'SET ROLE \'myuser\''
    schemas = ['schema1', 'schema2', 'schema3']
    createSchemas=true
    table = 'schema_history'
    tablespace = 'my_tablespace'
    callbacks = ['com.mycompany.proj.CustomCallback', 'com.mycompany.proj.AnotherCallback']
    skipDefaultCallbacks = false
    baselineVersion = 5
    baselineDescription = "Let's go!"
    workingDirectory = 'C:/myproject'
    jdbcProperties = [
      'someProperty' : 'someValue',
      'someOtherProperty' : 'someOtherValue'
    ]
}
```

## Sample output

<pre class="console">&gt; gradle flywayBaseline -i

Creating schema history table: "PUBLIC"."flyway_schema_history"
Schema baselined with version: 1</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/gradle/repair">Gradle: repair <i class="fa fa-arrow-right"></i></a>
</p>