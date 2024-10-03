---
subtitle: Diff Apply
---
# Diff Apply - Preview

{% include enterprise.html %}

This concept page assumes you understand the following area of Flyway:
 - [Diff](<Concepts/Diff concept>)

## Why is this useful ?
The `diffApply` command allows differences found by running the `diff` command to be applied to a target database environment or schema model.

## How is this used ?
To use `diffApply` a diff must first be performed, which will store the differences between the specified source and target in a `flyway.artifact.diff` file.
The differences in this file can then be applied to a specified target environment, which must be either `diff.source` or `diff.target`.

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

$ flyway diffApply -diffApply.target=schemaModel

Applied to schemaModel
 File updated: C:\Users\FlywayUser\Project\schema-model\sakila\Tables\inventory.rgm
```

 Note:
 - To apply only a subset of changes use the `diffApply.changes` option. e.g. `-diffApply.changes="APhfajbztVFslUjNVEexkWeTBvc"`.
 - The location of the diff artifact can be changed using the `diff.artifactFilename` and `diffApply.artifactFilename` options.
 - The inverse of the diff can be performed as well by specifying the `diff.source` as the `diffApply.target`. i.e. This will apply the inverse change to the `diff.source` environment.