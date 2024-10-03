---
subtitle: Diff concept
---

# Diff - Preview

{% include enterprise.html %}

## Why is this useful ?

The `diff` command calculates the differences between a configured source and target. This command can be used with the `generate`, `diffApply` and `diffText` commands to generate scripts and apply changes.

## How is this used ?

The `diff` command allows the flexibility of comparison between different types of sources:
 - Environment
 - Schema model
 - Build environment (The schema represented by your current migrations)
 - Snapshot
 - Empty

The target and the source must be provided in order to calculate the differences. The same source and target can be used in subsequent commands, where we could generate migration scripts or apply the differences. Additionally, the source and target must be same database type - it is not possible to compare a Sql Server database to an Oracle snapshot.

For example: Let's say you have some changes in your development database (called `dev` in this example). You would like to save these changes into your schema model for your project, the first step would be to perform diff to infer the differences between the two sources:

```
$ flyway diff -diff.source=dev -diff.target=schemaModel

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

The object level differences indicated as an ADD type are objects that were found only in the development database whereas the
DELETE type changes are objects found only in the schema model. Following this command you can perform a `diffApply`
command to apply the changes listed above to the schema model as below.

```
$ flyway diffApply

Flyway {{ site.flywayVersion }} by Redgate

Applied to schemaModel
```

Note that by default commands such as `diffApply` and `generate` will act as if they are moving changes to the `source` to the `target` specified by diff. You can move changes in the opposite direction by specifying what the target should be e.g. `diffApply -diffApply.target=<<source-here>>`.

Once the changes are applied as above upon re-running the `diff` command in the first step there would no longer be any
changes between the schema model folder and the dev environment as below.

```
$ flyway diff -diff.source=dev -diff.target=schemaModel
diff artifact generated: C:\Users\Projects\diffArtifacts\artifact
+-----------------------------+--------+-------------+--------+------+
| Id                          | Change | Object Type | Schema | Name |
+-----------------------------+--------+-------------+--------+------+
| No differences found                                               |
+-----------------------------+--------+-------------+--------+------+
```

## Difference artifacts

The diff command stores the calculated changes in a differences artifact which is read by subsequent commands such as `generate` and `diffApply`. By default, this is stored in a temporary location and overwritten after each run of `diff`, but you can specify where `diff` and the subsequent commands should look write & read this file from. This might be useful if you need to combine changes from more than one source.

The changes captured in a difference artifact are always the changes that were present at the time `diff` was run. If the source or target environments have changed in between calls to, for example, `diff` and `generate` then the generated script will not pick up these changes. As a result, it is intended that difference artifacts are short-lived. 

Difference artifacts will remain compatible across patch versions of flyway. However, flyway may add comparison features in minor version changes that mean there is no guarantee that difference artifacts can be fully compatible across these changes. It is strongly recommended that difference artifacts are not held for a long enough timescale for this to be a concern. If you want to keep a copy of the schema in an environment (or pair of environments) for later, consider using `flyway snapshot` instead.

## Flyway objects
When saving the schema of a database to a schema model, you may want to filter out flyway objects such as the schema history table. By default, `flyway diff` will filter these objects out from all comparisons. If you want to include this objects in the differences you will need to set `-diff.includeFlywayObjects=true`.