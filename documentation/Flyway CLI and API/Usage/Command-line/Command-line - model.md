---
pill: cli_model
subtitle: 'Command-line: model'
---
# Command-line: model - Preview

{% include enterprise.html %}

Applies the differences from a diff artifact, generated using the diff command, to the schema model.
The schema model must be one of the comparison sources used to create the diff artifact.

## Usage

<pre class="console"><span>&gt;</span> flyway model [options]</pre>

## Options

The following options can be provided to the `model` command in the format -key=value:
 - `model.changes` - A comma separated list of change ids. If unspecified, all changes will be used. May specify `-` to read the change ids from stdin.
 - `model.artifactFilename` - The location of the diff artifact to apply to the target. Defaults: `diff.artifactFilename` or `%temp%/flyway.artifact.diff`.

## Configuration

It is necessary to specify the location of the schema model folder, which can be done using the `schemaModelLocation` property.
It may be necessary to specify the schemas that should be compared as part of a diff operation, which can be done using the `schemaModelSchemas` property.
Example configuration that sets these properties is shown below:

```toml
[flyway]
schemaModelLocation = "./schema-model"
schemaModelSchemas = [ "sakila" ]
```

## Examples

### Applying to the schema model

The `diff` command must first be run to generate the diff artifact;
<pre class="console">&gt; flyway diff -diff.source=dev -diff.target=schemaModel</pre>

Then the `model` command is used to apply the changes to the schema model:
<pre class="console">&gt; flyway model -outputType=json

Flyway {{ site.flywayVersion }} by Redgate

{
  "messages" : [ ],
  "includedDependencies" : [ ],
  "filesChanged" : [ "C:\\Users\\Project\\schema-model\\MySchema\\Tables\\country.rgm" ]
}
</pre>

This workflow would typically happen before generating migration scripts - you may want to save the corresponding state of the database alongside the migration scripts, or use the schema model as the source to generate migration scripts from.
