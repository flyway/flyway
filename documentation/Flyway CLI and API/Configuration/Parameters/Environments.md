---
subtitle: placeholder page
---
# Environments

Environments are a set of associated properties used to connect to a database. These parameters all existed before but with the implicit assumption that Flyway would only communicate with one database. 

The concept of an environment has been created to allow Flyway to work easily with several different databases or configurations from a single configuration file.

## TOML Configuration
These parameters should be configured in an [environments](<configuration/Parameters/Environments>) namespace.

```powershell
[environments.example]
url = "jdbc:sqlite:local_database1.db"
username = "bob"
...
[environments.another_example]
url = "jdbc:sqlite:local_database2.db"
username = "jeff"
...
```
### Resolvers
Usually you wouldn't want to keep sensitive information in a plain text configuration file and so [resolvers](<Configuration/Property Resolvers>) can be used to pull information into your configuration from a variety of external sources like environment variables and secrets managers.

## Command line configuration
It is possible to define an environment on the commandline, for example:
```powershell
./flyway info -environments.example.url=jdbc:sqlite:local_database.db -environment=example
```
Note: You define an environment using the `environments` (plural) namespace but you specify which environment to use with the `environment` (singular) parameter. It's a bit of a trip hazard but it has some logical purpose to it.

## Legacy configuration
Properties that were originally part of the regular flyway configuration (`-url`, `-user`, `-password`) can still be used for backwards compatibility. Under the hood, Flyway will map these onto an environment named `default` for you and then use them like any other environment.

## Properties within an environment
The following are a list of items that can be configured for each environment.

<div id="children">
{% include childPages.html %}
</div>

