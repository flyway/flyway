---
pill: check.majorRules
subtitle: flyway.check.majorRules
---
# Check: Major Rules

{% include teams.html %}

{% include commandlineonly.html %}

## Description
You can configure your pipeline to fail when specified static code analysis rules beyond a given tolerance level are violated.

`majorRules` should contain a comma-separated list of [rules](Usage/Code Analysis Rules) which are considered to be major.

If the total number of `majorRules` violations exceeds the [majorTolerance](Configuration/Parameters/Major Tolerance), Flyway will fail.

## Default

No rules are regarded as major violations

## Usage

### Commandline
```powershell
./flyway check -code -check.majorRules=L001
```

### Configuration File
```properties
flyway.check.majorRules=L001
```