---
layout: documentation
menu: errorcodes
subtitle: Error Codes
---

# Error Codes

When Flyway commands fail, they throw an exception with a message to help you identify the problem. They also contain an error code which users of the API or those who have enabled machine readable output can inspect and handle accordingly. Below are details of each error code under the command that causes it along with a suggested solution.


## General Error Codes
These error codes may appear from any command, and are indicative of more general errors.

### `FAULT`
- **Caused by:** An unexpected error within Flyway (e.g. a null pointer exception)
- **Solution:** Please contact support or create a [GitHub issue](https://github.com/flyway/flyway/issues)

### `ERROR`
- **Caused by:** An error due to invalid configuration or usage not caught by a more specific error code
- **Solution:** Ensure all configuration and usage is as per the [documentation](/documentation)

### `JDBC_DRIVER`
- **Caused by:** The JDBC driver is unable to be instantiated
- **Solution:** Check whether the JDBC driver is present on the [classpath](/documentation/addingToTheClasspath)

### `DB_CONNECTION`
- **Caused by:** Issues in SQL statements reported by the JDBC driver
- **Solution:** Check whether the SQL provided to Flyway is correct

### `CONFIGURATION`
- **Caused by:** Incorrect configuration provided to Flyway
- **Solution:** Ensure your configuration is as per the [documentation](/documentation/configuration/parameters/)

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
- **Solution:** Ensure that you do not tamper with the schema history and all migrations that have been deleted are removed from [locations](/documentation/configuration/parameters/locations) known to Flyway


## Validate Error Codes
These error codes are surfaced when running `validate` or `validateWithResult`.

### `VALIDATE_ERROR`
- **Caused by:** Some migrations have failed validation
- **Solution:** Inspect the list `invalidMigrations` on the validate result to see the required actions

### `SCHEMA_DOES_NOT_EXIST`
- **Caused by:** The schema being validated against does not exist
- **Solution:** Manually create the schema or enable [`createSchemas`](/documentation/configuration/parameters/createSchemas)

### `FAILED_REPEATABLE_MIGRATION`
- **Caused by:** A failed repeatable migration was detected
- **Solution:** Remove any incomplete changes then run `repair` to fix the schema history

### `FAILED_VERSIONED_MIGRATION`
- **Caused by:** A failed versioned migration was detected
- **Solution:** Remove any incomplete changes then run `repair` to fix the schema history

### `APPLIED_REPEATABLE_MIGRATION_NOT_RESOLVED`
- **Caused by:** A repeatable migration that was applied wasn't resolved in any supplied locations
- **Solution:** If you removed this migration intentionally run `repair` to mark the migration as deleted

### `APPLIED_VERSIONED_MIGRATION_NOT_RESOLVED`
- **Caused by:** A versioned migration that was applied wasn't resolved in any supplied locations
- **Solution:** If you removed this migration intentionally run `repair` to mark the migration as deleted

### `RESOLVED_REPEATABLE_MIGRATION_NOT_APPLIED`
- **Caused by:** A repeatable migration that was resolved has not been applied
- **Solution:** To ignore this migration set [`ignoreMigrationPatterns`](/documentation/configuration/parameters/ignoreMigrationPatterns) to `*:ignored`

### `RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED`
- **Caused by:** A versioned migration that was resolved has not been applied
- **Solution:** To ignore this migration set [`ignoreMigrationPatterns`](/documentation/configuration/parameters/ignoreMigrationPatterns) to `*:ignored` and to allow executing this migration enable [`outOfOrder`](/documentation/configuration/parameters/outOfOrder)

### `OUTDATED_REPEATABLE_MIGRATION`
- **Caused by:** An applied repeatable migration was resolved with a newer checksum and can be reapplied
- **Solution:** Run `migrate` to execute this migration

### `TYPE_MISMATCH`
- **Caused by:** The type of the resolved migration (`BASELINE`, `SQL`, `UNDO_SQL`, ...) is different from the applied migration's
- **Solution:** Either revert the changes to the migration or run `repair` to update the schema history

### `CHECKSUM_MISMATCH`
- **Caused by:** The checksum of the resolved migration is different from the applied migration's
- **Solution:** Either revert the changes to the migration or run `repair` to update the schema history

### `DESCRIPTION_MISMATCH`
- **Caused by:** The description of the resolved migration is different from the applied migration's
- **Solution:** Either revert the changes to the migration or run `repair` to update the schema history


<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/usage/plugins">Community Plugins<i class="fa fa-arrow-right"></i></a>
</p>
