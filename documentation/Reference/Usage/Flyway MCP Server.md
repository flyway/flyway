---
subtitle: Flyway MCP Server
---

{% include enterprise.html %}

## What it is

**MCP** (Model Context Protocol) is an open standard that lets AI coding agents call tools exposed by an external
server,
instead of just reading project's files.

The **Flyway MCP** server exposes tools for the same development workflow you'd perform in Flyway Desktop or the Flyway
CLI: inspecting changes in development database, updating the schema model and generating migration scripts. An agent
can use these tools explicitly rather than having to infer them from your project.

At a high level:

**It can:**

- Read your project configuration and schema model
- Diff the development database/schema model/migration scripts against each other
- Update the schema model and generate migration scripts
- Apply changes to whichever environments your `flyway.toml`'s `[flywayDesktop]` development and shadow settings
  point at

**It can't:**

- Run Flyway verbs directly (a curated list of tools is provided instead)
- Modify your `flyway.toml` configuration

These boundaries apply to the MCP tools themselves. If `[flywayDesktop]`'s development or shadow environments point
at something other than a development database, or your AI tool edits `flyway.toml` directly outside the MCP
server's tools, those boundaries don't apply—see [Best practices](#best-practices) for securing every environment
the server can reach.

This feature is in **preview** and under active development. Tool names, parameters, and behavior may change
significantly, or tools may be removed, in future releases without notice.

---

## Setup

### 1. Prerequisites

- A build of Flyway **13.0.0 or later** (but latest release recommended),
  [downloaded here](https://documentation.red-gate.com/fd/installers-172490864.html). You must be
  [licensed and authenticated](https://documentation.red-gate.com/fd/licensing-164167730.html) for Flyway
  Enterprise -- check this by running `flyway version`, which reports your current license tier and version.
    - No organization-level opt-in is required beyond the license—any user licensed for Flyway Enterprise can use the
      MCP server.
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

### 2. Register the Flyway MCP server with your AI tools

The Flyway MCP server is a **stdio MCP server**—your AI tool starts it as a local process and communicates with it
directly, rather than connecting to it over HTTP. Most AI tools' own MCP documentation categorizes servers this
way.

Many AI tools support registering an MCP server by adding a config file to the root of your repository (the same
folder as your `.git` directory)—though this isn't necessary for every tool, and exact file names, locations, and
formats vary.

See [Register with your AI tools](<Usage/Flyway MCP Server/Register with your AI tools>) for an
example config using `.mcp.json`, or go straight to your tool's own setup docs:

- [Claude Code](https://code.claude.com/docs/en/mcp)
- [VS Code (GitHub Copilot Chat)](https://code.visualstudio.com/docs/copilot/customization/mcp-servers)
- [GitHub Copilot coding agent](https://docs.github.com/en/copilot/how-tos/copilot-on-github/customize-copilot/configure-mcp-servers)
- [OpenAI Codex](https://developers.openai.com/codex/mcp)

#### Outline your project structure and process

Consider adding a `CLAUDE.md`/`AGENTS.md` file describing your project's structure and the development workflow you
want the agent to follow.

If your repository contains several Flyway projects (for example, one per database in a replicated setup), this is
a good place to outline what each one is, so the agent knows which project to load when you refer to one by name
(for example, "find the changes I made in NodeB"). A single `.mcp.json` at the repository root is enough for
this—you don't need one per project.

### 3. Start your AI tool

Start your AI tool. It should detect the Flyway MCP server you registered (for example, via `.mcp.json`) and prompt
you to enable it. Once enabled, ask it to load your project first, then ask it to perform Flyway actions (for
example, "generate migrations for the changes in my development database") and it will call the appropriate tools
below on your behalf.

### 4. Try it out

A few prompts to confirm everything's working, run in order against a project with some uncommitted database
changes:

| Ask the agent to...                                     | Expect...                                                                                       |
|---------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| "Load the Flyway project in this folder"                | A workspace ID back—if you don't see one, the project wasn't loaded and nothing else will work. |
| "Diff my development database against the schema model" | A list of changes (or confirmation there are none).                                             |
| "Apply those schema model changes"                      | Confirmation of which schema model files were updated.                                          |

See [Worked example](<Usage/Flyway MCP Server/Worked Example>) for a fuller walkthrough of what the agent actually
does in response to a request like this.

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

See [Tool reference](<Usage/Flyway MCP Server/Tool Reference>) for the full list of available tools, their parameters,
and the concepts (workspaces, diffs, artifacts) they share.

---

## Worked example

See [Worked example](<Usage/Flyway MCP Server/Worked Example>) for a walkthrough of a natural-language request and
the sequence of tool calls the agent makes in response, with sample inputs and outputs.
