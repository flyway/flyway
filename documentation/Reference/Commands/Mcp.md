---
subtitle: Mcp
---

## Description

The `mcp` command starts the [Flyway MCP Server](<Usage/Flyway MCP Server>), which exposes tools that let AI coding
agents work with a Flyway project directly.

## Usage examples

### Starting the server with the state toolset

The minimal command needed to run the server is:

<pre class="console">&gt; flyway mcp -mcp.toolsets=develop_state</pre>

This enables only the tools for keeping the schema model and development database in sync. Use
`-mcp.toolsets=develop_migrations` to also enable the tools that generate migration scripts, or `-mcp.toolsets=all` to
enable every toolset (currently identical to `develop_migrations`). See
[Flyway MCP Server](<Usage/Flyway MCP Server>) for how to configure this command in an AI tool's `.mcp.json` (or
equivalent), and [Configuration](<Usage/Flyway MCP Server/MCP Server Configuration>) for what each toolset enables and
other parameters.

## Parameters

### Required

| Parameter  | Namespace | Description                                                                                                                                                                                         |
|------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `toolsets` | mcp       | The toolsets to enable on the MCP server. If omitted, the server fails to start with `mcp.toolsets must be configured with at least one toolset`. See [Configuration](<Usage/Flyway MCP Server/MCP Server Configuration>). |

### Optional

| Parameter | Namespace | Description |
|-----------|-----------|-------------|
| `maxLogs` | mcp       | The maximum number of MCP server log files to keep on disk. Defaults to `100`. Log locations are listed under [File system access](<Usage/Flyway MCP Server#FlywayMCPServer-Filesystemaccess>). |

Universal commandline parameters are listed [here](<Command-line Parameters>).
