---
layout: gradle
pill: gradle_info
subtitle: 'gradle flywayInfo'
---
# Gradle Task: flywayInfo

Prints the details and status information about all the migrations.

<a href="/documentation/command/info"><img src="/assets/balsamiq/command-info.png" alt="info"></a>

## Usage

<pre class="console">&gt; gradle flywayInfo</pre>

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
    locations = ['classpath:migrations1', 'migrations2', 'filesystem:/sql-migrations', 's3:migrationsBucket', 'gcs:migrationsBucket']
    sqlMigrationPrefix = 'Migration-'
    undoSqlMigrationPrefix = 'downgrade'
    repeatableSqlMigrationPrefix = 'RRR'
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
    target = '1.1'
    outOfOrder = false
    workingDirectory = 'C:/myproject'
    jdbcProperties = [
      'someProperty' : 'someValue',
      'someOtherProperty' : 'someOtherValue'
    ]
}
```

## Sample output

<pre class="console">&gt; gradle flywayInfo

Database: jdbc:h2:file:flyway.db (H2 1.3)

+------------+---------+----------------+------+---------------------+---------+----------+
| Category   | Version | Description    | Type | Installed on        | State   | Undoable |
+------------+---------+----------------+------+---------------------+---------+----------+
| Versioned  | 1       | First          | SQL  |                     | Pending | Yes      |
| Versioned  | 1.1     | View           | SQL  |                     | Pending | Yes      |
| Versioned  | 1.2     | Populate table | SQL  |                     | Pending | No       |
+------------+---------+----------------+------+---------------------+---------+----------+</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/gradle/validate">Gradle: validate <i class="fa fa-arrow-right"></i></a>
</p>