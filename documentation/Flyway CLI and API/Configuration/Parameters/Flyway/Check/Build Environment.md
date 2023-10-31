---
pill: check.buildUser
subtitle: flyway.check.buildUser
---
# Check: Build Environment

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description
[Environment](Configuration/Parameters/Flyway/Environment) for the build database
See [Check Concept](Concepts/Check Concept) for more information on how to configure the changes & drift reports

## Default
default_build

## Usage

### Commandline
```powershell
./flyway check -changes -environment=env1 -check.buildEnvironment=build1
```

### TOML Configuration File
```toml
[flyway.check]
buildEnvironment = "build1"
```

### Configuration File
```properties
flyway.check.buildEnvironment="build1"
```
