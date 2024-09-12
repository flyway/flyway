---
subtitle: Snapshot
---
# Snapshot

{% include enterprise.html %}

A snapshot is the representation of a database schema at a moment in time stored in a single file format.
A snapshot file can be used with a variety of Flyway commands.
For example, a snapshot can be used as a comparison source or target when running the `diff` command.

**Note:** A snapshot is schema only, no data is included in the snapshot.

## Why is this useful ?
A snapshot can be used to capture the state of a database at a specific point in time.
This can be done using the [Snapshot](Commands/Snapshot) command.
A snapshot has the advantage that it can be captured remotely, without needing access to a shared filesystem with the database server.

## How is this used ?
A snapshot can be used with the `check` and `diff` commands.
It's also possible to use a snapshot to provision a database, see the [Snapshot Provisioner](Configuration/Provisioners/Snapshot Provisioner) for more information.

A snapshot can be generated using the `snapshot` command.
For example, the command below generates a snapshot of the `dev` environment:
```
$ flyway snapshot -snapshot.source=dev -snapshot.filename=C:\snapshot.json

Result of snapshot written to C:\snapshot.json
```

### Usage with `diff`
See [here](<Concepts/Diff concept>) for more information on the `diff` command.

A snapshot can be used as the `diff.source` or `diff.target` when running the `diff` command.
The example command below diffs a snapshot against a `prod` environment:
```
$ flyway diff -diff.source=snapshot:C:\snapshot.json -diff.target=prod

diff artifact generated: C:\Users\FlywayUser\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+--------------------+---------+-------------------------+
| Id                          | Change | Object Type        | Schema  | Name                    |
+-----------------------------+--------+--------------------+---------+-------------------------+
| YoZgVMdZR3p7FZEygVaRX9MoF2w | Add    | DDL trigger        |         | ddlDatabaseTriggerLog   |
| ZKZljmz2_Vvl5wLmV.mczvanHzM | Add    | Extended property  |         | MS_Description          |
| VYj3ZC0OtkZR4CbJ_JHm9BMkg_c | Add    | Full text catalog  |         | AW2016FullTextCatalog   |
| qlJstpTbyOQ7nRXTfUvY4lnUDIA | Add    | Function           | dbo     | ufnGetAccountingEndDate |
| JHc9rtmXwzfuqC7Ax4sw2tYE9Z0 | Add    | Function           | dbo     | ufnLeadingZeros         |
+-----------------------------+--------+--------------------+---------+-------------------------+
```

### Usage with `check`

A snapshot can be used as the `check.nextSnapshot` or `check.deployedSnapshot` options when running the `check` command.
Please see the following pages for more information on how snapshots can be used with the `check` command:

- [Check concept](<Concepts/Check concept>)
- [Check: Deployed Snapshot](Configuration/Parameters/Flyway/Check/Deployed Snapshot)
- [Check: Next Snapshot](Configuration/Parameters/Flyway/Check/Next Snapshot)