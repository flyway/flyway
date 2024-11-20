---
pill: checkDriftOnMigrate
subtitle: flyway.checkDriftOnMigrate
redirect_from: Configuration/checkDriftOnMigrate/
---

# Check Drift On Migrate

{% include redgate.html %}

## Description

A boolean that enables or disables automatic drift checks on migrate. Requires an online authentication method to be
configured.
See [Flyway Licensing](<configuration/Flyway Licensing>) for more details. Also requires
[`publishResult`](<configuration/parameters/flyway/Publish Result>) to be set.

On migrate, a snapshot of your database schema is uploaded to [Flyway Pipelines](https://flyway.red-gate.com/).
Subsequent runs of migrate will compare the latest snapshot in Flyway Pipelines to your deployment target before script
execution. If your deployment target is different from the schema snapshot in Flyway Pipelines, a drift alert will be
issued to Flyway Pipelines.

Check drift on migrate is an alternative to the [Check Drift](<Concepts/Check Drift concept>) command.

## Usage

### Commandline

```bash
./flyway migrate -email="foo.bar@red-gate.com" -token="1234ABCD" -publishResult=true -checkDriftOnMigrate=true
```

### TOML

```properties
[flyway]
email="foo.bar@red-gate.com"
token="1234ABCD"
publishResult=true
checkDriftOnMigrate=true
```

### Environment Variable

```properties
FLYWAY_EMAIL=foo.bar@red-gate.com
FLYWAY_TOKEN=1234ABCD
FLYWAY_PUBLISH_RESULT=true
FLYWAY_CHECK_DRIFT_ON_MIGRATE=true
```
