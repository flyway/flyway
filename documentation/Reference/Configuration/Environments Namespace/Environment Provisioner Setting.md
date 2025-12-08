---
subtitle: flyway.environments.*.provisioner
---

{% include enterprise.html %}

## Description

Provisioners allow flyway to provision a database which does not exist yet before connecting to it, or potentially to re-provision an existing database, restoring the database back to a known state.

Flyway comes with support for the following provisioners:

<div id="children">
{% include childPages.html %}
</div>

Some provisioners double as [Resolvers](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers) and allow properties to be computed.

### Provisioning

Provisioning happens automatically before a connection to a database via any flyway verb.
If the database already exists, then no provisioning is needed, though the technology used for provisioning may supply the JDBC URL needed to connect to the database.
If the database does not yet exist, then it will be provisioned. This could be the creation of an empty database, or it could be restored to a populated state, such as making a development database match a snapshot of production (at least in terms of schema).

Common use cases of provisioning are:

* Spinning up a test database, for example to test the deployment of migrations
* Restoring a development database from a clone or snapshot of production
* Spinning up a development database with appropriate state on a git branch switch

Note that not all provisioner types will support provisioning. The [clean provisioner](Configuration/Environment Provisioner Setting/Clean Provisioner) is a method for re-provisioning only.

### Re-provisioning

Re-provisioning resets a database as if it were newly provisioned. This is a destructive operation, which could involve cleaning it and leaving it empty or resetting it to a populated state. Re-provisioning can only be performed on environments that specify a provisioner which supports it. For example, an environment using the `none` provisioner (the default value if a provisioner is not set) will raise an error if a re-provision is attempted.

Re-provisioning is typically triggered by Flyway Desktop or on environments specified as a `-buildEnvironment` to commands such as [Diff](<Commands/Diff>) or [Check](<Commands/Check>). In Flyway Desktop it is triggered for the [shadow database](https://documentation.red-gate.com/display/FD/Shadow+and+build+environments) whenever the state is stale and needs to be reset.
In this context, if it is used to reset to a baseline state, then it avoids the need for generating and executing a baseline migration script, which can be tricky with databases which have cross-database dependencies, or are very large.

Re-provisioning may be manually requested by using the [`provisionMode`](<Configuration/Flyway Namespace/Flyway Provision Mode Setting>) parameter on the command line.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog.
Note that Flyway Desktop implicitly sets the Shadow Database to use the  `clean` provisioner.

### Command-line

```powershell
./flyway -environments.sample.provisioner=clone info
```

### TOML Configuration File

```toml
[environments.development]
provisioner = "clone"
```