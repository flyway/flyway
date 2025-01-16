---
subtitle: Model
---

## Description

The `model` command allows differences found by running the diff command to be applied to the schema model.
The schema model must be one of the comparison sources used to create the diff artifact.

See [Schema Model](https://documentation.red-gate.com/display/FD/Schema+Model) for more information.

## Usage examples

### Applying to the schema model

```bash
flyway diff -source=dev -target=schemaModel
flyway model -outputType=json
```

## Parameters

### Optional

| Parameter                                                                                                                       | Namespace | Description                           |
|---------------------------------------------------------------------------------------------------------------------------------|-----------|---------------------------------------|
| [`artifactFilename`](<Configuration/Flyway Namespace/Flyway Model Namespace/Flyway Model Artifact Filename Setting>) | model     | The path to the diff artifact.        |
| [`changes`](<Configuration/Flyway Namespace/Flyway Model Namespace/Flyway Model Changes Setting>)                    | model     | A comma separated list of change ids. |

Universal commandline parameters are listed [here](<Command-line Parameters>).

## JSON output format

```json
{
  "messages" : [ ],
  "includedDependencies" : [ ],
  "filesChanged" : [ "C:\\Users\\Project\\schema-model\\MySchema\\Tables\\country.rgm" ]
}
```