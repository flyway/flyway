---
subtitle: flyway.check.code.generateDriftResolutionScripts
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

When drift is detected and this parameter is set, scripts will be generated to help resolve the drift:

- A revert script will be generated, which can be executed against the target environment to remove the drift
- An incorporate script will be generated, which can be executed against your upstream environments and used to
  incorporate the drift into your version controlled schema.
    - If your project contains migrations, this will be generated as a migration script with a version number greater
      than the last existing migration. Otherwise, this will just be a sql script.
- A filter file will be generated which can be used to ignore the drift. This file will currently only be generated for
  SQL Server and Oracle projects.
    - If you have an existing filter file, the new filter file will consist of the drift exclusions applied on top of
      your existing filter configurations.

If drift is found and these files are generated, they will be placed in a new folder, `drift-resolution`, which will be
created in your [configured working directory](<Command-line Parameters/Working Directory Parameter>) if set, and your
current working directory otherwise.

For more information on resolving drift,
see [Checking production environments for drift](https://documentation.red-gate.com/flyway/deploying-database-changes-using-flyway/checking-production-environments-for-drift).

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an
advanced parameter in operations on the Migrations page.

### Command-line

```powershell
"./flyway check -drift -generateDriftResolutionScripts=true"
```

### TOML Configuration File

```toml
[flyway.check]
generateDriftResolutionScripts = true
```