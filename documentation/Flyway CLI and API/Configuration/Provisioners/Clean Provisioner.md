---
subtitle: Clean Provisioner
---
# Clean Provisioner
{% include enterprise.html %}

This [provisioner](Configuration/Provisioners) allows for reprovisioning of databases using [clean](Commands/Clean).
It is used as the default mechanism in Flyway Desktop for resetting the [shadow database](https://documentation.red-gate.com/flyway/flyway-desktop/terminology-reference/shadow-database-or-shadow-schema) whenever the state is stale.

This affects reprovisioning only, and has no effect on provisioning. It can only be used with pre-existing databases.

To configure this provisioner, set the value of the [provisioner parameter](Configuration/Parameters/Environments/Provisioner) to `clean`

## Example
This can be used in the TOML configuration like this:
```toml
[environments.shadow]
url = "jdbc:sqlserver://localhost:1433;database=Shadow;encrypt=true;integratedSecurity=true"
provisioner = "clean"
```
