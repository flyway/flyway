---
layout: gradle
pill: gradle_clean
subtitle: 'gradle flywayClean'
---
Gradle Task: flywayClean

Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.

The schemas are cleaned in the order specified by the [`schemas`](/documentation/configuration/parameters/schemas) and [`defaultSchema`](/documentation/configuration/parameters/defaultSchema) property.

<a href="/documentation/command/clean"><img src="/assets/balsamiq/command-clean.png" alt="clean"></a>

## Usage

<pre class="console">&gt; gradle flywayClean</pre>

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
    callbacks = ['com.mycompany.proj.CustomCallback', 'com.mycompany.proj.AnotherCallback']
    skipDefaultCallbacks = false
    cleanDisabled = false
    workingDirectory = 'C:/myproject'
    jdbcProperties = [
      'someProperty' : 'someValue',
      'someOtherProperty' : 'someOtherValue'
    ]
}
```

## Sample output

<pre class="console">&gt; gradle flywayClean -i

Cleaned database schema 'PUBLIC' (execution time 00:00.016s)</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/gradle/info">Gradle: info <i class="fa fa-arrow-right"></i></a>
</p>