---
pill: cli_init
subtitle: 'Command-line: init'
---
# Command-line: init

Initialize a new Flyway TOML project

## Usage

<pre class="console"><span>&gt;</span> flyway init [options]</pre>

## Options

The following options can be provided to the `init` command in the format -key=value:
- `init.projectName` [REQUIRED] - The name of the project
- `init.databaseType` [REQUIRED] - The database type of the project
- `init.fileName` - The name of the main TOML settings file. Defaults to 'flyway.toml'
- `init.from` - If specified, instructs Flyway to initialize a project from an existing project. The supported existing project types are:
  - A Flyway '.conf' project - A Flyway project with a '.conf' configuration file. In this case `init.from` must be the path to the '.conf' configuration file
  - A SQL Source Control project - In this case `init.from` must be the path to the folder containing the project
  - A Source Control For Oracle project - In this case `init.from` must be the path to the folder containing the project
- `init.fromType` - By default, Flyway infers the type of the existing project based on what is provided to `init.from`. You can explicitly specify this type instead if needed. The supported types are:
  - `Default` - This type is used when initializing a new project, not from an existing project
  - `Conf` - This type is used when initializing a project from an existing Flyway '.conf' project
  - `SqlSourceControl` - This type is used when initializing a project from an existing SQL Source Control project
  - `SourceControlForOracle` - This type is used when initializing a project from an existing Source Control For Oracle project

## Examples

### Initialize a new project

<pre class="console">&gt; flyway init -init.projectName=ExampleProject -init.databaseType=postgresql</pre>

### Initialize a project from an existing Flyway '.conf' project

<pre class="console">&gt; flyway init -init.projectName=ExampleProject -init.databaseType=postgresql -init.from=path/to/flyway.conf</pre>

### Initialize a project from an existing SQL Source Control project

<pre class="console">&gt; flyway init -init.projectName=ExampleProject -init.databaseType=sqlserver -init.from=path/to/projectFolder</pre>

### Initialize a project from an existing Source Control For Oracle project

<pre class="console">&gt; flyway init -init.projectName=ExampleProject -init.databaseType=oracle -init.from=path/to/projectFolder</pre>
