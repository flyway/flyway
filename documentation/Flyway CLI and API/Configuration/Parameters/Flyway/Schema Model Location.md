---
pill: schemaModelLocation
subtitle: flyway.schemaModelLocation
redirect_from: Configuration/schemaModelLocation/
---

# Schema Model Location

## Description
The location of the schema model directory.
If not set then this defaults to the folder "schema-model" in the current working directory.

It may be necessary to specify this option if using `schemaModel` as a comparison source or target with the
`diff` command. It may also be necessary specify this option if using `schemaModel` as a target for the `diffApply`
command.

## Usage

### Commandline
```bash
./flyway -schemaModelLocation="C:\Users\FlywayUser\Project\schema-model"
```

### TOML
```properties
[flyway]
schemaModelLocation = 'C:\Users\FlywayUser\Project\schema-model'
```
