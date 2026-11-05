---
subtitle: Snowflake
---

- **Verified Versions:** 10.8
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                                 |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:snowflake://<i>account</i>.snowflakecomputing.com/?db=<i>database</i>&warehouse=<i>warehouse</i>&role=<i>role</i></code><br>(optionally <code>&schema=<i>schema</i></code> to specify current schema) |
| **Ships with Flyway Command-line** | Yes                                                                                                                                     |
| **Maven Central coordinates**      | `net.snowflake:snowflake-jdbc`                                                                                                          |
| **Supported versions**             | `4.0.2` and later                                                                                                                       |
| **Default Java class**             | `net.snowflake.client.api.driver.SnowflakeDriver`                                                                                       |

**Note:** The default schema is `PUBLIC` - if it does not exist or you want to use a different schema ensure you [set it in Flyway](<Configuration/Flyway Namespace/Flyway Default Schema Setting>)

**Note:** When Flyway creates the connection from a JDBC URL, it always sets `JDBC_QUERY_RESULT_FORMAT=JSON`, because the driver's default Arrow result format fails on Java 16 and later unless the JVM is started with `--add-opens=java.base/java.nio=ALL-UNNAMED`. Any other value you set is ignored. Flyway also sets `application=Redgate_Flyway` unless you set `application` yourself. If you give Flyway your own `DataSource`, for example with Spring Boot, set `JDBC_QUERY_RESULT_FORMAT=JSON` on it yourself.

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

## Authentication

Common authentication methods supported by Snowflake JDBC include:

| Method                                                | URL parameters                                                                  | Suits                             |
|-------------------------------------------------------|---------------------------------------------------------------------------------|-----------------------------------|
| Programmatic access token (PAT)                       | `&authenticator=programmatic_access_token&token=<access-token>`                 | CI and local use                  |
| Username and password with MFA                        | `&authenticator=username_password_mfa`                                          | Interactive use                   |
| [Key-based authentication](#key-based-authentication) | `&authenticator=snowflake_jwt&private_key_file=<absolute-location-of-pem-file>` | CI and local use                  |
| OAuth authorization code                              | `&authenticator=oauth_authorization_code`                                       | Interactive use (opens a browser) |
| Browser-based SSO                                     | `&authenticator=externalbrowser`                                                | Interactive use (opens a browser) |

For the full list of driver authentication options, refer to [Snowflake's documentation](https://docs.snowflake.com/en/developer-guide/jdbc/jdbc-parameters#authenticator).

For username and password with MFA, to reduce the number of MFA prompts, enable MFA caching by setting the `ALLOW_CLIENT_MFA_CACHING` parameter on your Snowflake account. See [Connecting to Snowflake with MFA](https://docs.snowflake.com/en/user-guide/security-mfa#connecting-to-snowflake-with-mfa).

For a programmatic access token (PAT), the `programmatic_access_token` authenticator only reads the token from the `token` parameter, not from the password. To avoid putting the token in the URL, you can set `token` with [`jdbcProperties`](<Configuration/Environments Namespace/Environment JDBC Properties Namespace>) instead. Alternatively, use the default authenticator (omit `authenticator`) and set Flyway's `password` to the access token.

For OAuth authorization code, Snowflake acts as the identity provider by default and no extra parameters are needed. To use an external identity provider, also set `oauthClientId`, `oauthClientSecret`, `oauthAuthorizationUrl` and `oauthTokenRequestUrl`. On Windows, the OAuth access token can't currently be cached because it exceeds the Windows Credential Manager size limit, so you'll be asked to log in on every run ([snowflake-jdbc#2762](https://github.com/snowflakedb/snowflake-jdbc/issues/2762)).

### Key-based authentication

Snowflake JDBC supports key-based authentication. To use this, you will need to:

- ensure you are using at least v3.11 of the Snowflake JDBC driver (Flyway ships with a more recent version)
- generate a public/private key pair
- assign the public key to the relevant Snowflake user account using <code>ALTER USER</code> - for complete
instructions on these steps, refer to [Snowflake's documentation](https://docs.snowflake.com/developer-guide/jdbc/jdbc-configure#using-key-pair-authentication-and-key-rotation)
- If you are using an encrypted key pair then you may need to set a JVM flag for the [Snowflake JDBC driver to be able to decrypt it](https://docs.snowflake.com/en/developer-guide/jdbc/jdbc-configure#key-decryption-errors):
  - For example, on Linux you would do it this way `export JAVA_OPTS='-Dnet.snowflake.jdbc.useBundledBouncyCastleForPrivateKeyDecryption=true'`

Finally, add the key-based authentication parameters from the [table above](#authentication) to your JDBC URL.

## Limitations

- Parallel migrations as described [here](Usage/Frequently Asked Questions#parallel) are unavailable in Snowflake. You can track the status of this feature in our GitHub issues [here](https://github.com/flyway/flyway/issues/3305).
- The JDBC URL must include the `db` and `warehouse` parameters. Without them, the connection succeeds but migrations fail.
- Snowflake commits DDL statements immediately, so a migration that fails part-way through leaves its earlier statements applied, and you need to clean them up by hand.

