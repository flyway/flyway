---
subtitle: General error codes
---

These error codes may appear from any command, and are indicative of more general errors.

### `FAULT`

- **Caused by:** An unexpected error within Flyway (e.g. a null pointer exception)
- **Solution:** Please contact support or create a [GitHub issue](https://github.com/flyway/flyway/issues)

### `ERROR`

- **Caused by:** An error due to invalid configuration or usage not caught by a more specific error code
- **Solution:** Ensure all configuration and usage is as per the [documentation] (Configuration)

### `JDBC_DRIVER`

- **Caused by:** The JDBC driver is unable to be instantiated
- **Solution:** Check whether the JDBC driver is present on the [classpath](<Usage/Adding to the classpath>)

### `DB_CONNECTION`

- **Caused by:** Issues in SQL statements reported by the JDBC driver
- **Solution:** Check whether the SQL provided to Flyway is correct

### `CONFIGURATION`

- **Caused by:** Incorrect configuration provided to Flyway
- **Solution:** Ensure your configuration is as per the [documentation](Configuration/)

### `DUPLICATE_VERSIONED_MIGRATION`

- **Caused by:** Multiple versioned migrations having the same version
- **Solution:** Ensure that all versioned migrations have a unique version

### `DUPLICATE_REPEATABLE_MIGRATION`

- **Caused by:** Multiple repeatable migrations having the same description
- **Solution:** Ensure that all repeatable migrations have a unique description

### `DUPLICATE_UNDO_MIGRATION`

- **Caused by:** Multiple undo migrations that undo the same versioned migration
- **Solution:** Ensure that there is at most one undo migration per versioned migration

### `DUPLICATE_DELETED_MIGRATION`

- **Caused by:** Schema history or filesystem corruption causing the same migration to appear to be deleted more than once
- **Solution:** Ensure that you do not tamper with the schema history and all migrations that have been deleted are removed from [locations](<Configuration/Flyway Namespace/Flyway Locations Setting>) known to Flyway

### `NON_EMPTY_SCHEMA_WITHOUT_SCHEMA_HISTORY_TABLE`

- **Caused by:** Having non-empty schemas but no schema history table (e.g. introducing Flyway to an existing database)
- **Solution:** Run `baseline` or set [baselineOnMigrate](<Configuration/Flyway Namespace/Flyway Baseline On Migrate Setting>) to true to initialize the schema history table
