---
subtitle: Clean Provisioner
---

This [provisioner](https://documentation.red-gate.com/flyway/flyway-concepts/environments/provisioning) allows for re-provisioning of databases using [clean](Commands/Clean).
It is used as the default mechanism in Flyway Desktop for resetting the [shadow database](https://documentation.red-gate.com/flyway/flyway-concepts/environments-todo/shadow-and-build-environments) whenever the state is stale.

This affects re-provisioning only, and has no effect on provisioning. It can only be used with pre-existing databases.

To configure this provisioner, set the value of the [provisioner parameter](<Configuration/Environments Namespace/Environment Provisioner Setting>) to `clean`

## Example
This can be used in the TOML configuration like this:
```toml
[environments.shadow]
url = "jdbc:sqlserver://localhost:1433;database=Shadow;encrypt=true;integratedSecurity=true"
provisioner = "clean"
```
