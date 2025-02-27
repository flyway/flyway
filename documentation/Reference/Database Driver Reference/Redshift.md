---
subtitle: Redshift
---

- **Verified Versions:** N/A
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                                               |
|------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:redshift://<i>host</i>:<i>port</i>/<i>database</i></code>                                                                                  |
| **Ships with Flyway Command-line** | No                                                                                                                                                    |
| **Download**                       | Follow the instructions at [docs.aws.amazon.com](http://docs.aws.amazon.com/redshift/latest/mgmt/configure-jdbc-connection.html#download-jdbc-driver) |
| **Supported versions**             | `1.2.10.1009` and later.<br>`2.0.0.5` and later are **partially** supported (see below)                                                               |
| **Default Java class**             | `com.amazon.redshift.jdbc42.Driver`                                                                                                                   |


## Java Usage
Redshift support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
Redshift is found within the `flyway-database-redshift` plugin module.
### Maven
#### Redgate
```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-redshift</artifactId>
</dependency>
```
#### Open Source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-redshift</artifactId>
</dependency>
```

### Gradle
#### Redgate
```groovy
dependencies {
    implementation "com.redgate.flyway:flyway-database-redshift"
}
```
#### Open Source
```groovy
dependencies {
    implementation "org.flywaydb:flyway-database-redshift"
}
```

## SQL Script Syntax

- Standard SQL syntax with statement delimiter **;**
- Stored procedures (CREATE FUNCTION with $$ escapes)

### Compatibility

- DDL exported by pg_dump can be used unchanged in a Flyway migration. Please note that Redshift does not support exporting data using
        pg_dump, so you must export only the schema, using <code>pg_dump -s</code>.
- Any Redshift SQL script executed by Flyway,
        can be executed by the PostgreSQL command-line tool and other PostgreSQL-compatible tools,
        such as SQL Workbench/J (after the placeholders have been replaced).

### Example

<pre class="prettyprint">/* Single line comment */
CREATE TABLE test_data (
  test_id INT IDENTITY NOT NULL PRIMARY KEY,
  value VARCHAR(25) NOT NULL
);

/*
Multi-line
comment
*/
INSERT INTO test_data (value) VALUES ('Hello');

CREATE VIEW value_only AS SELECT value FROM test_data;

CREATE TABLE another_table AS SELECT 'some-data' as name;

CREATE FUNCTION add(integer, integer) RETURNS integer
     IMMUTABLE
    AS $$
    select $1 + $2;
$$ LANGUAGE sql;

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');</pre>

## Limitations

Due to Redshift limitations `ALTER TABLE` and `DROP TABLE` for **external tables** can't run within a transaction, yet Flyway doesn't
autodetect this. You can work around this limitation and successfully execute such a statement by including a `VACUUM`
statement in the same SQL file as this will force Flyway to run the entire migration without a transaction.

The v2 driver is only supported from v2.0.0.5, and only then by Flyway setting the connection property `enableFetchRingBuffer=false`. 
We recommend using the latest v1 driver for the time being. See [here](https://github.com/aws/amazon-redshift-jdbc-driver/issues/4) for more details.
