---
pill: publish_result
subtitle: flyway.publish_result
redirect_from: Configuration/publish_result/
---

# Publish Result

## Description

A boolean value that tells Flyway whether or not to publish the result of your Flyway run to Flyway Pipelines. Set
to false by default. See the Flyway Pipelines documentation [here](https://red-gate.com/flyway/pipelines/documentation).

**Note:** Flyway Pipelines is currently in
preview.

## Usage

### Commandline
```bash
./flyway -publishResult=true
```

### TOML
```properties
[flyway]
publishResult = true
```

### Environment Variable
```properties
FLYWAY_PUBLISH_RESULT=true
```
