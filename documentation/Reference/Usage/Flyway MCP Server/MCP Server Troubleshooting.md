---
subtitle: Troubleshooting
---

Common problems when setting up or using the [Flyway MCP Server](<Usage/Flyway MCP Server>), and what to do about
them.

## `flyway` isn't recognized as a command

If `flyway version` fails in your terminal, `flyway` isn't on your system `PATH`—add the installation's `bin`
folder to your `PATH`, or use the full path to the `flyway`/`flyway.cmd` executable in `.mcp.json`'s `command`
field instead. See [Command flag in `.mcp.json`](<Usage/Flyway MCP Server/MCP Server Configuration>) for details, including
the Windows `.cmd` caveat for GUI-launched AI tools. If you use a Windows path in `.mcp.json`, remember to escape the
backslashes (`C:\\flyway\\flyway.cmd`), or the file isn't valid JSON.

## The AI tool doesn't detect the server

- Check the file is named exactly `.mcp.json`—including the leading dot. A file saved as `mcp.json` (no dot) or
  `.mcp.json.txt` (extension hidden by your OS/editor) won't be picked up.
- Check it's in the folder your AI tool was launched from (or pointed at via a flag, e.g. Claude Code's
  `--mcp-config`)—see [Register with your AI tools](<Usage/Flyway MCP Server/Register with your AI tools>) for
  where that should be.
- Restart your AI tool after creating or editing `.mcp.json`—most tools only read it on startup.

## The server starts and then immediately stops

Check the AI tool's MCP logs, or run the same command from a terminal (for example
`flyway mcp -mcp.toolsets=develop_state`) to see the error.

### mcp.toolsets must be configured with at least one toolset

The `-mcp.toolsets` argument is mandatory. Add it to `args` in `.mcp.json` with at least one of the
[available toolsets](<Usage/Flyway MCP Server/MCP Server Configuration#MCPServerConfiguration-Toolsetsin.mcp.json>).

## Licensing error encountered during tool call

This usually means the current permit has expired or needs to be refreshed. Run `flyway version` in a terminal: the
first line should name an Enterprise edition, and the MCP server needs Flyway 13.0.0 or later. If it doesn't, or your
permit has expired, re-authenticate or refresh your permit. The
[prerequisites](<Usage/Flyway MCP Server#FlywayMCPServer-Setup>) list the licensing requirements.

## Nothing seems to happen after asking the agent to do something

Every session needs to `load_project` before any other tool works. If the agent hasn't done this yet, other tool
calls will fail or the agent may fall back to just reading files instead of using the MCP tools. Start a new
session with an explicit instruction, and check the result matches what's expected:

| Ask the agent to... | Expect... |
|----------------------|------------|
| "Load the Flyway project in this folder" | A workspace ID back—if you don't see one, the project wasn't loaded and nothing else will work. |
| "Diff my development database against the schema model" | A list of changes (or confirmation there are none). |
| "Apply those schema model changes" | Confirmation of which schema model files were updated. |
| "Diff the schema model against my migration scripts" | A list of changes not yet represented by a migration script. |
| "Generate migration scripts from that" | The path to the new migration scripts. |

See [Worked example](<Usage/Flyway MCP Server/Worked Example>) for a fuller walkthrough of a request like this.

## Can't find `flyway.toml`, or the project folder isn't where expected

The folder Flyway is installed into is not your project folder—your project (with its own `flyway.toml`,
`schema-model`, and `migrations` folders) is likely a separate folder you created or that Flyway Desktop created
for you. If you're using Flyway Desktop, check where it actually created that folder before starting your AI
agent—see [Register with your AI tools](<Usage/Flyway MCP Server/Register with your AI tools>) for the Flyway
Desktop subfolder note. The agent needs this path to call `load_project`, even though `.mcp.json` itself usually
lives at your repository root rather than next to `flyway.toml`. If the path you give is wrong, `load_project` fails with
`Project folder does not exist: <path>` or `Project folder is not a directory: <path>`.

Also check for a `flyway.user.toml` file alongside `flyway.toml`—Flyway Desktop creates this to hold
environment and credential definitions, and Flyway merges it with `flyway.toml` automatically. See
[Tutorial: Flyway Desktop migrations workflow with Flyway CLI](<Tutorials/Tutorial - Flyway Desktop migrations workflow with Flyway CLI>)
for how the two files relate.

## flywayDesktop.developmentEnvironment or flywayDesktop.shadowEnvironment isn't configured

Tools that use the development or shadow environment can fail with one of these errors, where `<key>` is
`flywayDesktop.developmentEnvironment` or `flywayDesktop.shadowEnvironment`:

- `'<key>' is not configured. This can be set in the project toml.`
- `The environment specified by '<key>' does not match any environment in the project toml 'environments' namespace.`

The MCP server's `develop_migrations` and `develop_state` toolsets are built around the same development/shadow
workflow as Flyway Desktop. The [prerequisites](<Usage/Flyway MCP Server#FlywayMCPServer-Setup>) describe the required
`[flywayDesktop]` and `[environments.*]` configuration. If your project only has hand-written migrations against a
single target environment, without a development/shadow setup, these toolsets won't have the environments they need.

## Project workspace '<id>' is not loaded

Only one workspace is loaded at a time, and a workspace only lasts for the current server session. This error means the
`workspaceId` the agent used isn't the loaded one. This usually happens because the server restarted, or the agent
loaded a different project (which replaces the previous workspace). Ask the agent to run `load_project` again and use the new
`workspaceId`. Errors are reported with their error code first, for example `PROJECT_NOT_LOADED`.

## No artifact found, or artifact id does not match

These errors mean the `diffId` the agent used is no longer valid. The workspace keeps only one diff of each type, so
calling a `create_diff_*` tool again invalidates the previous `diffId`, and loading a new project discards them all.
Ask the agent to create the diff again and use the new `diffId`. The
[Tool reference](<Usage/Flyway MCP Server/Tool Reference>) explains how workspaces and diffs work.

## Database connection, permission or shadow database errors

Tools that use the development or shadow database (for example `create_diff_schema_model`, `update_development`,
`create_diff_migrations` and `test_migrate`) can fail on database problems. When one does, the underlying database
error is passed back to the agent. Typical causes for this are: a connection failure, a login or permissions problem,
or (for `test_migrate`) a failing migration. For a failing migration, `test_migrate` reports the per-file results and the error
message of the migration that failed. For connection problems, check each environment from a terminal, for example:

```
flyway testConnection -environment=shadow
```

[`testConnection`](<Commands/TestConnection>) reports the database-specific connection error. The account for the shadow environment must be able to create the
objects your migrations create, and the server may reprovision the shadow database as part of building migrations.

## Finding the server's log files

The server writes log files to a fixed location and keeps the most recent `mcp.maxLogs` of them (100 by default). The
[File system access](<Usage/Flyway MCP Server#FlywayMCPServer-Filesystemaccess>) table lists the location for each
operating system. Check the newest file when a tool call fails.

## Credentials saved in plaintext

Changing database credentials through Flyway Desktop's connection dialog writes them into a TOML file as plain
text. Use [secrets management](<Configuration/Flyway Namespace#FlywayNamespace-Secretsmanagementnamespaces>) (environment
variables, a secrets manager, or a resolver such as [Local Secret](<Configuration/Environments Namespace/Environment Resolvers Namespace/Local Secret Resolver>))
instead of leaving passwords in a TOML file that might end up committed to version control.
