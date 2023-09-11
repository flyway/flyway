---
pill: check.minorRules
subtitle: flyway.check.minorRules
---
# Check: Minor Rules

{% include teams.html %}

{% include commandlineonly.html %}

## Description
You can configure your pipeline to fail when specified static code analysis rules beyond a given tolerance level are violated.

`minorRules` should contain a comma-separated list of [rules](Usage/Code Analysis Rules) which are considered to be minor.

If the total number of `minorRules` violations exceeds the [minorTolerance](Configuration/Parameters/Minor Tolerance), Flyway will fail.

## Default

No rules are regarded as minor violations

## Usage

### Commandline
```powershell
./flyway check -code -check.minorRules=L002
```

### Configuration File
```properties
flyway.check.minorRules=L002
```