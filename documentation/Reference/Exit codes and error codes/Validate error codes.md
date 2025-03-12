---
subtitle: Validate error codes
---

These error codes are surfaced when running `validate` or `validateWithResult`.

### `VALIDATE_ERROR`

- **Caused by:** Some migrations have failed validation
- **Solution:** Inspect the list `invalidMigrations` on the validate result to see the required actions

### `SCHEMA_DOES_NOT_EXIST`

- **Caused by:** The schema being validated against does not exist
- **Solution:** Manually create the schema or enable [`createSchemas`](<Configuration/Flyway Namespace/Flyway Create Schemas Setting>)

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
- **Solution:** To ignore this migration set [`ignoreMigrationPatterns`](<Configuration/Flyway Namespace/Flyway Ignore Migration Patterns Setting>) to `*:ignored`

### `RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED`

- **Caused by:** A versioned migration that was resolved has not been applied
- **Solution:** To ignore this migration set [`ignoreMigrationPatterns`](<Configuration/Flyway Namespace/Flyway Ignore Migration Patterns Setting>) to `*:ignored` and to allow executing this migration enable [`outOfOrder`](<Configuration/Flyway Namespace/Flyway Out Of Order Setting>)

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
