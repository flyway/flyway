---
subtitle: snapshot
---

# Snapshot

{% include enterprise.html %}

{% include commandlineonly.html %}

This captures the schema of the specified database into a file for subsequent use with the [Check Command](Commands/Check Command).

This can be used to generate a snapshot of your database in its current state for use with [`check.deployedSnapshot`](Configuration/Parameters/Flyway/Check/Deployed Snapshot)
or to take a snapshot of a build database for use with [`check.nextSnapshot`](Configuration/Parameters/Flyway/Check/Next Snapshot)

#### Configuration parameters:
Required:
* Conventional Configuration
  * [url](Configuration/Parameters/Environments/url) / [user](Configuration/Parameters/Environments/user) / [password](Configuration/Parameters/Environments/password)
* _or_ 
  * [Environment](Configuration/Parameters/Flyway/environment) configuration
* [snapshot.filename](Configuration/Parameters/Flyway/Snapshot Filename) parameter


#### Usage example:

```
flyway snapshot -url=jdbc:example:database -user=username -password=password -snapshot.filename=C:\snapshots\my_snapshot
```

### `deployedSnapshot` and `nextSnapshot` example:

In order to generate these snapshots for use with [`check`](Commands/Check Command) we first need to get a list of the applied migrations
so we can accurately create the build database:

```
flyway info -url="jdbc://prod" -infoOfState="success,out_of_order,baseline" -migrationIds > applied_migrations.txt
```

Then we can apply these to our build database and take a snapshot, the `deployedSnapshot`:

```
flyway migrate -cherryPick=$(cat applied_migrations.txt) -url="jdbc://build"
flyway snapshot -snapshot.filename="deployed.snapshot" -url="jdbc://build"
```

Lastly, we can apply any pending migrations and then capture the `nextSnapshot`:

```
flyway migrate -url="jdbc://build"
flyway snapshot -snapshot.filename="next.snapshot" -url="jdbc://build"
```
#### Know limitations
##### Oracle
- If you don't specify the schemas to work with in your call to flyway you will get and error (`Expected database schemas option (schemas) to be provided`), the solution is to specify the [schemas](Configuration/Parameters/Environments/Schemas) you want to be included.
