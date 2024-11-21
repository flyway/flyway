---
pill: gradle_migrate
subtitle: 'gradle flywayMigrate'
---
# Gradle Task: flywayMigrate

Migrates the schema to the latest version. Flyway will create the schema history table automatically if it doesn't
    exist.

<a href="Commands/migrate"><img src="assets/command-migrate.png" alt="migrate"></a>

## Usage
<pre class="console">&gt; gradle flywayMigrate</pre>

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
    createSchemas=true
    table = 'schema_history'
    tablespace = 'my_tablespace'
    locations = ['classpath:migrations', 'classpath:db/pkg', 'filesystem:/sql-migrations', 's3:migrationsBucket', 'gcs:migrationsBucket']
    sqlMigrationPrefix = 'Migration-'
    undoSqlMigrationPrefix = 'downgrade'
    repeatableSqlMigrationPrefix = 'RRR'
    sqlMigrationSeparator = '__'
    sqlMigrationSuffixes = ['.sql', '.pkg', '.pkb']
    stream = true
    batch = true
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
    outputQueryResults = false
    validateOnMigrate = true
    mixed = false
    group = false
    cleanDisabled = false
    baselineOnMigrate = false
    baselineVersion = 5
    baselineDescription = "Let's go!"
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

<pre class="console">> gradle flywayMigrate -i

Current schema version: 0
Migrating to version 1
Migrating to version 1.1
Migrating to version 1.2
Migrating to version 1.3
Successfully applied 4 migrations (execution time 00:00.091s).</pre>
