---
pill: check.changes
subtitle: flyway.check.changes
---
# Check: Changes
{% include enterprise.html %}

{% include commandlineonly.html %}

# Description
This produces a report indicating differences between applied migration scripts on your target database and pending migrations scripts.

See also the [Change Report Concept](<Concepts/Check Changes Concept>) page
## Usage

### Commandline
```powershell
./flyway check -changes -environment=prod -check.buildEnvironment=build
```
- [`environment`](Configuration/Parameters/Flyway/Environment) - This would typically be your target/production environment
   - If you can't access your target database from your build environment you should use [`check.appliedMigrations`](<Configuration/Parameters/Flyway/Check/Applied Migrations>) with the list of migrations that represent the current state if your target.
   - You could alternatively use a snapshot of your target database instead and tell Flyway to use this via  [`check.deployedSnapshot`](<Configuration/Parameters/Flyway/Check/Deployed Snapshot>)
- [`check.buildEnvironment`](<Configuration/Parameters/Flyway/Check/Build Environment>) - This is where Flyway will populate the database based on the migrations available
  - If you do not have a build database you could specify a snapshot via [`check.nextSnapshot`](<Configuration/Parameters/Flyway/Check/Next Snapshot>)

If you want to explicitly specify the filename of the generated report then the [`reportFilename`](<Configuration/Parameters/Flyway/Report Filename>) parameter exists.

### TOML Configuration File
Typically you would define the [environments](Configuration/Parameters/Environments) you want to work with in the TOML configuration file. 

Alternatively you can configure some or all of the [environments](Configuration/Parameters/Environments) directly on the command line when calling Flyway.

### Configuration File
[Environments](Configuration/Parameters/Environments) are not supported in .conf files so the build environment needs to be configured using the parameters:
- [`check.buildUrl`](<Configuration/Parameters/Flyway/Check/Build Url>)
- [`check.buildUser`](<Configuration/Parameters/Flyway/Check/Build User>)
- [`check.buildPassword`](<Configuration/Parameters/Flyway/Check/Build Password>)

For example:
```conf
flyway.check.buildUrl=jdbc:sqlserver://localhost:1433;databaseName=build;encrypt=false
flyway.check.buildUser=dbUser
flyway.check.buildPassword=password
```
