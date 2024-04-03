---
pill: check.code
subtitle: flyway.check.code
---
{% include commandlineonly.html %}

{% include redgate.html %}

## Description
`Check` with the `-code` flag produces a report showing the results of running static code analysis over your SQL migrations.

One of the analysis engines is SQLFluff, is an external dependency and installation instructions are covered in [Check Code Concept](<Concepts/Check Code Concept>)

## Usage

```powershell
./flyway check -code -environment=development
```
Typically you would have defined an [environment](Configuration/Parameters/Environments) for Flyway to work with.
Flyway can also operate without a defined [environment](Configuration/Parameters/Environments) but then _all_ migrations will be analysed. Otherwise only _pending_ migrations will be analysed

If you want to explicitly specify filename of the generated report then the [`reportFilename`](<Configuration/Parameters/Flyway/Report Filename>) parameter can be configured.

If you want to explicitly specify where Flyway should look for rules then the [`rulesLocation`](<Configuration/Parameters/Flyway/Check/Rules Location>) parameter can be configured. This would help if you have created a number of custom rules that exist outside of the Flyway installation and don't want to have to copy them back into Flyway when you update.

### Analysis engine: Regular Expressions (Regex)

{% include enterprise.html %}

Customers can easily craft their own custom rules or take advantage of the set of rules Flyway provides:
- [Creating Regular Expression Rules](<Learn More/Creating Regular Expression Rules>)
- [Code Analysis Rules](<Usage/Code Analysis Rules>)

### Feature:Failing on Rule Violations

{% include teams.html %}

You can configure your pipeline to fail when specified conditions are met.
This can be done by configuring the following parameters:

- [`check.majorRules`](<Configuration/Parameters/Flyway/Check/Major Rules>)
- [`check.minorRules`](<Configuration/Parameters/Flyway/Check/Minor Rules>)
- [`check.majorTolerance`](<Configuration/Parameters/Flyway/Check/Major Tolerance>)
- [`check.minorTolerance`](<Configuration/Parameters/Flyway/Check/Minor Tolerance>)


#### Example:

```powershell
./flyway check -code -environment=development '-check.majorTolerance=3' '-check.majorRules=L034,L042'
```

This will fail if rules `L034` and `L042` are violated 4 or more times in total across all scanned migration scripts.

## Notes
- Flyway makes use of any configured [`locations`](configuration/parameters/flyway/locations) to determine what migrations to analyse.
