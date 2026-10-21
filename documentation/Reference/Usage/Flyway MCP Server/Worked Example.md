---
subtitle: Worked Example
---

This page walks through giving high level instructions to an agent, showing tool call by tool call, so
you can see how the pieces from [Tool reference](<Usage/Flyway MCP Server/Tool Reference>) fit together in
practice. You do not need to ask for specific tool calls, the agent will figure that out from your instructions and the
current state of the project.

This example uses `generate_migrations` and other migration tools, so it needs the `develop_migrations` (or `all`)
toolset. With only `develop_state`, the agent can keep the schema model and development database in sync but can't
generate migrations. See [Configuration](<Usage/Flyway MCP Server/MCP Server Configuration>) to choose a toolset.

## Load the project

Every session starts here, regardless of what you ask for next.

1. `load_project` with `projectFolder` set to the absolute path of the directory containing `flyway.toml`.

   Returns a `workspaceId`, e.g. `ws-1234`—the agent reuses this in every subsequent tool call for the session.

If you edit the project's `flyway.toml` mid-session (for example, to add an environment), the agent should call
`reload_project` with the same `workspaceId` rather than `load_project` again—this keeps existing diffs and
artifacts instead of discarding them.

## "Generate migration scripts for the views I've updated in my development database"

IDs in this example (`ws-1234`, `diff-1`, `chg-1`) are illustrative; real IDs are opaque strings returned by earlier
tool calls.

### 1. Capture the database changes into the schema model first

The request describes changes that exist in the database but haven't been captured into the schema model yet, so
the agent starts there:

1. `create_diff_schema_model` with `workspaceId: "ws-1234"`.

   Returns a `diffId`, e.g. `diff-1`, and a list of `differences`, e.g.

   ```json
   {
     "diffId": "diff-1",
     "differences": [
       { "id": "chg-1", "name": "dbo.CustomerSummary", "type": "View", "change": "Edit" }
     ]
   }
   ```

2. (recommended) `get_diff_details` with `workspaceId: "ws-1234"`, `diffId: "diff-1"` and `changeId: "chg-1"`.

   Returns the unified diff `text` for that one object, so you can check what will change in the schema model before
   anything is written.

3. `update_schema_model` with `workspaceId: "ws-1234"`, `diffId: "diff-1"`, `changes: ["*"]` (since the request said
   "the views I've updated", not a specific subset).

   Returns the list of schema model files that were updated, e.g. `["Views/dbo.CustomerSummary.sql"]`.

At this point the schema model reflects the database changes—nothing has touched a migration script yet.

### 2. Diff the schema model against the current migrations

4. `create_diff_migrations` with `workspaceId: "ws-1234"`.

   Diffs the schema model against the current migration scripts (built via the shadow environment), and returns a
   `diffId`, e.g. `diff-2`, with a list of changes not yet represented by a migration script—in this case, the view
   change just captured.

### 3. Generate the migration script

5. `generate_migrations` with `workspaceId: "ws-1234"`, `migrationsDiffId: "diff-2"`, `changes: ["*"]`, and a
   `description` the agent infers from context (e.g. `"update customer summary view"`).

   Returns the `migrationFolder` path and the generated `files`, e.g.
   `["V1.2__update_customer_summary_view.sql"]`, along with any `warnings` raised during generation. Each warning has
   a `file`, `message`, `type` and `severity`, so the agent can report any issues to you.

### 4. Check the script before finishing

6. `test_migrate` with `workspaceId: "ws-1234"`.

   Runs the pending migration scripts against the shadow database. When everything works, every script is reported as
   `success` and `error` is absent from the response:

   ```json
   {
     "results": [ { "file": "V1.2__update_customer_summary_view.sql", "status": "success" } ],
     "reprovisioned": false
   }
   ```

   If a script fails, execution stops: that script is reported as `failed`, any remaining scripts as `pending`, and
   `error` carries the database's error message:

   ```json
   {
     "results": [
       { "file": "V1.2__update_customer_summary_view.sql", "status": "failed" },
       { "file": "V1.3__add_orders_index.sql", "status": "pending" }
     ],
     "error": "<the error message from the database>",
     "reprovisioned": false
   }
   ```

   A typical agent response is to show you the error, fix the script and run `test_migrate` again.

7. (recommended) `review_code` with `workspaceId: "ws-1234"` and `file` set to the generated script's path.

   Returns any rule violations found, their definitions, and `total_rules_checked`. A well-behaved agent surfaces
   these to you rather than silently ignoring them.

Seven tool calls after loading the project (two of them recommended review steps), chosen and sequenced by the agent
from one sentence of instruction. A different request would have taken a different path. For example, "apply my
schema model changes to my development database" would start from `create_diff_development` instead, skipping the
capture and generate steps entirely.
