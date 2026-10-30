---
subtitle: Databricks
---

- **Verified DB Versions:** Databricks SQL 2026.39
- **Verified Client Version:** Flyway v13 & driver v3.4.2
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                    |
| ---------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| **URL format**                     | <code>jdbc:databricks://<i>host</i>:443/<i>schema</i>;transportMode=http;ssl=1;httpPath=<i>path</i>;EnableArrow=0;AuthMech=3;ConnCatalog=<i>catalog</i></code> |
| **SSL support**                    | Required (Databricks only accepts TLS connections; keep `ssl=1` in the URL)                                                |
| **Ships with Flyway Command-line** | Yes                                                                                                                        |
| **Maven Central coordinates**      | `com.databricks:databricks-jdbc`                                                                                           |
| **Supported versions**             | `3.4.2` (the version that ships with Flyway)  |
| **Default Java class**             | `com.databricks.client.jdbc.Driver`                                                                                        |

Copy the host and HTTP path from the **Connection details** tab of your SQL warehouse, for example:

<code>jdbc:databricks://<i>host</i>:443/default;transportMode=http;ssl=1;httpPath=/sql/1.0/warehouses/<i>warehouse-id</i>;EnableArrow=0;AuthMech=3;ConnCatalog=<i>catalog</i></code>

- `ConnCatalog` is required. Set it in the URL or [`jdbcProperties`](<Configuration/Environments Namespace/Environment JDBC Properties Namespace>).
- Set [`schemas`](<Configuration/Environments Namespace/Environment Schemas Setting>) and [`defaultSchema`](<Configuration/Flyway Namespace/Flyway Default Schema Setting>) to schema names within that catalog. If neither is set, Flyway uses the schema in the URL path (`default` above) and creates the schema history table there.
- Flyway sets `UserAgentEntry=Redgate_Flyway` unless you set `UserAgentEntry` in `jdbcProperties`.

## Java Usage

Databricks support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

#### Redgate

```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-databricks</artifactId>
</dependency>
```

#### Open Source

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-databricks</artifactId>
</dependency>
```

### Gradle

#### Redgate

```groovy
buildscript {
    dependencies {
        implementation "com.redgate.flyway:flyway-database-databricks"
    }
}
```

#### Open Source

```groovy
buildscript {
    dependencies {
        implementation "org.flywaydb:flyway-database-databricks"
    }
}
```

## SQL Script Syntax

- Databricks SQL syntax with statement delimiter **;**
- Identifiers are quoted with backticks, for example `` `my schema` ``
- `BEGIN ... END` compound statements, such as procedure bodies

### Compatibility

- Any SQL script executed by Flyway can be run in the Databricks SQL editor (after the placeholders have been replaced)

### Example

<pre class="prettyprint">/* Single line comment */
CREATE TABLE test_data (
  id BIGINT GENERATED ALWAYS AS IDENTITY,
  value STRING NOT NULL
);

/*
Multi-line
comment
*/

-- Sql-style comment

CREATE OR REPLACE PROCEDURE add_test_data(new_value STRING)
LANGUAGE SQL
SQL SECURITY INVOKER
AS
BEGIN
  INSERT INTO test_data (value) VALUES (new_value);
END;

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
</pre>

## Authentication

Flyway passes your URL and `jdbcProperties` to the Databricks JDBC driver, which handles authentication. Choose a method based on where Flyway runs:

| Method                         | URL parameters             | Suits                                  |
|--------------------------------|----------------------------|----------------------------------------|
| Personal access token          | `AuthMech=3`               | CI and local use                       |
| OAuth machine-to-machine (M2M) | `AuthMech=11;Auth_Flow=1`  | CI and other unattended use            |
| OAuth user-to-machine (U2M)    | `AuthMech=11;Auth_Flow=2`  | Interactive use only (opens a browser) |

With a personal access token, set `password` to the token.
With OAuth machine-to-machine, set the `OAuth2ClientId` and `OAuth2Secret` JDBC properties to the service principal's application ID and a Databricks OAuth secret.
With OAuth user-to-machine, the driver opens a browser to sign in and caches the tokens, encrypted with the passphrase in the `TokenCachePassPhrase` JDBC property.

For the full list of driver authentication options, refer to [Databricks' documentation](https://docs.databricks.com/aws/en/integrations/jdbc-oss/authentication).

For how to use authentication methods, see [Connecting to environments](https://documentation.red-gate.com/flyway/database-development-using-flyway/connecting-to-environments#Connectingtoenvironments-Authentication).
For credential storage and retrieval, see [Storing and retrieving credentials](https://documentation.red-gate.com/flyway/database-development-using-flyway/storing-and-retrieving-credentials#Storingandretrievingcredentials-Database-specificcredentialretrieval).

## Notes

For Databricks Lakebase, which Flyway connects to through its PostgreSQL support, see [Databricks Lakebase](<Database Driver Reference/Databricks Lakebase>).

## Limitations

- Due to a change in the Java 16+ runtime which Flyway depends upon, a formatting error will occur in the Databricks JDBC driver unless you define the handling of such, that can be done by either:
  - appending `EnableArrow=0` to your JDBC connection string
  - adding `--add-opens java.base/java.lang=ALL-UNNAMED`. This can be done via the [command line or environment variables](Usage/Command-Line)
- Databricks doesn't support transactional DDL, so Flyway runs migrations on Databricks without a transaction and each statement takes effect as soon as it runs. If a statement fails partway through a migration, the statements before it stay applied. Flyway records the migration as failed: you must undo the applied statements by hand, then run [repair](<Commands/Repair>) before migrating again. Keeping each migration to a single DDL statement limits the impact of a failure.
- Parallel migrations as described [here](<Usage/Frequently Asked Questions#parallel>) are unavailable in Databricks. Don't run more than one Flyway instance against the same schema history table at once.
- [Clean](<Commands/Clean>) doesn't remove procedures or volumes
