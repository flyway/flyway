---
pill: gradle_validate
subtitle: 'gradle flywayValidate'
---
# Gradle Task: flywayValidate

Validate applied migrations against resolved ones (on the filesystem or classpath)
to detect accidental changes that may prevent the schemas from being recreated exactly.

Validation fails if
- differences in migration names, types or checksums are found
- versions have been applied that aren't resolved locally anymore
- versions have been resolved that haven't been applied yet

<a href="Commands/validate"><img src="assets/command-validate.png" alt="validate"></a>

## Usage

<pre class="console">&gt; gradle flywayValidate</pre>

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
    table = 'schema_history'
    locations = ['classpath:migrations1', 'migrations2', 'filesystem:/sql-migrations']
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
    cleanOnValidationError = false
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

<pre class="console">&gt; gradle flywayValidate -i

Validated 5 migrations (execution time 00:00.030s)</pre>
