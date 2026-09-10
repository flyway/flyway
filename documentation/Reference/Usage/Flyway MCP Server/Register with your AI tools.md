---
subtitle: Register with your AI tools
---

## Example: Claude Code's `.mcp.json`

```json
{
  "mcpServers": {
    "flyway": {
      "type": "stdio",
      "command": "<path to flyway>",
      "args": [
        "mcp",
        "-mcp.toolsets=develop_migrations",
        "-mcp.maxLogs=100"
      ]
    }
  }
}
```

See [Configuration](<Usage/Flyway MCP Server/Configuration>) for what the `command` and `args` fields control,
including the available toolsets.

If your AI tool supports it, you can instead point it at a config file's location with a flag, rather than needing
to run it from the folder containing that file. For example, with Claude Code:

```bash
claude --mcp-config flyway-project/.mcp.json
```

**Note:** if you are using Flyway Desktop, check where it created your project's `flyway.toml` before starting
your AI agent—it may be a subfolder of the folder you originally pointed Desktop at, not that parent folder itself.
The MCP server doesn't need to be started from that folder, but the agent does need to know its path to load the
project.

## Other tools

VS Code (GitHub Copilot Chat) uses the same `stdio` server model, but its own file and shape: `.vscode/mcp.json`,
with servers under a `servers` key instead of `mcpServers`:

```json
{
  "servers": {
    "flyway": {
      "command": "<path to flyway>",
      "args": [
        "mcp",
        "-mcp.toolsets=develop_migrations",
        "-mcp.maxLogs=100"
      ]
    }
  }
}
```

The `command` and `args` values are the same regardless of which tool you're using—only the surrounding file
format changes. Check your AI tool's own documentation for its exact config file location and format.

## Checking this file into version control

Whether to check a config file like `.mcp.json` in, or keep it local (for example, via `.gitignore`), is up to you.

- **If you check it in**, avoid machine-specific local file paths in `command`. Use a bare `flyway` (relying on
  every contributor having it on their `PATH`) rather than something like `C:\flyway-{version}\flyway.cmd`, which
  won't exist on a teammate's machine.
- **If you check it in**, be aware that cloud-hosted agents supporting the same convention—not just AI tools
  running interactively on your own machine—may also pick it up and start the Flyway MCP server on your behalf.
  If that's not something you want, use your tool's own install instructions instead of a checked-in config file.
