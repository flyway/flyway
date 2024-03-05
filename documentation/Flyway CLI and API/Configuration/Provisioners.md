---
subtitle: Provisioners
---
# Provisioners
{% include enterprise.html %}

Provisioners allow flyway to provision a database which does not exist yet before connecting to it, or potentially to re-provision an existing database, restoring the database back to a known state.

Flyway comes with support for the following provisioners:
- [Clean](Configuration/Provisioners/Clean Provisioner)
- [Docker - Preview](Configuration/Provisioners/Docker Provisioner)
- [Redgate Clone - Preview](Configuration/Provisioners/Redgate Clone Provisioner)

## Using Provisioners In TOML Configuration Files
Provisioners are simply configured by setting the [provisioner](Configuration/Parameters/Environments/Provisioner) property on a configured environment
Some provisioners double as [property resolvers](Configuration/Property Resolvers) and allow properties to be computed

```toml
[environments.development]
url = "${clone.url}databaseName=my-database"
provisioner = "clone"

[environments.development.resolvers.clone]
url = "https://clone.red-gate.com:1234/cloning-api"
dataImage = "mssql-empty"
dataContainer = "MyContainer"
dataContainerLifetime = "1h"
authenticationToken = "${localSecret.RedgateCloneToken}"
```

## Provisioning
Provisioning happens automatically before a connection to a database via any flyway verb.
If the database already exists, then no provisioning is needed, though the technology used for provisioning may supply the JDBC URL needed to connect to the database.
If the database does not yet exist, then it will be provisioned. This could be the creation of an empty database, or it could be restored to a populated state, such as making a development database match a snapshot of production (at least in terms of schema).

Common use cases of provisioning are:
* Spinning up a test database, for example to test the deployment of migrations
* Restoring a development database from a clone or snapshot of production 
* Spinning up a development database with appropriate state on a git branch switch

Note that not all provisioner types will support provisioning. The [clean provisioner](Configuration/Provisioners/Clean Provisioner) is a method for re-provisioning only.

## Re-provisioning
Re-provisioning consists of resetting a database to a known state. This could be cleaning it and leaving it empty or resetting it to a populated state, such as a snapshot of production.

Re-provisioning is currently only triggered programmatically or via Flyway Desktop. In Flyway Desktop it is triggered for the [shadow database](https://documentation.red-gate.com/flyway/flyway-desktop/terminology-reference/shadow-database-or-shadow-schema) whenever the state is stale and needs to be reset.
In this context, if it is used to reset to a baseline state, then it avoids the need for generating and executing a baseline migration script, which can be tricky with databases which have cross-database dependencies, or are very large. 
