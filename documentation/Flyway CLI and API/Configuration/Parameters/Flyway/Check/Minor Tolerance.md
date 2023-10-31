---
pill: check.minorTolerance
subtitle: flyway.check.minorTolerance
---
# Check: Minor Tolerance

{% include teams.html %}

{% include commandlineonly.html %}

## Description
You can configure your pipeline to fail when specified static code analysis rules beyond a given tolerance level are violated.

`minorTolerance` sets the The number of minor rules violations to be tolerated before throwing an error

If the total number of [minorRules](Configuration/Parameters/Flyway/Check/Minor Rules) violations exceeds the `minorTolerance`, Flyway will fail.

## Default

There is no maximum tolerance (i.e. violations will not cause a failure)

## Usage

### Commandline
```powershell
./flyway check -code -check.minorTolerance=7
```

### TOML Configuration File
```toml
[flyway.check]
minorTolerance = 7
```

### Configuration File
```properties
flyway.check.minorTolerance=7
```
