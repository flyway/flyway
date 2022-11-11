---
layout: documentation
menu: configuration
pill: edition
subtitle: flyway.edition
redirect_from: /documentation/configuration/edition/
---

# Edition
{% include teams.html %}

## Description
Forces Flyway to use either Teams or Community edition. Should be either `-teams` or `-community` for Teams or Community editions respectively.

This config parameter only applies to the Command-Line version of Flyway. To change the edition of the Gradle or Maven plugins, simply change the dependency to the teams edition (e.g. `compile "org.flywaydb:flyway-core:{{ site.flywayVersion }}"` -> `compile "org.flywaydb.enterprise:flyway-core:{{ site.flywayVersion }}"`).

If omitted, Flyway Command-Line will ascertain the correct version to run based on the presence of a license key and the entitlement to a Teams trial if available.

## Usage

### Commandline
```powershell
./flyway -teams info
```

### Configuration File
Not available

### Environment Variable
```properties
FLYWAY_EDITION=teams
```

### API
See [upgrading api to Teams](/documentation/upgradingToTeams#api)

### Gradle
See [upgrading gradle to Teams](/documentation/upgradingToTeams#gradle)

### Maven
See [upgrading maven to Teams](/documentation/upgradingToTeams#maven)