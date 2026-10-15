---
subtitle: Configuration
---

This page covers the `command` and `args` fields inside the `flyway` entry of your `.mcp.json`—see
[Setup](<Usage/Flyway MCP Server>) if you haven't created that file yet.

```json
{
  "mcpServers": {
    "flyway": {
      "type": "stdio",
      "command": "<path to flyway>",
      "args": [
        "mcp",
        "-mcp.toolsets=develop_state",
        "-mcp.maxLogs=100"
      ]
    }
  }
}
```

## Command flag in `.mcp.json`

Set `command` to your `flyway` executable. If you installed Flyway following the
standard [installation instructions](<Usage/Command-line>), `flyway` is added to your system `PATH`, so
`command: "flyway"` is enough—the AI tool runs it just like you would from a terminal.

Use the full path instead (e.g. `C:\flyway-{version}\flyway.cmd` or `/opt/flyway-{version}/flyway`) if:

- `flyway` isn't on your `PATH`, or
- your AI tool is launched from a desktop shortcut rather than a terminal—some GUI-launched apps don't inherit your
  shell's `PATH`, particularly on macOS.

On Windows, if you do use a bare `flyway` and it isn't picked up, try `flyway.cmd` explicitly—some process launchers
don't resolve a bare command name to its `.cmd` extension the way a terminal does.

## The `args` list in `.mcp.json`

Configuration can be set in the `args` list in `.mcp.json`, as shown by the `-mcp.toolsets` setting in the example
above.

| Parameter      | Description                                                                                               | Default           |
|----------------|-----------------------------------------------------------------------------------------------------------|-------------------|
| `mcp.toolsets` | The toolsets to enable on the MCP server. Available values: `develop_migrations`, `develop_state`, `all`. | *(none—required)* |
| `mcp.maxLogs`  | The maximum number of MCP server log files to keep on disk.                                               | `100`             |

### Toolsets in `.mcp.json`

Toolsets allow you to configure which Tools you're happy for the Flyway MCP server to use with your prompts. See
[Tool reference](<Usage/Flyway MCP Server/Tool Reference>) for what each tool does.

`mcp.toolsets` has no default. You must explicitly configure at least one, or the server will fail to start with:

```
mcp.toolsets must be configured with at least one toolset. Available values: develop_migrations, develop_state, all
```

To enable more than one, separate them with commas.

| Toolset              | Enables                                                                                                                                                                         |
|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `develop_state`      | Keeping the schema model and development database in sync, without the tools that generate migration scripts.                                                                   |
| `develop_migrations` | Everything in `develop_state`, plus capturing schema model checkpoints, diffing against migration scripts, generating and testing migrations, and reviewing code.              |
| `all`                | Every available toolset. Today this is identical to `develop_migrations`.                                                                                                       |

`develop_migrations` includes everything in `develop_state`, so there's no need to combine them. Choose `develop_state` if you
don't want an agent generating migration scripts, and `develop_migrations` if you do. Prefer one of these over `all`, because
what `all` enables may change between releases.

See the Toolsets column of the [Tools](<Usage/Flyway MCP Server/Tool Reference#ToolReference-Tools>) table in the Tool reference for
the tools each toolset enables.

Three-sided development diffs depend on `create_schema_model_checkpoint`, so they're only available with
`develop_migrations` or `all`.
