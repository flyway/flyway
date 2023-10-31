---
pill: check.buildUrl
subtitle: flyway.check.buildUrl
---
# Check: Build Url

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description
URL for a build database.
See [Check Concept](Concepts/Check Concept) for more information on how to configure the changes & drift reports

## Default

None

## Usage

### Commandline
```powershell
./flyway check -changes -url="jdbc://url1" -check.buildUrl="jdbc://url2"
```

### TOML Configuration File
```toml
[flyway.check]
buildUrl = "jdbc://url2"
```

### Configuration File
```properties
flyway.check.buildUrl="jdbc://url2"
```