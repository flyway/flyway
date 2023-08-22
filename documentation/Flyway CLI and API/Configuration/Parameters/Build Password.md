---
pill: check.buildPassword
subtitle: flyway.check.buildPassword
---
# Check: Build Password

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description
Password for the build database.
See [Check Concept](Concepts/Check Concept) for more information on how to configure the changes & drift reports

## Default

Whatever is set as your 'flyway.password' (see [password](Configuration/parameters/password) )

## Usage

### Commandline
```powershell
./flyway check -changes -url="jdbc://url1" -check.buildPassword="mypassword"
```

### Configuration File
```properties
flyway.check.buildPassword="mypassword"
```
