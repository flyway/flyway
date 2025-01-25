---
pill: gradle_clean
subtitle: 'gradle flywayClean'
---
# Gradle Task: flywayClean

Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas. If Flyway automatically created them, then the schemas themselves will be dropped when cleaning.

<a href="Commands/clean"><img src="assets/command-clean.png" alt="clean"></a>

## Usage

<pre class="console">&gt; gradle flywayClean</pre>

## Configuration

See [configuration](Configuration/parameters) for a full list of supported configuration parameters.

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
