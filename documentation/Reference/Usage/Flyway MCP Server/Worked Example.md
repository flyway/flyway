---
subtitle: Worked Example
---

This page walks through giving high level instructions to an agent, showing tool call by tool call, so
you can see how the pieces from [Tool reference](<Usage/Flyway MCP Server/Tool Reference>) fit together in
practice. You do not need to ask for specific tool calls, the agent will figure that out from your instructions and the
current state of the project.

## Load the project

Every session starts here, regardless of what you ask for next.

1. `load_project` with `projectFolder` set to the absolute path of the directory containing `flyway.toml`.

   Returns a `workspaceId`, e.g. `ws-1234`—the agent reuses this in every subsequent tool call for the session.

If you edit the project's `flyway.toml` mid-session (for example, to add an environment), the agent should call
`reload_project` with the same `workspaceId` rather than `load_project` again—this keeps existing diffs and
artifacts instead of discarding them.

## "Generate migration scripts for the views I've updated in my development database"

### 1. Capture the database changes into the schema model first

The request describes changes that exist in the database but haven't been captured into the schema model yet, so
the agent starts there:

1. `create_diff_schema_model` with `workspaceId: "ws-1234"`.

   Returns a `diffId`, e.g. `diff-1`, and a list of changed objects, e.g.
   `[{changeId: "chg-1", object: "dbo.CustomerSummary", type: "View"}]`.

2. `update_schema_model` with `workspaceId: "ws-1234"`, `diffId: "diff-1"`, `changes: "*"` (since the request said
   "the views I've updated", not a specific subset).

   Returns the list of schema model files that were updated, e.g. `["Views/dbo.CustomerSummary.sql"]`.

At this point the schema model reflects the database changes—nothing has touched a migration script yet.

### 2. Diff the schema model against the current migrations

3. `create_diff_migrations` with `workspaceId: "ws-1234"`.

   Diffs the schema model against the current migration scripts (built via the shadow environment), and returns a
   `diffId`, e.g. `diff-2`, with a list of changes not yet represented by a migration script—in this case, the view
   change just captured.

### 3. Generate the migration script

4. `generate_migrations` with `workspaceId: "ws-1234"`, `migrationsDiffId: "diff-2"`, `changes: "*"`, and a
   `description` the agent infers from context (e.g. `"update customer summary view"`).

   Returns the migration folder path and the generated files, e.g.
   `["V1.2__update_customer_summary_view.sql"]`, along with any warnings raised during generation.

### 4. Review before finishing

5. (recommended) `review_code` with `workspaceId: "ws-1234"` and `file` set to the generated script's path.

   Returns any rule violations found, their definitions, and `total_rules_checked`. A well-behaved agent surfaces
   these to you rather than silently ignoring them.

Four tool calls, chosen and sequenced by the agent from one sentence of instruction. A different request would
have taken a different path. For example, "apply my schema model changes to my development database" would start
from `create_diff_development` instead, skipping the capture and generate steps entirely.