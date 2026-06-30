---
title: "Flyway Native Connectors - Cassandra"
date: 2026-06-02
tags: [flyway, cassandra, native-connectors, migrations]
---

Flyway now supports Apache Cassandra through a Native Connector, talking to your cluster directly with the Apache Cassandra Java driver rather than through a JDBC wrapper. If you already run Flyway against Cassandra over JDBC, this post explains what has changed and how to move across.

## What are we doing

Flyway was originally built around JDBC. That works well for relational databases, but Cassandra doesn't speak JDBC — it speaks CQL over its own native protocol — so support until now has relied on a third-party JDBC wrapper (`com.ing.data:cassandra-jdbc-wrapper`) to make a non-relational database look relational to the JDBC framework.

Native Connectors remove that translation layer. Instead of forcing every database through the universal-but-relational JDBC layer, each Native Connector uses that database's own driver or API directly — a real `CqlSession` and the Apache driver for Cassandra, just as the MongoDB and Oracle connectors use their native tooling. Flyway speaks CQL to your cluster the same way your application does, and the core commands you rely on — `migrate`, `clean`, `info`, `validate`, `baseline` and `repair` — are implemented natively on top of that driver, along with the schema history table and your versioned and repeatable migrations.

Like the JDBC implementation this is replacing, it is [Foundational support](https://documentation.red-gate.com/fd/supported-databases-and-versions-143754067.html) for the database.

The JDBC connector is now deprecated and will be removed in a future release, so we recommend moving to the Native Connector.

## Availability

Cassandra Native Connectors support arrived in Flyway 12.5.0 and the driver ships with the Flyway command line, so there is nothing extra to download. 
This was originally disabled by by default — but from Flyway 12.9.1 it is enabled by default. You can disable it with an environment variable:

```bash
export FLYWAY_NATIVE_CONNECTORS=false
```

## How to use this

Switching from the JDBC connector is mostly a configuration change.

**Update your connection URL.** The Native Connector uses a new URL format that includes the keyspace in the path:

```toml
# Before — JDBC connector (deprecated)
url = "jdbc:cassandra://localhost:9042?localdatacenter=datacenter1"

# After — Native Connector
url = "cassandra://localhost:9042/flyway_keyspace?localdatacenter=datacenter1"
```

The keyspace in the URL path becomes your default schema — Flyway maps its notion of a "schema" onto a Cassandra keyspace. The legacy `jdbc:cassandra://` prefix is still accepted, but it logs a deprecation warning.

**Configure Flyway to find your `.cql` migrations.** Flyway only scans for `.sql` files by default, so you must tell it to pick up `.cql` scripts, otherwise it will find nothing to migrate:

```toml
[flyway]
sqlMigrationSuffixes = [".cql"]
```

With those two pieces in place, a typical environment looks like this:

```toml
[environments.cassandra]
url = "cassandra://localhost:9042/flyway_keyspace?localdatacenter=datacenter1"

[flyway]
environment = "cassandra"
sqlMigrationSuffixes = [".cql"]
```

Given a single versioned migration, `V1__create_users.cql`:

```sql
CREATE TABLE users (
    id uuid PRIMARY KEY,
    name text,
    created_at timestamp
);
```

with `FLYWAY_NATIVE_CONNECTORS=true` set, running `flyway migrate` produces:

```
Flyway OSS Edition 12.5.0 by Redgate

Database: cassandra://localhost:9042/flyway_keyspace (Cassandra)
Schema history table "flyway_keyspace"."flyway_schema_history" does not exist yet
Successfully validated 1 migration (execution time 00:00.621s)
Creating Schema History table "flyway_keyspace"."flyway_schema_history" ...
Current version of schema "flyway_keyspace": << Empty Schema >>
Migrating schema "flyway_keyspace" to version "1 - create users" [non-transactional]
Successfully applied 1 migration to schema "flyway_keyspace", now at version v1 (execution time 00:01.044s)
Schema version: 1

+-----------+---------+------------------------------+--------+---------------------+---------+----------+
| Category  | Version | Description                  | Type   | Installed On        | State   | Undoable |
+-----------+---------+------------------------------+--------+---------------------+---------+----------+
|           |         | << Flyway Schema Creation >> | SCHEMA | 2026-06-02 10:33:15 | Success |          |
| Versioned | 1       | create users                 | SQL    | 2026-06-02 10:33:16 | Success | No       |
+-----------+---------+------------------------------+--------+---------------------+---------+----------+
```

The migration files and commands are the same as before; only the connection now runs over Cassandra's native protocol.

## Constraints

- **Keep the keyspace in the URL.** `defaultSchema` or `schemas` will override which keyspace Flyway treats as its default, but only the keyspace in the URL binds the underlying session. Without it, unqualified statements in your migrations (for example `CREATE TABLE users (...)`) have no keyspace to resolve against.
- **No transactions.** Cassandra has no multi-statement transactions, so migrations run non-transactionally — the same as under the JDBC connector.
- **No dry-run.** Dry-run is not currently supported for Cassandra.

## Feedback

For the full configuration reference, see the [Cassandra database documentation](https://documentation.red-gate.com/fd/cassandra-database-277579306.html), which links through to the Native Connectors page. As this is a preview feature, we'd welcome your feedback — please [open an issue on the Flyway GitHub repository](https://github.com/flyway/flyway/issues) to tell us how you get on or what you'd like to see next.
