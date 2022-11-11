---
layout: maven
pill: mvn_info
subtitle: 'mvn flyway:info'
---
# Maven Goal: Info

Prints the details and status information about all the migrations.

<a href="/documentation/command/info"><img src="/assets/balsamiq/command-info.png" alt="info"></a>

## Usage

<pre class="console"><span>&gt;</span> mvn flyway:info</pre>

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

<pre class="console">&gt; mvn flyway:info

[INFO] [flyway:info {execution: default-cli}]
[INFO] +-------------+------------------------+---------------------+---------+
[INFO] | Version     | Description            | Installed on        | State   |
[INFO] +-------------+------------------------+---------------------+---------+
[INFO] | 1           | Initial structure      | 2012-11-13 15:37:41 | Success |
[INFO] | 1.1         | Populate table         | 2012-11-13 15:37:41 | Success |
[INFO] | 1.2         | Another user           |                     | Pending |
[INFO] | 1.3         | And his brother        |                     | Pending |
[INFO] +-------------+------------------------+---------------------+---------+</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/maven/validate">Maven: validate <i class="fa fa-arrow-right"></i></a>
</p>