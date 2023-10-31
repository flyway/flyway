---
subtitle: snapshot
---

# Snapshot

{% include enterprise.html %}

`snapshot` captures the schema of the database specified in `flyway.url` into a file.

This can be used to generate a snapshot of your database in its current state for use with [`check.deployedSnapshot`](Configuration/Parameters/Flyway/Check/Deployed Snapshot)
or to take a snapshot of a build database for use with [`check.nextSnapshot`](Configuration/Parameters/Flyway/Check/Next Snapshot)

#### Configuration parameters:

_Format: -key=value_

| Parameter                    | Description
| ---------------------------- | -----------------------------------------------------------
|    snapshot.filename         | **[REQUIRED]** Destination filename for the snapshot

#### Usage example:

```
flyway snapshot -url=jdbc:example:database -user=username -password=password -snapshot.filename=C:\snapshots\my_snapshot
```

### `deployedSnapshot` and `nextSnapshot` example:

In order to generate these snapshots for use with [`check`](Commands/check) we first need to get a list of the applied migrations
so we can accurately create the build database:

```
flyway info -url="jdbc://prod" -infoOfState="success,out_of_order,baseline" -migrationIds > applied_migrations.txt
```

Then we can apply these to our build database and take a snapshot, the `deployedSnapshot`:

```
flyway migrate -cherrypick=$(cat applied_migrations.txt) -url="jdbc://build"
flyway snapshot -snapshot.filename="deployed.snapshot" -url="jdbc://build"
```

Lastly, we can apply any pending migrations and then capture the `nextSnapshot`:

```
flyway migrate -url="jdbc://build"
flyway snapshot -snapshot.filename="next.snapshot" -url="jdbc://build"
```

##### Example configuration file

```properties
flyway.url=jdbc:example:database
flyway.user=username
flyway.password=password
flyway.snapshot.filename=C:\snapshots\my_snapshot
```
