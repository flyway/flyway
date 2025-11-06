---
subtitle: code analysis rules
---

## Configuring Rules
These pages cover how to configure the various code analysis engines
- [Regex Rules](<Code Analysis Rules/Configuring Regex Rules>)
- [SQLFluff Rules](<Code Analysis Rules/Configuring SQLFluff Rules>)

The rules take one of three severity levels:
- Disabled - the rule will be ignored
- Warning - a rule violations will be reported but not cause the operation to fail
- Error - a rule violations will be reported and will cause the operation to fail if [`check.code.failOnError`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Fail On Error Setting>) is enabled

# Enforcing Code Analysis Policy
{% include enterprise.html %} 

Once you are satisfied that your code analysis rules are correctly configured you can enforce them in your project by setting [`check.code.failOnError`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Fail On Error Setting>) parameter to cause the `check -code` task to exit with a failure in case of any Error-level violations.

# Redgate Rules 
{% include enterprise.html %}

Redgate has added a set of rules that are of interest to customers with a larger and more complex database infrastructure. 

- [Redgate Regex Rules Library](<Code Analysis Rules/Redgate Regex Rules Library>)
- [Redgate SQLFluff Rules Library](<Code Analysis Rules/Redgate SQLFluff Rules Library>)
