---
pill: check.nextSnapshot
subtitle: flyway.check.nextSnapshot
---
# Check: Next Snapshot

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description
A snapshot containing all migrations including those that are pending (generated via [`snapshot`](Commands/snapshot))
See [Check Concept](Concepts/Check Concept) for more information on how to configure the changes & drift reports

## Default

None

## Usage

### Commandline
```powershell
./flyway check -changes -url="jdbc://url" -check.nextSnapshot="my_snapshot"
```

### Configuration File
```properties
flyway.check.nextSnapshot=my_snapshot
```