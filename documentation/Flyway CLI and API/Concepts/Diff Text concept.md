---
subtitle: Diff Text concept
---
# Diff Text

{% include enterprise.html %}

This concept page assumes you understand the following area of Flyway:
 - [Diff](<Concepts/Diff concept>)

If you have not done so, please review this page first.

## Why is this useful ?
The `diffText` commands prints out the object level differences from the artifact generated upon running `diff`. The `diffText` command reads the differences between the source and target environments which is stored in a non human-readable format in the diff artifact file.

## How is this used ?
To perform `diffText` a `diff` operation must first be performed. You may either configure the toml or use the command line as shown below.

Toml configuration:
```
[flyway.diffText]
artifactFilename = "./diffArtifacts/diffArtifact"
changes = ["id1","id2"]
```

CLI configuration:
```
flyway diffText -diffText.artifactFilename="./diffArtifact" -flyway.diffText.changes=["id1", "id2"]
```

To perform `diffText`, you need to provide the path to a valid diff artifact file and the list of changes you want to see the differences for.

### Note
- If the diff artifact provided is altered by any means which would result in the commandline being unable to read the artifact, then the `diffText` command will fail.
- The location of the diff artifact can be changed using the `diff.artifactFilename` and `diffText.artifactFilename` options.
- If changes are not specified then all changes will be used.