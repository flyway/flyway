---
subtitle: Diff concept
---

# Diff

{% include enterprise.html %}

## Why is this useful ?

The `diff` command calculates the differences between a configured source and target. This command creates a diff
artifact which can be used to execute the `generate`, `diffApply` and `diffText` commands.

## How is this used ?

The `diff` command allows the flexibility of comparison between different types of sources:
 - Environment
 - Build environment
 - Schema model
 - Snapshot
 - Empty type. 
The target and the source should be configured in order to calculate the differences from which we can either generate migration scripts or apply the differences on the
target specified which created the diff.

Note:
The two comparison sources have to be of the same database type to perform `diff`.

For example: In the scenario where the changes in the schema model folder need to be deployed to the dev
environment, the first step would be to perform diff to infer the differences between the two sources.
The command below would compare the schema model folder against the prod environment and generate a diff artifact.

```
$ flyway diff -diff.source=schemaModel -diff.target=dev

diff artifact generated: C:\Users\Projects\diffArtifacts\artifact
+-----------------------------+--------+-------------+----------+----------+
| Id                          | Change | Object Type | Schema   | Name     |
+-----------------------------+--------+-------------+----------+----------+
| 0Vq7ldnZ06ES23tylJKD2KHrM5M | Add    | Table       | dbo      | Table_1  |
| fvGooqeU94wq6HTCoAUlwvkd_bc | Add    | Table       | dbo      | Table_2  |
| a.o0T8ULN8u.YVZIz6UVT7dbhC0 | Edit   | Table       | dbo      | Table_3  |
| 1gvOcO43loujJCPktuUkhBMFbSI | Delete | Table       | dbo      | Table_4  |
+-----------------------------+--------+-------------+----------+----------+
```

The object level differences indicated as an ADD type are objects that were found only in the schema model folder whereas the
DELETE type changes are objects found only in the dev environment. Following this command you can perform a `diffApply`
command to apply the changes listed above to the dev environment as below.

```
$ flyway diffApply -diffApply.target=dev

Flyway {{ site.flywayVersion }} by Redgate

Applied to dev
```

Once the changes are applied as above upon re-running the `diff` command in the first step there would no longer be any
changes between the schema model folder and the dev environment as below.

```
$ flyway diff -diff.source=schemaModel -diff.target=dev
diff artifact generated: C:\Users\Projects\diffArtifacts\artifact
+-----------------------------+--------+-------------+--------+------+
| Id                          | Change | Object Type | Schema | Name |
+-----------------------------+--------+-------------+--------+------+
| No differences found                                               |
+-----------------------------+--------+-------------+--------+------+
```