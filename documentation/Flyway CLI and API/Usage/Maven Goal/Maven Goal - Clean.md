---
pill: mvn_clean
subtitle: 'mvn flyway:clean'
---
# Maven Goal: Clean

Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas. If Flyway automatically created them, then the schemas themselves will be dropped when cleaning.<br/>
The schemas are cleaned in the order specified by the <code>schemas</code> property.

<a href="Commands/clean"><img src="assets/command-clean.png" alt="clean"></a>

## Default Phase

- pre-integration-test

## Usage

<pre class="console"><span>&gt;</span> mvn flyway:clean</pre>

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
    <callbacks>
        <callback>com.mycompany.project.CustomCallback</callback>
        <callback>com.mycompany.project.AnotherCallback</callback>
    </callbacks>
    <skipDefaultCallbacks>false</skipDefaultCallbacks>
    <cleanDisabled>false</cleanDisabled>
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

<pre class="console">&gt; mvn flyway:clean

[INFO] [flyway:clean {execution: default-cli}]
[INFO] Cleaned database schema 'PUBLIC' (execution time 00:00.016s)</pre>
