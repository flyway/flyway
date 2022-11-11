---
layout: maven
pill: mvn_migrate
subtitle: 'mvn flyway:migrate'
---
# Maven Goal: Migrate

Migrates the schema to the latest version. Flyway will create the schema history table automatically if it doesn't
    exist.

<a href="/documentation/command/migrate"><img src="/assets/balsamiq/command-migrate.png" alt="migrate"></a>

## Default Phase

- pre-integration-test

## Usage

<pre class="console">&gt; mvn flyway:migrate</pre>

## Configuration

See [configuration](/documentation/configuration/parameters) for a full list of supported configuration parameters.

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
    <createSchemas>true</createSchemas>
    <table>schema_history</table>
    <tablespace>my_tablespace</tablespace>
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
    <stream>true</stream>
    <batch>true</batch>
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
    <outputQueryResults>false</outputQueryResults>
    <validateOnMigrate>true</validateOnMigrate>
    <cleanOnValidationError>false</cleanOnValidationError>
    <mixed>false</mixed>
    <group>false</group>
    <cleanDisabled>false</cleanDisabled>
    <baselineOnMigrate>false</baselineOnMigrate>
    <baselineVersion>5</baselineVersion>
    <baselineDescription>Let's go!</baselineDescription>
    <installedBy>my-user</installedBy>
    <skip>false</skip>
    <configFiles>
        <configFile>myConfig.conf</configFile>
        <configFile>other.conf</configFile>
    </configFiles>
    <workingDirectory>/my/working/dir</workingDirectory>
    <errorOverrides>
        <errorOverride>99999:17110:E</errorOverride>
        <errorOverride>42001:42001:W</errorOverride>
    </errorOverrides>
    <dryRunOutput>/my/sql/dryrun-outputfile.sql</dryRunOutput>
    <lockRetryCount>10</lockRetryCount>
    <oracle.sqlplus>true</oracle.sqlplus>
    <oracle.sqlplusWarn>true</oracle.sqlplusWarn>
    <jdbcProperties>
      <myProperty>myValue</myProperty>
      <myOtherProperty>myOtherValue</myOtherProperty>
    </jdbcProperties>
</configuration>
```

## Exposed properties
The new database version number is exposed in the `flyway.current` Maven property.

## Sample output
<pre class="console">&gt; mvn compile flyway:migrate

[INFO] [compiler:compile {execution: default-compile}]
[INFO] Nothing to compile - all classes are up to date
[INFO] [flyway:migrate {execution: default-cli}]
[INFO] Current schema version: 0
[INFO] Migrating to version 1
[INFO] Migrating to version 1.1
[INFO] Migrating to version 1.2
[INFO] Migrating to version 1.3
[INFO] Successfully applied 4 migrations (execution time 00:00.091s).</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/maven/clean">Maven: clean <i class="fa fa-arrow-right"></i></a>
</p>