---
subtitle: Cassandra
---
# Cassandra Database

- **Verified Versions:** N/A
- **Maintainer:** Redgate


## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                                      | Details                                                                                     |
|-------------------------------------------|---------------------------------------------------------------------------------------------|
| **URL format**                            | <code>jdbc:cassandra://<i>host</i>:<i>port</i>?localdatacenter=<i>datacenter1</i></code>     |
| **SSL support**                           | Yes, add `;enablessl=true` to URL                                                           |
| **Ships with Flyway Command-line**        | Yes                                                                                         |
| **Maven Central coordinates**             | `com.ing.data:cassandra-jdbc-wrapper`                                                       |
| **Supported versions**                    | `4.11.1` and later                                                                          |
| **Default Java class**                    | `com.ing.data.cassandra.jdbc.CassandraDriver`                                               |

## Good to know
### CQL file extensions
Cassandra migrations typically have a `.cql` migration suffix, we recommend configuring flyway to pick these up using the [`sqlMigrationSuffixes`](/configuration/parameters/flyway/sql-migration-suffixes) parameter.

You would specify this in your TOML configuration like this:
```
[flyway]
sqlMigrationSuffixes = [".cql"]
```
### Default schema/keyspace

Flyway maps it's concept of schema onto a keyspace in Cassandra. You should specify a default schema to use as otherwise Flyway will default to `system` and you will have troubles working there (for example, clean won't be able to work correctly in the `system` keyspace).
This can be configured using one of the following parameters:
- [`defaultSchema`](/configuration/parameters/flyway/default-schema)
- [`schemas`](/configuration/parameters/environments/schemas)

## Limitations

- You can't currently do a [Dry-run](<Concepts/Dry Runs>) on operations with Cassandra.

### Additional information
Our JDBC implementation for Cassandra comes from this Github project:
[Cassandra-jdbc-wrapper](https://github.com/ing-bank/cassandra-jdbc-wrapper)