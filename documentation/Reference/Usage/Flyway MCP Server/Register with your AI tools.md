---
subtitle: Register with your AI tools
---

## Example: Claude Code's `.mcp.json`

```json
{
  "mcpServers": {
    "flyway": {
      "type": "stdio",
      "command": "flyway",
      "args": [
        "mcp",
        "-mcp.toolsets=develop_state",
        "-mcp.maxLogs=100"
      ]
    }
  }
}
```

This assumes `flyway` is on your `PATH`; use a full path if it isn't (see
[Configuration](<Usage/Flyway MCP Server/MCP Server Configuration>)).

This example enables `develop_state`, the narrowest toolset. Change it to `-mcp.toolsets=develop_migrations` if you also
want the agent to generate migration scripts, as in the [Worked example](<Usage/Flyway MCP Server/Worked Example>).

See [Configuration](<Usage/Flyway MCP Server/MCP Server Configuration>) for what the `command` and `args` fields control,
including the available toolsets.

Instead of writing the file by hand, you can have Claude Code create it by running this from your repository root:

```bash
claude mcp add --transport stdio --scope project flyway -- flyway mcp -mcp.toolsets=develop_state
```

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
      "type": "stdio",
      "command": "flyway",
      "args": [
        "mcp",
        "-mcp.toolsets=develop_state",
        "-mcp.maxLogs=100"
      ]
    }
  }
}
```

OpenAI Codex stores MCP servers in TOML rather than JSON: `~/.codex/config.toml`, or `.codex/config.toml` in a trusted
project. Each server is a `[mcp_servers.<name>]` table:

```toml
[mcp_servers.flyway]
command = "flyway"
args = ["mcp", "-mcp.toolsets=develop_state", "-mcp.maxLogs=100"]
```

You can also add it with `codex mcp add flyway -- flyway mcp -mcp.toolsets=develop_state`.

The `command` and `args` values are the same in every tool; only the file format differs.

Other AI tools that can run a local `stdio` MCP server, such as Claude Desktop, Cursor or JetBrains tools, take the same
`command` and `args` in their own config format. Check their documentation for the file location and shape.

## Checking this file into version control

Whether to check a config file like `.mcp.json` in, or keep it local (for example, via `.gitignore`), is up to you.

- **If you check it in**, avoid machine-specific local file paths in `command`. Use a bare `flyway` (relying on
  every contributor having it on their `PATH`) rather than something like `C:\flyway-{version}\flyway.cmd`, which
  won't exist on a teammate's machine.
- **If you check it in**, be aware that cloud-hosted agents supporting the same convention—not just AI tools
  running interactively on your own machine—may also pick it up and start the Flyway MCP server on your behalf.
  If that's not something you want, use your tool's own install instructions instead of a checked-in config file.
