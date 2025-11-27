---
subtitle: Snowflake
---

- **Verified Versions:** 3.50, 9.19
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                                 |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:snowflake://<i>account</i>.snowflakecomputing.com/?db=<i>database</i>&warehouse=<i>warehouse</i>&role=<i>role</i><i>&JDBC_QUERY_RESULT_FORMAT=JSON</i></code><br>(optionally <code>&schema=<i>schema</i></code> to specify current schema) |
| **Ships with Flyway Command-line** | Yes                                                                                                                                     |
| **Maven Central coordinates**      | `net.snowflake:snowflake-jdbc`                                                                                                          |
| **Supported versions**             | `3.6.23` and later                                                                                                                      |
| **Default Java class**             | `net.snowflake.client.jdbc.SnowflakeDriver`                                                                                             |


## Java Usage
Snowflake support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
Snowflake is found within the `flyway-database-snowflake` plugin module.
### Maven
#### Redgate
```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-snowflake</artifactId>
</dependency>
```
#### Open Source
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-snowflake</artifactId>
</dependency>
```

### Gradle
#### Redgate
```groovy
buildscript {
    dependencies {
        implementation "com.redgate.flyway:flyway-database-snowflake"
    }
}
```
#### Open Source
```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-snowflake"
    }
}
```


## SQL Script Syntax

- Standard SQL syntax with statement delimiter **;**

### Compatibility

- DDL exported by the Snowflake web GUI can be used unchanged in a Flyway migration
- Any SQL script executed by Flyway, can be executed by the Snowflake web GUI (after the placeholders have been replaced)
- The Snowflake driver requires Java 8+. There is no support from Snowflake for Java 7 users.

### Example

<pre class="prettyprint">/* Single line comment */
CREATE TABLE test_data (
  value VARCHAR(25) NOT NULL PRIMARY KEY
);

/*
Multi-line
comment
*/

-- Sql-style comment

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
</pre>

## Key-based Authentication

Snowflake JDBC supports key-based authentication. To use this, you will need to:

- ensure you are using at least v3.11 of the Snowflake JDBC driver (Flyway ships with a more recent version)
- generate a public/private key pair
- assign the public key to the relevant Snowflake user account using <code>ALTER USER</code> - for complete
instructions on these steps, refer to [Snowflake's documentation](https://docs.snowflake.com/developer-guide/jdbc/jdbc-configure#using-key-pair-authentication-and-key-rotation)
- If you are using an encrypted key pair then you may need to set a JVM flag for the [Snowflake JDBC driver to be able to decrypt it](https://docs.snowflake.com/en/developer-guide/jdbc/jdbc-configure#key-decryption-errors):
  - For example  in linux you would do it this way `export JAVA_OPTS='-Dnet.snowflake.jdbc.enableBouncyCastle=true'`


Finally, amend your JDBC connection string with the extra parameters to enable key-based auth and to refer to the
location of the private key:
<code>authenticator=snowflake_jwt&private_key_file=&lt;absolute-location-of-pem-file&gt;</code>.


## Limitations

- Parallel migrations as described [here](Usage/Frequently Asked Questions#parallel) are unavailable in Snowflake. You can track the status of this feature in our GitHub issues [here](https://github.com/flyway/flyway/issues/3305).
- Due to a change in the Java 16+ runtime which Flyway depends upon, a formatting error will occur in the Snowflake JDBC driver unless you define the handling of such, that can be done by either:
  - appending `&JDBC_QUERY_RESULT_FORMAT=JSON` to your JDBC connection string
  - adding `--add-opens java.base/java.lang=ALL-UNNAMED`. This can be done via the [command line or environment variables](Usage/Command-Line)