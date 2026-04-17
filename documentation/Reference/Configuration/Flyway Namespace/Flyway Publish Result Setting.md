---
subtitle: flyway.publish_result
redirect_from: Configuration/publish_result/
---

{% include redgate.html %}

{% include preview.html %}

## Description

Whether to publish the result of your Flyway run to Flyway Pipelines.
See the Flyway Pipelines documentation [here](https://red-gate.com/flyway/pipelines/documentation).

**Note:** Flyway Pipelines is currently in preview.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

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
