---
pill: mvn_validate
subtitle: 'mvn flyway:validate'
---
# Maven Goal: Validate

Validate applied migrations against resolved ones (on the filesystem or classpath)
to detect accidental changes that may prevent the schemas from being recreated exactly.

Validation fails if
- differences in migration names, types or checksums are found
- versions have been applied that aren't resolved locally anymore
- versions have been resolved that haven't been applied yet

<a href="Commands/validate"><img src="assets/command-validate.png" alt="validate"></a>

## Default Phase

- pre-integration-test

## Usage

<pre class="console"><span>&gt;</span> mvn flyway:validate</pre>

## Configuration

See [configuration](Configuration/parameters) for a full list of supported configuration parameters.

## Sample configuration

```xml
<configuration>
    <driver>org.hsqldb.jdbcDriver</driver>
    <url>jdbc:hsqldb:file:${project.build.directory}/db/flyway_sample;shutdown=true</url>
    <user>SA</user>
    <password>mySecretPwd</password>
    <connectRetries>10</connectRetries>
    <initSql>SET ROLE 'myuser'</initSql>
    <schemas>
        <schema>schema1</schema>
        <schema>schema2</schema>
        <schema>schema3</schema>
    </schemas>
    <table>schema_history</table>
    <locations>
        <location>classpath:migrations1</location>
        <location>migrations2</location>
        <location>filesystem:/sql-migrations</location>
        <location>s3:migrationsBucket</location>
        <location>gcs:migrationsBucket</location>    
    </locations>
    <sqlMigrationPrefix>Migration-</sqlMigrationPrefix>
    <undoSqlMigrationPrefix>downgrade</undoSqlMigrationPrefix>
    <repeatableSqlMigrationPrefix>RRR</repeatableSqlMigrationPrefix>
    <sqlMigrationSeparator>__</sqlMigrationSeparator>
    <sqlMigrationSuffixes>
        <sqlMigrationSuffix>.sql</sqlMigrationSuffix>
        <sqlMigrationSuffix>.pkg</sqlMigrationSuffix>
        <sqlMigrationSuffix>.pkb</sqlMigrationSuffix>
    </sqlMigrationSuffixes>
    <encoding>ISO-8859-1</encoding>
    <placeholderReplacement>true</placeholderReplacement>
    <placeholders>
        <aplaceholder>value</aplaceholder>
        <otherplaceholder>value123</otherplaceholder>
    </placeholders>
    <placeholderPrefix>#[</placeholderPrefix>
    <placeholderSuffix>]</placeholderSuffix>
    <resolvers>
        <resolver>com.mycompany.project.CustomResolver</resolver>
        <resolver>com.mycompany.project.AnotherResolver</resolver>
    </resolvers>
    <skipDefaultResolvers>false</skipDefaultResolvers>
    <callbacks>
        <callback>com.mycompany.project.CustomCallback</callback>
        <callback>com.mycompany.project.AnotherCallback</callback>
    </callbacks>
    <skipDefaultCallbacks>false</skipDefaultCallbacks>
    <target>1.1</target>
    <outOfOrder>false</outOfOrder>
    <cleanOnValidationError>false</cleanOnValidationError>
    <oracle.sqlplus>true</oracle.sqlplus>
    <oracle.sqlplusWarn>true</oracle.sqlplusWarn>
    <skip>false</skip>
    <configFiles>
        <configFile>myConfig.conf</configFile>
        <configFile>other.conf</configFile>
    </configFiles>
    <workingDirectory>/my/working/dir</workingDirectory>
    <jdbcProperties>
      <myProperty>myValue</myProperty>
      <myOtherProperty>myOtherValue</myOtherProperty>
    </jdbcProperties>
</configuration>
```

## Sample output

<pre class="console">&gt; mvn flyway:validate

[INFO] [flyway:validate {execution: default-cli}]
[INFO] Validated 5 migrations (execution time 00:00.030s)</pre>
