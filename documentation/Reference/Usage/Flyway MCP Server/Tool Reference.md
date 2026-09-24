---
subtitle: Tool Reference
---

## Concepts

- **Workspace**: Calling `load_project` loads a Flyway project into a workspace, identified by a `workspaceId`. Only one
  workspace is loaded at a time; loading a new project discards artifacts and results from the previous one. If you've
  only changed the project's toml configuration, call `reload_project` instead, which preserves existing artifacts.
- **Diffs and artifacts**: Each `create_diff_*` tool computes the difference between two states of your project (for
  example, the development database and the schema model) and returns a `diffId`. Other tools then consume that
  `diffId`, together with a list of change IDs, to apply or generate something from the selected differences.
- **Selecting changes**: Tools that accept a `changes` list of IDs also accept a single `*` to select every change from
  a diff. You can't mix `*` with specific IDs. If a selected change depends on another change that wasn't selected, the
  dependency is included automatically.
- **Three-sided diff**: Instead of comparing two states, `create_diff_development` can compare three—the current
  schema model, the development database, and a schema model checkpoint captured earlier via
  `create_schema_model_checkpoint`. Each change is tagged with how the schema model has moved since the checkpoint 
  (`new`, `existing`, `missing`, or `conflict`), which lets you separate changes you introduced locally from ones brought 
  in from a shared source. See `create_diff_development` for details.

## Tools

| Tool                          | Toolsets                              | Purpose                                                                                                  |
|-------------------------------|---------------------------------------|----------------------------------------------------------------------------------------------------------|
| `load_project`                | `develop_migrations`, `develop_state` | Loads a Flyway project into a new workspace.                                                             |
| `reload_project`              | `develop_migrations`, `develop_state` | Reloads an already-loaded project from disk, preserving existing artifacts.                              |
| `create_schema_model_checkpoint` | `develop_migrations`               | Captures a checkpoint of the schema model for use as a baseline in a three-sided development diff.       |
| `create_diff_schema_model`    | `develop_migrations`, `develop_state` | Diffs the development database against the project's schema model.                                       |
| `update_schema_model`         | `develop_migrations`, `develop_state` | Applies selected changes from a schema model diff to the schema model files.                             |
| `create_diff_migrations`      | `develop_migrations`                  | Diffs the schema model against the current migration scripts (via the shadow environment).               |
| `generate_migrations`         | `develop_migrations`                  | Generates migration scripts from a migrations diff.                                                      |
| `review_code`                 | `develop_migrations`                  | Runs code review against a single migration script and reports the rule violations found.                |
| `test_migrate`                | `develop_migrations`                  | Runs pending migration scripts against the shadow database to verify they execute without errors.        |
| `create_diff_development`     | `develop_migrations`, `develop_state` | Diffs the schema model against the development database, in the direction needed to update the database. |
| `update_development`          | `develop_migrations`, `develop_state` | Applies selected changes from a development diff to the development database.                            |
| `get_diff_details`            | `develop_migrations`, `develop_state` | Returns the unified diff text for a single changed object within any diff.                               |

### load_project

Loads a Flyway project into a workspace. Required before any other tool can be used.

| Parameter       | Required | Description                                                            |
|-----------------|----------|------------------------------------------------------------------------|
| `projectFolder` | Yes      | Absolute path to the directory containing the project's `flyway.toml`. |

Returns a `workspaceId`, used by all subsequent tool calls.

### reload_project

Reloads a project from disk for an existing workspace, without discarding previously computed artifacts. Prefer this
over `load_project` when you've only edited the project's toml files.

| Parameter     | Required | Description                              |
|---------------|----------|------------------------------------------|
| `workspaceId` | Yes      | Workspace ID returned by `load_project`. |

### create_schema_model_checkpoint

Captures a checkpoint of the current schema model. The checkpoint can be used as input to `create_diff_development`
to categorize changes by origin (schema model vs. checkpoint) as a three-sided diff.

| Parameter     | Required | Description                       |
|---------------|----------|-----------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`. |

Returns a `checkpointId`, for use as an input to `create_diff_development`.

### create_diff_schema_model

Diffs the development database against the project's schema model. Feeds into `update_schema_model`. Only one such diff
is stored per workspace at a time.

| Parameter     | Required | Description                       |
|---------------|----------|-----------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`. |

Returns a `diffId` and a list of selectable differences.

### update_schema_model

Updates the schema model files using selected changes from a schema model diff.

