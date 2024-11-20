---
subtitle: Schema Model
---
# Schema Model

A schema model is a representation of a database in a file-based format, much like a snapshot. However, unlike a snapshot, each database object is stored in a separate file containing a human readable representation of the object. This makes it possible to store the schema of your database as it looks right now in a version control system alongside your migrations. This can be useful to track how individual objects have evolved over time.

## Why is this useful ?
Multiple developers can update the schema model with their changes using a version control system.
The schema model can then later be diffed with a database, and the changes applied to either the database or schema model.
This allows for a more controlled and collaborative approach to database development.

## How is this used ?
The schema model is primarily used with the `diff` and `model` commands.
In both cases the following properties should be set when using a schema model:

 - [schemaModelLocation](<Configuration/Parameters/Flyway/Schema Model Location>) - The location of the schema model folder on disk.
 - [schemaModelSchemas](<Configuration/Parameters/Flyway/Schema Model Schemas>) - A list of schemas contained in the schema model that should be considered when performing a diff. This is schema model's equivalent of the `flyway.schemas` property.

These two properties can be set in the toml configuration file as shown below:
```toml
[flyway]
schemaModelLocation = "./schema-model"
schemaModelSchemas = [ "SchemaName1", "SchemaName2" ]
```

Below is an example of the directory structure of a schema model folder for a MySQL database with a `sakila` schema:
```
$ tree schema-model
schema-model
├── model.json
└── sakila
    ├── Functions
    │ ├── get_customer_balance.rgm
    │ ├── inventory_held_by_customer.rgm
    │ └── inventory_in_stock.rgm
    ├── Procedures
    │ ├── film_in_stock.rgm
    │ ├── film_not_in_stock.rgm
    │ └── rewards_report.rgm
    ├── Tables
    │ ├── NewTable.rgm
    │ ├── actor.rgm
    │ ├── address.rgm
    │ ├── category.rgm
    │ ├── city.rgm
    │ ├── country.rgm
    │ ├── customer.rgm
    │ ├── film.rgm
    │ ├── film_actor.rgm
    │ ├── film_category.rgm
    │ ├── film_text.rgm
    │ ├── inventory.rgm
    │ ├── language.rgm
    │ ├── payment.rgm
    │ ├── rental.rgm
    │ ├── staff.rgm
    │ └── store.rgm
    ├── Views
    │ ├── TestView.rgm
    │ ├── actor_info.rgm
    │ ├── customer_list.rgm
    │ ├── film_list.rgm
    │ ├── nicer_but_slower_film_list.rgm
    │ ├── sales_by_film_category.rgm
    │ ├── sales_by_store.rgm
    │ └── staff_list.rgm
    └── sakila.rgm

5 directories, 33 files
```

### Usage with `diff`
See [here](<Concepts/Diff concept>) for more information on the `diff` command.

A schema model can be used as the `diff.source` or `diff.target` when running the `diff` command.
This allows the differences between the schema model and an alternative target to be calculated.

For example, the following command generates a diff between the schema model folder and a dev environment.
```
$ flyway diff -diff.source=schemaModel -diff.target=dev

diff artifact generated: C:\Users\FlywayUser\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+-------------+--------+------+
| Id                          | Change | Object Type | Schema | Name |
+-----------------------------+--------+-------------+--------+------+
| SaBpajbztVFslUjNVEexkWeTBvc | Edit   | Table       | sakila | film |
+-----------------------------+--------+-------------+--------+------+
```

These differences can then be used with the `model` command which is discussed below.

### Usage with `model`
See [here](<Concepts/Model concept>) for more information on the `model` command.

The `model` command applies the differences from a diff artifact to the schema model.

**Note:** Whilst the schema model can be updated by hand, it is recommended to use the `diff` and `model` commands
to ensure that any changes made are valid and applied correctly.

For example, the following commands generate a diff between a dev environment and the schema model folder.
All the differences are then applied to the schema model folder using the `model` command.
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

We can see from the output above that the `inventory` table in the `sakila` schema has been updated.
This change is reflected in the `inventory.rgm` file in the schema model folder.