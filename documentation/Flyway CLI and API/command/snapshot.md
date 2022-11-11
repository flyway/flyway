---
layout: documentation
menu: snapshot
subtitle: snapshot
---

# Snapshot

**The `snapshot` command is currently in beta. This feature will be available in future products, but during the beta phase you can access it through your Flyway Teams or Redgate Deploy license.**

{% include enterprise.html %}

`snapshot` captures the schema of the database specified in `flyway.url` into a file.

This can be used to generate a snapshot of your database in its current state for use with [`check.deployedSnapshot`](/documentation/command/check#configuration-parameters)
or to take a snapshot of a build database for use with [`check.nextSnapshot`](/documentation/command/check#configuration-parameters)

#### Requirements
- .NET 6 is required in order to generate reports. You can download it from [here](https://dotnet.microsoft.com/en-us/download/dotnet/6.0).

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
In order to generate these snapshots for use with [`check`](/documentation/command/check) we first need to get a list of the applied migrations
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