| Parameter     | Required | Description                                          |
|---------------|----------|------------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.                    |
| `diffId`      | Yes      | Diff ID from `create_diff_schema_model`.             |
| `changes`     | Yes      | List of change IDs to apply, or `*` for all changes. |

Returns the list of schema model files that were updated, relative to the project's schema model folder.

### create_diff_migrations

Diffs the schema model against the current state of the migration scripts, built via the shadow environment. Feeds into
`generate_migrations`. Only one such diff is stored per workspace at a time.

| Parameter     | Required | Description                       |
|---------------|----------|-----------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`. |

Returns a `diffId` and a list of selectable differences.

### generate_migrations

Generates migration scripts from a migrations diff. Scripts are named automatically using the project's configured
naming convention.

| Parameter          | Required | Description                                                   |
|--------------------|----------|---------------------------------------------------------------|
| `workspaceId`      | Yes      | Workspace ID from `load_project`.                             |
| `migrationsDiffId` | Yes      | Diff ID from `create_diff_migrations`.                        |
| `changes`          | Yes      | List of change IDs to include, or `*` for all changes.        |
| `description`      | No       | Description used in the generated migration script filenames. |

Returns the migration folder path, the list of generated files, and any warnings raised during generation.

### review_code

Runs code review against a single migration script and reports the rule violations found.

| Parameter     | Required | Description                                                            |
|---------------|----------|------------------------------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.                                      |
| `file`        | Yes      | Migration script to review. Absolute, or relative to the project root. |

Returns the absolute path of the reviewed file, the list of issues (rule violations) found, the definitions of the
violated rules, and `total_rules_checked` (the number of rules evaluated).

### test_migrate

Runs pending migration scripts against the shadow database to verify they execute without errors. The shadow database may be automatically reprovisioned first if the migration history has diverged. Each migration that ran during this call is reported as `success`; if one fails, execution stops and that migration is reported as `failed`, with any remaining scripts reported as `pending`.

| Parameter     | Required | Description                                                                                                                                                                                                                                    |
|---------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.                                                                                                                                                                                                              |
| `rebuild`     | No       | When `true`, forces a full reprovision of the shadow database before running migrations. Use this if you have manually altered the shadow database, or want to test every migration from the baseline rather than just new or changed scripts. |

Returns `results` (per-file migration results in execution order, each with a `file` name and a `status` of `success`,
`failed`, or `pending`), `error` (the error message from the failed migration, or `null` when all migrations succeeded),
and `reprovisioned` (whether the shadow database was fully reprovisioned during this call; always `false` if a migration failed, even when a reprovision happened first).

### create_diff_development

Diffs the schema model against the development database, describing the changes needed to bring the development database
in line with the schema model. Feeds into `update_development`. Only one such diff is stored per workspace at a time.

If a `checkpointId` is supplied (from `create_schema_model_checkpoint`), a three-sided diff is performed instead: the
schema model as it was at the checkpoint is compared against both the current schema model and the development
database, classifying each change by how the schema model has moved since the checkpoint (new, existing, missing, or
conflict). This distinguishes changes that predate the checkpoint from ones introduced since—for example, changes
brought in from a shared source versus your own local edits.

| Parameter      | Required | Description                                                             |
|----------------|----------|--------------------------------------------------------------------------|
| `workspaceId`  | Yes      | Workspace ID from `load_project`.                                       |
| `checkpointId` | No       | Checkpoint ID from `create_schema_model_checkpoint`, to enable a three-sided diff. |

Returns a `diffId` and a list of selectable differences. When `checkpointId` is supplied, also returns
`threeSidedDifferences`—the same differences, each tagged with how the schema model has moved since the checkpoint
(new, existing, missing, or conflict).

### update_development

Applies selected changes from a development diff to the development database.

| Parameter     | Required | Description                                          |
|---------------|----------|------------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.                    |
| `diffId`      | Yes      | Diff ID from `create_diff_development`.              |
| `changes`     | Yes      | List of change IDs to apply, or `*` for all changes. |

Returns `changesDeployed` (a boolean indicating whether a deployment action was performed against development—this
may be `false` if no changes were selected) and the list of objects updated in the development database.

### get_diff_details

Returns the unified diff text for a single changed object within a diff previously created by any `create_diff_*` tool,
in the same workspace.

| Parameter     | Required | Description                                    |
|---------------|----------|------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.              |
| `diffId`      | Yes      | Diff ID from any `create_diff_*` tool.         |
| `changeId`    | Yes      | ID of the changed object to fetch details for. |

Returns the unified diff text for the requested change.
