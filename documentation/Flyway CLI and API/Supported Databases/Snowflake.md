---
subtitle: Snowflake
---
# Snowflake

## Supported Versions

- `7.x` versions up to 7.1
- `6.x` versions up to 6.29
- `5.x` versions up to 5.1
- `4.x` versions up to 4.2
- `3.50` and later `3.x` versions

## Support Level

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>&#10003;</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>&#10003;</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>&#10003; {% include teams.html %}</td>
    </tr>
</table>

Support Level determines the degree of support available for this database ([learn more](Learn More/Database Support Levels)).

## Drivers

<table class="table">
<tr>
<th>URL format</th>
<td><code>jdbc:snowflake://<i>account</i>.snowflakecomputing.com/?db=<i>database</i>&warehouse=<i>warehouse</i>&role=<i>role</i></code>
(optionally <code>&schema=<i>schema</i></code> to specify current schema)</td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>Yes</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td><code>net.snowflake:snowflake-jdbc</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>3.6.23</code> and later</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>net.snowflake.client.jdbc.SnowflakeDriver</code></td>
</tr>
</table>

## SQL Script Syntax

- [Standard SQL syntax](Concepts/migrations#syntax) with statement delimiter **;**

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

- ensure you are using at least v3.11 of the Snowflake JDBC driver (Flyway currently ships with this version)
- generate a public/private key pair
- assign the public key to the relevant Snowflake user account using <code>ALTER USER</code> - for complete
instructions on these steps, refer to [Snowflake's documentation](https://docs.snowflake.net/manuals/user-guide/jdbc-configure.html#using-key-pair-authentication)

Finally, amend your JDBC connection string with the extra parameters to enable key-based auth and to refer to the
location of the private key:
<code>authenticator=snowflake_jwt&private_key_file=&lt;absolute-location-of-pem-file&gt;</code>.


## Limitations

- Parallel migrations as described [here](Learn More/Frequently Asked Questions#parallel) are unavailable in Snowflake. You can track the status of this feature in our GitHub issues [here](https://github.com/flyway/flyway/issues/3305).
