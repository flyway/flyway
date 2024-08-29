---
pill: mvn_baseline
subtitle: 'mvn flyway:baseline'
---
# Maven Goal: Baseline

Baselines an existing database, excluding all migrations up to and including baselineVersion.

<a href="Commands/baseline"><img src="assets/command-baseline.png" alt="baseline"></a>

## Usage

<pre class="console"><span>&gt;</span> mvn flyway:baseline</pre>

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
    <createSchemas>true</createSchemas>
    <table>schema_history</table>
    <tablespace>my_tablespace</tablespace>
    <callbacks>
        <callback>com.mycompany.project.CustomCallback</callback>
        <callback>com.mycompany.project.AnotherCallback</callback>
    </callbacks>
    <skipDefaultCallbacks>false</skipDefaultCallbacks>
    <baselineVersion>1.0</baselineVersion>
    <baselineDescription>Base Migration</baselineDescription>
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

<pre class="console">&gt; mvn flyway:baseline

[INFO] [flyway:baseline {execution: default-cli}]
[INFO] Creating schema history table: "PUBLIC"."flyway_schema_history"
[INFO] Schema baselined with version: 1</pre>
