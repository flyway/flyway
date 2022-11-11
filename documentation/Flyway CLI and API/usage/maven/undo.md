---
layout: maven
pill: mvn_undo
subtitle: 'mvn flyway:undo'
---
# Maven Goal: Undo
{% include teams.html %}

[Undoes](/documentation/command/undo) the most recently applied versioned migration.

<a href="/documentation/command/undo"><img src="/assets/balsamiq/command-undo.png" alt="undo"></a>

## Usage

<pre class="console">&gt; mvn flyway:undo</pre>

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
    <outputQueryResults>false</outputQueryResults>
    <target>1.1</target>
    <mixed>false</mixed>
    <group>false</group>
    <cleanOnValidationError>false</cleanOnValidationError>
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
<pre class="console">&gt; mvn compile flyway:undo

[INFO] [compiler:compile {execution: default-compile}]
[INFO] Nothing to compile - all classes are up to date
[INFO] [flyway:migrate {execution: default-cli}]
[INFO] Database: jdbc:h2:file:C:\Programs\flyway-0-SNAPSHOT\flyway.db (H2 1.3)
[INFO] Current version of schema "PUBLIC": 1
[INFO] Undoing migration of schema "PUBLIC" to version 1 - First
[INFO] Successfully undid 1 migration to schema "PUBLIC" (execution time 00:00.024s).</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/maven/baseline">Maven: baseline <i class="fa fa-arrow-right"></i></a>
</p>