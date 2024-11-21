---
subtitle: Model
---
# Model

{% include enterprise.html %}

This concept page assumes you understand the following area of Flyway:
 - [Diff](<Concepts/Diff concept>)

## Why is this useful ?
The `model` command allows differences found by running the `diff` command to be applied to the schema model.

## How is this used ?
To use `model` a diff must first be performed, which will store the differences between the specified source and target in a `flyway.artifact.diff` file.
The differences in this file can then be applied to the schema model, which must be either `diff.source` or `diff.target`.

For example, the following commands generate a diff between a dev environment and the schema model folder.
All of the differences are then applied to the schema model folder.
```
$ flyway diff -diff.source=dev -diff.target=schemaModel

diff artifact generated: C:\Users\FlywayUser\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+-------------+--------+-----------+
| Id                          | Change | Object Type | Schema | Name      |
+-----------------------------+--------+-------------+--------+-----------+
| APhfajbztVFslUjNVEexkWeTBvc | Edit   | Table       | sakila | inventory |
+-----------------------------+--------+-------------+--------+-----------+

$ flyway model

Applied to schemaModel
 File updated: C:\Users\FlywayUser\Project\schema-model\sakila\Tables\inventory.rgm
```

 Note:
 - To apply only a subset of changes use the `model.changes` option. e.g. `-model.changes="APhfajbztVFslUjNVEexkWeTBvc"`.
 - The location of the diff artifact can be changed using the `diff.artifactFilename` and `model.artifactFilename` options.
 - The schema model can be used as either a source or target with the `diff` command, as a subsequent `model` command will update the schema model to be equal to the other comparison source in either case. For example, the following command `flyway diff model -diff.source=schemaModel -diff.target=dev` and its inverse `flyway diff model -diff.source=dev -diff.target=schemaModel` are equivalent and have the same result of updating the schema model to be equal to the dev environment.

## Further Reading
See [here](<Usage/Command-line/Command-line - model>) for more information on how to use the `model` command.