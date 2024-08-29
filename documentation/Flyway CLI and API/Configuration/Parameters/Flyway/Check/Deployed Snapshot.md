---
pill: check.deployedSnapshot
subtitle: flyway.check.deployedSnapshot
---
# Check: Deployed Snapshot

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description
A snapshot containing all applied migrations and thus matching what should be in the target (generated via [`snapshot`](Commands/snapshot))
See [Check Concept](Concepts/Check Concept) for more information on how to configure the changes & drift reports

## Default

None

## Usage

### Commandline
```powershell
./flyway check -changes -url="jdbc://url" -check.deployedSnapshot="my_snapshot"
```

### TOML Configuration File
```toml
[flyway.check]
deployedSnapshot = "my_snapshot"
```


### Configuration File
```properties
flyway.check.deployedSnapshot=my_snapshot
```