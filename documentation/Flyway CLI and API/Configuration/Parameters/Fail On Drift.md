---
pill: failOnDrift
subtitle: flyway.failOnDrift
---
# Fail on Drift
{% include enterprise.html %}
## Description
Where drift has been detected as part of Flyway [check](Concepts/Check Concept), this can be used to cause Flyway to exit with an error (the return/exit code is not 0).
This would typically be used to stop a pipeline from proceeding to subsequent steps because the presence of drift suggests your DB has been changed outside of a flyway deployment pipeline and some action will need to be taken to bring the DB back to a managed state.

## Default
false

## Usage
```powershell
"./flyway check -drift -check.failOnDrift=true"
```
### Configuration File
```properties
flyway.check.failOnDrift=true
```