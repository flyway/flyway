---
pill: pipeline_id
subtitle: flyway.pipeline_id
redirect_from: Configuration/pipeline_id/
---

# Pipeline Id

## Description

An id for identifying your pipeline in Flyway Pipelines. Used to tell Flyway which pipeline in Flyway Pipelines to send
data to. Pipeline Ids are copyable from within Flyway Pipelines for use as the value to this parameter. See the Flyway
Pipelines documentation [here](https://red-gate.com/flyway/pipelines/documentation).

**Note:** Flyway Pipelines is currently in
preview.

## Usage

### Commandline
```bash
./flyway -pipelineId="0E305365CCA981B68883AB6F81629B9D"
```

### TOML
```properties
[flyway]
pipelineId = "0E305365CCA981B68883AB6F81629B9D"
```

### Environment Variable
```properties
FLYWAY_PIPELINE_ID=0E305365CCA981B68883AB6F81629B9D
```
