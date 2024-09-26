---
pill: cli_diffApply
subtitle: 'Command-line: diffApply'
---
# Command-line: diffApply

{% include enterprise.html %}

Applies the differences from a diff artifact, generated using the diff command, to a target.
The target may be either a database environment or the schema model folder.
The target must be one of the comparison sources used to create the diff artifact.

<img src="assets/command-diffApply.png" alt="diffApply">

## Usage

<pre class="console"><span>&gt;</span> flyway diffApply [options]</pre>

## Options

The following options can be provided to the `diffApply` command in the format -key=value:
 - `diffApply.target` - The target to apply the changes to, must be either `diff.source` or `diff.target` used by flyway diff. `diff.target` is used if not specified.
 - `diffApply.changes` - A comma separated list of change ids. If unspecified, all changes will be used. May specify `-` to read the change ids from stdin.
 - `diffApply.artifactFilename` - The location of the diff artifact to apply to the target. Defaults: `diff.artifactFilename` or `%temp%/flyway.artifact.diff`.

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

Then the `diffApply` command can be used to apply the changes to the schema model:
<pre class="console">&gt; flyway diffApply -diffApply.target=schemaModel -outputType=json

Flyway {{ site.flywayVersion }} by Redgate

{
  "messages" : [ ],
  "includedDependencies" : [ ],
  "filesChanged" : [ "C:\\Users\\Project\\schema-model\\MySchema\\Tables\\country.rgm" ]
}
</pre>

This workflow would typically happen before generating migration scripts - you may want to save the corresponding state of the database alongside the migration scripts, or use the schema model as the source to generate migration scripts from.

### Applying to a database environment

The `diff` command must first be run to generate the diff artifact;
<pre class="console">&gt; flyway diff -diff.source=schemaModel -diff.target=dev</pre>

Then the `diffApply` command can be used to apply specific changes to the `dev` environment:
<pre class="console">&gt; diffApply -diffApply.target=dev -diffApply.changes="LUl4TQxeClaiCgTdbkigq_tiRIs,O7mO.zpBl0kXLXWbnCKZOt6NP1k"

Flyway {{ site.flywayVersion }} by Redgate

Applied to dev
</pre>

This workflow may be useful if you have pulled schema model changes from your version control system and want to apply them back to your development environment.
