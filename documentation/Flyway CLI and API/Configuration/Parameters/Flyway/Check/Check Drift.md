---
pill: check.drift
subtitle: flyway.check.drift
---
# Check: Drift
{% include enterprise.html %}

{% include commandlineonly.html %}

# Description
This produces a report indicating differences between structure of your target database and structure created by the migrations applied by Flyway.

See also the [Drift Report Concept](<Concepts/Check Drift Concept>) page
## Usage

### Commandline
```powershell
./flyway check -drift -environment=production -check.buildEnvironment=build
```

You need to specify the environments Flyway should use for conducting the drift report.
- [`environment`](Configuration/Parameters/Flyway/Environment) - This would typically be your target/production environment
- [`check.buildEnvironment`](<Configuration/Parameters/Flyway/Check/Build Environment>) - This is where Flyway will populate the database based on the migrations that exist in your target environment.
  - Alternatively, if you do not have a build database you could use a snapshot via [`check.deployedSnapshot`](<Configuration/Parameters/Flyway/Check/Deployed Snapshot>) instead.

If you want to explicitly specify the filename of the generated report then the [`reportFilename`](<Configuration/Parameters/Flyway/Report Filename>) parameter can be configured.

If you want Flyway to return a non-zero exit code in the event of drift being present then the [`failOnDrift`](<Configuration/Parameters/Flyway/Check/Fail On Drift>) exists for configuring this. You could use this in a pipeline and you want to take action based on the presence of drift.

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