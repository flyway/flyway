---
subtitle: Databricks Lakebase
---

- **Verified Versions:** 17 (PostgreSQL major version)
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                       |
|------------------------------------|-----------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:postgresql://<i>host</i>/<i>database</i>?sslmode=require</code>                    |
| **SSL support**                    | Required - add `?sslmode=require`                                                             |
| **Ships with Flyway Command-line** | Yes                                                                                           |
| **Maven Central coordinates**      | `org.postgresql:postgresql`                                                                   |
| **Supported versions**             | `9.3-1104-jdbc4` and later                                                                    |
| **Default Java class**             | `org.postgresql.Driver`                                                                       |

Connectivity is through Flyway's existing PostgreSQL support, so you can refer to the [PostgreSQL page](<Database Driver Reference/PostgreSQL Database>) for further details on how to connect and communicate with the database. Copy the host from the connection details of your Lakebase compute endpoint, choosing the Java (JDBC) connection string.

When creating a project with [`flyway init`](<Commands/Init>), set the database type to `DatabricksLakebase`:

```bash
./flyway init -databaseType="DatabricksLakebase"
```

## Java Usage

Lakebase support uses the PostgreSQL plugin, which is a separate dependency for Flyway. See [PostgreSQL Java Usage](<Database Driver Reference/PostgreSQL Database>).

## Authentication

Lakebase uses PostgreSQL username/password authentication. The password is either a Databricks OAuth token for the Postgres role of a Databricks identity, or a native Postgres role password.
For interactive use, the [Lakebase resolver](<Configuration/Environments Namespace/Environment Resolvers Namespace/Lakebase Resolver>) signs you in to Databricks in a browser and supplies the user and OAuth token.

For how to use authentication methods, see [Connecting to environments](https://documentation.red-gate.com/flyway/database-development-using-flyway/connecting-to-environments#Connectingtoenvironments-Authentication).
For credential storage and retrieval, see [Storing and retrieving credentials](https://documentation.red-gate.com/flyway/database-development-using-flyway/storing-and-retrieving-credentials#Storingandretrievingcredentials-Database-specificcredentialretrieval).

## Notes

Lakebase is Databricks' managed PostgreSQL service, and Flyway usage is the same as for PostgreSQL. For more details, please refer to the [PostgreSQL](<Database Driver Reference/PostgreSQL Database>) page. For Databricks Lakehouse (Unity Catalog), see the [Databricks](<Database Driver Reference/Databricks>) page.
