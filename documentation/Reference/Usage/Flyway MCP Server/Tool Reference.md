---
subtitle: Tool Reference
---

This feature is in **preview**. Tool names, parameters, and behavior may change significantly, or tools may be removed, in
future releases without notice. This reference describes the tools in Flyway {{site.flywayVersion}}.

## Concepts

- **Workspace**: Calling `load_project` loads a Flyway project into a workspace, identified by a `workspaceId`. Only one
  workspace is loaded at a time; loading a new project discards artifacts and results from the previous one. If you've
  only changed the project's TOML configuration, call `reload_project` instead, which preserves existing artifacts.
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

- **Diff names**: A `create_diff_*` tool is named for what its changes are applied to, not for the direction of the
  comparison. `create_diff_schema_model` produces changes to apply to the schema model (by comparing the development
  database against it), `create_diff_migrations` produces changes to generate migration scripts from, and
  `create_diff_development` produces changes to apply to the development database.
- **Selectable differences**: Each `create_diff_*` tool returns a `diffId` and a list of `differences`. Each difference
  has an `id` (used in `changes` lists and as `changeId`), the object's `name` and `type`, a `change` of `Add`, `Edit`,
  `Rename` or `Delete`, and, for a rename, its `previousName`. Calling a `create_diff_*` tool again replaces the
  previous diff of that type, so the earlier `diffId` is no longer valid.
- **Parameter types**: IDs (`workspaceId`, `diffId`, `migrationsDiffId`, `checkpointId` and `changeId`) are strings
  returned by earlier tool calls. Copy them exactly rather than constructing them. `changes` is an array of strings,
  such as `["chg-1", "chg-3"]`, or `["*"]` to select everything. `generateUndoScripts` and `rebuild` take `true` or `false`;
  `projectFolder`, `file` and `description` are strings.
- **Errors**: A failed call returns an error result containing the error code and message, for example
  `PROJECT_NOT_LOADED Project workspace 'ws-1234' is not loaded.` The
  [Troubleshooting](<Usage/Flyway MCP Server/MCP Server Troubleshooting>) page covers the common ones.

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

### What each tool reads and writes

| Tool                             | Reads                                                         | Writes                                |
|----------------------------------|---------------------------------------------------------------|---------------------------------------|
| `load_project`                   | `flyway.toml` and the project files                           | Nothing in your project               |
| `reload_project`                 | `flyway.toml` and the project files                           | Nothing in your project               |
| `create_schema_model_checkpoint` | The schema model                                              | Nothing in your project               |
| `create_diff_schema_model`       | The development database and the schema model                 | Nothing in your project               |
| `update_schema_model`            | The stored schema model diff                                  | Schema model files                    |
| `create_diff_migrations`         | The schema model and migration scripts                        | The shadow database (builds migrations into it) |
| `generate_migrations`            | The stored migrations diff                                    | Migration script files                |
| `review_code`                    | One migration script                                          | Nothing                               |
| `test_migrate`                   | Pending migration scripts                                     | The shadow database                   |
| `create_diff_development`        | The schema model and the development database                 | Nothing in your project               |
| `update_development`             | The stored development diff                                   | The development database              |
| `get_diff_details`               | A stored diff                                                 | Nothing                               |

Only `update_development` changes your development database. `create_diff_migrations` and `test_migrate` use the shadow
database, and the `create_diff_*` tools keep their results in the server's temporary workspace rather than in your
project.

### load_project

Loads a Flyway project into a workspace. Required before any other tool can be used.

| Parameter       | Required | Description                                                            |
|-----------------|----------|------------------------------------------------------------------------|
| `projectFolder` | Yes      | Absolute path to the directory containing the project's `flyway.toml`. |

Returns a `workspaceId`, used by all subsequent tool calls.

### reload_project

Reloads a project from disk for an existing workspace, without discarding previously computed artifacts. Prefer this
over `load_project` when you've only edited the project's TOML files.

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

Returns a `diffId` and a list of `differences`.

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

Returns a `diffId` and a list of `differences`.

### generate_migrations

Generates migration scripts from a migrations diff. Scripts are named automatically using the project's configured
naming convention.

Whether an undo script is also generated for each versioned script is controlled by the project's
[`flyway.generate.types`](<Configuration/Flyway Namespace/Flyway Generate Namespace/Flyway Generate Types Setting>)
TOML setting, e.g.:

```toml
[flyway.generate]
types = ["versioned", "undo"]
```

unless overridden for this call via `generateUndoScripts`. If `flyway.generate.types` isn't set at all, the
`flywayDesktop.generate.undoScripts` toml setting is used as a fallback, e.g.:

```toml
[flywayDesktop.generate]
undoScripts = true
```

| Parameter             | Required | Description                                                                                                                                          |
|------------------------|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| `workspaceId`          | Yes      | Workspace ID from `load_project`.                                                                                                                   |
| `migrationsDiffId`     | Yes      | Diff ID from `create_diff_migrations`.                                                                                                               |
| `changes`              | Yes      | List of change IDs to include, or `*` for all changes.                                                                                              |
| `description`          | No       | Description used in the generated migration script filenames.                                                                                       |
| `generateUndoScripts`  | No       | Whether to also generate an undo script. If unset, follows the project's `flyway.generate.types` setting; if true/false, overrides it for this call only. |

Returns the `migrationFolder` path, the list of generated `files` (relative to that folder), any `warnings` raised during
generation (each with a `file`, `message`, `type` and `severity`), and whether undo scripts were generated
(`undoScriptsGenerated`).

### review_code

Runs code review against a single migration script and reports the rule violations found. It uses the project's code
review configuration, as `flyway check -code` does.

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
`failed`, or `pending`), `error` (the error message from the failed migration; omitted when all migrations succeeded),
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

Returns a `diffId` and a list of `differences`. When `checkpointId` is supplied, also returns
`threeSidedDifferences`—the same differences, each tagged with how the schema model has moved since the checkpoint
(new, existing, missing, or conflict).

### update_development

Applies selected changes from a development diff to the development database. **This modifies the database that your
`flywayDesktop.developmentEnvironment` points at.** For supported database types, a dependency of a selected change
is included automatically and, where available, listed in `objects`.

| Parameter     | Required | Description                                          |
|---------------|----------|------------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.                    |
| `diffId`      | Yes      | Diff ID from `create_diff_development`.              |
| `changes`     | Yes      | List of change IDs to apply, or `*` for all changes. |

Returns `changesDeployed` (a boolean indicating whether a deployment action was performed against development—this
may be `false` if no changes were selected) and `objects`, the list of objects updated in the development database,
each with a `name`, a `type` and where the update came from (`origin`). `objects` is only given for some database types,
and is omitted when nothing was applied.

### get_diff_details

Returns the unified diff text for a single changed object within a diff previously created by any `create_diff_*` tool,
in the same workspace.

| Parameter     | Required | Description                                    |
|---------------|----------|------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.              |
| `diffId`      | Yes      | Diff ID from any `create_diff_*` tool.         |
| `changeId`    | Yes      | ID of the changed object to fetch details for. |

Returns the unified diff as `text`. Very large objects fail with `The object detail exceeds the rendering limit of 500
lines.`
