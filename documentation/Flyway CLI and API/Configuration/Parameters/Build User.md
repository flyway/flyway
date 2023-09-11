---
pill: check.buildUser
subtitle: flyway.check.buildUser
---
# Check: Build User

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description
Username for the build database
See [Check Concept](Concepts/Check Concept) for more information on how to configure the changes & drift reports

## Default

Whatever is set as your 'flyway.user'

## Usage

### Commandline
```powershell
./flyway check -changes -url="jdbc://url1" -check.buildUser="sa"
```

### Configuration File
```properties
flyway.check.buildUser="sa"
```
