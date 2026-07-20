---
subtitle: Flyway MCP Server
---

## What it is

{% include enterprise.html %}

The Flyway MCP (Model Context Protocol) server lets AI coding agents and assistants work with a Flyway project directly.
It exposes a set of tools an agent can call to follow the same development workflow you'd normally drive by hand through
Flyway Desktop or the command line: inspecting changes in a development database, updating the project's schema model,
generating migration scripts, and applying changes to a development database.

This feature is in **preview** and under active development. Tool names, parameters, and behavior may change
significantly, or tools may be removed, in future releases without notice.

---

## Setup

### 1. Prerequisites

- A build of Flyway with MCP server support. You must be licensed and authenticated for Flyway Enterprise—check this
  by running `flyway version`, which reports your current license tier.
- Your project's `flyway.toml` must have both a development and a shadow environment configured under the
  `[flywayDesktop]` namespace:

```toml
[flywayDesktop]
developmentEnvironment = "development"
shadowEnvironment = "shadow"
```

These names must correspond to environments defined in the project's `environments` namespace. The MCP tools use the
development environment to detect and apply schema changes, and the shadow environment to build migration scripts, which
are the same environments Flyway Desktop uses.

### 2. Add an `.mcp.json` file

Add an `.mcp.json` file to the root of your project:

```json
{
  "mcpServers": {
    "flyway": {
      "type": "stdio",
      "command": "<path to flyway>",
      "args": [
        "mcp",
        "-mcp.toolsets=develop_migrations"
      ]
    }
  }
}
```

#### Command

Set `command` to your `flyway` executable. If you installed Flyway following the
standard [installation instructions](<Usage/Command-line>), `flyway` is added to your system `PATH`, so
`command: "flyway"` is enough—the AI tool runs it just like you would from a terminal.

Use the full path instead (e.g. `C:\flyway-{version}\flyway.cmd` or `/opt/flyway-{version}/flyway`) if:

- `flyway` isn't on your `PATH`, or
- your AI tool is launched from a desktop shortcut rather than a terminal—some GUI-launched apps don't inherit your
  shell's `PATH`, particularly on macOS.

On Windows, if you do use a bare `flyway` and it isn't picked up, try `flyway.cmd` explicitly—some process launchers
don't resolve a bare command name to its `.cmd` extension the way a terminal does.

#### Toolsets

`mcp.toolsets` has no default—you must explicitly configure at least one, or the server will fail to start. To enable
more than one, separate them with commas, e.g. `-mcp.toolsets=develop_migrations,develop_state`.

| Toolset              | Enables                                                                                                                                                          |
|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `develop_migrations` | The full development workflow: capturing database changes into the schema model, generating migration scripts, and applying changes to the development database. |
| `develop_state`      | Keeping the schema model and development database in sync, without the tools that generate migration scripts.                                                    |
| `all`                | Every available tool.                                                                                                                                            |

### 3. Start your AI tool

