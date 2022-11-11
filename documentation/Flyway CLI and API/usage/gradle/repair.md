---
layout: gradle
pill: gradle_repair
subtitle: 'gradle flywayRepair'
---
# Gradle Task: flywayRepair

Repairs the Flyway schema history table. This will perform the following actions:
- Remove any failed migrations on databases without DDL transactions<br/>
  (User objects left behind must still be cleaned up manually)
- Realign the checksums, descriptions and types of the applied migrations with the ones of the available migrations

<a href="/documentation/command/repair"><img src="/assets/balsamiq/command-repair.png" alt="repair"></a>

## Usage

<pre class="console">&gt; gradle flywayRepair</pre>

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
    workingDirectory = 'C:/myproject'
    jdbcProperties = [
      'someProperty' : 'someValue',
      'someOtherProperty' : 'someOtherValue'
    ]
}
```

## Sample output

<pre class="console">&gt; gradle flywayRepair -i

Repair not necessary. No failed migration detected.</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/errorcodes/">Error Codes<i class="fa fa-arrow-right"></i></a>
</p>