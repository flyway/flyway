---
subtitle: Mcp
---

## Description

The `mcp` command starts the [Flyway MCP Server](<Usage/Flyway MCP Server>), which exposes tools that let AI coding
agents work with a Flyway project directly.

## Usage examples

### Starting the server with every available tool

The minimal command needed to run the server is:

<pre class="console">&gt; flyway mcp -mcp.toolsets=all</pre>

This enables every tool across every toolset. See [Flyway MCP Server](<Usage/Flyway MCP Server>) for how to configure
this command in an AI tool's `.mcp.json` (or equivalent), and
[Configuration](<Usage/Flyway MCP Server/Configuration>) for the full list of available toolsets and other
parameters.

## Parameters

### Optional

| Parameter  | Namespace | Description                                                                                            |
|------------|-----------|----------------------------------------------------------------------------------------------------------|
| `toolsets` | mcp       | The toolsets to enable on the MCP server. See [Configuration](<Usage/Flyway MCP Server/Configuration>). |
| `maxLogs`  | mcp       | The maximum number of MCP server log files to keep on disk.                                             |

Universal commandline parameters are listed [here](<Command-line Parameters>).
