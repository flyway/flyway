---
layout: maven
pill: mvn_clean
subtitle: 'mvn flyway:clean'
---
# Maven Goal: Clean

Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.

The schemas are cleaned in the order specified by the [`schemas`](/documentation/configuration/parameters/schemas) and [`defaultSchema`](/documentation/configuration/parameters/defaultSchema) property.

<a href="/documentation/command/clean"><img src="/assets/balsamiq/command-clean.png" alt="clean"></a>

## Default Phase

- pre-integration-test

## Usage

<pre class="console"><span>&gt;</span> mvn flyway:clean</pre>

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

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/maven/info">Maven: info <i class="fa fa-arrow-right"></i></a>
</p>