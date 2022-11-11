---
layout: gradle
pill: gradle_undo
subtitle: 'gradle flywayUndo'
---
# Gradle Task: flywayUndo
{% include teams.html %}

[Undoes](/documentation/command/undo) the most recently applied versioned migration.

<a href="/documentation/command/undo"><img src="/assets/balsamiq/command-undo.png" alt="undo"></a>

## Usage
<pre class="console">&gt; gradle flywayUndo</pre>

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
    table = 'schema_history'
    locations = ['classpath:migrations', 'classpath:db/pkg', 'filesystem:/sql-migrations', 's3:migrationsBucket', 'gcs:migrationsBucket']
    sqlMigrationPrefix = 'Migration-'
    undoSqlMigrationPrefix = 'downgrade'
    sqlMigrationSeparator = '__'
    sqlMigrationSuffixes = ['.sql', '.pkg', '.pkb']
    encoding = 'ISO-8859-1'
    placeholderReplacement = true
    placeholders = [
        'aplaceholder' : 'value',
        'otherplaceholder' : 'value123'
    ]
    placeholderPrefix = '#['
    placeholderSuffix = ']'
    resolvers = ['com.mycompany.proj.CustomResolver', 'com.mycompany.proj.AnotherResolver']
    skipDefaultResolvers = false
    callbacks = ['com.mycompany.proj.CustomCallback', 'com.mycompany.proj.AnotherCallback']
    skipDefaultCallbacks = false
    outputQueryResults = false
    target = '1.1'
    mixed = false
    group = false
    cleanOnValidationError = false
    installedBy = "my-user"
    errorOverrides = ['99999:17110:E', '42001:42001:W']
    dryRunOutput = '/my/sql/dryrun-outputfile.sql'
    lockRetryCount = 10
    oracleSqlplus = true 
    oracleSqlplusWarn = true 
    workingDirectory = 'C:/myproject'
    jdbcProperties = [
      'someProperty' : 'someValue',
      'someOtherProperty' : 'someOtherValue'
    ]
}
```

## Sample output
<pre class="console">&gt; gradle flywayMigrate -i

Database: jdbc:h2:file:C:\Programs\flyway-0-SNAPSHOT\flyway.db (H2 1.3)
Current version of schema "PUBLIC": 1
Undoing migration of schema "PUBLIC" to version 1 - First
Successfully undid 1 migration to schema "PUBLIC" (execution time 00:00.024s).</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/gradle/baseline">Gradle: baseline <i class="fa fa-arrow-right"></i></a>
</p>