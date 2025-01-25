---
pill: check.majorTolerance
subtitle: flyway.check.majorTolerance
---
# Check: Major Tolerance

{% include teams.html %}

{% include commandlineonly.html %}

## Description
You can configure your pipeline to fail when specified static code analysis rules beyond a given tolerance level are violated.

`majorTolerance` sets the number of major rules violations to be tolerated before throwing an error

If the total number of [majorRules](Configuration/Parameters/Flyway/Check/Major Rules) violations exceeds the `majorTolerance`, Flyway will fail.

## Default

There is no maximum tolerance (i.e. violations will not cause a failure)

## Usage

### Commandline
```powershell
./flyway check -code -check.majorTolerance=3
```

### TOML Configuration File
```toml
[flyway.check]
majorTolerance = 3
```

### Configuration File
```properties
flyway.check.majorTolerance=3
```