Start your AI tool (e.g. Claude Code) from the project folder. It should detect the MCP server defined in `.mcp.json`
and prompt you to enable it. Once enabled, you can ask the agent to perform Flyway actions (for example, "generate
migrations for the changes in my development database") and it will call the appropriate tools below on your behalf.

### Configuration reference

These can be set in the `args` list in `.mcp.json`, as shown by the `-mcp.toolsets` setting in the example above.

| Parameter      | Description                                                                                               | Default           |
|----------------|-----------------------------------------------------------------------------------------------------------|-------------------|
| `mcp.toolsets` | The toolsets to enable on the MCP server. Available values: `develop_migrations`, `develop_state`, `all`. | *(none—required)* |
| `mcp.maxLogs`  | The maximum number of MCP server log files to keep on disk.                                               | `100`             |

---

## File system access

Beyond the project files and databases it's pointed at, the MCP server itself reads and writes to a few fixed locations
on the machine it's running on. If you need to allowlist, sandbox, or audit what the server touches, take these into
account:

| Purpose                                                                    | Windows                               | Linux                                                                 |
|----------------------------------------------------------------------------|---------------------------------------|-----------------------------------------------------------------------|
| Temporary workspace results (e.g. diff artifacts) for the current session. | `%tmp%`                               | `/tmp` (or wherever `$TMPDIR`/the JVM's `java.io.tmpdir` resolves to) |
| Licensing and feature usage information.                                   | `%appdata%\Redgate`                   | `~/.config/Redgate`                                                   |
| Server log files (see `mcp.maxLogs`).                                      | `%localappdata%\Red Gate\Logs\Flyway` | `~/.local/share/Red Gate/Logs/Flyway`                                 |

---

## Best practices

- **Only enable the toolsets you need.** Start with the narrowest toolset that covers your workflow (e.g.
  `develop_state` if you don't want an agent generating migration scripts) rather than `all`.
- **Only use the MCP server on projects and databases you trust.** The agent reads project files and queries connected
  databases as part of normal operation, so anything it has access to should be treated as within the agent's reach.
- **Secure every environment the server can reach.** This includes both the development and shadow environments defined
  in your toml, not just production-like ones.
- **Never store credentials in plaintext.** Use Flyway's
  [secrets management](<Configuration/Flyway Namespace#secrets-management-namespaces>) functionality (e.g. environment
  variables, a secrets manager, or the local secret resolver) instead of putting passwords or tokens directly in your
  toml.
- **Guide the agent with project instructions, not tool invocations.** Use your AI tool's project-level instructions
  file (e.g. `CLAUDE.md`) or a skill to describe the development workflow you want followed—for example, that
  database changes should be captured into the schema model before migrations are generated, or that you want to
  review changes before they're applied to a database. Describing the *workflow* rather than which tools to call in
  which order gives the agent room to sequence things sensibly, and keeps your instructions from going stale if tool
  names or parameters change.
- **Review before you apply.** Use the same scrutiny you'd give to any AI-generated change. For example, use
  `get_diff_details` to inspect the unified diff for a change, and review generated migration scripts, before letting an
  agent apply changes to a database.
- **Expect change.** This server is in active development; pin your toolset configuration deliberately and check release
  notes before upgrading, since tool names and behavior may change between versions.

---

## Tool reference

### Concepts

- **Workspace**: Calling `load_project` loads a Flyway project into a workspace, identified by a `workspaceId`. Only one
  workspace is loaded at a time; loading a new project discards artifacts and results from the previous one. If you've
  only changed the project's toml configuration, call `reload_project` instead, which preserves existing artifacts.
- **Diffs and artifacts**: Each `create_diff_*` tool computes the difference between two states of your project (for
  example, the development database and the schema model) and returns a `diffId`. Other tools then consume that
  `diffId`, together with a list of change IDs, to apply or generate something from the selected differences.
- **Selecting changes**: Tools that accept a `changes` list of IDs also accept a single `*` to select every change from
  a diff. You can't mix `*` with specific IDs. If a selected change depends on another change that wasn't selected, the
  dependency is included automatically.

### Tools

| Tool                          | Toolsets                              | Purpose                                                                                                  |
|-------------------------------|---------------------------------------|----------------------------------------------------------------------------------------------------------|
| `load_project`                | `develop_migrations`, `develop_state` | Loads a Flyway project into a new workspace.                                                             |
| `reload_project`              | `develop_migrations`, `develop_state` | Reloads an already-loaded project from disk, preserving existing artifacts.                              |
| `create_before_pull_snapshot` | `develop_migrations`                  | Snapshots the schema model before pulling changes from version control, for use as a baseline diff.      |
| `create_diff_schema_model`    | `develop_migrations`, `develop_state` | Diffs the development database against the project's schema model.                                       |
| `update_schema_model`         | `develop_migrations`, `develop_state` | Applies selected changes from a schema model diff to the schema model files.                             |
| `create_diff_migrations`      | `develop_migrations`                  | Diffs the schema model against the current migration scripts (via the shadow environment).               |
| `generate_migrations`         | `develop_migrations`                  | Generates migration scripts from a migrations diff.                                                      |
| `review_code`                 | `develop_migrations`                  | Runs code review against a single migration script and reports the rule violations found.                |
| `create_diff_development`     | `develop_migrations`, `develop_state` | Diffs the schema model against the development database, in the direction needed to update the database. |
| `update_development`          | `develop_migrations`, `develop_state` | Applies selected changes from a development diff to the development database.                            |
| `get_diff_details`            | `develop_migrations`, `develop_state` | Returns the unified diff text for a single changed object within any diff.                               |

#### load_project

Loads a Flyway project into a workspace. Required before any other tool can be used.

| Parameter       | Required | Description                                                            |
|-----------------|----------|------------------------------------------------------------------------|
| `projectFolder` | Yes      | Absolute path to the directory containing the project's `flyway.toml`. |

Returns a `workspaceId`, used by all subsequent tool calls.

#### reload_project

Reloads a project from disk for an existing workspace, without discarding previously computed artifacts. Prefer this
over `load_project` when you've only edited the project's toml files.

| Parameter     | Required | Description                              |
|---------------|----------|------------------------------------------|
| `workspaceId` | Yes      | Workspace ID returned by `load_project`. |

#### create_before_pull_snapshot

Creates a snapshot of the current schema model before pulling changes from version control. The snapshot can be used as
input to `create_diff_development` to categorize changes by origin (local vs. upstream).

| Parameter     | Required | Description                       |
|---------------|----------|-----------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`. |

Returns a `beforeSnapshotId`, intended for future use as an input to `create_diff_development`.

#### create_diff_schema_model

Diffs the development database against the project's schema model. Feeds into `update_schema_model`. Only one such diff
is stored per workspace at a time.

| Parameter     | Required | Description                       |
|---------------|----------|-----------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`. |

Returns a `diffId` and a list of selectable differences.

#### update_schema_model

Updates the schema model files using selected changes from a schema model diff.

| Parameter     | Required | Description                                          |
|---------------|----------|------------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.                    |
| `diffId`      | Yes      | Diff ID from `create_diff_schema_model`.             |
| `changes`     | Yes      | List of change IDs to apply, or `*` for all changes. |

Returns the list of schema model files that were updated, relative to the project's schema model folder.

#### create_diff_migrations

Diffs the schema model against the current state of the migration scripts, built via the shadow environment. Feeds into
`generate_migrations`. Only one such diff is stored per workspace at a time.

| Parameter     | Required | Description                       |
|---------------|----------|-----------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`. |

Returns a `diffId` and a list of selectable differences.

#### generate_migrations

Generates migration scripts from a migrations diff. Scripts are named automatically using the project's configured
naming convention.

| Parameter          | Required | Description                                                   |
|--------------------|----------|---------------------------------------------------------------|
| `workspaceId`      | Yes      | Workspace ID from `load_project`.                             |
| `migrationsDiffId` | Yes      | Diff ID from `create_diff_migrations`.                        |
| `changes`          | Yes      | List of change IDs to include, or `*` for all changes.        |
| `description`      | No       | Description used in the generated migration script filenames. |

Returns the migration folder path, the list of generated files, and any warnings raised during generation.

#### review_code

Runs code review against a single migration script and reports the rule violations found.

| Parameter     | Required | Description                                                            |
|---------------|----------|------------------------------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.                                      |
| `file`        | Yes      | Migration script to review. Absolute, or relative to the project root. |

Returns the absolute path of the reviewed file, the list of issues (rule violations) found, the definitions of the
violated rules, and `total_rules_checked` (the number of rules evaluated).

#### create_diff_development

Diffs the schema model against the development database, describing the changes needed to bring the development database
in line with the schema model. Feeds into `update_development`. Only one such diff is stored per workspace at a time.

| Parameter     | Required | Description                       |
|---------------|----------|-----------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`. |

Returns a `diffId` and a list of selectable differences.

#### update_development

Applies selected changes from a development diff to the development database.

| Parameter     | Required | Description                                          |
|---------------|----------|------------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.                    |
| `diffId`      | Yes      | Diff ID from `create_diff_development`.              |
| `changes`     | Yes      | List of change IDs to apply, or `*` for all changes. |

Returns `changesDeployed` (a boolean indicating whether a deployment action was performed against development—this
may be `false` if no changes were selected) and the list of objects updated in the development database.

#### get_diff_details

Returns the unified diff text for a single changed object within a diff previously created by any `create_diff_*` tool,
in the same workspace.

| Parameter     | Required | Description                                    |
|---------------|----------|------------------------------------------------|
| `workspaceId` | Yes      | Workspace ID from `load_project`.              |
| `diffId`      | Yes      | Diff ID from any `create_diff_*` tool.         |
| `changeId`    | Yes      | ID of the changed object to fetch details for. |

Returns the unified diff text for the requested change.

---

## Typical workflows

### Capture database changes into the schema model

Use this after making changes directly in the development database, to bring the schema model up to date.

1. `load_project`
2. `create_diff_schema_model`
3. (optional) `get_diff_details`, to review individual changes
4. `update_schema_model`, selecting the changes to keep

### Generate migration scripts

Use this once the schema model reflects the changes you want to ship, to produce migration scripts for them.

1. `create_diff_migrations`
2. `generate_migrations`, selecting the changes to include

### Apply schema model changes to the development database

Use this to bring your development database in line with the schema model—for example, after pulling migration scripts
a teammate has added and applying them via the shadow environment build.

1. `create_diff_development`
2. (optional) `get_diff_details`, to review individual changes
3. `update_development`, selecting the changes to apply

If you edit the project's `flyway.toml` mid-session, call `reload_project` rather than `load_project` to keep existing
diffs and artifacts.
