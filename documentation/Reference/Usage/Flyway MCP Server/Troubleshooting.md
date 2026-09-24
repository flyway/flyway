---
subtitle: Troubleshooting
---

Common problems when setting up or using the [Flyway MCP Server](<Usage/Flyway MCP Server>), and what to do about
them.

## `flyway` isn't recognized as a command

If `flyway version` fails in your terminal, `flyway` isn't on your system `PATH`—add the installation's `bin`
folder to your `PATH`, or use the full path to the `flyway`/`flyway.cmd` executable in `.mcp.json`'s `command`
field instead. See [Command flag in `.mcp.json`](<Usage/Flyway MCP Server/Configuration>) for details, including
the Windows `.cmd` caveat for GUI-launched AI tools.

## The AI tool doesn't detect the server

- Check the file is named exactly `.mcp.json`—including the leading dot. A file saved as `mcp.json` (no dot) or
  `.mcp.json.txt` (extension hidden by your OS/editor) won't be picked up.
- Check it's in the folder your AI tool was launched from (or pointed at via a flag, e.g. Claude Code's
  `--mcp-config`)—see [Register with your AI tools](<Usage/Flyway MCP Server/Register with your AI tools>) for
  where that should be.
- Restart your AI tool after creating or editing `.mcp.json`—most tools only read it on startup.

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
lives at your repository root rather than next to `flyway.toml`.

Also check for a `flyway.user.toml` file alongside `flyway.toml`—Flyway Desktop creates this to hold
environment and credential definitions, and Flyway merges it with `flyway.toml` automatically. See
[Tutorial: Flyway Desktop migrations workflow with Flyway CLI](<Tutorials/Tutorial - Flyway Desktop migrations workflow with Flyway CLI>)
for how the two files relate.

## `error: No such environment` or missing `[flywayDesktop]` environments

The MCP server's `develop_migrations` and `develop_state` toolsets are built around the same development/shadow
workflow as Flyway Desktop—see [Prerequisites](<Usage/Flyway MCP Server>) for the required `[flywayDesktop]` and
`[environments.*]` configuration. If your project only has hand-written migrations against a single target
environment, without a development/shadow setup, these toolsets won't have the environments they need.

## Credentials saved in plaintext

Changing database credentials through Flyway Desktop's connection dialog writes them into a toml file as plain
text. Use [secrets management](<Configuration/Flyway Namespace#secrets-management-namespaces>) (environment
variables, a secrets manager, or a resolver such as [Local Secret](<Configuration/Environments Namespace/Environment Resolvers Namespace/Local Secret Resolver>))
instead of leaving passwords in a toml file that might end up committed to version control.
